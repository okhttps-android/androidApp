package com.uas.appworks.OA.erp.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.SystemUtil;
import com.common.thread.ThreadUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.MessageDao;
import com.core.dao.UserDao;
import com.core.dao.WorkLocationDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.MissionModel;
import com.core.model.OAConfig;
import com.core.model.User;
import com.core.model.WorkLocationModel;
import com.core.model.WorkModel;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.BaiduMapUtil;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.core.utils.NotificationManage;
import com.core.utils.OnGetDrivingRouteResult;
import com.core.utils.TimeUtils;
import com.core.utils.WorkHandlerUtil;
import com.modular.apputils.utils.SwitchUtil;
import com.modular.apputils.utils.VoiceUtils;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.OA.erp.activity.MissionActivity;
import com.uas.appworks.OA.erp.activity.WorkActivity;
import com.uas.appworks.R;
import com.uas.appworks.dao.MissionDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.core.app.AppConstant.CHANGE_WORK_TASK;
import static com.core.utils.CommonUtil.getSharedPreferences;

/**
 * 负责签到   内外勤
 * Created by Bitliker on 2016/12/20.
 */
public class AutoErpSigninUitl implements OnHttpResultListener {

    private final int MAC_VAL = 0x11;//判断mac地址
    private final int SIGNING = 0x12;//内勤签到
    private final int LOAD_WORKLOG = 0x13;//打卡列表
    private final int SIGNIN_MISSION = 0x14;//外勤签到
    private final int HAVE_OUT_PLAN = 0x15;//还有未拜访外勤计划
    private final int WORK_DATA = 0x16;//内勤列表
    private final int WORK_LOG = 0x17;//下拉列表时候的获取打卡列表
    private final int LOAD_PLAN = 0x18;
    private final int LOAD_WORK_SET = 0x21;//获取打卡高级设置
    private boolean isB2b;
    private NotificationManage notificationManage;

    public AutoErpSigninUitl() {
        notificationManage = new NotificationManage();
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
    }

    private boolean signWorking = false;

    public boolean isB2b() {
        return isB2b;
    }

