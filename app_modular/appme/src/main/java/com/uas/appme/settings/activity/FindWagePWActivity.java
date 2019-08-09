package com.uas.appme.settings.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;

/**
 * Created by FANGlh on 2017/12/11.
 * function:
 */

public class FindWagePWActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mStep1Ll;
    private EditText mPhoneEt;
    private TextView mClearphoneTv;
    private Button mNext1Btn;
    private LinearLayout mStep2Ll;
    private TextView mTelTv;
    private EditText mCodeEt;
    private Button mNext2Btn;
    private LinearLayout mStep3Ll;
    private EditText mSetpwEt;
    private ImageView mEyeTv;
    private Button mNext3Btn;
    private Boolean canSeePW  = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wage_forgetps);
        initView();
        initEvents();
    }

    private void initEvents() {
        mSetpwEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();
                if (!StringUtil.isEmpty(str))
                    mEyeTv.setVisibility(View.VISIBLE);
                else
                    mEyeTv.setVisibility(View.GONE);

            }
        });
    }

    private void initView() {

        mStep1Ll = (LinearLayout) findViewById(R.id.step1_ll);
        mPhoneEt = (EditText) findViewById(R.id.phone_et);
        mPhoneEt.setText(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"user_phone")+"");
        mClearphoneTv = (TextView) findViewById(R.id.clearphone_tv);
        mNext1Btn = (Button) findViewById(R.id.next1_btn);
        mStep2Ll = (LinearLayout) findViewById(R.id.step2_ll);
        mTelTv = (TextView) findViewById(R.id.tel_tv);
        mCodeEt = (EditText) findViewById(R.id.code_et);
        mNext2Btn = (Button) findViewById(R.id.next2_btn);
        mStep3Ll = (LinearLayout) findViewById(R.id.step3_ll);
        mSetpwEt = (EditText) findViewById(R.id.setpw_et);
        mEyeTv = (ImageView) findViewById(R.id.eye_tv);
        mNext3Btn = (Button) findViewById(R.id.next3_btn);

        mNext1Btn.setOnClickListener(this);
        mNext2Btn.setOnClickListener(this);
        mNext3Btn.setOnClickListener(this);
        mEyeTv.setOnClickListener(this);
        mClearphoneTv.setOnClickListener(this);
        mPhoneEt.setFocusable(false);
        mPhoneEt.setKeyListener(null);

        showStepView(1);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        if (id == R.id.next1_btn){
            if (StringUtil.isEmpty(mPhoneEt.getText().toString())){
                ToastMessage("请输入手机号");
                return;
            }
            mTelTv.setText(mPhoneEt.getText().toString());
            HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//            HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
            httpClient.Api().send(new HttpClient.Builder()
                    .url("mobile/salary/verificationCode.action")
                    .header("Cookie","JSESSIONID="+CommonUtil.getSharedPreferences(BaseConfig.getContext(), "sessionId"))
                    .add("phone",mPhoneEt.getText().toString())
                    .method(Method.POST)
                    .build(),new ResultSubscriber<>(new ResultListener<Object>() {
                @Override
                public void onResponse(Object o) {
                    LogUtil.prinlnLongMsg("verificationCode", o.toString()+"");
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")){
                        Toast.makeText(ct,getString(R.string.msg_send_success),Toast.LENGTH_LONG).show();
//                        mCodeEt.setText(JSON.parseObject(o.toString()).getString("vecode")+"");
                        showStepView(2);
                    }

                }
            }));
        }else if (id == R.id.next2_btn){
            if (StringUtil.isEmpty(mCodeEt.getText().toString())){
                ToastMessage(getString(R.string.input_search_ycode));
                return;
            }
            showStepView(3);
        }else if (id == R.id.next3_btn){
            if (StringUtil.isEmpty(mSetpwEt.getText().toString())){
                ToastMessage(getString(R.string.please_input_new_password));
                return;
            }
            if (mSetpwEt.getText().toString().length()<6){
                ToastMessage(getString(R.string.please_settting_password));
                return;
            }
                doChangePW();
        }else if (id == R.id.eye_tv){
            if (canSeePW){
                canSeePW = false;
                mSetpwEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mEyeTv.setImageResource(R.drawable.icon_unshow);
            }else {
                canSeePW = true;
                mSetpwEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                mEyeTv.setImageResource(R.drawable.icon_show);
            }
        }else if (id == R.id.clearphone_tv){
            mPhoneEt.setText("");
        }
    }

    private void doChangePW() {
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/changePassword.action")
                .add("emcode", CommonUtil.getEmcode())
                .add("password",mSetpwEt.getText().toString())
                .add("phone",mPhoneEt.getText().toString())
                .add("em_uu", CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("master",CommonUtil.getSharedPreferences(ct, "erp_master"))
//                .add("master","uas_dev")
                .header("Cookie","JSESSIONID="+CommonUtil.getSharedPreferences(BaseConfig.getContext(), "sessionId"))
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("changePassword", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;

                if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")){
                    Toast.makeText(ct, getString(R.string.setting_password_success), Toast.LENGTH_LONG).show();
                    finish();
                }

            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    private void showStepView(int step){
       switch (step){
           case 1:
               mStep1Ll.setVisibility(View.VISIBLE);
               mStep2Ll.setVisibility(View.GONE);
               mStep3Ll.setVisibility(View.GONE);

               break;
           case 2:
               mStep1Ll.setVisibility(View.GONE);
               mStep2Ll.setVisibility(View.VISIBLE);
               mStep3Ll.setVisibility(View.GONE);
               break;
           case 3:
               mStep1Ll.setVisibility(View.GONE);
               mStep2Ll.setVisibility(View.GONE);
               mStep3Ll.setVisibility(View.VISIBLE);
               break;
       }
    }
}
