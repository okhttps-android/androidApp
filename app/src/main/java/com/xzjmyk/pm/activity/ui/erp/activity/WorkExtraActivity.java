package com.xzjmyk.pm.activity.ui.erp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.core.utils.RecognizerDialogUtil;
import com.core.utils.TimeUtils;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.SingleDialog;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.DbfindListActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.ui.erp.model.ExtraAddWork;
import com.xzjmyk.pm.activity.ui.erp.model.ExtraAddWorkItems;
import com.xzjmyk.pm.activity.ui.erp.model.ExtraUpdateWork;
import com.xzjmyk.pm.activity.ui.erp.model.ExtraWork;
import com.xzjmyk.pm.activity.ui.erp.model.ExtraWorkItems;
import com.xzjmyk.pm.activity.ui.erp.model.UpdateExtraAddWorkItems;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.ui.platform.pageforms.FormDetailActivity;
import com.xzjmyk.pm.activity.ui.platform.pageforms.WorkPageActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :LiuJie 2015年7月16日 上午11:04:30
 * @注释:加班申请
 */
public class WorkExtraActivity extends BaseActivity implements OnClickListener, RecognizerDialogListener {

    private static final int UPDATE_SAVE = 0x322;
    @ViewInject(R.id.et_extra_no)
    private FormEditText et_extra_no;// 编号
    @ViewInject(R.id.et_extra_company)
    private FormEditText et_extra_company;// 申请人
    @ViewInject(R.id.et_extra_type)
    private FormEditText et_extra_type;// 加班类型

    @ViewInject(R.id.et_extra_remark)
    private FormEditText et_extra_remark;// 加班原因

    @ViewInject(R.id.et_extra_isallday)
    private FormEditText et_extra_isallday;// 是否全天

    @ViewInject(R.id.et_extra_starttime)
    private FormEditText et_extra_starttime;// 起始时间

    @ViewInject(R.id.et_extra_endtime)
    private FormEditText et_extra_endtime;// 截止时间

    @ViewInject(R.id.et_extra_count)
    private TextView et_extra_count;// 加班时数


    @ViewInject(R.id.et_leave_mankind)
    private FormEditText et_extra_mankind;// 人员类型

    @ViewInject(R.id.ry_leave_mankind)
    private RelativeLayout ry_leave_mankind;


    // button
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


    @ViewInject(R.id.ly_bottom_handler)
    private LinearLayout ly_bottom_handler;

    private int va_id;
    private String va_code;
    private String jsondata;
    private List<String> lists = new ArrayList<String>();

    private final static int SUCCESS_SAVE = 1;
    private final static int SUCCESS_PRE = 0;
    private final static int SUCCESS_PRECODE = 6;
    private final static int SUCCESS_COMMIT = 2;
    private final static int SUCCESS_UNCOMMIT = 3;
    private final static int SUCCESS_UPDATE = 4;
    private final static int SUCCESS_DELETE = 5;

    private final static int SUCCESS_TYPE = 9;
    private final static int SUCCESS_ISALLDAY = 10;

    private final static int SUCCESS_MANKIND = 11;

    private Context ct;

