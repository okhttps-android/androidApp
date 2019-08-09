package com.xzjmyk.pm.activity.ui.erp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.common.data.Blowfish;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.model.LoginEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import org.apache.http.util.EncodingUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @注释：链接网址
 */
@SuppressWarnings("deprecation")
public class WebViewLoadActivity extends BaseActivity {

    protected String url;
    private String paramurl;// 连接到具体网页的url
    public boolean isStartApp = false;
    private String master;

    private PullToRefreshWebView webView;
    private ProgressBar pb;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SUCCESS_ERP:
                    Bundle bundle = msg.getData();
                    String result = bundle.getString("result");
                    Log.i("wang", "切换登录：" + result);
                    try {
                        String sessionId = FlexJsonUtil.fromJson(result).get("sessionId").toString();
                        String erpaccount = FlexJsonUtil.fromJson(result).get("erpaccount").toString();
                        String url = bundle.getString("url");
                        String uu = bundle.getString("uu");
                        String master = bundle.getString("master");
                        String name = bundle.getString("name");
                        String masterId = bundle.getString("masterId");
                        //添加获取报表地址
                        String en_admin = JSONUtil.getText(result, "EN_ADMIN");
                        String extrajaSperurl = JSONUtil.getText(result,
                                "jasper".equals(en_admin) ? "EN_URL" : "EN_EXTRAJASPERURL");
                        CommonUtil.setSharedPreferences(ct, "extrajaSperurl", extrajaSperurl);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "sessionId", sessionId);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "erp_username", erpaccount);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "erp_baseurl", url);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "erp_master", master);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "erp_commpany", name);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "erp_uu", uu);
                        CommonUtil.setSharedPreferences(WebViewLoadActivity.this, "erp_masterId", masterId);

                        loadUrl(paramurl, master);// 切换公司之后的具体页面url
                        Intent intent = new Intent("com.app.home.update");
                        intent.putExtra("falg", "ERP");
                        sendBroadcast(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ViewUtil.ShowMessageTitleAutoDismiss(WebViewLoadActivity.this, "参数异常！", 3000);
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };

    String title;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_demo01);
        isStartApp = false;
        webView = (PullToRefreshWebView) findViewById(R.id.webView_listview);
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setMax(100);

        Intent intent = this.getIntent();
        master = intent.getStringExtra("master");//参数账套
        String uu = intent.getStringExtra("uu");//参数账套
        String StartApp = intent.getStringExtra("isStartApp");//参数账套
        if (!StringUtil.isEmpty(StartApp)) {
            if (StartApp.equals("true")) {
                isStartApp = true;
            }
        }
        url = intent.getStringExtra("url");//参数账套
        String masterId = intent.getStringExtra("masterId");
        paramurl = url;
        title = "";// 动态改变文字显示
        title = intent.getStringExtra("p");
        if (title != null) {
            setTitle(title);
        }
        if ("UAS".equals(title)) {
            setTitle("UAS管理系统");
        }
        if ("Record".equals(title)) {
            setTitle("业务拜访记录");
        }
        webView.getRefreshableView().getSettings().setJavaScriptEnabled(true);
        webView.getRefreshableView().getSettings().setSupportZoom(true);
        webView.getRefreshableView().getSettings().setBuiltInZoomControls(true);
        webView.getRefreshableView().getSettings().setUseWideViewPort(true);
        webView.getRefreshableView().getSettings().setDomStorageEnabled(true);
        webView.getRefreshableView().getSettings().setLoadWithOverviewMode(true);
        webView.getRefreshableView().getSettings().setDefaultTextEncodingName("utf-8");
        webView.getRefreshableView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getRefreshableView().clearCache(true);
        webView.getRefreshableView().clearHistory();

        webView.getRefreshableView().setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!StringUtil.isEmpty(url) && url.contains("nodeId=")) {
                    WebViewLoadActivity.this.url = url;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (pb.getVisibility() == View.GONE) {
                    pb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (pb.getVisibility() == View.VISIBLE) {
                    pb.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        webView.getRefreshableView().setWebChromeClient(new WebChromeClient());
        if (!StringUtil.isEmpty(masterId) && !StringUtil.isEmpty(master)) {
            checkUUMaster(uu, masterId, master, url);// 推送，判别公司，账套，实现自动切换
        } else {
            loadUrl(url, master);// UU为空，默认本公司
        }
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            pb.setProgress(newProgress);
            if (newProgress == 100) {
                pb.setProgress(newProgress);
                pb.setVisibility(View.GONE);
                webView.onRefreshComplete();
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkUUMaster(String uu, String masterId, String master, String url) {
        Log.i("xinge", "onClik()触发 \nuu=" + uu + "\nmaster=" + master + "\nurl=" + paramurl);
        String erp_master = CommonUtil.getSharedPreferences(this, "erp_master");
        String erp_uu = CommonUtil.getSharedPreferences(this, "erp_uu");
        String erp_masterId = CommonUtil.getSharedPreferences(this, "erp_masterId");
        Log.i("PushParam", "本地 erp_master:" + erp_master);
        Log.i("PushParam", "本地 erp_uu:" + erp_uu);
        Log.i("PushParam", "本地 erp_masterId:" + erp_masterId);

        Log.i("PushParam", " 远程master:" + master);
        Log.i("PushParam", " 远程uu:" + uu);
        Log.i("PushParam", " 远程masterId:" + masterId);
        if (!StringUtil.isEmpty(erp_uu) && !StringUtil.isEmpty(uu) && !"null".equals(uu)) {
            if (!uu.equals(erp_uu)) { // uu不相同
                updateCompany(uu, master); // 切换公司
            } else {
                if (!master.equals(erp_master)) {// 切换账套
                    updateCompany(uu, master);
                } else {
                    loadUrl(paramurl, master);// 判断master相同
                }
            }
        } else {
            if (!StringUtil.isEmpty(erp_masterId) && !StringUtil.isEmpty(masterId)) {
                if (!masterId.equals(erp_masterId)) { // uu不相同
                    updateCompanyByMasterId(masterId, master); // 切换公司
                } else {
                    if (!master.equals(erp_master)) {// 切换账套
                        updateCompanyByMasterId(masterId, master);
                    } else {
                        loadUrl(paramurl, master);// 判断master相同
                    }
                }
            }


        }
    }


    private void loadUrl(String url, String master) {
        String password = CommonUtil.getSharedPreferences(this, "user_password");
        String urlload = CommonUtil.getAppBaseUrl(this) + "android/jprocessDeal.action";/** @注释：调转url */
        String d = null, t = null;
        if (password != null) {
            d = "" + new Date().getTime();
            Blowfish blowfish = new Blowfish("00" + d);
            t = blowfish.encryptString(password);
        }
        String urlitem = CommonUtil.getAppBaseUrl(this) + url;
        //登录信息参数
        String postData = "t=" + t + "&d=" + d + "" + "&u=" + CommonUtil.getSharedPreferences(this, "erp_username")
                + "&url=" + urlitem + "&master=" + master;
        if ("http://www.usoftchina.com/usoft/".equals(url)) {
            urlitem = url;
        }
        if ("已审批".equals(title) || "我发起的".equals(title)) {
            Log.i("WebView", "onResume url=" + urlload + postData);
            webView.getRefreshableView().postUrl(urlload, EncodingUtils.getBytes(postData, "base64"));
        } else {
            Log.i("WebView", "the url encoded : " + urlitem);
            Log.i("WebView", "onResume url=" + urlload + postData);
            webView.getRefreshableView().postUrl(urlload, EncodingUtils.getBytes(postData, "base64"));
        }
    }

    // 根据通知收到的公司enuu与账套master
    private void updateCompany(String uu, String master) {
        String json = CommonUtil.getSharedPreferences(this, "loginJson");
        System.out.println("login json=" + json);
        List<LoginEntity> logMsg = JSON.parseArray(json, LoginEntity.class);
        // FlexJsonUtil.fromJsonArray(json, LoginEntity.class);
        for (int i = 0; i < logMsg.size(); i++) {
            LoginEntity entity = logMsg.get(i);
            if ((String.valueOf(entity.getEnuu())).equals(uu)) {
                Log.i("WebView", "切换公司");
                String url = entity.getWebsite() + "mobile/login.action";
                Map<String, String> params = new HashMap<String, String>();
                String accountToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
                params.put("token", accountToken);
//                params.put("username", CommonUtil.getSharedPreferences(this, "user_phone"));
//                params.put("password", CommonUtil.getSharedPreferences(this, "user_password"));
                params.put("master", master);
                Log.i("wang", "url=" + url);
                Log.i("wang", "username=" + CommonUtil.getSharedPreferences(this, "user_phone"));
                Log.i("wang", "password=" + CommonUtil.getSharedPreferences(this, "user_password"));
                Log.i("wang", "master=" + master);
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("uu", uu);
                bundle.putString("master", master);
                bundle.putString("url", entity.getWebsite());
                bundle.putString("name", entity.getName());
                ViewUtil.startNetThread(url, params, handler, Constants.SUCCESS_ERP, message, bundle, "post");
            }
        }
    }


    // 根据通知收到的公司enuu与账套master
    private void updateCompanyByMasterId(String masterId, String master) {
        String json = CommonUtil.getSharedPreferences(this, "loginJson");
        System.out.println("login json=" + json);
        List<LoginEntity> logMsg = JSON.parseArray(json, LoginEntity.class);

        for (int i = 0; i < logMsg.size(); i++) {
            LoginEntity entity = logMsg.get(i);
            if ((String.valueOf(entity.getMasterId())).equals(masterId)) {
                Log.i("WebView", "切换公司");
                String url = entity.getWebsite() + "mobile/login.action";
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", CommonUtil.getSharedPreferences(this, "user_phone"));
                params.put("password", CommonUtil.getSharedPreferences(this, "user_password"));
                params.put("master", master);
                Log.i("wang", "url=" + url);
                Log.i("wang", "username=" + CommonUtil.getSharedPreferences(this, "user_phone"));
                Log.i("wang", "password=" + CommonUtil.getSharedPreferences(this, "user_password"));
                Log.i("wang", "master=" + master);

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("masterId", masterId);
                bundle.putString("master", master);
                bundle.putString("url", entity.getWebsite());
                bundle.putString("name", entity.getName());
                ViewUtil.startNetThread(url, params, handler, Constants.SUCCESS_ERP, message, bundle, "post");
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (isStartApp) {
            Intent intent = new Intent(WebViewLoadActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

}