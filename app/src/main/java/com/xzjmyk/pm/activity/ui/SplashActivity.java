package com.xzjmyk.pm.activity.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.file.FileUtils;
import com.common.hmac.Md5Util;
import com.common.preferences.PreferenceUtils;
import com.common.system.SystemUtil;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.UserDao;
import com.core.model.ConfigBean;
import com.core.model.LoginRegisterResult;
import com.core.model.User;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.StatusBarUtil;
import com.core.utils.TimeUtils;
import com.core.utils.helper.LoginHelper;
import com.core.utils.sp.UserSp;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.service.CommonIntentService;
import com.modular.login.activity.LoginActivity;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.ADActivity;
import com.xzjmyk.pm.activity.ui.me.TimeStatisticsActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 启动页
 *
 * @author Dean Tao
 * @version 1.0
 */
public class SplashActivity extends BaseActivity {
    private final int TIMER_SPLASH_COUNDOWN = 0x11;

    private RelativeLayout mSelectLv;
    private GifImageView mGifImageView;
    private TextView mSkipTextView;

    private final String IS_FIRST = "IS_FIRST";
    boolean isJumpable = false, isCountDown = false;

    private boolean mConfigReady = false;// 配置获取成功
    private GifDrawable mGifDrawable;
    private boolean mAnimationCompleted = false;
    private String mSplshUrl;
    private long mStartTime;
    private int mCountDown = 5;
    private Timer mTimer;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIMER_SPLASH_COUNDOWN) {
                if (mCountDown > 1) {
                    isCountDown = false;
                    mCountDown--;
                    mSkipTextView.setText("点击跳过\t" + mCountDown);
                } else {
                    isCountDown = true;
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    jumpImmediately();
                }
            }
        }
    };

    private void jumpImmediately() {
        int userStatus = LoginHelper.prepareUser(mContext);
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL://5
            case LoginHelper.STATUS_USER_NO_UPDATE://3
                trun2NextPage(1);
                break;
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE://2
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                trun2NextPage(0);
                break;
            case LoginHelper.STATUS_NO_USER://0
            default:
                stay();
                return;// must return
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        StatusBarUtil.immersive(this, 0x00000000, 0.0f);
        mSkipTextView = findViewById(R.id.splash_skip_tv);

        mStartTime = System.currentTimeMillis();

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                jump();
            }
        }, 2000);*/
        mGifImageView = findViewById(R.id.splash_gif_view);
        showSplash();
        mSelectLv = (RelativeLayout) findViewById(R.id.select_lv);
        mSelectLv.setVisibility(View.INVISIBLE);
        initConfig();// 初始化配置
        updateAccountToken();//更新账户中心token
        requestSplash();

        mSkipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimer != null) {
                    mTimer.cancel();
                }
                isCountDown = true;
                jumpImmediately();
            }
        });
    }

    private void showSplash() {
        try {
            List<String> pictures = FileUtils.getPictures(Constants.SPLASH_FILE_PATH);
            if (pictures != null && pictures.size() > 0) {
                isCountDown = false;
                mSkipTextView.setVisibility(View.VISIBLE);
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = Message.obtain();
                        message.what = TIMER_SPLASH_COUNDOWN;
                        mHandler.sendMessage(message);
                    }
                }, 1000, 1000);

                String localSplash = pictures.get(0);
                int dotIndex = localSplash.lastIndexOf(".");
                if (dotIndex >= 0) {
                    String resExtension = localSplash.substring(dotIndex + 1).toLowerCase();
                    if ("gif".equals(resExtension)) {
                        mGifDrawable = new GifDrawable(new File(localSplash));
                    } else {
                        Bitmap localBitmap = FileUtils.getLocalBitmap(localSplash);
                        mGifImageView.setImageBitmap(localBitmap);
                        mAnimationCompleted = true;
                        return;
                    }
                } else {
                    isCountDown = true;
                    mSkipTextView.setVisibility(View.GONE);
                    mGifDrawable = new GifDrawable(getAssets(), "gif_splash_welcome.gif");
                }
            } else {
                isCountDown = true;
                mSkipTextView.setVisibility(View.GONE);
                mGifDrawable = new GifDrawable(getAssets(), "gif_splash_welcome.gif");
            }
        } catch (Exception e) {
            try {
                isCountDown = true;
                mSkipTextView.setVisibility(View.GONE);
                mGifDrawable = new GifDrawable(getAssets(), "gif_splash_welcome.gif");
            } catch (IOException e1) {
            }
        }
        mGifDrawable.setLoopCount(1);
        mGifDrawable.setSpeed(1f);
        mGifDrawable.addAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                mAnimationCompleted = true;
                if (isJumpable) {
                    jump();
                }
            }
        });
        mGifImageView.setImageDrawable(mGifDrawable);
        mGifDrawable.start();
    }

    private void requestSplash() {
        if (!CommonUtil.isNetWorkConnected(mContext)) {
            return;
        }
        HttpRequest.getInstance().sendRequest(Constants.IM_BASE_URL,
                new HttpParams.Builder()
                        .url("user/appStart")
                        .method(Method.GET)
                        .addParam("token", 1)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        try {
                            String result = o.toString();
                            if (JSONUtil.validate(result)) {
                                JSONObject resultObject = JSON.parseObject(result);
                                JSONArray resArray = resultObject.getJSONArray("result");
                                if (resArray != null && resArray.size() > 0) {
                                    JSONObject resObject = resArray.getJSONObject(0);
                                    String resUrl = JSONUtil.getText(resObject, "aa_urlc");
                                    if (!TextUtils.isEmpty(resUrl)) {
                                        String oldUrl = CommonUtil.getSharedPreferences(mContext, Constants.CACHE.CACHE_SPLASH_URL);
                                        if (resUrl.equals(oldUrl)) {
                                            List<String> pictures = FileUtils.getPictures(Constants.SPLASH_FILE_PATH);
                                            if (pictures != null && pictures.size() > 0) {
                                                return;
                                            } else {
                                                downloadSplash(resUrl);
                                            }
                                        } else {
                                            downloadSplash(resUrl);
                                        }
                                    } else {
                                        clearSplashDir();
                                    }
                                } else {
                                    clearSplashDir();
                                }
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                    }
                });
    }

    private void downloadSplash(String resUrl) {
        initSplashDir();
        mSplshUrl = resUrl;
        CommonIntentService.downloadSplash(MyApplication.getInstance(), resUrl);
    }

    private void clearSplashDir() {
        initSplashDir();
        CommonUtil.setSharedPreferences(mContext, Constants.CACHE.CACHE_SPLASH_URL, "");
    }

    private void initSplashDir() {
        File directory = new File(Constants.SPLASH_FILE_PATH);
        if (!directory.exists() && !directory.isDirectory()) {
            boolean mkdirs = directory.mkdirs();
        } else {
            CommonUtil.delAllFile(Constants.SPLASH_FILE_PATH);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume()");
    }

    /**
     * 配置参数初始化
     */
    private void initConfig() {
        LogUtil.d("Arison","isNetworkActive():"+MyApplication.getInstance().isNetworkActive());
        if (!MyApplication.getInstance().isNetworkActive()) {// 没有网络的情况下
            setConfig(new ConfigBean());
            return;
        }

        HttpClient httpClient = new HttpClient.Builder("http://113.105.74.140:8092/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("config")
                .method(Method.GET)
                .connectTimeout(10000)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
              LogUtil.d("Arison","onResponse:"+JSON.toJSONString(o));

                ConfigBean configBean = null;
               String data=  JSON.parseObject(o.toString()).getString("data");
                ConfigBean result=JSON.parseObject(data, ConfigBean.class);
//                if (result == null || result.getResultCode() != Result.CODE_SUCCESS || result.getData() == null) {
//                    configBean = new ConfigBean();// 读取网络配置失败，使用默认配置
//                } else {
                    configBean = result;
//                }
                setConfig(configBean);
            }

            @Override
            public void onFailure(Object t) {
                LogUtil.d("Arison","onFailure:"+JSON.toJSONString(t));
                setConfig(new ConfigBean());// 读取网络配置失败，使用默认配置
            }
        }));
        
