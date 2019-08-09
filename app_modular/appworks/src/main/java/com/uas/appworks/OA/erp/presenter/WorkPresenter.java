package com.uas.appworks.OA.erp.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.dao.WorkLocationDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.WorkLocationModel;
import com.core.model.WorkModel;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.WorkHandlerUtil;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.modular.apputils.utils.SignUtils;
import com.uas.applocation.Interface.OnSearchLocationListener;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.applocation.utils.LocationNeerHelper;
import com.uas.applocation.utils.ModelChangeUtils;
import com.uas.appworks.OA.erp.view.IWorkView;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 班次管理器
 * Created by Bitliker on 2016/12/12.
 */
public class WorkPresenter implements OnHttpResultListener {
    private final int WORK_LOG = 0x12;
    private final int WORK_DATA = 0x11;
    private final int SIGNING = 0x15;//签到
    private final int LOAD_WORK_SET = 0x16;//获取考勤设置
    private final int ADDRESS_CHANGE = 0x14;//地址微调

    private float distance = -1;

    //本地位置
    private double longitude;
    private double latitude;
    private String loaction;
    private String address;

    private List<WorkLocationModel> locationList;
    private WorkLocationModel companyLocation;


    private boolean isSubmiting = false;


    private boolean isFree = false;
    private String days;
    private String name;


    private IWorkView iWorkView;
    private boolean isB2b;

    public WorkPresenter(IWorkView iWorkView, String macAddress) {
        if (iWorkView == null) new NullPointerException("iWorkView not be null ");
        this.iWorkView = iWorkView;

    }

