package com.modular.apputils.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.SystemUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.dao.UserDao;
import com.core.dao.WorkLocationDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.User;
import com.core.model.WorkLocationModel;
import com.core.model.WorkModel;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.modular.apputils.R;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.core.net.http.ViewUtil.ct;
import static com.core.utils.CommonUtil.getSharedPreferences;


/**
 * 只负责判断|签到
 * 1.判断距离
 * 2.判断mac
 * Created by Bitlike on 2018/5/3.
 */

public class SignUtils implements OnHttpResultListener {
    private final long INTERVAL = 10 * 1000;//间隔时间


    private final int LOAD_MAC = 14;//获取网络mac地址
    private final int MAC_VAL = 13;//判断mac地址
    private final int LOAD_WORKLOG = 12;//获取打卡列表
    private final int SIGNING = 11;//打卡


    private SignListener mSignListener;
    public PoiInfo localPoi;


    private long lastTime = 0;//最后一次打卡时间

    public SignUtils(SignListener mSignListener) {
        this.mSignListener = mSignListener;
        getMac();
    }

    public interface SignListener {
        void sign(boolean signOk, String message);
    }

    public void changeLocalPoi(PoiInfo localPoi) {
        if (localPoi != null && localPoi.location != null && localPoi.location.latitude > 0 && localPoi.location.longitude > 0) {
            this.localPoi = localPoi;
        }
    }

