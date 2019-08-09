package com.uas.appworks.OA.erp.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.excep.utils.Base64Util;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.file.FileUtils;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.MessageDao;
import com.core.model.MissionModel;
import com.core.model.SelectBean;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.BitmapUtils;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.WorkHandlerUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.OA.erp.model.IMission;
import com.uas.appworks.OA.erp.utils.AutoErpSigninUitl;
import com.uas.appworks.R;
import com.uas.appworks.dao.MissionDao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressFlower;


/**
 * Created by Bitliker on 2016/12/19.
 */

public class MissionPresenter implements IMissionPresenter, OnHttpResultListener {
    private final int SUBMIT = 0x16;
    private final int CODE_WHAT = 0x15;
    private final int ID_WHAT = 0x14;
    private final int LOAD_PLAN = 0x11;
    private final int SAVE_PLAN = 0x12;
    private final int SAVE_ADDRESS = 0x13;
    private final int SIGNIN_MISSION = 0x17;
    private final int FIND_LIKER = 0x18;
    private final int END_MISSION = 0x19;
    private IMission iMission;
    private boolean isB2b;
    private MissionModel modelIntent;
    private int mFaceSign;
    private ACProgressFlower mUploadLoading;

    public MissionPresenter(IMission iMission) {
        this.iMission = iMission;
    }

    /**
     * 1.下拉数据或是从数据库获取数据（修改返回id后先数据库再网络，当前先网络再数据库）
     * 2.保存数据
     */
    @Override
    public void start(Intent intent) {
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (iMission != null) {//初始化
            List<MissionModel> models = new ArrayList<>();
            MissionModel entity = new MissionModel();
            entity.setType(1);
            entity.setLocation(UasLocationHelper.getInstance().getUASLocation().getName());//当前位置
            entity.setRecorddate(TimeUtils.f_long_2_str(System.currentTimeMillis()));//当前时间
            models.add(entity);
            iMission.showModels(models);
//            iMission.showLoading();
        }
        if (intent != null) {
            modelIntent = intent.getParcelableExtra("model");
            if (modelIntent != null && modelIntent.getStatus() != 5)
                modelIntent.setStatus(4);
        }
        if (modelIntent == null) {
            if (MyApplication.getInstance().isNetworkActive()) {
                loadMissionPlan();
            } else {
//                loadMissionByDB();
                ToastUtil.showToast(MyApplication.getInstance(), R.string.networks_out);
            }
        } else {
            List<MissionModel> models = new ArrayList<>();
            models.add(modelIntent);
            iMission.showModels(models);
        }

    }

    boolean isSubmiting = false;

    @Override
    public void submit(List<MissionModel> models) {
        if (ListUtils.isEmpty(models)) return;
        if (isSubmiting) return;
        if (!MyApplication.getInstance().isNetworkActive()) {
            if (iMission != null) iMission.showToast(R.string.networks_out, R.color.load_warning);
            return;
        }
        for (int i = 0; i < models.size(); i++) {
            MissionModel e = models.get(i);
            if (canSubmit(e, i + 1)) {
                savePlan2Net(e, i);
            }
        }
    }


    @Override
    public void sign(List<MissionModel> models) {
        if (ListUtils.isEmpty(models)) return;
        if (isSubmiting) return;
        List<MissionModel> plans = new ArrayList<>();
        for (MissionModel e : models) {
            if (e.getStatus() != 0) {
                plans.add(e);
            }
        }
        MissionModel mission = reckonMission(plans);//判断，获取距离最近的外勤计划
        if (mission == null) {
            iMission.showToast("无可签到签退的计划", R.color.load_error);
        } else {
            mFaceSign = PreferenceUtils.getInt(AppConfig.FACE_SIGN, -1);//-1:人脸设置不存在 1:打开 0:关闭
            if (mFaceSign == 1) {
                iMission.faceSign(mission);
            } else {
                signinMission(mission, null);
            }
        }
    }

