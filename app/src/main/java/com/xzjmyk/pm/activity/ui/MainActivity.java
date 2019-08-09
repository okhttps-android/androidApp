package com.xzjmyk.pm.activity.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.SignAutoLogDao;
import com.core.dao.UserDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.CircleMessage;
import com.core.model.NewFriendMessage;
import com.core.model.OAConfig;
import com.core.model.User;
import com.core.model.WorkModel;
import com.core.net.NetWorkObservable;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.FastVolley;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.utils.CommonInterface;
import com.core.utils.IntentUtils;
import com.core.utils.helper.LoginHelper;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.AuthStateListener;
import com.core.xmpp.model.SignAutoLogEntity;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.modular.appmessages.presenter.MessagePresenter;
import com.modular.apptasks.presenter.AutoPresenter;
import com.modular.apptasks.presenter.SchedulePresenter;
import com.modular.apptasks.util.AlarmUtil;
import com.modular.login.activity.LoginActivity;
import com.uas.appcontact.listener.ImStatusListener;
import com.uas.appcontact.ui.fragment.ContactsFragment;
import com.uas.appcontact.ui.fragment.GroupChatFragment;
import com.uas.appme.other.model.Master;
import com.uas.appme.pedometer.service.StepService;
import com.uas.appme.pedometer.utils.StepCountCheckUtil;
import com.uas.appme.pedometer.utils.StepUtils;
import com.uas.appme.settings.activity.FeedbackActivity;
import com.uas.appme.settings.activity.SettingActivity;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.model.bean.TimeHelperBean;
import com.uas.appworks.utils.ScheduleUtils;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.adapter.MainScheduleAdapter;
import com.xzjmyk.pm.activity.ui.circle.BusinessCircleFragment;
import com.xzjmyk.pm.activity.ui.erp.fragment.WorkPlatFragment;
import com.xzjmyk.pm.activity.ui.find.MyFriendFragment;
import com.xzjmyk.pm.activity.ui.me.MeFragment;
import com.xzjmyk.pm.activity.ui.me.ScreenListener;
import com.xzjmyk.pm.activity.ui.message.MessageFragment;
import com.xzjmyk.pm.activity.ui.tool.WebViewActivity;
import com.xzjmyk.pm.activity.util.dialog.QSearchPpwindowUtils;
import com.xzjmyk.pm.activity.util.im.Constants;
import com.xzjmyk.pm.activity.util.oa.BadgeUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.view.DivideRadioGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;


public class MainActivity extends BaseActivity implements ImStatusListener, NetWorkObservable.NetWorkObserver, MessagePresenter.UnReaderListener, AuthStateListener {
    private static final int LOG_SEND_REQUEST = 17519;
    public static String HASHCODE = "MainActivity";
    public static boolean isUnReadWork;   //判断我的空间是否有新内容
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_MY = "myfriend";
    private static final String TAG_NEARBY = "nearby";
    private static final String TAG_GROUP_CHAT = "group_chat";
    private static final String TAG_ME = "me";
    private static final String TAG_BusinessCircle = "my_BusinessCircle";

    /* UserCheck */
    private static final int MSG_USER_CHECK = 0x1;
    // 因为RadioGroup的check方法，会调用onCheckChange两次，用mLastFragment保存最后一次添加的fragment，防止重复add
    // fragment 出错
    private static final int RETRY_CHECK_DELAY_MAX = 30000;// 为成功的情况下，最长30s检测一次
    public static String SIP_DOMAIN = "120.24.211.24";
    public static String SIP_SERVER_HOST = "120.24.211.24";
    public String SIP_USERNAME = "10000072";
    private boolean mBind;
    private CoreService mXmppService;
    private FastVolley mFastVolley;
    //private BusinessCircleFragment mBusinessCircleFragment;
    private ActivityManager mActivityManager;
    // 界面组件
    private DivideRadioGroup mTabRadioGroup;
    /**
     * @注释：主界面Fragment
     */
    private Fragment mLastFragment;
    private MessageFragment mMessageFragment;
    private MyFriendFragment mMyFriendFragment;//我的朋友---相互关注，单项关注，房间
    private WorkPlatFragment mWorksFragment;//工作
//	private WorksFragment mWorksFragment;//工作

