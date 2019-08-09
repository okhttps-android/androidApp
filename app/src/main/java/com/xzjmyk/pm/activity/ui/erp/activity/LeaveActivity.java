package com.xzjmyk.pm.activity.ui.erp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.SingleDialog;
import com.core.widget.view.Activity.SelectActivity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.DbfindListActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.LeaveAddEntity;
import com.xzjmyk.pm.activity.ui.erp.model.LeaveEntity;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.ui.platform.pageforms.FormDetailActivity;
import com.xzjmyk.pm.activity.ui.platform.pageforms.LeavePageActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//implements OnClickListener
public class LeaveActivity extends BaseActivity implements OnClickListener, RecognizerDialogListener {

    private final static int SUCCESS_SAVE = 1;
    private final static int SUCCESS_PRE = 0;
    private final static int SUCCESS_PRECODE = 6;
    private final static int SUCCESS_COMMIT = 2;
    private final static int SUCCESS_UNCOMMIT = 3;
    private final static int SUCCESS_UPDATE = 4;
    private final static int SUCCESS_DELETE = 5;
    private final static int SUCCESS_LEAVETYPE = 9;
    private final static int SUCCESS_VACATION = 10;
    private final static int SUCCESS_MANKIND = 11;

    @ViewInject(R.id.ry_set_startTime)
    private RelativeLayout startDate;
    @ViewInject(R.id.ry_set_endTime)
    private RelativeLayout endDate;
    @ViewInject(R.id.ry_leave_type)
    private RelativeLayout ry_leave_type;
    @ViewInject(R.id.ry_leave_category)
    private RelativeLayout ry_leave_category;
    @ViewInject(R.id.et_leave_man)
    private FormEditText et_leave_man; // 请假人
    @ViewInject(R.id.et_leave_type)
    private FormEditText et_leave_type; // 类型
    @ViewInject(R.id.et_leave_mankind)
    private FormEditText et_leave_mankind;
    @ViewInject(R.id.ry_leave_mankind)
    private RelativeLayout ry_leave_mankind;
    @ViewInject(R.id.voice_search_iv)
    private ImageView voice_search_iv;


    /**
     * @注释：天数
     */
    @ViewInject(R.id.et_leave_days)
    private FormEditText et_leave_days; // 天数
    @ViewInject(R.id.et_leave_hours)
    private FormEditText et_leave_hours; // 时数
    @ViewInject(R.id.et_leave_category)
    private FormEditText et_leave_category; // 假期类型
    @ViewInject(R.id.et_leave_reason)
    private FormEditText et_leave_reason;
    private String en_code;
    @ViewInject(R.id.tv_start_time)
    private FormEditText tv_start_time;
    @ViewInject(R.id.tv_end_time)
    private FormEditText tv_end_time;
    @ViewInject(R.id.bt_save)
    private Button bt_save;
    @ViewInject(R.id.bt_add)
    private RadioButton bt_add;
    @ViewInject(R.id.bt_commit)
    private RadioButton bt_commit;
    @ViewInject(R.id.bt_uncommit)
    private RadioButton bt_uncommit;
    @ViewInject(R.id.bt_update)
    private RadioButton bt_update;
    @ViewInject(R.id.ly_bottom_save)
    private LinearLayout ly_bottom_save;
    @ViewInject(R.id.ly_bottom_handler)
    private LinearLayout ly_bottom_handler;
    private DateTimePickerDialog dialog;
    private SingleDialog singleDialog;
    private SingleDialog typeDialog;
    private int va_id;
    private String va_code;
    private String jsondata;
    private Context ct;

    private String et_emcode;
    private String et_code;
    private String et_recoder;

    private String[] mLeaveTypes = {"病假", "产假", "事假", "婚假", "丧假", "年假", "产检假", "陪产假", "产前假", "调休", "哺乳假"};