    private void loadLog(ArrayList<WorkModel> models, long time) {
        if (iWorkView != null)
            iWorkView.showLoading();
        String date = TimeUtils.s_long_2_str(time);
        Map<String, Object> param = new HashMap<>();
        String code = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        param.put("currentMaster", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        if (isB2b)
            param.put("pageNumber", 1);
        else
            param.put("page", 1);
        param.put("pageSize", 1000);
        if (!isB2b)
            param.put("condition", "cl_emcode='" + code + "' and to_char(cl_time,'yyyy-MM-dd')='" + date + "'");
        else
            param.put("date", DateFormatUtil.long2Str(time, "yyyyMMdd"));
        param.put("caller", "CardLog");
        param.put("emcode", code);
        param.put("master", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("models", models);
        bundle.putLong("time", time);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_record_url :
                "mobile/oa/workdata.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setWhat(WORK_LOG)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }




    /*获取高级设置*/
    private void loadWorkSet(long time) {
        if (iWorkView != null)
            iWorkView.showLoading();
        //获取考勤高级设置时间请求
        Map<String, Object> param = new HashMap<>();
        Bundle bundle = new Bundle();
        bundle.putLong("time", time);
        if (!isB2b)
            param.put("code", 1);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().get_plat_senior_setting_url :
                "/mobile/getconfigs.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setWhat(LOAD_WORK_SET)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    /*请求班次数据*/
    private void loadWorkData(long time) {
        Map<String, Object> param = new HashMap<>();
        param.put("date", DateFormatUtil.long2Str(time, "yyyyMMdd"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bundle = new Bundle();
        bundle.putLong("time", time);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().punch_schedule_url :
                "mobile/getWorkDate.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setWhat(WORK_DATA)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    public void upDateLocation() {
        try {
            setBaiduLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置位置信息
    private void setBaiduLocation() {
        boolean isLocation = UasLocationHelper.getInstance().getUASLocation().isLocationOk();
        if (!isLocation) {
            if (iWorkView != null) {
                iWorkView.showNotLocation();
            }
            return;
        }
        longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        loaction = UasLocationHelper.getInstance().getUASLocation().getName();
        address = UasLocationHelper.getInstance().getUASLocation().getAddress();

        if (mSignUtils != null) {
            PoiInfo localPoi = new PoiInfo();
            localPoi.name = loaction;
            localPoi.address = address;
            localPoi.location = new LatLng(latitude, longitude);
            mSignUtils.changeLocalPoi(localPoi);
        }


        if (iWorkView == null) return;
        distance = -1;
        iWorkView.showDistance(getDistance());
        iWorkView.showLocation(address);

    }

    //获取与公司距离
    private float getDistance() {
        try {
            if (ListUtils.isEmpty(locationList)) return -1;
            for (WorkLocationModel b : locationList) {
                float dis =LocationDistanceUtils.getDistanceBackFrist(b.getLocation(),new LatLng(latitude,longitude));
                if (dis<0) continue;
                if (distance == -1 || distance > dis) {
                    distance = dis;
                    companyLocation = b;
                }
            }
            return distance;
        } catch (Exception e) {
            return -1;
        }
    }


    public void gotoLocationActivity(Activity ct) {
        if (companyLocation == null) {
            iWorkView.showToast(R.string.not_addr_to_select, R.color.load_error);
            return;
        }
        Intent intent = new Intent(ct, SearchLocationActivity.class);
        SearchPoiParam poiParam = new SearchPoiParam();
        poiParam.setType(1);
        poiParam.setTitle(MyApplication.getInstance().getResources().getString(R.string.unoffice));
        poiParam.setRadius(300);
        poiParam.setShowRange(companyLocation.getValidrange());
        poiParam.setContrastLatLng(new LatLng(companyLocation.getLocation().longitude, companyLocation.getLocation().latitude));
        poiParam.setResultCode(ADDRESS_CHANGE);
        poiParam.setDistanceTag(MyApplication.getInstance().getResources().getString(R.string.rice));
        intent.putExtra("data", poiParam);
        ct.startActivityForResult(intent, ADDRESS_CHANGE);
    }

    private void getPoi() {
        if (companyLocation == null) return;
        //公司地址必须反过来
        final LatLng compayLng = new LatLng(companyLocation.getLongitude(), companyLocation.getLatitude());
        LatLng latLng = new LatLng(latitude, longitude);
        LocationNeerHelper.getInstance().loadDataByNeer(MyApplication.getInstance(), 500, 0, latLng, new OnSearchLocationListener() {
            @Override
            public void onCallBack(boolean isSuccess, List<UASLocation> locations) {
                LogUtil.i("gongTest","isSuccess="+isSuccess+"||\n"+JSON.toJSONString(locations));
                if (isSuccess){
                    if ( companyLocation == null) return;
                    List<PoiInfo> pois=new ArrayList<>();
                    for (int i = 0; i < locations.size(); i++) {
                        double dis = LocationDistanceUtils.getDistance(compayLng, locations.get(i).getLocation());
                        if (companyLocation.getValidrange()>= dis) {
                            pois.add(ModelChangeUtils.location2PoiInfo(locations.get(i)));
                        }
                    }
                    if (iWorkView != null) iWorkView.setPois(pois, compayLng);
                }
            }
        });
    }


    /**
     * 处理打卡签到
     *
     * @param isJSON
     * @param message
     * @param time
     */
    private void handlerWorkData(boolean isJSON, String message, long time) throws Exception {
        if (!isJSON) {
            if (iWorkView != null) {
                iWorkView.showToast(message, R.color.load_error);
                showModels(null, time);
            }
            return;
        }
        JSONObject object = null;
        object = JSON.parseObject(message);
        days = JSONUtil.getText(object, "wd_day", "day");
        name = JSONUtil.getText(object, "wd_name", "name");
        initLocation(WorkHandlerUtil.handerLocation(object, isB2b));
        ArrayList<WorkModel> models = WorkHandlerUtil.handlerWorkData(object, isB2b);
        try {
            if (ListUtils.isEmpty(locationList) && iWorkView != null && !ListUtils.isEmpty(models))
                iWorkView.showToast(R.string.not_addr_message, R.color.load_warning);
            else getPoi();
            setBaiduLocation();
        } catch (ClassCastException e) {
            if (e != null)
                LogUtil.i("handlerWorkData ClassCastException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                LogUtil.i("handlerWorkData Exception=" + e.getMessage());
        }
        //b2b平台接口出现异常
        Object ifNeedSignCard = object.get("ifNeedSignCard");
        if (ifNeedSignCard instanceof Boolean)
            isFree = !((boolean) ifNeedSignCard);
        else if (ifNeedSignCard instanceof String)
            isFree = !"是".equals(ifNeedSignCard);
        if (isFree) {
            loadLog(null, time);
            return;
        }
        if (ListUtils.isEmpty(models)) {
            if (iWorkView != null) {
                iWorkView.showToast(R.string.not_work_message, R.color.load_error);
                showModels(null, time);
            }
            return;
        }
        loadLog(models, time);
    }


    private void initLocation(List<WorkLocationModel> locationList) {
        this.locationList = locationList;
        //获取当前最近的位置信息
        if (!ListUtils.isEmpty(locationList)) {
            float dis = 0;
            boolean first = true;
            for (WorkLocationModel location : locationList) {
                float distance = LocationDistanceUtils.distanceMeBack(location.getLocation());
                if (first || dis > distance) {
                    dis = distance;
                    companyLocation = location;
                }
            }
        } else {
            companyLocation = null;
        }
    }

    /**
     * 处理打卡签到列表，建议在线程钟使用
     *
     * @param isJSON  是否是json数据
     * @param message 返回信息
     * @param logTime 日期时间戳
     * @throws Exception
     */
    private void handlerWorkLog(boolean isJSON, String message, final ArrayList<WorkModel> workModels, long logTime) throws Exception {
        if (!isJSON || (!isFree && ListUtils.isEmpty(workModels))) {
            showModels(workModels, logTime);
            return;
        }
        JSONObject object = JSON.parseObject(message);
        ArrayList<WorkModel> models;
        if (!isFree) {
            models = WorkHandlerUtil.handlerWorkLog(object, workModels);
        } else {
            models = WorkHandlerUtil.handlerFreeLog(object);
        }
        showModels(models, logTime);
    }


    private void showModels(List<WorkModel> models, long longTime) {
        if (ListUtils.isEmpty(models)) {
            WorkModelDao.getInstance().clearByDate(longTime);
        } else {
            WorkModelDao.getInstance().createOrUpdata(models, longTime);
        }
        showModels(WorkModelDao.getInstance().query(true, longTime));

    }

    private void showModels(List<WorkModel> models) {
        Collections.sort(models, new Comparator<WorkModel>() {
            @Override
            public int compare(WorkModel lhs, WorkModel rhs) {
                return lhs.getId() - rhs.getId();
            }
        });
        iWorkView.showModel(isFree, models);
        iWorkView.dimssLoading();
    }


    private long subMitTime;

    /**
     * 判断是否符合签到
     *
     * @param models
     * @return
     */
    public boolean isSubmitAble(ArrayList<WorkModel> models) {
//        if (ListUtils.isEmpty(models) && !isFree) {
//            if (iWorkView != null)
//                iWorkView.showToast(R.string.not_work_message, R.color.load_warning);
//            return false;
//        }
        if (isSubmiting) {
            if (iWorkView != null)
                iWorkView.showToast(R.string.not_signin_agin, R.color.load_warning);
            return false;
        } else if (companyLocation == null || companyLocation.getValidrange() < distance) {
            if (iWorkView != null)
                iWorkView.showToast(R.string.too_long, R.color.load_warning);
            return false;
        } else if (subMitTime != 0 && (System.currentTimeMillis() - subMitTime) < (20 * 1000)) {
            if (iWorkView != null)
                iWorkView.showToast(R.string.not_signin_agin, R.color.load_warning);
            return false;
        } else if (!MyApplication.getInstance().isNetworkActive()) {
            if (iWorkView != null)
                iWorkView.showToast(R.string.networks_out, R.color.load_warning);
            return false;
        }
        int resId = mSignUtils.judgeFrontFace();
        if (resId == -1) {
            return true;
        } else {
            iWorkView.showToast(resId);
            return false;
        }
    }


    /********************
     * 对外接口
     *********************************/

    public void start(boolean canShowTocat) {
        // 判断当前为平台用户;
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (MyApplication.getInstance().isNetworkActive()) {

            loadWorkData(System.currentTimeMillis(), canShowTocat);
        } else if (canShowTocat) {
            iWorkView.showToast(R.string.networks_out, R.color.load_warning);
        }
        if (mSignUtils == null) {
            mSignUtils = new SignUtils(mSignListener);
        }
        local();
    }


    public void loadWorkData(long time, boolean canShowTocat) {
        if (!MyApplication.getInstance().isNetworkActive() && canShowTocat) {
            iWorkView.showToast(R.string.networks_out, R.color.load_warning);
            return;
        }
        if (isB2b) {
            loadWorkData(time);
        } else {
            loadWorkSet(time);
        }
    }

    private void local() {
        initLocation(WorkLocationDao.getInstance().queryByEnCode());
        List<WorkModel> localModel = WorkModelDao.getInstance().query(true);
        if (!ListUtils.isEmpty(localModel)) {
            iWorkView.showModel(isFree, localModel);
        }
    }

    public void submit(ArrayList<WorkModel> models, String macAddress) {
//            if (isB2b) {
//                Bundle bundle = new Bundle();
//                bundle.putParcelableArrayList("models", models);
//                signinWork(bundle, "");
//            } else {
//                submitWork(models, macAddress);
//            }
        iWorkView.showLoading();
        mSignUtils.sign(isB2b, null);

    }

    private SignUtils mSignUtils = null;
    private SignUtils.SignListener mSignListener = new SignUtils.SignListener() {
        @Override
        public void sign(boolean signOk, String message) {
            if (signOk) {
                iWorkView.showModel(isFree, WorkModelDao.getInstance().query(true));
            }
            if (message.equals(StringUtil.getMessage(R.string.show_frist_mac))) {
                iWorkView.showFristMac();
            } else if (message.contains("不是考勤打卡常用设备,是否需要更换")) {
                iWorkView.showErrorMac();
            } else if (message.contains("设备正处于申请变更绑定阶段")) {
                if (iWorkView != null) {
                    iWorkView.showToast(R.string.mac_changing, R.color.load_warning);
                    iWorkView.setErrorMac(MyApplication.getInstance().getResources().getString(R.string.mac_changing));
                }
            } else if (message.contains("该设备已被他人绑定")) {
                iWorkView.showToast(R.string.mac_other, R.color.load_warning);
                iWorkView.setErrorMac(MyApplication.getInstance().getResources().getString(R.string.mac_other));
            } else {
                iWorkView.showToast(message);
            }
            iWorkView.dimssLoading();
        }
    };


    public void submitByFrist(ArrayList<WorkModel> models, String macAddress) {
        if (isSubmitAble(models)) {
            iWorkView.showLoading();
            mSignUtils.signFristMac(isB2b, null);
//            submitWork(models, macAddress);
        }
    }



    /**
     * 更改位置信息
     *
     * @param poi 位置信息
     */
    public void changPoi(PoiInfo poi) {
        if (mSignUtils!=null){
            mSignUtils.changeLocalPoi(poi);
        }
        loaction = StringUtil.isEmpty(poi.name) ? "" : poi.name;//位置名称
        address = StringUtil.isEmpty(poi.address) ? "" : poi.address;
        latitude = poi.location.latitude;
        longitude = poi.location.longitude;
        if (iWorkView == null) return;
        iWorkView.showLocation(address);
        distance = -1;
        iWorkView.showDistance(getDistance());
    }


    @Override
    public void result(int what, boolean isJSON, String message, final Bundle bundle) {
        try {
            ArrayList<WorkModel> models = null;
            if (iWorkView != null) iWorkView.dimssLoading();
            switch (what) {
                case LOAD_WORK_SET://获取打卡设置
                    long t = System.currentTimeMillis();
                    if (bundle != null) t = bundle.getLong("time");
                    if (isJSON)
                        WorkHandlerUtil.handlerWorkSet(JSON.parseObject(message));
                    loadWorkData(t);
                    break;
                case WORK_DATA://打卡班次
                    long time = System.currentTimeMillis();
                    if (bundle != null) time = bundle.getLong("time");
                    handlerWorkData(isJSON, message, time);
                    break;
                case WORK_LOG://打卡列表
                    long logTime = System.currentTimeMillis();
                    if (bundle != null) models = bundle.getParcelableArrayList("models");
                    if (bundle != null) logTime = bundle.getLong("time");
                    handlerWorkLog(isJSON, message, models, logTime);
                    break;
                case SIGNING:
                    //签到成功
                    isSubmiting = false;
                    if (isJSON && JSON.parseObject(message).containsKey("success") && JSON.parseObject(message).getBoolean("success")) {
                        if (iWorkView != null)
                            iWorkView.showToast(R.string.signin_ok, R.color.load_submit);
                        if (bundle != null) models = bundle.getParcelableArrayList("models");
                        loadLog(models, System.currentTimeMillis());
                    } else if (iWorkView != null)
                        iWorkView.showToast(R.string.signin_error, R.color.load_error);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            if (e != null)
                LogUtil.i("result= " + what + " " + e.getMessage());
        }
    }


    @Override
    public void error(int what, String message, Bundle bundle) {
        try {
            isSubmiting = false;
            subMitTime = 0;
            if (iWorkView != null) iWorkView.dimssLoading();
            if (StringUtil.isInclude(message, "该设备不是考勤打卡常用设备")) {
                if (iWorkView != null) iWorkView.showErrorMac();
            } else if (StringUtil.isInclude(message, "设备正处于申请变更绑定阶段")) {
                if (iWorkView != null) {
                    long time = 0;
                    if (bundle != null) time = bundle.getLong("time");
                    if (time != 0 && !DateFormatUtil.long2Str(DateFormatUtil.YMD).equals(TimeUtils.s_long_2_str(time))) {
                    } else {
                        iWorkView.showToast(R.string.mac_changing, R.color.load_warning);
                        iWorkView.setErrorMac(MyApplication.getInstance().getResources().getString(R.string.mac_changing));
                    }
                }
            } else if (StringUtil.isInclude(message, MyApplication.getInstance().getResources().getString(R.string.mac_other))) {
                iWorkView.showToast(R.string.mac_other, R.color.load_warning);
                iWorkView.setErrorMac(MyApplication.getInstance().getResources().getString(R.string.mac_other));
            } else {
                String mes = StringUtil.getChinese(message);
                if (!StringUtil.isEmpty(mes) && iWorkView != null)
                    iWorkView.showToast(mes, R.color.load_warning);
            }
        } catch (Exception e) {
            if (e != null) LogUtil.i("Error Exception =" + e.getMessage());
        }
    }

    /*调转显示我的考勤*/
    public void showMyRele(Intent intent, List<WorkModel> models) {
        if (models instanceof ArrayList)
            intent.putParcelableArrayListExtra("data", (ArrayList<WorkModel>) models);
        intent.putExtra("isFree", isFree);
        intent.putExtra("day", days);
        intent.putExtra("name", name);
    }
}
