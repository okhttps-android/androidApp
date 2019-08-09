package com.modular.login.activity;

import android.view.View;
import android.widget.LinearLayout;

import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.BasePresenter;
import com.modular.login.R;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/5/4 11:19
 */
public class ModifyPasswordModeActivity extends BaseMVPActivity {
    private LinearLayout mPhoneView, mEmailView;

    @Override
    protected int getLayout() {
        return R.layout.activity_modify_password_mode;
    }

    @Override
    protected void initView() {
        setTitle(R.string.forget_password);
        mPhoneView = $(R.id.modify_password_mode_phone_ll);
        mEmailView = $(R.id.modify_password_mode_email_ll);
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mPhoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ModifyPasswordPhoneActivity.class);
            }
        });

        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ModifyPasswordEmailVerifyActivity.class);
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}