    private GroupChatFragment mGroupChatFragment;//群聊
    private MeFragment mMeFragment;//我
    private ContactsFragment mBusinessCircleFragment;
    public static String BAIDU_PUSH = "BAIDUPUSH";
    public static String UU_STEP = "UUSTEP";
    public static String Q_SEARCH = "Q_SEARCH";
    private boolean mXmppBind;
    private CoreService mCoreService;
    private boolean isPause = true;// 界面是否暂停
    private ScreenListener screenListener;//锁屏开屏监听
    private int mRetryCheckDelay = 0;
    public static int UPDATA_LIST = 101;
    public static String NEW_FUNCTION_NOTICE = "NEW_FUNCTION_NOTICE2017_09_26";
    public static String NEW_VERSION_RATING = "NEW_VERSION_RATING";  // 判断是否为新版本弹出评分标志
    private TextView main_tab_three_tv;
    private SignAutoLogDao signAutoLogDao = new SignAutoLogDao();
    private Handler mUserCheckHander = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_USER_CHECK) {
                if (mRetryCheckDelay < RETRY_CHECK_DELAY_MAX) {
                    mRetryCheckDelay += 5000;
                }
                mUserCheckHander.removeMessages(RETRY_CHECK_DELAY_MAX);
                doUserCheck();
            }

            if (msg.what == LOG_SEND_REQUEST) {
                String Log_Send_request = msg.getData().getString("result");
                Log.i("Log_Send_request", Log_Send_request + "");
                signAutoLogDao.cleanLocalData(); // 整理本地数据
                try {
                    if (!StringUtil.isEmpty(Log_Send_request) &&
                            JSON.parseObject(Log_Send_request).containsKey("result") &&
                            JSON.parseObject(Log_Send_request).getBoolean("result")) {

                        signAutoLogDao.updateCurData(isendEntity); // 将上传的日志状态标为1 ，是本地数据保留本次上传的和产生的
                        Toast.makeText(ct, "日志上传成功", Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private int mImStatus = AuthStateListener.AUTH_STATE_NOT;
    private List<SignAutoLogEntity> signAutoLogEntity;
    private List<SignAutoLogEntity> isendEntity;
    private Boolean platform;
    private RadioButton main_tab_one, main_tab_two, main_tab_five, main_tab_three;
    private PopupWindow mUpdatePopupWindow;
    private PopupWindow mSchedulePop;

    public int getmImStatus() {
        return mImStatus;
    }

    private ServiceConnection mXmppServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.CoreServiceBinder) service).getService();
            mImStatus = mCoreService.isAuthenticated() ? AuthStateListener.AUTH_STATE_SUCCESS : AuthStateListener.AUTH_STATE_NOT;
        }
    };

    @Override
    public boolean needCommonToolBar() {
        return false;
    }

    /**
     * 启动返回
     * by gongpm
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (UPDATA_LIST == requestCode) {
            //TODO 更新
//            if (mBusinessCircleFragment != null) {
//                mBusinessCircleFragment.onResult();
//            }
        } else if (requestCode == 0x11) {
            if (resultCode == 0x12) {
                mTabRadioGroup.check(R.id.main_tab_five);
            }
        }
    }

    int oldERPNum = 0;


    private void initPermission() {
        initPermission(0);
    }

    private void initPermission(int item) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initPermission(item + 1);
            }
        };
        String permission = null;
        switch (item) {
            case 0:
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            case 1:
                permission = Manifest.permission.RECORD_AUDIO;
                break;
            case 2:
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
        }
        if (item <= 2) {
            requestPermission(permission, runnable, runnable);
        }
    }


    private Animation getImgAnimation(View view) {
        Object tag = view.getTag(R.id.tag_key);
        if (tag != null && tag instanceof ScaleAnimation) {
            ScaleAnimation animation = (ScaleAnimation) tag;
            return animation;
        }
        ScaleAnimation animation = new ScaleAnimation(
                0.5f, 1.0f, 0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);
        animation.setDuration(300);//设置动画持续时间
//      animation.setRepeatCount(-1);//设置重复次数
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        animation.setStartOffset(0);//执行前的等待时间
        view.setTag(R.id.tag_key, animation);
        return animation;
    }

    private Timer delayTimer;
    private Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
            if (main_tab_three != null) {
                main_tab_three.startAnimation(getImgAnimation(main_tab_three));
            }
        }
    };


    //进来时候登陆
    public void Login() {
        Master master = new Master();
        master.setMa_function(CommonUtil.getMaster());
        String url = com.core.utils.CommonUtil.getAppBaseUrl(ct) + "mobile/login.action";
        Map<String, Object> params = new HashMap<String, Object>();
        String accountToken = CommonUtil.getSharedPreferences(ct, com.core.app.Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        params.put("token", accountToken);
        params.put("master", CommonUtil.getMaster());
        Message message = new Message();
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mUserCheckHander, headers, 0x12312, message, null, "get");

//       new Thread(()->{
//           while (true){
//               try {
//                   Thread.sleep(1000);
//               } catch (InterruptedException e) {
//                   e.printStackTrace();
//               }
//               LogUtil.d(TAG,"执行一次网络请求..."+DateFormatUtil.getStrDate4Date(new Date(),DateFormatUtil.YMD_HMS));
//               ViewUtil.AutoLoginErp(this);
//    
//               com.core.utils.CommonUtil.displayBriefMemory();
//           }
//       }).start();

    }

    private void delay() {
        if (delayTimer == null) {
            delayTimer = new Timer();
        }
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                OAHttpHelper.getInstance().post(uiRunnable);
            }
        }, 100);
    }

    private void startAnimator(View view) {
//        if (view != null && main_tab_three == view) {
//            delay();
//        } else {
//            view.startAnimation(getImgAnimation(view));
//
//        }
        view.startAnimation(getImgAnimation(view));
    }

    /**
     * @desc:主页菜单入口
     * @author：Administrator on 2016/1/26 10:03
     */
    private DivideRadioGroup.OnCheckedChangeListener mTabRadioGroupChangeListener = new DivideRadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(DivideRadioGroup group, int checkedId) {
            if (checkedId == R.id.main_tab_one) {
                if (mMessageFragment == null) {
                    mMessageFragment = new MessageFragment();
                }
                changeFragment(mMessageFragment, TAG_MESSAGE);//消息
                updateMessageTitle();
                startAnimator(main_tab_one);
            } else if (checkedId == R.id.main_tab_two) {
                if (mBusinessCircleFragment == null) {
                    mBusinessCircleFragment = new ContactsFragment();
                }

                changeFragment(mBusinessCircleFragment, TAG_BusinessCircle);//发现
                setTitle(getString(R.string.contact_title));
                startAnimator(main_tab_two);
            } else if (checkedId == R.id.main_tab_three) {  //点击工作选项
                if (mWorksFragment == null) {
                    mWorksFragment = new WorkPlatFragment();
                }
                changeFragment(mWorksFragment, TAG_NEARBY);
                String role = com.core.utils.CommonUtil.getUserRole();
                if (role.equals("2")) {
                    String master_ch = CommonUtil.getSharedPreferences(ct, "Master_ch");
                    setTitle(TextUtils.isEmpty(master_ch) ? getString(R.string.work_title) : master_ch);
                } else {
                    setTitle(getString(R.string.work_title));
                }
                main_tab_three_tv.setVisibility(View.INVISIBLE);
                PreferenceUtils.putInt(MainActivity.NEW_FUNCTION_NOTICE, 1);
                startAnimator(main_tab_three);
            } else if (checkedId == R.id.main_tab_five) {
                if (mMeFragment == null) {
                    mMeFragment = new MeFragment();
                }
                CommonUtil.setSharedPreferences(ct, com.core.app.Constants.NEW_ME_TAG, true);
                changeFragment(mMeFragment, TAG_ME);//我
                setTitle(R.string.me);
                startAnimator(main_tab_five);
            }
        }
    };
    private AppConfig config;
    private String mLoginUserId;

    private void requestMyBusiness() {
        if (config == null) {
            config = MyApplication.getInstance().getConfig();
        }
        if (mLoginUserId == null || mLoginUserId.length() <= 0) {
            mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("pageSize", "1");
        StringJsonArrayRequest<CircleMessage> request = new StringJsonArrayRequest<CircleMessage>(
                config.MSG_LIST, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
            }
        }, new StringJsonArrayRequest.Listener<CircleMessage>() {
            @Override
            public void onResponse(ArrayResult<CircleMessage> result) {
                boolean success = Result.defaultParser(ct, result, false);
                if (success) {
                    //获取到数据
                    try {
                        List<CircleMessage> datas = result.getData();
                        if (ListUtils.isEmpty(datas)) return;
                        long time = datas.get(0).getTime();
                        String userId = datas.get(0).getUserId();
                        Long oldTime = PreferenceUtils.getLong("TIMEMAIN", -1);
                        if (!userId.equals(MyApplication.getInstance().mLoginUser.getUserId()) && oldTime < time) {
                            setShowUnRead(true);
                        } else {
                            setShowUnRead(false);
                        }
                    } catch (Exception e) {
                    }
                } else {
                    setShowUnRead(false);
                }
            }
        }, CircleMessage.class, params);
        if (mFastVolley == null) {
            mFastVolley = MyApplication.getInstance().getFastVolley();
        }
        mFastVolley.addDefaultRequest(HASHCODE, request);
    }


    private void setShowUnRead(boolean isUnRead) {
        isUnReadWork = isUnRead;
        if (isUnRead) {
            unWorkReadTV.setVisibility(View.VISIBLE);
        } else if (CommonUtil.getSharedPreferencesBoolean(ct, com.core.app.Constants.NEW_ME_TAG, false)) {
            unWorkReadTV.setVisibility(GONE);
        }
        if (mMeFragment == null) return;
        mMeFragment.setChangerRemain(!isUnRead);

    }

    /***********************
     * 未读数量的更新功能
     *****************/
    private Handler mUnReadHandler = new Handler();
    private TextView mMsgUnReadTv;
    private TextView unWorkReadTV;
    private int mMsgUnReadNum = 0;
    private BroadcastReceiver mUserLogInOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LoginHelper.ACTION_LOGIN)) {
                User user = MyApplication.getInstance().mLoginUser;
                Intent startIntent = CoreService.getIntent(MainActivity.this, user.getUserId(), user.getPassword(), user.getNickName());
                startService(startIntent);
                // ToastUtil.showNormalToast(MainActivity.this, "开始Xmpp登陆");
                checkUserDb(user.getUserId());

                mTabRadioGroup.clearCheck();
                mTabRadioGroup.check(R.id.main_tab_one);
            } else if (action.equals(LoginHelper.ACTION_LOGOUT)) {
                try {
                    MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                    if (mCoreService != null) {
                        mCoreService.logout();
                    }
                    cancelUserCheckIfExist();
                    AlarmUtil.cancelAlarm(AlarmUtil.ID_SCHEDULE, AlarmUtil.ACTION_SCHEDULE);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    // mFindRb.setChecked(true);
                    MainActivity.this.finish();
                    removeNeedUserFragment(false);
                } catch (Exception e) {
                }
            } else if (action.equals(LoginHelper.ACTION_CONFLICT)) {
                // 改变用户状态
                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
                mCoreService.logout();
                removeNeedUserFragment(true);
                cancelUserCheckIfExist();
                // 弹出对话框---用户冲突LoginHelper.STATUS_USER_TOKEN_CHANGE
                startActivity(new Intent(MainActivity.this, UserCheckedActivity.class));
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB) {
                    mActivityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    mActivityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
                }

            } else if (action.equals(LoginHelper.ACTION_TOKEN)) {  //TOKEN异常
                // 改变用户状态
                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_TOKEN_CHANGE;
                mCoreService.logout();
                removeNeedUserFragment(true);
                cancelUserCheckIfExist();
                // 弹出对话框---LoginHelper.STATUS_USER_TOKEN_CHANGE=4
                startActivity(new Intent(MainActivity.this, UserCheckedActivity.class));
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB) {
                    mActivityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    mActivityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_NO_USER_ACTION);
                }
            } else if (action.equals(LoginHelper.ACTION_NEED_UPDATE)) {
                // mFindRb.setChecked(true);
                removeNeedUserFragment(true);
                cancelUserCheckIfExist();
                // 弹出对话框
                startActivity(new Intent(MainActivity.this, UserCheckedActivity.class));
            } else if (action.equals(LoginHelper.ACTION_LOGIN_GIVE_UP)) {
                cancelUserCheckIfExist();
                MyApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_NO_UPDATE;
                mCoreService.logout();
            }

        }

    };
    private boolean mMsgNumNeedUpdate = false;
    private BroadcastReceiver mUpdateUnReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!AppConfig.COMPANY) return;
            String action = intent.getAction();
            if (action.equals(MsgBroadcast.ACTION_MSG_NUM_UPDATE)) {
                int operation = intent.getIntExtra(MsgBroadcast.EXTRA_NUM_OPERATION, MsgBroadcast.NUM_ADD);
                int count = intent.getIntExtra(MsgBroadcast.EXTRA_NUM_COUNT, 0);
                mMsgUnReadNum = (operation == MsgBroadcast.NUM_ADD) ? mMsgUnReadNum + count : mMsgUnReadNum - count;
                updateMsgUnReadTv();
            } else if (action.equals(MsgBroadcast.ACTION_MSG_NUM_RESET)) {
                if (isPause) {// 等待恢复的时候更新
                    mMsgNumNeedUpdate = true;
                } else {// 立即更新
                    initMsgUnReadTips(MyApplication.getInstance().mLoginUser.getUserId());
                }
            } else if (AlarmUtil.ACTION_SCHEDULE.equals(action)) {
                SchedulePresenter.getInstance().startSchedule();
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((CoreService.CoreServiceBinder) service).getService();
        }
    };