    private CustomProgressDialog progressDialog;
    private int mkeyValue = -1;
    private List<String> lists = new ArrayList<String>();
    public Handler handler = new Handler() {
        @SuppressWarnings({"unchecked"})
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_SAVE:
                    progressDialog.dismiss();

                    String result = msg.getData().getString("result");
                    mkeyValue = JSON.parseObject(result).getIntValue("va_id");
                    if (mkeyValue != -1){
                        jumpTODetails(mkeyValue);
                    }
                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg.getData().getString("result"));

                        if ((Boolean) keMap.get("success")) {
                            ToastMessage("保存成功！");
                            bt_update.setEnabled(true);
                            bt_update.setChecked(false);
                            bt_update.setTextColor(getResources()
                                    .getColor(R.color.black));
                            bt_commit.setEnabled(true);
                            bt_commit.setChecked(false);
                            bt_commit.setTextColor(getResources()
                                    .getColor(R.color.black));
                            bt_uncommit.setEnabled(false);
                            bt_uncommit.setClickable(true);
                            bt_uncommit.setTextColor(getResources()
                                    .getColor(R.color.gray));
                            ly_bottom_handler.setVisibility(View.VISIBLE);
                            ly_bottom_save.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        messageDisplay(msg);
                    }

                    System.out.println("保存 result:" + result);

                    break;

                case SUCCESS_PRE:
                    System.out.println("获取id result:" +
                            msg.getData().getString("result"));
                    va_id = Integer.valueOf(FlexJsonUtil.fromJson(
                            msg.getData().getString("result")).get("id")
                            .toString());
                    getCodeHttpData();

                    break;

