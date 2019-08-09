package com.core.utils;

import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.model.WorkModel;

import java.util.ArrayList;

/**
 * Created by Bitliker on 2017/3/23.
 */

public class CommonInterfaceHandler {

    public static boolean saveWorkData(JSONObject object, Bundle bundle) throws Exception {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        ArrayList<WorkModel> models = WorkHandlerUtil.handlerWorkData(object, isB2b);
        bundle.putParcelableArrayList("models", models);
        WorkHandlerUtil.handerLocation(object, isB2b);
        String days = JSONUtil.getText(object, "wd_day", "day");
        String name = JSONUtil.getText(object, "wd_name", "name");
        bundle.putString("days", days);
        bundle.putString("name", name);
        return true;
    }


    /*保存管理员状态*/
    public static boolean saveMainStatus(JSONObject object) throws Exception {
        boolean isAdmin = false;
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (isB2b)
            object = object.getJSONObject("data");
        if (object.containsKey("isAdmin")) {
            if (StringUtil.isEmpty(JSONUtil.getText(object, "isAdmin"))) {
                isAdmin = JSONUtil.getInt(object, "isAdmin") > 0;
            } else {
                String adminStatus = JSONUtil.getText(object, "isAdmin");
                isAdmin = Integer.valueOf(adminStatus) > 0;
            }
            PreferenceUtils.putBoolean(AppConfig.IS_ADMIN, isAdmin);
        }
        return isAdmin;
    }

    /*保存外勤设置*/
    public static boolean getOutSet(JSONObject o) {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        JSONObject object = null;
        if (!isB2b) {
            JSONArray array = o.getJSONArray("result");
            if (ListUtils.isEmpty(array)) {
                PreferenceUtils.putBoolean(AppConfig.AUTO_MISSION, false);
                PreferenceUtils.putBoolean(AppConfig.NEED_PROCESS, false);
                PreferenceUtils.putInt(AppConfig.ALARM_MISSION_DISTANCE, 0);
                PreferenceUtils.putInt(AppConfig.AUTO_MISSION_TIME, 0);
                PreferenceUtils.putInt(AppConfig.FACE_SIGN, -1);
                return false;
            }
            object = array.getJSONObject(0);
        } else object = o;
        int distance = JSONUtil.getInt(object, "mo_distance", "MO_DISTANCE");
        int time = JSONUtil.getInt(object, "mo_time", "MO_TIME");
        boolean isAuto = JSONUtil.getInt(object, "mo_autosign", "MO_AUTOSIGN") > 0;
        boolean needprocess = JSONUtil.getInt(object, "MO_NEEDPROCESS", "MO_NEEDPROCESS") > 0;
        int faceSign = -1;
        if (object.containsKey("MO_FACESIGN")) {
            faceSign = JSONUtil.getInt(object, "MO_FACESIGN");
        } else {
            faceSign = -1;
        }
        //获取到外勤设置   保存下来
        PreferenceUtils.putBoolean(AppConfig.AUTO_MISSION, isAuto);
        PreferenceUtils.putBoolean(AppConfig.NEED_PROCESS, needprocess);
        PreferenceUtils.putInt(AppConfig.ALARM_MISSION_DISTANCE, distance);
        PreferenceUtils.putInt(AppConfig.AUTO_MISSION_TIME, time);
        PreferenceUtils.putInt(AppConfig.FACE_SIGN, faceSign);
        return true;
    }


}
