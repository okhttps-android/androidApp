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
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
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
import com.xzjmyk.pm.activity.ui.erp.model.TravelAddEntity;
import com.xzjmyk.pm.activity.ui.erp.model.TravelAddItems;
import com.xzjmyk.pm.activity.ui.erp.model.TravelEntity;
import com.xzjmyk.pm.activity.ui.erp.model.TravelItems;
import com.xzjmyk.pm.activity.ui.erp.model.TravelUpdateEntity;
import com.xzjmyk.pm.activity.ui.erp.model.UpdateTravelAddItems;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;
import com.xzjmyk.pm.activity.ui.erp.view.DateTimePickerDialog;
import com.xzjmyk.pm.activity.ui.platform.pageforms.FormDetailActivity;
import com.xzjmyk.pm.activity.ui.platform.pageforms.TravelPageActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :LiuJie 2015年7月16日 上午11:06:19
 * @注释:出差申请
 */
public class TravelActivity extends BaseActivity implements OnClickListener, RecognizerDialogListener {
    private static final int UPDATE_SAVE = 0x322;
    @ViewInject(R.id.et_extra_no)
    private FormEditText et_extra_no;
    @ViewInject(R.id.et_trave_linkman)
    private FormEditText et_trave_linkman;
    @ViewInject(R.id.et_extra_deparment)
    private FormEditText et_extra_deparment;
    @ViewInject(R.id.et_trave_address)
    private FormEditText et_trave_address;//预计天数
    @ViewInject(R.id.et_trave_route)
    private FormEditText et_trave_route;//出差事由
    @ViewInject(R.id.et_extra_sign)
    private FormEditText et_extra_sign;
    @ViewInject(R.id.et_trave_type)
    private FormEditText et_trave_type;

    // 起始时间
    @ViewInject(R.id.et_extra_starttime)
    private FormEditText et_extra_starttime;
    @ViewInject(R.id.et_extra_endtime)
    private FormEditText et_extra_endtime;

    // 交通费，住宿费，公关费
    @ViewInject(R.id.et_trave_traffic)
    private FormEditText et_trave_traffic;//目的地
    @ViewInject(R.id.et_trave_stay)
    private FormEditText et_trave_stay;//接洽对象
    @ViewInject(R.id.et_trave_relations)
    private FormEditText et_trave_relations;//具体工作和目的

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

    @ViewInject(R.id.ly_bottom_save)
    private LinearLayout ly_bottom_save;
    @ViewInject(R.id.ly_bottom_handler)
    private LinearLayout ly_bottom_handler;
    @ViewInject(R.id.ry_extra_startdate)
    private RelativeLayout ry_extra_starttime;
    @ViewInject(R.id.ry_extra_endtime)
    private RelativeLayout ry_extra_endtime;

    private SingleDialog signDialog;
    private SingleDialog typeDialog;
    private List<String> lists = new ArrayList<String>();

    private final static int SUCCESS_SAVE = 1;
    private final static int SUCCESS_PRE = 0;
    private final static int SUCCESS_PRECODE = 6;
    private final static int SUCCESS_COMMIT = 2;
    private final static int SUCCESS_UNCOMMIT = 3;
    private final static int SUCCESS_UPDATE = 4;
    private final static int SUCCESS_DELETE = 5;

    private final static int SUCCESS_MANKIND = 9;
    private final static int SUCCESS_SIGN = 10;
    private String jsondata;
    private int va_id;
    private String va_code;
    private int va_fpid;

    private Context ct;
    private CustomProgressDialog progressDialog;

