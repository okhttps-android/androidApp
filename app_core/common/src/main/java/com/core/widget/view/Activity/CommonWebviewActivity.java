package com.core.widget.view.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.common.file.DownloadUtil;
import com.core.app.R;

/**
 * 公共网页打开页面
 * Created by RaoMeng on 2016/9/14.
 */
public class CommonWebviewActivity extends Activity {
    private WebView mWebView;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_webview);
        mWebView = (WebView) findViewById(R.id.webview);

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
                /*view.loadUrl(url);
                return true;*/
                return false;
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.d("weburl", url);
                String fileName = url.substring(url.lastIndexOf("/") + 1);

                // fileName=fileName.split("&")[3].split("=")[1];

                new AlertDialog.Builder(CommonWebviewActivity.this)
                        .setTitle("提示").setMessage("确定下载文件" + fileName + "吗?")
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DownloadUtil.DownloadFile(CommonWebviewActivity.this, url, "/sdcard/uu");

                            }
                        }).create().show();

            }
        });

        Intent intent = getIntent();
        mUrl = intent.getStringExtra("scan_url");

        mWebView.loadUrl(mUrl);
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
