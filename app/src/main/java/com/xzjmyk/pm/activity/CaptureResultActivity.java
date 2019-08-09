package com.xzjmyk.pm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.file.DownloadUtil;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.xzjmyk.pm.activity.ui.me.ScanInfoResultsActivity;
import com.xzjmyk.pm.activity.view.crouton.Crouton;
import com.xzjmyk.pm.activity.view.crouton.LifecycleCallback;


/**
 * @author RaoMeng
 * update fanglh 2017-6-7 新增扫描名片二维码需求
 * update fanglh 2017-9-14 新增扫描UAS二维码登录功能
 */
public class CaptureResultActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_result);
        initActivity();
        initData();
    }

    private void initActivity() {
        mWebView = (WebView) findViewById(R.id.result_webview);

        WebSettings webSettings = mWebView.getSettings();
        //允许加载JavaScript
        webSettings.setJavaScriptEnabled(true);
        //网页自适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
                return false;
            }

        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
                Log.d("fileurl", url);
                String fileName = url.substring(url.lastIndexOf("/") + 1);

                PopupWindowHelper.showAlart(CaptureResultActivity.this,
                        "提示", "确定下载文件<" + fileName + ">吗?",
                        new PopupWindowHelper.OnSelectListener() {
                            @Override
                            public void select(boolean selectOk) {
                                DownloadUtil.DownloadFile(CaptureResultActivity.this, url, "/sdcard/uu");
                            }
                        });
            }
        });
    }


    private void initData() {
        Intent intent = getIntent();
        if (null != intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                String result = bundle.getString(CodeUtils.RESULT_STRING);
                Log.d("scanurl", result);
                //TODO update fanglh 2017-6-7 新增扫描名片二维码需求
                if (StringUtil.isEmpty(result)) {
                    Crouton crouton = Crouton.makeText(CaptureResultActivity.this, "您扫描的二维码信息为空", 0xffff4444, 1500);
                    crouton.show();
                    crouton.setLifecycleCallback(new LifecycleCallback() {
                        @Override
                        public void onDisplayed() {

                        }

                        @Override
                        public void onRemoved() {
                            finish();
                        }
                    });
                } else {
                    if (CommonUtil.isWebsite(result)) {
                        mWebView.loadUrl(result);
                    } else {
                        doJudgeInfoCard(result);//进行是否为名片二维码判断
                    }
                }
            }
        }
    }

    private void doJudgeInfoCard(String result) {
        Boolean isJSONData = JSONUtil.validate(result);//是否是JSON格式字符
        if (isJSONData && result.contains("uu_name") && result.contains("uu_phone")) {
            startActivity(new Intent(this, ScanInfoResultsActivity.class)
                    .putExtra("ScanResults", result)
                    .putExtra("isQRData", true));// true ：扫描到的是名片信息标志
        } else if (isJSONData && result.contains("clientId")) {
            doUasLoginRequest(result);
        } else if (isJSONData && result.contains("token")) {
            doB2BLoginRequest(result);
        } else {
            startActivity(new Intent(this, ScanInfoResultsActivity.class)
                    .putExtra("ScanResults", result)
                    .putExtra("isQRData", false));
        }
        finish();
    }

    private void doB2BLoginRequest(String result) {
        String token = JSON.parseObject(result).getString("token");
        if (StringUtil.isEmail(token)) {
            return;
        }
        String url = null;
//        url = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_baseurl") + "common/checkQrcodeScan.action";
        url = "http://192.168.253.123:8889/" + "sso/qrcode/check";
        String photo = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "user_phone");
        String password = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "user_password");
        String enuu = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu");
        Intent intent_web = new Intent("com.modular.main.WebViewCommActivity");
        url = url + "?uid=" + photo + "&token=" + token + "&password=" + password + "&enuu=" + enuu;
        intent_web.putExtra("url", url);
        LogUtil.i("loginUrl", url);
        intent_web.putExtra("title", "扫码登录");
        intent_web.putExtra("cookie", true);
        startActivity(intent_web);
        finish();
    }

    /**
     * 新增扫描UAS二维码登录功能
     *
     * @param result
     */
    private void doUasLoginRequest(String result) {
        String clientId = JSON.parseObject(result).getString("clientId");
        if (StringUtil.isEmail(clientId)) return;

        /*HttpClient httpClient = new HttpClient.Builder("http://192.168.253.63:8080/ERP/").build();
        httpClient.Api().send(new HttpClient.Builder()
        .url("common/checkQrcodeScan.action")
        .add("clientId",clientId)
        .add("em_code ", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"))
        .add("sob",CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"))
        .method(Method.GET)
        .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                Log.i("FLH",JSON.toJSONString(o)+"");
                Toast.makeText(MyApplication.getInstance(),JSON.toJSONString(o)+"",Toast.LENGTH_LONG).show();
            }
        }));*/
        String url = null;
        url = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_baseurl") + "common/checkQrcodeScan.action";
//        url = "http://192.168.253.6/ERP/"+"common/checkQrcodeScan.action";
        String em_code = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        String sob = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
        String password = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "user_password");
        Intent intent_web = new Intent("com.modular.main.WebViewCommActivity");
        url = url + "?em_code=" + em_code + "&sob=" + sob + "&clientId=" + clientId + "&password=" + password;
        intent_web.putExtra("url", url);
        LogUtil.i("flh", url);
        intent_web.putExtra("title", "扫码登录");
        intent_web.putExtra("cookie", true);
        startActivity(intent_web);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
