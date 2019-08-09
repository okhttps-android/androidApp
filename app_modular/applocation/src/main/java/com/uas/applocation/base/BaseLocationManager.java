package com.uas.applocation.base;

import com.uas.applocation.Interface.OnLocationListener;
import com.uas.applocation.model.UASLocation;

public interface BaseLocationManager {
    /**
     *关闭定位
     */
    void release();

    /**
     * 是否已经更新
     *
     * @return
     */
    boolean isLocationUpdate();

    /**
     * 重新刷新定位
     *
     * @param mOnLocationListener
     */
    void requestLocation(OnLocationListener mOnLocationListener);

    /**
     * 获取位置信息
     *
     * @return
     */
    UASLocation getUASLocation();

}
