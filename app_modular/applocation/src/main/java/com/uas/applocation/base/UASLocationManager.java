package com.uas.applocation.base;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.CoordinateUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 定位管理器，负责定位相关
 */
public class UASLocationManager {
    private final String COOR_TYPE_BD = "bd09ll";
    private final String COOR_TYPE_WGS = "WGS84";
    private final String COOR_TYPE_GCJ = "gcj02";

    private boolean isOutChina = false;
    private final LocationClient mLocationClient;
    private int mFaildCount;//错误次数
    private UASLocation mUASLocation;
    private OnLocationListener mOnLocationListener;//临时回调器
    private boolean isUpdateLocation;
    private Context ct;

    public UASLocationManager(Context ct) {
        // 声明LocationClient类
        this.ct = ct.getApplicationContext();
        mLocationClient = new LocationClient(ct);
        mLocationClient.registerLocationListener(mLocationListener); // 注册监听函数
        updateOption();
    }

    private void updateOption() {
        LocationClientOption option = null;
        if (isOutChina) {
            option = getOptionByGPS();
            option.setCoorType(COOR_TYPE_WGS);
        } else {
            option = getOptionByGPS();
            option.setOpenGps(false);
            option.setCoorType(COOR_TYPE_BD);
        }
        mLocationClient.setLocOption(option);

    }

    //设置配置，使用gps
    private LocationClientOption getOptionByGPS() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
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

    private void clearLocation() {
        if (mUASLocation != null) {
            mUASLocation.clear();
        }
    }

    private BDLocationListener mLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                if (isLocationOk(location)) {
                    mFaildCount = 0;
                    mLocationClient.stop();
                    isUpdateLocation = true;
                    int locationWhere = location.getLocationWhere();
                    if (locationWhere == BDLocation.LOCATION_WHERE_OUT_CN) {
                        //国外
                        if (isOutChina) {
                            setLocation(location);
                        } else {
                            isOutChina = true;
                            updateOption();
                            requestLocation(mOnLocationListener);
                        }
                    } else {
                        //国内
                        setLocation(location);
                    }
                } else {
                    if (mUASLocation == null) {
                        mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU);
                    } else {
                        clearLocation();
                    }
                    //统一为定位失败
                    mUASLocation.setLocationOk(false);
                    String errorMessage = getLocationError(location);
                    mUASLocation.setRemarks(errorMessage);
                    if (mFaildCount < 3) {
                        mFaildCount++;
                        requestLocation(mOnLocationListener);
                    }
                }

            } catch (Exception e) {
                clearLocation();
            }
        }
    };

    public void setOutChina(boolean outChina) {
        isOutChina = outChina;
    }

    public boolean isOutChina() {
        return isOutChina;
    }

    //关闭定位
    public void release() {
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    //刷新定位
    public void requestLocation(OnLocationListener mOnLocationListener) {
        if (mOnLocationListener != null) {
            this.mOnLocationListener = mOnLocationListener;
        }
        if (mLocationClient == null) return;
        isUpdateLocation = false;
        if (!mLocationClient.isStarted()) {
            mFaildCount = 0;
            mLocationClient.start();
        } else {
            mLocationClient.requestLocation();
        }
    }

    //是否更新定位
    public boolean isLocationUpdate() {
        return isUpdateLocation;
    }

    public UASLocation getUASLocation() {
        return mUASLocation == null ? mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU) : mUASLocation;
    }


    private boolean isLocationOk(BDLocation location) {
        return location.getLocType() == BDLocation.TypeGpsLocation// GPS定位结果
                || location.getLocType() == BDLocation.TypeNetWorkLocation//网络定位
                || location.getLocType() == BDLocation.TypeOffLineLocation//离线定位（未验证离线定位的有效性）
                ;
    }

    private String getLocationError(BDLocation location) {
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
        return errorMessage;
    }

    private void setLocation(BDLocation location) throws Exception {
        //定位成功
        if (mUASLocation == null) {
            mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU);
        } else {
            clearLocation();
        }
        String CoorType = location.getCoorType();
        LatLng mLatlng = new LatLng(location.getLatitude(), location.getLongitude());
        changeCoord(CoorType, mLatlng);
        mUASLocation.setLocationOk(true);
        mUASLocation.setProvince(location.getProvince());
        mUASLocation.setCityName(location.getCity());
        mUASLocation.setAddress(location.getAddrStr());
        mUASLocation.setName(location.getLocationDescribe());
        if (!mUASLocation.isLegalAble() && (TextUtils.isEmpty(CoorType) || !COOR_TYPE_BD.toUpperCase().equals(CoorType.toUpperCase()))) {
            analysisGps(mUASLocation.getGpsLatitude(), mUASLocation.getGpsLongitude());
        }
        if (mOnLocationListener!=null){
            mOnLocationListener.onReceiveLocation(mUASLocation);
        }
    }

    private void analysisGps(double latitude, double longitude) {
        Geocoder gc = new Geocoder(ct, Locale.getDefault());
        List<Address> locationList = null;
        try {
            locationList = gc.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (locationList != null && locationList.size() > 0) {
            isUpdateLocation = true;
            Address mAddress = locationList.get(0);
            if (mAddress != null) {
                mUASLocation.setProvince(mAddress.getLocality());
                mUASLocation.setCityName(mAddress.getSubAdminArea());
                mUASLocation.setCountry(mAddress.getCountryName());
                mUASLocation.setName(mAddress.getFeatureName());
                mUASLocation.setDistrict(mAddress.getThoroughfare());
                mUASLocation.setAddress(mAddress.getAddressLine(0));
            }
        }
    }


    private void changeCoord(String type, LatLng mLatLng) {
        if (mLatLng == null) return;
        LatLng latlng = null;

        if (!TextUtils.isEmpty(type)) {
            if (COOR_TYPE_BD.toUpperCase().equals(type.toUpperCase())) {//百度坐标
                latlng = mLatLng;
            } else if (COOR_TYPE_GCJ.toUpperCase().equals(type.toUpperCase())) {//国测局坐标
                latlng = CoordinateUtils.common2Baidu(mLatLng);
            } else {
                latlng = CoordinateUtils.gps2Baidu(mLatLng);
            }
        } else {
            latlng = CoordinateUtils.gps2Baidu(mLatLng);
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
}
