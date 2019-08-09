package com.common.config;

import android.view.View;

import com.common.preferences.RedSpUtil;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;

/**
 * Created by Bitliker on 2017/9/14.
 */
public class VersionUtil {

    public static boolean showUUHelper() {
        return true;
    }

    public static boolean canShowCrm2_0() {
        if (!CommonUtil.getSharedPreferencesBoolean(MyApplication.getInstance(), "erp_login"))
            return false;
        if (BaseConfig.isDebug()) return false;
        //TODO default
        return false;
    }



}
