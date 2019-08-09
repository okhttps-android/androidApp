package com.modular.apputils.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.me.network.app.http.ssl.TrustAllCerts;
import com.modular.apputils.R;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.TbsReaderView.ReaderCallback;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.OkHttpClient;

public class SimpleWebActivity extends OABaseActivity implements ReaderCallback {
    private static final String TAG = "SimpleWebActivity";
    private WebView webView;
    private TbsReaderView mTbsReaderView;
    private String filepath;
    private OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_web);

        webView = findViewById(R.id.mSimpleWebView);
        mTbsReaderView = new TbsReaderView(this, this);
        RelativeLayout rootRl = findViewById(R.id.rl_root);
        rootRl.addView(mTbsReaderView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setTextZoom(100);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        setTitle(intent.getStringExtra("p"));
        filepath = intent.getStringExtra("filepath");
        if (!TextUtils.isEmpty(filepath)) {
            if (isLocalExist()) {
                openFileByTBS();
            }
            return;
        }

        setThirdPartyCookiesEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith("sms:") || url.startsWith(WebView.SCHEME_MAILTO)) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException ignored) {
                    }
                    return true;
                } else if (url.contains("jsps/oa/persontask/workDaily/addWorkDaily.jsp")) {
                    LogUtil.d(TAG, "event url=" + url);
                    ToastUtil.showToast(mContext, "调转日报界面");
                    return true;
                }
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        LogUtil.d(TAG, "url=" + url);
        webView.setWebChromeClient(mWebChromeClient);
        webView.loadUrl(url);
    }

    private boolean isLocalExist() {
        return getLocalFile().exists();
    }

    private File getLocalFile() {
        return new File(filepath);
    }

    private void openFileByTBS() {
        String fileName = parseFormat(filepath);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filepath);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        boolean result = mTbsReaderView.preOpen(fileName, false);
        if (result) {
            mTbsReaderView.openFile(bundle);
        }

    }

    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    //企业uu为空，则取附近账套和子帐套的
    public void initBusinessToken() {
        LogUtil.d(TAG, CommonUtil.getSharedPreferences(this, "erp_uu"));
        HttpClient httpClient = new HttpClient.Builder("https://account.ubtob.com/").build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("api/user/mobile/getToken")
                .add("appId", "b2b")
                .add("spaceDialectUID", CommonUtil.getSharedPreferences(this, "erp_uu"))
                .add("uid", MyApplication.getInstance().mLoginUser.getTelephone())
                .method(Method.GET)
                .build(), new ResultSubscriber<Object>(new ResultListener<Object>() {

            @Override
            public void onResponse(Object o) {
                try {
                    LogUtil.d(TAG, o.toString());
                    String token = JSON.parseObject(o.toString()).getString("content");

                    String url = "https://sso.ubtob.com/sso/login/proxy?appId=b2b&returnURL=https://mall.usoftchina.com/&token=" + token + "&baseURL=https://mall.usoftchina.com//login/other&isLoginAll=false";
                    webView.loadUrl(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

//    public static void synCookies(Context context, String url, String cookies) {
//        CookieSyncManager.createInstance(context);
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        cookieManager.removeSessionCookie();//移除  
//        LogUtil.d(TAG, cookies);
//        cookieManager.setCookie(url, cookies);
//        CookieSyncManager.getInstance().sync();
//    }


    @SuppressWarnings("static-method")
//    public void setCookiesEnabled(final boolean enabled) {
//        CookieManager.getInstance().setAcceptCookie(enabled);
//    }

    @SuppressLint("NewApi")
    public void setThirdPartyCookiesEnabled(final boolean enabled) {
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, enabled);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent("com.modular.main.MainActivity"));
        finish();
        overridePendingTransition(com.core.app.R.anim.anim_activity_back_in, com.core.app.R.anim.anim_activity_back_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }


    //由于优软商城无法兼容弹出窗体，现在添加
    private WebChromeClient mWebChromeClient = new WebChromeClient() {


        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        // For Lollipop 5.0+ Devices
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }
    };
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 2;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(getBaseContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }

}
