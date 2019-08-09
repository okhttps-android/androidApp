package com.modular.login.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.SimpleView;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.login.R;

/**
 * @author RaoMeng
 * @describe 修改密码(通过邮箱)
 * @date 2018/5/4 10:45
 */
public class ModifyPasswordEmailActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView {
    private final int FLAG_SEND_EMAIL = 22;

    private TextView mContentTextView, mConfirmButton;
    private String mEmail, mToken;

    @Override
    protected int getLayout() {
        return R.layout.activity_modify_password_email;
    }

    @Override
    protected void initView() {
        setTitle(R.string.title_password_reset);

        Intent intent = getIntent();
        if (intent != null) {
            mEmail = intent.getStringExtra(Constants.FLAG.MODIFY_PASSWORD_EMAIL);
            mToken = intent.getStringExtra(Constants.FLAG.MODIFY_PASSWORD_TOKEN);
        }
        mContentTextView = $(R.id.modify_password_email_content_tv);
        mConfirmButton = $(R.id.modify_password_email_btn);
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
                mPresenter.httpRequest(ModifyPasswordEmailActivity.this, Constants.ACCOUNT_CENTER_HOST,
                        new HttpParams.Builder()
                                .url("im/resetPwd/sendEmail")
                                .method(Method.GET)
                                .flag(FLAG_SEND_EMAIL)
                                .addParam("token", mToken)
                                .build());
            }
        });
    }

    @Override
    protected void initData() {
        if (mEmail != null && mEmail.length() > 3) {
            int index = mEmail.lastIndexOf("@");
            if (index != -1) {
                String emailStart = mEmail.substring(0, 3);
                String emailEnd = mEmail.substring(index);

                mContentTextView.setText("*使用电子邮箱" + emailStart + "***" + emailEnd + "进行验证，有效期7天");
            }
        }
    }

    @Override
    public void requestSuccess(int what, Object object) {
        try {
            String result = object.toString();
            if (JSONUtil.validate(result)) {
                JSONObject resultObject = JSON.parseObject(result);
                boolean success = JSONUtil.getBoolean(resultObject, "success");
                if (success) {
                    mConfirmButton.setText("已发送验证邮件，请查收");
                    mConfirmButton.setEnabled(false);
                } else {
                    String errorMsg = JSONUtil.getText(resultObject, "errMsg");
                    if (TextUtils.isEmpty(errorMsg)) {
                        toast("邮件发送异常，请重新发送");
                    } else {
                        toast(errorMsg);
                    }
                }
            } else {
                toast("邮件发送异常，请重新发送");
            }
        } catch (Exception e) {
            toast("邮件发送异常，请重新发送");
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
