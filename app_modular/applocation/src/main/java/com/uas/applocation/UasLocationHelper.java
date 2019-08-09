package com.uas.applocation;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.base.BaseLocationManager;
import com.uas.applocation.base.UASLocationManager;
import com.uas.applocation.model.UASLocation;

public class UasLocationHelper implements BaseLocationManager {

    public static final String ACTION_LOCATION_CHANGE = "action_location_Change";//位置改变发送广播
    private static UasLocationHelper instance;
    private final Intent broadcast = new Intent(ACTION_LOCATION_CHANGE);//更新后广播
    private Context ct;
    private UASLocationManager mLocationHelper = null;

    public static UasLocationHelper getInstance() {
        if (instance == null) {
            synchronized (UasLocationHelper.class) {
                if (instance == null) {
                    instance = new UasLocationHelper();
                }
            }
        }
        return instance;
    }


    public UASLocationManager getLocationManager() {
        return mLocationHelper;
    }

    public void initConfig(Context ct) {
        if (ct != null) {
            this.ct = ct.getApplicationContext();
        }
        mLocationHelper = new UASLocationManager(ct);
        if (mLocationHelper != null) {
            mLocationHelper.requestLocation(mOnLocationListener);
        }
    }

    public void setTest(boolean mOutChina) {
        mLocationHelper.setOutChina(mOutChina);
    }

    public boolean isOutChina() {
        return mLocationHelper != null && mLocationHelper.isOutChina();
    }

    @Override
    public void release() {
        if (mLocationHelper != null) {
            mLocationHelper.release();
        }
    }

    @Override
    public boolean isLocationUpdate() {
        return mLocationHelper == null ? false : mLocationHelper.isLocationUpdate();
    }

    public void requestLocation() {
        requestLocation(null);
    }

    /**
     * @param mOnLocationListener 当外界传进来时候，不会走通用广播
     */
    @Override
    public void requestLocation(OnLocationListener mOnLocationListener) {
        if (mLocationHelper != null) {
            mLocationHelper.requestLocation(mOnLocationListener == null ? this.mOnLocationListener : mOnLocationListener);
        }
    }

    @Override
    public UASLocation getUASLocation() {
        return mLocationHelper == null ? new UASLocation(UASLocation.TYPE_BAIDU) : mLocationHelper.getUASLocation();
    }

    private OnLocationListener mOnLocationListener = new OnLocationListener() {
        @Override
        public void onReceiveLocation(UASLocation mUASLocation) {
            //发送广播
            broadcast.putExtra(ACTION_LOCATION_CHANGE, mUASLocation == null ? false : mUASLocation.isLocationOk());
            LocalBroadcastManager.getInstance(ct).sendBroadcast(broadcast);
        }
    };
}
