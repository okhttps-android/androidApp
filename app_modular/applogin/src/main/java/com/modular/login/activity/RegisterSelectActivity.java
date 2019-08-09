package com.modular.login.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.core.app.Constants;
import com.core.base.SupportToolBarActivity;
import com.core.utils.IntentUtils;
import com.modular.login.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterSelectActivity extends SupportToolBarActivity implements View.OnClickListener {
    private CircleImageView ivHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_select);
        ivHead = (CircleImageView) findViewById(R.id.iv_head);
        setTitle("新用户注册");
        findViewById(R.id.person_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IntentUtils.webLinks(mContext,
//                        Constants.ACCOUNT_CENTER_HOST + "register/personalRegistration",
//                        "个人注册");
                startActivity(new Intent(mContext, PersonalRegActivity.class));
                finish();
            }
        });

        findViewById(R.id.company_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.webLinks(mContext,
                        Constants.ACCOUNT_CENTER_HOST + "register/enterpriseRegistration",
                        "企业注册");
//                startActivity(new Intent(mContext, EnterpriseRegisterActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
