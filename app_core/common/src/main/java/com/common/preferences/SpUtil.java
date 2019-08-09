package com.common.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.common.config.BaseConfig;

/**
 * 封装SharedPreferences，具体实现需要继承
 * Created by Bitliker on 2017/1/4.
 */
public abstract class SpUtil {


    protected abstract String getName();

    protected Context getContext() {
        return BaseConfig.getContext();
    }

    private SharedPreferences getPreferences() {
        return getContext().getSharedPreferences(getName(), Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return getPreferences().edit();
    }


    public int getInt(String key, int defValues) {
        return getPreferences().getInt(key, defValues);
    }

    public String getString(String key, String defValues) {
        return getPreferences().getString(key, defValues);
    }

    public String getString(String key) {
        return getPreferences().getString(key, "");
    }

    public boolean getBoolean(String key, boolean defValues) {
        return getPreferences().getBoolean(key, defValues);
    }

    public float getFloat(String key, float defValues) {
        return getPreferences().getFloat(key, defValues);
    }

    public long getLong(String key, long defValues) {
        return getPreferences().getLong(key, defValues);
    }

    public void put(String key, int values) {
        getEditor().putInt(key, values).apply();
    }

    public void put(String key, String values) {
        getEditor().putString(key, values).apply();
    }

    public void put(String key, float values) {
        getEditor().putFloat(key, values).apply();
    }

    public void put(String key, boolean values) {
        getEditor().putBoolean(key, values).apply();
    }

    public void put(String key, long values) {
        getEditor().putLong(key, values).apply();
    }

    public void remove(String... keys) {
        if (keys != null && keys.length > 0) {
            for (String key : keys) {
                getEditor().remove(key).apply();
            }
        }
    }

    public void clear() {
        getEditor().clear().apply();
    }

}
