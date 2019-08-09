package com.modular.apputils.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.thread.ThreadPool;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.widget.CustomProgressDialog;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;


/**
 * Created by Bitlike on 2018/1/10.
 */

public abstract class BaseNetActivity extends SupportToolBarActivity {
    protected AppCompatActivity ct;
    private CustomProgressDialog progressDialog;
    protected HttpClient httpClient;


    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        preOnCreacte();
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        ct = this;
        progressDialog = CustomProgressDialog.createDialog(this);
        //重启之后恢复到之前的语言
        switchLanguage(PreferenceUtils.getString(this, "language", "rCN"));
        initHttpConfig();
        View layout = getLayoutView();
        if (layout == null) {
            int layoutId = getLayoutId();
            if (layoutId > 0) {
                setContentView(layoutId);
            }
        } else {
            setContentView(layout);
        }
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void preOnCreacte() {
    }

    protected abstract int getLayoutId();

    protected View getLayoutView() {
        return null;
    }

    protected abstract void init() throws Exception;

    protected abstract String getBaseUrl();


    /**************网络请求**********************/

    private void initHttpConfig() {
        String baseUrl = getBaseUrl();
        if (!StringUtil.isEmpty(baseUrl)) {
            httpClient = new HttpClient.Builder(baseUrl).isDebug(true)
                    .connectTimeout(5000)
                    .readTimeout(5000).build();
        }
    }


    protected void requestHttp(Parameter.Builder builder, final OnSmartHttpListener onHttpListener) {
        if (httpClient != null && builder != null) {
            HttpClient.Builder httpBuilder = new HttpClient.Builder();
            final Parameter parameter = builder.builder();
            httpBuilder.addParams(parameter.getParams())
                    .addHeaders(parameter.getHeaders())
                    .method(parameter.getMode())
                    .url(parameter.getUrl())
                    .isDebug(false);
            if (parameter.autoProgress()) {
                showProgress();
            }
            HttpClient mHttpClient = httpBuilder.build();
            if (parameter.showLog()) {
                if (parameter.isSaveLog()) {
                    deleteRequest();
                    saveRequest("url=" + httpClient.getBaseUrl() + mHttpClient.getBaseUrl() + "\n"
                            , "params=" + JSON.toJSONString(parameter.getParams()) + "\n"
                            , "headers=" + JSON.toJSONString(parameter.getHeaders()) + "\n");
                }
            }
            httpClient.Api().send(mHttpClient, new ResultSubscriber<>(new Result2Listener<Object>() {
                @Override
                public void onResponse(Object o) {
                    if (onHttpListener != null) try {
                        Tags tags = parameter.getTag();
                        int record = 0;
                        if (tags != null) {
                            record = tags.getRecord();
                        }
                        String message = o.toString();
                        if (parameter.showLog()) {
                            LogUtil.prinlnLongMsg("SmartHttp", "onResponse=" + message);
                            if (parameter.isSaveLog()) {
                                saveRequest("onResponse=" + message + "\n");
                            }
                        }
                        onHttpListener.onSuccess(record, message, tags);
                    } catch (Exception e) {
                        if (e != null && e.getMessage() != null) {
                            Log.i(TAG, e.getMessage());
                        }
                        e.printStackTrace();
                    }
                    dismissProgress();
                }

                @Override
                public void onFailure(Object t) {
                    if (onHttpListener != null) try {
                        Tags tags = parameter.getTag();
                        int record = 0;
                        if (tags != null) {
                            record = tags.getRecord();
                        }
                        String message = t.toString();
                        if (parameter.showLog()) {
                            LogUtil.prinlnLongMsg("SmartHttp", "onFailure=" + message);
                        }
                        onHttpListener.onFailure(record, message, tags);
                    } catch (Exception e) {
                        LogUtil.i("oooo=" + e.getMessage());
                        e.printStackTrace();
                    }
                    dismissProgress();
                }
            }));
        }
    }

    protected void requestCompanyHttp(Parameter.Builder builder, final OnSmartHttpListener onHttpListener) {
        if (builder != null) {
            String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
            String emCode = CommonUtil.getEmcode();
            builder.addSuperParams("sessionId", sessionId);
            builder.addSuperParams("master", CommonUtil.getMaster());
            builder.addSuperParams("sessionUser", emCode);
            builder.addSuperHeaders("sessionUser", emCode);
            builder.addSuperHeaders("Cookie", "JSESSIONID=" + sessionId);
            requestHttp(builder, onHttpListener);
        }
    }

