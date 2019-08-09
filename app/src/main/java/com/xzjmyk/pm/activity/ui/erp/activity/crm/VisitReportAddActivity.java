package com.xzjmyk.pm.activity.ui.erp.activity.crm;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonInterface;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.SingleDialog;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.model.SelectAimModel;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.modular.apputils.utils.PopupWindowHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.SelectAimActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.SelectRemarkActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 拜访报告页面
 */
public class VisitReportAddActivity extends BaseActivity implements View.OnClickListener, RecognizerDialogListener {

    private static final int LOAD_JIEDUAN = 67;
    private static final int RES_SUBMIT_VISITRECORD = 0x213;
    private static final int UPDATE_VISITRECORD = 0x214;
    private static final int DELETE_VISITRECORD = 0x215;
    private static final int CURRENT_DUR = 0x216;
    @ViewInject(R.id.tv_address_login)
    private FormEditText tv_address_login;
    @ViewInject(R.id.tv_customer_login)
    private FormEditText tv_customer_login;
    @ViewInject(R.id.tv_linksman_login)
    private FormEditText tv_linksman_login;
    @ViewInject(R.id.tv_date_start)
    private FormEditText tv_date_start;
    @ViewInject(R.id.tv_date_end)
    private FormEditText tv_date_end;
    @ViewInject(R.id.tv_visit_theme)
    private FormEditText tv_visit_theme;
    @ViewInject(R.id.tv_visit_steps)
    private FormEditText tv_visit_steps;
    @ViewInject(R.id.tv_visit_content)
    private FormEditText tv_visit_content;
    @ViewInject(R.id.tv_relate_business)
    private FormEditText tv_relate_business;
    @ViewInject(R.id.phone_tv)
    private FormEditText phone_tv;//手机号

    @ViewInject(R.id.tv_visit_type)
    private FormEditText tv_visit_type;
    @ViewInject(R.id.save_btn)
    private Button save_btn;
    @ViewInject(R.id.delete_btn)
    private Button delete_btn;

