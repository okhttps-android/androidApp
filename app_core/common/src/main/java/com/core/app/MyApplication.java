package com.core.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.beefe.picker.PickerViewPackage;
import com.common.config.BaseApplication;
import com.common.config.BaseConfig;
import com.core.db.DBOpenHelper;
import com.core.db.DatabaseManager;
import com.core.model.ConfigBean;
import com.core.model.User;
import com.core.net.NetWorkObservable;
import com.core.net.ssl.AuthImageDownloader;
import com.core.net.utils.NetUtils;
import com.core.net.volley.FastVolley;
import com.core.service.InitializeService;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.yamill.orientation.OrientationPackage;
import com.hss01248.notifyutil.NotifyUtil;
import com.lidroid.xutils.HttpUtils;
import com.microsoft.codepush.react.CodePush;
import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.test.TestLocationOpinion;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

import org.apache.http.cookie.Cookie;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class MyApplication extends BaseApplication implements ReactApplication {

    private static MyApplication INSTANCE = null;
    private AppConfig mConfig;
    private FastVolley mFastVolley;
    public static Cookie cookieERP;
    public static RequestQueue mRequestQueue;
    public static HttpUtils mHttpUtils;

    /* 文件缓存的目录 */
    public String mAppDir;
    public String mPicturesDir;
    public String mVoicesDir;
    public String mVideosDir;
    public String mFilesDir;
    /***********************
     * 保存当前登陆用户的全局信息
     ***************/
    public String roomName;
    public String mAccessToken;
    public long mExpiresIn;
    public int mUserStatus;
    public boolean mUserStatusChecked = false;
    public User mLoginUser = new User();// 当前登陆的用户

    /*******************
     * 初始化图片加载
     **********************/
    // 显示的设置
    public static DisplayImageOptions mNormalImageOptions;
    public static DisplayImageOptions mAvatarRoundImageOptions;
    public static DisplayImageOptions mAvatarNormalImageOptions;

    /*********************
     * 提供网络全局监听
     ************************/
    private NetWorkObservable mNetWorkObservable;
    private final static int CWJ_HEAP_SIZE = 6* 1024* 1024 ;
    
    

    @Override
    public void initConfig() throws Exception {
        //后台服务初始化application
        InitializeService.initApplication(this);
        INSTANCE = this;
        //初始化RN
        SoLoader.init(this, /* native exopackage */ false);
        
        mRequestQueue = Volley.newRequestQueue(this);
        mHttpUtils = new HttpUtils();
        configHttpUtils();
        initUmengStatistics() ;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 初始化定位
                initLocation();
                // 初始化App目录
                initAppDir();
                // 初始化图片加载
                initImageLoader();
                NotifyUtil.init(getInstance());
                

            }
        }, 1);

        // 初始化网络监听
        mNetWorkObservable = new NetWorkObservable(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                MyActivityManager.getInstance().setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public void destory() {
        if (AppConfig.DEBUG) {
            Log.d(AppConfig.TAG, "MyApplication destory");
        }
        // 结束百度定位
        UasLocationHelper.getInstance().release();
        // 关闭网络状态的监听
        if (mNetWorkObservable != null) {
            mNetWorkObservable.release();
        }
        // 清除图片加载
//		ImageLoader.getInstance().destroy();
        releaseFastVolley();
        android.os.Process.killProcess(android.os.Process.myPid());
        UasLocationHelper.getInstance().release();
    }

    private void initLocation() {
        if (BaseConfig.isDebug()) {
            //开启模拟定位服务
            TestLocationOpinion mTestLocationOpinion = new TestLocationOpinion(this);
            mTestLocationOpinion.initLocation();
            mTestLocationOpinion.asynTaskUpdateCallBack();
        }
        UasLocationHelper.getInstance().initConfig(this);
    }

    public void initShareAPI() {
//        PlatformConfig.setWeixin("wx2539cc96bf158e00", "b057aa3e807ba7c505a2f03cc541bbf6");
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
        DatabaseManager.initializeInstance(DBOpenHelper.getInstance(this));
    }

    private void initUmengStatistics() {
        MobclickAgent.UMAnalyticsConfig config = new MobclickAgent.UMAnalyticsConfig(this, "57ea27bb67e58e088c003bbf",
                "baidu", MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.startWithConfigure(config);
       // MobclickAgent.setCatchUncaughtExceptions(false);
      
    }

    private void configHttpUtils() {
        mHttpUtils.configRequestThreadPoolSize(4);
        mHttpUtils.configRequestRetryCount(3);
        mHttpUtils.configResponseTextCharset("utf-8");
        mHttpUtils.configTimeout(10000);
    }

    public boolean isNetworkActive() {
        return NetUtils.isNetWorkConnected(BaseConfig.getContext());
    }

    public void registerNetWorkObserver(NetWorkObservable.NetWorkObserver observer) {
        if (mNetWorkObservable != null) {
            mNetWorkObservable.registerObserver(observer);
        }
    }

    public void unregisterNetWorkObserver(NetWorkObservable.NetWorkObserver observer) {
        if (mNetWorkObservable != null) {
            mNetWorkObservable.unregisterObserver(observer);
        }
    }

    private void initAppDir() {
        File file = getExternalFilesDir(null);
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
            mAppDir = file.getAbsolutePath();
        }

        file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
            mPicturesDir = file.getAbsolutePath();
        }

        file = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
            mVoicesDir = file.getAbsolutePath();
        }
        file = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
            mVideosDir = file.getAbsolutePath();
        }
        file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
            mFilesDir = file.getAbsolutePath();
        }
    }

    private void initImageLoader() {
        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 5);
        MemoryCacheAware<String, Bitmap> memoryCache;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            memoryCache = new LruMemoryCache(memoryCacheSize);
        } else {
            memoryCache = new LRULimitedMemoryCache(memoryCacheSize);
        }

        mNormalImageOptions = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisc(true)
