package com.xzjmyk.pm.activity.ui.erp.activity.crm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.SingleDialog;
import com.core.widget.view.Activity.SelectActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.view.crouton.Crouton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @功能:拜访计划录入
 * @author:Arisono
 * @param:
 * @return:
 */
public class VisitReportPlanActivity extends BaseActivity implements View.OnClickListener {
    @ViewInject(R.id.tv_date_select)
    private FormEditText tv_date_select;
    @ViewInject(R.id.tv_visit_enddate)
    private FormEditText tv_date_end;
    @ViewInject(R.id.tv_visit_theme)
    private FormEditText tv_visit_theme;

    @ViewInject(R.id.tv_address_login)
    private FormEditText tv_address_login;
    @ViewInject(R.id.tv_customer_login)
    private FormEditText tv_customer_login;
    @ViewInject(R.id.tv_linksman_login)
    private FormEditText tv_linksman_login;

    private int mVpId;
    private static final String TAG = "VisitReportPlanActivity";

    private String mCustomerName, mContactName, mCustomerAddress;
    private String mCustomerCode;

//    @ViewInject(R.id.tv_visit_login)
//    private FormEditText tv_visit_login;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (requestCode == 0x22 && resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null || StringUtil.isEmpty(b.getJson())) return;
            if (JSONUtil.validate(b.getJson())) {
                JSONObject object = JSON.parseObject(b.getJson());
                vp_code = object.containsKey("CU_CODE") ? object.getString("CU_CODE") : "";
                tv_customer_login.setText(object.containsKey("CU_NAME") ? object.getString("CU_NAME") : " ");
                tv_linksman_login.setText(object.containsKey("CU_CONTACT") ? object.getString("CU_CONTACT") : " ");
                tv_address_login.setText(object.containsKey("CU_ADD1") ? object.getString("CU_ADD1") : " ");
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_report_plan);
        Intent intent = getIntent();
        if (intent != null) {
            mCustomerName = intent.getStringExtra("customer_name");
            mContactName = intent.getStringExtra("contact_name");
            mCustomerAddress = intent.getStringExtra("customer_address");
            vp_code = intent.getStringExtra("customer_code");
        }
        initView();
        initData();
    }


    private void initView() {
        ViewUtils.inject(this);
      setTitle("拜访计划");
        tv_date_select.setText(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));

        tv_date_select.setKeyListener(null);
        tv_date_select.setFocusable(false);
        tv_date_select.setOnClickListener(this);

        tv_date_end.setKeyListener(null);
        tv_date_end.setFocusable(false);
        tv_date_end.setOnClickListener(this);

        tv_customer_login.setOnClickListener(this);

        if (!TextUtils.isEmpty(mCustomerName)) {
            tv_customer_login.setText(mCustomerName);
        }
        if (!TextUtils.isEmpty(mContactName)) {
            tv_linksman_login.setText(mContactName);
        }
        if (!TextUtils.isEmpty(mCustomerAddress)) {
            tv_address_login.setText(mCustomerAddress);
        } else {
            // tv_address_login.setText(MyApplication.getInstance().getBdLocationHelper().getAddress());
        }
    }


    private void initData() {
        CommonUtil.getCommonId(this, "VISITPLAN_SEQ", mHandler, GET_VP_ID);
        getCodeByNet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        menu.getItem(0).setTitle("保存");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_save:
                String start = tv_date_select.getText().toString().trim();
                String end = tv_date_end.getText().toString().trim();
                if (StringUtil.isEmpty(start)) {
                    Crouton.makeText(ct, "开始时间不能为空");
                } else if (StringUtil.isEmpty(end)) {
                    Crouton.makeText(ct, "截止时间不能为空");
                } else if (start.compareTo(end) < 0) {
                    sendHttpResquest(Constants.HTTP_SUCCESS_INIT);
                } else {
                    Crouton.makeText(ct, R.string.end_less_start);
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private JSONArray array;
    private static final int GET_VP_ID = 58;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage("保存成功！");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            endActivity();
                        }
                    }, 3000);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(msg.getData().getString("result"));
                    break;
                case codeWhat://获取编号
                    progressDialog.dismiss();
                    tv_code = JSON.parseObject(msg.getData().getString("result")).getString("code");
                    break;
                case 3:
                    progressDialog.dismiss();
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
                case GET_VP_ID:
                    String resultStr = msg.getData().getString("result");
                    JSONObject resultJsonObject = JSON.parseObject(resultStr);
                    if (resultJsonObject != null && resultJsonObject.getBoolean("success")) {
                        mVpId = resultJsonObject.getInteger("id");
                    }

                    break;
            }
        }
    };

    private void sendHttpResquest(int what) {
        String emname = "";
        if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_emname"))) {
            emname = CommonUtil.getSharedPreferences(ct, "erp_emname").trim();
        } else {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        String formStore = "";
        if (tv_date_select.testValidity() && tv_date_end.testValidity()
                && tv_customer_login.testValidity() && tv_linksman_login.testValidity()
                && tv_address_login.testValidity() && tv_visit_theme.testValidity()) {
            formStore = "\n" +
                    "{\n" +
                    " \"vp_id\":" + mVpId + ",\n" +
                    "\"vp_date\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd") +
                    "\",\n" +
                    "\"vp_code\":\"" + tv_code +
                    "\",\n" +
                    "\"vp_startdate\":\"" +
                    tv_date_select.getText().toString() +
                    "\",\n" +
                    "\"vp_enddate\":\"" +
                    tv_date_end.getText().toString() +
                    "\",\n" +
                    "\"vp_remark\":\"" +
                    CommonUtil.getNoMarkEditText(tv_visit_theme) +
                    "\",\n" +
                    "\"vp_custcode\":\"" +//客户编号
                    vp_code +
                    "\",\n" +
                    "\"vp_custname\":\"" +//客户名称
                    CommonUtil.getNoMarkEditText(tv_customer_login) +
                    "\",\n" +
                    "\"vp_address\":\"" +
                    CommonUtil.getNoMarkEditText(tv_address_login) +
                    "\",\n" +
                    "\"vp_contact\":\"" +
                    CommonUtil.getNoMarkEditText(tv_linksman_login) +
                    "\",\n" +
                    "\"vp_visitman\":\"" +
                    emname +
                    "\",\n" +
                    "\"vp_recordman\":\"" +
                    emname +
                    "\",\n" +
                    "\"vp_status\":\"" +
                    "未拜访" +
                    "\",\n" +
                    "\"vp_visitmancode\":\"" +
                    CommonUtil.getSharedPreferences(ct, "erp_username") +
                    "\",\n" +
                    "\"vp_recordate\":\"" +
                    DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd") +
                    "\"\n" +
                    "}";
        } else {
            return;
        }
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/saveVisitPlan.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        params.put("caller", "visitplan");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void endActivity() {
        Intent intent = new Intent();
        intent.putExtra("data", "data");
        setResult(0x20, intent);
        finish();
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
                Log.i(TAG, "onDateTimePicked:" + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                switch (tv.getId()) {
                    case R.id.tv_date_select:
                        tv_date_select.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                        break;
                    case R.id.tv_visit_enddate:
                        tv_date_end.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                        break;
                }
            }
        });
        picker.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_date_select:
                showDateDialog((FormEditText) v.findViewById(R.id.tv_date_select));
                break;
            case R.id.tv_visit_enddate:
                showDateDialog((FormEditText) v.findViewById(R.id.tv_visit_enddate));
                break;
            case R.id.tv_customer_login:
                //弹框
                HashMap<Object, Object> param = new HashMap<>();
                param.put("sellercode", CommonUtil.getSharedPreferences(ct, "erp_username"));
                Bundle bundle = new Bundle();
                bundle.putSerializable("param", param);
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("reid", R.style.OAThemeMeet);
                intent.putExtras(bundle);
                intent.putExtra("key", "customers");
                intent.putExtra("showKey", "CU_NAME");
                intent.putExtra("action", "mobile/crm/getCustomerbySeller.action");
                intent.putExtra("title", "客户列表");
                startActivityForResult(intent, 0x22);
                break;
        }
    }

    private static final int codeWhat = 0x11;
    private String tv_code;

    private void getCodeByNet() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
        final Map<String, Object> param = new HashMap<>();
        String caller = "visitplan";
        param.put("caller", caller);
        param.put("type", 2);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, codeWhat, null, null, "post");
    }


    private void loadCustomer(int what) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getCustomerbySeller.action";
        Map<String, Object> params = new HashMap<>();
        params.put("sellercode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


    private SingleDialog singleDialog;
    private String vp_code;
    private List<String> lists = new ArrayList<String>();

    public void showSimpleDialog(final FormEditText et, String title) {
        if (singleDialog != null) {
            if (singleDialog.isShowing())
                return;
        }
        singleDialog = new SingleDialog(ct, title,
                new SingleDialog.PickDialogListener() {
                    @Override
                    public void onListItemClick(int position, String value) {
                        et.setText(value);
                        for (int i = 0; i < array.size(); i++) {
                            if (value.equals(array.getJSONObject(i).getString("CU_NAME"))) {
                                vp_code = array.getJSONObject(i).getString("CU_CODE");
                                tv_linksman_login.setText(array.getJSONObject(i).getString("CU_CONTACT"));
                                tv_address_login.setText(array.getJSONObject(i).getString("CU_ADD1"));
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
}
