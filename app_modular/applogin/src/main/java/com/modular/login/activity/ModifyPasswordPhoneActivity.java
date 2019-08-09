package com.modular.login.activity;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.base.view.SimpleView;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.core.widget.StrengthView;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.login.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author RaoMeng
 * @describe 修改密码(通过手机号)
 * @date 2018/5/4 10:45
 */
public class ModifyPasswordPhoneActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView, View.OnClickListener {
    public static final String REGEXP_MOBILE_CONTINENT = "1[0-9]{10}";
    private static final int FLAG_OBTAIN_CODE = 11;
    private static final int FLAG_CONFIRM_MODIFY = 12;
    private static final int VERIFICATION_TIME_TASK = 13;

    private ClearEditText mPhoneEditText, mCodeEditText, mPasswordEditText, mPasswordConfirmEditText;
    private TextView mObtainCodeTextView, mConfirmButton, mSuccessButton;
    private StrengthView mStrengthView;
    private LinearLayout mSuccessLinearLayout, mPasswordLinearLayout, mPasswordConfirmLinearLayout;

    private String mVerifyToken;
    private ScheduledExecutorService mExecutorService;
    private ScheduledFuture<?> mScheduledFuture;
    private int mTime = 60;
    private Timer mTimer;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VERIFICATION_TIME_TASK:
                    if (mTime > 0) {
                        mTime--;
                        mObtainCodeTextView.setEnabled(false);
                        mObtainCodeTextView.setText("剩余" + mTime + "s");
                    } else {
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        mObtainCodeTextView.setEnabled(true);
                        mObtainCodeTextView.setText(R.string.obtain_verify_code);
                    }
                    break;
            }
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.activity_modify_password_phone;
    }

    @Override
    protected void initView() {
        AndroidBug5497Workaround.assistActivity(this);
        setTitle(R.string.title_password_reset);

        mPhoneEditText = $(R.id.modify_password_phone_mobile_et);
        mCodeEditText = $(R.id.modify_password_phone_code_et);
        mPasswordEditText = $(R.id.modify_password_phone_password_et);
        mPasswordConfirmEditText = $(R.id.modify_password_phone_password_confirm_et);
        mObtainCodeTextView = $(R.id.modify_password_phone_code_btn);
        mConfirmButton = $(R.id.modify_password_phone_confirm_tv);
        mStrengthView = $(R.id.modify_password_phone_strength_sv);
        mSuccessButton = $(R.id.modify_password_success_btn);
        mSuccessLinearLayout = $(R.id.modify_password_success_ll);
        mPasswordLinearLayout = $(R.id.modify_password_phone_password_ll);
        mPasswordConfirmLinearLayout = $(R.id.modify_password_phone_password_confirm_ll);
        mPasswordLinearLayout.setEnabled(false);
        mPasswordConfirmLinearLayout.setEnabled(false);
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initEvent() {
        mObtainCodeTextView.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);
        mSuccessButton.setOnClickListener(this);

        mPhoneEditText.addTextChangedListener(new EnableTextWatcher());
        mCodeEditText.addTextChangedListener(new EnableTextWatcher());
        mPasswordConfirmEditText.addTextChangedListener(new EnableTextWatcher());
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                StrengthView.Level passwordLevel = checkPasswordLevel(editable.toString());
                if (passwordLevel == StrengthView.Level.STRENGTH_NONE) {
                    mStrengthView.setVisibility(View.GONE);
                } else {
                    mStrengthView.setVisibility(View.VISIBLE);
                    mStrengthView.setLevel(passwordLevel);
                    if (passwordLevel == StrengthView.Level.STRENGTH_WEAK) {
                        mConfirmButton.setEnabled(false);
                    } else {
                        isConfirmEnable();
                    }
                }
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.modify_password_phone_code_btn) {
            String phone = mPhoneEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(phone) && phone.matches(REGEXP_MOBILE_CONTINENT)) {
                if (CommonUtil.isNetWorkConnected(this)) {
                    mObtainCodeTextView.setEnabled(false);
                    mObtainCodeTextView.setText(R.string.verify_code_obtaining);

                    mPresenter.httpRequest(this, Constants.ACCOUNT_CENTER_HOST,
                            new HttpParams.Builder()
                                    .url("im/resetPwd/checkCode")
                                    .method(Method.GET)
                                    .flag(FLAG_OBTAIN_CODE)
                                    .addParam("mobile", phone)
                                    .build());
                } else {
                    ToastUtil.showToast(this, R.string.networks_out);
                }
            } else {
                if (TextUtils.isEmpty(phone)) {
                    toast("手机号为空");
                } else {
                    toast("请填写正确的手机号");
                }
            }
        } else if (i == R.id.modify_password_phone_confirm_tv) {
            String phone = mPhoneEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(phone) && phone.matches(REGEXP_MOBILE_CONTINENT)) {
                String password1 = mPasswordEditText.getText().toString();
                String password2 = mPasswordConfirmEditText.getText().toString();
                String codeToken = CommonUtil.getSharedPreferences(this, Constants.CACHE.MODIFY_PASSWORD_VERIFY_CODE);
                if (password1 != null && password1.equals(password2)) {
                    String verifyCode = mCodeEditText.getText().toString().trim();
                    mPresenter.httpRequest(this, Constants.ACCOUNT_CENTER_HOST,
                            new HttpParams.Builder()
                                    .url("im/resetPwd")
                                    .method(Method.POST)
                                    .flag(FLAG_CONFIRM_MODIFY)
                                    .addParam("mobile", phone)
                                    .addParam("token", codeToken)
                                    .addParam("code", verifyCode)
                                    .addParam("password", password1).build());
                } else {
                    toast("两次输入的密码不一致");
                }
            } else {
                if (TextUtils.isEmpty(phone)) {
                    toast("手机号为空");
                } else {
                    toast("请填写正确的手机号");
                }
            }
        } else if (i == R.id.modify_password_success_btn) {
            startActivity(LoginActivity.class);
            finish();
        }
    }

    class EnableTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            isConfirmEnable();
        }
    }

    private void isConfirmEnable() {
        String phone = mPhoneEditText.getText().toString().trim();
        String code = mCodeEditText.getText().toString().trim();
        String password1 = mPasswordEditText.getText().toString();
        String password2 = mPasswordConfirmEditText.getText().toString();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(code)) {
            mPasswordLinearLayout.setEnabled(false);
            mPasswordEditText.setEnabled(false);
            mPasswordConfirmLinearLayout.setEnabled(false);
            mPasswordConfirmEditText.setEnabled(false);
        } else {
            mPasswordLinearLayout.setEnabled(true);
            mPasswordEditText.setEnabled(true);
            mPasswordConfirmLinearLayout.setEnabled(true);
            mPasswordConfirmEditText.setEnabled(true);
        }

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(code) || TextUtils.isEmpty(password1) || TextUtils.isEmpty(password2)) {
            mConfirmButton.setEnabled(false);
        } else {
            mConfirmButton.setEnabled(true);
        }
    }

    @Override
    public void requestSuccess(int what, Object object) {
        String result = (object == null ? "" : object.toString());
        switch (what) {
            case FLAG_OBTAIN_CODE:
                try {
                    analysisVerifyCode(result);
                } catch (Exception e) {
                    toast(getString(R.string.obtain_verify_code_failed));
                    mObtainCodeTextView.setEnabled(true);
                    mObtainCodeTextView.setText(R.string.obtain_verify_code);
                }
                break;
            case FLAG_CONFIRM_MODIFY:
                try {
                    JSONObject resultObject = JSON.parseObject(result);
                    boolean success = JSONUtil.getBoolean(resultObject, "success");
                    if (success) {
                        mSuccessLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        String errorMsg = JSONUtil.getText(resultObject, "errMsg");
                        if (TextUtils.isEmpty(errorMsg)) {
                            toast("密码修改异常");
                        } else {
                            toast(errorMsg);
                        }
                    }
                } catch (Exception e) {

                }
                break;
            default:
                break;
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        switch (what) {
            case FLAG_OBTAIN_CODE:
                toast(getString(R.string.obtain_verify_code_failed));
                mObtainCodeTextView.setEnabled(true);
                mObtainCodeTextView.setText(R.string.obtain_verify_code);
                break;
            case FLAG_CONFIRM_MODIFY:
                toast(errorMsg);
                break;
            default:
                break;
        }
    }

    private void analysisVerifyCode(String result) {
        if (JSONUtil.validate(result)) {
            Log.d("modifyCode", result);
            JSONObject resultObject = JSON.parseObject(result);
            JSONObject contentObject = resultObject.getJSONObject("content");
            if (contentObject != null) {
                toast(R.string.verify_code_have_sent);
                mVerifyToken = JSONUtil.getText(contentObject, "token");
                CommonUtil.setSharedPreferences(this, Constants.CACHE.MODIFY_PASSWORD_VERIFY_CODE, mVerifyToken);
                mTime = 60;
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = Message.obtain();
                        message.what = VERIFICATION_TIME_TASK;
                        mHandler.sendMessage(message);
                    }
                }, 0, 1000);
            } else {
                toast(getString(R.string.obtain_verify_code_failed));
                mObtainCodeTextView.setEnabled(true);
                mObtainCodeTextView.setText(R.string.obtain_verify_code);
            }
        } else {
            toast(getString(R.string.obtain_verify_code_failed));
            mObtainCodeTextView.setEnabled(true);
            mObtainCodeTextView.setText(R.string.obtain_verify_code);
        }
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();

    }

    public static StrengthView.Level checkPasswordLevel(String password) {
        String strongRegex = "^(?=.{8,20})(((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]))|((?=.*[0-9])((?=.*[a-zA-Z]))(?=.*[^a-zA-Z0-9]))).*$";
        String mediumRegex = "^(?=.{8,20})(((?=.*[0-9])(?=.*[a-z]))|((?=.*[0-9])(?=.*[A-Z]))).*$";
        if (TextUtils.isEmpty(password)) {
            return StrengthView.Level.STRENGTH_NONE;
        } else if (password.matches(strongRegex)) {
            return StrengthView.Level.STRENGTH_STRONG;
        } else if (password.matches(mediumRegex)) {
            return StrengthView.Level.STRENGTH_MEDIUM;
        } else {
            return StrengthView.Level.STRENGTH_WEAK;
        }
    }
}
