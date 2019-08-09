package com.modular.appmessages.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.modular.appmessages.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 订阅号详情页
 * Created by RaoMeng on 2016/10/11.
 */
public class SubscribeDetailActivity extends BaseActivity {
    public static final String APP_CACHE_DIRNAME = "uuCache";
    private Button mApplyButton;
    private int mSubId = -1;
    private String mSubName;
    private int mSubStatus = -1;
    private int mIsApplied = -2;
    private Intent intent;
    private final static int REMOVE_MY_SUBSCRIPTION = 66;
    private final static int APPLY_DETAIL_SUBSCRIPTION = 55;
    private PopupWindow mCancleWindow;
    private String flag;

    private WebView mWebView;
    private String subscribeUrl;
    private ProgressBar mProgressBar;

    private Handler mHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APPLY_DETAIL_SUBSCRIPTION:
                    progressDialog.dismiss();
                    Log.d("applysubscription", msg.getData().getString("result"));
                    CommonUtil.imageToast(mContext, R.drawable.ic_apply_submit_success, "", 2000);
                    mSubStatus = 2;
                    mApplyButton.setText(getString(R.string.subscribe_requested));
                    mApplyButton.setEnabled(false);
                    Intent intent = new Intent();
                    intent.putExtra("status", mSubStatus);
                    setResult(9, intent);
                    break;
                case REMOVE_MY_SUBSCRIPTION:
                    progressDialog.dismiss();
                   ToastUtil.showToast(mContext, getString(R.string.subscribe_cancled));
                    Intent myIntent = new Intent();
                    setResult(44, myIntent);
                    mApplyButton.setText(getString(R.string.subscribe_detail_commit));
                    flag = "all";
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    ToastUtil.showToast(mContext, msg.getData().getString("result"));
                    break;
            }
        }
    };
    private String loginSession;
    public final String TIME = "SubscribeDetailActivity_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_detail);
        intent = getIntent();
        if (intent != null) {
            mSubId = intent.getIntExtra("subId", 0);
            mSubName = intent.getStringExtra("subTitle");
            flag = intent.getStringExtra("flag");
            if ("all".equals(flag)) {
                mSubStatus = intent.getIntExtra("subStatus", 0);
            } else if ("my".equals(flag)) {
                mIsApplied = intent.getIntExtra("isApplied", -1);
            }
        }
//        long oldtime = PreferenceUtils.getLong(this, TIME, System.currentTimeMillis());
//        if (((System.currentTimeMillis() - oldtime) / (1000 * 60)) > 20) {
//            login(this);
//        }
        if (TextUtils.isEmpty(mSubName)) {
            setTitle(getString(R.string.subscribe_detail_title));
        } else {
            setTitle(mSubName);
        }
        initViews();
        initEvents();
        initDatas();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_about) {
            new ShareAction(activity).setDisplayList(
                    SHARE_MEDIA.SINA,
                    SHARE_MEDIA.QQ,
                    SHARE_MEDIA.QZONE,
                    SHARE_MEDIA.WEIXIN,
                    SHARE_MEDIA.WEIXIN_CIRCLE,
                    SHARE_MEDIA.WEIXIN_FAVORITE,
                    SHARE_MEDIA.MORE)
                    .withTitle("UU互联订阅号")
                    .withText(mSubName)
                    .withMedia(new UMImage(activity, "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png"))
                    .withTargetUrl(subscribeUrl + "&sessionUser=" + CommonUtil.getSharedPreferences(mContext, "erp_username"))
                    .setCallback(CommonUtil.umShareListener)
                    .open();
        } else if (android.R.id.home == item.getItemId()) {
            onBackPressed();
        }
        return true;
    }

    private void initEvents() {
        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == null || mSubId == -1) {
                    return;
                }
                if ("all".equals(flag)) {
                    sendApplySubs();
                } else if ("my".equals(flag)) {
                    View cancleView = View.inflate(mContext, R.layout.pop_cancle_my_subscribe, null);
                    TextView cancleTextView = (TextView) cancleView.findViewById(R.id.cancel_subscribe_cancle_tv);
                    TextView contineTextView = (TextView) cancleView.findViewById(R.id.cancel_subscribe_contine_tv);

                    cancleTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendRemoveRequest();
                            closeWarningPopupWindow();
                        }
                    });

                    contineTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closeWarningPopupWindow();
                        }
                    });

                    mCancleWindow = new PopupWindow(cancleView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                    mCancleWindow.setAnimationStyle(R.style.MenuAnimationFade);
                    mCancleWindow.showAtLocation(mApplyButton, Gravity.BOTTOM, 0, 0);
                    DisplayUtil.backgroundAlpha(SubscribeDetailActivity.this, 0.5f);

                    mCancleWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            closeWarningPopupWindow();
                        }
                    });
                }
            }
        });
    }

    private void closeWarningPopupWindow() {
        if (mCancleWindow != null) {
            mCancleWindow.dismiss();
            mCancleWindow = null;
            DisplayUtil.backgroundAlpha(mContext, 1f);

        }

    }


    /**
     * 取消订阅
     */
    private void sendRemoveRequest() {
        progressDialog.show();
        String subsUrl = CommonUtil.getAppBaseUrl(mContext) + "common/charts/removeSubsMans.action";
        Map<String, Object> params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(mContext, "erp_username"));
        params.put("numIds", mSubId);

        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
        ViewUtil.httpSendRequest(mContext, subsUrl, params, mHandler, headers, REMOVE_MY_SUBSCRIPTION, null, null, "post");

    }

    /**
     * 申请订阅
     */
    private void sendApplySubs() {
        progressDialog.show();
        String applyUrl = CommonUtil.getAppBaseUrl(mContext) + "common/charts/vastAddSubsApply.action";
        Map<String, Object> params = new HashMap<>();
        params.put("ids", mSubId);
        params.put("caller", "VastAddSubsApply");

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("sessionUser", CommonUtil.getSharedPreferences(mContext, "erp_username"));
        ViewUtil.httpSendRequest(mContext, applyUrl, params, mHandler, headers, APPLY_DETAIL_SUBSCRIPTION, null, null, "post");
    }

    private void initDatas() {
        if (mSubStatus != -1) {
            if (mSubStatus == 1) {
                mApplyButton.setText(R.string.subscribe_confirmed);
                mApplyButton.setEnabled(false);
            } else if (mSubStatus == 2) {
                mApplyButton.setText(R.string.subscribe_requested);
                mApplyButton.setEnabled(false);
            } else if (mSubStatus == 3) {
                mApplyButton.setText(R.string.subscribe_detail_commit);
                mApplyButton.setEnabled(true);
            }
        }
        if (mIsApplied != -2) {
            if (mIsApplied == -1) {
                mApplyButton.setText(R.string.unsubscribe);
                mApplyButton.setEnabled(true);
            } else if (mIsApplied == 0) {
                mApplyButton.setText(R.string.not_unsubscribe_able);
                mApplyButton.setEnabled(false);
            }
        }
        if (subscribeUrl != null) {
            LinkedHashMap<String, String> headers = new LinkedHashMap<>();
            headers.put("sessionUser", CommonUtil.getSharedPreferences(mContext, "erp_username"));
            mWebView.loadUrl(subscribeUrl, headers);
        }
    }

    @SuppressLint("NewApi")
    private void initViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.subscribe_detail_pb);
        mApplyButton = (Button) findViewById(R.id.subscribe_detail_apply_btn);
        mWebView = (WebView) findViewById(R.id.subscribe_detail_wv);

        final WebSettings webSettings = mWebView.getSettings();
        //允许加载JavaScript
        webSettings.setJavaScriptEnabled(true);
        //网页自适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);

        //设置网页缓存模式
