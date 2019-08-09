package com.core.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.app.MyApplication;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公用接口封装类
 * Created by Bitliker on 2016/12/30.
 */
public class CommonInterface implements OnHttpResultListener {
    private static CommonInterface instance;

    public static final int CODE_WHAT = 0x11;//获取code
    public static final int ID_WHAT = 0x12;//获取id
    public static final int OUT_SET_WHAT = 0x13;//获取外勤设置
    public static final int ADD_OUT_SET_WHAT = 0x14;//外勤设置
    public static final int LOAD_COMPANY_WHAT = 0x15;//外勤设置
    public static final int ISMAIN_WHAT = 0x16;//是否管理员
    public static final int LOAD_CONFIG_WHAT = 0x17;//获取高级设置
    public static final int WORK_DATA_WHAT = 0x18;//获取班次接口
    public static final int WORK_LOG_WHAT = 0x19;//获取班次打卡记录接口
    public static final int END_MISSION = 0x20;//更新外勤状态
    public static final int ADD_CONTACT = 0x21;//添加了联系人

    public static CommonInterface getInstance() {
        if (instance == null) {
            synchronized (CommonInterface.class) {
                instance = new CommonInterface();
            }
        }
        return instance;
    }


    /**
     * 获取服务端表的id
     *
     * @param sql              表名+"_sql"
     * @param onResultListener 回调
     */
    public void getIdByNet(String sql, OnResultListener onResultListener) {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) return;
        Map<String, Object> param = new HashMap<>();
        param.put("seq", sql);
        loadNet(ID_WHAT, "common/getId.action", param, new Bundle(), Request.Mode.GET, onResultListener);
    }

    /**
     * 获取服务端表的编号
     *
     * @param titleName        表名
     * @param onResultListener 回调
     */
    public void getCodeByNet(String titleName, OnResultListener onResultListener) {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) return;
        Map<String, Object> param = new HashMap<>();
        param.put("type", 2);
        param.put("caller", titleName);
        loadNet(CODE_WHAT, "common/getCodeString.action", param, new Bundle(), Request.Mode.GET, onResultListener);
    }


    /**
     * 获取外勤设置，判断是否是
     *
     * @param onResultListener 回调
     */
    public void getOutSetInfo(OnResultListener onResultListener) {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getOutSet : "mobile/getOutSetInfo.action";
        loadNet(OUT_SET_WHAT, url, new HashMap<String, Object>(), new Bundle(), Request.Mode.GET, onResultListener);
    }


    /**
     * 更新外勤设置
     *
     * @param distance         范围
     * @param time             预留时间
     * @param isAuto           是否开启自动外勤
     * @param faceSign         是否开启人脸打卡
     * @param onResultListener
     */
    public void addOutSet(int distance, int time, boolean isAuto, boolean needprocess, int faceSign, OnResultListener onResultListener) {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        if (isB2b) {
            formStore.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            formStore.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        } else param.put("caller", "OUTSET");

        formStore.put("mo_distance", distance);//距离
        formStore.put("mo_needprocess", needprocess ? 1 : 0);//距离
        formStore.put("mo_time", time);//预留时间
        formStore.put("mo_autosign", isAuto ? 1 : 0);//是否自动外勤
        if (faceSign != -1) {
            formStore.put("mo_facesign", faceSign);//是否人脸打卡
        }
        param.put("formStore", JSONUtil.map2JSON(formStore));
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveOutSet : "mobile/addOutSet.action";
        loadNet(ADD_OUT_SET_WHAT, url, param, new Bundle(), Request.Mode.POST, onResultListener);

    }

    /*获取企业架构数据*/
    public void loadCompanyData(OnResultListener onResultListener) {
        String master = CommonUtil.getMaster();
        Map<String, Object> param = new HashMap<>();
        param.put("master", master);
        param.put("lastdate", "");
        Bundle bundle = new Bundle();
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        bundle.putBoolean("isB2b", isB2b);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getUsersInfo : "mobile/getAllHrorgEmps.action";
        loadNet(LOAD_COMPANY_WHAT, url, param, bundle, Request.Mode.GET, onResultListener);
    }


    //判断是否管理员
    public void judgeManager(OnResultListener onResultListener) {
        Map<String, Object> param = new HashMap<>();
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getUserInfo : "mobile/ifadmin.action";
        param.put("emcode", CommonUtil.getEmcode());
        loadNet(ISMAIN_WHAT, url, param, new Bundle(), Request.Mode.GET, onResultListener);
    }

    //获取高级设置数据
    public void loadConfigs(OnResultListener onResultListener) {
        //获取考勤高级设置时间请求
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        Map<String, Object> param = new HashMap<>();
        if (!isB2b)
            param.put("code", 1);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().get_plat_senior_setting_url : "/mobile/getconfigs.action";
        loadNet(LOAD_CONFIG_WHAT, url, param, new Bundle(), Request.Mode.GET, onResultListener);
    }

    //获取班次数据
    private void loadWorkData(long time, OnResultListener onResultListener) {
        Map<String, Object> param = new HashMap<>();
        param.put("date", DateFormatUtil.long2Str(time, "yyyyMMdd"));
        Bundle bundle = new Bundle();
        bundle.putLong("time", time);
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (!isB2b)
            param.put("emcode", CommonUtil.getEmcode());
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_schedule_url : "mobile/getWorkDate.action";
        loadNet(WORK_DATA_WHAT, url, param, bundle, Request.Mode.GET, onResultListener);
    }


    /**
     * 更新外勤计划状态
     *
     * @param id     外勤id
     * @param isDone 是否已完成，否则未签退
     */
    public void endMission(int id, boolean isDone) {
        if (id == 0) return;
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().updateOutplanStatus : "mobile/mobileplanUpdate.action";
        if (isB2b)
            param.put("statuscode", isDone ? "done" : "CHECKOUT");
        Request request = new Request.Bulider()
                .setWhat(END_MISSION)
                .setUrl(url)
                .setParam(param)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    //获取打卡数据
    private void loadLog(Bundle bundle) {
        long time = 0;
        if (bundle == null) bundle = new Bundle();
        else time = bundle.getLong("time");
        if (time == 0)
            time = System.currentTimeMillis();
        String date = TimeUtils.s_long_2_str(time);
        Map<String, Object> param = new HashMap<>();
        String code = CommonUtil.getEmcode();
        param.put("currentMaster", CommonUtil.getMaster());
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (isB2b)
            param.put("pageNumber", 1);
        else
            param.put("page", 1);
        param.put("pageSize", 100);
        if (!isB2b)
            param.put("condition", "cl_emcode='" + code + "' and to_char(cl_time,'yyyy-MM-dd')='" + date + "'");
        else
            param.put("date", DateFormatUtil.long2Str(time, "yyyyMMdd"));
        param.put("caller", "CardLog");
        param.put("emcode", code);
        param.put("master", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        bundle.putLong("time", time);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_record_url
                : "mobile/oa/workdata.action";
        Request request = new Request.Bulider()
                .setWhat(WORK_LOG_WHAT)
                .setUrl(url)
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    private void loadNet(int waht, String url, Map<String, Object> param, Bundle bundle, Request.Mode mode, OnResultListener onResultListener) {
        if (bundle == null) bundle = new Bundle();
        bundle.putSerializable("onResultListener", onResultListener);

        Request request = new Request.Bulider()
                .setWhat(waht)
                .setUrl(url)
                .setParam(param)
                .setBundle(bundle)
                .setMode(mode)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    /*添加联系人接口*/
    public void addContact(List<Map<String, Object>> formStores, OnHttpResultListener listener) {
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "contact");
        param.put("formStore", JSONUtil.map2JSON(formStores));
        Request request = new Request.Bulider()
                .setWhat(ADD_CONTACT)
                .setUrl("mobile/crm/addContactPerson.action")
                .setParam(param)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, listener == null ? this : listener);
    }

    /**
     * @param sourcecode  来源编号
     * @param contactName 联系人名字
     * @param mobile      联系人手机
     * @param cuname      客户名字
     * @param cucode      客户编号
     * @param address     客户地址
     * @param job         岗位
     * @return
     */
    public Map<String, Object> getFormStoreContact(String sourcecode,
                                                   String contactName,
                                                   String mobile,
                                                   String cuname,
                                                   String cucode,
                                                   String address,
                                                   String job) {
        Map<String, Object> param = new HashMap<>();
        param.put("ct_sourcecode", sourcecode);//来源编号
        param.put("ct_name", contactName);//联系人名字
        param.put("ct_mobile", mobile);//联系人手机
        param.put("ct_cuname", cuname);//客户名字
        param.put("ct_cucode", cucode);//客户编号
        param.put("ct_address", address);//客户地址
        param.put("ct_job", job);      //岗位
        return param;
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        OnResultListener listener = (OnResultListener) bundle.getSerializable("onResultListener");
        String resultMessage = message;
        try {
            if (!isJSON) {
                if (listener != null)
                    listener.result(false, what, message);
                return;
            }
            JSONObject object = JSON.parseObject(message);
            boolean success = true;
            switch (what) {
                case CODE_WHAT://获取编号
                    resultMessage = object.getString("code");
                    break;
                case ID_WHAT:
                    success = (object.containsKey("success") && object.getBoolean("success"));
                    int id = JSONUtil.getInt(object, "id");
                    if (id != 0)
                        resultMessage = String.valueOf(id);
                    else success = false;
                    break;
                case OUT_SET_WHAT://获取外勤地址
                    success = CommonInterfaceHandler.getOutSet(object);
                    break;
                case ADD_OUT_SET_WHAT://添加外勤地址
                    success = true;
                    break;
                case LOAD_COMPANY_WHAT://获取企业信息的内容，有与该信息比较特殊，将数据处理逻辑放到外面去
                    break;
                case ISMAIN_WHAT://判断是否是管理员
                    success = CommonInterfaceHandler.saveMainStatus(object);
                    break;
                case LOAD_CONFIG_WHAT://获取高级设置
                    WorkHandlerUtil.handlerWorkSet(object);
                    break;
                case WORK_DATA_WHAT://获取班次接口
                    success = CommonInterfaceHandler.saveWorkData(object, bundle);
                    if (success)
                        loadLog(bundle);
                    break;
                case WORK_LOG_WHAT://获取班次打卡记录接口

                    break;

            }
            if (listener != null)
                listener.result(success, what, resultMessage);
        } catch (NullPointerException e) {
            if (e != null) {
                Log.i("gongpengming", "handleMessage NullPointerException=" + e.getMessage());
                resultMessage = e.getMessage();
            }
            if (listener != null)
                listener.result(false, what, resultMessage);
        } catch (Exception e) {
            if (e != null) {
                Log.i("gongpengming", "handleMessage Exception=" + e.getMessage());
                resultMessage = e.getMessage();
            }
            if (listener != null)
                listener.result(false, what, resultMessage);
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        OnResultListener listener = (OnResultListener) bundle.getSerializable("onResultListener");
        if (listener != null)
            listener.result(false, what, message);
    }


    public interface OnResultListener extends Serializable {
        /**
         * 通用接口保存后返回接口
         *
         * @param success 成功
         * @param what    请求的what
         * @param message 返回的外面需要用到的信息
         */
        void result(@NonNull boolean success, @NonNull int what, @Nullable String message);
    }
}
