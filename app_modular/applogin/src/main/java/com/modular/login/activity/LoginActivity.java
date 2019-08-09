package com.modular.login.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.hmac.Md5Util;
import com.common.preferences.PreferenceUtils;
import com.common.system.SystemUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.LoginRegisterResult;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.StatusBarUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.helper.LoginHelper;
import com.modular.login.R;
import com.uas.applocation.UasLocationHelper;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class LoginActivity extends BaseActivity implements View.OnClickListener {
    public static final String PASS_WORDS = "PASS_WORDS";
    private CircleImageView mHeader;
    private EditText mPhoneNumberEdit;
    private EditText mPasswordEdit;

    private TextView tv_register, tv_findPwd, mLoginButton;
    private View mToastHead;
    private FrameLayout mToastLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        StatusBarUtil.immersive(this, 0x00000000, 0.0f);
        initView();
        initPhonePass();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!StringUtil.isEmpty(MyApplication.getInstance().mLoginUser.getUserId())) {
            AvatarHelper.getInstance().display(MyApplication.getInstance().mLoginUser.getUserId(), mHeader, true, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initView() {
        mToastHead = findViewById(R.id.login_toast_head);
        mToastLayout = findViewById(R.id.login_toast_fl);
        StatusBarUtil.setPaddingSmart(this, mToastHead);

        mPhoneNumberEdit = (EditText) findViewById(R.id.phone_numer_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        mHeader = (CircleImageView) findViewById(R.id.iv_head);
        tv_findPwd = (TextView) findViewById(R.id.tv_findPwd);
        tv_register = (TextView) findViewById(R.id.tv_register);
        mLoginButton = findViewById(R.id.login_btn);
        if (CommonUtil.getSharedPreferences(this, "user_phone") != null && CommonUtil.getSharedPreferences(this, "user_phone").length() > 0) {
            mPhoneNumberEdit.setText(CommonUtil.getSharedPreferences(this, "user_phone"));


        }
        if (PreferenceUtils.getString(this, PASS_WORDS) != null && PreferenceUtils.getString(this, PASS_WORDS).length() > 0) {
            mPasswordEdit.setText(PreferenceUtils.getString(this, PASS_WORDS));

        }
        //登陆账号
        mLoginButton.setOnClickListener(this);
        tv_findPwd.setOnClickListener(this);
        tv_register.setOnClickListener(this);
        if (!StringUtil.isEmpty(MyApplication.getInstance().mLoginUser.getUserId())) {
            AvatarHelper.getInstance().display(MyApplication.getInstance().mLoginUser.getUserId(), mHeader, true, true);
        }

        mPhoneNumberEdit.addTextChangedListener(new MyTextWatcher());
        mPasswordEdit.addTextChangedListener(new MyTextWatcher());
    }

    private void initPhonePass() {
        requestPermission(Manifest.permission.READ_PHONE_STATE, null, null);
        if (!UasLocationHelper.getInstance().isLocationUpdate()) {
            UasLocationHelper.getInstance().requestLocation();
        }
        if (ViewUtil.mdProcessDialog != null) {
            ViewUtil.mdProcessDialog.cancel();
        }
        if (CommonUtil.getSharedPreferences(this, "user_phone") != null && CommonUtil.getSharedPreferences(this, "user_phone").length() > 0) {
            mPhoneNumberEdit.setText(CommonUtil.getSharedPreferences(this, "user_phone"));
        }
        if (PreferenceUtils.getString(this, PASS_WORDS) != null && PreferenceUtils.getString(this, PASS_WORDS).length() > 0) {
            mPasswordEdit.setText(PreferenceUtils.getString(this, PASS_WORDS));
        }
    }


    class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String phone = mPhoneNumberEdit.getText().toString().trim();
            String password = mPasswordEdit.getText().toString();

            if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
                mLoginButton.setEnabled(false);
            } else {
                mLoginButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_btn) {
//            final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
//            final String password = mPasswordEdit.getText().toString();
//            ViewUtil.LoginTask(phoneNumber, password, mContext);

            login();
        } else if (v.getId() == R.id.tv_register) {
            startActivity(new Intent(mContext, RegisterSelectActivity.class));
        } else if (v.getId() == R.id.tv_findPwd) {
//            IntentUtils.webLinks(mContext,
//                    Constants.ACCOUNT_CENTER_HOST + "reset/forgetPasswordValidationAccount?appId=home&returnURL=http%3A%2F%2Fwww.ubtob.com%2F&source=UU",
//                    getString(R.string.login_getpwd));
            startActivity(new Intent(mContext, ModifyPasswordModeActivity.class));
        }
    }

    private void login() {
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        final String password = mPasswordEdit.getText().toString();
        PreferenceUtils.putString(this, PASS_WORDS, password);
        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtil.showToast(this, R.string.login_account_empty, mToastLayout);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            ToastUtil.showToast(this, R.string.login_pwd_empty, mToastLayout);
            return;
        }
        // 加密之后的密码
        final String digestPwd = new String(Md5Util.toMD5(password));
        final String requestTag = "login";
        final ProgressDialog dialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait), true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelAll(requestTag);
            }
        });
        dialog.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.progress_color));
        ProgressDialogUtil.show(dialog);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("telephone", Md5Util.toMD5(phoneNumber));// 账号登陆的时候需要MD5加密，服务器需求
//		params.put("loginNo", phoneNumber);// 账号登陆的时候需要MD5加密，服务器需求
        params.put("password", digestPwd);
        // 附加信息
        params.put("model", SystemUtil.getModel());
        params.put("osVersion", SystemUtil.getOsVersion());
        params.put("serial", SystemUtil.getDeviceId(MyApplication.getInstance()));
        // 地址信息
        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        
        final StringJsonObjectRequest<LoginRegisterResult> request = new StringJsonObjectRequest<LoginRegisterResult>(mConfig.USER_LOGIN,
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ProgressDialogUtil.dismiss(dialog);
                        ToastUtil.showToast(mContext, R.string.net_exception, mToastLayout);
                    }
                }, new StringJsonObjectRequest.Listener<LoginRegisterResult>() {

            @Override
            public void onResponse(ObjectResult<LoginRegisterResult> result) {
                LogUtil.d("HttpLogs", "IM login:" + JSON.toJSONString(result));
                if (result == null) {
                    ProgressDialogUtil.dismiss(dialog);
                    ToastUtil.showToast(mContext, R.string.data_exception, mToastLayout);
                    return;
                }
                boolean success = false;
                if (result.getResultCode() == Result.CODE_SUCCESS) {
                    success = LoginHelper.setLoginUser(mContext, phoneNumber, digestPwd, result);// 设置登陆用户信息
                }
                if (success) {
                    login_manage(phoneNumber, password);
                } else {
                    String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.login_failed) : result.getResultMsg();
                    ToastUtil.showToast(mContext, message, mToastLayout);
                }
                ProgressDialogUtil.dismiss(dialog);
            }
        }, LoginRegisterResult.class, params);
        request.setTag(requestTag);
        addDefaultRequest(request);
    }


    private void login_manage(String phone, String password) {
        if (TextUtils.isEmpty(phone)) {
            return;
        }
        if (TextUtils.isEmpty(password)) {
            return;
        }
        mContext = null;
        if (mContext == null) {
            mContext = this;
        }
        ViewUtil.LoginTask(phone, password, mContext);
    }

}
