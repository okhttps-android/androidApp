package com.modular.login.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.core.base.BaseActivity;
import com.modular.login.R;

/**
 * Created by RaoMeng on 2017/9/21.
 * 企业注册完成页面
 */

public class EnterpriseCompleteActivity extends BaseActivity {
    private TextView mCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterprise_complete);
       setTitle("注册完成");

        mCompleteTextView = (TextView) findViewById(R.id.enterprise_complete_login_tv);

        mCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(EnterpriseCompleteActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected boolean onHomeAsUp() {
        Intent intent = new Intent();
        intent.setClass(EnterpriseCompleteActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.setClass(EnterpriseCompleteActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