    //人脸打卡 1.先判断班次  2.判断距离
    public int judgeFrontFace() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            return R.string.networks_out;
        } else {
            List<WorkLocationModel> beanList = WorkLocationDao.getInstance().queryByEnCode();
            if (ListUtils.isEmpty(beanList)) {
                return R.string.not_addr_message;
            } else {

                if (!ListUtils.isEmpty(beanList)) {
                    for (WorkLocationModel bean : beanList) {
                        float dis= LocationDistanceUtils.distanceMeBack(bean.getLocation());
                        if ( dis< bean.getValidrange()) {
                            return -1;
                        }
                    }
                }
                return R.string.too_long;
            }
        }
    }

    //确认第一次签到
    private void signFristMac(Bundle bundle) {
        boolean isB2b = false;
        WorkModel model = null;
        if (bundle != null) {
            isB2b = bundle.getBoolean(KEY.IS_B2B, false);
            model = bundle.getParcelable(KEY.WORK);
        }
        signFristMac(isB2b, model);
    }

    public void signFristMac(final boolean isB2b, WorkModel model) {
        if (System.currentTimeMillis() - lastTime < INTERVAL) {
            callBack(false, R.string.not_signin_agin);
            return;
        }
        if (!MyApplication.getInstance().isNetworkActive()) {
            callBack(false, StringUtil.getMessage(R.string.networks_out));
        } else {

            UasLocationHelper.getInstance().requestLocation();
            List<WorkLocationModel> beanList = WorkLocationDao.getInstance().queryByEnCode();
            if (ListUtils.isEmpty(beanList)) {
                callBack(false, R.string.not_addr_message);
                //没有打卡地址，TODO 获取班次接口
            } else {
                LatLng latLng = null;
                //判断位置
                if (this.localPoi == null) {
                    UASLocation helper = UasLocationHelper.getInstance().getUASLocation();
                    latLng = helper.getLocation();
                } else {
                    latLng = this.localPoi.location;
                }
                if (latLng == null) {
                    callBack(false, R.string.not_addr_message);
                    return;
                }
                float minDis = -1;
                for (WorkLocationModel bean : beanList) {
                    Float dis =  LocationDistanceUtils.getDistance(new LatLng(bean.getLocation().longitude,bean.getLocation().latitude), latLng);
                    if (dis < bean.getValidrange() && (minDis < 0 || dis < minDis)) {
                        minDis = dis;
                    }
                }
                //判断打卡距离结束
                if (minDis != -1) {
                    lastTime = System.currentTimeMillis();
                    //时间和距离上符合打卡
                    if (SwitchUtil.needMacForSign()) {
                        validatorMac(isB2b, minDis, model);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(KEY.IS_B2B, isB2b);
                        bundle.putFloat(KEY.DISTANCE, minDis);
                        bundle.putParcelable(KEY.WORK, model);
                        toSignWork(bundle);
                    }
                } else {
                    callBack(false, R.string.too_long);
                }
            }
        }
    }

    //签到，判断是否第一次和当前mac地址时候存在问题
    public void sign(final boolean isB2b, WorkModel model) {
        if (SwitchUtil.needMacForSign()) {
            loadMacInNet(isB2b, model);
        } else {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY.IS_B2B, isB2b);
            bundle.putParcelable(KEY.WORK, model);
            signFristMac(bundle);
        }
    }


    /*判断mac地址是否符合*/
    private void validatorMac(boolean isB2b, float minDis, WorkModel model) {
        Map<String, Object> param = new HashMap<>();
        param.put("macAddress", macAddress);
        param.put("emcode", getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY.IS_B2B, isB2b);
        bundle.putFloat(KEY.DISTANCE, minDis);
        bundle.putParcelable(KEY.WORK, model);
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

    private String macAddress = null;

    public String getMac() {
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = SystemUtil.getMac(MyApplication.getInstance());
            if (StringUtil.isEmpty(macAddress)) {
                //当没有获取到MACAddress的时候，判断wifi状态，如果wifi为未启动
                //mac地址不合法时候   提示开启wifi
                WifiReceiverUtil wifiReceiver = new WifiReceiverUtil();
                wifiReceiver.regReceiver(ct, new WifiReceiverUtil.OnWifiStatusChangeLinstener() {
                    @Override
                    public void callBack(boolean isOpen) {
                        if (isOpen) {
                            macAddress = SystemUtil.getMac(ct);
                        }
                    }
                });
            }
        }
        return macAddress;
    }

    private void toSignWork(final Bundle bundle) {
        final boolean isB2b = bundle.getBoolean(KEY.IS_B2B, false);
        final float minDis = bundle.getFloat(KEY.DISTANCE, 1);
        if (isB2b) {
            signinWork(isB2b, bundle, "", minDis);
        } else {
            CommonInterface.getInstance().getCodeByNet("CardLog", new CommonInterface.OnResultListener() {
                @Override
                public void result(boolean isOk, int result, String message) {
                    signinWork(isB2b, bundle, message, minDis);
                }
            });
        }
    }

    //签到
    private void signinWork(boolean isB2b, Bundle bunder, String code, float dis) {
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
            } else {
                phone = getSharedPreferences(MyApplication.getInstance(), "user_phone");
            }
        }
        form.put("cl_phone", phone);
        form.put("cl_emcode", getSharedPreferences(ct, "erp_username"));
        form.put("cl_emname", CommonUtil.getName());
        form.put("cl_distance", dis);
        boolean isp = isHasLocation(form);
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
    private boolean isHasLocation(Map<String, Object> form) {
        try {
            //判断是否符合打卡
            String address = null;
            String name = null;
            if (this.localPoi == null) {
                UASLocation helper =UasLocationHelper.getInstance().getUASLocation();
                address = helper.getAddress();
                name = helper.getName();
            } else {
                name = this.localPoi.name;
                address = this.localPoi.address;
            }
            if (StringUtil.isEmpty(address)) {
                address = PreferenceUtils.getString("bdaddress");
            }
            form.put("cl_address", address);
            form.put("cl_location", name);
            return true;

        } catch (NullPointerException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }


    //获取打卡记录
    private void loadLog(Bundle bunder) {
        boolean isB2b = false;
        if (bunder != null) {
            isB2b = bunder.getBoolean(KEY.IS_B2B);
        }
        String date = DateFormatUtil.long2Str(DateFormatUtil.YMD);
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

    //判断mac地址是否已经上传
    public void loadMacInNet(final boolean isB2b, WorkModel model) {
        //获取网络数据
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY.IS_B2B, isB2b);
        bundle.putParcelable(KEY.WORK, model);
        Map<String, Object> param = new HashMap<>();
        param.put("macaddress", macAddress);
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        String url = "mobile/queryMobileMac.action";
        Request request = new Request.Bulider()
                .setUrl(url)
                .setBundle(bundle)
                .setWhat(LOAD_MAC)
                .setMode(Request.Mode.GET)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        try {
            switch (what) {
                case SIGNING:
                    loadLog(bundle);
                    break;
                case LOAD_WORKLOG:
                    if (isJSON && JSON.parseObject(message).containsKey("listdata")) {
                        saveSignin2DB(bundle, JSON.parseObject(message).getJSONArray("listdata"));
                    }
                    break;

                case MAC_VAL:
                    if (isJSON && JSONUtil.getBoolean(message, "success")) {
                        toSignWork(bundle);
                    } else {
                        CommonUtil.saveAutoLogtoLocal(StringUtil.getMessage(R.string.auto_sign_failed), message);
                    }
                    break;
                case LOAD_MAC://判断是否是第一次请求数据
                    if (isJSON) {
                        JSONObject object = JSON.parseObject(message);
                        if (object.containsKey("success") && object.getBoolean("success")) {
                            //返回成功
                            JSONArray array = object.getJSONArray("macaddress");
                            if ("0".equals(array.getJSONObject(0).getString("MACADDRESS"))) {
                                callBack(false, R.string.show_frist_mac);
                            } else {
                                signFristMac(bundle);

                            }
                        }
                    } else {
                        callBack(false, "签到失败+" + message);
                    }
                    break;
            }
        } catch (Exception e) {
            if (this.mSignListener != null && e != null) {
                this.mSignListener.sign(false, "e=" + e.getMessage());
            }
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        lastTime = 0;
        callBack(false, message);
    }

    //保存到数据库
    private void saveSignin2DB(Bundle bundle, JSONArray array) throws Exception {
        WorkModel model = null;
        if (bundle != null && bundle.getParcelable(KEY.WORK) != null) {
            model = bundle.getParcelable(KEY.WORK);
        }
        if (model == null) {
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
            WorkModelDao.getInstance().update(model);
        } else if (StringUtil.isEmpty(model.getOffSignin()) && model.getOffTime().compareTo(time) <= 0
                && model.getOffend().compareTo(time) >= 0) {
            model.setOffSignin(time);
            WorkModelDao.getInstance().update(model);
        }
        callBack(true, "打卡成功");
    }

    private void callBack(boolean signOk, @StringRes int messageId) {
        if (MyApplication.getInstance() != null) {
            callBack(signOk, StringUtil.getMessage(messageId));
        }

    }

    private void callBack(boolean signOk, String message) {
        if (mSignListener != null) {
            mSignListener.sign(signOk, message);
        }
    }


    private interface KEY {
        String IS_B2B = "isB2b";
        String WORK = "work";
        String DISTANCE = "distance";
    }
}
