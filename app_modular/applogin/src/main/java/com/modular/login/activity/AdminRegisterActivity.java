package com.modular.login.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.data.RegexUtil;
import com.common.hmac.Md5Util;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.modular.login.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by RaoMeng on 2017/9/21.
 * 管理员信息注册页面
 */

public class AdminRegisterActivity extends BaseActivity implements View.OnClickListener {
    private final int OBTAIN_VERIFICATION_CODE = 0x01;
    private final int ADMIN_REGISTER = 0x02;
    private final int VERIFICATION_TIME_TASK = 0x03;
    private final int UPLOAD_REGISTER_MESSAGE = 0x04;

    private Button mNextStepButton;
    private ClearEditText mAdminNameEt;
    private ClearEditText mAdminPhoneEt;
    private ClearEditText mAdminEmailEt;
    private ClearEditText mAdminVerificationEt;
    private TextView mAdminObtainCodeTv;
    private ClearEditText mAdminPasswordEt;
    private ImageView mAdminPasswordVisiableIv;
    private TextView mNameErrorTextView, mPhoneErrorTextView, mEmailErrorTextView, mCodeErrorTextView, mPasswordErrorTextView;
    private boolean isPasswordVisiable = false;
    private String mCheckcodeToken, mCompanyName = "", mIndustry = "", mAddress = "", mLatitude = "", mLongitude;
    private boolean isNameAdopt = false, isPhoneAdopt = false, isEmailAdopt = false, isCodeAdopt = false, isPasswordAdopt = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String result = null;
            if (msg.what != VERIFICATION_TIME_TASK) {
                result = msg.getData().getString("result");
                Log.d("adminresponse", result);
            }
            switch (msg.what) {
                case OBTAIN_VERIFICATION_CODE:
                    progressDialog.dismiss();
                    if (result != null) {
                        try {
                            JSONObject resultObject = new JSONObject(result);
                            if (resultObject.optBoolean("success")) {
                                JSONObject contentObject = resultObject.optJSONObject("content");
                                if (contentObject != null) {
                                    mFlag = 60;
                                    mTimer = new Timer();
                                    mTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            Message message = Message.obtain();
                                            message.what = VERIFICATION_TIME_TASK;
                                            mHandler.sendMessage(message);
                                        }
                                    }, 0, 1000);

                                    ToastUtil.showToast(AdminRegisterActivity.this
                                            , getString(R.string.verify_code_have_sent));
                                    mCheckcodeToken = contentObject.optString("token");
                                    CommonUtil.setSharedPreferences(AdminRegisterActivity.this
                                            , Constants.ENTERPRISE_REGISTER_CODE, mCheckcodeToken);
                                } else {
                                    progressDialog.dismiss();
                                    mAdminObtainCodeTv.setEnabled(true);
                                    mAdminObtainCodeTv.setText(R.string.obtain_verify_code);
                                    toast(getString(R.string.obtain_verify_code_failed));
                                }
                            } else {
                                progressDialog.dismiss();
                                String errMsg = resultObject.optString("errMsg");
                                ToastUtil.showToast(AdminRegisterActivity.this
                                        , TextUtils.isEmpty(errMsg) ? getString(R.string.obtain_verify_code_failed) : errMsg);
                                mAdminObtainCodeTv.setEnabled(true);
                                mAdminObtainCodeTv.setText(R.string.obtain_verify_code);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mAdminObtainCodeTv.setEnabled(true);
                        mAdminObtainCodeTv.setText("获取验证码");
                        ToastUtil.showToast(AdminRegisterActivity.this
                                , "验证码获取失败，请重试");
                    }
                    break;
                case ADMIN_REGISTER:
//                    progressDialog.dismiss();
                    if (result != null) {
                        try {
                            JSONObject resultObject = new JSONObject(result);
                            if (resultObject.optBoolean("success")) {
//                                /*Intent intent = new Intent();
//                                intent.setClass(AdminRegisterActivity.this, EnterpriseCompleteActivity.class);
//                                startActivity(intent);
//                                finish();*/
                                JSONObject content = resultObject.optJSONObject("content");
                                String enUU = "";
                                String imid = "";
                                if (content != null) {
                                    enUU = content.optString("enUU");
                                    imid = content.optString("imid");
                                }
                                uploadMsg(enUU, imid);

                            } else {
                                progressDialog.dismiss();
                                String errMsg = resultObject.optString("errMsg");
                                ToastUtil.showToast(AdminRegisterActivity.this, errMsg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case UPLOAD_REGISTER_MESSAGE:
                    progressDialog.dismiss();
                    if (result != null) {
                        try {
                            JSONObject resultObject = new JSONObject(result);
                            if ("true".equals(resultObject.optString("result"))) {
                                Intent intent = new Intent();
                                intent.setClass(AdminRegisterActivity.this, EnterpriseCompleteActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String resultMsg = resultObject.optString("resultMsg");
                                ToastUtil.showToast(AdminRegisterActivity.this, resultMsg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case VERIFICATION_TIME_TASK:
                    if (mFlag > 0) {
                        mFlag--;
                        mAdminObtainCodeTv.setEnabled(false);
                        mAdminObtainCodeTv.setText(mFlag + "秒后重新获取");
                    } else {
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        mAdminObtainCodeTv.setEnabled(true);
                        mAdminObtainCodeTv.setText("获取验证码");
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    if (result != null) {
                        ToastMessage(result);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private int mFlag;
    private Timer mTimer;

    private TextWatcher mNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            nameAdopt(text);
        }
    };

    private TextWatcher mPhoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            phoneAdopt(text);
        }
    };

    private TextWatcher mEmailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            emailAdopt(text);
        }
    };


    private TextWatcher mCodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            codeAdopt(text);
        }
    };


    private TextWatcher mPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            passwordAdopt(text);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);
        setTitle("企业注册(2/2)");

        initViews();

        initEvents();
    }

    private void initViews() {
        mNextStepButton = (Button) findViewById(R.id.admin_register_next_step_btn);
        mAdminNameEt = (ClearEditText) findViewById(R.id.admin_register_name_et);
        mAdminPhoneEt = (ClearEditText) findViewById(R.id.admin_register_phone_et);
        mAdminEmailEt = (ClearEditText) findViewById(R.id.admin_register_email_et);
        mAdminVerificationEt = (ClearEditText) findViewById(R.id.admin_register_verification_et);
        mAdminObtainCodeTv = (TextView) findViewById(R.id.admin_register_obtain_code_tv);
        mAdminPasswordEt = (ClearEditText) findViewById(R.id.admin_register_password_et);
        mAdminPasswordEt.setTypeface(Typeface.DEFAULT);
        mAdminPasswordEt.setTransformationMethod(new PasswordTransformationMethod());
        mAdminPasswordVisiableIv = (ImageView) findViewById(R.id.admin_register_password_visiable_iv);
        mNameErrorTextView = (TextView) findViewById(R.id.admin_register_name_error_tv);
        mPhoneErrorTextView = (TextView) findViewById(R.id.admin_register_phone_error_tv);
        mEmailErrorTextView = (TextView) findViewById(R.id.admin_register_email_error_tv);
        mCodeErrorTextView = (TextView) findViewById(R.id.admin_register_code_error_tv);
        mPasswordErrorTextView = (TextView) findViewById(R.id.admin_register_password_error_tv);

        mCheckcodeToken = CommonUtil.getSharedPreferences(this, Constants.ENTERPRISE_REGISTER_CODE);
        Intent intent = getIntent();
        if (intent != null) {
            mCompanyName = intent.getStringExtra("companyName");
            mIndustry = intent.getStringExtra("industry");
            mAddress = intent.getStringExtra("address");
            mLatitude = intent.getStringExtra("latitude");
            mLongitude = intent.getStringExtra("longitude");
        }
    }

    private void initEvents() {
        mNextStepButton.setOnClickListener(this);
        mAdminObtainCodeTv.setOnClickListener(this);
        mAdminPasswordVisiableIv.setOnClickListener(this);

        mAdminNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                nameAdopt(mAdminNameEt.getText().toString());
            }
        });

        mAdminPhoneEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                phoneAdopt(mAdminPhoneEt.getText().toString());
            }
        });

        mAdminEmailEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                emailAdopt(mAdminEmailEt.getText().toString());
            }
        });

        mAdminVerificationEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                codeAdopt(mAdminVerificationEt.getText().toString());
            }
        });

        mAdminPasswordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                passwordAdopt(mAdminPasswordEt.getText().toString());
            }
        });

        mAdminNameEt.addTextChangedListener(mNameTextWatcher);
        mAdminPhoneEt.addTextChangedListener(mPhoneTextWatcher);
        mAdminEmailEt.addTextChangedListener(mEmailTextWatcher);
        mAdminObtainCodeTv.addTextChangedListener(mCodeTextWatcher);
        mAdminPasswordEt.addTextChangedListener(mPasswordTextWatcher);
    }

    private void uploadMsg(String enUU, String imid) {
        String url = Constants.IM_BASE_URL() + "user/appSaveCompany";
//        String url = "http://192.168.253.136:8092/user/appSaveCompany";

        Map<String, String> map = new HashMap<>();
        map.put("sc_uu", TextUtils.isEmpty(enUU) ? "0" : enUU);
        map.put("sc_companyname", mCompanyName);
        map.put("sc_industry", mIndustry);
        map.put("sc_address", mAddress);
        map.put("sc_adminname", mAdminNameEt.getText().toString());
        map.put("sc_telephone", mAdminPhoneEt.getText().toString());
        map.put("sc_longitude", mLongitude);
        map.put("sc_latitude", mLatitude);
        String industrycode = "0";
        if ("医疗".equals(mIndustry)) {
            industrycode = "10001";
        } else if ("运动健身".equals(mIndustry)) {
            industrycode = "10002";
        } else if ("餐饮".equals(mIndustry)) {
            industrycode = "10003";
        } else if ("美容美发".equals(mIndustry)) {
            industrycode = "10004";
        } else if ("会所".equals(mIndustry)) {
            industrycode = "10005";
        } else if ("KTV".equals(mIndustry)) {
            industrycode = "10006";
        }
        map.put("sc_industrycode", industrycode);

        Map<String, Object> params = new HashMap<>();
        params.put("map", JSON.toJSON(map).toString());
        params.put("imid", TextUtils.isEmpty(imid) ? "0" : imid);
        params.put("telephone", mAdminPhoneEt.getText().toString());
        params.put("password", Md5Util.toMD5(mAdminPasswordEt.getText().toString()));
        params.put("nickname", mAdminNameEt.getText().toString());
        params.put("description", "UU互联");
        params.put("sex", "0");
        params.put("birthday", "946656000");

        Log.d("uploadparams", params.toString());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, mHandler, headers, UPLOAD_REGISTER_MESSAGE, null, null, "post");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.admin_register_next_step_btn) {
            if (CommonUtil.isNetWorkConnected(this)) {
                if (isNameAdopt && isPhoneAdopt && isEmailAdopt && isCodeAdopt && isPasswordAdopt) {
                    progressDialog.show();

                    String url = "https://account.ubtob.com/sso/mobile/userspace/setAdmin";
//                    String url = "http://192.168.253.66:8082/sso/mobile/userspace/setAdmin";

                    Map<String, Object> params = new HashMap<>();
                    params.put("adminName", mAdminNameEt.getText().toString());
                    params.put("adminTel", mAdminPhoneEt.getText().toString());
                    params.put("adminEmail", mAdminEmailEt.getText().toString());
                    params.put("checkcode", mAdminVerificationEt.getText().toString());
                    params.put("checkcodeToken", mCheckcodeToken);
                    params.put("password", mAdminPasswordEt.getText().toString());
                    params.put("pageToken", CommonUtil.getSharedPreferences(ct, "pageToken"));
                    LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                    headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                    ViewUtil.httpSendRequest(this, url, params, mHandler, headers, ADMIN_REGISTER, null, null, "post");

                } else {
                    ToastUtil.showToast(this, "请完善管理员注册信息");
                }
            } else {
                ToastUtil.showToast(this, R.string.networks_out);
            }

        } else if (id == R.id.admin_register_obtain_code_tv) {
            if (isPhoneAdopt && isEmailAdopt) {
                if (CommonUtil.isNetWorkConnected(this)) {
                    mAdminObtainCodeTv.setEnabled(false);
                    mAdminObtainCodeTv.setText("验证码获取中...");

                    progressDialog.show();
                    String url = "https://account.ubtob.com/sso/userspace/checkcode";
//                    String url = "http://192.168.253.66:8082/sso/userspace/checkcode";
                    Map<String, Object> params = new HashMap<>();
                    params.put("tel", mAdminPhoneEt.getText().toString());
                    params.put("email", mAdminEmailEt.getText().toString());
                    params.put("pageToken", CommonUtil.getSharedPreferences(ct, "pageToken"));
                    LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                    headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                    ViewUtil.httpSendRequest(this, url, params, mHandler, headers, OBTAIN_VERIFICATION_CODE, null, null, "get");
                } else {
                    ToastUtil.showToast(this, R.string.networks_out);
                }

            } else {
                ToastUtil.showToast(this, "请填写正确的手机号和邮箱");
            }
        } else if (id == R.id.admin_register_password_visiable_iv) {
            isPasswordVisiable = !isPasswordVisiable;
            if (isPasswordVisiable) {
                mAdminPasswordVisiableIv.setImageResource(R.drawable.ic_password_visiable);
                mAdminPasswordEt.setTransformationMethod(new HideReturnsTransformationMethod());
            } else {
                mAdminPasswordVisiableIv.setImageResource(R.drawable.ic_password_invisible);
//                mAdminPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mAdminPasswordEt.setTransformationMethod(new PasswordTransformationMethod());
            }
            mAdminPasswordEt.setSelection(mAdminPasswordEt.getText().length());
        }
    }


    private void passwordAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mPasswordErrorTextView.setVisibility(View.VISIBLE);
            isPasswordAdopt = false;
        } else {
            mPasswordErrorTextView.setVisibility(View.INVISIBLE);
            isPasswordAdopt = true;
        }
        isRegButtonEnable();
    }

    private void nameAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mNameErrorTextView.setVisibility(View.VISIBLE);
            isNameAdopt = false;
        } else {
            mNameErrorTextView.setVisibility(View.INVISIBLE);
            isNameAdopt = true;
        }
        isRegButtonEnable();
    }

    private void phoneAdopt(String text) {
        if (TextUtils.isEmpty(text) || !RegexUtil.checkRegex(text, RegexUtil.REGEX_MOBILE)) {
            mPhoneErrorTextView.setVisibility(View.VISIBLE);
            isPhoneAdopt = false;
        } else {
            mPhoneErrorTextView.setVisibility(View.INVISIBLE);
            isPhoneAdopt = true;
        }
        isRegButtonEnable();
    }

    private void emailAdopt(String text) {
        if (TextUtils.isEmpty(text) || !RegexUtil.checkRegex(text, RegexUtil.REGEX_EMAIL)) {
            mEmailErrorTextView.setVisibility(View.VISIBLE);
            isEmailAdopt = false;
        } else {
            mEmailErrorTextView.setVisibility(View.INVISIBLE);
            isEmailAdopt = true;
        }
        isRegButtonEnable();
    }

    private void codeAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mCodeErrorTextView.setVisibility(View.VISIBLE);
            isCodeAdopt = false;
        } else {
            mCodeErrorTextView.setVisibility(View.INVISIBLE);
            isCodeAdopt = true;
        }
        isRegButtonEnable();
    }

    private void isRegButtonEnable() {
        if (isNameAdopt && isPhoneAdopt && isEmailAdopt && isCodeAdopt && isPasswordAdopt) {
            mNextStepButton.setEnabled(true);
        } else {
            mNextStepButton.setEnabled(false);
        }
    }
}
