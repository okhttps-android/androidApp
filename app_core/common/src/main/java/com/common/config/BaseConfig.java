package com.common.config;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * 通用工具类
 * Created by Bitlier on 2017/6/9
 */
public class BaseConfig {
    private static Context context;
    private static boolean debug;
    private static boolean canShowTocat = true;

    private BaseConfig() {
    }

    /*初始化工具类*/
    public static void init(Application context) {
        BaseConfig.context = context.getApplicationContext();
        isApkInDebug();
    }

    /*判断当前应用是否是debug状态*/
    private static void isApkInDebug() {
        try {
            ApplicationInfo info = getContext().getApplicationInfo();
            debug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            debug = false;
        }
    }


    public static Context getContext() {
        if (context != null) return context;
        else BaseApplication.api();
        throw new NullPointerException("u should init first");
    }

    /*是否显示log*/
    public static boolean showLogAble() {
        return debug && canShowTocat;
    }

    public static boolean isDebug() {
        return debug;
    }
}