//TODO 尝试不再activity上处理toolbar
//    @Override
//    public boolean needNavigation() {
//        return false;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SIP_USERNAME = MyApplication.getInstance().mLoginUser.getUserId();
        String host = mConfig.MeetingHost;
        SIP_DOMAIN = host == null ? "120.24.211.24" : host;
        SIP_SERVER_HOST = host == null ? "120.24.211.24" : host;
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                CommonUtil.getMetaValue(this, "api_key"));
        int isPush = PreferenceUtils.getInt(MyApplication.getInstance(), AppConstant.BAIDU_PUSH);
        if (isPush == 0) {
            PushManager.stopWork(this);
        }
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        initView(savedInstanceState);//---
        // 注册网络改变回调
        MyApplication.getInstance().registerNetWorkObserver(this);
        //更新版本已升级到最新版本
        updateVersion();
        // 绑定监听
        ListenerManager.getInstance().addAuthStateChangeListener(this);
        // 注册消息更新广播
        IntentFilter msgIntentFilter = new IntentFilter();
        msgIntentFilter.addAction(MsgBroadcast.ACTION_MSG_NUM_UPDATE);
        msgIntentFilter.addAction(MsgBroadcast.ACTION_MSG_NUM_RESET);
        msgIntentFilter.addAction(AlarmUtil.ACTION_SCHEDULE);
        registerReceiver(mUpdateUnReadReceiver, msgIntentFilter);
        // 注册用户登录状态广播
        registerReceiver(mUserLogInOutReceiver, LoginHelper.getLogInOutActionFilter());

        // 绑定服务
        mXmppBind = bindService(CoreService.getIntent(), mXmppServiceConnection, BIND_AUTO_CREATE);

        // 检查用户的状态，做不同的初始化工作
        User loginUser = MyApplication.getInstance().mLoginUser;
        if (!LoginHelper.isUserValidation(loginUser)) {
            LoginHelper.prepareUser(this);
        }

        if (!MyApplication.getInstance().mUserStatusChecked) {// 用户状态没有检测，那么开始检测
            mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
        } else {
            if (MyApplication.getInstance().mUserStatus == LoginHelper.STATUS_USER_VALIDATION) {
                LoginHelper.broadcastLogin(this);
            } else {// 重新检测
                MyApplication.getInstance().mUserStatusChecked = false;
                mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
            }
        }
        getScheduleList();
        mBind = bindService(CoreService.getIntent(), mServiceConnection, BIND_AUTO_CREATE);

        screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                Log.d("wang", "MainActivity....开屏");
            }

            @Override
            public void onScreenOff() {
                Log.d("wang", "MainActivity....锁屏");
            }

            @Override
            public void onUserPresent() {
                Log.d("wang", "MainActivity....解锁");
            }
        });

        
        doHiTask();
        autoPresenter = AutoPresenter.onCreate();
        SchedulePresenter.getInstance().startSchedule();
        CommonInterface.getInstance().loadConfigs(null);