//        StringJsonObjectRequest<ConfigBean> request = new StringJsonObjectRequest<ConfigBean>(AppConfig.CONFIG_URL, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError arg0) {
//                LogUtil.d("Arison","网络异常："+JSON.toJSONString(arg0));
//                setConfig(new ConfigBean());// 读取网络配置失败，使用默认配置
//            }
//        }, new StringJsonObjectRequest.Listener<ConfigBean>() {
//            @Override
//            public void onResponse(ObjectResult<ConfigBean> result) {
//                ConfigBean configBean = null;
//                if (result == null || result.getResultCode() != Result.CODE_SUCCESS || result.getData() == null) {
//                    configBean = new ConfigBean();// 读取网络配置失败，使用默认配置
//                } else {
//                    configBean = result.getData();
//                }
//                setConfig(configBean);
//            }
//        }, ConfigBean.class, null);
//        addShortRequest(request);
    }

    /**
     * 更新账户中心token
     */
    private void updateAccountToken() {
        String oldToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        if (TextUtils.isEmpty(oldToken)) {
            return;
        }
        HttpRequest.getInstance().sendRequest(Constants.ACCOUNT_CENTER_HOST,
                new HttpParams.Builder()
                        .url("sso/login/updateToken")
                        .addParam("token", oldToken)
                        .method(Method.GET)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        if (o != null) {
                            if (JSONUtil.validate(o.toString())) {
                                JSONObject resultObject = JSON.parseObject(o.toString());
                                String newToken = JSONUtil.getText(resultObject, "content");
                                if (!TextUtils.isEmpty(newToken)) {
                                    CommonUtil.setSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN, newToken);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                    }
                });
    }

    private void setConfig(ConfigBean configBean) {
        MyApplication.getInstance().setConfig(AppConfig.initConfig(this, configBean));// 初始化配置
        isNeedLogin();
    }


    @SuppressLint("NewApi")
    private void jump() {
        if (isDestroyed()) {
            return;
        }
        if (!mAnimationCompleted) {
            return;
        }

        if (!isCountDown) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long duration = currentTime - mStartTime;
        if (duration < 2000) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    jump();
                }
            }, 2000 - duration);
            return;
        }

        jumpImmediately();
    }

    private void isNeedLogin() {
        boolean idIsEmpty = TextUtils.isEmpty(UserSp.getInstance(this).getUserId(""));
        boolean telephoneIsEmpty = TextUtils.isEmpty(UserSp.getInstance(this).getTelephone(null));
        if (!idIsEmpty && !telephoneIsEmpty) {// 用户标识都不为空，那么就能代表一个完整的用户
            // 进入之前，加载本地已经存在的数据
            String userId = UserSp.getInstance(this).getUserId("");
            User user = UserDao.getInstance().getUserByUserId(userId);
            if (!LoginHelper.isUserValidation(user)) {// 用户数据错误,那么就认为是一个游客
                isJumpable = true;
                jump();
            } else {
                MyApplication.getInstance().mLoginUser = user;
                MyApplication.getInstance().mAccessToken = UserSp.getInstance(this).getAccessToken(null);
                MyApplication.getInstance().mExpiresIn = UserSp.getInstance(this).getExpiresIn(0);
                login(user.getTelephone(), user.getPassword());
            }
        } else {
            isJumpable = true;
            jump();
        }
    }

    //登陆
    private void login(final String phoneNumber, final String password) {
        if (TextUtils.isEmpty(phoneNumber)) {
            isJumpable = true;
            jump();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            isJumpable = true;
            jump();
            return;
        }
        // 加密之后的密码
        final String requestTag = "login";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("telephone", Md5Util.toMD5(phoneNumber));// 账号登陆的时候需要MD5加密，服务器需求
        params.put("password", password);
        // 附加信息
        params.put("model", SystemUtil.getModel());
        params.put("osVersion", SystemUtil.getOsVersion());
        params.put("serial", SystemUtil.getDeviceId(mContext));
        // 地址信息
        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        final StringJsonObjectRequest<LoginRegisterResult> request = new StringJsonObjectRequest<LoginRegisterResult>(mConfig.USER_LOGIN,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        isJumpable = true;
                        jump();
                    }
                }, new StringJsonObjectRequest.Listener<LoginRegisterResult>() {
            @Override
            public void onResponse(ObjectResult<LoginRegisterResult> result) {
                isJumpable = true;
                if (result == null) {
                    jump();
                    return;
                }
                boolean success = false;
                if (result.getResultCode() == Result.CODE_SUCCESS) {
                    success = LoginHelper.setLoginUser(mContext, phoneNumber, password, result);// 设置登陆用户信息
                }
                if (success) {// 登陆IM成功
                    String nowtime = TimeUtils.f_long_2_str(System.currentTimeMillis());
                    String saved_time = PreferenceUtils.getString(MyApplication.getInstance(), TimeStatisticsActivity.Login_In);
                    if (!TextUtils.isEmpty(saved_time)) {
                        PreferenceUtils.putString(TimeStatisticsActivity.Login_In, saved_time + "," + nowtime);
                    } else {
                        PreferenceUtils.putString(TimeStatisticsActivity.Login_In, nowtime);
                    }
                    Log.d("gifdrawableim", "animation->" + mAnimationCompleted);
                    if (mAnimationCompleted) {
                        long currentTime = System.currentTimeMillis();
                        long duration = currentTime - mStartTime;
                        if (duration < 2000) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isCountDown) {
                                        trun2NextPage(1);
                                    }
                                }
                            }, 2000 - duration);
                        } else {
                            if (isCountDown) {
                                trun2NextPage(1);
                            }
                        }
                    }
                } else {// 登录失败
                    jump();
                }
            }
        }, LoginRegisterResult.class, params);
        request.setTag(requestTag);
        addDefaultRequest(request);
    }


    private void trun2NextPage(final int type) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mTimer != null) {
                    mTimer.cancel();
                }
                UasLocationHelper.getInstance().requestLocation();
                trun2NextPage2(type);
            }
        };
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, runnable, runnable);
    }

    private void trun2NextPage2(int type) {
        boolean isFirst = PreferenceUtils.getBoolean(IS_FIRST, true);
        Intent intent = new Intent();
        if (isFirst) {
            intent.setClass(this, ADActivity.class);
            intent.putExtra("type", type);
        } else {
            if (type == 1) {
                intent.setClass(this, MainActivity.class);
            } else {
                intent.setClass(this, LoginActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }

    // 停留在此界面
    private void stay() {
        mSelectLv.setVisibility(View.GONE);
        trun2NextPage(0);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
        mSelectLv.startAnimation(anim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGifDrawable != null) {
            mGifDrawable.recycle();
            mGifDrawable = null;
        }
    }
}