    private CustomProgressDialog progressDialog;
    private int  mkeyValue = -1;
    public Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_SAVE:
                    progressDialog.dismiss();
                    try{
                        String result = msg.getData().getString("result");
//                    try {
//                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
//                                .getData().getString("result"));
//                        if ((Boolean) keMap.get("success")) {
//                            ToastMessage("保存成功！");
//                            bt_commit.setEnabled(true);
//                            bt_commit.setChecked(false);
//                            bt_commit.setTextColor(getResources().getColor(
//                                    R.color.black));
//                            bt_uncommit.setEnabled(false);
//                            bt_uncommit.setChecked(true);
//                            bt_uncommit.setTextColor(getResources().getColor(
//                                    R.color.gray));
//                            bt_update.setEnabled(true);
//                            bt_update.setChecked(false);
//                            bt_update.setTextColor(getResources().getColor(
//                                    R.color.black));
//                            bt_save.setVisibility(View.GONE);
//                            ly_bottom_handler.setVisibility(View.VISIBLE);
////                            getPanelId(va_id);//再次发起请求
//                        }
//                    } catch (Exception e) {
//
//                    }

                        mkeyValue = JSON.parseObject(result).getIntValue("wo_id");
                        if (mkeyValue != -1){
                            jumpTODetails(mkeyValue);
                        }
                        System.out.println("保存 result:" + result);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case UPDATE_SAVE:
                    try{
                        if (msg.getData() != null) {
                            if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                                String doc_update_save_result = msg.getData().getString("result");
                                Log.d("doc_update_save_result",doc_update_save_result);
                                Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_LONG).show();
                                jumpTODetails(mkeyValue);
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case SUCCESS_PRE:
                    try{
                        System.out.println("获取id result:"
                                + msg.getData().getString("result"));
                        va_id = Integer.valueOf(FlexJsonUtil
                                .fromJson(msg.getData().getString("result")).get("id")
                                .toString());
                        getCodeHttpData();
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case SUCCESS_COMMIT:
                    progressDialog.dismiss();
//                    try {
//                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
//                                .getData().getString("result"));
//                        if ((Boolean) keMap.get("success")) {
//                            ToastMessage("提交成功！");
//                            bt_uncommit.setTextColor(getResources().getColor(
//                                    R.color.black));
//                            bt_uncommit.setEnabled(true);
//                            bt_uncommit.setChecked(false);
//                            bt_commit.setTextColor(getResources().getColor(
//                                    R.color.gray));
//                            bt_commit.setEnabled(false);
//                            bt_commit.setChecked(true);
//                            bt_update.setTextColor(getResources().getColor(
//                                    R.color.gray));
//                            bt_update.setEnabled(false);
//                            bt_update.setChecked(true);
//                            editnoclik();
//                        }
//                    } catch (Exception e) {
//                        messageDisplayCommit(msg);
//                        editnoclik();
//                    }
                    try{
                        System.out.println("提交 result:"
                                + msg.getData().getString("result"));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("提交 result:"
                            + msg.getData().getString("result"));
                    break;
                case SUCCESS_DELETE:
                    try{
                        System.out.println("删除  result:"
                                + msg.getData().getString("result"));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("删除  result:"
                            + msg.getData().getString("result"));
                    break;
                case SUCCESS_UNCOMMIT:
                    progressDialog.dismiss();
                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
                                .getData().getString("result"));
                        if ((Boolean) keMap.get("success")) {
                            ToastMessage("反提交成功！");
                            bt_commit.setTextColor(getResources().getColor(
                                    R.color.black));
                            bt_commit.setEnabled(true);
                            bt_commit.setChecked(false);
                            bt_update.setTextColor(getResources().getColor(
                                    R.color.black));
                            bt_update.setEnabled(true);
                            bt_update.setChecked(false);
                            bt_uncommit.setTextColor(getResources().getColor(
                                    R.color.gray));
                            bt_uncommit.setEnabled(false);
                            bt_uncommit.setChecked(true);
                            editclik();
                        }
                    } catch (Exception e) {
                        messageDisplay(msg);
                    }
                    System.out.println("反提交 result:"
                            + msg.getData().getString("result"));
                    break;
                case SUCCESS_UPDATE:
                    progressDialog.dismiss();
                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
                                .getData().getString("result"));
                        if ((Boolean) keMap.get("success")) {
                            ToastMessage("更新成功！");
                        }
                    } catch (Exception e) {
                        messageDisplay(msg);
                    }
                    System.out.println("更新 result:"
                            + msg.getData().getString("result"));
                    break;
                case SUCCESS_PRECODE:
                    try{
                        va_code = FlexJsonUtil
                                .fromJson(msg.getData().getString("result"))
                                .get("code").toString();
                        httpSave();
                        System.out.println("va_code=" + va_code);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case Constants.SUCCESS_INITDATA:
                    progressDialog.dismiss();
                    try {
                        String jsondata = msg.getData().getString("result");
                        Log.i("jsondata", jsondata);
                        Map<String, Object> map = FlexJsonUtil.fromJson(jsondata);
                        Log.i("jsondata",
                                "init paneldata  json=" + "["
                                        + FlexJsonUtil.toJson(map.get("panelData"))
                                        + "]");
                        List<ExtraWork> leaveEntities = FlexJsonUtil.fromJsonArray("["
                                        + FlexJsonUtil.toJson(map.get("panelData")) + "]",
                                ExtraWork.class);
                        List<ExtraWorkItems> items = FlexJsonUtil.fromJsonArray(
                                FlexJsonUtil.toJson(map.get("detailDatas")),
                                ExtraWorkItems.class);

                        initDataFromServer(leaveEntities, items);
                    } catch (Exception e) {
                        ViewUtil.ShowMessageTitle(ct, "数据解析异常");
                    }
                    break;
                case SUCCESS_TYPE://加班类型
                    progressDialog.dismiss();
                    try{
                        lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                                "combdatas");
                        if (lists.isEmpty()) {
                            lists.add("无");
                        }
                        title = "加班类型";
                        showTypeDialog(findViewById(R.id.et_extra_type));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case SUCCESS_ISALLDAY://是否全天
                    progressDialog.dismiss();
                    try{
                        lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                                "combdatas");
                        if (lists.isEmpty()) {
                            lists.add("无");
                        }
                        title = "是否为全天";
                        showTypeDialog(findViewById(R.id.et_extra_isallday));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case SUCCESS_MANKIND:
                    progressDialog.dismiss();
                    try{
                        lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                                "combdatas");
                        if (lists.isEmpty()) {
                            lists.add("无");
                        }
                        title = "是否为全天";
                        showListDialog("人员类型", et_extra_mankind);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    try{
                        String exception = msg.getData().getString("result");
                        ViewUtil.ToastMessage(mContext, exception);
                        progressDialog.dismiss();
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    };
    private double work_hours = 0.0;
    private int wod_id;

    /**
     * @desc:计算加班时间
     * @author：Arison on 2017/3/16
     */
    private double doCountTime(String start,String end) {
        double result = 0.0;
        if (!StringUtil.isEmpty(start)&&!StringUtil.isEmpty(end)){
            //计算加班时数
            Date startDate= null;
            Date endDate=null;
            try {
                startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(start);
                endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(end);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            result= (double) (DateFormatUtil.getDifferenceNum(startDate, endDate, 1))/60;
            LogUtil.d("当天加班时数：" + result);
        }
        return  result;
    }

    private void jumpTODetails(int mkeyValue) {
        JSONObject map = new JSONObject(true);
//        map.put("单据状态", "已提交");
        map.put(getString(R.string.overwork_purpose),et_extra_remark.getText().toString());
        LogUtil.d(map.toJSONString());
        JSONArray detail=new JSONArray();
        for (int i = 0; i < 1; i++) {
            JSONObject temp=new JSONObject(true);
            temp.put(getString(R.string.start_time),et_extra_starttime.getText().toString());
            temp.put(getString(R.string.end_time),et_extra_endtime.getText().toString());
            temp.put(getString(R.string.overwork_hours),et_extra_count.getText().toString());
            detail.add(temp);
        }
        startActivity(new Intent(ct, FormDetailActivity.class)
                .putExtra("data", map.toJSONString())
                .putExtra("detail", detail.toJSONString())
                .putExtra("title", getString(R.string.overtime_doc) + getString(R.string.doc_detail))
                .putExtra("mkeyValue",mkeyValue)
                .putExtra("whichpage",3)
                .putExtra("status", "已提交")
                .putExtra("ADDUI", "ADDUI"));

        Log.i("WorkExtraActivity.this",JSON.toJSONString(detail));

        finish();
    }

    private void initDataFromServer(List<ExtraWork> leaveEntities, List<ExtraWorkItems> items) {
        if (leaveEntities.isEmpty()) {
            return;
        }
        //主表
        et_extra_remark.setText(leaveEntities.get(0).getWo_worktask());
        et_extra_company.setText(leaveEntities.get(0).getWo_emname());
        et_extra_mankind.setText(leaveEntities.get(0).getWo_mankind());

        if (!items.isEmpty()) {//明细
            et_extra_type.setText(items.get(0).getWod_type());
            et_extra_starttime.setText(items.get(0).getWod_startdate()
                    .substring(0, items.get(0).getWod_startdate().length() - 3));
            et_extra_endtime.setText(items.get(0).getWod_enddate()
                    .substring(0, items.get(0).getWod_enddate().length() - 3));
            String status = leaveEntities.get(0).getWo_status();
            et_extra_count.setText("" + items.get(0).getWod_count());
            et_extra_company.setText(items.get(0).getWod_empname());
            wo_id = items.get(0).getWod_id();
            Wo_recorder = items.get(0).getWod_empcode();
            if (!StringUtil.isEmpty(status)) {
                if ("已提交".equals(status)) {
                    bt_commit.setTextColor(getResources().getColor(R.color.gray));
                    bt_commit.setEnabled(false);
                    bt_commit.setChecked(true);
                    bt_update.setTextColor(getResources().getColor(R.color.gray));
                    bt_update.setEnabled(false);
                    bt_update.setChecked(true);
                    editnoclik();
                }
                if ("在录入".equals(status)) {
                    bt_uncommit.setTextColor(getResources().getColor(R.color.gray));
                    bt_uncommit.setEnabled(false);
                    bt_uncommit.setChecked(true);
                    editclik();
                }
            }
        }
        va_code = leaveEntities.get(0).getWo_code();
        va_id = leaveEntities.get(0).getWo_id();

        bt_save.setVisibility(View.GONE);
        ly_bottom_handler.setVisibility(View.VISIBLE);
    }

    private String itemsdata;
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();

    }

    private String resubmit;
    public void initView() {
        setContentView(R.layout.act_extra_work);
        ViewUtils.inject(this);
        ct = this;
        progressDialog = new CustomProgressDialog(ct).createDialog(this);
        et_extra_type.setKeyListener(null);
        et_extra_no.setKeyListener(null);
        et_extra_company.setKeyListener(null);
        et_extra_endtime.setKeyListener(null);
        et_extra_starttime.setKeyListener(null);
        et_extra_isallday.setKeyListener(null);
        et_extra_type.setKeyListener(null);
        et_extra_mankind.setKeyListener(null);

        et_extra_type.setFocusable(false);
        et_extra_no.setFocusable(false);
        et_extra_company.setFocusable(false);
        et_extra_endtime.setFocusable(false);
        et_extra_starttime.setFocusable(false);
        et_extra_isallday.setFocusable(false);
        et_extra_type.setFocusable(false);
        et_extra_mankind.setFocusable(false);

        et_extra_company.setOnClickListener(this);
        et_extra_company.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));

        et_extra_endtime.setOnClickListener(this);
        et_extra_isallday.setOnClickListener(this);
        et_extra_starttime.setOnClickListener(this);
        et_extra_type.setOnClickListener(this);

        bt_save.setOnClickListener(this);
        bt_add.setOnClickListener(this);
        bt_commit.setOnClickListener(this);
        bt_uncommit.setOnClickListener(this);
        bt_update.setOnClickListener(this);
        et_extra_type.setOnClickListener(this);

        et_extra_mankind.setOnClickListener(this);
        findViewById(R.id.voice_search_iv).setOnClickListener(this);

        String current_time = TimeUtils.f_long_2_str(System.currentTimeMillis());
        et_extra_starttime.setText(current_time);
        et_extra_endtime.setText(current_time);
        Intent intent = getIntent();
        resubmit = intent.getStringExtra("submittype");
        if (!StringUtil.isEmpty(resubmit)){
            doShowResubmitData(intent);
        }
    }

