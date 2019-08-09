package com.uas.applocation.utils;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

/**
 * 坐标系转换
 */
public class CoordinateUtils {

    /**
     * 将制定坐标系经纬度转换成百度坐标
     *
     * @param sourceLatLng 原始经纬度
     * @param var1         原始经纬度来源
     * @return
     */
    public static LatLng common2Baidu(LatLng sourceLatLng, CoordinateConverter.CoordType var1) {
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(var1);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        return converter.convert();
    }


    public static LatLng gps2Baidu(LatLng sourceLatLng) {
        return common2Baidu(sourceLatLng, CoordinateConverter.CoordType.GPS);
    }

    /**
     * 别的地图sdk转成百度经纬度
     *
     * @param sourceLatLng
     * @return
     */
    public static LatLng common2Baidu(LatLng sourceLatLng) {
        return common2Baidu(sourceLatLng, CoordinateConverter.CoordType.COMMON);
    }
}
