package com.core.service;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.common.LogUtil;
import com.common.preferences.PreferenceUtils;
import com.common.preferences.SharedUtil;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.db.DBOpenHelper;
import com.core.db.DatabaseManager;
import com.core.db.SQLiteHelper;
import com.facebook.stetho.Stetho;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.me.network.app.http.ssl.TrustAllCerts;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

/**
 * @author RaoMeng
 * @describe 初始化应用配置
 * @date 2018/1/5 16:55
 */

public class InitializeService extends IntentService {
    private static final String ACTION_INIT_APPLICATION = "initApplication";

    private static Context mContext;

    public InitializeService() {
        super("InitializeService");
    }

    public static void initApplication(Context context) {
        mContext = context;
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_APPLICATION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_INIT_APPLICATION.equals(action)) {
                initApplication();
            }
        }
    }

    private void initApplication() {
        Stetho.initializeWithDefaults(mContext);
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=5876dc99");
        //自定义闪退页面初始化
        CustomActivityOnCrash.install(mContext);
        //设置ErrorActivity在后台启动，默认为true
        CustomActivityOnCrash.setLaunchErrorActivityWhenInBackground(true);
        //设置展示详细错误信息，默认为true
        CustomActivityOnCrash.setShowErrorDetails(true);
        //启用APP Restart，默认为true
        CustomActivityOnCrash.setEnableAppRestart(true);
        //设置重新启动的activity
        //  CustomActivityOnCrash.setRestartActivityClass(SplashActivity.class);

        TrustAllCerts.allowAllSSL();//信任所有证书

        Config.REDIRECT_URL = "http://sns.whalecloud.com/sina2/callback";
        ZXingLibrary.initDisplayOpinion(mContext);
        SharedUtil.init(mContext);

        initShareAPI();

        initWxPay(mContext);
        //  CrashReport.initCrashReport(getApplicationContext(), "900050585", false);
        PreferenceUtils.putBoolean(mContext, Constants.IS_NOTIFICATION, false);//不进行通知

        //定位场景设置
        MobclickAgent.setScenarioType(mContext, MobclickAgent.EScenarioType.E_UM_NORMAL);
        initUmengStatistics();

        SDKInitializer.initialize(getApplicationContext());
        // 初始化数据库
        SQLiteHelper.copyDatabaseFile(getApplicationContext());

        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);

        if (AppConfig.DEBUG) {
            Log.d(AppConfig.TAG, "MyApplication onCreate");
        }
        if (AppConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }

        initErpDB();
    }

    public void initShareAPI() {
        PlatformConfig.setWeixin(Constants.WXPAY_APPID, Constants.WEIXIN_SECRET);
        PlatformConfig.setSinaWeibo("493318722", "7def652c3683603b08bab87d34b1f944");
        PlatformConfig.setQQZone("1104894295", "Nk8zMIz5YSqwRQ1F");
        Config.IsToastTip = false;
    }

    public void initWxPay(Context context) {
        IWXAPI wxApi = WXAPIFactory.createWXAPI(context, Constants.WXPAY_APPID, false);
        wxApi.registerApp(Constants.WXPAY_APPID);
    }

    private void initErpDB() {
        DatabaseManager.initializeInstance(DBOpenHelper.getInstance(mContext));
    }

    private void initUmengStatistics() {
        MobclickAgent.UMAnalyticsConfig config = new MobclickAgent.UMAnalyticsConfig(mContext, "57ea27bb67e58e088c003bbf",
                "baidu", MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.startWithConfigure(config);
        LogUtil.d("initUmengStatistics()");
//        if (!CommonUtil.isReleaseVersion()) {//当前为开发版本时候不需要上传友盟
           // MobclickAgent.setCatchUncaughtExceptions(false);
//        }
    }
}
