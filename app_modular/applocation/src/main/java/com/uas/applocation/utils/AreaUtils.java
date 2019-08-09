package com.uas.applocation.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.uas.applocation.model.UASLocation;

import java.util.Locale;

/**
 * 判读当前所在的地区
 */
public class AreaUtils {
    public static int type = UASLocation.TYPE_BAIDU;

    /**
     * 判断当前的卡是否是国内的卡，如果是国内的开，是不能使用google服务的
     *
     * @param context
     */
    public static void initArea(Context context) {
        int ty = getType(context);
        if (ty == 0) {
            type = isCN(context) || isChinaSimCard(context) ? UASLocation.TYPE_BAIDU : UASLocation.TYPE_NATIVE;
        } else {
            type = ty;
        }
    }

    public static int getType(Context ct) {
        SharedPreferences sPreferences = ct.getSharedPreferences("area", Context.MODE_PRIVATE);
        return sPreferences.getInt("type", 0);
    }

    public static void setType(Context ct, int intV) {
        SharedPreferences sPreferences = ct.getSharedPreferences("area", Context.MODE_PRIVATE);
        sPreferences.edit().putInt("type", intV).apply();
    }


    public static int getLocationServiceType() {
        Log.i("gong","getLocationServiceType="+type);
        return type;
    }

    /**
     * 判断国家是否是国内用户
     * <p>
     * 方法一
     *
     * @return
     */
    public static boolean isCN(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = tm.getSimCountryIso();
        boolean isCN = false;//判断是不是大陆
        if (!TextUtils.isEmpty(countryIso)) {
            countryIso = countryIso.toUpperCase(Locale.US);
            if (countryIso.contains("CN")) {
                isCN = true;
            }
        }
        return isCN;

    }

    /**
     * 查询手机的 MCC+MNC
     */
    private static String getSimOperator(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return tm.getSimOperator();
        } catch (Exception e) {

        }
        return null;
    }


    /**
     * 因为发现像华为Y300，联想双卡的手机，会返回 "null" "null,null" 的字符串
     */
    private static boolean isOperatorEmpty(String operator) {
        if (operator == null) {
            return true;
        }
        if (operator.equals("") || operator.toLowerCase(Locale.US).contains("null")) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否是国内的 SIM 卡，优先判断注册时的mcc
     */
    public static boolean isChinaSimCard(Context c) {
        String mcc = getSimOperator(c);
        if (isOperatorEmpty(mcc)) {
            return true;//默认是国内
        } else {
            return mcc.startsWith("460");
        }
    }


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        Log.i("gong", "gpsgpsgps=" + gps);
        if (gps) {
            return true;
        }
        return false;
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    public static final void openGPS(Context context) {
        Log.i("gong", "openGPS");
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


}
