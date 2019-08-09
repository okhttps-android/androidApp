package com.common.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.common.config.BaseConfig;

/**
 * SharedPreferences存储数据类
 */
public class PreferenceUtils {
    public interface Constants {
        String AUTO_SIGN_SW = "AUTO_SIGN_SW";//是否开启自动打卡
        String SAVE_SYSTEM_SCHEDULE = "saveSystemSchedule";//是否保存日程到系统
        String DEF_WARN_TIME = "DEF_WARN_TIME";//默认提醒时间设置
        String DEF_REPEAT_TIME = "DEF_repeat_TIME";//默认重复时间

    }


    private static SharedPreferences sp;

    private static SharedPreferences getPreferences(Context context) {

        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp;
    }

    /**
     * 获得boolean类型的信息,如果没有返回false
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static boolean getBoolean(String key, boolean vaule) {
        return getBoolean(BaseConfig.getContext(), key, vaule);
    }

    /**
     * 获得boolean类型的信息
     *
     * @param context
     * @param key
     * @param defValue ： 没有时的默认值
     * @return
     */
    public static boolean getBoolean(Context context, String key,
                                     boolean defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getBoolean(key, defValue);
    }

    /**
     * 设置boolean类型的 配置数据
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    /**
     * @param key
     * @param value
     */
    public static void putBoolean(String key, boolean value) {
        SharedPreferences sp = getPreferences(BaseConfig.getContext());
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    /**
     * 存储String的数据
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putString(key, value);//去定义一个常量
        edit.commit();
    }

    public static void putString(String key, String value) {
        SharedPreferences sp = getPreferences(BaseConfig.getContext());
        Editor edit = sp.edit();
        edit.putString(key, value);//去定义一个常量
        edit.commit();
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getString(key, defValue);
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = getPreferences(context);
        return getString(context, key, null);
    }

    public static String getString(String key) {
        return getString(BaseConfig.getContext(), key, "");
    }

    /**
     * 存储int型数据
     *
     * @param key
     * @param value
     */
    public static void putInt(String key, int value) {
        SharedPreferences sp = getPreferences(BaseConfig.getContext());
        Editor edit = sp.edit();
        edit.putInt(key, value);//去定义一个常量
        edit.commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getInt(key, defValue);
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = getPreferences(context);
        return getInt(context, key, -1);
    }

    public static int getInt(String key, int value) {
        return getInt(BaseConfig.getContext(), key, value);
    }

    /**
     * 存储long型数据
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putLong(Context context, String key, long value) {
        SharedPreferences sp = getPreferences(context);
        Editor edit = sp.edit();
        edit.putLong(key, value);//去定义一个常量
        edit.commit();
    }

    public static Long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = getPreferences(context);
        return sp.getLong(key, defValue);
    }

    public static Long getLong(String key, long defValue) {
        SharedPreferences sp = getPreferences(BaseConfig.getContext());
        return sp.getLong(key, defValue);
    }

    public static Long getLong(Context context, String key) {
        SharedPreferences sp = getPreferences(context);
        return getLong(context, key, 1l);
    }

    public static Float getFloat(String key) {
        return getFloat(key, -1);
    }

    public static Float getFloat(String key, float defValue) {
        SharedPreferences sp = getPreferences(BaseConfig.getContext());
        return sp.getFloat(key, defValue);
    }

    public static void putFloat(String key, float defValue) {
        try {
            SharedPreferences sp = getPreferences(BaseConfig.getContext());
            Editor edit = sp.edit();
            edit.putFloat(key, defValue);
            edit.commit();
        } catch (Exception e) {

        }
    }

    public static void remove(String key) {
        try {
            SharedPreferences sp = getPreferences(BaseConfig.getContext());
            Editor editor = sp.edit();
            editor.remove(key);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