    private int vp_id = 0;
    private int mVrId;
    private String vr_code;
    private boolean isB2b;
    private boolean isOutplan;
    private boolean isMe = true;
    /*
     *单据状态和显示按钮的对应：
     * 0.无状态==》提交==》saveVisitRecord.action=>submitVisitRecord
     * 1.在录入||反提交后(在录入)==》删除&&更新==》deleteVisitRecord.action||updateVisitRecord.action
     * 2.已提交==》反提交==》resSubmitVisitRecord.action
     * 3.已审核==》所有的按钮没有了
     */
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_report_add);
        AndroidBug5497Workaround.assistActivity(this);
        ViewUtils.inject(this);
        initData();
        initView();
        initListener();
    }

    private void initData() {
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        Intent intent = getIntent();
        if (intent == null || intent.getIntExtra("type", -1) == -1) {
            return;
        }
        isOutplan = intent.getBooleanExtra("isOutplan", false);
        isMe = intent.getBooleanExtra("isMe", true);
        String chche = getIntent().getStringExtra("data");
        if (!JSONUtil.validate(chche)) return;
        JSONObject object = JSON.parseObject(chche);
        //共有
        tv_date_start.setText(TimeUtils.f_long_2_str(JSONUtil.getTime(object, "vr_visittime", "vr_startdate", "mpd_actdate", "mp_firsttime")));
        tv_date_end.setText(TimeUtils.f_long_2_str(JSONUtil.getTime(object, "vr_visitend", "vr_enddate", "mpd_outdate", "mp_lasttime")));
        tv_customer_login.setText(JSONUtil.getText(object, "custname", "vr_cuname", "mpd_company"));
        tv_address_login.setText(JSONUtil.getText(object, "address", "vr_cuaddress", "mpd_address"));
        tv_visit_theme.setText(JSONUtil.getText(object, "vr_title", "mpd_remark"));
        //独享
        tv_linksman_login.setText(JSONUtil.getText(object, "vr_cucontact"));
        tv_visit_steps.setText(JSONUtil.getText(object, "vr_nichestep"));
        tv_visit_type.setText(JSONUtil.getText(object, "vr_class"));
        tv_visit_content.setText(JSONUtil.getText(object, "vr_detail"));
        nichecode = object.getString(JSONUtil.getText(object, "vr_nichecode"));
        tv_relate_business.setText(JSONUtil.getText(object, "vr_nichename"));
        phone_tv.setText(JSONUtil.getText(object, "vr_tel"));
        //变量
        String vr_status = JSONUtil.getText(object, "vr_status");
        vp_id = JSONUtil.getInt(object, "mpd_id");
        mVrId = JSONUtil.getInt(object, "id");
        vr_code = JSONUtil.getText(object, "vr_code");
        if (StringUtil.isEmpty(vr_status)) {
            status = isB2b ? 2 : 0;
        } else if ("在录入".equals(vr_status)) {
            status = 1;
        } else if ("已提交".equals(vr_status)) {
            status = 2;
        } else if ("已审核".equals(vr_status)) {
            status = 3;
        }
    }

    private void initView() {
        setTitle(getString(R.string.visitrecord));
        findViewById(R.id.voice_search_iv).setOnClickListener(this);
        if (isB2b) {
            findViewById(R.id.stage_rl).setVisibility(View.GONE);
            findViewById(R.id.business_rl).setVisibility(View.GONE);
            findViewById(R.id.phone_rl).setVisibility(View.GONE);
        }
        initStatus(status);
        if (mVrId == 0 && !isB2b) {
            CommonUtil.getCommonId(this, "VISITRECORD_SEQ", mHandler, GET_VR_ID);
        }
    }

    private void initStatus(int status) {
        this.status = status;
        if (isB2b) {
            save_btn.setText(R.string.common_save_button);
            delete_btn.setVisibility(View.GONE);
            delete_btn.setOnClickListener(null);
            if (status == 2) save_btn.setVisibility(View.GONE);
        } else
            switch (status) {
                case 0:
                    save_btn.setText(R.string.common_save_button);
                    delete_btn.setVisibility(View.GONE);
                    delete_btn.setOnClickListener(null);
                    break;
                case 1:
                    save_btn.setText(R.string.common_update_button);
                    delete_btn.setVisibility(View.VISIBLE);
                    delete_btn.setOnClickListener(this);
                    break;
                case 2:
                    save_btn.setText(R.string.unsubmit);
                    delete_btn.setVisibility(View.GONE);
                    delete_btn.setOnClickListener(null);
                    break;
                case 3:
                    save_btn.setVisibility(View.GONE);
                    delete_btn.setVisibility(View.GONE);
                    delete_btn.setOnClickListener(null);
                    break;
            }
    }

    /**
     * 存在的情况：
     * 1.新增单据进来的==》所有按钮可以点击没有限制，提交按钮为提交，隐藏删除按钮
     * 2.点击oa首页拜访报告单进来的==》已提交（所有内容不能修改，显示反提交） 在录入（所有内容都可以修改，显示更新和删除按钮）
     * 3.点击oa首页外勤计划单进来添加拜访报告的==》个别存在的字段不可以点击更改，其他可以修改
     */
    private void initListener() {
        if (!isMe || status == 2 || status == 3) {
            //当不可编辑tv_date_end
            tv_date_start.setFocusable(false);
            phone_tv.setFocusable(false);
            tv_date_end.setFocusable(false);
            tv_customer_login.setFocusable(false);
            tv_visit_steps.setFocusable(false);
            tv_relate_business.setFocusable(false);
            tv_visit_content.setFocusable(false);
            tv_visit_theme.setFocusable(false);
            tv_address_login.setFocusable(false);
            tv_linksman_login.setFocusable(false);
            tv_visit_type.setFocusable(false);
            tv_visit_steps.setOnClickListener(this);
            if (status != 2) {
                save_btn.setFocusable(false);
                delete_btn.setFocusable(false);
            } else {
                save_btn.setOnClickListener(this);
                delete_btn.setOnClickListener(this);
            }
        } else {
            phone_tv.setFocusableInTouchMode(true);
            tv_visit_steps.setFocusable(false);
            tv_visit_content.setFocusableInTouchMode(true);
            tv_address_login.setFocusableInTouchMode(true);
            tv_linksman_login.setFocusableInTouchMode(true);
            tv_relate_business.setKeyListener(null);
            tv_relate_business.setOnClickListener(this);
            tv_visit_type.setOnClickListener(this);
            tv_visit_type.setKeyListener(null);
            tv_visit_type.setFocusable(false);
            tv_visit_theme.setOnClickListener(this);
            tv_visit_theme.setKeyListener(null);
            tv_visit_theme.setFocusable(false);
            save_btn.setOnClickListener(this);
            delete_btn.setOnClickListener(this);
            tv_visit_steps.setOnClickListener(this);
            if (isOutplan) {
                setFocusable(tv_date_start, isMe && TextUtils.isEmpty(tv_date_start.getText()));
                setFocusable(tv_date_end, isMe && TextUtils.isEmpty(tv_date_end.getText()));
                setFocusable(tv_customer_login, isMe && TextUtils.isEmpty(tv_customer_login.getText()));
                setFocusable(tv_address_login, isMe && TextUtils.isEmpty(tv_address_login.getText()));
                setFocusable(tv_visit_theme, isMe && TextUtils.isEmpty(tv_visit_theme.getText()));
                setFocusable(tv_visit_type, isMe && TextUtils.isEmpty(tv_visit_type.getText()));
            } else {
                tv_date_start.setOnClickListener(this);
                tv_date_start.setKeyListener(null);
                tv_date_end.setOnClickListener(this);
                tv_date_end.setKeyListener(null);
                tv_customer_login.setOnClickListener(this);
            }

        }
    }


    private void setFocusable(FormEditText v, boolean focusable) {
        if (focusable) {
            v.setOnClickListener(this);
            v.setKeyListener(null);
        } else
            v.setFocusable(focusable);

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (getIntent().getBooleanExtra("isMe", true) && isB2b) {
//            getMenuInflater().inflate(R.menu.main_btn_submit, menu);
//            if (!getIntent().getBooleanExtra("isAgen", false)) {
//                menu.getItem(0).setTitle(getString(R.string.common_save_button));
//            }
//        }
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_save:
                saveVisit();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void saveVisit() {
        if (canSubmit()) {
            if (isB2b) {
                saveB2bByHttp();
            } else
                CommonInterface.getInstance().getCodeByNet("VisitRecord", new CommonInterface.OnResultListener() {
                    @Override
                    public void result(boolean isOk, int result, String message) {
                        sendHttpResquest(Constants.HTTP_SUCCESS_INIT, message);
                    }
                });
        }
    }

    private static final int GET_VR_ID = 62;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(getString(R.string.common_save_success));
                    if (!isB2b && vp_id != 0)//更新接口
                        updataCode();
                    else if (!isB2b && !getIntent().getBooleanExtra("isAgen", false) && !isB2b) {//第一次
                        submit();
                    } else
                        endActivity();
                    break;
                case 0x16://更新状态接口
                    ToastMessage(getString(R.string.update_success));
                    if (!getIntent().getBooleanExtra("isAgen", false)) {//第一次
                        submit();
                    } else
                        endActivity();
                    break;
                case 0x17:
                    endActivity();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(msg.getData().getString("result"));
                    break;
                case 3:
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String json = msg.getData().getString("result");
                    array = JSON.parseObject(json).getJSONArray("customers");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            lists.add(array.getJSONObject(i).getString("CU_NAME"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add("无");
                    }
                    showSimpleDialog(tv_customer_login, "客户列表");
                    break;
                case GET_VR_ID:
                    String resultStr = msg.getData().getString("result");
                    JSONObject resultJsonObject = JSON.parseObject(resultStr);
                    mVrId = resultJsonObject.getInteger("id");
                    break;
                case LOAD_JIEDUAN:
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String jieduanJson = msg.getData().getString("result");
                    array = JSON.parseObject(jieduanJson).getJSONArray("stages");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            lists.add(array.getJSONObject(i).getString("BS_NAME"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add("无");
                    }
                    showSimpleDialog(tv_visit_steps, "商机阶段");
                    break;
                case RES_SUBMIT_VISITRECORD://反提交（重新提交）
                    initStatus(1);//反提交后，状态变成在录入
                    initListener();
                    break;
                case UPDATE_VISITRECORD://更新，保存数据成功
                    ToastUtil.showToast(ct, R.string.update_success);
                    submit();
                    initStatus(2);
                    initListener();
                    break;
                case DELETE_VISITRECORD://删除单据
                    ToastUtil.showToast(ct, R.string.delete_succeed_notice1);
                    endActivity();
                    break;
                case CURRENT_DUR:
                    if (!StringUtil.isEmpty(msg.getData().toString())) {
                        String result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("CURRENT_DUR", result + "");

                        doSelectCurDur(result);
                    }
                    break;
            }
        }
    };

    //当前阶段请求后的数据处理并跳转传值
    private void doSelectCurDur(String result) {

        JSONObject object = JSON.parseObject(result);
        if (object == null) return;
        JSONArray array = object.getJSONArray("stages");
        if (ListUtils.isEmpty(array)) return;

        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < array.size(); i++) {
            selectBean = new SelectBean();
            if (!StringUtil.isEmpty(array.getJSONObject(i).getString("BS_NAME"))) {
                selectBean.setName(array.getJSONObject(i).getString("BS_NAME"));
                formBeaan.add(selectBean);
            }

            if (i == array.size() - 1 && !ListUtils.isEmpty(formBeaan)) {
                Intent intent = new Intent();
                intent.setClass(this, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putExtra("title", getString(R.string.current_stage));
                intent.putParcelableArrayListExtra("data", formBeaan);
                startActivityForResult(intent, 0x828);
            }
        }
    }

    private void endActivity() {
        if (isOutplan && isB2b)
            CommonInterface.getInstance().endMission(vp_id, true);
        Intent intent = new Intent();
        intent.putExtra("data", "data");
        setResult(0x20, intent);
        finish();
    }


    private boolean canSubmit() {
        if (!CommonUtil.isNetWorkConnected(ct)) {
            showToast(R.string.networks_out);
            return false;
        }
        String start = tv_date_start.getText().toString().trim();
        String end = tv_date_end.getText().toString().trim();
        String time = TimeUtils.f_long_2_str(System.currentTimeMillis());
        if (!StringUtil.isEmpty(start) && time.compareTo(start) < 0) {
            showToast(getString(R.string.startT_cannott_big_sT));
            return false;
        }
        if (!StringUtil.isEmpty(end) && time.compareTo(end) < 0) {
            showToast(getString(R.string.endT_cannott_big_sT));
            return false;
        }
        if (!StringUtil.isEmpty(end) && !StringUtil.isEmpty(start) && start.compareTo(end) >= 0) {
            showToast(R.string.not_time_start_biger_end);
            return false;
        }
        if (!isB2b && (TextUtils.isEmpty(phone_tv.getText())
                || !StringUtil.isMobileNumber(phone_tv.getText().toString()))) {
            ToastUtil.showToast(ct, R.string.phone_number_format_error);
            return false;
        }
        if (TextUtils.isEmpty(tv_visit_content.getText())) {
            showToast(getString(R.string.input_vist_context));
            return false;
        }
        if (!validatorInput()) {
            ToastMessage(getString(R.string.limit_unno_zijie));
        }
        return tv_customer_login.testValidity()
                && tv_linksman_login.testValidity()
                && tv_address_login.testValidity()
                && tv_visit_theme.testValidity()
                ;
    }


    private boolean validatorInput(String... message) {
        if (message == null || message.length <= 0) return true;
        for (String e : message) {
            if (!StringUtil.isEmpty(e) && !validatorInput(e)) return false;
        }
        return true;
    }

    private boolean validatorInput(String e) {
        return true;
    }


    @Override
    public void onClick(View v) {
        HashMap<String, Object> param = null;
        Bundle bundle = null;
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_date_start:
                showDateDialog((FormEditText) v.findViewById(R.id.tv_date_start));
                break;
            case R.id.tv_date_end:
                showDateDialog((FormEditText) v.findViewById(R.id.tv_date_end));
                break;
            case R.id.tv_customer_login:

                intent = new Intent(ct, SelectAimActivity.class);
                intent.putExtra("type", 1);
                startActivityForResult(intent, 0x22);
                break;
            case R.id.tv_visit_steps:
//                Toast.makeText(this,"当前阶段",Toast.LENGTH_SHORT).show();
                String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "mobile/crm/getBusinessChanceStage.action";
                Map<String, Object> params = new HashMap<>();
                LinkedHashMap headers = new LinkedHashMap();
                headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
                ViewUtil.httpSendRequest(getApplicationContext(), url, params, mHandler, headers, CURRENT_DUR, null, null, "post");
                break;
            case R.id.tv_relate_business:
                param = new HashMap<>();
                param.put("cu_code", cu_code);
                param.put("page", 1);
                param.put("custname", tv_customer_login.getText().toString());
                param.put("pageSize", 100);
                bundle = new Bundle();
                bundle.putSerializable("param", param);
                intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("reid", R.style.OAThemeMeet);
                intent.putExtras(bundle);
                intent.putExtra("key", "businesschance");
                intent.putExtra("showKey", "name");
                intent.putExtra("action", "mobile/crm/getnichecodes.action");
                intent.putExtra("title", getString(R.string.connect_businedd));
                startActivityForResult(intent, 0x24);
                break;
            case R.id.tv_visit_type:
                intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putExtra("reid", R.style.OAThemeMeet);
                List<SelectBean> beanList = new ArrayList<>();

                //华东地区，华南地区，华北地区，华中地区，西南地区，西北地区，东北地区，港澳台地区，海外地区，其它地区
                SelectBean ben = new SelectBean();
                ben.setName(getString(R.string.crmmain_customer_visit));
                ben.setObject("OfficeClerk");
                ben.setJson("OfficeClerk");
                beanList.add(ben);

                ben = new SelectBean();
                ben.setName(getString(R.string.Original_visit));
                ben.setObject("VisitRecord!Vender");
                ben.setJson("VisitRecord!Vender");
                beanList.add(ben);
                intent.putExtra("title", getString(R.string.visitting_type));
                intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) beanList);
                startActivityForResult(intent, 0x27);
                break;
            case R.id.tv_visit_theme:
                if (getIntent().getIntExtra("type", -1) == 3 && !TextUtils.isEmpty(tv_visit_theme.getText()))
                    return;
                intent = new Intent(ct, SelectRemarkActivity.class).putExtra("title", getString(R.string.visit_aim));
                startActivityForResult(intent, 0x29);

                break;
            case R.id.voice_search_iv:
                RecognizerDialogUtil.showRecognizerDialog(ct, this);
                break;
            case R.id.save_btn:
                if (save_btn.getText().toString().trim().equals(getString(R.string.unsubmit)))
                    resSubmitVisitRecord();
                else if (save_btn.getText().toString().trim().equals(getString(R.string.common_update_button)))
                    updateVisitRecord();
                else if (save_btn.getText().toString().trim().equals(getString(R.string.common_save_button)))
                    saveVisit();
                else saveVisit();
                break;
            case R.id.delete_btn:
                PopupWindowHelper.showAlart(VisitReportAddActivity.this,
                        getString(R.string.prompt_title), getString(R.string.delete_prompt),
                        new PopupWindowHelper.OnSelectListener() {
                            @Override
                            public void select(boolean selectOk) {
                                deleteVisitRecord();
                            }
                        });
                break;
        }
    }

    private String nichecode;//商机编号
    private String cu_code;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d("onActivityResult:" + requestCode + requestCode);
        if (data == null) return;
        if (requestCode == 0x22 && resultCode == 0x20) {
            SelectAimModel model = data.getParcelableExtra("data");
            if (model == null) return;
            LogUtil.d(JSON.toJSONString(model));
            doEditShortName(model); //编辑地址简称
            if (StringUtil.isEmpty(model.getObject()) || !JSONUtil.validate(model.getObject()))
                return;
            tv_visit_steps.setText(JSON.parseObject(model.getObject()).getString("CU_NICHESTEP"));
            tv_linksman_login.setText(StringUtil.isEmpty(JSON.parseObject(model.getObject()).getString("CU_TEL")) ? "" : JSON.parseObject(model.getObject()).getString("CU_TEL"));
//            String address = model.getAddress();
//            String name = model.getName();
//            tv_customer_login.setText(name);
//            tv_address_login.setText(address);
//            SelectBean b = data.getParcelableExtra("data");
//            if (b == null || StringUtil.isEmpty(b.getJson())) return;
//            if (JSONUtil.validate(b.getJson())) {
//                JSONObject object = JSON.parseObject(b.getJson());
//                cu_code = object.containsKey("CU_CODE") ? object.getString("CU_CODE") : "";
//                tv_customer_login.setText(object.containsKey("CU_NAME") ? object.getString("CU_NAME") : " ");
//                tv_linksman_login.setText(object.containsKey("CU_CONTACT") ? object.getString("CU_CONTACT") : " ");
//                tv_address_login.setText(object.containsKey("CU_ADD1") ? object.getString("CU_ADD1") : " ");
//                tv_visit_steps.setText(object.containsKey("CU_NICHESTEP") ? object.getString("CU_NICHESTEP") : " ");
//
//            }
        } else if (requestCode == 0x23 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) {
                return;
            } else {
                String json = b.getJson();
                if (JSONUtil.validate(json)) {
                    JSONObject object = JSON.parseObject(json);
                    if (object == null) return;
                    tv_visit_steps.setText(object.getString("BS_NAME"));
                }
            }
        } else if (requestCode == 0x24 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null || StringUtil.isEmpty(b.getJson())) return;
            tv_relate_business.setText(b.getName());
            if (JSONUtil.validate(b.getJson())) {
                JSONObject object = JSON.parseObject(b.getJson());
                nichecode = object.getString("code");
            }
        } else if (requestCode == 0x27 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            LogUtil.d("b:" + JSON.toJSONString(b));
            if (b == null || StringUtil.isEmpty(b.getJson())) return;
            tv_visit_type.setText(b.getName());
            tv_visit_type.setHint(b.getJson());
        } else if (requestCode == 0x28 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            LogUtil.d("b:" + JSON.toJSONString(b));
            if (b == null || StringUtil.isEmpty(b.getJson())) return;
            tv_visit_theme.setText(b.getName());
            tv_visit_theme.setHint(b.getJson());
            if ("vr_nichestep".equals(b.getObject())) {
                tv_visit_steps.setText(b.getName());
            } else {
                tv_visit_steps.setText("");
            }
        } else if (requestCode == 0x29) {
            String message = data.getStringExtra("data");
            tv_visit_theme.setText(StringUtil.isEmpty(message) ? getString(R.string.maintain_customers) : message);
        } else if (requestCode == 0x828 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b != null && !StringUtil.isEmpty(b.getName())) {
                tv_visit_steps.setText(b.getName());
            }
        }
    }

    private PopupWindow popupWindow = null;

    private void doEditShortName(final SelectAimModel model) {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        popupWindow = PopupWindowHelper.create(this, getString(com.modular.booking.R.string.perfect_company_name), model, new PopupWindowHelper.OnClickListener() {
            @Override
            public void result(SelectAimModel model) {
                tv_customer_login.setText(model.getName() + "");
                tv_address_login.setText(model.getAddress() + "");
            }
        }, null);

    }



    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    private void showDateDialog(final FormEditText tv) {
        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                tv.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
//                switch (tv.getId()) {
//                    case R.id.tv_date_start:
//                        tv_date_start.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
//                        break;
//                    case R.id.tv_date_end:
//                        tv_date_end.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
//                        break;
//                }
            }
        });
        picker.show();

    }

    private void saveB2bByHttp() {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveVisitRecord;
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> formStore = new HashMap<>();
        formStore.put("vr_class", tv_visit_type.getText().toString());//拜访类别
        formStore.put("vr_startdate", tv_date_start.getText().toString());//开始时间
        formStore.put("vr_enddate", tv_date_end.getText().toString());//结束时间
        formStore.put("emuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));//录入人uu
        formStore.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));//企业uu
        formStore.put("vr_title", tv_visit_theme.getText().toString());//主题
        formStore.put("vr_detail", StringUtil.toHttpString(tv_visit_content.getText().toString()));//内容
        formStore.put("vr_cucode", cu_code);//客户编号
        formStore.put("vr_cuname", StringUtil.toHttpString(tv_customer_login.getText().toString()));//客户名字
        formStore.put("vr_cuaddress", tv_address_login.getText().toString());//客户地址
        formStore.put("vr_cucontact", StringUtil.toHttpString(tv_linksman_login.getText().toString()));//客户联系人
        params.put("formStore", JSONUtil.map2JSON(formStore));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }

    //更新外勤计划
    private void updataCode() {
        if (progressDialog != null)
            progressDialog.show();
        if (StringUtil.isEmpty(vp_code))
            vp_code = getIntent().getStringExtra("code");
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") + "mobile/crm/updateVistPlan.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("vr_id", mVrId);
        param.put("vp_id", vp_id);
        //cu_nichestep商机阶段
        //cu_code客户编号
        param.put("cu_nichestep", tv_visit_steps.getText().toString());
        param.put("cu_code", vp_code);
//        param.put("bc_nichecode", tv_relate_business.getText().toString());
        param.put("bc_nichecode", StringUtil.isEmpty(nichecode) ? "" : nichecode);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, mHandler, headers, 0x16, null, null, "post");
    }

    private void resSubmitVisitRecord() {
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") + "crm/customermgr/resSubmitVisitRecord.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "VisitRecord");
        param.put("id", mVrId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, mHandler, headers, RES_SUBMIT_VISITRECORD, null, null, "post");
    }

    private void deleteVisitRecord() {
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") +
                "crm/customermgr/deleteVisitRecord.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "VisitRecord");
        param.put("id", mVrId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, mHandler, headers, DELETE_VISITRECORD, null, null, "post");
    }

    //保存
    private void sendHttpResquest(int what, String code) {
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        String formStore = "";
        if (tv_customer_login.testValidity() && tv_linksman_login.testValidity()
                && tv_address_login.testValidity() && tv_visit_theme.testValidity()
                && tv_visit_content.testValidity()) {
            formStore = getFormStore(emname, code);
        } else {
            return;
        }
        if (progressDialog != null)
            progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "crm/customermgr/saveVisitRecord.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        params.put("caller", "VisitRecord");
        String param1 = "{\"pl_detno\":1,\"pl_name\":" + emname + ",\"pl_vrid\":" + mVrId + "}";
        params.put("param1", "[]");
        params.put("param2", "[]");
        params.put("param3", param1);
        params.put("param4", "[]");
        params.put("param5", "[]");
        params.put("param6", "[]");
        params.put("param7", "[]");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void updateVisitRecord() {
        if (!canSubmit()) return;
        progressDialog.show();
        String emname = CommonUtil.getName();
        String formStore = getFormStore(emname, vr_code);
        if (progressDialog != null)
            progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "crm/customermgr/updateVisitRecord.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        params.put("caller", "VisitRecord");
        String param1 = "{\"pl_detno\":1,\"pl_name\":" + emname + ",\"pl_vrid\":" + mVrId + "}";
        params.put("param1", "[]");
        params.put("param2", "[]");
        params.put("param3", param1);
        params.put("param4", "[]");
        params.put("param5", "[]");
        params.put("param6", "[]");
        params.put("param7", "[]");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, UPDATE_VISITRECORD, null, null, "post");
    }

    //提交审批流
    private void submit() {
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") +
                "crm/customermgr/submitVisitRecord.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "VisitRecord");
        param.put("id", mVrId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, mHandler, headers, 0x17, null, null, "post");
    }


    private String getFormStore(String emname, String vr_code) {
        return
                "{\n" +
                        "\"vr_id\":" + mVrId + ",\n" +
                        "\"vr_class\":\"" + tv_visit_type.getHint().toString() + "\",\n" +
                        "\"vr_code\":\"" + vr_code
                        + "\",\n" +
                        "\"vr_tel\":\"" +
                        CommonUtil.getNoMarkEditText(phone_tv)
                        + "\",\n" +
                        "\"vr_visittime\":\""
                        + tv_date_start.getText().toString() + //开始时间
                        "\",\n" +
                        "\"vr_visitplace\":\"" +
                        CommonUtil.getNoMarkEditText(tv_address_login) +
                        "\",\n" +
                        "\"vr_cuname\":\"" +
                        CommonUtil.getNoMarkEditText(tv_customer_login) +
                        "\",\n" +
                        "\"vr_cucontact\":\"" +
                        CommonUtil.getNoMarkEditText(tv_linksman_login) +
                        "\",\n" +
                        "\"vr_contact\":\"" +
                        CommonUtil.getNoMarkEditText(tv_linksman_login) +
                        "\",\n" +
                        "\"vr_title\":\"" +
                        CommonUtil.getNoMarkEditText(tv_visit_theme) +//主题
                        "\",\n" +
                        "\"vr_nichestep\":\"" +
                        CommonUtil.getNoMarkEditText(tv_visit_steps) +
                        "\",\n" +
                        "\"vr_detail\":\"" +
                        CommonUtil.getNoMarkEditText(tv_visit_content) +
                        "\",\n" +
                        "\"vr_nichecode\":\"" +
                        nichecode +
                        "\",\n" +
                        "\"vr_nichename\":\"" +
                        (TextUtils.isEmpty(tv_relate_business.getText()) ? "" : tv_relate_business.getText().toString()) +
                        "\",\n" +
                        "\"vr_recorddate\":\"" +
                        DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd") +
                        "\",\n" +
                        "\"vr_statuscode\":\"" +
                        "ENTERING" +
                        "\",\n" +
                        "\"vr_recorder\":\"" +
                        emname +
                        "\",\n" +
                        "\"vr_status\":\"" +
                        "在录入" +
                        "\",\n" +
                        "\"vr_cuuu\":\"" +
                        cu_code +
                        "\",\n" +
                        "\"vr_visitend\":\"" +
                        tv_date_end.getText().toString() +//结束时间
                        "\"\n" +
                        "}";
    }


    private JSONArray array;
    private SingleDialog singleDialog;
    private String vp_code;
    private List<String> lists = new ArrayList<String>();

    public void showSimpleDialog(final FormEditText et, String title) {
        if (singleDialog != null) {
            if (singleDialog.isShowing())
                return;
        }
        singleDialog = new SingleDialog(ct, title, new SingleDialog.PickDialogListener() {
            @Override
            public void onListItemClick(int position, String value) {
                et.setText(value);
                for (int i = 0; i < array.size(); i++) {
                    if (value.equals(array.getJSONObject(i).getString("CU_NAME"))) {
                        if (StringUtil.isEmpty(vp_code))
                            vp_code = array.getJSONObject(i).getString("CU_CODE");
                        tv_linksman_login.setText(array.getJSONObject(i).getString("CU_CONTACT"));
                        tv_address_login.setText(array.getJSONObject(i).getString("CU_ADD1"));
                        tv_visit_steps.setText(array.getJSONObject(i).getString("CU_NICHESTEP"));
                    }
                }
            }
        });
        singleDialog.show();
        singleDialog.initViewData(lists);
       /* } else {
            singleDialog.show();
            singleDialog.initViewData(lists);
        }*/
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        tv_visit_content.setText(tv_visit_content.getText().toString() + text);
    }

    @Override
    public void onError(SpeechError speechError) {

    }
}
