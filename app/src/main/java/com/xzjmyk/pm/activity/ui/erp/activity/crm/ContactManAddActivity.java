package com.xzjmyk.pm.activity.ui.erp.activity.crm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.time.wheel.DatePicker;
import com.core.utils.time.wheel.OptionPicker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContactManAddActivity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.tv_name)
    private EditText tv_name;
    @ViewInject(R.id.tv_sex)
    private EditText tv_sex;
    @ViewInject(R.id.tv_company_login)
    private EditText tv_company_login;
    @ViewInject(R.id.tv_department_login)
    private EditText tv_department_login;
    @ViewInject(R.id.tv_position_login)
    private EditText tv_position_login;
    @ViewInject(R.id.tv_tel_login)
    private EditText tv_tel_login;
    @ViewInject(R.id.tv_phone_login)
    private EditText tv_phone_login;
    @ViewInject(R.id.tv_email_login)
    private EditText tv_email_login;
    @ViewInject(R.id.tv_address_login)
    private EditText tv_address_login;
    @ViewInject(R.id.tv_birthday_login)
    private EditText tv_birthday_login;
    @ViewInject(R.id.tv_notes_login)
    private EditText tv_notes_login;
    @ViewInject(R.id.tv_card_login)
    private EditText tv_card_login;

    @ViewInject(R.id.sex_erp_rl)
    private RelativeLayout sex_erp_rl;
    @ViewInject(R.id.address_erp_rl)
    private RelativeLayout address_erp_rl;
    @ViewInject(R.id.birthday_erp_rl)
    private RelativeLayout birthday_erp_rl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_man_add);
        initView();
        initData();
        initListener();
    }

    private void initData() {
          getCodeByNet();
    }

    private void initView() {
        ViewUtils.inject(this);
       setTitle("新增联系人");
        tv_birthday_login.setOnKeyListener(null);
        tv_birthday_login.setFocusable(false);
        
        tv_sex.setKeyListener(null);
        sex_erp_rl.setFocusable(false);
    }

    private void initListener() {
       
        birthday_erp_rl.setOnClickListener(this);
        sex_erp_rl.setOnClickListener(this);
        address_erp_rl.setOnClickListener(this);
        tv_birthday_login.setOnClickListener(this);
        tv_sex.setOnClickListener(this);
        tv_address_login.setOnClickListener(this);
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_save:
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

  
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                     ToastMessage("保存成功！");
                    break;
                case codeWhat://获取编号
                    progressDialog.dismiss();
                     tv_code= JSON.parseObject(msg.getData().getString("result")).getString("code");
                    
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage("保存异常！");
                    break;
            }
        }
    };

    private void sendHttpResquest(int what) {
        progressDialog.show();
        String sex="1";
        if (tv_sex.getText().toString().equals("男")){
            sex="2";
        }else{
            sex="1";
        }
        String formStore="{" +
                "\"ct_code\":\"" + tv_code + "\"," +
                "\"ct_name\":\"" + tv_name.getText().toString() + "\"," +
                "\"ct_sex\":" +
                sex +
                ",\"ct_cuname\":\"" +
                tv_company_login.getText().toString() +
                "\",\"ct_dept\":\"" +
                tv_department_login.getText().toString() +
                "\",\"ct_position\":\"" +
               tv_position_login.getText().toString() +
                "\",\"ct_officephone\":\"" +
                tv_tel_login.getText().toString() +
                "\",\"ct_mobile\":\"" +
                tv_phone_login.getText().toString()+
                "\",\"ct_personemail\":\"" +
               tv_email_login.getText().toString() +
                "\",\"ct_address\":\"" +
                tv_address_login.getText().toString() +
                "\",\"ct_birthday\":\"" +
                tv_birthday_login.getText().toString() +
                "\",\"ct_reamrk\":\"" +
                tv_notes_login.getText().toString() +
                "\",\"ct_attach\":\"" +
                tv_card_login.getText().toString() +
                "\"}";
        String url = CommonUtil.getAppBaseUrl(ct) + "crm/customermgr/saveContact.action";
        Map<String, Object> params = new HashMap<>();
        params.put("formStore", formStore);
        params.put("caller", "Contact");
        params.put("param", "[]");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.birthday_erp_rl:
                showBirthdayDialog();
                break;
            case R.id.tv_birthday_login:
                showBirthdayDialog();
                break;
            case R.id.sex_erp_rl:
                showSexDialog();
                break;
            case R.id.tv_sex:
                showSexDialog();
                break;
            case R.id.address_erp_rl:

                break;
        }
    }

    private void showSexDialog() {
        OptionPicker sex_option = new OptionPicker(this, new String[]{
                "男", "女"
        });
        sex_option.setOffset(1);
        sex_option.setSelectedIndex(0);
        sex_option.setTextSize(18);
        sex_option.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(int position, String option) {
//                ToastMessage(position+option);
                tv_sex.setText(option);
            }
            
        });
        sex_option.show();
    }

    private void showBirthdayDialog() {
        DatePicker picker = new DatePicker(this);
        picker.setRange(1950, 2030);
        picker.setSelectedItem(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
            @Override
            public void onDatePicked(String year, String month, String day) {
//                        ToastUtil.showToast(ct, year + "-" + month + "-" + day);
                tv_birthday_login.setText(year + "-" + month + "-" + day);
            }
        });
        picker.show();
    }

    private static final int codeWhat = 0x11;
    private  String tv_code;
    private void getCodeByNet() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
        final Map<String, Object> param = new HashMap<>();
        String caller = "Contact";
        param.put("caller", caller);
        param.put("type", 2);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
       ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, codeWhat, null, null, "post");
    }
}
