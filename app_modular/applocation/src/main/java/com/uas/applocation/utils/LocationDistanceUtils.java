package com.uas.applocation.utils;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;

import java.text.DecimalFormat;

/**
 * 计算距离工具类
 */
public class LocationDistanceUtils {

    /**
     * 判断两个经纬度之间的距离
     *
     * @param a
     * @param b
     * @return
     */
    public static float getDistance(LatLng a, LatLng b) {
        try {
            return Float.valueOf(getDistanceStr(a,b));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getDistance(double a1, double a2, double b1, double b2) {
        LatLng a = new LatLng(a1, a2);
        LatLng b = new LatLng(b1, b2);
        return getDistanceStr(a, b);
    }


    public static String getDistanceStr(LatLng a, LatLng b) {
        double distance = Math.abs(DistanceUtil.getDistance(a, b));
        DecimalFormat df = new DecimalFormat(".##");
        return df.format(distance);
    }


    /**
     * 判断距离我当前的距离
     *
     * @param a
     * @return
     */
    public static float distanceMe(LatLng a) {
        String distance = distanceMeStr(a);
        try {
            return Float.valueOf(distance);
        } catch (ClassCastException e) {
            return -1f;
        } catch (Exception e) {
            return -1f;
        }
    }

    /**
     * 获取距离，将第一个参数的经纬度反过来
     * @param a
     * @param b
     * @return
     */
    public static float getDistanceBackFrist(LatLng a, LatLng b) {
        try {
            return Float.valueOf(getDistanceStr(new LatLng(a.longitude,a.latitude),b));
        } catch (Exception e) {
            return 0;
        }
    }
    public static float distanceMeBack(LatLng a) {
        if (a==null)return -1;
        String distance = distanceMeStr(new LatLng(a.longitude,a.latitude));
        try {
            return Float.valueOf(distance);
        } catch (ClassCastException e) {
            return -1f;
        } catch (Exception e) {
            return -1f;
        }
    }
    public static String distanceMeStr(LatLng a) {
        if (a == null) return "";
        UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();
        if (mUASLocation.getLatitude() == 0 || mUASLocation.getLongitude() == 0) return "";
        try {
            double distance = Math.abs(DistanceUtil.getDistance(a, mUASLocation.getLocation()));
            DecimalFormat df = new DecimalFormat(".##");
            return df.format(distance);
        } catch (Exception e) {
            return "";
        }
    }
}