                case SUCCESS_COMMIT:
                    progressDialog.dismiss();

                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg.getData()
                                .getString("result"));

                        if ((Boolean) keMap.get("success")) {
                            ToastMessage("提交成功！");
                            bt_uncommit.setTextColor(getResources()
                                    .getColor(R.color.black));
                            bt_uncommit.setEnabled(true);
                            bt_uncommit.setClickable(false);
                            bt_commit.setTextColor(getResources()
                                    .getColor(R.color.gray));
                            bt_commit.setEnabled(false);
                            bt_commit.setChecked(true);
                            bt_update.setTextColor(getResources()
                                    .getColor(R.color.gray));
                            bt_update.setEnabled(false);
                            bt_update.setChecked(true);
                            editnoclik();
                        }
                    } catch (Exception e) {
                        messageDisplayCommit(msg);
                        editnoclik();
                    }

                    System.out.println("提交 result:" +
                            msg.getData().getString("result"));

                    break;

                case SUCCESS_DELETE:
                    System.out.println("删除  result:" +
                            msg.getData().getString("result"));

                    break;

                case SUCCESS_UNCOMMIT:
                    progressDialog.dismiss();

                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg.getData()
                                .getString("result"));

                        if ((Boolean) keMap.get("success")) {
                            ToastMessage("反提交成功！");
                            bt_commit.setTextColor(getResources()
                                    .getColor(R.color.black));
                            bt_commit.setEnabled(true);
                            bt_commit.setChecked(false);
                            bt_update.setTextColor(getResources()
                                    .getColor(R.color.black));
                            bt_update.setEnabled(true);
                            bt_update.setChecked(false);
                            bt_uncommit.setTextColor(getResources()
                                    .getColor(R.color.gray));
                            bt_uncommit.setEnabled(false);
                            bt_uncommit.setClickable(true);
                            editclik();
                        }
                    } catch (Exception e) {
                        messageDisplay(msg);
                    }

                    System.out.println("反提交 result:" +
                            msg.getData().getString("result"));

                    break;

                case SUCCESS_UPDATE:
                    progressDialog.dismiss();

                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg.getData()
                                .getString("result"));

                        if ((Boolean) keMap.get("success")) {
                            ToastMessage("更新成功！");
                        }
                    } catch (Exception e) {
                        messageDisplay(msg);
                    }

                    System.out.println("更新 result:" +
                            msg.getData().getString("result"));

                    break;

                case SUCCESS_PRECODE:
                    va_code = FlexJsonUtil.fromJson(msg.getData()
                            .getString("result"))
                            .get("code").toString();
                    //保存操作
                    httpSave();
                    System.out.println("va_code=" + va_code);

                    break;

                case Constants.SUCCESS_INITDATA:
                    progressDialog.dismiss();
                    try {
                        //	初始化数据
                        String jsondata = msg.getData().getString("result");
                        Map<String, Object> map = FlexJsonUtil.fromJson(jsondata);
                        Log.i("jsondata",
                                "init paneldata  json=" + "[" +
                                        FlexJsonUtil.toJson(map.get("panelData")) + "]");

                        List<LeaveEntity> leaveEntities = FlexJsonUtil.fromJsonArray(
                                "[" + FlexJsonUtil.toJson(map.get("panelData")) +
                                        "]", LeaveEntity.class);
                        initDataFromServer(leaveEntities);
                    } catch (Exception e) {
                        ViewUtil.ShowMessageTitle(ct, "数据解析异常");
                    }
                    break;
                case SUCCESS_LEAVETYPE:
                    progressDialog.dismiss();
                    lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                            "combdatas");
                    if (lists.isEmpty()) {
                        lists.add("无");
                    }
                    View view = findViewById(R.id.et_leave_type);
                    showSimpleDialog(view);
                    break;
                case SUCCESS_VACATION:
                    progressDialog.dismiss();
                    lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                            "combdatas");
                    if (lists.isEmpty()) {
                        lists.add("无");
                    }
                    view = findViewById(R.id.et_leave_category);
                    showTypeDialog(view);
                    break;
                case SUCCESS_MANKIND:
                    progressDialog.dismiss();
                    lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                            "combdatas");
                    if (lists.isEmpty()) {
                        lists.add("无");
                    }
                    showListDialog("人员类型", et_leave_mankind);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    String exception = msg.getData().getString("result");
                    ViewUtil.ToastMessage(mContext, exception);
                    progressDialog.dismiss();
                    break;
                default:
                    break;
            }
        }

        private void jumpTODetails(int mkeyValue) {
            JSONObject map = new JSONObject(true);
            map.put("请假类型", et_leave_category.getText().toString());
            map.put("单据状态", "已提交");
            map.put("开始时间", tv_start_time.getText().toString());
            map.put("结束时间", tv_end_time.getText().toString());
            map.put("请假原因", et_leave_reason.getText().toString());
            Log.d("mkeyValue", mkeyValue + "");
            LogUtil.d(map.toJSONString());
            startActivity(new Intent(LeaveActivity.this, FormDetailActivity.class)
                    .putExtra("data", map.toString())
                    .putExtra("title", "请假单详情")
                    .putExtra("mkeyValue", mkeyValue)
                    .putExtra("whichpage", 1));
        }
        /**
         * @param leaveEntities
         */
        private void initDataFromServer(List<LeaveEntity> leaveEntities) {
            LeaveEntity leaveEntitie = leaveEntities.get(0);
            et_leave_man.setText(leaveEntitie.getVa_emname());
            et_leave_days.setText(String.valueOf(leaveEntitie.getVa_alldays()));
            et_leave_category.setText(leaveEntitie.getVa_vacationtype());
            et_leave_hours.setText(String.valueOf(leaveEntitie.getVa_alltimes()));
            et_leave_reason.setText(leaveEntitie.getVa_remark());
            et_leave_type.setText(leaveEntitie.getVa_holidaytype());
            et_leave_mankind.setText(leaveEntitie.getVa_mankind());
            tv_start_time.setText(leaveEntitie.getVa_startime().substring(0, leaveEntitie.getVa_startime().length() - 3));
            tv_end_time.setText(leaveEntitie.getVa_endtime().substring(0, leaveEntitie.getVa_endtime().length() - 3));
            String status = leaveEntities.get(0).getVa_status();

            et_code = leaveEntitie.getVa_code();
            et_recoder = leaveEntitie.getVa_recordor();
            et_emcode = leaveEntitie.getVa_emcode();
            System.out.println("et_code:" + et_code);
            System.out.println("et_recoder:" + et_recoder);
            System.out.println("et_emcode:" + et_emcode);
            System.out.println("初始化数据：" + JSON.toJSONString(leaveEntitie));
            if (!StringUtil.isEmpty(status)) {
                if ("已提交".equals(status)) {
                    bt_commit.setTextColor(getResources()
                            .getColor(R.color.gray));
                    bt_commit.setEnabled(false);
                    bt_commit.setChecked(true);
                    bt_update.setTextColor(getResources()
                            .getColor(R.color.gray));
                    bt_update.setEnabled(false);
                    bt_update.setChecked(true);
                    editnoclik();
                }

                if ("在录入".equals(status)) {
                    bt_uncommit.setTextColor(getResources()
                            .getColor(R.color.gray));
                    bt_uncommit.setEnabled(false);
                    bt_uncommit.setClickable(true);
                    editclik();
                }
            }

            va_id = leaveEntitie.getVa_id();
            va_code = leaveEntitie.getVa_code();

            ly_bottom_save.setVisibility(View.GONE);
            ly_bottom_handler.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ry_set_startTime:
//                showDialog(v);
                showDateDialog(this, tv_start_time);

                break;

            case R.id.ry_set_endTime:
//                showDialog(v);
                showDateDialog(this, tv_end_time);

                break;
            case R.id.ry_leave_mankind:
                progressDialog.show();
                loadDataForServer("va_mankind", SUCCESS_MANKIND);
                break;
            case R.id.et_leave_mankind:
                progressDialog.show();
                loadDataForServer("va_mankind", SUCCESS_MANKIND);
                break;
            case R.id.ry_leave_type:
                progressDialog.show();
                loadDataForServer("va_holidaytype", SUCCESS_LEAVETYPE);

                break;

            case R.id.ry_leave_category:
//                progressDialog.show();
//                loadDataForServer("va_vacationtype", SUCCESS_VACATION);
                selectLeaveType();
                break;

            case R.id.bt_save:
                if (et_leave_reason.testValidity() &&
                        tv_start_time.testValidity() &&
                        tv_end_time.testValidity() &&
                        et_leave_category.testValidity()
//                        && et_leave_mankind.testValidity()
                        ) {
                    boolean falg = ViewUtil.isCheckDateTime(tv_start_time.getText()
                                    .toString(),
                            tv_end_time.getText().toString(), "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage("结束时间小于开始时间！");
                    } else {
                        progressDialog.show();
                        httpSave();
                    }
                }
                break;
            case R.id.bt_commit:
                progressDialog.show();
                httpCommit();
                break;
            case R.id.bt_add:
                // 清空编辑框数据
                // 隐藏button
                // 显示保存butto
                et_leave_man.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));
                et_leave_category.setText("");
                et_leave_hours.setText("");
                et_leave_days.setText("");
                et_leave_reason.setText("");
                et_leave_type.setText("");
                tv_end_time.setText("");
                tv_start_time.setText("");
                et_leave_type.setText("");
                et_leave_mankind.setText("");
                editclik();
                ly_bottom_save.setVisibility(View.VISIBLE);
                ly_bottom_handler.setVisibility(View.GONE);

                break;
            case R.id.bt_uncommit:
                progressDialog.show();
                UnhttpCommit();
                break;
            case R.id.bt_update:
                if (et_leave_man.testValidity() &&
//                        et_leave_days.testValidity() &&
//                        et_leave_hours.testValidity() &&
                        et_leave_reason.testValidity() &&
                        tv_start_time.testValidity() && tv_end_time.testValidity() &&
                        et_leave_category.testValidity()
                        && et_leave_mankind.testValidity()) {
                    boolean falg = ViewUtil.isCheckDateTime(tv_start_time.getText()
                                    .toString(), tv_end_time.getText().toString(),
                            "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage("结束时间小于开始时间！");
                    } else {
                        progressDialog.show();
                        httpUpdate();
                    }
                }
                break;

            case R.id.et_leave_category:
