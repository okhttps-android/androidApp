package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.OAConfig;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.WorkHandlerUtil;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.SwitchView;
import com.lidroid.xutils.ViewUtils;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.uas.appworks.R.id.senior_setting_leave_early_tv;

/**
 * Created by FANGlh on 2017/1/16.
 * function:打卡2.0高级设置
 */
public class SignSeniorSettingActivity extends BaseActivity implements View.OnClickListener, SwitchView.OnCheckedChangeListener {
    private static final int LATE_TIME_CODE = 11701;
    private static final int ERIOUS_LATE_TIME = 11702;
    private static final int LEAVE_EARLY_TIME = 11703;
    private static final int ABSENTEEISM_TIME = 11704;
    private static final int GET_SENIOR_CONFIGS_TIME = 11801;
    private static final int SENIOR_TIME_SAVE_REQUEST = 11901;
    private static final int AUTO_SIGN_REQUEST = 11902;
    private static final int GET_PLAT_SENIOR_SETTING = 3101;
    private static final int SAVE_PLAT_SENIOR_TIME = 3102;
    private String default_late_time = "5";
    private String default_serious_late_time = "10";
    private String default_leave_early_time = "15";
    private String default_absenteeism_time = "60";
    private SwitchView auto_sign_sw;
    private SwitchView signin_alert_sv;
    private TextView late_time_tv;
    private TextView serious_late_time_tv;
    private TextView leave_early_tv;
    private TextView absenteeism_time_tv;
    private Button save_btn;
    private boolean isAuto;
    private String caller = "MOBILE_ATTENDSYSTEM";
    private int save_return_id;
    private int auto_sign;
    private String first_autosign;
    private String first_latetime;
    private String first_overlatetime;
    private String first_earlyoff;
    private String first_nonclass;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_SENIOR_CONFIGS_TIME:
                    String getconfigs_time_result = msg.getData().getString("result");
                    LogUtil.prinlnLongMsg("getconfigs_time_result", getconfigs_time_result);
                    doShowGetConfigsTime(getconfigs_time_result);
                    break;
                case SENIOR_TIME_SAVE_REQUEST:
                    if (msg.getData() != null) {
                        String sava_time_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("sava_time_result", sava_time_result);
                        if (JSON.parseObject(sava_time_result).getBoolean("success")) {
                            Toast.makeText(ct, getString(R.string.common_save_success), Toast.LENGTH_LONG).show();
//                            latetime,seniouslatetime,leaveearlytime,absenteeismtime
                            OAConfig.latetime = latetime;
                            OAConfig.overlatetime = seniouslatetime;
                            OAConfig.earlyoff = leaveearlytime;
                            OAConfig.nonclass = absenteeismtime;
                            OAConfig.needValidateFace = signin_face_sv.isChecked();
                            try {
                                save_return_id = JSON.parseObject(sava_time_result).getIntValue("id");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finish();
                        } else {
                            Toast.makeText(ct, getString(R.string.common_save_failed), Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                        save_btn.setEnabled(true);
                    }

                    break;
                case AUTO_SIGN_REQUEST:
                    if (msg.getData() != null) {
                        String auto_sign_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("auto_sign_result", auto_sign_result);
                        if (JSON.parseObject(auto_sign_result).getBoolean("success")) {
                            if (auto_sign == 1) {
                                ToastMessage(getString(R.string.ss_sign_seted));
                                auto_sign_sw.setChecked(true);
                                OAConfig.autosign = true;
                            } else {
                                ToastMessage(getString(R.string.ss_sign_closed));
                                auto_sign_sw.setChecked(false);
                                OAConfig.autosign = false;

                            }
                        }
                    }
                    break;

                // 平台部分
                case GET_PLAT_SENIOR_SETTING:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("get_plat_senior_setting", result);
                            doShowPlatSSTime(result);
                        }
                    }
                    progressDialog.dismiss();
                    break;
                case SAVE_PLAT_SENIOR_TIME:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("save_plat_senior_time", result);
//                            Toast.makeText(getApplicationContext(), "平台考勤时间设置成功", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                        save_btn.setEnabled(true);
                        finish();
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                        progressDialog.dismiss();
                        save_btn.setEnabled(true);
                    }
                    break;
            }
        }
    };

    private void doShowPlatSSTime(String result) {

        try {
            save_return_id = JSON.parseObject(result).getIntValue("id");
            first_latetime = JSON.parseObject(result).getString("latetime");
            first_overlatetime = JSON.parseObject(result).getString("overlatetime");
            first_earlyoff = JSON.parseObject(result).getString("earlyoff");
            first_nonclass = JSON.parseObject(result).getString("nonclass");
            first_autosign = JSON.parseObject(result).getString("autosign");
            JSONObject o = JSON.parseObject(result);
            WorkHandlerUtil.handlerWorkSet(o);
            auto_sign = CommonUtil.getNumByString(first_autosign);
            if (auto_sign == 1) {
                auto_sign_sw.setChecked(true);
            } else if (auto_sign == 0) {
                auto_sign_sw.setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(first_latetime)) {
            late_time_tv.setText(first_latetime + "分钟");
        }
        if (!TextUtils.isEmpty(first_overlatetime)) {
            serious_late_time_tv.setText(first_overlatetime + "分钟");
        }

        if (!TextUtils.isEmpty(first_earlyoff)) {
            leave_early_tv.setText(first_earlyoff + "分钟");
        }

        if (!TextUtils.isEmpty(first_nonclass)) {
            absenteeism_time_tv.setText(first_nonclass + "分钟");
        }

        progressDialog.dismiss();
        save_btn.setEnabled(true);
    }

    private int latetime;
    private int seniouslatetime;
    private int leaveearlytime;
    private int absenteeismtime;
    private Boolean platform;
    private SwitchView signin_face_sv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_senior_setting);
        ViewUtils.inject(this);
        initView();
        initData();
    }


    private void initView() {

        auto_sign_sw = (SwitchView) findViewById(R.id.senior_setting_auto_sign_sw);
        signin_face_sv = (SwitchView) findViewById(R.id.signin_face_sv);
        signin_alert_sv = (SwitchView) findViewById(R.id.signin_alert_sv);
        late_time_tv = (TextView) findViewById(R.id.senior_setting_late_time_tv);
        serious_late_time_tv = (TextView) findViewById(R.id.senior_setting_serious_late_time_tv);
        leave_early_tv = (TextView) findViewById(senior_setting_leave_early_tv);
        absenteeism_time_tv = (TextView) findViewById(R.id.senior_setting_absenteeism_tv);
        save_btn = (Button) findViewById(R.id.senior_setting_save_bt);
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        setTitle(getString(R.string.super_setting));
        isAuto = true; //初次进来设置状态
        auto_sign_sw.setChecked(isAuto);
        auto_sign_sw.setOnCheckedChangeListener(this);
        signin_face_sv.setOnCheckedChangeListener(this);
        auto_sign_sw.setOnClickListener(this);

        boolean isAlert = PreferenceUtils.getBoolean(PreferenceUtils.Constants.AUTO_SIGN_SW, true);
        signin_alert_sv.setChecked(isAlert);
        signin_alert_sv.setOnCheckedChangeListener(this);

        late_time_tv.setOnClickListener(this);
        serious_late_time_tv.setOnClickListener(this);
        leave_early_tv.setOnClickListener(this);
        absenteeism_time_tv.setOnClickListener(this);
        save_btn.setOnClickListener(this);
        progressDialog.show();
    }

    private void initData() {
        late_time_tv.setText(default_late_time + getString(R.string.minute));
        serious_late_time_tv.setText(default_serious_late_time + getString(R.string.minute));
        leave_early_tv.setText(default_leave_early_time + getString(R.string.minute));
        absenteeism_time_tv.setText(default_absenteeism_time + getString(R.string.minute));
        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastMessage(getString(R.string.networks_out));
            progressDialog.dismiss();
            return;
        } else {
            //获取考勤高级设置时间请求
            if (!platform) {
                String url_getconfigs = CommonUtil.getAppBaseUrl(getApplicationContext()) + "/mobile/getconfigs.action";
                Map<String, Object> param = new HashMap<>();
                param.put("code", 1);
                LinkedHashMap headers = new LinkedHashMap();
                headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
                ViewUtil.httpSendRequest(getApplicationContext(), url_getconfigs, param, handler, headers, GET_SENIOR_CONFIGS_TIME, null, null, "post");
            } else {
                String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().get_plat_senior_setting_url;
                Map<String, Object> param = new HashMap<>();
                param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
                param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
                LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
                ViewUtil.httpSendRequest(this, url, param, handler, headers, GET_PLAT_SENIOR_SETTING, null, null, "get");
            }
        }


    }

    //每次进入高级设置界面时请求url后返回要显示的设置时间
    private void doShowGetConfigsTime(String getconfigs_time_result) {
        try {
            JSONObject object = JSON.parseObject(getconfigs_time_result);
            save_return_id = object.getIntValue("id");
            first_latetime = object.getString("latetime");
            first_overlatetime = object.getString("overlatetime");
            first_earlyoff = object.getString("earlyoff");
            first_nonclass = object.getString("nonclass");
            first_autosign = object.getString("autosign");
            boolean needValidateFace = JSONUtil.getBoolean(object, "needValidateFace");
            signin_face_sv.setOnCheckedChangeListener(null);
            signin_face_sv.setChecked(needValidateFace);
            signin_face_sv.setOnCheckedChangeListener(this);
            JSONObject o = JSON.parseObject(getconfigs_time_result);
            WorkHandlerUtil.handlerWorkSet(o);
            auto_sign = CommonUtil.getNumByString(first_autosign);
            if (auto_sign == 1) {
                auto_sign_sw.setChecked(true);
            } else if (auto_sign == 0) {
                auto_sign_sw.setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(first_latetime)) {
            if (Integer.parseInt(first_latetime) > 0) {
                late_time_tv.setText(first_latetime + getString(R.string.minute));
            } else {
                late_time_tv.setText(0 + getString(R.string.minute));
            }

        }
        if (!TextUtils.isEmpty(first_overlatetime)) {
            if (Integer.parseInt(first_overlatetime) > 0) {
                serious_late_time_tv.setText(first_overlatetime + getString(R.string.minute));
            } else {
                serious_late_time_tv.setText(0 + getString(R.string.minute));
            }
        }

        if (!TextUtils.isEmpty(first_earlyoff)) {
            if (Integer.parseInt(first_earlyoff) > 0) {
                leave_early_tv.setText(first_earlyoff + getString(R.string.minute));
            } else {
                leave_early_tv.setText(0 + getString(R.string.minute));
            }
        }

        if (!TextUtils.isEmpty(first_nonclass)) {
            if (Integer.parseInt(first_nonclass) > 0) {
                absenteeism_time_tv.setText(first_nonclass + getString(R.string.minute));
            } else {
                absenteeism_time_tv.setText(0 + getString(R.string.minute));
            }
        }

        progressDialog.dismiss();
        save_btn.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int requestCode = 0;
        String[] time = null;
        if (v.getId() == R.id.senior_setting_late_time_tv) {
            time = getResources().getStringArray(R.array.late_time);
            requestCode = LATE_TIME_CODE;
        } else if (v.getId() == R.id.senior_setting_serious_late_time_tv) {
            time = getResources().getStringArray(R.array.serious_late_time);
            requestCode = ERIOUS_LATE_TIME;
        } else if (v.getId() == R.id.senior_setting_leave_early_tv) {
            time = getResources().getStringArray(R.array.serious_leave_early_time);
            requestCode = LEAVE_EARLY_TIME;
        } else if (v.getId() == R.id.senior_setting_absenteeism_tv) {
            time = getResources().getStringArray(R.array.absenteeism_time);
            requestCode = ABSENTEEISM_TIME;
        } else if (v.getId() == R.id.senior_setting_save_bt) {
            doTimejudgment();
        } else if (v.getId() == R.id.senior_setting_auto_sign_sw) {
            if (!platform) {
                doCheckjudge();
            } else {
                if (auto_sign == 1) {
                    ToastMessage(getString(R.string.close_sign_notice1));
                    auto_sign = 0;
                    auto_sign_sw.setChecked(false);
                } else if (auto_sign == 0) {
                    ToastMessage(getString(R.string.open_sign_notice1));
                    auto_sign = 1;
                    auto_sign_sw.setChecked(true);
                }
            }
        }
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        if (time != null) {
            for (String e : time) {
                bean = new SelectBean();
                bean.setName(e);
                bean.setClick(false);
                beans.add(bean);
            }
            Intent intent = new Intent(ct, SelectActivity.class);
            intent.putExtra("type", 2);
            intent.putParcelableArrayListExtra("data", beans);
            intent.putExtra("title", getString(R.string.ss_select_time));
            startActivityForResult(intent, requestCode);
        }
    }

    private void doCheckjudge() {
        if (auto_sign == 1) {
            PopupWindowHelper.showAlart(SignSeniorSettingActivity.this, getString(R.string.common_notice)
                    , getString(R.string.ss_sign_close_notice2), new PopupWindowHelper.OnSelectListener() {
                        @Override
                        public void select(boolean selectOk) {
                            if (selectOk) {
                                if (!platform) {
                                    auto_sign = 0;
                                    doAutoSignSetting();
                                } else {
                                    ToastMessage(getString(R.string.ss_sign_closed));
                                }
                            }
                        }
                    });
        } else if (auto_sign == 0) {
            if (!platform) {
                auto_sign = 1;
                doAutoSignSetting();
            } else {
                ToastMessage(getString(R.string.ss_sign_seted));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) return;
            String selected_time = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
            if (requestCode == LATE_TIME_CODE) {
                late_time_tv.setText(selected_time);
            } else if (requestCode == ERIOUS_LATE_TIME) {
                serious_late_time_tv.setText(selected_time);
            } else if (requestCode == LEAVE_EARLY_TIME) {
                leave_early_tv.setText(selected_time);
            } else if (requestCode == ABSENTEEISM_TIME) {
                absenteeism_time_tv.setText(selected_time);
            }
        }
    }

    private void doTimejudgment() {
        latetime = CommonUtil.getNumByString(late_time_tv.getText().toString());
        seniouslatetime = CommonUtil.getNumByString(serious_late_time_tv.getText().toString());
        leaveearlytime = CommonUtil.getNumByString(leave_early_tv.getText().toString());
        absenteeismtime = CommonUtil.getNumByString(absenteeism_time_tv.getText().toString());
        if (seniouslatetime > latetime && absenteeismtime > leaveearlytime && absenteeismtime > seniouslatetime) {
            doSaveSeniorSetting(latetime, seniouslatetime, leaveearlytime, absenteeismtime);
        } else {
            if (seniouslatetime <= latetime) {
                ToastMessage(getString(R.string.ss_time_notice1));
            }
            if (absenteeismtime <= seniouslatetime) {
                ToastMessage(getString(R.string.ss_time_notice2));
            }
            if (absenteeismtime <= leaveearlytime) {
                ToastMessage(getString(R.string.ss_time_notice3));
            }
        }
    }

    //保存操作
    private void doSaveSeniorSetting(int t1, int t2, int t3, int t4) {
        progressDialog.show();
        save_btn.setEnabled(false);
        Map<String, Object> formStoreMap = new HashMap<>();
        if (!platform) {
            formStoreMap.put("MA_LATETIME", t1);
            formStoreMap.put("MA_SERIOUSLATETIME", t2);
            formStoreMap.put("MA_EARLYTIME", t3);
            formStoreMap.put("MA_ABSENTTIME", t4);
            formStoreMap.put("MA_NEEDVALIDATEFACE", signin_face_sv.isChecked() ? 1 : 0);
            String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/saveconfigs.action";
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", caller);
            params.put("formStore", formStore);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, handler, headers, SENIOR_TIME_SAVE_REQUEST, null, null, "post");
        } else {
            formStoreMap.put("latetime", t1);
            formStoreMap.put("overlatetime", t2);
            formStoreMap.put("earlyoff", t3);
            formStoreMap.put("nonclass", t4);
            formStoreMap.put("autosign", auto_sign);
            formStoreMap.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
            formStoreMap.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());

            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().save_plat_senior_time_url;
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("formStore", formStore);
//            params.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
//            params.put("emcode",CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(this, url, params, handler, headers, SAVE_PLAT_SENIOR_TIME, null, null, "post");
        }

    }

    @Override
    public void onCheckedChanged(View view, final boolean isChecked) {
        if (view.getId() == R.id.signin_alert_sv) {
            PreferenceUtils.putBoolean(PreferenceUtils.Constants.AUTO_SIGN_SW, isChecked);
        } else if (R.id.signin_face_sv == view.getId()) {
            //TODO
            ToastUtil.showToast(ct, "请点击下方保存按钮以保存数据");
        }
    }

    private int getIntByJson(JSONObject object, String key) {
        if (object.containsKey(key))
            return object.getIntValue(key);
        else return 1;
    }

    private void doAutoSignSetting() {
        Map<String, Object> formStoreMap = new HashMap<>();
        formStoreMap.put("ma_id", save_return_id);
        formStoreMap.put("AUTOCARDLOG", auto_sign);

        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/autoCardLog.action";
        HashMap<String, Object> params = new HashMap<>();
        String formStore = JSON.toJSONString(formStoreMap);
        params.put("caller", caller);
        params.put("formStore", formStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, handler, headers, AUTO_SIGN_REQUEST, null, null, "post");
    }
}