//                .displayer(new RoundedBitmapDisplayer(10))
                .resetViewBeforeLoading(false)
                .showImageForEmptyUri(R.drawable.image_download_fail_icon)
                .showImageOnFail(R.drawable.image_download_fail_icon)
                .build();

        mAvatarRoundImageOptions = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565).
                        cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.avatar_round)
                .showImageOnFail(R.drawable.avatar_round)
                .showImageOnLoading(R.drawable.avatar_round)
                .build();

        mAvatarNormalImageOptions = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.avatar_normal)
                .showImageOnFail(R.drawable.avatar_normal)
                .showImageOnLoading(R.drawable.avatar_normal).build();

        if (mPicturesDir != null) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .defaultDisplayImageOptions(mNormalImageOptions)
                    // .denyCacheImageMultipleSizesInMemory()
                    .discCache(new TotalSizeLimitedDiscCache(new File(mPicturesDir), 50 * 1024 * 1024))
                    // 最多缓存50M的图片
                    .discCacheFileNameGenerator(new Md5FileNameGenerator())
                    .memoryCache(memoryCache)
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .imageDownloader(new AuthImageDownloader(this))
                    .denyCacheImageMultipleSizesInMemory()
                    .threadPoolSize(4)
                    .build();
            ImageLoader.getInstance().init(config);
        } else {
            //getCacheDir()
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .defaultDisplayImageOptions(mNormalImageOptions)
                    // .denyCacheImageMultipleSizesInMemory()
                    .discCache(new TotalSizeLimitedDiscCache(getCacheDir(), 50 * 1024 * 1024))
                    // 最多缓存50M的图片
                    .discCacheFileNameGenerator(new Md5FileNameGenerator())
                    .memoryCache(memoryCache)
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .imageDownloader(new AuthImageDownloader(this))
                    .denyCacheImageMultipleSizesInMemory()
                    .threadPoolSize(4)
                    .build();
            ImageLoader.getInstance().init(config);
        }


    }

    public void setConfig(AppConfig config) {
        mConfig = config;
    }

    public AppConfig getConfig() {
        if (mConfig == null) {
            mConfig = AppConfig.initConfig(getApplicationContext(), new ConfigBean());
        }
        return mConfig;
    }

    public FastVolley getFastVolley() {
        if (mFastVolley == null) {
            synchronized (MyApplication.class) {
                if (mFastVolley == null) {
                    mFastVolley = new FastVolley(this);
                    mFastVolley.start();
                }
            }
        }
        return mFastVolley;
    }

    private void releaseFastVolley() {
        if (mFastVolley != null) {
            mFastVolley.stop();
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public String getLoginUserId() {
        return (mLoginUser != null && !TextUtils.isEmpty(mLoginUser.getUserId())) ? mLoginUser.getUserId() : "";
    }

    public static MyApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }


    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new RNGestureHandlerPackage(),
                    new PickerViewPackage(),
                    new CodePush("",INSTANCE),
                    new OrientationPackage()
            );
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }
    };
}