    public Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_SAVE:
                    progressDialog.dismiss();
                    try{
                        String result = msg.getData().getString("result");

                        Toast.makeText(getApplicationContext(), getString(R.string.common_save_success), Toast.LENGTH_LONG).show();
                        mkeyValue = JSON.parseObject(result).getIntValue("fp_id");

                        if (mkeyValue != -1 || mkeyValue != -1) {
                            jumpTODetails(mkeyValue);
                        } else {
                            progressDialog.dismiss();
                            bt_save.setEnabled(true);
                        }
                        LogUtil.e("travel", result);
//                    try {
//                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
//                                .getData().getString("result"));
//                        if ((Boolean) keMap.get("success")) {
//                            ToastMessage("保存成功！");
//                            bt_update.setEnabled(true);
//                            bt_update.setChecked(false);
//                            bt_update.setTextColor(getResources().getColor(R.color.black));
//                            bt_commit.setEnabled(true);
//                            bt_commit.setChecked(false);
//                            bt_commit.setTextColor(getResources().getColor(R.color.black));
//                            bt_uncommit.setEnabled(false);
//                            bt_uncommit.setChecked(true);
//                            bt_uncommit.setTextColor(getResources().getColor(R.color.gray));
//                            ly_bottom_save.setVisibility(View.GONE);
//                            ly_bottom_handler.setVisibility(View.VISIBLE);
//                            va_fpid = JSON.parseObject(result).getJSONArray("fpd_data").getJSONArray(0).getIntValue(0);
//                        }
//                    } catch (Exception e) {
//
//                    }
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
                                String update_save_result = msg.getData().getString("result");
                                Log.d("doc_update_save_result", update_save_result);
                                Toast.makeText(getApplicationContext(), getString(R.string.update_success), Toast.LENGTH_LONG).show();
                                jumpTODetails(mkeyValue);
                            } else {
                                progressDialog.dismiss();
                                bt_save.setEnabled(true);
                            }
                        } else {
                            progressDialog.dismiss();
                            bt_save.setEnabled(true);
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
                    try {
                        Map<Object, Object> keMap = FlexJsonUtil.fromJson(msg
                                .getData().getString("result"));
                        if ((Boolean) keMap.get("success")) {
                            ToastMessage(getString(R.string.submit_success));
                            bt_uncommit.setTextColor(getResources().getColor(
                                    R.color.black));
                            bt_uncommit.setEnabled(true);
                            bt_uncommit.setChecked(false);
                            bt_commit.setTextColor(getResources().getColor(
                                    R.color.gray));
                            bt_commit.setEnabled(false);
                            bt_commit.setChecked(true);
                            bt_update.setTextColor(getResources().getColor(
                                    R.color.gray));
                            bt_update.setEnabled(false);
                            bt_update.setChecked(true);
                            editnoclik();
                        }
                    } catch (Exception e) {
                        messageDisplayCommit(msg);
                        editnoclik();
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
                        System.out.println("va_code=" + va_code);
                        httpSave();
                    }catch (JSONException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case Constants.SUCCESS_INITDATA:
                    progressDialog.dismiss();
                    String jsondata = msg.getData().getString("result");
                    Log.i("jsondata", jsondata);
                    Map<String, Object> map = FlexJsonUtil.fromJson(jsondata);
                    Log.i("jsondata",
                            "init paneldata  json=" + "["
                                    + FlexJsonUtil.toJson(map.get("panelData"))
                                    + "]");
                    List<TravelEntity> leaveEntities = new ArrayList<TravelEntity>();
                    List<TravelItems> items = new ArrayList<TravelItems>();
                    try {
                        leaveEntities = FlexJsonUtil.fromJsonArray(
                                "[" + FlexJsonUtil.toJson(map.get("panelData")) + "]",
                                TravelEntity.class);
                        items = FlexJsonUtil.fromJsonArray(
                                FlexJsonUtil.toJson(map.get("detailDatas")),
                                TravelItems.class);
                        initDataFromServer(leaveEntities, items);
                    } catch (Exception e) {
                        ViewUtil.ShowMessageTitle(ct, "数据解析异常");
                    }

                    break;
                case SUCCESS_SIGN:
                    progressDialog.dismiss();
                    try{
                        lists = (List<String>) FlexJsonUtil.fromJson(msg.getData().getString("result")).get(
                                "combdatas");
                        if (lists.isEmpty()) {
                            lists.add("无");
                        }
                        showSignDialog(findViewById(R.id.et_extra_sign));
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
                        showTypeDialog(findViewById(R.id.et_trave_type));
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
    private int fpd_id;

    private void jumpTODetails(int mkeyValue) {
        progressDialog.dismiss();
        bt_save.setEnabled(true);
        JSONObject map = new JSONObject(true);
//        map.put("单据状态", "已提交");
        map.put(getString(R.string.start_time), et_extra_starttime.getText().toString());
        map.put(getString(R.string.end_time), et_extra_endtime.getText().toString());
        map.put(getString(R.string.travel_reason), et_trave_route.getText().toString());
        LogUtil.d(map.toJSONString());
        JSONArray detail = new JSONArray();
        for (int i = 0; i < 1; i++) {
            JSONObject temp = new JSONObject(true);
            temp.put(getString(R.string.travel_destination), et_trave_traffic.getText().toString());
            detail.add(temp);
        }
        startActivity(new Intent(ct, FormDetailActivity.class)
                .putExtra("data", map.toJSONString())
                .putExtra("detail", detail.toJSONString())
                .putExtra("title", getString(R.string.travel_doc) + getString(R.string.doc_detail))
                .putExtra("mkeyValue", mkeyValue)
                .putExtra("whichpage", 2)
                .putExtra("status", "已提交")
                .putExtra("ADDUI", "ADDUI"));

        finish();
    }

    private void doShowResubmitData(Intent intent) {
        bt_save.setText(getString(R.string.common_resubmit_button));
        String date = intent.getStringExtra("data");
        String detail = intent.getStringExtra("detailJson");
        mkeyValue = intent.getIntExtra("mkeyValue", -1);

        fpd_id = intent.getIntExtra("fpd_id", -1);
        et_extra_starttime.setText(String.valueOf(JSON.parseObject(date).getString(getString(R.string.start_time))));
        et_extra_endtime.setText(String.valueOf(JSON.parseObject(date).getString(getString(R.string.end_time))));
        et_trave_route.setText(JSON.parseObject(date).getString(getString(R.string.travel_reason)));

        et_trave_traffic.setText(JSON.parseArray(detail).getJSONObject(0).getString(getString(R.string.travel_destination)));
    }

    private String itemsdata;
    private int voice_type;
    private int mkeyValue = -1;
    private String resubmit;

    public void initView() {
        setContentView(R.layout.form_travel_main);
        ViewUtils.inject(this);
        ct = this;
        progressDialog = CustomProgressDialog.createDialog(this);
//        setTitle("出差申请");

        et_trave_type.setKeyListener(null);
        et_extra_sign.setKeyListener(null);
        et_extra_deparment.setKeyListener(null);
        et_trave_linkman.setKeyListener(null);
        et_extra_endtime.setKeyListener(null);
        et_extra_starttime.setKeyListener(null);

        et_trave_type.setFocusable(false);
        et_extra_sign.setFocusable(false);
        et_extra_deparment.setFocusable(false);
        et_trave_linkman.setFocusable(false);
        et_extra_endtime.setFocusable(false);
        et_extra_starttime.setFocusable(false);


        et_extra_endtime.setOnClickListener(this);
        et_extra_starttime.setOnClickListener(this);
        ry_extra_starttime.setOnClickListener(this);
        ry_extra_endtime.setOnClickListener(this);
        et_trave_linkman.setOnClickListener(this);
        bt_save.setOnClickListener(this);
        bt_commit.setOnClickListener(this);
        bt_add.setOnClickListener(this);
        bt_update.setOnClickListener(this);
        bt_uncommit.setOnClickListener(this);
        et_trave_type.setOnClickListener(this);
        et_extra_sign.setOnClickListener(this);
        et_trave_linkman.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));
        findViewById(R.id.voice_search_iv).setOnClickListener(this);
        findViewById(R.id.voice_event_iv).setOnClickListener(this);


        String current_time = TimeUtils.f_long_2_str(System.currentTimeMillis());
        et_extra_starttime.setText(current_time);
        et_extra_endtime.setText(current_time);
        Intent intent = getIntent();
        resubmit = intent.getStringExtra("submittype");
        if (!StringUtil.isEmpty(resubmit)) {
            doShowResubmitData(intent);
        }
    }


    protected void initDataFromServer(List<TravelEntity> leaveEntities,
                                      List<TravelItems> items) {
        if (leaveEntities.isEmpty()) {
            return;
        }
        TravelEntity entity = leaveEntities.get(0);
        if (!items.isEmpty()) {
            TravelItems entityitems = items.get(0);
            et_extra_starttime.setText(entityitems.getFpd_date1()
                    .substring(0, entityitems.getFpd_date1().length() - 3));
            et_extra_endtime.setText(entityitems.getFpd_date2()
                    .substring(0, entityitems.getFpd_date2().length() - 3));
            va_fpid = entityitems.getFpd_id();
            va_id = entityitems.getFpd_fpid();
            et_trave_traffic.setText(entityitems.getFPD_D6());
            et_trave_stay.setText(entityitems.getFPD_D4());
            et_trave_relations.setText(entityitems.getFpd_d2());
        }

        et_trave_type.setText(entity.getFp_type());
        //et_extra_endtime.setText(entity.getFp_preenddate());
        et_trave_address.setText("" + entity.getFP_N6());
        //et_trave_route.setText(entity.getFp_v3());

        et_trave_linkman.setText(entity.getFp_recordman());
//		et_extra_deparment.setText(entity.getFp_department());
        et_trave_type.setText(entity.getFp_type());
        //et_trave_address.setText(entity.getFp_v1());
        et_trave_route.setText(entity.getFp_v3());
        et_extra_sign.setText(entity.getFp_v6());

//		et_trave_traffic.setText(String.valueOf(entity.getFp_n2()));
//		et_trave_stay.setText(String.valueOf(entity.getFp_n3()));
//		et_trave_relations.setText(String.valueOf(entity.getFp_n4()));


        String status = leaveEntities.get(0).getFp_status();
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

        va_code = entity.getFp_code();
        ly_bottom_save.setVisibility(View.GONE);
        ly_bottom_handler.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
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
            progressDialog.show();

            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, handler, headers, Constants.SUCCESS_INITDATA, null, null, "get");
        } else {
//			getPreHttpData();
//			getCodeHttpData();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!TextUtils.isEmpty(resubmit) && (resubmit.equals("resubmit"))) {

        } else {
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
        if (item.getItemId() == R.id.oa_leave) {
            Intent intent = new Intent();
            intent.setClass(this, TravelPageActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == android.R.id.home) {
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
        // String url="http://192.168.253.167/ERP/common/getId.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("seq", "FEEPLEASE_SEQ");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRE, null, null, "get");
    }

    public void getCodeHttpData() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "common/getCodeString.action";
        // String url="http://192.168.253.167/ERP/common/getCodeString.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", "2");
        params.put("caller", "FeePlease!CCSQ");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_PRECODE, null, null, "get");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save:
                if (et_trave_traffic.testValidity() && et_trave_route.testValidity()
                        && et_extra_starttime.testValidity() && et_extra_endtime.testValidity()) {
                    boolean falg = ViewUtil.isCheckDateTime(et_extra_starttime.getText().toString()
                            , et_extra_endtime.getText().toString()
                            , "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage(getString(R.string.endT_large_startT));
                    } else {
//                        getPreHttpData();
                        if (MyApplication.getInstance().isNetworkActive()) {
                            httpSave();
                        } else {
                            ToastMessage(getString(R.string.networks_out));
                        }
                    }
                }
                break;
            case R.id.bt_add:
                httpAdd();
                break;
            case R.id.bt_commit:
                progressDialog.show();
                httpCommit();
                break;
            case R.id.bt_uncommit:
                progressDialog.show();
                httpUncommit();
                break;
            case R.id.bt_update:
                if (et_trave_traffic.testValidity()
                        && et_trave_stay.testValidity()
                        && et_trave_relations.testValidity()) {
                    boolean falg = ViewUtil.isCheckDateTime(et_extra_starttime.getText().toString()
                            , et_extra_endtime.getText().toString()
                            , "yyyy-MM-dd HH:mm");
                    if (falg) {
                        ToastMessage(getString(R.string.endT_large_startT));
                    } else {
                        progressDialog.show();
                        httpUpdate();
                    }
                }
                break;

            case R.id.et_extra_sign:
                progressDialog.show();
                loadDataForServer("fp_v6", SUCCESS_SIGN);
                break;
            case R.id.et_trave_type:
                //加载人员类型
                progressDialog.show();
                loadDataForServer("fp_type", SUCCESS_MANKIND);
                break;
            case R.id.et_trave_linkman:
                Intent et_dbfind = new Intent(ct, DbfindListActivity.class);
                startActivityForResult(et_dbfind, 1);
                break;
            case R.id.et_extra_endtime:
//                showDateDialog(this, et_extra_endtime);
                startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                                .putExtra("startDate", et_extra_starttime.getText().toString())
                                .putExtra("endDate", et_extra_endtime.getText().toString())
                                .putExtra("hasMenu", false)
                                .putExtra("caller", "FeePlease!CCSQ")
                        , 0x30);
                break;
            case R.id.et_extra_starttime:
