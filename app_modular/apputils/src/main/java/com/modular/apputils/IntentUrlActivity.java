package com.modular.apputils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.common.LogUtil;
import com.common.system.ActivityUtils;
import com.core.app.Constants;

/**
 * @desc:负责分发外部URL链接的中转界面
 * @author：Arison on 2018/1/9
 */
public class IntentUrlActivity extends Activity {
    private static final String TAG = "IntentUrlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_url);
        final Intent i_getvalue = getIntent();
        final String action = i_getvalue.getAction();
        final boolean isOpenApp = ActivityUtils.isRunningInForeground();
        LogUtil.d(TAG, "isOpenApp:" + isOpenApp);
        if (isOpenApp) {
            openIntentUrl(action, i_getvalue);
        } else {
            startActivity(new Intent("com.modular.main.SplashActivity"));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openIntentUrl(action, i_getvalue);
                }
            }, 2000);
        }
    }

    private void openIntentUrl(String action, Intent i_getvalue) {
        try {
            if (Intent.ACTION_VIEW.equals(action)) {
                Uri uri = i_getvalue.getData();
                if (uri != null) {
                    String pagekind = uri.getQueryParameter("pagekind");
                    String id = uri.getQueryParameter("id");
//                    String enuu = uri.getQueryParameter("uu");
                    String enuu = Constants.FLAG.GET_LOCAL_ENUU;
                    String phone = uri.getQueryParameter("telephone");

                    LogUtil.d(TAG, "pagekind:" + pagekind + " id:" + id + " enuu:" + enuu);
                    if ("A".equals(pagekind)) {
                        Intent intent = new Intent("com.modular.work.platform.activity.CustomerInquiryDetailActivity");
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, id);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ENUU, enuu);
                        startActivity(intent);
                    }
                    if ("B".equals(pagekind)) {//公共询价单
                        Intent intent = new Intent("com.modular.work.platform.activity.CustomerInquiryDetailActivity");
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, id);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ENUU, enuu);
                        startActivity(intent);
                    }
                    if ("C".equals(pagekind)) {
                        Intent intent = new Intent("com.modular.work.platform.activity.PurchaseDetailsActivity");
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, id);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, Constants.FLAG.STATE_PURCHASE_ORDER_TODO);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ENUU, enuu);
                        startActivity(intent);
                    }
                    if (TextUtils.isEmpty(pagekind) || "null".equals(pagekind)) {
                        startActivity(new Intent("com.modular.main.SplashActivity"));
                    }
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
