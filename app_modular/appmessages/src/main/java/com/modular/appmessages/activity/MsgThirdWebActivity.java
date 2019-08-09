package com.modular.appmessages.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.common.LogUtil;
import com.common.data.TextUtil;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.modular.apputils.utils.OpenFilesUtils;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;
import com.modular.appmessages.R;
import com.modular.apputils.network.FileDownloader;
import com.uas.appworks.OA.erp.activity.CommonDocDetailsActivity;

import org.w3c.dom.Text;

import java.io.File;

/**
 * Created by Bitlike on 2018/1/16.
 */
public class MsgThirdWebActivity extends MessageWebActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
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

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Intent intent = getIntent();
                if (intent != null) {
                    String title = intent.getStringExtra(IntentUtils.KEY_TITLE);
                    String caller = intent.getStringExtra("caller");
                    int keyValue = intent.getIntExtra("keyValue", -1);
                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(caller) && keyValue != -1) {
                        startActivity(new Intent(ct, CommonDocDetailsActivity.class)
                                .putExtra("caller", caller)
                                .putExtra("keyValue", keyValue)
                                .putExtra("title", title)
                                .putExtra("device", false)
                                .putExtra("message", true)
                                .putExtra("status", "已审批"));
                    }


                }
                result.cancel();
                return true;
            }
        };
    }

    @Override
    protected boolean loadUrlViewClient(WebView view, String url) {
        LogUtil.i("shouldOverrideUrlLoading url=" + url);
        if (needCookie) {
            synCookies(ct, url);
        }
        String downLoadUrl = CommonUtil.getAppBaseUrl(ct) + "common/downloadbyId.action";
        if (!TextUtils.isEmpty(url) && url.startsWith(downLoadUrl)) {
            requestPermissionByLoadFile(url);
        } else {
            view.loadUrl(url);
        }
        return true;
    }


    private void requestPermissionByLoadFile(final String url) {
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new Runnable() {
            @Override
            public void run() {
                downFile(url);
            }
        }, new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(ct, R.string.not_system_permission);
            }
        });
    }

    private void downFile(String downloadUrl) {
        final CustomProgressDialog progressDialog = CustomProgressDialog.createDialog(ct);
        progressDialog.setTitile("正在预览");
        progressDialog.setMessage("正在生成附件预览，请勿关闭程序");
        if (progressDialog != null && ct != null) {
            progressDialog.show();
        }
        FileDownloader fileDownloader = new FileDownloader(downloadUrl, new FileDownloader.OnDownloaderListener() {
            @Override
            public void onProgress(long allProress, long progress) {
//                LogUtil.i("allProress=" + (allProress / 1000) + "k" + "progress=" + (progress / 1000) + "k");
            }

            @Override
            public void onSuccess(final File file) {
                LogUtil.i("onSuccess=" + (file == null ? "" : file.getPath()));
                if (ct != null) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    try {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new Runnable() {
                            @Override
                            public void run() {
                                OpenFilesUtils.openCommonFils(ct, file);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(ct, R.string.not_system_permission);
                            }
                        });
                    } catch (Exception e) {

                    }
                }

            }

            @Override
            public void onFailure(String exception) {
                LogUtil.i("onFailure=" + (exception == null ? "" : exception));
                if (ct != null) {
                    ToastUtil.showToast(ct, exception);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
        fileDownloader.download(0L);
    }

}
