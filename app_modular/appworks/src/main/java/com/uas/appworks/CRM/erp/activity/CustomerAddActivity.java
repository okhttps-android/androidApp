package com.uas.appworks.CRM.erp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.SingleDialog;
import com.core.widget.view.Activity.SelectActivity;
import com.uas.appworks.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 预录入客户页面
 */
public class CustomerAddActivity extends BaseActivity implements View.OnClickListener {

    private FormEditText et_cu_code;
    private FormEditText et_cu_name;
    private FormEditText et_cu_shortname;
    private FormEditText tv_cu_address;
    private FormEditText et_cu_kind;
    private FormEditText et_cu_district;
    private FormEditText et_cu_payments;
    private FormEditText et_cu_sellername;
    private FormEditText tv_cu_contact;
    private FormEditText tv_cu_degree;
    private FormEditText tv_cu_mobile;
    private FormEditText tv_cu_email;
    private FormEditText tv_cu_remark;
    private FormEditText tv_cu_businesscode;
    private FormEditText tv_cu_currency;
    private FormEditText tv_cu_taxrate;
    private FormEditText tv_cu_nichestep;
    private LinearLayout ll_infomal;
    private TextView bt_save;
    private TextView bt_commit;
    private TextView bt_uncommit;

    private List<String> mLists = new ArrayList<String>();
    private String et_cu_sellercode;
    private String mCuId;

    private String tv_code;
    private int type;//预录入客户，正式客户
    PopupMenu popup = null;
    private final int COMMIT = 9;
    private final int UNCOMMIT = 10;
    private final int UPDATE = 11;
    private String pa_code;
    private String cu_status;

    private String cu_code;//客户编号
    private String mCompanyName;//客户名称
    private String mShortName;//简称
    private String mCompanyAddress;//客户地址
    private String mCuKind;//客户类型
    private String mCuDistrict;//所属地区
    private String mPayments;//收款方式
    private String mSellerMan;//业务员
    private String mContact;//联系人
    private String mPosition;//岗位
    private String mTelephone;//电话
    private String mEmail;//邮箱
    private String mBusinessCode;//营业执照
    private String mCurrency;//币别
    private String mTaxrate;//税率
    private String mNicheStep;//当前阶段
    private String mRemark;//备注