    private void doShowResubmitData(Intent intent) {
        bt_save.setText(getString(R.string.common_resubmit_button));
        String date = intent.getStringExtra("data");
        String detail = intent.getStringExtra("detailJson");
        mkeyValue = intent.getIntExtra("mkeyValue",-1);

        wod_id = intent.getIntExtra("wod_id",-1);
        et_extra_remark.setText(JSON.parseObject(date).getString(getString(R.string.overwork_purpose)));

        et_extra_starttime.setText(JSON.parseArray(detail).getJSONObject(0).getString(getString(R.string.start_time)));
        et_extra_endtime.setText(JSON.parseArray(detail).getJSONObject(0).getString(getString(R.string.end_time)));

        et_extra_count.setText(CommonUtil.getTwoPointDouble(doCountTime(et_extra_starttime.getText().toString(),et_extra_endtime.getText().toString())) + "");
//        et_extra_count.setText(JSON.parseArray(detail).getJSONObject(0).getString("加班时长")+"");
    }

    public void initData() {
        Intent intent = getIntent();
        String formCondition = intent.getStringExtra("formCondition");
        String gridCondition = intent.getStringExtra("gridCondition");
        String caller = intent.getStringExtra("caller");
        if (!StringUtil.isEmpty(caller)) {
            // 需要初始化编辑界面
            String url = CommonUtil.getAppBaseUrl(this)
                    + "mobile/common/getPanel.action";
            String sessionId = CommonUtil.getSharedPreferences(this,
                    "sessionId");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("caller", caller);
            params.put("formCondition", formCondition);
            params.put("gridCondition", gridCondition);
            params.put("sessionId", sessionId);
//			progressDialog.show();

            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, handler, headers, Constants.SUCCESS_INITDATA, null, null, "get");
        } else {
            // getPreHttpData();
            // getCodeHttpData();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.et_extra_company:
                Intent et_dbfind = new Intent(ct, DbfindListActivity.class);
                startActivityForResult(et_dbfind, 1);
                break;
            case R.id.bt_save:
                if (et_extra_endtime.testValidity()
                        && et_extra_remark.testValidity()
                        && et_extra_starttime.testValidity()
                        ) {
                    boolean falg = ViewUtil.isCheckDateTime(et_extra_starttime
                            .getText().toString(), et_extra_endtime.getText()
                            .toString(), "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage(getString(R.string.endT_large_startT));
                    } else {
//                       getPreHttpData();
                        if (MyApplication.getInstance().isNetworkActive()){
                            httpSave();
                        }else {
                            ToastMessage(getResources().getString(R.string.networks_out));
                        }
                    }
                }
                break;
            case R.id.bt_add:   //新增
                httpAdd();
                if (bt_add.isEnabled()) {

                } else {

                }
                break;
            case R.id.bt_commit://提交
                httpCommit();
                break;
            case R.id.bt_uncommit:
                httpUncommit();
                break;
            case R.id.bt_update:
                if (et_extra_company.testValidity()
                        && et_extra_endtime.testValidity()
                        && et_extra_remark.testValidity()
                        && et_extra_mankind.testValidity()
                        && et_extra_starttime.testValidity()
                        && et_extra_type.testValidity()
                        ) {

                    boolean falg = ViewUtil.isCheckDateTime(et_extra_starttime
                            .getText().toString(), et_extra_endtime.getText()
                            .toString(), "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage(getString(R.string.endT_large_startT));
                    } else {
                        httpUpdate();
                    }
                }
                break;
            case R.id.et_extra_type:
                progressDialog.show();
                loadDataForServer("wod_type", SUCCESS_TYPE);

                break;
            case R.id.et_extra_isallday:
                progressDialog.show();
                loadDataForServer("wod_isallday", SUCCESS_ISALLDAY);
                break;
            case R.id.et_extra_endtime:
//                showDateDialog(this, et_extra_endtime);
                startActivityForResult(new Intent(mContext,SelectCalendarActivity.class)
                        .putExtra("startDate", et_extra_starttime.getText().toString())
                        .putExtra("endDate", et_extra_endtime.getText().toString())
                        .putExtra("hasMenu",false)
                        .putExtra("caller", "Workovertime")
                        ,0x30);
                break;
            case R.id.et_extra_starttime:
//                showDateDialog(this, et_extra_starttime);
                startActivityForResult(new Intent(mContext,SelectCalendarActivity.class)
                        .putExtra("startDate", et_extra_starttime.getText().toString())
                        .putExtra("endDate", et_extra_endtime.getText().toString())
                        .putExtra("hasMenu",false)
                        .putExtra("caller", "Workovertime")
                        ,0x30);
                break;
            case R.id.et_leave_mankind:
                progressDialog.show();
                loadDataForServer("wo_mankind", SUCCESS_MANKIND);
                break;
            case R.id.voice_search_iv:
                RecognizerDialogUtil.showRecognizerDialog(ct,this);
                break;
            case R.id.et_extra_count:
                work_hours = doCountTime(et_extra_starttime.getText().toString(),et_extra_endtime.getText().toString());
                if (work_hours > 0){
                    et_extra_count.setText(work_hours + "");
                    Log.d("work_hours",work_hours + "");
                }
                break;
            default:
                break;
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if ( !TextUtils.isEmpty(resubmit) && (resubmit.equals("resubmit"))){

        }else {
            getMenuInflater().inflate(R.menu.menu_platdoc_set, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_platdoc_set, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.oa_leave){
            Intent intent = new Intent();
            intent.setClass(this, WorkPageActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == android.R.id.home){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        super.onBackPressed();
    }
    /**
     * @注释：获取主键ID
     */
    public void getPreHttpData() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getId.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("seq", "SpeAttendance_SEQ");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRE, null, null, "get");
    }

    public void getCodeHttpData() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "common/getCodeString.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", "2");
        params.put("caller", "Workovertime");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRECODE, null, null, "get");
    }

    public void httpSave() {
//        if (et_extra_company.testValidity()
//                && et_extra_type.testValidity())
        {
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().overtime_save_url;
            Map<String, Object> params = new HashMap<String, Object>();
            if (mkeyValue != -1){
                ExtraUpdateWork entity = getUpdateJsonData();
                jsondata = FlexJsonUtil.toJson(entity);

                UpdateExtraAddWorkItems items = getUpdateJsonDataItem();
                itemsdata = FlexJsonUtil.toJson(items);
            }else {
                ExtraAddWork entity = getSaveJsonData();
                jsondata = FlexJsonUtil.toJson(entity);

                ExtraAddWorkItems items = getSaveJsonDataItem();
                itemsdata = FlexJsonUtil.toJson(items);
            }

            System.out.println("formStore=" + jsondata);
            System.out.println("gridStore=" + "[" + itemsdata + "]");
            params.put("formStore", jsondata);
            params.put("gridStore", "[" + itemsdata + "]");
            params.put("sessionId",
                    CommonUtil.getSharedPreferences(ct, "sessionId"));

            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            if (mkeyValue != -1 || mkeyValue != -1){
                ViewUtil.httpSendRequest(ct, url, params, handler, headers, UPDATE_SAVE, null, null, "post");
            }else {
                ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_SAVE, null, null, "post");
            }
        }
    }

    private ExtraUpdateWork getUpdateJsonData() {
        ExtraUpdateWork entity = new ExtraUpdateWork();
        entity.setWo_worktask(et_extra_remark.getText().toString().trim());
        entity.setWo_id(mkeyValue);
        entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));

        return entity;
    }


    public void getPanelId(int va_id) {
        String formCondition = "wo_id=" + va_id;
        String gridCondition = "wod_woid=" + va_id;
        String caller = "Workovertime";
        String url = CommonUtil.getAppBaseUrl(this)
                + "mobile/common/getPanel.action";
        String sessionId = CommonUtil.getSharedPreferences(this,
                "sessionId");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("caller", caller);
        params.put("formCondition", formCondition);
        params.put("gridCondition", gridCondition);
        params.put("sessionId", sessionId);
        Log.i("jsondata", "url=" + url + "\ncaller=" + caller + "\nformCondition=" + formCondition + "\ngridCondition=" + gridCondition + "\nsessionId=" + sessionId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + sessionId);
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, Constants.SUCCESS_INITDATA, null, null, "get");
    }

    private int wo_id;

    /**
     * @return
     */
    private ExtraAddWorkItems getSaveJsonDataItem() {
        ExtraAddWorkItems items = new ExtraAddWorkItems();
//        items.setWod_detno(1);
//        items.setWod_woid(va_id);//主表
//        items.setWod_id(wo_id);//明细表
//        items.setWod_empcode(CommonUtil.getSharedPreferences(ct, "erp_username"));
//        items.setWod_empname(et_extra_company.getText().toString());
        //items.setWod_empcode(va_code);
        items.setWod_count(Double.valueOf(et_extra_count.getText().toString().trim()).doubleValue());
//        items.setWod_type(et_extra_type.getText().toString());
        items.setWod_enddate(et_extra_endtime.getText().toString().trim());
        items.setWod_startdate(et_extra_starttime.getText().toString().trim());
        return items;
    }
    /**
     * @return
     */
    private UpdateExtraAddWorkItems getUpdateJsonDataItem() {
        UpdateExtraAddWorkItems items = new UpdateExtraAddWorkItems();
//        items.setWod_detno(1);
//        items.setWod_woid(va_id);//主表
//        items.setWod_id(wo_id);//明细表
//        items.setWod_empcode(CommonUtil.getSharedPreferences(ct, "erp_username"));
//        items.setWod_empname(et_extra_company.getText().toString());
        //items.setWod_empcode(va_code);
        items.setWod_id(wod_id);//明细表
        items.setWod_count(Double.valueOf(et_extra_count.getText().toString().trim()).doubleValue());
//        items.setWod_type(et_extra_type.getText().toString());
        items.setWod_enddate(et_extra_endtime.getText().toString().trim());
        items.setWod_startdate(et_extra_starttime.getText().toString().trim());
        return items;
    }
    private String Wo_recorder;//录入人  为空的时候，就取本地

    /**
     * @return
     */
    private ExtraAddWork getSaveJsonData() {
        ExtraAddWork entity = new ExtraAddWork();
//        entity.setWo_code(va_code);
//        entity.setWo_id(va_id);
//        if (StringUtil.isEmpty(Wo_recorder)) {//防止更新其它人,导致变换录入人
//            entity.setWo_emcode(CommonUtil.getSharedPreferences(ct, "erp_username"));
//        } else {
//            entity.setWo_emcode(Wo_recorder);
//        }
//        entity.setWo_emname(et_extra_company.getText().toString());
        entity.setWo_worktask(et_extra_remark.getText().toString().trim());
//        entity.setWo_mankind(et_extra_mankind.getText().toString());
        //entity.setWo_hour(Integer.valueOf(et_extra_count.getText().toString()));
//        entity.setWo_recorddate(new SimpleDateFormat("yyyy-MM-dd")
//                .format(new Date()));
        //entity.setWo_emname(CommonUtil.getSharedPreferences(ct, "username"));
        // entity.setWo_remark(et_extra_remark.getText().toString());
//        entity.setWo_status("在录入");
//        entity.setWo_statuscode("ENTERING");
//        if (StringUtil.isEmpty(Wo_recorder)) {
//            entity.setWo_recorder(CommonUtil.getSharedPreferences(ct, "erp_emname"));
//        } else {
//            entity.setWo_recorder(Wo_recorder);
//        }
//        entity.setWo_auditstatus("未处理");


        entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));

        return entity;
    }

    public void httpAdd() {
        et_extra_company.setText(CommonUtil.getSharedPreferences(ct,
                "erp_emname"));
        et_extra_type.setText("");
        et_extra_isallday.setText("");
        et_extra_endtime.setText("");
        et_extra_starttime.setText("");
        et_extra_mankind.setText("");
        et_extra_count.setText("");
        et_extra_remark.setText("");
        editclik();
        bt_save.setVisibility(View.VISIBLE);
        ly_bottom_handler.setVisibility(View.GONE);
    }

    String caller = "Workovertime";

    public void httpCommit() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "hr/attendance/submitWorkovertime.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", caller);
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_COMMIT, null, null, "get");
    }

    public void httpUncommit() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "hr/attendance/resSubmitWorkovertime.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", caller);
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UNCOMMIT, null, null, "get");
    }

    public void httpUpdate() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "hr/attendance/updateWorkovertime.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", caller);
        ExtraAddWork entity = getSaveJsonData();
        jsondata = FlexJsonUtil.toJson(entity);
        ExtraAddWorkItems entityItem = getSaveJsonDataItem();
        itemsdata = FlexJsonUtil.toJson(entityItem);
        System.out.println("明细：" + itemsdata);
        System.out.println("主数据：" + jsondata);
        params.put("formStore", jsondata);
        params.put("param", itemsdata);
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UPDATE, null, null, "get");
    }

    public void showTypeDialog(final View view) {
        // 要记住选中的记录，必须升级为全局变量
        SingleDialog typeDialog = new SingleDialog(ct, title,
                new SingleDialog.PickDialogListener() {
                    @Override
                    public void onListItemClick(int position, String value) {
                        if (view.getId() == R.id.et_extra_isallday) {
                            et_extra_isallday.setText(value);
                        }
                        if (view.getId() == R.id.et_extra_type) {
                            et_extra_type.setText(value);
                        }

                    }
                });
        typeDialog.show();
        typeDialog.initViewData(lists);
    }

    public void showDateDialog(final View view) {
        final DateTimePickerDialog dialog = new DateTimePickerDialog(this,
                System.currentTimeMillis());
        dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dia, long date) {
                if (view.getId() == R.id.et_extra_starttime) {
                    et_extra_starttime.setText(CommonUtil.getStringDateMM(date));
                }
                if (view.getId() == R.id.et_extra_endtime) {
                    et_extra_endtime.setText(CommonUtil.getStringDateMM(date));
                }
                /** @注释：保证 初始化当前时间 */

            }
        });
        dialog.show();
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

                String startTime = et_extra_starttime.getText().toString();
                String endTime = et_extra_endtime.getText().toString();
                if(!StringUtil.isEmpty(endTime) && !StringUtil.isEmpty(startTime) && endTime.compareTo(startTime) > 0){
                    work_hours = CommonUtil.getTwoPointDouble(doCountTime(startTime, endTime));
                    if (work_hours > 0){
                        et_extra_count.setText(work_hours + "");
                        Log.d("work_hours",work_hours + "");
                    }
                }
            }
        });
        picker.show();
    }

    /**
     * 系统提示信息
     *
     * @param msg
     */
    private void messageDisplay(Message msg) {
        String message = FlexJsonUtil
                .fromJson(msg.getData().getString("result"))
                .get("exceptionInfo").toString();
        ViewUtil.ShowMessageTitle(ct, message);

    }

    /**
     * 提交操作，异常也算成功，故写此方法
     *
     * @param msg
     */
    private void messageDisplayCommit(Message msg) {
        String message = FlexJsonUtil
                .fromJson(msg.getData().getString("result"))
                .get("exceptionInfo").toString();
        ViewUtil.ShowMessageTitle(ct, message);
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
        et_extra_company.setEnabled(false);
        et_extra_type.setEnabled(false);
        et_extra_isallday.setEnabled(false);
        et_extra_endtime.setEnabled(false);
        et_extra_mankind.setEnabled(false);
        et_extra_starttime.setEnabled(false);
        et_extra_remark.setEnabled(false);
    }

    public void editclik() {
        et_extra_company.setEnabled(true);
        et_extra_type.setEnabled(true);
        et_extra_isallday.setEnabled(true);
        et_extra_mankind.setEnabled(true);
        et_extra_endtime.setEnabled(true);
        et_extra_starttime.setEnabled(true);
        et_extra_remark.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                String en_name = data.getStringExtra("en_name");
                et_extra_company.setText(en_name);
                break;
            default:
                break;
        }
        if (requestCode == 0x30 && resultCode == 0x11){
            String startDate=data.getStringExtra("startDate");
            String endDate=data.getStringExtra("endDate");
            startDate=startDate+":00";
            endDate=endDate+":00";
            et_extra_starttime.setText(startDate);
            et_extra_endtime.setText(endDate);

            String startTime = et_extra_starttime.getText().toString();
            String endTime = et_extra_endtime.getText().toString();
            if(!StringUtil.isEmpty(endTime) && !StringUtil.isEmpty(startTime) && endTime.compareTo(startTime) > 0){
                work_hours = CommonUtil.getTwoPointDouble(doCountTime(startTime, endTime));
                if (work_hours > 0){
                    et_extra_count.setText(work_hours + "");
                    Log.d("work_hours",work_hours + "");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void loadDataForServer(String field, int what) {
        Log.i("leave", "what=" + what);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getCombo.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("caller", "Workovertime");
        param.put("field", field);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.getDataFormServer(ct, handler, url, param, what);
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
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        et_extra_remark.setText(et_extra_remark.getText().toString() + text);
    }

    @Override
    public void onError(SpeechError speechError) {

    }
}