    /**
     * 签到内勤
     *
     * @param model 签到班次对象
     */
    public void signinWork(boolean isWork, WorkModel model) {
        if (signWorking) {
            return;
        }
        signWorking = true;
        //1.判读mac地址是否符合     3.签到
        if (!MyApplication.getInstance().isNetworkActive()) {
            CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_sign_failed), getString(R.string.common_notlinknet));
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putParcelable("data", model);
        bundle.putInt("isWork", isWork ? 1 : 0);
        if (isB2b) {
            signinWork(bundle, "");
        } else {
            if (SwitchUtil.needMacForSign()) {
                validatorMac(model, SystemUtil.getMac(MyApplication.getInstance()));  // 关闭mac地址验证
            } else {
                CommonInterface.getInstance().getCodeByNet("CardLog", new CommonInterface.OnResultListener() {
                    @Override
                    public void result(boolean isOk, int result, String message) {
                        signinWork(bundle, message);
                    }
                });
            }

        }
    }

    /**
     * 签到外勤
     *
     * @param model 签到对象
     */
    public void signinMission(MissionModel model) {
        if (model == null) return;
        if (!MyApplication.getInstance().isNetworkActive()) {
            notificationManage.sendNotification(BaseConfig.getContext(), R.string.out_net_signin, MissionActivity.class);
            CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_outplan_failed), getString(R.string.common_notlinknet));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        String emcode = CommonUtil.getEmcode();
        String name = CommonUtil.getName();
        String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
        if (StringUtil.isEmpty(address))
            address = model.getCompanyAddr();
        String remark = model.getStatus() == 2 ? "Android自动外勤签退:自动"
                : "Android自动外勤签到:自动";
        String location = model.getCompanyName();
        if (StringUtil.isEmpty(location) || isB2b)
            location = UasLocationHelper.getInstance().getUASLocation().getName();
        formStore.put("mo_remark", remark);//备注
        formStore.put("mo_company", location);
        formStore.put("mo_address", address);
        if (isB2b) {
            formStore.put("enuu", CommonUtil.getMaster());
            formStore.put("mpd_id", model.getId());
            formStore.put("emcode", emcode);
        } else {
            param.put("mpd_id", model.getId());
            param.put("caller", "Mobile_outsign");
            formStore.put("mo_man", name);
            formStore.put("mo_mancode", emcode);
        }
        param.put("formStore", JSONUtil.map2JSON(formStore));
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", model);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveOutSign :
                "mobile/addAutoSign.action";
        Request request = new Request.Bulider()
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .setParam(param)
                .setUrl(url)
                .setWhat(SIGNIN_MISSION)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /*判断mac地址是否符合*/
    private void validatorMac(WorkModel model, String macAddress) {
        Map<String, Object> param = new HashMap<>();
        param.put("macAddress", macAddress);
        param.put("emcode", getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", model);
        String url = "mobile/addMobileMac.action";
        Request request = new Request.Bulider()
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .setParam(param)
                .setUrl(url)
                .setWhat(MAC_VAL)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    //签到
    private void signinWork(Bundle bunder, String code) {
        Context ct = MyApplication.getInstance();
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> form = new HashMap<>();
        if (!isB2b)
            form.put("cl_code", code);
        String phone = MyApplication.getInstance().mLoginUser.getTelephone();
        if (StringUtil.isEmpty(phone)) {
            String userId = MyApplication.getInstance().mLoginUser.getUserId();
            if (!StringUtil.isEmpty(userId)) {
                User user = UserDao.getInstance().getUserByUserId(userId);
                phone = user.getTelephone();
            } else
                phone = getSharedPreferences(MyApplication.getInstance(), "user_phone");
        }
        form.put("cl_phone", phone);
        form.put("cl_emcode", getSharedPreferences(ct, "erp_username"));
        String emname = getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        form.put("cl_emname", emname);
        boolean isp = isPlay(form);
        if (!isp) {
            return;//不符合打卡
        }
        if (isB2b) {
            form.put("enuu", getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            form.put("emcode", getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        } else
            param.put("caller", "CardLog");

        String formStore = JSONUtil.map2JSON(form);
        param.put("formStore", formStore);
        param.put("facecard", "1");
        param.put("_noc", "1");
        param.put("emcode", getSharedPreferences(ct, "erp_username"));
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_worksignin_url :
                "mobile/saveCardLog.action";
        Request request = new Request.Bulider()
                .setBundle(bunder)
                .setMode(Request.Mode.POST)
                .setParam(param)
                .setUrl(url)
                .setWhat(SIGNING)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }


    /*判断是否符合打卡*/
    private boolean isPlay(Map<String, Object> form) {
        try {
            //判断是否符合打卡
            UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();
            String address = mUASLocation.getAddress();
            if (StringUtil.isEmpty(address))
                address = PreferenceUtils.getString("bdaddress");
            form.put("cl_address", address);
            form.put("cl_location", "android " + MyApplication.getInstance().getString(R.string.auto_work_signin_log));
//            int comDistance = PreferenceUtils.getInt("distance", 0);
            float distance = -1;
            List<WorkLocationModel> beanList = WorkLocationDao.getInstance().queryByEnCode();
            if (!ListUtils.isEmpty(beanList)) {
                for (WorkLocationModel bean : beanList) {
                    float dis = LocationDistanceUtils.distanceMeBack(bean.getLocation());
                    if (dis < bean.getValidrange()) {
                        distance = dis;
                        break;
                    }
                }
            }
            if (distance == -1) {
                //判断是否有外勤
                boolean isOutPlan = PreferenceUtils.getBoolean(AppConfig.HAVE_OUT_PLAN, false);
                return false;
            } else {
                form.put("cl_distance", distance);
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    //获取打卡记录
    private void loadLog(Bundle bunder) throws Exception {
        String date = TimeUtils.s_long_2_str(System.currentTimeMillis());
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        String code = getSharedPreferences(MyApplication.getInstance(), "erp_username");
        param.put("currentMaster", getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        if (isB2b) {
            param.put("pageNumber", 1);
            param.put("date", DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMMdd"));
        } else {
            param.put("page", 1);
            param.put("condition", "cl_emcode='" + code + "' and to_char(cl_time,'yyyy-MM-dd')='" + date + "'");
        }
        param.put("pageSize", 100);
        param.put("caller", "CardLog");
        param.put("emcode", code);
        param.put("master", getSharedPreferences(MyApplication.getInstance(), "erp_master"));

        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_record_url :
                "mobile/oa/workdata.action";
        Request request = new Request.Bulider()
                .setBundle(bunder)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setUrl(url)
                .setWhat(LOAD_WORKLOG)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    //获取是否有外勤计划
    public void loadIsMission(MissionModel model) {
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bunder = new Bundle();
        bunder.putParcelable("data", model);
        String url = "mobile/yesornoplan.action";
        Request request = new Request.Bulider()
                .setParam(param)
                .setBundle(bunder)
                .setUrl(url)
                .setWhat(HAVE_OUT_PLAN)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    /*****************
     * 下拉列表
     ***********************/
    /**
     * 下拉内勤数据
     */
    public void loadWorkData() {
        Map<String, Object> param = new HashMap<>();
        param.put("date", DateFormatUtil.long2Str("yyyyMMdd"));
        param.put("emcode", getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bundle = new Bundle();
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_schedule_url :
                "mobile/getWorkDate.action";
        Request request = new Request.Bulider()
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setUrl(url)
                .setWhat(WORK_DATA)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }


    private void loadLog(ArrayList<WorkModel> models) {
        String date = TimeUtils.s_long_2_str(System.currentTimeMillis());
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        String code = getSharedPreferences(MyApplication.getInstance(), "erp_username");
        param.put("currentMaster", getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        if (isB2b) {
            param.put("date", DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMMdd"));
            param.put("pageNumber", 1);
        } else {
            param.put("condition", "cl_emcode='" + code + "' and to_char(cl_time,'yyyy-MM-dd')='" + date + "'");
            param.put("page", 1);
        }
        param.put("pageSize", 100);
        param.put("caller", "CardLog");
        param.put("emcode", code);
        param.put("master", getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("models", models);

        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_record_url :
                "mobile/oa/workdata.action";
        Request request = new Request.Bulider()
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setUrl(url)
                .setWhat(WORK_LOG)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    public void loadWorkSet() {
        //获取考勤高级设置时间请求
        Map<String, Object> param = new HashMap<>();
        if (!isB2b)
            param.put("code", 1);

        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().get_plat_senior_setting_url :
                "mobile/getconfigs.action";
        Request request = new Request.Bulider()
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setUrl(url)
                .setWhat(LOAD_WORK_SET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    //获取外勤计划列表
    public void loadMissionPlan() {
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getOutPlan :
                "mobile/mobileoutplan.action";
        Request request = new Request.Bulider()
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setUrl(url)
                .setWhat(LOAD_PLAN)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    @Override
    public void result(int what, boolean isJSON, String message, final Bundle bundle) {
        try {
            switch (what) {
                case MAC_VAL://判断mac地址
                    if (isJSON && JSON.parseObject(message).containsKey("success") &&
                            JSON.parseObject(message).getBoolean("success")) {
                        CommonInterface.getInstance().getCodeByNet("CardLog", new CommonInterface.OnResultListener() {
                            @Override
                            public void result(boolean isOk, int result, String message) {
                                signinWork(bundle, message);
                            }
                        });
                    } else {
                        CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_sign_failed), message);
                    }
                    break;
                case SIGNING:
                    String subtitle = UasLocationHelper.getInstance().getUASLocation().getName();
                    if (PreferenceUtils.getBoolean(PreferenceUtils.Constants.AUTO_SIGN_SW, true)) {
                        MediaUtils.saveLogOk(MyApplication.getInstance());
                        boolean saveOk = MessageDao.getInstance().instartSignin(getString(R.string.auto_work_signin_alert),
                                StringUtil.isEmpty(subtitle) ? getString(R.string.uu_auto_work_signined_up) : subtitle);
                        if (saveOk) {
                            Intent intent = new Intent(OAConfig.AUTO_SIGIN_ALART);
                            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
                            PreferenceUtils.putBoolean("hasAutoSign", true);
                            int isWork = bundle == null ? -1 : bundle.getInt("isWork", -1);
                            int rawId = R.raw.voice_work;
                            switch (isWork) {
                                case 0:
                                    rawId = R.raw.voice_off;
                                    break;
                                case 1:
                                    rawId = R.raw.voice_work;
                                    break;
                            }
                            VoiceUtils.signVoice(rawId);
                        }
                    }
                    loadLog(bundle);
                    break;
                case LOAD_WORKLOG://签到后保存到打卡数据库里面
                    if (isJSON && JSON.parseObject(message).containsKey("listdata")) {
                        saveSignin2DB(bundle, JSON.parseObject(message).getJSONArray("listdata"));
                        signWorking = false;
                    }
                    break;
                case SIGNIN_MISSION:
                    if (isJSON) {
                        if (JSON.parseObject(message).containsKey("success") && JSON.parseObject(message).getBoolean("success")) {
                            //成功  更新数据库
                            if (bundle == null) return;
                            MissionModel mission = bundle.getParcelable("data");
                            if (mission == null) return;
                            updataMissonDB(mission, true);
                        }
                    }
                    break;
                case HAVE_OUT_PLAN:
                    if (!isJSON) return;
                    JSONObject object = JSON.parseObject(message);
                    if (!object.containsKey("success") || !object.getBoolean("success")) return;
                    if (object.containsKey("isOffline")) {
                        int isOffline = Integer.valueOf(object.getString("isOffline"));
                        //当判断到外勤计划为有值  1.外勤计划有  2.自动外勤
                        if (bundle != null && bundle.getParcelable("data") != null) {
                            MissionModel model = bundle.getParcelable("data");
                            if (model == null) {
                                LogUtil.i("model==null");
                                return;
                            }
                            boolean saveOk = MissionDao.getInstance().updata(model);
                            if (isOffline > 0) {//还有外勤计划
                                if (saveOk)
                                    notificationManage.sendNotification(MyApplication.getInstance(),
                                            model.getCompanyName() + MyApplication.getInstance().getString(R.string.success_signin_down_mission), MissionActivity.class);
                            } else {//没有外勤计划
                                endMission(model, saveOk);
                                List<MissionModel> models = MissionDao.getInstance().queryByEnCode();
                                if (ListUtils.isEmpty(models) || !isMoreMission(models))
                                    endOfMission();

                            }
                        }
                    }
                case WORK_DATA://打卡班次
                    handlerWorkData(isJSON, message);
                    break;
                case WORK_LOG://
                    ArrayList<WorkModel> models = null;
                    if (bundle != null) models = bundle.getParcelableArrayList("models");
                    handlerWorkLog(isJSON, message, models);
                    break;
                case LOAD_PLAN:
                    JSONArray array = null;
                    if (isJSON && JSON.parseObject(message).get("data") instanceof JSONArray) {
                        array = JSON.parseObject(message).getJSONArray("data");
                    } else if (JSON.parseObject(message).get("success") instanceof JSONArray) {
                        array = JSON.parseObject(message).getJSONArray("success");
                    }
                    handlerData(array);
                    break;
                case LOAD_WORK_SET:
                    if (isJSON) {
                        handlerWorkSet(JSON.parseObject(message));
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            if (e != null) {
                LogUtil.i("result=" + e.getMessage());
                CommonUtil.saveAutoLogtoLocal(getString(R.string.app_monitor_log), e.getMessage());
            }
        }
    }

    boolean isErrorMac;

    @Override
    public void error(int what, String message, Bundle bundle) {
        if (!StringUtil.isEmpty(message)) {
            String msg = null;
            if (StringUtil.isInclude(message, MyApplication.getInstance().getString(R.string.is_not_common_device))) {
                msg = MyApplication.getInstance().getString(R.string.is_not_common_device_not_signin);
                CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_sign_failed), msg);
            } else if (StringUtil.isInclude(message, "设备正处于申请变更绑定阶段")) {
                msg = MyApplication.getInstance().getString(R.string.is_changeing_not_signin);
                CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_sign_failed), msg);
            } else if (StringUtil.isInclude(message, "该设备已被他人绑定")) {
                msg = MyApplication.getInstance().getString(R.string.is_binded_other_not_signin);
                CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_sign_failed), msg);
            } else if (what == SIGNIN_MISSION) {
                try {
                    Intent intent = new Intent();
                    intent.putExtra(AppConstant.CHANGE_MISSION_TASK, true);
                    intent.setAction(AppConstant.CHANGE_MISSION_TASK);
                    MsgBroadcast.sendLocalBroadcast(intent);
                    if (!StringUtil.isEmpty(message))
                        CommonUtil.saveAutoLogtoLocal(getString(R.string.auto_outplan_failed), message);
                } catch (Exception e) {
                }
            }
            if (!StringUtil.isEmpty(msg))
                CommonUtil.saveAutoLogtoLocal(getString(R.string.app_monitor_log), msg);
            if (!isErrorMac && !StringUtil.isEmpty(msg)) {
                isErrorMac = true;
                notificationManage.sendNotification(MyApplication.getInstance(), msg, WorkActivity.class);
            }
        }
    }

    /**
     * 外勤签到签退处理
     * 1.签退：status==1&&离开范围之内==》mission.setStatus(2) ,先把状态设置位2在传进来签退
     * 2.签到：在范围之内，status不做任何改变 传进来签到
     *
     * @param mission 传进来的对象
     * @throws Exception
     */

    public void updataMissonDB(MissionModel mission, boolean isAuto) throws Exception {
        //更新数据库
        if (mission == null) return;//如果数据库没有，一般不会出现这样的情况
        if (StringUtil.isEmpty(mission.getRealTime()))
            mission.setRealTime(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        else mission.setRealLeave(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        String title = "";
        String subTitle = "";
        if (mission.getStatus() == 2) {//签退
            if (isAuto) {
                title = getString(R.string.auto_mission_alert);
            } else {
                title = getString(R.string.manual_mission_alert);
            }
            subTitle = getMissionSubTitle(mission);

            CommonInterface.getInstance().endMission(mission.getId(), false);
            loadIsMission(mission);
        } else {//签到
            if (mission.getStatus() < 1) {
                if (isAuto) {
                    title = getString(R.string.auto_mission_alert);
                } else {
                    title = getString(R.string.manual_mission_alert);
                }
                subTitle = getMissionSubTitle(mission);
                mission.setStatus(1);//说明没有打过卡
                boolean saveOk = MissionDao.getInstance().updata(mission);
                if (saveOk)//XXX公司自动外勤签到成功
                    notificationManage.sendNotification(MyApplication.getInstance(), mission.getCompanyName()
                                    + getString(R.string.success_signin_up_mission),
                            MissionActivity.class);
            }
        }
        MissionDao.getInstance().updata(mission);
        if (!StringUtil.isEmpty(title) && !StringUtil.isEmpty(subTitle))
            MessageDao.getInstance().instartSignin(title, subTitle);
    }

    public static String getMissionSubTitle(MissionModel mission) {
        String subTitle;
        if (mission.getStatus() == 2) {
            if (StringUtil.isEmpty(mission.getCompanyName()))
                subTitle = MyApplication.getInstance().getString(R.string.uu_auto_signined_down);
            else
                subTitle = MyApplication.getInstance().getString(R.string.visit) + mission.getCompanyName() + getString(R.string.success_signin_down);
        } else {
            if (StringUtil.isEmpty(mission.getCompanyName()))
                subTitle = getString(R.string.uu_auto_signined_up);
            else
                subTitle = getString(R.string.visit) + mission.getCompanyName() + getString(R.string.success_signin_up);
        }
        return subTitle;
    }

    /**
     * 外勤签退后判断时候还有外勤计划，如果没有，判断是否符合返回公司  返回提示请返回公司签到
     *
     * @param model
     * @param saveOk
     * @throws Exception
     */
    private void endMission(final MissionModel model, final boolean saveOk) throws Exception {
        float longitude = PreferenceUtils.getFloat("longitude");
        float latitude = PreferenceUtils.getFloat("latitude");
        BaiduMapUtil.getInstence().getDrivingRoute(UasLocationHelper.getInstance().getUASLocation().getLocation(), new LatLng(latitude, longitude), new OnGetDrivingRouteResult() {
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                List<DrivingRouteLine> list = drivingRouteResult.getRouteLines();
                if (ListUtils.isEmpty(list)) {//获取路线列表为空
                    if (saveOk)
                        notificationManage.sendNotification(MyApplication.getInstance(), getString(R.string.to) + model.getCompanyName() + getString(R.string.success_signin_down_mission),
                                MissionActivity.class);
                    return;
                }
                int minTime = 0;
                for (DrivingRouteLine e : list) {
                    if (minTime == 0 || minTime > e.getDuration()) {
                        minTime = e.getDuration();
                    }
                }
                LogUtil.i("最短时间为" + minTime);
                //获取使用时间为。。。。
                //当前时间
                List<WorkModel> models = WorkModelDao.getInstance().query(true);
                if (ListUtils.isEmpty(models)) {
                    LogUtil.i("数据库中的班次为空的");
                    if (saveOk)
                        notificationManage.sendNotification(MyApplication.getInstance(), getString(R.string.to) + model.getCompanyName() + getString(R.string.success_signin_down_mission),
                                MissionActivity.class);
                    return;
                }
                String lastTime = null;
                for (WorkModel e : models) {
                    if (StringUtil.isEmpty(e.getOffTime())) continue;
                    if (StringUtil.isEmpty(lastTime) || e.getOffTime().compareTo(lastTime) > 0)
                        lastTime = e.getOffTime();
                }
                if (StringUtil.isEmpty(lastTime)) {
                    if (saveOk)
                        notificationManage.sendNotification(MyApplication.getInstance(), getString(R.string.to) + model.getCompanyName() + getString(R.string.success_signin_down_mission),
                                MissionActivity.class);
                    return;
                }
                int time = getTime(lastTime);
                LogUtil.i("与下班时间相差" + time);
                int deTime = PreferenceUtils.getInt(AppConfig.AUTO_MISSION_TIME, 10);
                if ((minTime + deTime * 60) < time) {
                    notificationManage.sendNotification(MyApplication.getInstance(),
                            R.string.success_signin_down_can_back, WorkActivity.class);
                } else {
                    Log.i("gongpengming", "时间不符合");
                    if (saveOk)
                        notificationManage.sendNotification(MyApplication.getInstance(),
                                R.string.success_signin_down_task_ok, MissionActivity.class);
                }
            }
        });
    }

    //保存到数据库
    private void saveSignin2DB(Bundle bundle, JSONArray array) throws Exception {
        WorkModel model = null;
        LogUtil.i("saveSignin2DB");
        if (bundle != null && bundle.getParcelable("data") != null) {
            model = bundle.getParcelable("data");
        }
        if (model == null) {
            LogUtil.i("model == null");
            List<WorkModel> models = WorkModelDao.getInstance().query(false);
            if (ListUtils.isEmpty(models)) return;
            String time = DateFormatUtil.long2Str(System.currentTimeMillis(), "HH:mm");
            for (WorkModel m : models) {
                //当前时间位于该班次之间
                if (m.getWorkStart().compareTo(time) < 0 && m.getOffend().compareTo(time) > 0) {
                    model = m;
                    break;
                }
            }
        }
        //取最后一个
        JSONObject object = array.getJSONObject(array.size() - 1);
        String time = object.getString("cl_time");//获取最后一次打卡信息，班次打卡信息
        time = DateFormatUtil.long2Str(DateFormatUtil.str2Long(time, DateFormatUtil.YMD_HMS), DateFormatUtil.HM);//获取到的
        if (StringUtil.isEmpty(time) || model == null) return;
        if (StringUtil.isEmpty(model.getWorkSignin()) && model.getWorkStart().compareTo(time) <= 0
                && model.getWorkTime().compareTo(time) >= 0) {
            model.setWorkSignin(time);
            long i = WorkModelDao.getInstance().update(model);
            if (PreferenceUtils.getBoolean(PreferenceUtils.Constants.AUTO_SIGN_SW, true)) {
                notificationManage.sendNotification(MyApplication.getInstance(),
                        R.string.auto_signin, WorkActivity.class);
                LogUtil.i("发送上班打卡通知");
            }
            boolean b = hasMore();
            Intent intent = new Intent();
            intent.setAction(CHANGE_WORK_TASK);
            intent.putExtra(CHANGE_WORK_TASK, b);
            MsgBroadcast.sendLocalBroadcast(intent);
        } else if (StringUtil.isEmpty(model.getOffSignin()) && model.getOffTime().compareTo(time) <= 0
                && model.getOffend().compareTo(time) >= 0) {
            model.setOffSignin(time);
            WorkModelDao.getInstance().update(model);
            if (PreferenceUtils.getBoolean(PreferenceUtils.Constants.AUTO_SIGN_SW, true)) {
                notificationManage.sendNotification(MyApplication.getInstance(),
                        R.string.auto_signin, WorkActivity.class);
            }
            LogUtil.i("发送下班打卡通知");
            boolean b = hasMore();
            Intent intent = new Intent();
            intent.setAction(CHANGE_WORK_TASK);
            intent.putExtra(CHANGE_WORK_TASK, b);
            MsgBroadcast.sendLocalBroadcast(intent);
        }
    }

    private boolean hasMore() {
        List<WorkModel> models = WorkModelDao.getInstance().query(false);
        if (ListUtils.isEmpty(models)) return false;
        else {
            for (WorkModel m : models) {
                if (StringUtil.isEmpty(m.getWorkSignin()) || StringUtil.isEmpty(m.getOffSignin()))
                    return true;
            }
        }
        return false;
    }


    /**
     * 处理打卡签到
     *
     * @param isJSON
     * @param message
     */
    private void handlerWorkData(boolean isJSON, String message) throws Exception {
        if (!isJSON)
            return;
        JSONObject object = JSON.parseObject(message);
        ArrayList<WorkModel> models = WorkHandlerUtil.handlerWorkData(object, isB2b);
        OAConfig.name = JSONUtil.getText(object, "wd_name");
        OAConfig.days = JSONUtil.getText(object, "wd_day");
        WorkHandlerUtil.handerLocation(object, isB2b);
        if (ListUtils.isEmpty(models)) {
            return;
        }
        loadLog(models);
    }


    /**
     * 处理打卡签到列表，建议在线程钟使用
     *
     * @param isJSON     是否是json数据
     * @param message    返回信息
     * @param workModels 获取班次信息时候的数据
     * @throws Exception
     */
    private void handlerWorkLog(boolean isJSON, String message, ArrayList<WorkModel> workModels) throws Exception {
        if (!isJSON || ListUtils.isEmpty(workModels)) {
            saveDb(workModels);
            return;
        }
        JSONObject object = JSON.parseObject(message);
        ArrayList<WorkModel> models = WorkHandlerUtil.handlerWorkLog(object, workModels);
        saveDb(models);
    }


    private void saveDb(final List<WorkModel> models) {
        if (!ListUtils.isEmpty(models)) {
            ThreadUtil.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    boolean saveOK = WorkModelDao.getInstance().createOrUpdata(models, System.currentTimeMillis());
                    if (saveOK) {
                        OAHttpHelper.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setAction(CHANGE_WORK_TASK);//保存到数据库成功，开启
                                intent.putExtra(CHANGE_WORK_TASK, true);
                                MsgBroadcast.sendLocalBroadcast(intent);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 第一次进来应用会请求一次数据，并把数据保存到数据库中，保存成功后发送广播，更新轮询
     * 解析处理外勤计划列表并保存到数据库
     *
     * @param array
     */
    private void handlerData(JSONArray array) throws Exception {
        if (ListUtils.isEmpty(array)) {
            ThreadUtil.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    List<MissionModel> models = MissionDao.getInstance().queryByEnCode();
                    if (!ListUtils.isEmpty(models)) {
                        OAHttpHelper.getInstance().post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.putExtra(AppConstant.CHANGE_MISSION_TASK, true);
                                intent.setAction(AppConstant.CHANGE_MISSION_TASK);
                                MsgBroadcast.sendLocalBroadcast(intent);
                            }
                        });

                    }
                }
            });
        } else {
            JSONObject object = null;
            MissionModel entity = null;
            final List<MissionModel> entities = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                object = array.getJSONObject(i);
                entity = WorkHandlerUtil.handlerEntity(isB2b, object);
                if (entity == null)
                    continue;
                entities.add(entity);
            }
            if (!ListUtils.isEmpty(entities)) {
                LogUtil.i("保存到数据库成功");
                ThreadUtil.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        boolean saveOk = false;
                        try {
                            saveOk = MissionDao.getInstance().updataOrCreate(entities);
                            if (!saveOk)
                                saveOk = !ListUtils.isEmpty(MissionDao.getInstance().queryByEnCode());
                        } catch (Exception e) {

                        }
                        if (saveOk) {
                            CommonUtil.saveAutoLogtoLocal(getString(R.string.app_start_log), getString(R.string.app_outplan_running));
                            //TODO 保存异常数据库
                            OAHttpHelper.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent();
                                    intent.putExtra(AppConstant.CHANGE_MISSION_TASK, true);
                                    intent.setAction(AppConstant.CHANGE_MISSION_TASK);
                                    MsgBroadcast.sendLocalBroadcast(intent);
                                    LogUtil.i("发送广播成功");
                                }
                            });
                        }
                    }
                });
            }
        }
    }


    private void handlerWorkSet(JSONObject o) {
        try {
            WorkHandlerUtil.handlerWorkSet(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (OAConfig.autosign) {
            loadWorkData();
        }
    }

    private boolean isMoreMission(List<MissionModel> models) {
        for (MissionModel e : models) {
            if (e.getType() != 1) {
                LogUtil.i("返回正确");
                return true;
            }
        }
        return false;
    }

    private void endOfMission() {
        Intent intent = new Intent();
        intent.setAction(AppConstant.CHANGE_MISSION_TASK);
        intent.putExtra(AppConstant.CHANGE_MISSION_TASK, false);
        MsgBroadcast.sendLocalBroadcast(intent);
    }

    //获取当前时间鱼下班时间的差
    private int getTime(String lastTime) {
        long time = TimeUtils.f_str_2_long(TimeUtils.s_long_2_str(System.currentTimeMillis())
                + " " + lastTime + ":00");
        long time2 = time - System.currentTimeMillis();
        if (time < 0) return 0;
        return (int) (time2 / 1000);
    }

    private static String getString(int id) {
        return MyApplication.getInstance().getString(id);
    }

}
