package com.modular.login.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.SimpleView;
import com.core.utils.VerifyCode;
import com.core.widget.ClearEditText;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.login.R;

/**
 * @author RaoMeng
 * @describe 修改密码(通过邮箱)-验证手机
 * @date 2018/5/4 10:45
 */
public class ModifyPasswordEmailVerifyActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView {
    public static final String REGEXP_MOBILE_CONTINENT = "1[0-9]{10}";
    public static final int FLAG_GET_EMAIL = 21;

    private ClearEditText mMobileEditText, mCodeEditText;
    private TextView mConfirmButton;
    private ImageView mCodeImageView;
    private String mVerifyCode;

    @Override
    protected int getLayout() {
        return R.layout.activity_modify_password_email_verify;
    }

    @Override
    protected void initView() {
        setTitle(R.string.title_password_reset);

        mMobileEditText = $(R.id.modify_password_email_mobile_et);
        mCodeEditText = $(R.id.modify_password_email_code_et);
        mConfirmButton = $(R.id.modify_password_email_confirm_tv);
        mCodeImageView = $(R.id.modify_password_email_code_iv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeImageView.setImageBitmap(VerifyCode.getInstance().createBitmap());
        mVerifyCode = VerifyCode.getInstance().getCode().toLowerCase();
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initEvent() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = mMobileEditText.getText().toString().trim();
                String code = mCodeEditText.getText().toString().trim().toLowerCase();
                if (!TextUtils.isEmpty(phone) && phone.matches(REGEXP_MOBILE_CONTINENT)) {
                    if (code.equals(mVerifyCode)) {
                        mPresenter.httpRequest(ModifyPasswordEmailVerifyActivity.this, Constants.ACCOUNT_CENTER_HOST,
                                new HttpParams.Builder()
                                        .url("im/resetPwd/getEmail")
                                        .method(Method.GET)
                                        .addParam("mobile", phone)
                                        .flag(FLAG_GET_EMAIL)
                                        .build());
                    } else {
                        toast("验证码不正确");
                    }
                } else {
                    toast("请填写正确的手机号");
                }
            }
        });

        mCodeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeImageView.setImageBitmap(VerifyCode.getInstance().createBitmap());
                mVerifyCode = VerifyCode.getInstance().getCode().toLowerCase();
            }
        });

        mMobileEditText.addTextChangedListener(new MyTextWatcher());
        mCodeEditText.addTextChangedListener(new MyTextWatcher());
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
            String mobile = mMobileEditText.getText().toString().trim();
            String code = mCodeEditText.getText().toString().trim();

            if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(code)) {
                mConfirmButton.setEnabled(false);
            } else {
                mConfirmButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void requestSuccess(int what, Object object) {
        if (what == FLAG_GET_EMAIL) {
            try {
                String result = object.toString();
                if (JSONUtil.validate(result)) {
                    JSONObject resultObject = JSON.parseObject(result);
                    boolean success = JSONUtil.getBoolean(resultObject, "success");
                    if (success) {
                        JSONObject contentObject = resultObject.getJSONObject("content");
                        if (contentObject != null) {
                            String email = JSONUtil.getText(contentObject, "email");
                            String token = JSONUtil.getText(contentObject, "token");

                            Intent intent = new Intent();
                            intent.setClass(ModifyPasswordEmailVerifyActivity.this, ModifyPasswordEmailActivity.class);
                            intent.putExtra(Constants.FLAG.MODIFY_PASSWORD_EMAIL, email);
                            intent.putExtra(Constants.FLAG.MODIFY_PASSWORD_TOKEN, token);
                            startActivity(intent);
                            finish();
                        } else {
                            toast("获取邮箱异常，请重新获取");
                        }
                    } else {
                        String errorMsg = JSONUtil.getText(resultObject, "errMsg");
                        if (TextUtils.isEmpty(errorMsg)) {
                            toast("获取邮箱异常，请重新获取");
                        } else {
                            toast(errorMsg);
                        }
                    }
                } else {
                    toast("获取邮箱异常，请重新获取");
                }
            } catch (Exception e) {
                toast("获取邮箱异常，请重新获取");
            }
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        toast(errorMsg);
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }
}