//		startService(new Intent(MainActivity.this, AutoErpService.class));
        LogUtil.d("OnCreate end:" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));

        //初始化面部识别
        com.baidu.aip.excep.utils.FaceConfig.initFace(ct);
        Login();
        
          
        
    }
    
 

    private AutoPresenter autoPresenter;
    private Handler handler = new Handler();
    private Runnable runnable = new MyRunnable();
    private Boolean firstRed = true;

    private void doHiTask() {
        int isUUStep = PreferenceUtils.getInt(MyApplication.getInstance(), MainActivity.UU_STEP);
        if (isUUStep != 0 && StepCountCheckUtil.isSupportStepCountSensor(this)) { // 开启UU运动
            LogUtil.i("userid=" + MyApplication.getInstance().getLoginUserId());
            startService(new Intent(mContext, StepService.class)
                    .putExtra("my_userid", MyApplication.getInstance().getLoginUserId())
                    .putExtra("token", MyApplication.getInstance().mAccessToken));
            PreferenceUtils.putInt(MainActivity.UU_STEP, 1);
            PushManager.resumeWork(MyApplication.getInstance());
        }
//        doSendSignAutoLog(); // 获取数据库中监控日志上传至服务器操作
        //桌面红点显示逻辑
        if (OAConfig.canShowRed) {
            if (handler == null) {
                handler = new Handler();
            }
            if (runnable == null) {
                runnable = new MyRunnable();
            }
            handler.post(runnable);
        }
        //语音导航 ，目前还未正式开发
//		hiQuickSearch();

        //将本地的UU运动步数更新至服务器
        StepUtils.doSaveLocalStepsToJudgeHttps();
    }

    private void hiQuickSearch() {
        int qs = PreferenceUtils.getInt(MyApplication.getInstance(), MainActivity.Q_SEARCH);
        if (CommonUtil.isReleaseVersion() || MainActivity.class == null || qs == 1) return;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                QSearchPpwindowUtils.qSearchWindows(MainActivity.this);
            }
        }, 4000);
    }

    public class MyRunnable implements Runnable {

        @Override
        public void run() {
            handler.postDelayed(runnable, 5000);
            BadgeUtil.setBadgeCount(getApplicationContext(), mMsgUnReadNum, R.drawable.hongdian);//启动红点显示逻辑
        }
    }

    private PopupWindow mScorePopupWindow;

    private void initScoringWindows() {
        View contentView = LayoutInflater.from(ct).inflate(R.layout.layout_menu_scoring, null);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 250);
        h_screen = DisplayUtil.dip2px(this, 250);
        mScorePopupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        mScorePopupWindow.setTouchable(true);
        mScorePopupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        mScorePopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_round_bg));
        // 设置好参数之后再show
        mScorePopupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
        contentView.findViewById(R.id.scoring_now_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    IntentUtils.webLinks(ct, "http://apk.91.com/Soft/Android/com.xzjmyk.pm.activity-54.html", "UU互联");
                }
                mScorePopupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.complaints_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FeedbackActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra(WebViewActivity.EXTRA_URL, mConfig.help_url);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.Rated_suggest));
                startActivity(intent);
                mScorePopupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.no_thanking_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScorePopupWindow.dismiss();
            }
        });
        PreferenceUtils.putInt(MainActivity.NEW_VERSION_RATING, 1);
    }

    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (mScorePopupWindow == null) return;
        mScorePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mScorePopupWindow.dismiss();
                clearBackgroundAlpha();
            }
        });
    }

    private void clearBackgroundAlpha() {
        if (mScorePopupWindow != null && mScorePopupWindow.isShowing()) {
            return;
        }

        if (mUpdatePopupWindow != null && mUpdatePopupWindow.isShowing()) {
            return;
        }

        if (mSchedulePop != null && mSchedulePop.isShowing()) {
            return;
        }

        setBackgroundAlpha(MainActivity.this, 1f);
    }

    /**
     * 获取日程列表
     */
    private void getScheduleList() {
        int cacheDate = CommonUtil.getSharedPreferencesInt(mContext, com.core.app.Constants.CACHE.OBTAIN_SCHEDULE_DATE, 0);
        String currentDate = DateFormatUtil.long2Str("yyyyMMdd");
        int dateInt = Integer.parseInt(currentDate);

        if ((dateInt - cacheDate) >= 1) {
            HttpRequest.getInstance().sendRequest("https://mobile.ubtob.com:8443/",
                    new HttpParams.Builder()
                            .url("schedule/schedule/getByDaySchedule")
                            .method(Method.GET)
                            .addParam("imid", MyApplication.getInstance().getLoginUserId())
                            .addParam("day", DateFormatUtil.long2Str(System.currentTimeMillis(), DateFormatUtil.YMD))
                            .addParam("uasUrl", com.core.utils.CommonUtil.getAppBaseUrl(this))
                            .addParam("emcode", com.core.utils.CommonUtil.getEmcode())
                            .addParam("master", com.core.utils.CommonUtil.getMaster())
                            .addParam("sessionId", com.core.utils.CommonUtil.getSharedPreferences(ct, "sessionId"))
                            .build(), new HttpCallback() {
                        @Override
                        public void onSuccess(int flag, Object o) throws Exception {
                            String currentDate = DateFormatUtil.long2Str("yyyyMMdd");
                            int dateInt = Integer.parseInt(currentDate);
                            CommonUtil.putSharedPreferencesInt(mContext, com.core.app.Constants.CACHE.OBTAIN_SCHEDULE_DATE, dateInt);

                            try {
                                analysisSchedule(o.toString());
                            } catch (Exception e) {
                                clearBackgroundAlpha();
                                CommonUtil.setSharedPreferences(mContext, com.core.app.Constants.CACHE.OBTAIN_SCHEDULE_DATE, 0);
                                Log.e("mainschedule", e.toString());
                            }
                        }

                        @Override
                        public void onFail(int flag, String failStr) throws Exception {

                        }
                    });
        }
    }

    private void initSchedulePop(List<TimeHelperBean> timeHelperBeans) {
        View scheduelView = View.inflate(this, R.layout.pop_main_schedule, null);
        mSchedulePop = new PopupWindow(scheduelView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        int screenHeigh = getResources().getDisplayMetrics().heightPixels;
        mSchedulePop.setHeight(Math.round(screenHeigh * 0.7f));

        LinearLayout mExitLayout = scheduelView.findViewById(R.id.pop_main_schedule_exit_ll);
        mExitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSchedulePop != null) {
                    mSchedulePop.dismiss();
                }
                clearBackgroundAlpha();
            }
        });
        RecyclerView recyclerView = (RecyclerView) scheduelView.findViewById(R.id.pop_main_schedule_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MainScheduleAdapter mainScheduleAdapter = new MainScheduleAdapter(timeHelperBeans);
        recyclerView.setAdapter(mainScheduleAdapter);
        mSchedulePop.setAnimationStyle(com.uas.appme.R.style.MenuAnimationFade);
        mSchedulePop.setBackgroundDrawable(new BitmapDrawable());
        mSchedulePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mSchedulePop != null) {
                    mSchedulePop.dismiss();
                }
                clearBackgroundAlpha();
            }
        });

        mSchedulePop.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(MainActivity.this, 0.5f);
    }

    private void analysisSchedule(String result) {
        LogUtil.prinlnLongMsg("mainschedule", result);
        if (JSONUtil.validate(result)) {
            List<TimeHelperBean> timeHelperBeans = new ArrayList<>();
            JSONObject resultObject = JSON.parseObject(result);
            JSONArray dataArray = resultObject.getJSONArray("data");
            if (dataArray != null && dataArray.size() > 0) {
                long currentTimeMillis = System.currentTimeMillis();

                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("UTC+8"));
                cal.setTime(new Date());
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 1);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startcal = cal.getTimeInMillis();
                long endcal = cal.getTimeInMillis() + 24 * 60 * 60 * 1000;
                List<Schedule> systemSchedule =null;
                try{
                    ScheduleUtils.getSystemCalendar(MyApplication.getInstance(), startcal, endcal);
                }catch (Exception e){

                }
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    if (dataObject != null) {
                        TimeHelperBean timeHelperBean = new TimeHelperBean();
                        Object scheduleId = dataObject.get("scheduleId");
                        if (scheduleId == null) {
                            timeHelperBean.setScheduleId(-1);
                        } else {
                            timeHelperBean.setScheduleId(JSONUtil.getInt(dataObject, "scheduleId"));
                        }
                        timeHelperBean.setImid(JSONUtil.getInt(dataObject, "imid"));
                        timeHelperBean.setType(JSONUtil.getText(dataObject, "type"));
                        timeHelperBean.setAllDay(JSONUtil.getInt(dataObject, "allDay"));
                        timeHelperBean.setRepeat(JSONUtil.getText(dataObject, "repeat"));
                        timeHelperBean.setTitle(JSONUtil.getText(dataObject, "title"));
                        timeHelperBean.setTag(JSONUtil.getText(dataObject, "tag"));
                        timeHelperBean.setRemarks(JSONUtil.getText(dataObject, "remarks"));

                        String startTime = JSONUtil.getText(dataObject, "startTime");
                        timeHelperBean.setStartTime(startTime);

                        timeHelperBean.setEndTime(JSONUtil.getText(dataObject, "endTime"));
                        timeHelperBean.setWarnTime(JSONUtil.getInt(dataObject, "warnTime"));
                        timeHelperBean.setWarnRealTime(JSONUtil.getText(dataObject, "warnRealTime"));
                        timeHelperBean.setAddress(JSONUtil.getText(dataObject, "address"));
                        timeHelperBean.setStatus(JSONUtil.getInt(dataObject, "status"));
                        timeHelperBean.setDetail(JSONUtil.getText(dataObject, "details"));
                        int genre = JSONUtil.getInt(dataObject, "genre");
                        timeHelperBean.setScheduleType(genre);
                        if (genre == 1) {
                            timeHelperBean.setFromWhere(Schedule.TYPE_BOOK);
                        } else if (genre == 2) {
                            timeHelperBean.setFromWhere(Schedule.TYPE_UU);
                        }

                        timeHelperBeans.add(timeHelperBean);

                        if (!ListUtils.isEmpty(systemSchedule)) {
                            for (Schedule e : systemSchedule) {
                                if (e.getId() == timeHelperBean.getScheduleId()) {
                                    systemSchedule.remove(e);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (systemSchedule != null) {
                    for (int i = 0; i < systemSchedule.size(); i++) {
                        Schedule schedule = systemSchedule.get(i);
                        TimeHelperBean timeHelperBean = new TimeHelperBean();
                        timeHelperBean.setScheduleId(schedule.getId());
                        timeHelperBean.setImid(0);
                        timeHelperBean.setType(schedule.getType());
                        timeHelperBean.setAllDay(schedule.getAllDay());
                        timeHelperBean.setRepeat(schedule.getRepeat());
                        timeHelperBean.setTitle(schedule.getTitle());
                        timeHelperBean.setTag(schedule.getTag());
                        timeHelperBean.setRemarks(schedule.getRemarks());

                        String startTime = DateFormatUtil.long2Str(schedule.getStartTime(), DateFormatUtil.YMD_HMS);
                        timeHelperBean.setStartTime(startTime);

                        timeHelperBean.setEndTime(DateFormatUtil.long2Str(schedule.getEndTime(), DateFormatUtil.YMD_HMS));
                        timeHelperBean.setWarnTime(schedule.getWarnTime());
                        timeHelperBean.setWarnRealTime(DateFormatUtil.long2Str(schedule.getWarnRealTime(), DateFormatUtil.YMD_HMS));
                        timeHelperBean.setAddress(schedule.getAddress());
                        timeHelperBean.setStatus(0);
                        timeHelperBean.setDetail("");
                        timeHelperBean.setScheduleType(2);
                        timeHelperBean.setFromWhere(Schedule.TYPE_PHONE);

                        timeHelperBeans.add(timeHelperBean);
                    }
                }
                Collections.sort(timeHelperBeans);

                if (timeHelperBeans.size() != 0) {
                    initSchedulePop(timeHelperBeans);
                }
            }
        }
    }


    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    public class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

        @Override
        public void onCheckUpdateCallback(final AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
            if (infoForInstall != null && !TextUtils.isEmpty(infoForInstall.getInstallPath())) {
                BDAutoUpdateSDK.cpUpdateInstall(getApplicationContext(), infoForInstall.getInstallPath());
            } else if (info != null) {
                mUpdatePopupWindow = SettingActivity.showUpdateVersionPopup(MainActivity.this, info);
                if (mUpdatePopupWindow != null) {
                    mUpdatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            if (mUpdatePopupWindow != null) {
                                mUpdatePopupWindow.dismiss();
                            }
                            clearBackgroundAlpha();
                        }
                    });
                }
            }
        }
    }

    public void updateVersion() {
        BDAutoUpdateSDK.cpUpdateCheck(this, new MyCPCheckUpdateCallback());
    }


    private void doUserCheck() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            return;
        }
        if (MyApplication.getInstance().mUserStatusChecked) {
            return;
        }
        LoginHelper.checkStatusForUpdate(this, new LoginHelper.OnCheckedFailedListener() {
            @Override
            public void onCheckFailed() {
                mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
            }
        });
    }

    private void cancelUserCheckIfExist() {
        mUserCheckHander.removeMessages(RETRY_CHECK_DELAY_MAX);
        cancelAll("checkStatus");
    }

    private void checkUserDb(final String userId) {
        // 检测用户基本数据库信息的完整性
        new Thread(new Runnable() {
            @Override
            public void run() {
                initMsgUnReadTips(userId);
            }
        }).start();
    }

    /* 当注销当前用户时，将那些需要当前用户的Fragment销毁，以后重新登陆后，重新加载为初始状态 */
    private void removeNeedUserFragment(boolean startAgain) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开始事物
        if (mMessageFragment != null) {
            fragmentTransaction.remove(mMessageFragment);
        }
        if (mMyFriendFragment != null) {
            fragmentTransaction.remove(mMyFriendFragment);
        }
        if (mWorksFragment != null) {
            fragmentTransaction.remove(mWorksFragment);
        }
        if (mGroupChatFragment != null) {
            fragmentTransaction.remove(mGroupChatFragment);
        }
        if (mMeFragment != null) {
            fragmentTransaction.remove(mMeFragment);
        }
        if (mBusinessCircleFragment != null) {
            fragmentTransaction.remove(mBusinessCircleFragment);

        }
        fragmentTransaction.commitAllowingStateLoss();
        mMessageFragment = null;
        mMyFriendFragment = null;
        mWorksFragment = null;
        mGroupChatFragment = null;
        mMeFragment = null;
        mBusinessCircleFragment = null;
        mLastFragment = null;
        if (startAgain) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


    private void doSendSignAutoLog() {
        signAutoLogEntity = new ArrayList<>();
        isendEntity = new ArrayList<>();
        signAutoLogEntity = signAutoLogDao.getAllDatas();
        if (ListUtils.isEmpty(signAutoLogEntity)) {
            Log.i("fang", "Logdatas为空");
            return;
        }
        LogUtil.prinlnLongMsg("signAutoLogEntity2", JSON.toJSONString(signAutoLogEntity));
        int size = signAutoLogEntity.size();
        for (int i = 0; i < size; i++) {
            if (signAutoLogEntity.get(i).getSendstatus() == 0) {  // 状态为0则未发送过到服务器
//                signAutoLogEntity.remove(i);
                isendEntity.add(signAutoLogEntity.get(i));
            }
            if (i == (size - 1)) {
                LogSendToHttp(isendEntity);  // 开始发送
            }
        }
    }

    private void LogSendToHttp(List<SignAutoLogEntity> isendEntity) {
        String url = "http://113.105.74.140:8092/user/appAutoLog";
        Map<String, Object> param = new HashMap<>();
        param.put("map", JSON.toJSONString(isendEntity));
        LogUtil.prinlnLongMsg("isendEntity", JSON.toJSONString(isendEntity));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        if (platform) {
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        } else {
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        }
        ViewUtil.httpSendRequest(this, url, param, mUserCheckHander, headers, LOG_SEND_REQUEST, null, null, "post");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void saveOfflineTime() {
        long time = System.currentTimeMillis() / 1000;//将现在的时间存起来,
        Log.d("wang", "time_destory::" + time + "");
        PreferenceUtils.putLong(this, Constants.OFFLINE_TIME, time);
        MyApplication.getInstance().mLoginUser.setOfflineTime(time);
        UserDao.getInstance().updateUnLineTime(MyApplication.getInstance().mLoginUser.getUserId(), time);
    }


    @Override
    protected void onDestroy() {
        saveOfflineTime();
        MyApplication.getInstance().unregisterNetWorkObserver(this);
        ListenerManager.getInstance().removeAuthStateChangeListener(this);
        if (mBind) {
            unbindService(mXmppServiceConnection);
        }
        unregisterReceiver(mUpdateUnReadReceiver);
        unregisterReceiver(mUserLogInOutReceiver);
        screenListener.unregisterListener();
        //移除广播接收
        if (autoPresenter != null) {
            autoPresenter.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveOfflineTime();
    }

    private void restoreState(Bundle savedInstanceState) {
        mLastFragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
        mMessageFragment = (MessageFragment) getSupportFragmentManager().findFragmentByTag(TAG_MESSAGE);
        mMyFriendFragment = (MyFriendFragment) getSupportFragmentManager().findFragmentByTag(TAG_MY);
        mWorksFragment = (WorkPlatFragment) getSupportFragmentManager().findFragmentByTag(TAG_NEARBY);
        mGroupChatFragment = (GroupChatFragment) getSupportFragmentManager().findFragmentByTag(TAG_GROUP_CHAT);
        mMeFragment = (MeFragment) getSupportFragmentManager().findFragmentByTag(TAG_ME);
        mBusinessCircleFragment = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(TAG_BusinessCircle);
    }

    /**
     * @desc:RadioGroup设置监听
     * @author：Administrator on 2016/1/27 16:20
     */
    private void initView(Bundle savedInstanceState) {
        mTabRadioGroup = (DivideRadioGroup) findViewById(R.id.mDivideRadioGroup);
        main_tab_one = findViewById(R.id.main_tab_one);
        main_tab_two = findViewById(R.id.main_tab_two);
        main_tab_five = findViewById(R.id.main_tab_five);
        main_tab_three = findViewById(R.id.main_tab_three);
        mTabRadioGroup.setOnCheckedChangeListener(mTabRadioGroupChangeListener);
        if (savedInstanceState == null) {
            mTabRadioGroup.check(R.id.main_tab_one);
        }
        mMsgUnReadTv = (TextView) findViewById(R.id.main_tab_one_tv);
        unWorkReadTV = (TextView) findViewById(R.id.main_tab_five_tv);
        main_tab_three_tv = (TextView) findViewById(R.id.main_tab_three_tv);

        PushManager.resumeWork(MyApplication.getInstance());

        int new_function_notice = PreferenceUtils.getInt(MyApplication.getInstance(), MainActivity.NEW_FUNCTION_NOTICE);
        if (new_function_notice == 1) {
            main_tab_three_tv.setVisibility(View.INVISIBLE);
        } else {
            main_tab_three_tv.setVisibility(View.VISIBLE);
        }

        unWorkReadTV.setVisibility(CommonUtil.getSharedPreferencesBoolean(ct, com.core.app.Constants.NEW_ME_TAG, false) ? View.GONE : View.VISIBLE);
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        initPermission();
    }

    private void changeFragment(Fragment addFragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开始事物
        if (mLastFragment == addFragment) {
            return;
        }
        if (mLastFragment != null && mLastFragment != addFragment) {// 如果最后一次加载的不是现在要加载的Fragment，那么僵最后一次加载的移出
            fragmentTransaction.detach(mLastFragment);
        }
        if (addFragment == null) {
            return;
        }
        if (!addFragment.isAdded())// 如果还没有添加，就加上
            fragmentTransaction.add(R.id.main_content, addFragment, tag);
        if (addFragment.isDetached())
            fragmentTransaction.attach(addFragment);
        mLastFragment = addFragment;

        //尝试不在activity上使用toolbar
//        if (addFragment == mWorksFragment) {
//            hideToolBar();
//        } else {
//            showToolBar();
//            Toolbar mToolBar = getCommonToolBar();
//            if (mToolBar != null) {
//                if (addFragment == mMessageFragment) {
//                    mToolBar.setBackgroundResource(R.drawable.common_toolbar_message_bg);
//                } else {
//                    mToolBar.setBackgroundResource(R.drawable.common_toolbar_bg);
//                }
//            }
//        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAuthStateChange(int authState) {
        mImStatus = authState;
        if (mTabRadioGroup.getCheckedRadioButtonId() == R.id.main_tab_one) {
            updateMessageTitle();
        }
    }

    /**
     * 更改消息在线不在线的状态
     * msg_online>消息(在线)  msg_offline>消息(离线)  msg_connect">消息(连接中)
     */
    private void updateMessageTitle() {
        int messageTitle = R.string.msg_online;
        if (mImStatus == AuthStateListener.AUTH_STATE_NOT) {
            messageTitle = R.string.msg_offline;
            // mMessageFragment.setNetNoticeVisiable(true);
        } else if (mImStatus == AuthStateListener.AUTH_STATE_ING) {
            messageTitle = R.string.msg_connect;
        } else if (mImStatus == AuthStateListener.AUTH_STATE_SUCCESS) {
            messageTitle = R.string.msg_online;
            // mMessageFragment.setNetNoticeVisiable(false);
        }
        if (mMessageFragment != null) {
            mMessageFragment.setTitle(messageTitle);
        }
    }

    @Override
    public void onNetWorkStatusChange(boolean connected) {
        // 当网络状态改变时，判断当前用户的状态，是否需要更新
        if (connected) {
            if (!MyApplication.getInstance().mUserStatusChecked) {
                mRetryCheckDelay = 0;
                mUserCheckHander.sendEmptyMessageDelayed(MSG_USER_CHECK, mRetryCheckDelay);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume start:" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        if (!AppConfig.COMPANY) return;
        isPause = false;
        if (mMsgNumNeedUpdate) {
            initMsgUnReadTips(MyApplication.getInstance().mLoginUser.getUserId());
        }
        requestMyBusiness();
        Runtime.getRuntime().gc();
        int new_function_notice = PreferenceUtils.getInt(MyApplication.getInstance(), MainActivity.NEW_FUNCTION_NOTICE);
        if (new_function_notice == 1) {
            main_tab_three_tv.setVisibility(View.INVISIBLE);
        } else {
            main_tab_three_tv.setVisibility(View.VISIBLE);
        }
        LogUtil.d("onResume end:" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));

        int new_version_rating = PreferenceUtils.getInt(MyApplication.getInstance(), MainActivity.NEW_VERSION_RATING);
        if (new_version_rating != 1 && MyApplication.getInstance().isNetworkActive()) {
            showsScoreWindowJudge();  // 显示为UU评分对话框逻辑
        }
    }

    private void showsScoreWindowJudge() {
        List<WorkModel> models = WorkModelDao.getInstance().query(true);
        if (ListUtils.isEmpty(models)) return;

        String off_time = models.get(models.size() - 1).getOffTime();
        if (StringUtil.isEmpty(off_time)) return;
        String cur_time = DateFormatUtil.long2Str(System.currentTimeMillis(), "HH:mm");
        if (cur_time.compareTo(off_time) > 0) {
            new Handler().postDelayed(new Runnable() { // 下班时间
                @Override
                public void run() {
                    initScoringWindows();  // 评价UU弹框
                }
            }, 1000);
        }
        Log.i("off_time,cur_time", off_time + "," + cur_time + ":" + cur_time.compareTo(off_time));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }


    private void initMsgUnReadTips(String userId) {// 初始化未读条数
        // 消息未读条数累加
        mMsgUnReadNum = FriendDao.getInstance().getMsgUnReadNumTotal(userId);
        mUnReadHandler.post(new Runnable() {
            @Override
            public void run() {
                updateMsgUnReadTv();
            }
        });
    }

    private void updateMsgUnReadTv() {
        if (mMsgUnReadNum > 0) {
            mMsgUnReadTv.setVisibility(View.VISIBLE);
            String numStr = mMsgUnReadNum >= 99 ? "99+" : mMsgUnReadNum + "";
            mMsgUnReadTv.setText(numStr);
        } else {
            mMsgUnReadTv.setVisibility(View.INVISIBLE);
        }
    }

    public void exitMucChat(String toUserId) {
        if (mCoreService != null) {
            mCoreService.exitMucChat(toUserId);
        }
    }

    public void sendNewFriendMessage(String toUserId, NewFriendMessage message) {
        if (mBind && mXmppService != null) {
            mXmppService.sendNewFriendMessage(toUserId, message);
        }
    }

    /**
     * 获得fragment对象
     *
     * @return
     */
    public BusinessCircleFragment getBusinessCircleFragment() {

        FragmentManager sfmanager = getSupportFragmentManager();
        return (BusinessCircleFragment) sfmanager.findFragmentByTag(TAG_BusinessCircle);
    }

    public void setUnReader(int num) {
        mMsgUnReadNum = mMsgUnReadNum - oldERPNum > 0 ? (mMsgUnReadNum - oldERPNum) : 0;
        mMsgUnReadNum = mMsgUnReadNum + num;
        oldERPNum = num;
        updateMsgUnReadTv();
    }


}