    @Override
    public void finder(String licker) {
        if (StringUtil.isEmpty(licker)) return;
        iMission.showLoading();
        Map<String, Object> param = new HashMap<>();
        param.put("likestr", licker);
        param.put("page", 1);
        param.put("pageSize", 100);
        Bundle bundle = new Bundle();
        Request request = new Request.Bulider()
                .setUrl("mobile/crm/searchCustomer.action")
                .setWhat(FIND_LIKER)
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    /**
     * 保存拜访计划
     *
     * @param mode    对象
     * @param postion 指针
     */

    private void savePlan2Net(MissionModel mode, int postion) {
        isSubmiting = true;
        if (isB2b) {
            Bundle bundle = new Bundle();
            bundle.putInt("position", postion);
            bundle.putParcelable("data", mode);
            savePlan2Net(bundle);
        } else
            loadId(mode, postion);
    }

    private void loadId(MissionModel entity, int position) {
        isSubmiting = true;
        Map<String, Object> param = new HashMap<>();
        param.put("seq", "MOBILE_OUTPLAN_SEQ");
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", entity);
        bundle.putInt("position", position);
        Request request = new Request.Bulider()
                .setUrl("common/getId.action")
                .setWhat(ID_WHAT)
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void loadCode(Bundle bundle) {
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "MOBILE_OUTPLAN");
        param.put("type", 2);
        Request request = new Request.Bulider()
                .setUrl("common/getCodeString.action")
                .setWhat(CODE_WHAT)
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    //保存外勤计划接口
    private void savePlan2Net(Bundle bundle) {
        if (bundle == null) return;
        int id = 0;
        String code = "";
        if (!isB2b) {
            id = bundle.getInt("id");
            code = bundle.getString("code");
        }
        int position = bundle.getInt("position");
        MissionModel entity = bundle.getParcelable("data");
        if (!isB2b && (id == 0 || StringUtil.isEmpty(code)) || entity == null) return;
        isSubmiting = true;
        //获取网络数据
        if (iMission != null) iMission.showLoading();
        Context ct = MyApplication.getInstance();
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        String name = CommonUtil.getName();
        /***formStore***/
        if (!isB2b) {
            formStore.put("mp_id", id);//请求的id MOBILE_OUTPLAN_SEQ
            formStore.put("mp_code", code);//code MOBILE_OUTPLAN_SEQ
        }
        formStore.put("mp_visittime", TimeUtils.f_long_2_str(System.currentTimeMillis()));
        if (isB2b) {
            formStore.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            formStore.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        } else {
            formStore.put("mp_status", "在录入");
            formStore.put("mp_statuscode", "ENTERING");
        }
        formStore.put("mp_recorder", name);//用户名
        String mp_recordercode = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu") :
                CommonUtil.getSharedPreferences(ct, "erp_username");
        if (isB2b)
            formStore.put("recorderCode", mp_recordercode);//编号
        else
            formStore.put("mp_recordercode", mp_recordercode);//编号
        formStore.put("mp_address", UasLocationHelper.getInstance().getUASLocation().getAddress());//当前地址
        /***params***/
        if (!isB2b)
            params.put("mpd_detno", position + 1);
        if (!isB2b)
            params.put("mpd_id", "");
        params.put("mpd_kind", entity.getType() == 1 ? getStrByResources(R.string.half_day)
                : getStrByResources(R.string.all_day));
        params.put("mpd_remark", StringUtil.toHttpString(entity.getRemark()));
        params.put("mpd_distance", LocationDistanceUtils.distanceMe(entity.getLatLng()));
        params.put("mpd_arrivedate", entity.getVisitTime());
        if (!isB2b)
            params.put("mpd_mpid", id);
        params.put("mpd_company", entity.getCompanyName());
        params.put("mpd_address", entity.getCompanyAddr());
        param.put("caller", "lp");
        /******update by 2016/12/19**************/
        params.put("mpd_location", entity.getLocation());
        params.put("mpd_recorddate", entity.getRecorddate());
        if (isB2b) {
            params.put("longitude", entity.getLatLng().longitude);
            params.put("latitude", entity.getLatLng().latitude);
        }
        param.put("formStore", JSONUtil.map2JSON(formStore));
        param.put("param", "[" + JSONUtil.map2JSON(params) + "]");

        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveOutPlan :
                "mobile/addOutPlan.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setWhat(SAVE_PLAN)
                .setParam(param)
                .setBundle(bundle)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
        saveOutAddress(entity);
    }


    //保存外勤计划目的地
    private void saveOutAddress(MissionModel entity) {
        if (entity.getLatLng() == null || entity.getLatLng().latitude == 0 || entity.getLatLng().longitude == 0) {
            if (iMission != null)
                iMission.showToast(R.string.not_save_illegal_bill, R.color.load_warning);
            return;
        }
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        param.put("caller", "lp");
        /***formStore***/
//        formStore.put("Md_id", "");//请求的id MOBILE_OUTPLAN_SEQ
        formStore.put(isB2b ? "company" : "Md_company", entity.getCompanyName());//拜访公司
        String length = entity.getCompanyAddr();
        if (length.length() >= 200)
            length = length.substring(0, 190);
        formStore.put("Md_address", length);//拜访地址
        formStore.put("Md_visitcount", 1);//固定为1，由后台加1
        formStore.put("Md_visittime", entity.getVisitTime());//预计时间
        formStore.put("Md_longitude", entity.getLatLng().longitude);//经度
        formStore.put("Md_latitude", entity.getLatLng().latitude);//纬度
        String emcode = CommonUtil.getEmcode();
        formStore.put("Md_emcode", emcode);//纬度
        if (isB2b) {
            formStore.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            formStore.put("emcode", emcode);
        }
        param.put("formStore", JSONUtil.map2JSON(formStore));
        Bundle bundle = new Bundle();
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveOutAddress :
                "mobile/addOutAddress.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setWhat(SAVE_ADDRESS)
                .setParam(param)
                .setMode(Request.Mode.POST)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private boolean canSubmit(MissionModel e, int i) {
        if (e == null) return false;
        else if (e.getStatus() != 0) {
            return false;
        } else if (StringUtil.isEmpty(e.getCompanyName())) {
            if (iMission != null)
                iMission.showToast(getStrByResources(R.string.you_destination)
                        + i +
                        getStrByResources(R.string.visit_company) + getStrByResources(R.string.not_edit), R.color.load_warning);
            return false;
        } else if (StringUtil.isEmpty(e.getCompanyAddr())) {
            if (iMission != null)
                iMission.showToast(getStrByResources(R.string.you_destination) + i +
                        getStrByResources(R.string.visit_address) + getStrByResources(R.string.not_edit), R.color.load_warning);
            return false;
        } else if (e.getLatLng() == null) {
            if (iMission != null)
                iMission.showToast(getStrByResources(R.string.you_destination) + i + getStrByResources(R.string.location_error_try_agen), R.color.load_warning);
            return false;
        } else if (StringUtil.isEmpty(e.getVisitTime())) {
            if (iMission != null)
                iMission.showToast(getStrByResources(R.string.you_destination) + i +
                        getStrByResources(R.string.expected_arrival_time) + getStrByResources(R.string.not_edit), R.color.load_warning);
            return false;
        } else if (StringUtil.isEmpty(e.getRemark())) {
            if (iMission != null)
                iMission.showToast(getStrByResources(R.string.you_destination) + i +
                        getStrByResources(R.string.visit_aim) + getStrByResources(R.string.not_edit), R.color.load_warning);
            return false;
        }
        return true;
    }


    //获取外勤计划列表
    private void loadMissionByDB() {
        //没有网络情况下获取缓存的数据
        List<MissionModel> models = MissionDao.getInstance().queryByEnCode();
        if (!ListUtils.isEmpty(models)) {
            if (iMission != null) {
                iMission.dimssLoading();
                for (int i = 0; i < models.size(); i++) {
                    if (models.get(i).getStatus() == 0) {
                        models.get(i).setStatus(1);
                    }
                }
                if (modelIntent != null) {
                    if (modelIntent.getStatus() != 5) {
                        modelIntent.setStatus(0);
                    }
                    models.add(modelIntent);
                }
                iMission.showModels(models);
            }
        } else {
            if (modelIntent != null) {
                if (modelIntent.getStatus() != 5) {
                    modelIntent.setStatus(0);
                }
                models.add(modelIntent);
                iMission.showModels(models);
            } else {
                if (iMission != null) iMission.dimssLoading();
            }
        }
    }

    //获取外勤计划列表
    public void loadMissionPlan() {
        if (!isB2b) {
            iMission.showLoading();
        }
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));

        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getOutPlan :
                "mobile/mobileoutplan.action";
        LogUtil.i("url=" + url);
        Request request = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_PLAN)
                .setParam(param)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);


    }

    /*提交审批了*/
    private void submitCommon(int id) {
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "MOBILEOUTPLAN");
        param.put("id", id);
        Request request = new Request.Bulider()
                .setUrl("common/submitCommon.action")
                .setWhat(SUBMIT)
                .setParam(param)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private boolean submiting = false;

    @Override
    public void signinMission(MissionModel model, String faceBase64) {
        if (model == null) return;
        if (!MyApplication.getInstance().isNetworkActive()) {
            iMission.showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        if (submiting) return;
        submiting = true;
        iMission.showLoading();
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        String emcode = CommonUtil.getEmcode();
        String name = CommonUtil.getName();
        String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
        if (StringUtil.isEmpty(address))
            address = model.getCompanyAddr();
        String remark = model.getStatus() == 2 ? "Android手动外勤签退:手动" : "Android手动外勤签到:手动";
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
        bundle.putString("faceBase64", faceBase64);
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

    /**
     * 更新外勤计划状态
     *
     * @param mission 外勤id
     * @param isDone  是否已完成，否则未签退
     */
    public void endMission(MissionModel mission, boolean isDone) {
        if (mission == null) return;
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mission.getId());
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().updateOutplanStatus : "mobile/mobileplanUpdate.action";
        if (isB2b)
            param.put("statuscode", isDone ? "done" : "CHECKOUT");
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", mission);
        Request request = new Request.Bulider()
                .setWhat(END_MISSION)
                .setUrl(url)
                .setBundle(bundle)
                .setParam(param)
                .setMode(Request.Mode.POST)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        try {
            if (isJSON) {
                JSONObject object = JSON.parseObject(message);
                switch (what) {
                    case LOAD_PLAN:
                        JSONArray array = null;
                        if (object.get("data") instanceof JSONArray) {
                            array = object.getJSONArray("data");
                        } else if (object.get("success") instanceof JSONArray) {
                            array = object.getJSONArray("success");
                        }
                        handlerData(array);
                        submiting = false;
                        break;
                    case SAVE_PLAN:
                        isSubmiting = false;
                        if (isJSON && JSON.parseObject(message).getBoolean("success")) {
                            int mpd_id = JSON.parseObject(message).getIntValue("mpd_id");
                            int id = 0;
                            if (bundle != null)
                                id = bundle.getInt("id");
                            if (id == 0)
                                id = JSON.parseObject(message).getIntValue("mp_id") - 1;
                            MissionModel entity = bundle.getParcelable("data");
                            if (mpd_id != 0 && entity != null) {//保存数据库
                                entity.setId(mpd_id);
                                boolean saveOk = MissionDao.getInstance().updataOrCreate(entity);
                                if (saveOk) {
                                    Intent intent = new Intent();
                                    intent.setAction(AppConstant.CHANGE_MISSION_TASK);
                                    intent.putExtra(AppConstant.CHANGE_MISSION_TASK, true);
                                    MsgBroadcast.sendLocalBroadcast(intent);
                                }
                            }
                            if (PreferenceUtils.getBoolean(AppConfig.NEED_PROCESS, true) && !isB2b)
                                submitCommon(id);
                        }
                        if (iMission != null) iMission.dimssLoading();
                        int position = bundle.getInt("position", -1);//成功保存第几个目标
                        if (iMission != null)
                            iMission.showToast(R.string.save_ok, R.color.load_submit);
                        if (iMission != null) {
                            iMission.changModelStatus(1, position);
                        }
                        break;
                    case ID_WHAT:
                        if (isJSON && JSON.parseObject(message).containsKey("success") && JSON.parseObject(message).getBoolean("success")) {
                            int id = JSON.parseObject(message).getInteger("id");
                            if (id != 0 && bundle != null) {
                                bundle.putInt("id", id);
                                loadCode(bundle);
                            }
                        }
                        break;
                    case CODE_WHAT:
                        String code = JSON.parseObject(message).getString("code");
                        if (!StringUtil.isEmpty(code) && bundle != null) {
                            bundle.putString("code", code);
                            savePlan2Net(bundle);
                        }
                        break;
                    case SIGNIN_MISSION:
                        if (JSONUtil.getBoolean(message, "success")) {
                            //成功  更新数据库
                            if (bundle != null) {
                                MissionModel mission = bundle.getParcelable("data");
                                if (mission != null) {
                                    //数据库插入手动外勤记录
                                    MessageDao.getInstance().instartSignin(getString(R.string.manual_mission_alert)
                                            , AutoErpSigninUitl.getMissionSubTitle(mission));
                                    if (mission.getStatus() == 2) {//签退
                                        iMission.showToast("手动签退成功", R.color.load_error);
                                        endMission(mission, false);
                                    } else {
                                        iMission.showToast("手动签到成功", R.color.load_error);
                                        loadMissionPlan();
                                    }

                                    String faceBase64 = bundle.getString("faceBase64", null);
                                    if (!TextUtils.isEmpty(faceBase64)) {
                                        iMission.faceUpload(mission, faceBase64);
                                    }
                                }
                            }
                        }
                        break;
                    case END_MISSION:
                        if (bundle != null) {
                            MissionModel mission = bundle.getParcelable("data");
                            if (mission != null) {
                                mission.setStatus(2);
                                mission.setRealLeave(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
                                MissionDao.getInstance().updata(mission);
                            }
                        }
                        loadMissionPlan();
                        break;
                    case FIND_LIKER:
                        if (isJSON) {
                            JSONArray datas = JSONUtil.getJSONArray(object, "datas");
                            if (!ListUtils.isEmpty(datas)) {
                                List<SelectBean> models = new ArrayList<>();
                                for (int i = 0; i < datas.size(); i++) {
                                    if (!StringUtil.isEmpty(datas.getString(i))) {
                                        models.add(new SelectBean(datas.getString(i)));
                                    }
                                }
                                if (!ListUtils.isEmpty(models)) {
                                    iMission.showFinds(models);
                                }
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {

        } finally {
            if (iMission != null) iMission.dimssLoading();
        }
    }


    @Override
    public void error(int what, String message, Bundle bundle) {
        isSubmiting = false;
        if (what == LOAD_PLAN && !StringUtil.isEmpty(StringUtil.getChinese(message)) && StringUtil.getChinese(message).equals("程序错误")) {
            loadMissionByDB();
        } else if (what == SUBMIT) {

        } else {
            String showMessage = StringUtil.getChinese(message);
            if (iMission != null) iMission.dimssLoading();
            if (StringUtil.isEmpty(showMessage)) return;
            if (iMission != null) iMission.showToast(showMessage, R.color.load_error);
        }
    }

    @Override
    public void uploadFace(final Context context, MissionModel mission, String faceBase64) {
        if (mUploadLoading == null) {
            mUploadLoading = CommonUtil.newLoading(context, "附件上传中...");
        }
        mUploadLoading.show();
        File faceFile = null;
        try {
            faceFile = BitmapUtils.saveBitmapToFile(Base64Util.base64ToBitmap(faceBase64), "face");
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpRequest.getInstance().uploadFile(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/signUploadPicture.action")
                        .method(Method.GET)
                        .addParam("mpd_id", mission.getId())
                        .addParam("emcode", CommonUtil.getEmcode())
                        .addParam("result", "true")
                        .addParam("master", CommonUtil.getMaster())
                        .addParam("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"))
                        //人脸照
                        .addParam("img", faceFile)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        if (mUploadLoading.isShowing()) {
                            mUploadLoading.dismiss();

                            Toast.makeText(context, "附件上传成功", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        if (mUploadLoading.isShowing()) {
                            mUploadLoading.dismiss();

                            Toast.makeText(context, "附件上传失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    /**
     * 解析处理外勤计划列表并保存到数据库
     *
     * @param array
     */
    private void handlerData(JSONArray array) throws Exception {
        if (ListUtils.isEmpty(array)) {
            if (iMission != null) {
//                loadMissionByDB();

                iMission.dimssLoading();
                List<MissionModel> models = new ArrayList<>();
                MissionModel entity = new MissionModel();
                entity.setType(1);
                entity.setLocation(UasLocationHelper.getInstance().getUASLocation().getName());//当前位置
                entity.setRecorddate(TimeUtils.f_long_2_str(System.currentTimeMillis()));//当前时间
                models.add(entity);
                iMission.showModels(models);
            }
            return; //获取到拜访计划数据
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
            if (!ListUtils.isEmpty(entities))
                setModels(entities);
        }
    }


    private void setModels(final List<MissionModel> models) {
        boolean saveOk = MissionDao.getInstance().updataOrCreate(models);
        if (saveOk)
            loadMissionByDB();
    }

    private String getStrByResources(int id) {
        return MyApplication.getInstance().getResources().getString(id);
    }

    public MissionModel reckonMission(List<MissionModel> list) {
        int companyDistance = PreferenceUtils.getInt(AppConfig.ALARM_MISSION_DISTANCE, 500);
        MissionModel minBean = null;
        float minDistance = 0;
        if (ListUtils.isEmpty(list)) {
            return minBean;
        }
        for (MissionModel e : list) {
//			if (!timeAllowMission(e)) continue;//时间上不符合该外勤签到或签退
            if (e.getLatLng() == null) continue;//定位不存在
            //获取当前与目的地的距离
            float distance = LocationDistanceUtils.distanceMe(e.getLatLng());
            if (distance == -1f) return null;
            if (e.getStatus() != 2 &&
                    distance > companyDistance
                    && !StringUtil.isEmpty(e.getRealTime())) {
                //符合外勤签退  1.判断进行中的外勤计划，如果离开了签到最后一次，结束该计划
                minBean = e;
                minBean.setStatus(2);//结束该外勤
                break;
            } else if (distance < companyDistance && (e.getType() != 1 || e.getStatus() != 2)) { //判断符合自动外勤
                if (minDistance <= 0 || minDistance > distance) {//2.如果第一个不存在，判断获取最近的位置
                    minDistance = distance;
                    minBean = e;
                    if (minBean.getStatus() == 2)
                        minBean.setStatus(3);
                }
            }
        }
        return minBean;
    }

    /**
     * 判断该对应班次最后一次签到时间和当前是否有足够长的时间间隔
     *
     * @param bean
     * @return
     */
    private boolean timeAllowMission(MissionModel bean) {
        if (bean == null) return false;
        if (StringUtil.isEmpty(bean.getRealTime())) return true;
        String lastTime = StringUtil.isEmpty(bean.getRealLeave()) ? bean.getRealTime() : bean.getRealLeave();
        return long2LastTime(lastTime);
    }

    /**
     * 判断最后一次签到是否相隔足够长的时间
     *
     * @param lastSignin 最后一次签到时间  yyyy-MM-dd HH:mm:ss
     * @return
     */
    private boolean long2LastTime(String lastSignin) {
        long last = DateFormatUtil.str2Long(lastSignin, DateFormatUtil.YMD_HMS);
        if ((System.currentTimeMillis() - last) > (15 * 60 * 1000)) {
            //如果遍历到了，但是时间相差很小，就更新
            return true;
        } else {
            iMission.showToast("距离上一次打卡时间太短，请稍后再试");
            return false;
        }
    }

    private String getString(int id) {
        return MyApplication.getInstance().getString(id);
    }
}
