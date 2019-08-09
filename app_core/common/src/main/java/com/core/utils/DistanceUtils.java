package com.core.utils;

/**
 * Created by Arison on 2017/10/16.
 */

public class DistanceUtils {

    public static String mToKm(float f,String format){
        try {
            if(f>1000){
                return new java.text.DecimalFormat(format).format(f/1000)+"km";
            }else{
                return new java.text.DecimalFormat(format).format(f)+"m";
            }
        } catch (Exception e) {
            throw new RuntimeException("参数不合法");
        }
    }
}
