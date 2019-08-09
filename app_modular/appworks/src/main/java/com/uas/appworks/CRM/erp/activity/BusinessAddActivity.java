package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DatePicker;
import com.core.widget.SingleDialog;
import com.core.widget.view.Activity.SelectActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appworks.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @功能:商机添加
 * @author:Arisono
 * @param:
 * @return:
 */
public class BusinessAddActivity extends BaseActivity implements View.OnClickListener {

    private FormEditText et_business_no; // 天数
    private FormEditText et_business_name;
    private FormEditText et_business_source;
    private FormEditText et_business;
    private FormEditText et_business_jieDuan;
    private FormEditText et_business_enterMan;
    private FormEditText et_business_remark;
    private FormEditText et_business_type;
    private FormEditText et_company_name;
    private FormEditText et_company_address;
    private FormEditText et_company_man;
    private FormEditText et_company_position;
    private FormEditText et_company_tele;
    private FormEditText et_company_businesslicense;
    private FormEditText et_company_planmoney;
    private FormEditText et_company_plantime;
    private FormEditText et_company_depart;
    private FormEditText et_bc_factory;
    private FormEditText et_bc_tel;
    private MenuItem saveMenu;

    private List<String> lists = new ArrayList<String>();
    private int et_business_enterCode;
    private int mBcId;
    private String enterManCode;

    private static final int codeWhat = 0x11;
    private static final int LOAD_SOURCE = 2;
    private static final int LOAD_BUSINESS = 3;
    private static final int LOAD_JIEDUAN = 4;
    private static final int LOAD_MANGENJIN = 5;
    private static final int GET_BC_ID = 6;
    private JSONArray jsons;
    private JSONArray enterArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_add);
        AndroidBug5497Workaround.assistActivity(this);
        initIDS();
        initView();
        initData();
        initListener();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case codeWhat://获取编号
                    progressDialog.dismiss();
                    String temp = JSON.parseObject(msg.getData().getString("result")).getString("code");
                    et_business_no.setText(temp);
                    break;
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    ViewUtil.ToastMessage(activity, getString(R.string.business_add_success));
                    //保存商机成功
                    //TODO 2017-09-22 需求修改
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            finish();
//                        }
//                    }, 3000);
                    //TODO 2017-09-22 需求修改