//                progressDialog.show();
//                loadDataForServer("va_vacationtype", SUCCESS_VACATION);
                selectLeaveType();
                break;
            case R.id.et_leave_type:
                progressDialog.show();
                loadDataForServer("va_holidaytype", SUCCESS_LEAVETYPE);
                break;
            case R.id.tv_start_time:
//                showDialog(v);
                showDateDialog(this, tv_start_time);
                break;
            case R.id.tv_end_time:
//                showDialog(v);
                showDateDialog(this, tv_end_time);
                break;

            case R.id.et_leave_man:
                Intent et_dbfind = new Intent(ct, DbfindListActivity.class);
                startActivityForResult(et_dbfind, 1);
                break;
            case R.id.voice_search_iv:
                RecognizerDialogUtil.showRecognizerDialog(ct,this);
                break;
            default:
                break;
        }
    }

    private void selectLeaveType() {
        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < mLeaveTypes.length; i++) {
            selectBean = new SelectBean();
            selectBean.setName(mLeaveTypes[i]);
            formBeaan.add(selectBean);
        }

        Intent intent = new Intent();
        intent.setClass(this, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("title", "请假类型");
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, 0x11);
    }

    /**
     * 系统提示信息
     *
     * @param msg
     */
    private void messageDisplay(Message msg) {
        String message = FlexJsonUtil.fromJson(msg.getData().getString("result"))
                .get("exceptionInfo").toString();
        ViewUtil.ShowMessageTitle(ct, message);
        bt_commit.setTextColor(getResources().getColor(R.color.grey));
        bt_uncommit.setTextColor(getResources().getColor(R.color.black));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        setContentView(R.layout.from_leave_view);
        ViewUtils.inject(this);
        ct = this;
        progressDialog = CustomProgressDialog.createDialog(this);

        tv_start_time.setKeyListener(null);
        tv_end_time.setKeyListener(null);
        et_leave_type.setKeyListener(null);
        et_leave_category.setKeyListener(null);
        et_leave_man.setKeyListener(null);
        et_leave_mankind.setKeyListener(null);

        tv_start_time.setFocusable(false);
        tv_end_time.setFocusable(false);
        et_leave_type.setFocusable(false);
        et_leave_category.setFocusable(false);
        et_leave_man.setFocusable(false);
        et_leave_mankind.setFocusable(false);

        et_leave_man.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));


        setTitle("请假单");
        et_leave_man.setOnClickListener(this);
        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        ry_leave_type.setOnClickListener(this);
        ry_leave_category.setOnClickListener(this);
        ry_leave_mankind.setOnClickListener(this);

        bt_save.setOnClickListener(this);
        bt_add.setOnClickListener(this);
        bt_commit.setOnClickListener(this);
        bt_uncommit.setOnClickListener(this);
        bt_update.setOnClickListener(this);

        et_leave_type.setOnClickListener(this);
        et_leave_category.setOnClickListener(this);
        tv_end_time.setOnClickListener(this);
        tv_start_time.setOnClickListener(this);
        et_leave_mankind.setOnClickListener(this);
        voice_search_iv.setOnClickListener(this);

    }

    public void initData() {
        Intent intent = getIntent();
        String formCondition = intent.getStringExtra("formCondition");
        String gridCondition = intent.getStringExtra("gridCondition");
        String caller = intent.getStringExtra("caller");

        if (!StringUtil.isEmpty(caller)) {
            //需要初始化编辑界面
            String url = CommonUtil.getAppBaseUrl(this) +
                    "mobile/common/getPanel.action";
            String sessionId = CommonUtil.getSharedPreferences(this, "sessionId");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("caller", caller);
            params.put("formCondition", formCondition);
            params.put("gridCondition", gridCondition);
            params.put("sessionId", sessionId);
            progressDialog.show();
            Log.i("jsondata", "url=" + url + "\ncaller=" + caller + "\nformCondition=" + formCondition + "\ngridCondition=" + gridCondition + "\nsessionId=" + sessionId);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, handler, headers, Constants.SUCCESS_INITDATA, null, null, "get");
        } else {
            //		   getPreHttpData();
            //		   getCodeHttpData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signin_set, menu);
        menu.findItem(R.id.oa_signin_set).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.oa_leave:
                Intent intent = new Intent();
                intent.setClass(this, LeavePageActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @注释：获取主键ID
     */
    public void getPreHttpData() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getId.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("seq", "SpeAttendance_SEQ");
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRE, null, null, "post");
    }

    public void getCodeHttpData() {
        String url = CommonUtil.getAppBaseUrl(ct) +
                "common/getCodeString.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", "2");
        params.put("caller", "Ask4Leave");
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRECODE, null, null, "get");
    }

    /**
     * @注释：保存
     */
    public void httpSave() {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().leave_save_url;
        Map<String, Object> params = new HashMap<String, Object>();
        LeaveAddEntity entity = getSaveJsonData();
        jsondata = FlexJsonUtil.toJson(entity);
        System.out.println("url:" + url);
        System.out.println("formStore=" + jsondata);
        params.put("formStore", jsondata);
        params.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_SAVE, null, null, "post");
    }

    private LeaveAddEntity getSaveJsonData() {
        LeaveAddEntity entity = new LeaveAddEntity();

        entity.setVa_vacationtype(et_leave_category.getText().toString());
        entity.setVa_remark(et_leave_reason.getText().toString());
        entity.setVa_startime(tv_start_time.getText().toString());
        entity.setVa_endtime(tv_end_time.getText().toString());
        entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));

        return entity;
    }


    /**
     * @注释：提交
     */
    public void httpCommit() {
        String url = CommonUtil.getAppBaseUrl(ct) + "oa/check/submitVacation.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "Ask4Leave");
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_COMMIT, null, null, "get");
    }

    /**
     * @注释：反提交
     */
    public void UnhttpCommit() {
        String url = CommonUtil.getAppBaseUrl(ct) +
                "oa/check/resSubmitVacation.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "Ask4Leave");
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UNCOMMIT, null, null, "get");

    }

    /**
     * @注释：删除
     */
    public void httpDelete() {
        String url = CommonUtil.getAppBaseUrl(ct) +
                "oa/check/deleteVacation.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "Ask4Leave");
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_DELETE, null, null, "get");
    }

    /**
     * @注释：更新
     */
    public void httpUpdate() {
        String url = CommonUtil.getAppBaseUrl(ct) +
                "oa/check/updateVacation.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "Ask4Leave");
        LeaveAddEntity entity = getSaveJsonData();
        jsondata = FlexJsonUtil.toJson(entity);
        System.out.println("更新：" + jsondata);
        params.put("formStore", jsondata);
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UPDATE, null, null, "get");
    }

    public void showDialog(final View view) {
        if (dialog == null) {
            dialog = new DateTimePickerDialog(this, System.currentTimeMillis());
        }

        dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dia, long date) {
                if ((view.getId() == R.id.ry_set_startTime) ||
                        (view.getId() == R.id.tv_start_time)) {
                    tv_start_time.setText(CommonUtil.getStringDateMM(date));
                }

                if ((view.getId() == R.id.ry_set_endTime) ||
                        (view.getId() == R.id.tv_end_time)) {
                    tv_end_time.setText(CommonUtil.getStringDateMM(date));
                }

                /** @注释：保证 初始化当前时间 */
                dialog = null;
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void showSimpleDialog(View view) {
        if (singleDialog == null) {
            singleDialog = new SingleDialog(ct, "请假类型",
                    new SingleDialog.PickDialogListener() {
                        @Override
                        public void onListItemClick(int position, String value) {
                            et_leave_type.setText(value);
                        }
                    });
            singleDialog.show();
            singleDialog.initViewData(lists);
        } else {
            singleDialog.show();
            singleDialog.initViewData(lists);
        }
    }

    public void showTypeDialog(View view) {
        if (typeDialog == null) {
            typeDialog = new SingleDialog(ct, "假期类型",
                    new SingleDialog.PickDialogListener() {
                        @Override
                        public void onListItemClick(int position, String value) {
                            et_leave_category.setText(value);
                        }
                    });
            typeDialog.show();
            typeDialog.initViewData(lists);
        } else {
            typeDialog.show();
            typeDialog.initViewData(lists);
        }
    }


    private SingleDialog listDialog;

    public void showListDialog(String title, final FormEditText fEditText) {
        if (listDialog == null) {
            listDialog = new SingleDialog(ct, title,
                    new SingleDialog.PickDialogListener() {
                        @Override
                        public void onListItemClick(int position, String value) {
                            fEditText.setText(value);
                        }
                    });
            listDialog.show();
            listDialog.initViewData(lists);
        } else {
            listDialog.show();
            listDialog.initViewData(lists);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                String en_name = data.getStringExtra("en_name");
                et_leave_man.setText(en_name);
                en_code = data.getStringExtra("en_code");
                break;
            case 0x11:
                if (resultCode == 0x20) {
                    SelectBean b = data.getParcelableExtra("data");
                    if (b != null) {
                        et_leave_category.setText(b.getName());
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 提交操作，异常也算成功，故写此方法
     *
     * @param msg
     */
    private void messageDisplayCommit(Message msg) {
        String message = FlexJsonUtil.fromJson(msg.getData().getString("result"))
                .get("exceptionInfo").toString();
        ViewUtil.ShowMessageTitle(ct, message);
        bt_commit.setTextColor(getResources().getColor(R.color.grey));
        bt_commit.setEnabled(false);
        bt_commit.setChecked(true);
        bt_uncommit.setTextColor(getResources().getColor(R.color.black));
        bt_uncommit.setEnabled(true);
        bt_uncommit.setChecked(false);
        bt_update.setTextColor(getResources().getColor(R.color.grey));
        bt_update.setEnabled(false);
        bt_update.setChecked(true);
    }

    public void editnoclik() {
        et_leave_man.setEnabled(false);
        et_leave_category.setEnabled(false);
        et_leave_hours.setEnabled(false);
        et_leave_days.setEnabled(false);
        et_leave_reason.setEnabled(false);
        et_leave_type.setEnabled(false);
        et_leave_mankind.setEnabled(false);
        tv_end_time.setEnabled(false);
        tv_start_time.setEnabled(false);
    }

    public void editclik() {
        et_leave_man.setEnabled(true);
        et_leave_category.setEnabled(true);
        et_leave_hours.setEnabled(true);
        et_leave_days.setEnabled(true);
        et_leave_reason.setEnabled(true);
        et_leave_type.setEnabled(true);
        et_leave_mankind.setEnabled(true);
        tv_end_time.setEnabled(true);
        tv_start_time.setEnabled(true);
    }

    public void loadDataForServer(String field, int what) {
        Log.i("leave", "what=" + what);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getCombo.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("caller", "Ask4Leave");
        param.put("field", field);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.getDataFormServer(ct, handler, url, param, what);
    }

    private void showDateDialog(Context ct, final EditText tv) {
        DateTimePicker picker = new DateTimePicker((Activity) ct, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        //赋值当前系统时间
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));

        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                tv.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
            }
        });
        picker.show();
    }


    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        et_leave_reason.setText(et_leave_reason.getText().toString() + CommonUtil.getPlaintext(text));
    }

    @Override
    public void onError(SpeechError speechError) {

    }
}