//                showDateDialog(this, et_extra_starttime);
                startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                                .putExtra("startDate", et_extra_starttime.getText().toString())
                                .putExtra("endDate", et_extra_endtime.getText().toString())
                                .putExtra("hasMenu", false)
                                .putExtra("caller", "FeePlease!CCSQ")
                        , 0x30);
                break;
            case R.id.ry_extra_startdate:
                showDateDialog(this, et_extra_starttime);
                break;
            case R.id.ry_extra_endtime:
                showDateDialog(this, et_extra_endtime);
                break;
            case R.id.voice_search_iv:
                voice_type = 1;
                RecognizerDialogUtil.showRecognizerDialog(ct, this);

                break;
            case R.id.voice_event_iv:
                voice_type = 2;
                RecognizerDialogUtil.showRecognizerDialog(ct, this);
                break;
            default:
                break;
        }
    }

    /**
     *
     */
    public void httpSave() {

        progressDialog.show();
        bt_save.setEnabled(false);
        if (
//                et_trave_linkman.testValidity() &&
                et_trave_route.testValidity()
//                        && et_trave_address.testValidity() &&
//                        et_trave_type.testValidity() &&
//                        et_extra_sign.testValidity()
                ) {

            if (mkeyValue != -1) {
                TravelUpdateEntity entity = getUpdateJsonData();
                jsondata = FlexJsonUtil.toJson(entity);

                // 明细
                UpdateTravelAddItems items = getUpdateJsonDataItem();
                itemsdata = FlexJsonUtil.toJson(items);
            } else {
                TravelAddEntity entity = getSaveJsonData();
                jsondata = FlexJsonUtil.toJson(entity);

                // 明细
                TravelAddItems items = getSaveJsonDataItem();
                itemsdata = FlexJsonUtil.toJson(items);
            }


            // 路径
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().travel_save_url;
            Map<String, Object> params = new HashMap<String, Object>();
            System.out.println("url:" + url);

            System.out.println("formStore=" + jsondata);
            System.out.println("gridStore=" + itemsdata);
            params.put("formStore", jsondata);
            params.put("gridStore", "[" + itemsdata + "]");
            params.put("sessionId",
                    CommonUtil.getSharedPreferences(ct, "sessionId"));

            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            if (mkeyValue != -1 || mkeyValue != -1) {
                ViewUtil.httpSendRequest(ct, url, params, handler, headers, UPDATE_SAVE, null, null, "post");
            } else {
                ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_SAVE, null, null, "post");
            }

        }
    }

    private TravelUpdateEntity getUpdateJsonData() {
        TravelUpdateEntity entity = new TravelUpdateEntity();
        entity.setFp_id(mkeyValue);
        entity.setFp_prestartdate(et_extra_starttime.getText().toString());//开始时间
        entity.setFp_preenddate(et_extra_endtime.getText().toString());//结束时间
        entity.setFp_v3(et_trave_route.getText().toString());//
        entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));
        return entity;
    }

    /**
     * @return
     */
    private TravelAddEntity getSaveJsonData() {
        TravelAddEntity entity = new TravelAddEntity();
        //entity.setFp_department(et_extra_deparment.getText().toString());
        //entity.setFp_pleaseman(et_trave_linkman.getText().toString());
//        entity.setFp_type(et_trave_type.getText().toString());
//		entity.setFp_v1(et_trave_address.getText().toString());
//        entity.setFp_v6(et_extra_sign.getText().toString());
//		entity.setFp_n2(Integer.valueOf(et_trave_traffic.getText().toString()));
//		entity.setFp_n3(Integer.valueOf(et_trave_stay.getText().toString()));
//		entity.setFp_n4(Integer
//				.valueOf(et_trave_relations.getText().toString()));
//        entity.setFp_code(va_code);
//        entity.setFP_PEOPLE2(CommonUtil.getSharedPreferences(ct, "erp_username"));//人员编号
        //entity.setFP_N6(Integer.valueOf(et_trave_address.getText().toString()));//预计天数//
        entity.setFp_prestartdate(et_extra_starttime.getText().toString());//开始时间
        entity.setFp_preenddate(et_extra_endtime.getText().toString());//结束时间
        entity.setFp_v3(et_trave_route.getText().toString());//
//        entity.setFp_id(va_id);//
//        entity.setFp_statuscode("ENTERING");
//        entity.setFp_status("在录入");
//        entity.setFp_kind("出差申请单");
//        entity.setFp_recordman(MyApplication.getInstance().mLoginUser.getNickName());
//        entity.setFp_recorddate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));//
        entity.setEnuu(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        entity.setEmcode(CommonUtil.getSharedPreferences(this, "b2b_uu"));

        return entity;
    }

    /**
     * @return
     */
    private TravelAddItems getSaveJsonDataItem() {
        TravelAddItems items = new TravelAddItems();
        items.setFpd_location(et_trave_traffic.getText().toString().trim());
//        items.setFPD_D6(et_trave_traffic.getText().toString());
//        items.setFPD_D4(et_trave_stay.getText().toString());
//        items.setFpd_d2(et_trave_relations.getText().toString());
//        items.setFpd_id(va_fpid);
//        items.setFpd_fpid(va_id);
//        items.setFpd_detno(1);
//        items.setFpd_date2(et_extra_endtime.getText().toString() + ":00");
//        items.setFpd_date1(et_extra_starttime.getText().toString() + ":00");
        return items;
    }

    private UpdateTravelAddItems getUpdateJsonDataItem() {
        UpdateTravelAddItems items = new UpdateTravelAddItems();
        items.setFpd_location(et_trave_traffic.getText().toString().trim());
        items.setFpd_id(fpd_id);
//        items.setFPD_D6(et_trave_traffic.getText().toString());
//        items.setFPD_D4(et_trave_stay.getText().toString());
//        items.setFpd_d2(et_trave_relations.getText().toString());
//        items.setFpd_id(va_fpid);
//        items.setFpd_fpid(va_id);
//        items.setFpd_detno(1);
//        items.setFpd_date2(et_extra_endtime.getText().toString() + ":00");
//        items.setFpd_date1(et_extra_starttime.getText().toString() + ":00");
        return items;
    }

    public void httpAdd() {

        et_extra_endtime.setText("");
        et_extra_starttime.setText("");

        et_extra_deparment.setText("");
        et_trave_linkman.setText(CommonUtil.getSharedPreferences(this, "erp_emname"));
        et_trave_type.setText("");
        et_trave_address.setText("");
        et_trave_route.setText("");
        et_extra_sign.setText("");
        et_trave_traffic.setText("");
        et_trave_stay.setText("");
        et_trave_relations.setText("");

        editclik();
        ly_bottom_save.setVisibility(View.VISIBLE);
        ly_bottom_handler.setVisibility(View.GONE);
    }

    /**
     * @注释：提交
     */
    public void httpCommit() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "oa/fee/submitFeePlease.action";
        // String
        // url="http://192.168.253.167/ERP/oa/fee/submitFeePlease.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "FeePlease!CCSQ");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_COMMIT, null, null, "get");


    }

    /**
     * @注释：反提交
     */
    public void httpUncommit() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "oa/fee/resSubmitFeePlease.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "FeePlease!CCSQ");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UNCOMMIT, null, null, "get");
    }

    /**
     * @注释：删除
     */
    public void httpDelete() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "oa/check/deleteVacation.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "FeePlease!CCSQ");
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_DELETE, null, null, "get");
    }

    /**
     * @注释：更新
     */
    public void httpUpdate() {
        String url = CommonUtil.getAppBaseUrl(ct)
                + "oa/fee/updateFeePlease.action";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", String.valueOf(va_id));
        params.put("caller", "FeePlease!CCSQ");
//		if (StringUtil.isEmpty(jsondata)) {
        TravelAddEntity entity = getSaveJsonData();
        jsondata = FlexJsonUtil.toJson(entity);
//		}
//		if (StringUtil.isEmpty(itemsdata)) {
        TravelAddItems entityItem = getSaveJsonDataItem();
        itemsdata = FlexJsonUtil.toJson(entityItem);
        System.out.println("items:" + itemsdata);
//		}
        params.put("formStore", jsondata);
        params.put("param", itemsdata);
        params.put("sessionId",
                CommonUtil.getSharedPreferences(ct, "sessionId"));


        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, SUCCESS_UPDATE, null, null, "get");
    }

    public void showTypeDialog(View view) {
        if (typeDialog == null) {
            typeDialog = new SingleDialog(ct, "人员类型", new SingleDialog.PickDialogListener() {
                @Override
                public void onListItemClick(int position, String value) {
                    et_trave_type.setText(value);
                }
            });
            typeDialog.show();
            typeDialog.initViewData(lists);
        } else {
            typeDialog.show();
            typeDialog.initViewData(lists);
        }
    }

    public void showSignDialog(View view) {
        if (signDialog == null) {
            signDialog = new SingleDialog(ct, "考勤", new SingleDialog.PickDialogListener() {
                @Override
                public void onListItemClick(int position, String value) {
                    et_extra_sign.setText(value);
                }
            });
            signDialog.show();
            signDialog.initViewData(lists);
        } else {
            signDialog.show();
            signDialog.initViewData(lists);
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
                et_trave_linkman.setText(en_name);
                data.getStringExtra("en_code");
                et_extra_deparment.setText(data.getStringExtra("en_depart"));
                break;
            default:
                break;
        }
        if (requestCode == 0x30 && resultCode == 0x11) {
            String startDate = data.getStringExtra("startDate");
            String endDate = data.getStringExtra("endDate");
            startDate = startDate + ":00";
            endDate = endDate + ":00";
            et_extra_starttime.setText(startDate);
            et_extra_endtime.setText(endDate);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        bt_commit.setTextColor(getResources().getColor(R.color.grey));
        bt_uncommit.setTextColor(getResources().getColor(R.color.black));
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
        et_extra_endtime.setEnabled(false);
        et_extra_starttime.setEnabled(false);
        et_extra_deparment.setEnabled(false);
        et_trave_linkman.setEnabled(false);
        et_trave_type.setEnabled(false);
        et_trave_address.setEnabled(false);
        et_trave_route.setEnabled(false);
        et_extra_sign.setEnabled(false);
        et_trave_traffic.setEnabled(false);
        et_trave_stay.setEnabled(false);
        et_trave_relations.setEnabled(false);
    }

    public void editclik() {
        et_extra_endtime.setEnabled(true);
        et_extra_starttime.setEnabled(true);
        et_extra_deparment.setEnabled(true);
        et_trave_linkman.setEnabled(true);
        et_trave_type.setEnabled(true);
        et_trave_address.setEnabled(true);
        et_trave_route.setEnabled(true);
        et_extra_sign.setEnabled(true);
        et_trave_traffic.setEnabled(true);
        et_trave_stay.setEnabled(true);
        et_trave_relations.setEnabled(true);
    }

    public void loadDataForServer(String field, int what) {
        Log.i("leave", "what=" + what);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getCombo.action";
        Map<String, String> param = new HashMap<String, String>();
        param.put("caller", "FeePlease!CCSQ");
        param.put("field", field);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.getDataFormServer(ct, handler, url, param, what);
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        if (voice_type == 1) {
            et_trave_traffic.setText(et_trave_traffic.getText().toString() + CommonUtil.getPlaintext(text));
        } else if (voice_type == 2) {
            et_trave_route.setText(et_trave_route.getText().toString() + text);
        }
    }

    @Override
    public void onError(SpeechError speechError) {

    }
}
