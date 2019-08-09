package com.uas.applocation.base;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.AreaUtils;

@Deprecated
public class BDLocationManager implements BaseLocationManager {
    private final LocationClient mLocationClient;
    private int mFaildCount;//错误次数
    private UASLocation mUASLocation;
    private OnLocationListener mOnLocationListener;//临时回调器
    private boolean isUpdateLocation;

    public BDLocationManager(Context ct) {
        // 声明LocationClient类
        mLocationClient = new LocationClient(ct);
        mLocationClient.registerLocationListener(mLocationListener); // 注册监听函数
        mLocationClient.setLocOption(getOptionNotGPS());
    }

    //设置配置
    private LocationClientOption getOptionNotGPS() {
        LocationClientOption option = getOptionByGPS();
        option.setOpenGps(false);
        return option;
    }

    //设置配置
    private LocationClientOption getOptionByGPS() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
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


    private BDLocationListener mLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            try {
                int locationWhere = location.getLocationWhere();
                if (locationWhere == BDLocation.LOCATION_WHERE_OUT_CN) {
                    //判断到是海外的情况下关闭百度地图manager
                    //当再次进行初始化的时候，ct必然是有值的
                    release();
                    AreaUtils.type=UASLocation.TYPE_NATIVE;
                    UasLocationHelper.getInstance().initConfig(null);
                } else {
                    setLocation(location);
                }
            } catch (Exception e) {
                clearLocation();
            }
        }
    };

    private void clearLocation() {
        if (mUASLocation != null) {
            mUASLocation.clear();
        }
    }

    private void setLocation(BDLocation location) throws Exception {
        if (location.getLocType() == BDLocation.TypeGpsLocation// GPS定位结果
                || location.getLocType() == BDLocation.TypeNetWorkLocation//网络定位
                || location.getLocType() == BDLocation.TypeOffLineLocation//离线定位（未验证离线定位的有效性）
                ) {
            //定位成功
            mFaildCount = 0;
            mLocationClient.stop();
            if (mUASLocation == null) {
                mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU);
            } else {
                clearLocation();
            }
            mUASLocation.setLocationOk(true);
            mUASLocation.setLatitude(location.getLatitude());
            mUASLocation.setLongitude(location.getLongitude());
            mUASLocation.setProvince(location.getProvince());
            mUASLocation.setCityName(location.getCity());
            mUASLocation.setAddress(location.getAddrStr());
            mUASLocation.setName(location.getLocationDescribe());
            isUpdateLocation = true;
            if (mOnLocationListener!=null){
                mOnLocationListener.onReceiveLocation(mUASLocation);
            }
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
            if (mFaildCount < 3) {
                mFaildCount++;
                requestLocation(mOnLocationListener);
            }
        }
    }


    @Override
    public void release() {
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }


    @Override
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

    @Override
    public boolean isLocationUpdate() {
        return isUpdateLocation;
    }

    @Override
    public UASLocation getUASLocation() {
        return mUASLocation == null ? mUASLocation = new UASLocation(UASLocation.TYPE_BAIDU) : mUASLocation;
    }
}
