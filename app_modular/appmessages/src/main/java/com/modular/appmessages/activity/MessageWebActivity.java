package com.modular.appmessages.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ZoomButtonsController;

import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.modular.appmessages.R;
import com.modular.apputils.widget.VeriftyDialog;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.webempty.EmptyRefreshLayout;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import org.apache.http.cookie.Cookie;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MessageWebActivity extends BaseActivity {


    protected WebView webViewRefresh;
    protected EmptyRefreshLayout mEmptyRefreshLayout;
    protected ProgressBar progressBar;
    protected String url;
    protected boolean needCookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_web);
        webViewRefresh = (WebView) findViewById(R.id.webView);
        mEmptyRefreshLayout = findViewById(R.id.mEmptyRefreshLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent != null) {
            mEmptyRefreshLayout.setEnablePullDown(intent.getBooleanExtra("EnablePullDown", true));
        }
        mEmptyRefreshLayout.setEnabledPullUp(false);
        initView();
        initData();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webViewRefresh.canGoBack()) {
            webViewRefresh.goBack();
            return true;
        } else {
            setResult();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setResult() {
        setResult(0x21);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent() != null && getIntent().getBooleanExtra(IntentUtils.KEY_NEER_SHARE, false)) {
            getMenuInflater().inflate(R.menu.menu_about, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.app_about == item.getItemId()) {
            String title = getToolBarTitle().toString();
            String content = null;
            String imageUrl = null;
            Intent intent = getIntent();
            if (intent != null) {
                content = intent.getStringExtra(IntentUtils.KEY_SHARE_CONTENT);
                imageUrl = intent.getStringExtra(IntentUtils.KEY_SHARE_IMAGE);
            }
            if (content == null) {
                content = "UU互联 Android客户端" + SystemUtil.getVersionName(mContext);
                imageUrl = "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png";
            }
            LogUtil.i("url=" + url);
            LogUtil.i("content=" + content);
            LogUtil.i("imageUrl=" + imageUrl);
            new ShareAction(activity).setDisplayList(
                    SHARE_MEDIA.SINA,
                    SHARE_MEDIA.QQ,
                    SHARE_MEDIA.QZONE,
                    SHARE_MEDIA.WEIXIN,
                    SHARE_MEDIA.WEIXIN_CIRCLE,
                    SHARE_MEDIA.WEIXIN_FAVORITE,
                    SHARE_MEDIA.MORE)
                    .withTitle(title)
                    .withText(content)
                    .withMedia(new UMImage(activity, imageUrl))
                    .withTargetUrl(url)
                    .setCallback(CommonUtil.umShareListener)
                    .open();
        } else if (item.getItemId() == android.R.id.home) {
            setResult();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initView() {
        mEmptyRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                mEmptyRefreshLayout.stopRefresh();
            }

            @Override
            public void onLoadMore() {

            }
        });
        webViewRefresh.getSettings().setJavaScriptEnabled(true);
        webViewRefresh.getSettings().setSupportZoom(true);
        webViewRefresh.getSettings().setBuiltInZoomControls(true);
        webViewRefresh.getSettings().setUseWideViewPort(true);
        webViewRefresh.getSettings().setDomStorageEnabled(true);
        webViewRefresh.getSettings().setLoadWithOverviewMode(true);
        webViewRefresh.getSettings().setDefaultTextEncodingName("utf-8");
        webViewRefresh.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webViewRefresh.getSettings().setDisplayZoomControls(false);
        } else {
            setZoomControlGone();
        }
        webViewRefresh.clearCache(true);
        webViewRefresh.clearHistory();
        webViewRefresh.setWebViewClient(getWebViewClient());
        webViewRefresh.setWebChromeClient(getWebChromeClient());
    }


    protected WebChromeClient getWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setProgress(newProgress);
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        };
    }

    @Override
    public void onPause() {
        if (mVeriftyDialog != null) {
            mVeriftyDialog.dismiss();
            mVeriftyDialog = null;
        }
        super.onPause();
    }
    private VeriftyDialog mVeriftyDialog;
    private void showPlyEnd() {
        mVeriftyDialog = new VeriftyDialog.Builder(this)
                .setCanceledOnTouchOutside(false)
                .setContent("微信支付需要在浏览器上打开，是否确认跳转？")
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                        if (clickSure){
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    }
                });
    }
    protected boolean loadUrlViewClient(WebView view, String url) {
        if (needCookie) {
            synCookies(ct, url);
        }
        try {
            if (url.startsWith("baidu://") ||
                    url.startsWith("weixin://") //微信
                    || url.startsWith("alipays://") //支付宝
                    || url.startsWith("mailto://") //邮件
                    || url.startsWith("tel://")//电话
                    || url.startsWith("dianping://")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } else if (url.contains("https://wx.tenpay.com")) {
                showPlyEnd();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        view.loadUrl(url);
        return true;
    }

    protected WebViewClient getWebViewClient() {
        return new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return loadUrlViewClient(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogUtil.i("onPageStarted url=" + url);
                if (progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (needCookie) {
                    ToastUtil.showToast(ct, "Cookie已经过期，请重新登陆");
                    if (loginHandler == null) {
                        login();
                    }
                }
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        };
    }

    public void setZoomControlGone() {
        Class classType;
        Field field;
        try {
            classType = WebView.class;
            field = classType.getDeclaredField("mZoomButtonsController");
            field.setAccessible(true);
            ZoomButtonsController mZoomButtonsController = new ZoomButtonsController(webViewRefresh);
            mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
            try {
                field.set(webViewRefresh, mZoomButtonsController);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            url = intent.getStringExtra(IntentUtils.KEY_URL);
            String title = intent.getStringExtra(IntentUtils.KEY_TITLE);
            needCookie = intent.getBooleanExtra(IntentUtils.KEY_NEER_COOKIE, false);
            if (title != null) {
                setTitle(title);
            }
            if (needCookie) {
                synCookies(ct, url);
            }
        }
        loadData();
    }

    private void loadData() {
        if (url != null) {
            webViewRefresh.loadUrl(url);
        }

    }


    /**
     * 同步一下cookie
     */
    public void synCookies(Context context, String url) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        Cookie sessionCookie = MyApplication.cookieERP;
        if (sessionCookie != null) {
            String cookieStr = sessionCookie.getName() + "="
                    + sessionCookie.getValue() + "; domain="
                    + sessionCookie.getDomain() + "; path="
                    + sessionCookie.getPath();
            cookieManager.setCookie(url, cookieStr);//cookies是在HttpClient中获得的cookie
            CookieSyncManager.getInstance().sync();
        }
    }


    private Handler loginHandler = null;

    private void login() {
        if (loginHandler == null) {
            loginHandler = newLoginHandler();
        }
        ViewUtil.ct = MyApplication.getInstance();
        ViewUtil.LoginERPTask(this, loginHandler, 11);
    }


    private Handler newLoginHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 11) {
                    //登录成功
                    String result = msg.getData().getString("result");
                    if (JSONUtil.getBoolean(result, "success")) {
                        doNextLoadURL();
                    }
                }
            }
        };
    }

    private void doNextLoadURL() {
        if (needCookie) {
            synCookies(this, url);
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("clientType", "uas client");
        webViewRefresh.loadUrl(url, headers);
    }

}