    public final static int RESULT_CUSTOMER_LIST = 55;
    private String bc_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customter_add);
        AndroidBug5497Workaround.assistActivity(this);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        et_cu_kind.setOnClickListener(this);
        et_cu_district.setOnClickListener(this);
        tv_cu_nichestep.setOnClickListener(this);
        et_cu_payments.setOnClickListener(this);
        tv_cu_currency.setOnClickListener(this);
        et_cu_sellername.setOnClickListener(this);

        et_cu_kind.setFocusable(false);
        et_cu_district.setFocusable(false);
        tv_cu_nichestep.setFocusable(false);
        et_cu_payments.setFocusable(false);
        tv_cu_currency.setFocusable(false);
        et_cu_sellername.setFocusable(false);

        et_cu_kind.setKeyListener(null);
        et_cu_district.setKeyListener(null);
        tv_cu_nichestep.setKeyListener(null);
        et_cu_payments.setKeyListener(null);
        tv_cu_currency.setKeyListener(null);
        et_cu_sellername.setKeyListener(null);

        bt_save.setOnClickListener(this);
        bt_commit.setOnClickListener(this);
        bt_uncommit.setOnClickListener(this);

        tv_cu_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!StringUtil.isEmpty(s.toString())) {
                    tv_cu_remark.testValidity();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initData() {
        if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_emname"))) {
            et_cu_sellername.setText(CommonUtil.getSharedPreferences(ct, "erp_emname").trim());
        } else {
            et_cu_sellername.setText(MyApplication.getInstance().mLoginUser.getNickName().trim());
        }

        if (cu_code != null) {
            et_cu_code.setText(cu_code);
            et_cu_code.setEnabled(false);
        } else {
            // getCodeByNet();
        }
        loadParams(FORM_TYPE);//判断预录入
    }


    private void initView() {
        initIDS();
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", 0);
            cu_code = intent.getStringExtra("code");
            cu_status = intent.getStringExtra("status");
            bc_code = intent.getStringExtra("bc_code");

            mCompanyName = intent.getStringExtra("companyname");
            mCompanyAddress = intent.getStringExtra("companyaddress");
            mContact = intent.getStringExtra("contact");
            mPosition = intent.getStringExtra("position");
            mTelephone = intent.getStringExtra("telephone");
            et_cu_sellercode = CommonUtil.getSharedPreferences(ct, "erp_username");
            if ("已提交".equals(cu_status)) {
                disableEdit(false);
            } else {
                disableEdit(true);
            }
            if (type == 1) {
                ll_infomal.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.formal_customer));
                if (StringUtil.isEmpty(cu_code)) {
                    et_cu_code.setText("");
                    et_cu_code.setEnabled(true);
                } else {
                    initCustomerData(INITCUSTOMER, cu_code);   //初始化数据
                }

            } else {
                ll_infomal.setVisibility(View.GONE);
                setTitle(getString(R.string.client_yu_luru));
            }

            if (mCompanyName != null) {
                et_cu_name.setText(mCompanyName);
            }
            if (mCompanyAddress != null) {
                tv_cu_address.setText(mCompanyAddress);
            }
            if (mContact != null) {
                tv_cu_contact.setText(mContact);
            }
            if (mPosition != null) {
                tv_cu_degree.setText(mPosition);
            }
            if (mTelephone != null) {
                tv_cu_mobile.setText(mTelephone);
            }
        }
    }

    private void initIDS() {
        et_cu_code = (FormEditText) findViewById(R.id.et_cu_code);
        et_cu_name = (FormEditText) findViewById(R.id.et_cu_name);
        et_cu_shortname= (FormEditText) findViewById(R.id.et_cu_shortname);
        tv_cu_address = (FormEditText) findViewById(R.id.tv_cu_address);
        et_cu_kind = (FormEditText) findViewById(R.id.et_cu_kind);
        et_cu_district = (FormEditText) findViewById(R.id.et_cu_district);
        et_cu_payments= (FormEditText) findViewById(R.id.et_cu_payments);
        et_cu_sellername = (FormEditText) findViewById(R.id.et_cu_sellername);
        tv_cu_contact= (FormEditText) findViewById(R.id.tv_cu_contact);
        tv_cu_degree= (FormEditText) findViewById(R.id.tv_cu_degree);
        tv_cu_mobile = (FormEditText) findViewById(R.id.tv_cu_mobile);
        tv_cu_email = (FormEditText) findViewById(R.id.tv_cu_email);
        tv_cu_remark = (FormEditText) findViewById(R.id.tv_cu_remark);
        tv_cu_businesscode = (FormEditText) findViewById(R.id.tv_cu_businesscode);
        tv_cu_businesscode = (FormEditText) findViewById(R.id.tv_cu_businesscode);
        tv_cu_taxrate = (FormEditText) findViewById(R.id.tv_cu_taxrate);
        tv_cu_nichestep= (FormEditText) findViewById(R.id.tv_cu_nichestep);
        ll_infomal = (LinearLayout) findViewById(R.id.ll_infomal);
        bt_save = (TextView) findViewById(R.id.bt_save);
        bt_commit= (TextView) findViewById(R.id.bt_commit);
        bt_uncommit = (TextView) findViewById(R.id.bt_uncommit);
        tv_cu_currency = (FormEditText) findViewById(R.id.tv_cu_currency);
    }

    private Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        mMenu = menu;
        if (type == 1) {
            if (cu_code != null && !"已提交".equals(cu_status)) {
                menu.findItem(R.id.btn_save).setVisible(true);

            } else {
                menu.findItem(R.id.btn_save).setVisible(false);

            }

        } else if (type == 0) {
            menu.findItem(R.id.btn_save).setVisible(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_save){
            createStore();
            if (!TextUtils.isEmpty(formStore)) {
                update(UPDATE, formStore);
            }
        }else if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    String formStore = "";

    /**
     * @desc:保存之后，出现提交按钮，更新按钮，可以编辑
     * @author：Arison on 2016/9/20
     */
    private void saveData() {
        createStore();
        if (!StringUtil.isEmpty(formStore)) {
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, formStore);
        }
    }

    public void createStore() {
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        Log.i("commoncuid", mCuId + "");
        if (type == 1) {
            if (et_cu_code.testValidity() && et_cu_name.testValidity()
                    && tv_cu_address.testValidity() && et_cu_kind.testValidity()
                    && et_cu_district.testValidity() && et_cu_kind.testValidity()
                    && et_cu_payments.testValidity() && et_cu_sellername.testValidity()
                    && tv_cu_contact.testValidity() && tv_cu_degree.testValidity()
                    && tv_cu_mobile.testValidity() && tv_cu_email.testValidity()
                    && tv_cu_businesscode.testValidity() && tv_cu_currency.testValidity()
                    && tv_cu_taxrate.testValidity() && tv_cu_nichestep.testValidity()
                    ) {
                //
                formStore = "{\n" +
                        " \"cu_nichecode\":" + bc_code + ",\n" +
                        " \"cu_id\":" + mCuId + ",\n" +
                        " \"cu_recordman\":\"" + emname + "\",\n" +
                        " \"cu_recorddate\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +
                        " \"cu_status\":" + "\"长期\"" + ",\n" +
                        " \"cu_arcode\":" + "\"\"" + ",\n" +
                        " \"cu_arname\":" + "\"\"" + ",\n" +
                        " \"cu_code\":\"" + CommonUtil.getNoMarkEditText(et_cu_code) + "\",\n" +
                        " \"cu_name\":\"" + CommonUtil.getNoMarkEditText(et_cu_name) + "\",\n" +
                        " \"cu_shortname\":\"" + CommonUtil.getNoMarkEditText(et_cu_shortname) + "\",\n" +
                        " \"cu_add1\":\"" + CommonUtil.getNoMarkEditText(tv_cu_address) + "\",\n" +
                        " \"cu_kind\":\"" + CommonUtil.getNoMarkEditText(et_cu_kind) + "\",\n" +
                        " \"cu_district\":\"" + CommonUtil.getNoMarkEditText(et_cu_district) + "\",\n" +
                        " \"cu_auditstatus\":\"" + "在录入" + "\",\n" +
                        " \"cu_auditdate\":\"" + "" + "\",\n" +
                        " \"cu_auditstatuscode\":\"" + "ENTERING" + "\",\n" +
                        " \"cu_sellercode\":\"" + et_cu_sellercode + "\",\n" +
                        " \"cu_payments\":\"" + CommonUtil.getNoMarkEditText(et_cu_payments) + "\",\n" +
                        " \"cu_paymentscode\":\"" + pa_code + "\",\n" +
                        " \"cu_sellername\":\"" + CommonUtil.getNoMarkEditText(et_cu_sellername) + "\",\n" +
                        " \"cu_contact\":\"" + CommonUtil.getNoMarkEditText(tv_cu_contact) + "\",\n" +
                        " \"cu_degree\":\"" + CommonUtil.getNoMarkEditText(tv_cu_degree) + "\",\n" +
                        " \"cu_mobile\":\"" + CommonUtil.getNoMarkEditText(tv_cu_mobile) + "\",\n" +
                        " \"cu_email\":\"" + CommonUtil.getNoMarkEditText(tv_cu_email) + "\",\n" +
                        " \"cu_businesscode\":\"" + CommonUtil.getNoMarkEditText(tv_cu_businesscode) + "\",\n" +
                        " \"cu_currency\":\"" + CommonUtil.getNoMarkEditText(tv_cu_currency) + "\",\n" +
                        " \"cu_taxrate\":\"" + CommonUtil.getNoMarkEditText(tv_cu_taxrate) + "\",\n" +
                        " \"cu_nichestep\":\"" + CommonUtil.getNoMarkEditText(tv_cu_nichestep) + "\",\n" +
                        " \"cu_remark\":\"" + CommonUtil.getNoMarkEditText(tv_cu_remark) + "\"\n" +
                        "}";
            } else {
                return;
            }
        } else {
            if (et_cu_code.testValidity() && et_cu_name.testValidity()
                    && tv_cu_address.testValidity() && et_cu_kind.testValidity()
                    && et_cu_district.testValidity() && et_cu_kind.testValidity()
                    && et_cu_payments.testValidity() && et_cu_sellername.testValidity()
                    && tv_cu_contact.testValidity() && tv_cu_degree.testValidity()
                    && tv_cu_mobile.testValidity() && tv_cu_email.testValidity()
                    ) {
                formStore = "{\n" +
                        " \"cu_id\":" + mCuId + ",\n" +
                        " \"cu_code\":\"" + CommonUtil.getNoMarkEditText(et_cu_code) + "\",\n" +
                        " \"cu_name\":\"" + CommonUtil.getNoMarkEditText(et_cu_name) + "\",\n" +
                        " \"cu_shortname\":\"" + CommonUtil.getNoMarkEditText(et_cu_shortname) + "\",\n" +
                        " \"cu_add1\":\"" + CommonUtil.getNoMarkEditText(tv_cu_address) + "\",\n" +
                        " \"cu_kind\":\"" + CommonUtil.getNoMarkEditText(et_cu_kind) + "\",\n" +
                        " \"cu_district\":\"" + CommonUtil.getNoMarkEditText(et_cu_district) + "\",\n" +
                        " \"cu_payments\":\"" +CommonUtil.getNoMarkEditText(et_cu_payments) + "\",\n" +
                        " \"cu_paymentscode\":\"" + pa_code + "\",\n" +
                        " \"cu_sellername\":\"" + CommonUtil.getNoMarkEditText(et_cu_sellername) + "\",\n" +
                        " \"cu_contact\":\"" + CommonUtil.getNoMarkEditText(tv_cu_contact) + "\",\n" +
                        " \"cu_degree\":\"" + CommonUtil.getNoMarkEditText(tv_cu_degree) + "\",\n" +
                        " \"cu_mobile\":\"" + CommonUtil.getNoMarkEditText(tv_cu_mobile) + "\",\n" +
                        " \"cu_email\":\"" + CommonUtil.getNoMarkEditText(tv_cu_email) + "\",\n" +
                        " \"cu_remark\":\"" + CommonUtil.getNoMarkEditText(tv_cu_remark) + "\",\n" +
                        " \"cu_sellercode\":\"" + et_cu_sellercode + "\",\n" +
                        " \"cu_recordman\":\"" + emname + "\",\n" +
                        " \"cu_recorddate\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +
                        " \"cu_auditstatus\":\"" + "在录入" + "\",\n" +
                        " \"cu_auditstatuscode\":\"" + "ENTERING" + "\",\n" +
                        " \"cu_status\":" + "\"长期\"" + ",\n" +
                        "}";
            } else {
                return;
            }

        }
        LogUtil.prinlnLongMsg("result", formStore);
    }

    private final int CURRENT_STAGE = 21;
    private final int CUSTOMTER_TYPE = 24;
    private final int PAY_METHODS = 22;
    private final int GET_CURRENCY = 23;
    private final int REQUEST_CLERK = 1;
    private final int GET_CU_ID = 25;
    private final int FORM_TYPE = 26;
    private final int INITCUSTOMER = 18;
    private JSONArray array;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(getString(R.string.common_save_success));

                    setdatas();

                    bt_uncommit.setVisibility(View.GONE);
                    if (type == 0) {
                        bt_commit.setVisibility(View.GONE);
                    } else if (type == 1) {
                        bt_commit.setVisibility(View.VISIBLE);
                    }
                    bt_save.setVisibility(View.GONE);
                    mMenu.findItem(R.id.btn_save).setTitle(getString(R.string.common_update_button));
                    mMenu.findItem(R.id.btn_save).setVisible(true);

                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(msg.getData().getString("result"));
                    break;
                case INITCUSTOMER:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    com.alibaba.fastjson.JSONObject jsonArray = JSON.parseObject(msg.getData().getString("result")).getJSONObject("customer");
                    if (jsonArray != null) {
                        setDatas(jsonArray);

                        et_cu_code.setText(cu_code);
                        et_cu_name.setText(mCompanyName);
                        et_cu_shortname.setText(mShortName);
                        tv_cu_address.setText(mCompanyAddress);
                        et_cu_kind.setText(mCuKind);
                        et_cu_district.setText(mCuDistrict);
                        et_cu_payments.setText(mPayments);
                        et_cu_sellername.setText(mSellerMan);
                        tv_cu_contact.setText(mContact);
                        tv_cu_degree.setText(mPosition);
                        tv_cu_mobile.setText(mTelephone);
                        tv_cu_email.setText(mEmail);
                        tv_cu_remark.setText(mRemark);
                        tv_cu_businesscode.setText(mBusinessCode);
                        tv_cu_currency.setText(mCurrency);
                        tv_cu_taxrate.setText(mTaxrate);
                        tv_cu_nichestep.setText(mNicheStep);

                        et_cu_sellercode = jsonArray.getString("cu_sellercode");
                        pa_code = jsonArray.getString("cu_paymentscode");
                        mCuId = jsonArray.getString("cu_id");
                        bt_save.setVisibility(View.GONE);

                        if (type == 1) {
                            if (!StringUtil.isEmpty(cu_status)) {
                                if (cu_status.equals("已提交")) {
                                    bt_commit.setVisibility(View.GONE);
                                    bt_uncommit.setVisibility(View.VISIBLE);
                                } else {
                                    bt_uncommit.setVisibility(View.GONE);
                                    bt_commit.setVisibility(View.VISIBLE);
                                }
                            }


                        } else if (type == 0) {
                            bt_commit.setVisibility(View.GONE);
                        }
                    }
                    break;
                case FORM_TYPE:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    int falg = Integer.valueOf(JSON.parseObject(msg.getData().getString("result")).getString("isStart"));
                    if (falg == 0) {
                        ll_infomal.setVisibility(View.VISIBLE);
                        type = 1;//正式客户
                     setTitle(getString(R.string.formal_customer));

                        if (StringUtil.isEmpty(cu_code)) {
                            CommonUtil.getCommonId(ct, "PRECUSTOMER_SEQ", mHandler, GET_CU_ID);
                        }
                    } else {
                        ll_infomal.setVisibility(View.GONE);
                        type = 0;//客户预录入
                        setTitle(getString(R.string.client_yu_luru));

                        if (StringUtil.isEmpty(cu_code)) {
                            CommonUtil.getCommonId(ct, "CUSTOMER_SEQ", mHandler, GET_CU_ID);
                        }
                        bt_commit.setVisibility(View.GONE);
                        // bt_save.setVisibility(View.GONE);
                        bt_uncommit.setVisibility(View.GONE);
                        bt_uncommit.setVisibility(View.GONE);
                    }
                    break;
                case codeWhat://获取编号
                    progressDialog.dismiss();
                    Log.d("code", msg.getData().getString("result"));
                    tv_code = JSON.parseObject(msg.getData().getString("result")).getString("code");
                    et_cu_code.setText(tv_code);
                    break;
                case CURRENT_STAGE://获取阶段列表
                    progressDialog.dismiss();
                    mLists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String json = msg.getData().getString("result");
                    array = JSON.parseObject(json).getJSONArray("stages");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            mLists.add(array.getJSONObject(i).getString("BS_NAME"));
                        }
                    }
                    if (mLists.isEmpty()) {
                        mLists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(tv_cu_nichestep, getString(R.string.business_stage));
                    break;
                case PAY_METHODS://获取收款方式列表
                    progressDialog.dismiss();
                    mLists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    json = msg.getData().getString("result");
                    array = JSON.parseObject(json).getJSONArray("datas");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            mLists.add(array.getJSONObject(i).getString("pa_name"));
                        }
                    }

                    if (mLists.isEmpty()) {
                        mLists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(et_cu_payments, getString(R.string.client_payment_method));
                    break;
                case GET_CURRENCY://获取币别列表
                    progressDialog.dismiss();
                    mLists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    json = msg.getData().getString("result");
                    array = JSON.parseObject(json).getJSONArray("datas");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            mLists.add(array.getJSONObject(i).getString("cr_name"));
                        }
                    }

                    if (mLists.isEmpty()) {
                        mLists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(tv_cu_currency, getString(R.string.client_Currency));
                    break;
                case GET_CU_ID:
                    json = msg.getData().getString("result");
                    try {
                        JSONObject resultJsonObject = new JSONObject(json);
                        if (resultJsonObject != null && resultJsonObject.getBoolean("success")) {
                            mCuId = resultJsonObject.getString("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case CUSTOMTER_TYPE:
                    progressDialog.dismiss();
                    json = msg.getData().getString("result");
                    Log.i(TAG, "handleMessage:" + json);
                    mLists.clear();
                    array = JSON.parseObject(json).getJSONArray("datas");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            mLists.add(array.getJSONObject(i).getString("ck_kind"));
                        }
                    }

                    if (mLists.isEmpty()) {
                        mLists.add(getString(R.string.no_));
                    }
                    showSimpleDialog(et_cu_kind, getString(R.string.client_type));
                    break;
                case UPDATE:
                    progressDialog.dismiss();
                    setdatas();
                    ToastMessage(getString(R.string.common_update_button));
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    break;
                case COMMIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(getString(R.string.submit_success));
                    bt_uncommit.setVisibility(View.VISIBLE);
                    bt_commit.setVisibility(View.GONE);
                    // mMenu.findItem(R.id.btn_save).setTitle("更新");
                    mMenu.findItem(R.id.btn_save).setVisible(false);
                    //bt_save.setVisibility(View.GONE);
                    disableEdit(false);
                    setResult(RESULT_CUSTOMER_LIST);
                    break;
                case UNCOMMIT:
                    progressDialog.dismiss();
                    ToastMessage(getString(R.string.Operation_succeeded));
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    bt_uncommit.setVisibility(View.GONE);
                    bt_commit.setVisibility(View.VISIBLE);
                    mMenu.findItem(R.id.btn_save).setTitle(getString(R.string.common_update_button));
                    mMenu.findItem(R.id.btn_save).setVisible(true);
                    //bt_save.setVisibility(View.GONE);
                    disableEdit(true);
                    setResult(RESULT_CUSTOMER_LIST);
                    break;
            }
        }
    };

    private void setdatas() {
        cu_code = getEditString(et_cu_code);
        mCompanyName = getEditString(et_cu_name);
        mShortName = getEditString(et_cu_shortname);
        mCompanyAddress = getEditString(tv_cu_address);
        mCuKind = getEditString(et_cu_kind);
        mCuDistrict = getEditString(et_cu_district);
        mPayments = getEditString(et_cu_payments);
        mSellerMan = getEditString(et_cu_sellername);
        mContact = getEditString(tv_cu_contact);
        mPosition = getEditString(tv_cu_degree);
        mTelephone = getEditString(tv_cu_mobile);
        mEmail = getEditString(tv_cu_email);
        mRemark = getEditString(tv_cu_remark);
        mBusinessCode = getEditString(tv_cu_businesscode);
        mCurrency = getEditString(tv_cu_currency);
        mTaxrate = getEditString(tv_cu_taxrate);
        mNicheStep = getEditString(tv_cu_nichestep);
    }

    private void setDatas(com.alibaba.fastjson.JSONObject jsonArray) {
        //cu_code = jsonArray.getString("cu_code") == null ? "" : jsonArray.getString("cu_code").trim();
        mCompanyName = jsonArray.getString("cu_name") == null ? "" : jsonArray.getString("cu_name").trim();
        mShortName = jsonArray.getString("cu_shortname") == null ? "" : jsonArray.getString("cu_shortname").trim();
        mCompanyAddress = jsonArray.getString("cu_add1") == null ? "" : jsonArray.getString("cu_add1").trim();
        mCuKind = jsonArray.getString("cu_kind") == null ? "" : jsonArray.getString("cu_kind").trim();
        mCuDistrict = jsonArray.getString("cu_district") == null ? "" : jsonArray.getString("cu_district").trim();
        mPayments = jsonArray.getString("cu_payments") == null ? "" : jsonArray.getString("cu_payments").trim();
        mSellerMan = jsonArray.getString("cu_sellername") == null ? "" : jsonArray.getString("cu_sellername").trim();
        mContact = jsonArray.getString("cu_contact") == null ? "" : jsonArray.getString("cu_contact").trim();
        mPosition = jsonArray.getString("cu_degree") == null ? "" : jsonArray.getString("cu_degree").trim();
        mTelephone = jsonArray.getString("cu_mobile") == null ? "" : jsonArray.getString("cu_mobile").trim();
        mEmail = jsonArray.getString("cu_email") == null ? "" : jsonArray.getString("cu_email").trim();
        mRemark = jsonArray.getString("cu_remark") == null ? "" : jsonArray.getString("cu_remark").trim();
        mBusinessCode = jsonArray.getString("cu_businesscode") == null ? "" : jsonArray.getString("cu_businesscode").trim();
        mCurrency = jsonArray.getString("cu_currency") == null ? "" : jsonArray.getString("cu_currency").trim();
        mTaxrate = jsonArray.getString("cu_taxrate") == null ? "" : jsonArray.getString("cu_taxrate").trim();
        mNicheStep = jsonArray.getString("cu_nichestep") == null ? "" : jsonArray.getString("cu_nichestep").trim();
    }

    private String getEditString(FormEditText formEditText) {
        return formEditText.getText().toString().trim() == null ? "" : formEditText.getText().toString().trim();
    }

    private void jumpToStateActivity() {
        Intent intent = new Intent();
        intent.setClass(CustomerAddActivity.this, BusinessStateActivity.class);
        startActivity(intent);
    }

    private SingleDialog singleDialog;

    public void showSimpleDialog(final FormEditText et, String title) {
        if (singleDialog != null) {
            if (singleDialog.isShowing())
                return;
        }
        singleDialog = new SingleDialog(ct, title,
                new SingleDialog.PickDialogListener() {
                    @Override
                    public void onListItemClick(int position, String value) {
                        if (et.getId() == R.id.et_cu_payments){
                            for (int i = 0; i < array.size(); i++) {
                                if (value.equals(array.getJSONObject(i).getString("pa_name"))) {
                                    pa_code = array.getJSONObject(i).getString("pa_code");
                                }
                            }
                            et.setText(value);
                        }else {
                            et.setText(value);
                        }
                    }
                });
        singleDialog.show();
        singleDialog.initViewData(mLists);
       /* } else {
            singleDialog.show();
            singleDialog.initViewData(lists);
        }*/
    }

    private void sendHttpResquest(int what, String formStore) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "scm/sale/savePreCustomer.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        if (type == 1) {
            url = CommonUtil.getAppBaseUrl(ct) + "scm/sale/saveCustomerBase.action";
            params.put("caller", "Customer!Base");
        } else {
            params.put("caller", "PreCustomer");
        }
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


    private static final int codeWhat = 0x11;


    private void getCodeByNet() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
        final Map<String, Object> param = new HashMap<>();
        String caller = "PreCustomer";
        if (type == 1) {
            caller = "Customer";
        }
        param.put("caller", caller);
        param.put("type", 2);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, codeWhat, null, null, "post");
    }

    public void onPopupButtonClick(View button) {

        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("reid", R.style.OAThemeMeet);
        List<SelectBean> beanList = new ArrayList<>();

        //华东地区，华南地区，华北地区，华中地区，西南地区，西北地区，东北地区，港澳台地区，海外地区，其它地区
        SelectBean ben = new SelectBean();
        ben.setName(getString(R.string.client_Huadong_Region));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_Huanan_Region));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_Huabei_Region));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_Huazhong_Region));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_Southwest_Region));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_North_west_region));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_North_east_area));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_HongKong_Macao_and_Taiwan_regions));
        ben.setJson("{}");
        beanList.add(ben);

        ben = new SelectBean();
        ben.setName(getString(R.string.client_other_areas));
        ben.setJson("{}");
        beanList.add(ben);

        intent.putExtra("title", getString(R.string.client_payment_method));
        intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) beanList);
        startActivityForResult(intent, 0x27);

