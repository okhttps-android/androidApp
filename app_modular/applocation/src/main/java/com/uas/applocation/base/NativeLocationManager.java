package com.uas.applocation.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.Interface.OnSearchLocationListener;
import com.uas.applocation.R;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.CoordinateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 本地定位，只负责定位
 */
@Deprecated
public class NativeLocationManager implements BaseLocationManager {
    private final String DEF_OUT_CHS_COOE_TYPE = "bd09ll";//bd09ll  //WGS84
    private final Logger MLOGGER = Logger.getLogger("NativeLocationManager");

    private static final String TAG = "NativeLocationManager";
    private UASLocation mUASLocation;
    private OnLocationListener mOnLocationListener;//临时回调器
    private boolean isUpdateLocation;
    private LocationManager mLocationManager;
    private Context ct;
    private HttpClient mHttpClient;
    private LocationClient mLocationClient;
    private Handler mMainHandler = null;

    public NativeLocationManager(Context ct) {
        if (ct != null) {
            mMainHandler = new Handler(Looper.getMainLooper());
            this.ct = ct.getApplicationContext();
            mLocationManager = (LocationManager) ct.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            initBaiduManager();
        }
    }

    /**
     * 配置
     * 百度地图SDK在国内（包括港澳台）使用的是BD09坐标（定位SDK默认使用GCJ02坐标）；
     * 在海外地区，统一使用WGS84坐标。开发者在使用百度地图相关服务时，请注意选择
     *
     * @return
     */
    private LocationClientOption getOptionByGPS() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(DEF_OUT_CHS_COOE_TYPE);//可选，默认gcj02，设置返回的定位结果坐标系 bd09ll
        option.setScanSpan(1);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(true);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setNeedDeviceDirect(false);
        return option;
    }

    private void initBaiduManager() {
        mLocationClient = new LocationClient(ct);
        mLocationClient.registerLocationListener(mLocationListener); // 注册监听函数
        mLocationClient.setLocOption(getOptionByGPS());
    }


    /**
     * 获取定位条件
     *
     * @param locationManager
     * @return
     */
    private String getWellProvider(LocationManager locationManager) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费流量
        criteria.setPowerRequirement(Criteria.POWER_HIGH);//低功耗
        String provider = locationManager.getBestProvider(criteria, true); // true 代表从打开的设备中查找
        List<String> providerList = locationManager.getProviders(true);
        // 测试一般都在室内，这里颠倒了书上的判断顺序
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        }
        return provider;
    }

    public void setLocation(int type, String coord, Address address) {
        if (mUASLocation == null) {
            mUASLocation = new UASLocation(type);
        } else if (type == mUASLocation.getType() || mUASLocation.getType() == UASLocation.TYPE_NATIVE || !mUASLocation.isLegalAble()) {
            clearLocation();
            mUASLocation.setType(type);
            mUASLocation.setLocationOk(true);
            mUASLocation.setProvince(address.getLocality());
            mUASLocation.setCityName(address.getSubAdminArea());
            mUASLocation.setCountry(address.getCountryName());
            mUASLocation.setName(address.getFeatureName());
            mUASLocation.setDistrict(address.getThoroughfare());
            mUASLocation.setAddress(address.getAddressLine(0));
            changeCoord(coord, new LatLng(address.getLatitude(), address.getLongitude()));
            if (mOnLocationListener != null) {
                mOnLocationListener.onReceiveLocation(mUASLocation);
            }
            release();
        }
    }

    private void clearLocation() {
        if (mUASLocation != null) {
            mUASLocation.clear();
        }
    }

    @Override
    public void release() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(locationListener);
        }
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }


    @Override
    public void requestLocation(final OnLocationListener mOnLocationListener) {
        if (mLocationManager == null) {
            return;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            requestLocationUI(mOnLocationListener);
        } else {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    requestLocationUI(mOnLocationListener);
                }
            });
        }

        if (mLocationClient != null) {
            if (!mLocationClient.isStarted()) {
                mLocationClient.start();
            } else {
                mLocationClient.requestLocation();
            }
        }
    }

    private void requestLocationUI(OnLocationListener mOnLocationListener) {
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        this.mOnLocationListener = mOnLocationListener;
        isUpdateLocation = false;
        String provider = getWellProvider(mLocationManager);
        //刷新定位
        mLocationManager.requestLocationUpdates(provider, 10000, 0, locationListener);
    }

    @Override
    public boolean isLocationUpdate() {
        return isUpdateLocation;
    }

    @Override
    public UASLocation getUASLocation() {
        return mUASLocation == null ? mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU) : mUASLocation;
    }


    private BDLocationListener mLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                String CoorType = location.getCoorType();
                Log.i(TAG, "CoorType=" + CoorType);
                String name = location.getLocationDescribe();
                String address = location.getAddrStr();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(address) && latitude * longitude != 0) {
                    setLocation(CoorType, location);
                } else if (latitude * longitude != 0) {
                    Geocoder gc = new Geocoder(ct, Locale.getDefault());
                    List<Address> locationList = null;
                    try {
                        locationList = gc.getFromLocation(latitude, longitude, 1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (locationList != null && locationList.size() > 0) {
                        isUpdateLocation = true;
                        setLocation(UASLocation.TYPE_BAIDU, CoorType, locationList.get(0));
                    }
                }
            } catch (Exception e) {
                clearLocation();
            }
        }
    };
    private LocationListener locationListener = new LocationListener() {
        //当位置改变的时候调用
        @Override
        public void onLocationChanged(Location location) {
            //经度
            double longitude = location.getLongitude();
            //纬度
            double latitude = location.getLatitude();
            //海拔
            double altitude = location.getAltitude();
            Geocoder gc = new Geocoder(ct, Locale.getDefault());
            List<Address> locationList = null;
            try {
                locationList = gc.getFromLocation(latitude, longitude, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (locationList != null && locationList.size() > 0) {
                isUpdateLocation = true;
                Address address = locationList.get(0);//得到Address实例
                setLocation(UASLocation.TYPE_NATIVE, "gps", address);
            }
        }

        //当GPS状态发生改变的时候调用
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String message = null;
            switch (status) {
                case LocationProvider.AVAILABLE:
                    message = "当前GPS为可用状态!";
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    message = "当前GPS不在服务内!";
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    message = "当前GPS为暂停服务状态!";
                    break;
            }
            Log.i(TAG, "message=" + message);
        }

        //GPS开启的时候调用
        @Override
        public void onProviderEnabled(String provider) {

        }

        //GPS关闭的时候调用
        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    private void setLocation(String type, BDLocation location) throws Exception {
        if (location.getLocType() == BDLocation.TypeGpsLocation// GPS定位结果
                || location.getLocType() == BDLocation.TypeNetWorkLocation//网络定位
                || location.getLocType() == BDLocation.TypeOffLineLocation//离线定位（未验证离线定位的有效性）
                ) {
            //定位成功
            mLocationClient.stop();
            if (mUASLocation == null) {
                mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU);
            } else {
                clearLocation();
            }
            mUASLocation.setLocationOk(true);
            changeCoord(type, new LatLng(location.getLatitude(), location.getLongitude()));
            mUASLocation.setProvince(location.getProvince());
            mUASLocation.setCityName(location.getCity());
            mUASLocation.setAddress(location.getAddrStr());
            mUASLocation.setName(location.getLocationDescribe());
            isUpdateLocation = true;
        } else {
            if (mUASLocation == null) {
                mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU);
            } else {
                clearLocation();
            }
            //统一为定位失败
            mUASLocation.setLocationOk(false);
            String errorMessage = null;
            if (location.getLocType() == BDLocation.TypeServerError) {
                //服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因
                errorMessage = "服务端网络定位失败";
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                //网络不同导致定位失败，请检查网络是否通畅
                errorMessage = "网络不同导致定位失败，请检查网络是否通畅";
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                //无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机
                errorMessage = "无法获取有效定位依据导致定位失败";
            } else {
                errorMessage = "未知错误";
            }
            mUASLocation.setRemarks(errorMessage);
        }
    }


    private void changeCoord(String type, LatLng mLatLng) {
        if (mLatLng == null) return;
        LatLng latlng = null;
        if (type != null && !"BD09LL".equals(type.toUpperCase())) {
            if (type.toUpperCase().equals("GCJ02")) {
                latlng = CoordinateUtils.common2Baidu(mLatLng);
            } else {
                latlng = CoordinateUtils.gps2Baidu(mLatLng);
            }
        }
        mUASLocation.setGpsLatitude(mLatLng.latitude);
        mUASLocation.setGpsLongitude(mLatLng.longitude);
        if (latlng == null) {
            mUASLocation.setLatitude(mLatLng.latitude);
            mUASLocation.setLongitude(mLatLng.longitude);
        } else {
            mUASLocation.setLatitude(latlng.latitude);
            mUASLocation.setLongitude(latlng.longitude);
        }
    }


    private HttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new HttpClient.Builder("https://maps.googleapis.com/maps/api/place/").isDebug(false)
                    .connectTimeout(5000)
                    .readTimeout(5000).build();
        }
        return mHttpClient;
    }


    /**
     * 获取当前位置附近位置
     *
     * @param ct
     * @param latitude
     * @param longitude
     * @param mOnSearchLocationListener
     */
    public void loadNativeByNeer(final Context ct, double latitude, double longitude, float radius, final OnSearchLocationListener mOnSearchLocationListener) {
        HttpClient request = new HttpClient.Builder()
                .url("nearbysearch/json")
                .isDebug(true)
                .add("location", latitude + "," + longitude)
                .add("radius", radius)
                .add("keyword", "公司")
                .add("output", "json")
                .method(Method.GET)
                .add("key", ct.getString(R.string.app_google_key))
                .build();
        getHttpClient().Api().send(request, new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object s) {
                try {
                    JSONObject object = JSON.parseObject(s.toString());
                    if (object != null
                            && object.containsKey("status") && "OK".equals(object.getString("status").toUpperCase())
                            && object.containsKey("results") && mOnSearchLocationListener != null) {
                        handlerNeer(object.getJSONArray("results"), mOnSearchLocationListener);
                    }
                    MLOGGER.log(Level.INFO, s.toString());
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Object t) {
                MLOGGER.log(Level.INFO, t.toString());
            }
        }));
    }


    /**
     * 通过输入进行搜索
     *
     * @param ct
     * @param city
     * @param keyWord
     * @param mOnSearchLocationListener
     */
    public void loadByInput(Context ct, String city, String keyWord, final OnSearchLocationListener mOnSearchLocationListener) {
        if (ct == null || mOnSearchLocationListener == null)
            return;
        HttpClient.Builder requestBuilder = new HttpClient.Builder()
                .isDebug(true)
                .url("textsearch/json")
                .add("query", keyWord)
                .method(Method.GET)
                .add("key", ct.getString(R.string.app_google_key));
        HttpClient request = requestBuilder.build();
        MLOGGER.log(Level.INFO, "发送请求");
        getHttpClient().Api().send(request, new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object s) {
                try {
                    JSONObject object = JSON.parseObject(s.toString());
                    if (object != null
                            && object.containsKey("status") && "OK".equals(object.getString("status").toUpperCase())
                            && object.containsKey("results") && mOnSearchLocationListener != null) {
                        handlerNeer(object.getJSONArray("results"), mOnSearchLocationListener);
                    }
                    MLOGGER.log(Level.INFO, s.toString());
                } catch (Exception e) {

                } finally {
                }
            }

            @Override
            public void onFailure(Object t) {
                MLOGGER.log(Level.INFO, t.toString());
            }
        }));
    }


    private void handlerNeer(JSONArray array, OnSearchLocationListener mOnSearchLocationListener) throws Exception {
        JSONObject object;
        UASLocation mUASLocation;
        List<UASLocation> mUASLocations = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            mUASLocation = new UASLocation(UASLocation.TYPE_NATIVE);
            String name = object.getString("name");
            String address = null;
            if (object.containsKey("vicinity")) {
                address = object.getString("vicinity");
            }
            if (TextUtils.isEmpty(address) && object.containsKey("formatted_address")) {
                address = object.getString("formatted_address");
            }
//            String province = object.getString("province");
            String city = object.getString("city");//TODO 城市
//            String area = object.getString("area");
            mUASLocation.setName(name);
            mUASLocation.setAddress(address);
            mUASLocation.setCityName(city);
            JSONObject geometry = object.getJSONObject("geometry");
            if (geometry != null) {
                JSONObject location = geometry.getJSONObject("location");
                if (location != null) {
                    float lat = location.getFloat("lat");
                    float lng = location.getFloat("lng");
                    if (lat > 0 && lng > 0) {
                        mUASLocation.setLatitude(lat);
                        mUASLocation.setLongitude(lng);
                    }
                }
            }
            mUASLocations.add(mUASLocation);
        }
        mOnSearchLocationListener.onCallBack(true, mUASLocations);
    }


}
