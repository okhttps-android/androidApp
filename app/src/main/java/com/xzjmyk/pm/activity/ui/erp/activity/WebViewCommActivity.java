package com.xzjmyk.pm.activity.ui.erp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.PermissionUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.ToastUtil;
import com.modular.appmessages.model.SubMessage;
import com.modular.login.activity.LoginActivity;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/5.
 */
public class WebViewCommActivity extends BaseActivity implements View.OnClickListener {
    public static final String TIME = "WebViewCommActivity_time";
    private final static int FILECHOOSER_RESULTCODE = 1;
    private final static int FLAG_SCAN_REQUEST = 31;

    private com.tencent.smtt.sdk.WebView webView;
    private ProgressBar pb;
    private ImageView back;
    private ImageView refresh;
    private String url;
    private TextView title;
    private TextView tck_icon;
    private boolean isStartApp = false;
    private boolean isCookie;
    private boolean isLoginSuccess = false;
    private int reCode = 201;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    private ArrayList<SubMessage> mSubscriptionMessages;
    private String mSubsAct;
    private int mPosition;

    private String mSubsurl;
    private TextView mPreTv;
    private TextView mNextTv;
    private List<Object> mReadSubs;
    private long mScanIndex = -1;

    private Handler hander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == reCode) {
                //登录成功
                String result = msg.getData().getString("result");
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = JSON.parseObject(result).getBoolean("success");

                    if (success) {
                        isLoginSuccess = true;
                        if (!jsonObject.isNull("sessionId")) {
                            CommonUtil.setSharedPreferences(WebViewCommActivity.this, "sessionId", jsonObject.getString("sessionId"));
                        }
                        doNextLoadURL();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void doNextLoadURL() {
        if (isCookie) {
            synCookies(this, url);
        }
        webView.loadUrl(url, getWebHeader());
    }

    private Map<String, String> getWebHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("clientType", "uas client");
        headers.put("Cookie", com.core.utils.CommonUtil.getErpCookie(ct));
        headers.put("master", com.core.utils.CommonUtil.getMaster());
        headers.put("sessionUser", com.core.utils.CommonUtil.getEmcode());
        headers.put("sessionId", com.core.utils.CommonUtil.getSharedPreferences(ct, "sessionId"));
        return headers;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_webview);
        webView = (com.tencent.smtt.sdk.WebView) findViewById(R.id.webView_listview);
        mPreTv = (TextView) findViewById(R.id.web_previous_tv);
        mNextTv = (TextView) findViewById(R.id.web_next_tv);
        mReadSubs = new ArrayList<>();
        isStartApp = false;
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setMax(100);
        url = intent.getStringExtra("url");
        mSubsurl = intent.getStringExtra("url");
        isCookie = intent.getBooleanExtra("cookie", false);
        String msg_title = intent.getStringExtra("title");
        if (!StringUtil.isEmpty(msg_title)) {
            setTitle(msg_title);
        }
        String StartApp = intent.getStringExtra("isStartApp");//参数账套
//        if (intent.getBooleanExtra("orientation",false)){
//            LogUtil.i("jinlai sssss");
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//        }
        mSubsAct = intent.getStringExtra("subsact");
        mPosition = intent.getIntExtra("position", -1);
        mSubscriptionMessages = (ArrayList<SubMessage>) intent.getSerializableExtra("subsdata");
        mReadSubs.add(mPosition);
        if (mSubsAct != null && mSubsAct.equals("subsDetail")) {
            url = mSubsurl + "?numId=" + mSubscriptionMessages.get(mPosition).getNumId()
                    + "&mainId=" + mSubscriptionMessages.get(mPosition).getInstanceId()
                    + "&insId=" + mSubscriptionMessages.get(mPosition).getId()
                    + "&title=" + mSubscriptionMessages.get(mPosition).getTitle()
                    + "&sessionId=" + CommonUtil.getSharedPreferences(ct, "sessionId");
            if (mSubscriptionMessages.size() <= 2) {
                mPreTv.setVisibility(View.GONE);
                mNextTv.setVisibility(View.GONE);
            } else if (mPosition == 0 || mSubscriptionMessages.get(mPosition - 1) == null || mSubscriptionMessages.get(mPosition - 1).getStatus() == 0) {
                mPreTv.setVisibility(View.GONE);
                mNextTv.setVisibility(View.VISIBLE);
            } else if (mPosition == mSubscriptionMessages.size() - 1) {
                mPreTv.setVisibility(View.VISIBLE);
                mNextTv.setVisibility(View.GONE);
            } else {
                mPreTv.setVisibility(View.VISIBLE);
                mNextTv.setVisibility(View.VISIBLE);
            }

            mPreTv.setOnClickListener(this);
            mNextTv.setOnClickListener(this);
        }

        if (!StringUtil.isEmpty(StartApp)) {
            if (StartApp.equals("true")) {
                isStartApp = true;
            }
        }
        String p = "";//动态改变文字显示
        try {
            p = intent.getStringExtra("p");
        } catch (Exception e) {
        }
        if (p != null) {
            setTitle(p);
        }
        if (StringUtil.isEmpty(url)) {//为空时默认为b2b平台的
            url = "http://www.baidu.com";
        }
        long oldtime = PreferenceUtils.getLong(this, TIME, System.currentTimeMillis());
        if (((System.currentTimeMillis() - oldtime) / (1000 * 60)) > 10) {
            if (isCookie) {
                login(this);
            }
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
        //  webView.getRefreshableView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); //设置 缓存模式
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSaveFormData(true);

        // 修改ua使得web端正确判断
        webView.addJavascriptInterface(new JSWebView(), "JSWebView");
        //在JSWebView类里实现javascript想调用的方法，并将其实例化传入webview, "JSWebView"这个字串告诉javascript调用哪个实例的方法

        setThirdPartyCookiesEnabled(true);

        String ua = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(ua + " uasClient");

        if (isCookie) {
            synCookies(this, url);
        }
        Log.d("webUrl", url);
        webView.loadUrl(url, getWebHeader());
        webView.setWebChromeClient(new ChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (isCookie && !isLoginSuccess) {
                    ViewUtil.ct = WebViewCommActivity.this;
                    ViewUtil.LoginERPTask(WebViewCommActivity.this, hander, reCode);
                }

                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
                if (isCookie && !isLoginSuccess && webResourceResponse.getStatusCode() == 500) {
                    ToastUtil.showToast(ct, "登录信息已过期，正在重新登录");
                    ViewUtil.ct = WebViewCommActivity.this;
                    ViewUtil.LoginERPTask(WebViewCommActivity.this, hander, reCode);
                }
                super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("webUrl", url);
                if (!TextUtils.isEmpty(url)) {
                    if (url.contains("https://account.ubtob.com/sso/login") ||
//                            url.equals("https://sso.ubtob.com/") ||
                            url.equals("http://surecloseweb.com")) {
                        Intent loginIntent = new Intent();
                        loginIntent.setClass(WebViewCommActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        WebViewCommActivity.this.finish();
                    } else {
                        if (isCookie) {
                            synCookies(WebViewCommActivity.this, url);
                            view.loadUrl(url, getWebHeader());
                        } else {
//                        ViewUtil.ShowMessageTitleAutoDismiss(WebViewCommActivity.this,
//                                "您的登录会话过期！请重新登录！",1000);
                            view.loadUrl(url, getWebHeader());
                        }
                    }
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                String CookieStr = cookieManager.getCookie(url);
                LogUtil.d(TAG, "Cookies = " + CookieStr);
                super.onPageFinished(view, url);
            }
        });
    }

    private static final String TAG = "WebViewCommActivity";

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.web_previous_tv:
                mPosition--;
                if (mSubscriptionMessages.get(mPosition).getStatus() == 0) {
                    mPosition--;
                }
                break;
            case R.id.web_next_tv:
                mPosition++;
                if (mSubscriptionMessages.get(mPosition).getStatus() == 0) mPosition++;
                break;
        }
        if (mPosition < 0 || mPosition > mSubscriptionMessages.size()) return;
        if (mSubscriptionMessages != null) {
            mReadSubs.add(mPosition);
            setTitle(mSubscriptionMessages.get(mPosition).getTitle());
            if (mPosition == 0) {
                mPreTv.setVisibility(View.GONE);
                mNextTv.setVisibility(View.VISIBLE);
            } else if (mPosition == mSubscriptionMessages.size() - 1) {
                mPreTv.setVisibility(View.VISIBLE);
                mNextTv.setVisibility(View.GONE);
            } else if (mPosition == mSubscriptionMessages.size() - 2 && mSubscriptionMessages.get(mPosition + 1).getStatus() == 0) {
                mPreTv.setVisibility(View.VISIBLE);
                mNextTv.setVisibility(View.GONE);
            } else if (mPosition == 1 && mSubscriptionMessages.get(0).getStatus() == 0) {
                mPreTv.setVisibility(View.GONE);
                mNextTv.setVisibility(View.VISIBLE);
            } else {
                mPreTv.setVisibility(View.VISIBLE);
                mNextTv.setVisibility(View.VISIBLE);
            }

            url = mSubsurl + "?numId=" + mSubscriptionMessages.get(mPosition).getNumId()
                    + "&mainId=" + mSubscriptionMessages.get(mPosition).getInstanceId()
                    + "&insId=" + mSubscriptionMessages.get(mPosition).getId()
                    + "&title=" + mSubscriptionMessages.get(mPosition).getTitle()
                    + "&sessionId=" + CommonUtil.getSharedPreferences(ct, "sessionId");

            webView.loadUrl(url, getWebHeader());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mReadSubs.isEmpty()) {
            List<Integer> singleElement = (List) CommonUtil.getSingleElement(mReadSubs);
            ArrayList<Integer> readIntegers = new ArrayList<>();
            readIntegers.addAll(singleElement);
            Intent intent = new Intent();
            intent.putIntegerArrayListExtra("readsubs", readIntegers);
            setResult(22, intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class ChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            pb.setProgress(newProgress);
            if (newProgress == 100) {
                pb.setProgress(newProgress);
                pb.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }

        //The undocumented magic method override
        //Eclipse will swear at you if you try to put @Override here  
        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            WebViewCommActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            WebViewCommActivity.this.startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FILECHOOSER_RESULTCODE);
        }

        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            WebViewCommActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), WebViewCommActivity.FILECHOOSER_RESULTCODE);

        }


        // For Android 5.0+
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            WebViewCommActivity.this.startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FILECHOOSER_RESULTCODE);
            return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else if (requestCode == FLAG_SCAN_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            int resultType = extras.getInt(CodeUtils.RESULT_TYPE);
            if (resultType == CodeUtils.RESULT_SUCCESS) {
                String scanResult = extras.getString(CodeUtils.RESULT_STRING);
                scanResult = mScanIndex + "," + scanResult;
                if (Build.VERSION.SDK_INT < 18) {
                    webView.loadUrl("javascript:scanCompleted('" + scanResult + "')");
                } else {
                    webView.evaluateJavascript("javascript:scanCompleted('" + scanResult + "')", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {

                        }
                    });
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILECHOOSER_RESULTCODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    /**
     * 同步一下cookie
     */
    public void synCookies(Context context, String url) {
        if (MyApplication.cookieERP == null) {
            login(context);
            return;
        }
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        LogUtil.prinlnLongMsg("cookieERP", MyApplication.cookieERP.toString());
        Cookie sessionCookie = MyApplication.cookieERP;
        String cookieStr = sessionCookie.getName() + "="
                + sessionCookie.getValue() + "; domain="
                + sessionCookie.getDomain() + "; path="
                + sessionCookie.getPath();
        cookieManager.setCookie(url, cookieStr);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    public void clearCookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();//移除
        cookieManager.removeAllCookie();
        webView.clearHistory();
        webView.clearCache(true);
    }

    private void login(Context context) {
        ViewUtil.ct = MyApplication.getInstance();
        ViewUtil.LoginERPTask(this, hander, reCode);
        PreferenceUtils.putLong(context, TIME, System.currentTimeMillis());
    }

    @SuppressLint("NewApi")
    public void setThirdPartyCookiesEnabled(final boolean enabled) {
        if (Build.VERSION.SDK_INT >= 21) {
            com.tencent.smtt.sdk.CookieManager.getInstance().setAcceptThirdPartyCookies(webView, enabled);
        } else {
            com.tencent.smtt.sdk.CookieManager.getInstance().setAcceptCookie(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mReadSubs.isEmpty()) {
            List<Integer> singleElement = (List) CommonUtil.getSingleElement(mReadSubs);
            ArrayList<Integer> readIntegers = new ArrayList<>();
            readIntegers.addAll(singleElement);
            Intent intent = new Intent();
            intent.putIntegerArrayListExtra("readsubs", readIntegers);
            setResult(22, intent);
        }
        if (isStartApp) {
            Intent intent = new Intent(WebViewCommActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_about:
                new ShareAction(activity).setDisplayList(
                        SHARE_MEDIA.SINA,
                        SHARE_MEDIA.QQ,
                        SHARE_MEDIA.QZONE,
                        SHARE_MEDIA.WEIXIN,
                        SHARE_MEDIA.WEIXIN_CIRCLE,
                        SHARE_MEDIA.WEIXIN_FAVORITE,
                        SHARE_MEDIA.MORE)
                        .setShareboardclickCallback(new ShareBoardlistener() {
                            @Override
                            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                                if (share_media.name().equals("WEIXIN")
                                        || share_media.name().equals("WEIXIN_CIRCLE")
                                        || share_media.name().equals("WEIXIN_FAVORITE")) {
                                    if (!isWeixinAvilible(WebViewCommActivity.this)) {
                                        Toast.makeText(WebViewCommActivity.this, "您未安装微信", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                if (share_media.name().equals("QQ")
                                        || share_media.name().equals("QZONE")) {
                                    if (!isQQClientAvailable(WebViewCommActivity.this)) {
                                        Toast.makeText(WebViewCommActivity.this, "您未安装QQ", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                new ShareAction(activity)
                                        .setPlatform(share_media)
                                        .withTitle(getToolBarTitle().toString())
                                        .withText("UU互联 Android客户端" + SystemUtil.getVersionName(mContext))
                                        .withMedia(new UMImage(activity, "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png"))
                                        .withTargetUrl(url)
                                        .setCallback(CommonUtil.umShareListener)
                                        .share();
                            }
                        })
                        .open();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    public boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断qq是否可用
     *
     * @param context
     * @return
     */
    public boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 同步一下cookie
     */
    public static void synComCookies(Context context, String url) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除  
        cookieManager.setCookie(url, com.core.utils.CommonUtil.getB2BSession(MyApplication.getInstance()));//cookies是在HttpClient中获得的cookie  
        CookieSyncManager.getInstance().sync();
    }


    public class JSWebView {

        @JavascriptInterface
        public void closeWebWindow() {
            finish();
        }

        @JavascriptInterface
        public void openScan(long index) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] permissions = new String[]{Manifest.permission.CAMERA};
                    if (PermissionUtil.lacksPermissions(ct, permissions)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(permissions, PermissionUtil.DEFAULT_REQUEST);
                        }
                    } else {
                        mScanIndex = index;
                        Intent intent = new Intent(mContext, CaptureActivity.class);
                        startActivityForResult(intent, FLAG_SCAN_REQUEST);
                    }
                }
            });
        }
    }

}