    private void deleteRequest() {
        try {
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/uu/download";
            File file = new File(path);
            String fileName = "requestLog.txt";
            if (!file.exists()) {
                return;
            }
            file = new File(path + "/" + fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {

        }
    }

    private void saveRequest(final String... message) {
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new Runnable() {
            @Override
            public void run() {
                // 判断是否有SD卡
                ThreadPool.getThreadPool().addTask(new Runnable() {
                    @Override
                    public void run() {
                        if (message != null && message.length > 0) try {
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/uu/download";
                                //如果存在文件，不在下载
                                File file = new File(path);
                                String fileName = "requestLog.txt";
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                file = new File(path + "/" + fileName);
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                                for (String e : message) {
                                    out.write(e + "\n");
                                }
                                out.close();
                            }
                        } catch (Exception e) {

                        }
                    }
                });
            }
        }, null);
    }

    /*****************************显示进度框***************************************/
    protected void showProgress() {
        showProgress(true, "", "");
    }

    protected void showProgress(boolean cancelable) {
        showProgress(cancelable, "", "");
    }

    protected void showProgress(boolean cancelable, String message) {
        showProgress(cancelable, "", message);
    }

    protected void showProgress(boolean cancelable, String title, String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
            progressDialog.setMessage(title);
            progressDialog.setCanceledOnTouchOutside(cancelable);
            progressDialog.setCancelable(cancelable);
            progressDialog.show();
        }
    }

    protected void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /*********************切换语言***************************/
    protected void switchLanguage(String language) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals("en")) {
            config.locale = Locale.ENGLISH;
        } else if (language.equals("rCN")) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else if (language.equals("rTW")) {
            config.locale = Locale.TRADITIONAL_CHINESE;
        } else if (language.equals("sys")) {
            config.locale = Locale.getDefault();
        }
        resources.updateConfiguration(config, dm);
        PreferenceUtils.putString("language", language);
    }


    /*********************处理权限问题*********************/
    private SparseArray<Runnable> allowablePermissionRunnables;
    private SparseArray<Runnable> disallowablePermissionRunnables;
    private int permissionItem = 0;

    /***********************判断是否缺少权限**********************/
    protected boolean lacksPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(ct, permission) != PackageManager.PERMISSION_GRANTED;
        } else {
            return false;
        }
    }

    /**
     * 请求权限，先判断，如果没有权限就去请求
     * 尽量不将权限请求放在onResume 中，会出现不断循环请求
     *
     * @param permission           权限
     * @param allowableRunnable    当取得权限后执行操作，主线程
     * @param disallowableRunnable 当用户拒绝权限后执行操作，主线程
     */
    public void requestPermission(String permission, Runnable allowableRunnable, Runnable disallowableRunnable) {
        permissionItem++;
        if (allowableRunnable != null) {
            if (allowablePermissionRunnables == null) {
                allowablePermissionRunnables = new SparseArray<>();
            }
            allowablePermissionRunnables.put(permissionItem, allowableRunnable);
        }
        if (disallowableRunnable != null) {
            if (disallowablePermissionRunnables == null) {
                disallowablePermissionRunnables = new SparseArray<>();
            }
            disallowablePermissionRunnables.put(permissionItem, disallowableRunnable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //减少是否拥有权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {//没有获取到权限
//				if (!shouldShowRequestPermissionRationale(permission)) {
                //弹出对话框接收权限
                ActivityCompat.requestPermissions(ct, new String[]{permission}, permissionItem);
//				} else {
//					ToastUtil.showToast(ct, R.string.not_camera_permission);
//				}
            } else {
                if (allowableRunnable != null) {
                    allowableRunnable.run();
                }
            }
        } else {
            if (allowableRunnable != null) {
                allowableRunnable.run();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions != null) {
            for (String p : permissions) {
                LogUtil.i("permission=" + p);
            }
        }
        if (grantResults != null && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (allowablePermissionRunnables != null) {
                    Runnable allowRun = allowablePermissionRunnables.get(requestCode);
                    if (allowRun != null) {
                        allowRun.run();
                    }
                }

            } else {
                if (disallowablePermissionRunnables != null) {
                    Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
                    if (disallowRun != null) {
                        disallowRun.run();
                    }
                }

            }
        }
    }

}
