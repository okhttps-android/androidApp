package com.modular.appmessages.activity;

import android.os.Bundle;
import android.view.View;

import com.common.system.SystemUtil;
import com.core.base.BaseActivity;
import com.modular.appmessages.R;

public class UUHelperDetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uuhelper_details);
        findViewById(R.id.takePhotoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtil.phoneAction(ct, "4008301818");
            }
        });
    }


}
