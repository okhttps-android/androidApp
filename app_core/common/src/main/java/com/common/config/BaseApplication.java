package com.common.config;

import android.app.Application;


/**
 * Created by Bitliker on 2017/6/19.
 */
public abstract class BaseApplication extends com.me.network.app.base.BaseApplication {
    private static Application api = null;

    @Override
    public void onCreate() {
        super.onCreate();
        api = this;
        //初始化配置
        BaseConfig.init(this);
        try {
            initConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public abstract void initConfig() throws Exception;


    public static Application api() {
        return api;
    }
}