//        webSettings.setAppCacheEnabled(true);
        if (CommonUtil.isNetWorkConnected(this)) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
//        String cacheDirPath = getFilesDir().getAbsolutePath() + File.separator
//                + APP_CACHE_DIRNAME;
//        Log.i("cachePath", cacheDirPath);
//        // 设置数据库缓存路径
//        webSettings.setDatabasePath(cacheDirPath); // API 19 deprecated
//        // 设置Application caches缓存目录
//        webSettings.setAppCachePath(cacheDirPath);
//        webSettings.setAppCacheMaxSize(8 * 1024 * 1024);
//        webSettings.setAllowFileAccess(true);

//        webSettings.setSupportZoom(true);
//        webSettings.setBuiltInZoomControls(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);//提高渲染的优先级
        webSettings.setBlockNetworkImage(true);//把图片加载放在最后来加载
        if (Build.VERSION.SDK_INT >= 19) {//硬件加速器的使用
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (mProgressBar.getVisibility() == View.GONE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webSettings.setBlockNetworkImage(false);
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (mProgressBar.getVisibility() == View.GONE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                mProgressBar.setProgress(newProgress);
                if (mProgressBar.getVisibility() == View.VISIBLE && newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

//            @Override
//            public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
//                quotaUpdater.updateQuota(requiredStorage * 2);
//            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return true;
            }
        });

        subscribeUrl = CommonUtil.getAppBaseUrl(this) + "common/charts/mobilePreview.action?id="
                + mSubId + "&sessionId=" + CommonUtil.getSharedPreferences(ct, "sessionId")
                + "&sessionUser=" + CommonUtil.getSharedPreferences(ct, "erp_username")
                + "&master=" + CommonUtil.getSharedPreferences(ct, "erp_master");

        Log.d("subsurl: ", subscribeUrl);

        synCookies(mContext, subscribeUrl);
    }

    private int reCode = 201;

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
                        if (!jsonObject.isNull("sessionId")) {
                            CommonUtil.setSharedPreferences(SubscribeDetailActivity.this, "sessionId", jsonObject.getString("sessionId"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void login(Context context) {
        ViewUtil.ct = MyApplication.getInstance();
        ViewUtil.LoginERPTask(this, hander, reCode);
        PreferenceUtils.putLong(context, TIME, System.currentTimeMillis());
    }

    public void synCookies(Context context, String url) {
        if (MyApplication.cookieERP == null) {
            login(context);
            return;
        }
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        Cookie sessionCookie = MyApplication.cookieERP;
        String cookieStr = sessionCookie.getName() + "="
                + sessionCookie.getValue() + "; domain="
                + sessionCookie.getDomain();
        cookieManager.setCookie(url, cookieStr);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //防止WebView加载内存泄漏
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
    }

}
