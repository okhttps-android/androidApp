package com.modular.apputils.utils;

import com.core.app.MyApplication;
import com.core.utils.CommonUtil;

public class ImageViewUtils {

    public static String getErpImageUrl(String path) {
        return CommonUtil.getAppBaseUrl(MyApplication.getInstance()) + "common/download.action?path=" + path + "&sessionId=" +
                CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username") +
                "&master=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
    }

    public static String getErpImageUrl(int id) {
        return CommonUtil.getAppBaseUrl(MyApplication.getInstance()) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId") +
                "&sessionUser=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username") +
                "&master=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
    }
}
