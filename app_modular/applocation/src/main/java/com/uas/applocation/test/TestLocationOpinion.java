package com.uas.applocation.test;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class TestLocationOpinion {

    private LocationManager mLocationManager;
    private Context mContext;
    private Location mlocation;

    private boolean mbUpdate;

    public TestLocationOpinion(Context mContext) {
        this.mContext = mContext;
        this.mlocation = new Location(LocationManager.GPS_PROVIDER);
    }

    //启动模拟位置服务
    public boolean initLocation() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        try {
            //如果未开启模拟位置服务，则添加模拟位置服务
            mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 0, 5);
            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //停止模拟位置服务
    public void stopMockLocation() {
        mbUpdate = false;
        if (mLocationManager != null) {
            try {
                mLocationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
                mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                Log.e("GPS", e.toString());
            }
        }
    }

    private Bundle bundle = new Bundle();
    double testData = 0.0;

    public void asynTaskUpdateCallBack() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mbUpdate) {
                    //测试的location数据
                    mlocation.setLongitude(testData++);
                    mlocation.setLatitude(testData++);
                    mlocation.setAltitude(testData++);
                    mlocation.setTime(System.currentTimeMillis());
                    mlocation.setBearing((float) 1.2);
                    mlocation.setSpeed((float) 1.2);
                    mlocation.setAccuracy((float) 1.2);
                    //额外的自定义数据，使用bundle来传递
                    bundle.putString("test1", "666");
                    bundle.putString("test2", "66666");
                    mlocation.setExtras(bundle);
                    try {
                        if (Build.VERSION.SDK_INT >= 17) {
                            mlocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        }
                        mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, 100, bundle, System.currentTimeMillis());
                        mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mlocation);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        return;
                    }
                }

            }
        }).start();
    }
}