//        popup = new PopupMenu(this, button);
//        switch (button.getId()) {
//            case R.id.et_cu_kind:
//                getMenuInflater().inflate(R.menu.menu_cu_kind, popup.getMenu());
//                break;
//            case R.id.et_cu_district:
//                 getMenuInflater().inflate(R.menu.menu_cu_district, popup.getMenu());
//                break;
//        }
//
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.cu_desgin:
//                        et_cu_kind.setText(item.getTitle());
//                        break;
//                    case R.id.cu_project:
//                        et_cu_kind.setText(item.getTitle());
//                        break;
//                    case R.id.cu_proxy:
//                        et_cu_kind.setText(item.getTitle());
//                        break;
//                    case R.id.cu_sale:
//                        et_cu_kind.setText(item.getTitle());
//                        break;
//                    case R.id.cu_eastChina:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_southChina:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_northChina:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_center:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_southwest:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_northwest:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_northeast:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_webPages:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_seas:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                    case R.id.cu_other:
//                        et_cu_district.setText(item.getTitle());
//                        break;
//                }
//                return true;
//            }
//        });
//        popup.show(); //showing popup menu 
    }

    /**
     * @desc:加载阶段
     */
    public void loadJieDuan() {
//        progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceStage.action";
//        Map<String, Object> params = new HashMap<>();
//        params.put("condition", "1=1");
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, CURRENT_STAGE, null, null, "post");

        HashMap params = new HashMap<>();
        params.put("condition", "1=1");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", params);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "stages");
        intent.putExtra("showKey", "BS_NAME");
        intent.putExtra("action", "/mobile/crm/getBusinessChanceStage.action");
        intent.putExtra("title", getString(R.string.business_stage));
        startActivityForResult(intent, 0x24);
    }

    public void loadCustomerType() {
//        progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getDbfind.action";
//        Map<String, Object> params = new HashMap<>();
//        params.put("which", "form");
//        params.put("caller", "Customer!Base");
//        params.put("field", "cu_kind");
//        params.put("pageSize", 1000);
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, CUSTOMTER_TYPE, null, null, "post");

        HashMap param = new HashMap<>();
        param.put("which", "form");
        param.put("caller", "Customer!Base");
        param.put("field", "cu_kind");
        param.put("pageSize", 1000);
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "datas");
        intent.putExtra("showKey", "ck_kind");
        intent.putExtra("action", "mobile/common/getDbfind.action");
        intent.putExtra("title", getString(R.string.client_type));
        startActivityForResult(intent, 0x21);

    }

    /**
     * 加载收款方式
     */
    public void loadPayments() {
        //       progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getDbfind.action";
//        Map<String, Object> params = new HashMap<>();
//        params.put("which", "form");
//        params.put("caller", "Customer!base");
//        params.put("field", "cu_paymentscode");
//        params.put("pageSize", 1000);
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, PAY_METHODS, null, null, "post");


        HashMap params = new HashMap<>();
        params.put("which", "form");
        params.put("caller", "Customer!base");
        params.put("field", "cu_paymentscode");
        params.put("pageSize", 1000);
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", params);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "datas");
        intent.putExtra("showKey", "pa_name");
        intent.putExtra("action", "mobile/common/getDbfind.action");
        intent.putExtra("title", getString(R.string.client_payment_method));
        startActivityForResult(intent, 0x22);
    }

    /**
     * 加载币别
     */
    public void loadCurrency() {
//        progressDialog.show();
//        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getDbfind.action";
//        Map<String, Object> params = new HashMap<>();
//        params.put("which", "form");
//        params.put("caller", "Customer!base");
//        params.put("field", "cu_currency");
//        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
//        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
//        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, GET_CURRENCY, null, null, "post");

        HashMap params = new HashMap<>();
        params.put("which", "form");
        params.put("caller", "Customer!base");
        params.put("field", "cu_currency");
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", params);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "datas");
        intent.putExtra("showKey", "cr_name");
        intent.putExtra("action", "mobile/common/getDbfind.action");
        intent.putExtra("title", getString(R.string.client_payment_method));
        startActivityForResult(intent, 0x23);

    }

    private void loadParams(int what) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/ifuseprecustomer.action";
        Map<String, Object> params = new HashMap<>();
        params.put("currentsystem=", CommonUtil.getSharedPreferences(ct, "erp_master"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0x20://单选
                if (data == null) return;
                SelectBean b = data.getParcelableExtra("data");
                LogUtil.d(JSON.toJSONString(b));
                if (b == null || StringUtil.isEmpty(b.getJson())) return;
                switch (requestCode) {
                    case 0x21:
                        et_cu_kind.setText(b.getName());
                        break;
                    case 0x22:
                        et_cu_payments.setText(b.getName());
                        pa_code = JSON.parseObject(b.getJson()).getString("pa_code");
                        break;
                    case 0x23:
                        tv_cu_currency.setText(b.getName());
                        break;
                    case 0x24:
                        tv_cu_nichestep.setText(b.getName());
                        break;
                    case 0x27:
                        et_cu_district.setText(b.getName());
                        break;
                    case 0x25:
                        tv_cu_nichestep.setText(b.getName());
                        break;
                }
                break;
            case REQUEST_CLERK:
                if (resultCode == 2 && data != null) {
                    et_cu_sellername.setText(data.getStringExtra("en_name"));
                    et_cu_sellercode = data.getStringExtra("en_code");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.et_cu_kind){
            loadCustomerType();
        }else if (v.getId() == R.id.et_cu_district){
            onPopupButtonClick(v);
        }else if (v.getId() == R.id.tv_cu_nichestep){
            loadJieDuan();
        }else if (v.getId() ==  R.id.et_cu_payments){
            loadPayments();
        }else if (v.getId() == R.id.tv_cu_currency){
            loadCurrency();
        }else if (v.getId() ==  R.id.et_cu_sellername){
            Intent intent = new Intent();
            intent.setClass(this, DbfindListActivity.class);
            startActivityForResult(intent, REQUEST_CLERK);
        }else if (v.getId() == R.id.bt_save){
            saveData();
        }else if (v.getId() == R.id.bt_commit){
            if (isStrEquals(cu_code, et_cu_code) && isStrEquals(mCompanyName, et_cu_name)
                    && isStrEquals(mShortName, et_cu_shortname) && isStrEquals(mCompanyAddress, tv_cu_address)
                    && isStrEquals(mCuKind, et_cu_kind) && isStrEquals(mCuDistrict, et_cu_district)
                    && isStrEquals(mPayments, et_cu_payments) && isStrEquals(mSellerMan, et_cu_sellername)
                    && isStrEquals(mContact, tv_cu_contact) && isStrEquals(mPosition, tv_cu_degree)
                    && isStrEquals(mTelephone, tv_cu_mobile) && isStrEquals(mEmail, tv_cu_email)
                    && isStrEquals(mBusinessCode, tv_cu_businesscode) && isStrEquals(mCurrency, tv_cu_currency)
                    && isStrEquals(mTaxrate, tv_cu_taxrate) && isStrEquals(mNicheStep, tv_cu_nichestep)
                    && isStrEquals(mRemark, tv_cu_remark)) {
                commit(COMMIT, mCuId);
            } else {
                new AlertDialog.Builder(this).setTitle(getString(R.string.common_notice))
                        .setMessage(getString(R.string.CRM_current_page_change_ecet))
                        .setPositiveButton(getString(R.string.common_sure), null).show();
            }
        }else if (v.getId() == R.id.bt_uncommit){
            unCommit(UNCOMMIT, mCuId);
        }
    }


    private boolean isStrEquals(String s, FormEditText editText) {
        String et = editText.getText().toString().trim();
        if (et == null) {
            et = "";
        }
        return s.equals(et);
    }

    /**
     * @desc:初始化客户数据---未审核状态的数据
     * @author：Arison on 2016/9/20
     */
    public void initCustomerData(int what, String code) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getCustomerbycode.action";
        Map<String, Object> params = new HashMap<>();
        params.put("cu_code", code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");

    }

    /**
     * @desc:提交之后，出现反提交按钮；更新按钮隐藏
     * @author：Arison on 2016/9/20
     */
    public void commit(int what, String id) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "scm/sale/submitCustomerBase.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "Customer!Base");
        params.put("id", id);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    public void unCommit(int what, String id) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "scm/sale/resSubmitCustomerBase.action";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("caller", "Customer!Base");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    public void update(int what, String formStore) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "scm/sale/updateCustomerBase.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "Customer!Base");
        params.put("formStore", formStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    //应接口要求更新商机
//    private void updateBusiness(int bc_id, com.alibaba.fastjson.JSONObject object) {
//        String url = "crm/chance/updateBusinessChance.action";
//        Bundle bundle = new Bundle();
//        bundle.putString("BS_NAME", JSONUtil.getText(object,"bc_custname"));
//        bundle.putString("bc_nichehouse", JSONUtil.getText(object,""));
//        bundle.putInt("bc_id", bc_id);
//        bundle.putString("bc_code", bc_code);
//        Map<String, Object> param = new HashMap<>();
//        param.put("caller", "BusinessChance");
//        param.put("formStore", JSONUtil.map2JSON(getFormStore(bundle)));
//        Request request = new Request.Bulider()
//                .setParam(param)
//                .setUrl(url)
//                .bulid();
//        OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
//            @Override
//            public void result(int what, boolean isJSON, String message, Bundle bundle) {
//
//            }
//
//            @Override
//            public void error(int what, String message, Bundle bundle) {
//
//            }
//        });
//    }

//    public Map<String, Object> getFormStore(Bundle bundle) {
//        String bc_contact = "";
//        String bc_tel = "";
//        String bc_currentprocess = "";
//        String bc_code = "";
//        String bc_nichehouse = "";
//        int bc_id = 0;
//        if (bundle != null) {
//            bc_id = bundle.getInt("bc_id");
//            bc_code = bundle.getString("bc_code");
//            bc_currentprocess = bundle.getString("BS_NAME");
//            bc_nichehouse = bundle.getString("bc_nichehouse");
//        }
//        if (adapter != null && ListUtils.getSize(adapter.getContacts()) > 0) {
//            saveContact(bc_code, adapter.getContacts());
//            EmployeesModel contact = adapter.getContacts().get(0);
//            bc_contact = contact.getEmployeeNames();
//            bc_tel = contact.getEmployeecode();
//        }
//        String name = CommonUtil.getName();
//        String company = company_tv.getText().toString();
//        String address = company_add_tv.getText().toString();
//        String time = TimeUtils.f_long_2_str(System.currentTimeMillis());
//        Map<String, Object> formStore = new HashMap<>();
//        formStore.put("bc_id", bc_id);
//        formStore.put("bc_code", bc_code);
////        formStore.put("bc_custcode", "");
//        formStore.put("bc_doman", name);
//        formStore.put("bc_domancode", CommonUtil.getEmcode());
//        formStore.put("bc_statuscode", "AUDITED");
//        formStore.put("bc_status", getString(R.string.status_approved));
//        formStore.put("bc_recorder", name);
//        formStore.put("bc_recorddate", time);
//        formStore.put("bc_lastdate", time);
//        formStore.put("bc_description", company);
//        formStore.put("bc_custname", company);
//        formStore.put("bc_address", address);
//        formStore.put("bc_desc6", address);
//        if (latLng != null) {
//            formStore.put("bc_longitude", latLng.longitude);
//            formStore.put("bc_latitude", latLng.latitude);
//        }
//        formStore.put("bc_contact", bc_contact);
//        formStore.put("bc_tel", bc_tel);
//        formStore.put("bc_currentprocess", bc_currentprocess);
//        formStore.put("bc_remark", StringUtil.toHttpString(remark_tv.getText().toString()));
//        formStore.put("bc_date7", DateFormatUtil.long2Str(DateFormatUtil.YMD));
//        formStore.put("bc_desc8", "");//成本金额
//        formStore.put("bc_desc4", "");//部门
//        formStore.put("bc_desc5", "");//岗位
//        formStore.put("bc_from", "");// 商机来源
//        formStore.put("bc_nichehouse", StringUtil.isEmpty(bc_nichehouse) ? "" : bc_nichehouse);//商机库
//        return formStore;
//    }

    public void disableEdit(boolean falg) {
        et_cu_code.setEnabled(falg);
        et_cu_name.setEnabled(falg);
        et_cu_shortname.setEnabled(falg);
        tv_cu_address.setEnabled(falg);
        et_cu_kind.setEnabled(falg);
        et_cu_district.setEnabled(falg);
        et_cu_payments.setEnabled(falg);
        et_cu_sellername.setEnabled(falg);
        tv_cu_contact.setEnabled(falg);
        tv_cu_degree.setEnabled(falg);
        tv_cu_mobile.setEnabled(falg);
        tv_cu_email.setEnabled(falg);
        tv_cu_remark.setEnabled(falg);
        tv_cu_businesscode.setEnabled(falg);
        tv_cu_currency.setEnabled(falg);
        tv_cu_taxrate.setEnabled(falg);
        tv_cu_nichestep.setEnabled(falg);
    }


}
