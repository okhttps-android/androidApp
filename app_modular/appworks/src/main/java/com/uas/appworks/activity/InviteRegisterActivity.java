package com.uas.appworks.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.common.data.DateFormatUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.BasePresenter;
import com.core.utils.CommonUtil;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.uas.appworks.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.util.List;

/**
 * @author RaoMeng
 * @describe 邀请注册
 * @date 2018/1/31 16:04
 */

public class InviteRegisterActivity extends BaseMVPActivity {
    private static final int RESULT_FILE_CHOOSER = 0x11;
    private WebView mWebView;
    private String mRegisterUrl;
    private LinearLayout mSuccessLinearLayout;
    private android.webkit.ValueCallback<Uri> mUploadMessage;
    private android.webkit.ValueCallback<Uri[]> uploadMessageAboveL;

    @Override
    protected int getLayout() {
        return R.layout.activity_invite_register;
    }

    @Override
    protected void initView() {
        setTitle(R.string.str_work_invite_register);

        mWebView = $(R.id.invite_register_wv);
        mSuccessLinearLayout = $(R.id.invite_register_success);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (url.contains("https://mobile.ubtob.com:8443/openapp/")) {
                    mSuccessLinearLayout.setVisibility(View.VISIBLE);
                    return true;
                }
                return super.shouldOverrideUrlLoading(webView, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = valueCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                InviteRegisterActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), RESULT_FILE_CHOOSER);
                return true;
            }

            @Override
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                openFileChooser(valueCallback);
            }

            public void openFileChooser(ValueCallback uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                InviteRegisterActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), RESULT_FILE_CHOOSER);
            }

            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                openFileChooser(uploadMsg);
            }

        });
    }

    @Override
    protected void initData() {
        mRegisterUrl = Constants.ACCOUNT_CENTER_HOST + "register/enterpriseRegistration?inviteUserUU="
                + CommonUtil.getSharedPreferences(mContext, "b2b_uu")
                + "&inviteSpaceUU=" + CommonUtil.getEnuu(mContext)
                + "&invitationTime=" + DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMMdd");
        mWebView.loadUrl(mRegisterUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_FILE_CHOOSER) {
            if (null == mUploadMessage && null == uploadMessageAboveL) {
                return;
            }
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != RESULT_FILE_CHOOSER || uploadMessageAboveL == null) {
            return;
        }
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
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int visibility = mSuccessLinearLayout.getVisibility();
        if (keyCode == KeyEvent.KEYCODE_BACK && visibility == View.GONE) {
            new MaterialDialog.Builder(this).title(R.string.common_notice)
                    .content(R.string.register_unsuccessed_sure_to_exit)
                    .negativeText(R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            materialDialog.dismiss();
                        }
                    })
                    .positiveText(R.string.sure)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            InviteRegisterActivity.this.finish();
                        }
                    }).build().show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
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
                    SHARE_MEDIA.WEIXIN_FAVORITE)
                    .setShareboardclickCallback(new ShareBoardlistener() {
                        @Override
                        public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                            if (share_media.name().equals("WEIXIN")
                                    || share_media.name().equals("WEIXIN_CIRCLE")
                                    || share_media.name().equals("WEIXIN_FAVORITE")) {
                                if (!isWeixinAvilible(InviteRegisterActivity.this)) {
                                    Toast.makeText(InviteRegisterActivity.this, "您未安装微信", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            if (share_media.name().equals("QQ")
                                    || share_media.name().equals("QZONE")) {
                                if (!isQQClientAvailable(InviteRegisterActivity.this)) {
                                    Toast.makeText(InviteRegisterActivity.this, "您未安装QQ", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            new ShareAction(activity)
                                    .setPlatform(share_media)
                                    .withTitle("UU互联企业注册")
                                    .withText("发现一款超好用的办公助手，邀请您注册使用！")
                                    .withMedia(new UMImage(activity, "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png"))
                                    .withTargetUrl(mRegisterUrl)
                                    .setCallback(CommonUtil.umShareListener)
                                    .share();
                        }
                    })
                    .open();
        } else if (android.R.id.home == item.getItemId()) {
            int visibility = mSuccessLinearLayout.getVisibility();
            if (visibility == View.GONE) {
                new MaterialDialog.Builder(this).title(R.string.common_notice)
                        .content(R.string.register_unsuccessed_sure_to_exit)
                        .negativeText(R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                materialDialog.dismiss();
                            }
                        })
                        .positiveText(R.string.sure)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                InviteRegisterActivity.this.finish();
                            }
                        }).build().show();
            } else {
                onBackPressed();
            }
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

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }

}
