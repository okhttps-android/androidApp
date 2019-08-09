package com.core.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.app.ActionBackActivity;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.net.volley.FastVolley;
import com.core.utils.CommonUtil;
import com.core.utils.NotifyUtils;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;
import com.core.widget.SchedulePromptPop;
import com.core.widget.crouton.Crouton;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * 带网络请求的Activity继承
 *
 * @author Dean Tao
 */
public abstract class BaseActivity extends ActionBackActivity {
    public static final String PROMPT_ACTION = "PromptAction";
    public static final String CONTENT = "content";
    private FastVolley mFastVolley;
    private String HASHCODE;
    public CustomProgressDialog progressDialog;
    public Context ct;
    public Activity activity;


    /**
     * 提示广播
     */
    private BroadcastReceiver mPromptReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null
                    && BaseActivity.this != null
                    && intent.getAction() != null
                    && intent.getAction().equals(PROMPT_ACTION)) {
                String content = intent.getStringExtra(CONTENT);
                if (!StringUtil.isEmpty(content) && isTopActivity()) {
                    new SchedulePromptPop(BaseActivity.this, content).showPopupWindow();
                }
            }
        }
    };

    private boolean isTopActivity() {
        boolean isTop = false;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().contains(TAG)) {
            isTop = true;
        }
        return isTop;
    }

    public void preOnCreacte() {
    }

    public BaseActivity() {
        super();
        HASHCODE = Integer.toHexString(this.hashCode()) + "@";// 加上@符号，将拼在一起的两个HashCode分开
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        // getRootView().setFitsSystemWindows(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initStyle();
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 108) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //设置根布局的内边距
//            RelativeLayout relativeLayout = (RelativeLayout)
//                    findViewById(R.id.layout);
//            relativeLayout.setPadding(0, getActionBarHeight() + getStatusBarHeight(), 0,
//                    0);
        }
        preOnCreacte();
        ct = this;
        activity = this;
        mFastVolley = MyApplication.getInstance().getFastVolley();
        Intent intent = getIntent();
        if (intent == null || intent.getBooleanExtra("ORIENTATION_PORTRAIT", true)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        }
        progressDialog = CustomProgressDialog.createDialog(this);

        //重启之后恢复到之前的语言
        switchLanguage(PreferenceUtils.getString(this, "language", "rCN"));

        initFontScale(CommonUtil.getSharedPreferencesInt(MyApplication.getInstance(), "app_font_scale", 0));
        // SystemUtil.setSystemBarTint(this);
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PROMPT_ACTION);
        LocalBroadcastManager.getInstance(ct).registerReceiver(mPromptReceiver, filter);
    }

    private void unRegisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(ct).unregisterReceiver(mPromptReceiver);
        } catch (Exception e) {

        }
    }

    private void initStyle() {
        if (getIntent() == null || getIntent().getIntExtra("style", -1) == -1) return;
        try {
            int theme = getSharedPreferences("cons", MODE_PRIVATE).
                    getInt("theme", getIntent().getIntExtra("style", -1));
            setTheme(theme);
        } catch (Exception e) {
        }
    }

    public View getRootView() {
        return ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
    }


    public int getStatusBarHeight() {
        Class c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    // 获取ActionBar的高度
    public int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))// 如果资源是存在的、有效的
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }


    @Override
    protected void onDestroy() {
        // 取消所有HASHCODE包含该类名的request
        mFastVolley.cancelAll(HASHCODE);

        Crouton.cancelAllCroutons();
        unRegisterReceiver();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceUtils.putBoolean(this, Constants.IS_NOTIFICATION, false);//不进行通知
    }

    public void addDefaultRequest(Request<?> request) {
        mFastVolley.addDefaultRequest(HASHCODE, request);
    }

    public void addShortRequest(Request<?> request) {
        mFastVolley.addShortRequest(HASHCODE, request);
    }

    public void addRequest(Request<?> request, RetryPolicy retryPolicy) {
        mFastVolley.addRequest(HASHCODE, request, retryPolicy);
    }

    public void cancelAll(Object tag) {
        mFastVolley.cancelAll(HASHCODE, tag);
    }

    public void cancelAll() {
        mFastVolley.cancelAll(HASHCODE);
    }

    public boolean isNetworkActive() {
        return MyApplication.getInstance().isNetworkActive();
    }


    public void ToastMessage(String message) {
        NotifyUtils.ToastMessage(this, message);
    }


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
        LogUtil.d("MainActivity" + "当前语言版本模式：" + language);
        resources.updateConfiguration(config, dm);
        PreferenceUtils.putString("language", language);
    }

    public void initFontScale(int i) {
        Configuration configuration = getResources().getConfiguration();
        switch (i) {
            case 0:
                configuration.fontScale = 1;
                break;
            case 1:
                configuration.fontScale = 1.2f;
                break;
            case 2:
                configuration.fontScale = 1.3f;
                break;

        }
        //0.85 小, 1 标准大小, 1.15 大，1.3 超大 ，1.45 特大
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    /*处理权限问题*/
    private SparseArray<Runnable> allowablePermissionRunnables;
    private SparseArray<Runnable> disallowablePermissionRunnables;
    private int permissionItem = 0;

    /**
     * 判断是否缺少权限
     *
     * @param permission
     * @return true 缺少   false  以获取
     */
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
        LogUtil.i("requestPermission");
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
                LogUtil.i("ActivityCompat.requestPermissions=" + permission);
                ActivityCompat.requestPermissions(BaseActivity.this, new String[]{permission}, permissionItem);
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

    @SuppressWarnings("unchecked")
    public <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    protected void toast(String text) {
        ToastUtil.showToast(this, text);
    }

    protected void toast(int resId) {
        try {
            ToastUtil.showToast(this, resId);
        } catch (Exception e) {

        }
    }

    protected void toast(String text, ViewGroup viewGroup) {
        ToastUtil.showToast(this, text, viewGroup);
    }

    protected void toast(int resId, ViewGroup viewGroup) {
        try {
            ToastUtil.showToast(this, resId, viewGroup);
        } catch (Exception e) {

        }
    }

    /**
     * [页面跳转]
     *
     * @param clz
     */
    public void startActivity(Class<?> clz) {
        startActivity(clz, null);
    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * @param bundle
     * @param requestCode
     */
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }
}