//                    if (saveMenu != null) {
//                        saveMenu.setTitle(R.string.common_submit_button);
//                    }
                    submit();
                    break;
                case LOAD_BUSINESS://商机库
                    progressDialog.dismiss();
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String object = msg.getData().getString("result");
                    jsons = JSON.parseObject(object).getJSONArray("combos");
                    if (!jsons.isEmpty()) {
                        for (int i = 0; i < jsons.size(); i++) {
                            lists.add(jsons.getJSONObject(i).getString("BD_NAME"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(et_business, getString(R.string.business_Library));
                    break;
                case LOAD_JIEDUAN:
                    progressDialog.dismiss();
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String json = msg.getData().getString("result");
                    JSONArray array = JSON.parseObject(json).getJSONArray("stages");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            lists.add(array.getJSONObject(i).getString("BS_NAME"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(et_business_jieDuan, getString(R.string.business_stage));
                    break;
                case LOAD_MANGENJIN:
                    progressDialog.dismiss();
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    json = msg.getData().getString("result");
                    enterArray = JSON.parseObject(json).getJSONArray("recorders");
                    if (!enterArray.isEmpty()) {
                        for (int i = 0; i < enterArray.size(); i++) {
                            lists.add(enterArray.getJSONObject(i).getString("BC_DOMAN"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(et_business_enterMan, getString(R.string.Follow_up_person));
                    break;
                case LOAD_SOURCE://商机来源
                    progressDialog.dismiss();
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    object = msg.getData().getString("result");
                    jsons = JSON.parseObject(object).getJSONArray("combos");
                    if (!jsons.isEmpty()) {
                        for (int i = 0; i < jsons.size(); i++) {
                            //TODO 没有数据，不知道字段是什么  已实际情况修改字段
                            lists.add(jsons.getJSONObject(i).getString("DLC_VALUE"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(et_business_source, getString(R.string.business_from));
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(msg.getData().getString("result"));
                    break;
                case GET_BC_ID:
                    String resultStr = msg.getData().getString("result");
                    try {
                        JSONObject resultJsonObject = new JSONObject(resultStr);
                        if (resultJsonObject != null && resultJsonObject.getBoolean("success")) {
                            mBcId = resultJsonObject.getInt("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x14:
                    String message = msg.getData().getString("result");
                    if (!StringUtil.isEmpty(message) && JSONUtil.validate(message)) {
                        com.alibaba.fastjson.JSONObject o = JSON.parseObject(message);
                        JSONArray assigns = JSONUtil.getJSONArray(o, "assigns");
                        if (!ListUtils.isEmpty(assigns)) {
                            o = assigns.getJSONObject(0);
                            String noid = "";
                            if (o != null && o.containsKey("JP_NODEID")) {
                                noid = o.getString("JP_NODEID");
                            }
                            JSONArray data = null;
                            if (o != null && o.containsKey("JP_CANDIDATES")) {
                                data = o.getJSONArray("JP_CANDIDATES");
                            }
                            if (!StringUtil.isEmpty(noid) && data != null && data.size() > 0) {
                                sendToSelect(noid, data);
                            } else {
                                progressDialog.dismiss();
                                finish();
                            }
                        } else {
                            progressDialog.dismiss();
                            finish();
                        }
                    } else {
                        progressDialog.dismiss();
                        finish();
                    }
                    break;
                case 0x15:
                    progressDialog.dismiss();
                    finish();
                    Toast.makeText(MyApplication.getInstance(), R.string.submit_success, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void initIDS() {
        et_business_no = (FormEditText) findViewById(R.id.et_business_no);
        et_business_name = (FormEditText) findViewById(R.id.et_business_name);
        et_business_source = (FormEditText) findViewById(R.id.et_business_source);
        et_business = (FormEditText) findViewById(R.id.et_business);
        et_business_jieDuan = (FormEditText) findViewById(R.id.et_business_jieDuan);
        et_business_enterMan = (FormEditText) findViewById(R.id.et_business_enterMan);
        et_business_remark = (FormEditText) findViewById(R.id.et_business_remark);
        et_business_type = (FormEditText) findViewById(R.id.et_business_type);
        et_company_name = (FormEditText) findViewById(R.id.et_company_name);
        et_company_address = (FormEditText) findViewById(R.id.et_company_address);
        et_company_man = (FormEditText) findViewById(R.id.et_company_man);
        et_company_position = (FormEditText) findViewById(R.id.et_company_position);
        et_company_tele = (FormEditText) findViewById(R.id.et_company_tel);
        et_company_businesslicense = (FormEditText) findViewById(R.id.et_company_businesslicense);
        et_company_planmoney = (FormEditText) findViewById(R.id.et_company_planmoney);
        et_company_plantime = (FormEditText) findViewById(R.id.et_company_plantime);
        et_company_depart = (FormEditText) findViewById(R.id.et_company_depart);
        et_bc_factory = (FormEditText) findViewById(R.id.et_bc_factory);
        et_bc_tel = (FormEditText) findViewById(R.id.et_bt_tel);
    }

    private void initListener() {
        et_business.setOnClickListener(this);
        et_business_jieDuan.setOnClickListener(this);
        et_business_source.setOnClickListener(this);
        et_business_enterMan.setOnClickListener(this);

        et_company_plantime.setOnClickListener(this);
//        et_business_no.setOnClickListener(this);
        //   et_business.setKeyListener(null);
//        et_business_no.setKeyListener(null);
        //et_business_jieDuan.setKeyListener(null);
        et_business_source.setKeyListener(null);
        //   et_business_enterMan.setKeyListener(null);

        et_business_type.setOnClickListener(this);
//        et_business_enterTime.setOnClickListener(this);
        et_business_type.setKeyListener(null);
//        et_business_enterTime.setKeyListener(null);
        et_business_type.setFocusable(false);
//        et_business_enterTime.setFocusable(false);

        et_business_no.setKeyListener(null);
        et_business_no.setFocusable(false);

        et_company_plantime.setKeyListener(null);
        et_company_plantime.setFocusable(false);
    }

    private void initData() {
        CommonUtil.getCommonId(this, "BUSINESSCHANCE_SEQ", mHandler, GET_BC_ID);
        getCodeByNet();
    }

    private void initView() {
       setTitle("创建商机");
        if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_emname"))) {
            et_business_enterMan.setText(CommonUtil.getSharedPreferences(ct, "erp_emname"));
        } else {
            et_business_enterMan.setText(MyApplication.getInstance().mLoginUser.getNickName());
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.et_business) {
            loadBusiness();
        } else if (v.getId() == R.id.et_business_source) {
            loadSource();
        } else if (v.getId() == R.id.et_business_enterMan) {
            loadManGenJin();
        } else if (v.getId() == R.id.et_business_jieDuan) {
            loadJieDuan();
        } else if (v.getId() == R.id.et_business_type) {
            onPopupButtonClick(v);
        } else if (v.getId() == R.id.et_company_plantime) {
            showDateDialog((FormEditText) v.findViewById(R.id.et_company_plantime));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        saveMenu = menu.findItem(R.id.btn_save);
        saveMenu.setTitle(getString(R.string.common_save_button));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_save) {
            if (getString(R.string.common_submit_button).equals(item.getTitle())) {
                submit();
            } else {
                if (et_business_name.testValidity() &&
                        et_business_source.testValidity() &&
                        et_business.testValidity() &&
                        et_business_jieDuan.testValidity() &&
                        et_company_tele.testValidity() &&
                        et_company_businesslicense.testValidity() &&
                        et_company_address.testValidity() &&
                        et_company_name.testValidity() &&
                        et_bc_tel.testValidity() &&
                        et_bc_factory.testValidity()) {
                    sendHttpResquest();
                }
            }
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private String getEditText(FormEditText et) {
        String temp = "";
        temp = et.getText().toString().trim();
        temp = CommonUtil.removeStringMark(temp);
        return temp;
    }


    /**
     * @desc:保存商机
     * @author：Arison on 2016/7/20
     */
    private void sendHttpResquest() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "crm/chance/saveBusinessChance.action";
        Map<String, Object> params = new HashMap<>();
        String name = getEditText(et_business_name); //商机名称
        String from = getEditText(et_business_source); //商机来源
        String bt_tel = getEditText(et_company_tele); //电话（联系方式）
//        String bc_recorddate = getEditText(et_business_createTime); //创建时间
        String bc_nichehouse = getEditText(et_business); //商机库
        String bc_currentprocess = getEditText(et_business_jieDuan); //当前阶段
        String bc_recorder = CommonUtil.getSharedPreferences(mContext, "erp_emname"); //创建人
        String bc_address = getEditText(et_company_address); //地址
//        String bc_lastdate = getEditText(et_business_enterTime); //最后跟进时间
        String bc_custname = getEditText(et_company_name); //企业名称
        String bc_doman = getEditText(et_business_enterMan);//跟进人
        String bc_contact = getEditText(et_company_man); //联系人
        String bc_remark = getEditText(et_business_remark);//备注
        String bc_position = getEditText(et_company_position); //职位
        String bc_type = getEditText(et_business_type);  //商机类型
        String bc_plantime = getEditText(et_company_plantime);//预计成交时间
        String bc_planmoney = getEditText(et_company_planmoney);//预计成交时间
        String bc_depart = getEditText(et_company_depart);//部门
        String bc_linecse = getEditText(et_company_businesslicense);//营业执照
        String bc_factory = getEditText(et_bc_factory);//工厂地址
        String bc_tel = getEditText(et_bc_tel);//公司总机
        String enterCode = null;
        if (et_business_enterCode == 0) {//不可编辑  为空
            enterCode = "";
        } else {//可编辑
            if (StringUtil.isEmpty(enterManCode)) {//跟进人编号为空
                enterCode = CommonUtil.getSharedPreferences(ct, "erp_username");//默认自己
            } else {
                enterCode = enterManCode;
            }

        }
        LogUtil.e("commonbcid", mBcId + "");
        String status = "在录入";
        String statusCode = "ENTERING";
//        if ("(贝腾)贝腾科技".equals("公司名称")) {
//            status = "已提交";
//            statusCode = "COMMITED";
//        }
        String formStore =
                "{\n" +
                        "\"bc_id\":" + mBcId + "," +
                        "\"bc_position\":\"" + bc_factory + "\"," +//工厂地址
                        "\"bc_desc1\":\"" + bc_tel + "\"," +//公司总机
                        "\"bc_date7\":\"" + bc_plantime + "\"," +
                        "\"bc_desc8\":\"" + bc_planmoney + "\"," +
                        "\"bc_desc4\":\"" + bc_depart + "\"," +
                        "\"bc_desc6\":\"" + bc_linecse + "\"," +
                        "\"bc_code\":\"" + et_business_no.getText().toString() + "\"," +   //商机名称
                        "\"bc_status\":\"" + status + "\"," +
                        "\"bc_domancode\":\"" + enterCode + "\"," +
                        "\"bc_statuscode\":\"" + statusCode + "\"," +
                        "\"bc_description\":\"" + name + "\"," +   //商机名称
                        "\"bc_from\":\"" + from + "\"," +          //商机来源
                        "\"bc_nichehouse\":\"" + bc_nichehouse + "\"," +      //商机库
                        "\"bc_currentprocess\":\"" + bc_currentprocess + "\",\n" +   //当前阶段
                        "\"bc_recorddate\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +   //创建时间
                        "\"bc_recorder\":\"" + bc_recorder + "\",\n" +   //创建人
                        "\"bc_lastdate\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +   //最后跟进时间
                        "\"bc_doman\":\"" + bc_doman + "\",\n" +   //跟进人
                        "\"bc_remark\":\"" + bc_remark + "\",\n" +   //备注
                        "\"bc_custname\":\"" + bc_custname + "\",\n" +   //企业名称
                        "\"bc_address\":\"" + bc_address + "\",\n" +   //地址
                        "\"bc_contact\":\"" + bc_contact + "\",\n" +   //联系人
                        "\"bc_desc5\":\"" + bc_position + "\",\n" +   //职位bc_position
                        "\"bc_tel\":\"" + bt_tel + "\"\n" +   //电话
                       /* "\"bc_type\":\"" + bc_type + "\",\n" +   //商机类型*/
                        "}";
        Log.i(TAG, "sendHttpResquest:" + formStore);
        params.put("formStore", formStore);
        params.put("caller", "BusinessChance");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }

    private void submit() {
        if (mBcId <= 0) return;
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true)
                .connectTimeout(5000)
                .readTimeout(5000)
                .build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("crm/chance/submitBusinessChance.action")
                .add("caller", "BusinessChance")
                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId"))
                .add("master", CommonUtil.getSharedPreferences(ct, "erp_master"))
                .add("sessionUser", CommonUtil.getSharedPreferences(ct, "erp_username"))
                .add("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"))
                .add("id", mBcId)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object t) {
                String message = t.toString();
                LogUtil.i("message=" + message);
                judgeApprovers();
            }

            @Override
            public void onFailure(Object t) {
                String message = t.toString();
                ToastUtil.showToast(ct, message);
            }
        }));
    }

    private void judgeApprovers() {
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "BusinessChance");
        param.put("id", mBcId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, 0x14, null, null, "post");
    }

    private String noid;

    private void sendToSelect(String noid, JSONArray data) {
        this.noid = noid;
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        for (int i = 0; i < data.size(); i++) {
            bean = new SelectBean();
            bean.setName(data.getString(i));
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", getString(R.string.select_approvel_people));
        startActivityForResult(intent, 0x25);
    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) {
            finish();
            return;
        }
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                selectApprovers(name);
            } else {
                progressDialog.dismiss();
                finish();
            }
        } else {
            progressDialog.dismiss();
            finish();
        }
    }

    private void selectApprovers(String emName) {
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "common/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", emName);
        params.put("nodeId", noid);
        param.put("_noc", 1);
        param.put("params", JSONUtil.map2JSON(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, 0x15, null, null, "post");
    }


    private void getCodeByNet() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
        final Map<String, Object> param = new HashMap<>();
        String caller = "BusinessChance";
        param.put("caller", caller);
        param.put("type", 2);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, codeWhat, null, null, "post");
    }

    /**
     * @desc:加载来源
     * @author：Arison on 2016/7/20
     */
    public void loadSource() {
//        progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceCombo.action";
//        Map<String, Object> params = new HashMap<>();
//        params.put("caller", "BusinessChance");
//        params.put("field", "bc_from");
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_SOURCE, null, null, "post");

        HashMap param = new HashMap<>();
        param.put("caller", "BusinessChance");
        param.put("field", "bc_from");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combos");
        intent.putExtra("showKey", "DLC_VALUE");
        intent.putExtra("action", "/mobile/crm/getBusinessChanceCombo.action");
        intent.putExtra("title", getString(R.string.business_from));
        startActivityForResult(intent, 0x21);
    }

    /**
     * @desc:加载商机库
     * @author：Arison on 2016/7/20
     */
    public void loadBusiness() {
//        progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getNichehouse.action";
//        Map<String, Object> params = new HashMap<>();
////        params.put("caller", "nichefrom");
////        params.put("field", "bc_nichehouse");
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_BUSINESS, null, null, "post");


        HashMap param = new HashMap<>();
        param.put("condition", "1=1");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combos");
        intent.putExtra("showKey", "BD_NAME");
        intent.putExtra("action", "/mobile/crm/getNichehouse.action");
        intent.putExtra("title", getString(R.string.business_Library));
        startActivityForResult(intent, 0x24);
    }


    /**
     * @desc:加载阶段
     * @author：Arison on 2016/7/20
     */
    public void loadJieDuan() {
//        progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceStage.action";
//        Map<String, Object> params = new HashMap<>();
//        params.put("condition", "1=1");
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_JIEDUAN, null, null, "post");

        HashMap param = new HashMap<>();
        param.put("condition", "1=1");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "stages");
        intent.putExtra("showKey", "BS_NAME");
        intent.putExtra("action", "/mobile/crm/getBusinessChanceStage.action");
        intent.putExtra("title", getString(R.string.business_stage));
        startActivityForResult(intent, 0x22);
    }


    /**
     * @desc:加载跟进人
     * @author：Arison on 2016/7/20
     */
    public void loadManGenJin() {
//        HashMap param = new HashMap<>();
//        param.put("caller", "employee");
//        param.put("condition", "1=1");
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("param", param);
//        Intent intent = new Intent(ct, SelectActivity.class);
//        intent.putExtra("type", 1);
//        intent.putExtra("reid", R.style.OAThemeMeet);
//        intent.putExtras(bundle);
//        intent.putExtra("key", "recorders");
//        intent.putExtra("showKey", "BC_DOMAN");
//        intent.putExtra("action", "/mobile/crm/getBusinessChanceRecorder.action");
//        intent.putExtra("mTitle", "商机跟进入");
//        startActivityForResult(intent, 0x23);

        HashMap param = new HashMap<>();
        param.put("caller", "Ask4leave");
        param.put("which", "form");
        param.put("field", "va_emcode");
        param.put("condition", "1=1");
        param.put("page", "1");
        param.put("pageSize", "100");
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "");
        intent.putExtra("showKey", "em_name");
        intent.putExtra("action", "common/dbfind.action");
        intent.putExtra("title", getString(R.string.business_followup_people));
        startActivityForResult(intent, 0x23);
    }


    private SingleDialog singleDialog;

    public void showSimpleDialog(final FormEditText et, final String title) {
        if (singleDialog != null) {
            if (singleDialog.isShowing())
                return;
        }
        singleDialog = new SingleDialog(ct, title,
                new SingleDialog.PickDialogListener() {
                    @Override
                    public void onListItemClick(int position, String value) {
                        if (StringUtil.isEmpty(value)) return;
                        if (et.getId() == R.id.et_business_enterMan) {
                            et.setText(value);
                            for (int i = 0; i < enterArray.size(); i++) {
                                if (value.equals(enterArray.getJSONObject(i).getString("BC_DOMAN"))) {
                                    //获取跟进人编号
                                    enterManCode = enterArray.getJSONObject(i).getString("EM_CODE");
                                }
                            }
                        } else if (et.getId() == R.id.et_business_source) {
                            et.setText(value);
                        } else if (et.getId() == R.id.et_business) {
                            et.setText(value);
                            for (int i = 0; i < jsons.size(); i++) {
                                if (jsons.getJSONObject(i).getString("BD_NAME").equals(value)) {
                                    String bd_prop = jsons.getJSONObject(i).getString("BD_PROP");
                                    if (bd_prop.equals(getString(R.string.business_common))
                                            || bd_prop.equals(getString(R.string.business_get_split))) {
                                        //跟进人不可编辑，不可点击
                                        et_business_enterMan.setClickable(false);
                                        et_business_enterMan.setEnabled(false);
                                        et_business_enterMan.setText("");
                                        et_business_enterCode = 0;
                                    } else {
                                        //跟进人可编辑
                                        et_business_enterCode = 1;
                                        et_business_enterMan.setEnabled(true);
                                        et_business_enterMan.setClickable(true);
                                        jsons.getJSONObject(i).getString("BD_PROP");
                                        // et_business_enterMan.setText(MyApplication.getInstance().mLoginUser.getNickName());
                                    }
                                }
                            }
                        } else {
                            et.setText(value);
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

    private void showDateDialog(final FormEditText tv) {
        DatePicker picker = new DatePicker(this);
        picker.setRange(1950, 2030);
        picker.setSelectedItem(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
            @Override
            public void onDatePicked(String year, String month, String day) {
                tv.setText(year + "-" + month + "-" + day);
            }
        });
        picker.show();
    }


    PopupMenu popup = null;

    public void onPopupButtonClick(View button) {
        popup = new PopupMenu(this, button);
        if (button.getId() == R.id.et_business_type) {
            getMenuInflater().inflate(R.menu.menu_business_type, popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.items_public)
                    et_business_type.setText(item.getTitle());
                else if (item.getItemId() == R.id.items_private)
                    et_business_type.setText(item.getTitle());
                return true;
            }
        });
        popup.show();
    }


    /**
     * @desc:调转界面返回参数数据
     * @author：Arison on 2016/11/14
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.i("requestCode=" + requestCode);


        switch (resultCode) {
            case 0x20://单选
                SelectBean b = null;
                if (data != null) {
                    b = data.getParcelableExtra("data");
                }
                if (b == null) {
                    if (requestCode == 0x25) {
                        finish();
                    }
                } else {
                    if (StringUtil.isEmpty(b.getJson())) {
                        b.setJson("{}");
                    }
                    LogUtil.d(JSON.toJSONString(b));
                    switch (requestCode) {
                        case 0x21://来源
                            et_business_source.setText(b.getName());
                            break;
                        case 0x24://商机库
                            et_business.setText(b.getName());
                            String bd_prop = JSON.parseObject(b.getJson()).getString("BD_PROP");
                            if (bd_prop.equals(getString(R.string.business_common)) || bd_prop.equals(getString(R.string.business_get_split))) {
                                //跟进人不可编辑，不可点击
                                et_business_enterMan.setClickable(false);
                                et_business_enterMan.setEnabled(false);
                                et_business_enterMan.setText("");
                                et_business_enterCode = 0;
                            } else {
                                //跟进人可编辑
                                et_business_enterCode = 1;
                                et_business_enterMan.setEnabled(true);
                                et_business_enterMan.setClickable(true);
                            }
                            break;
                        case 0x23://跟进入
                            enterManCode = JSON.parseObject(b.getJson()).getString("em_code");
                            et_business_enterMan.setText(b.getName());
                            break;
                        case 0x22://当前阶段
                            et_business_jieDuan.setText(b.getName());
                            break;
                        case 0x25:
                            String name = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
                            getEmnameByReturn(name);
                            break;
                    }
                }
                break;
            default:
                if (requestCode == 0x25) {
                    finish();
                }

        }
    }
}
