package com.xzjmyk.pm.activity.ui.me;

import android.content.Intent;
import android.os.Bundle;

import com.core.base.BaseActivity;

public class PayTestActivity extends BaseActivity {
    private int paytype = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pay_test);
//        initView();
    }

//    private void initView() {
//        findViewById(R.id.wxpay_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                paytype = 1;
//              gotoPayActivity(paytype);
//            }
//        });
//        findViewById(R.id.alipay_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                paytype = 2;
//                gotoPayActivity(paytype);
//            }
//        });
//        findViewById(R.id.unionpay_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                paytype = 3;
//                gotoPayActivity(paytype);
//        }
//        });
//    }

    private void gotoPayActivity(int type) {
        startActivity(new Intent(this,PayListActivity.class)
                                    .putExtra("paytype",type));
    }
}
