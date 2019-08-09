package com.xzjmyk.pm.activity.ui.circle.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.broadcast.MsgBroadcast;
import com.core.model.AttentionUser;
import com.core.model.Friend;
import com.core.model.NewFriendMessage;
import com.core.model.User;
import com.core.model.XmppMessage;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.DataLoadView;
import com.core.xmpp.CoreService;
import com.core.xmpp.FriendHelper;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.dao.NewFriendDao;
import com.core.xmpp.listener.NewFriendListener;
import com.core.xmpp.model.AddAttentionResult;
import com.core.xmpp.utils.CardcastUiUpdateUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.CRM.erp.activity.TaskAddErpActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.message.ChatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊处理发送非好友非本企业好友名片
 *
 * @author gongpm
 */
public class CardInfoActivity extends BaseActivity implements NewFriendListener {

    @ViewInject(R.id.max_img)
    private ImageView max_img;
    @ViewInject(R.id.avatar_img)
    private ImageView avatar_img;
    @ViewInject(R.id.phone_img)
    private ImageView phone_img;
    @ViewInject(R.id.email_img)
    private ImageView email_img;
    @ViewInject(R.id.name_tv)
    private TextView name_tv;
    @ViewInject(R.id.sub_tv)
    private TextView sub_tv;
    @ViewInject(R.id.phone_tv)
    private TextView phone_tv;
    @ViewInject(R.id.email_tv)
    private TextView email_tv;
    @ViewInject(R.id.do_next_tv)
    private TextView do_next_tv;
    @ViewInject(R.id.push_task_tv)
    private TextView push_task_tv;
    @ViewInject(R.id.data_load_view)
    private DataLoadView mDataLoadView;

    private User mUser;
    private Friend mFriend;// 如果这个用户是当前登陆者的好友或者关注着，那么该值有意义
    private ProgressDialog mProgressDialog;
    private boolean mBind;
    private CoreService mXmppService;

    private String mLoginUserId;
    private boolean isMyInfo = false;// 快捷判断
    private boolean showMenu = false;
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
    private String userId;
    private String userName;

    private PopupWindow mMoreWindow;
    private View mMoreMenuView;
    private TextView mRemarkNameTv, mRemoveBlackTv, mAddBlackTv, mCancelAttentionTv, mDeleteAllTv;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
        if (getIntent() != null) {
            userName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        }
        if (userName != null) {
            requestData();
        } else {
            ToastMessage("空的  0.0");
        }
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        setContentView(R.layout.activity_basic_info);
        ViewUtils.inject(this);
        mProgressDialog = ProgressDialogUtil.init(this, null, getString(R.string.please_wait));
        initView();
        ListenerManager.getInstance().addNewFriendListener(this);
        mBind = bindService(CoreService.getIntent(), mServiceConnection, BIND_AUTO_CREATE);
    }


    private void requestData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", String.valueOf(0));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        if (!TextUtils.isEmpty(userName)) {
            params.put("nickname", userName);
        }
        params.put("active", String.valueOf(0));

        StringJsonArrayRequest<User> request = new StringJsonArrayRequest<User>(mConfig.USER_QUERY, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);

            }
        }, new StringJsonArrayRequest.Listener<User>() {
            @Override
            public void onResponse(ArrayResult<User> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {

                    List<User> datas = result.getData();
                    if (datas != null && datas.size() > 0) {
                        userId = datas.get(0).getUserId();
                        if (userId.equals("0")) {            //当imid不存在时候
                            isMyInfo = false;
                            loadOthersInfoFromDB();
                        } else {
                            if (mLoginUserId.equals(userId)) {  //当点击的是自己的时候
                                isMyInfo = true;
                                loadMyInfoFromDb();
                            } else {
                                isMyInfo = false;
                                loadOthersInfoFromNet();
                            }
                        }
                    }
                }
            }
        }, User.class, params);
        addDefaultRequest(request);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (showMenu && mFriend != null) {
            menu.clear();//将之前的clear掉,防止重复
            if (mFriend.getStatus() != Friend.STATUS_BLACKLIST && mFriend.getStatus() == Friend.STATUS_ATTENTION
                    && mFriend.getStatus() == Friend.STATUS_FRIEND) {
            } else {
                getMenuInflater().inflate(R.menu.menu_basic_info, menu);
                /*if (mFriend.getStatus() == Friend.STATUS_BLACKLIST) {// 在黑名单中,显示“设置备注名”、“移除黑名单”,"取消关注"，“彻底删除”
                    menu.findItem(R.id.add_blacklist).setVisible(false);
                } else {
                    menu.findItem(R.id.remove_blacklist).setVisible(false);
                }*/
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showMenu && mFriend != null) {
            if (mFriend.getStatus() != Friend.STATUS_BLACKLIST && mFriend.getStatus() == Friend.STATUS_ATTENTION
                    && mFriend.getStatus() == Friend.STATUS_FRIEND) {
            } else {
                getMenuInflater().inflate(R.menu.menu_basic_info, menu);
                /*if (mFriend.getStatus() == Friend.STATUS_BLACKLIST) {// 在黑名单中,显示“设置备注名”、“移除黑名单”,"取消关注"，“彻底删除”
                    menu.findItem(R.id.add_blacklist).setVisible(false);
                } else {
                    menu.findItem(R.id.remove_blacklist).setVisible(false);
                }*/
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (mFriend == null) {
            return super.onOptionsItemSelected(item);
        }

        if (mFriend.getStatus() != Friend.STATUS_BLACKLIST && mFriend.getStatus() == Friend.STATUS_ATTENTION
                && mFriend.getStatus() == Friend.STATUS_FRIEND) {
            return super.onOptionsItemSelected(item);
        }

        int itemId = item.getItemId();
        switch (itemId) {
            /*case R.id.set_remark_name:// 设置备注名
                showRemarkDialog(mFriend);
                break;
            case R.id.remove_blacklist:// 加入黑名单，或者移除黑名单
                showBlacklistDialog(mFriend);
                break;
            case R.id.add_blacklist:// 加入黑名单，或者移除黑名单
                showBlacklistDialog(mFriend);
                break;
            case R.id.cancel_attention:// 设置备注名
                showCancelAttentionDialog(mFriend);
                break;
            case R.id.delete_all:// 设置备注名
                showDeleteAllDialog(mFriend);
                break;*/
            case R.id.basic_info_more:

                if (mFriend.getStatus() == Friend.STATUS_BLACKLIST) {// 在黑名单中,显示“设置备注名”、“移除黑名单”,"取消关注"，“彻底删除”
                    mAddBlackTv.setVisibility(View.GONE);
                    mRemoveBlackTv.setVisibility(View.VISIBLE);
                } else {
                    mAddBlackTv.setVisibility(View.VISIBLE);
                    mRemoveBlackTv.setVisibility(View.GONE);
                }

                mMoreWindow.showAtLocation(CardInfoActivity.this.findViewById(R.id.basic_info_ll), Gravity.BOTTOM, 0, 0);
                DisplayUtil.backgroundAlpha(this, 0.5f);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeMorePopupWindow() {
        if (mMoreWindow != null) {
            mMoreWindow.dismiss();
//            mMoreWindow = null;
            DisplayUtil.backgroundAlpha(this, 1f);

        }

    }

    /**
     * 懒得判断操作的用户到底属于好友、企业、还是公司，直接发广播，让所有的名片盒页面都更新
     */
    private void updateAllCardcastUi() {
        CardcastUiUpdateUtil.broadcastUpdateUi(this);
    }

    private void loadMyInfoFromDb() {
        mDataLoadView.showSuccess();
        mUser = MyApplication.getInstance().mLoginUser;
        updateUI();
    }

    private void loadOthersInfoFromDB() {
        mDataLoadView.setVisibility(View.GONE);
        String code = null;
        if (getIntent() != null) {
            code = getIntent().getStringExtra(AppConstant.EXTRA_NICK_CODE);
        }
        name_tv.setText(userName);
        phone_tv.setText(code);
        do_next_tv.setText("该用户暂未开通app");
        do_next_tv.setClickable(false);
        do_next_tv.setPressed(true);
        push_task_tv.setVisibility(View.GONE);
    }

    private void loadOthersInfoFromNet() {
        mDataLoadView.showLoading();
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("userId", userId);
        LogUtil.i("access_token=" + MyApplication.getInstance().mAccessToken);
        LogUtil.i("userId=" + userId);
        StringJsonObjectRequest<User> request = new StringJsonObjectRequest<User>(mConfig.USER_GET_URL,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ToastUtil.showErrorNet(mContext);
                        mDataLoadView.showFailed();
                    }
                }, new StringJsonObjectRequest.Listener<User>() {

            @Override
            public void onResponse(ObjectResult<User> result) {
                LogUtil.i("result=" + result);
                LogUtil.i("result=" + result.getData().toString());
                boolean success = Result.defaultParser(mContext, result, true);
                if (success && result.getData() != null) {
                    mUser = result.getData();
                    // 如果本地的好友状态不正确，那么就更新本地好友状态
                    AttentionUser attentionUser = mUser.getFriends();// 服务器的状态
                    boolean changed = FriendHelper.updateFriendRelationship(mLoginUserId, mUser.getUserId(),
                            attentionUser);
                    if (changed) {
                        //Log.i("LoginInfo","更新本页面所以信息....");
                        updateAllCardcastUi();
                    }

                    mDataLoadView.showSuccess();
                    updateUI();
                } else {
                    mDataLoadView.showFailed();
                }
            }
        }, User.class, params);
        addDefaultRequest(request);
    }

    private void updateUI() {
        if (mUser == null) {
            return;
        }
        if (isMyInfo) {
            setTitle(R.string.my_data);
            showMenu = false;
        } else {
            setTitle(R.string.basic_info);
            // 在这里查询出本地好友的状态
            initFriendMoreAction();
        }

        // 设置头像
        AvatarHelper.getInstance().displayAvatar(mUser.getUserId(), max_img, false);
        AvatarHelper.getInstance().displayAvatar(mUser.getUserId(), avatar_img, false);
        // 判断是否有备注名,有就显示
        if (mFriend != null) {
            if (StringUtil.isEmpty(userName)) {
                if (mFriend.getRemarkName() != null) {
                    name_tv.setText(mFriend.getRemarkName());
                } else {
                    name_tv.setText(mUser.getNickName());

                }
            } else {
                name_tv.setText(userName);
            }
            mFriend.setRemarkName(userName);
        } else {
            Log.i("LoginInfo", "设置当前用户的名字");
            //mNameTv.setText(MyApplication.getInstance().mLoginUser.getNickName());
            if (StringUtil.isEmpty(userName)) {
                name_tv.setText(mUser.getNickName());//陌生人也显示正确名字
            } else {
                name_tv.setText(userName);
            }
        }

//        mSexTv.setText(mUser.getSex() == 0 ? R.string.sex_woman : R.string.sex_man);
//        if (mUser.getSex() == -1) mSexTv.setText("未填写");
//        mBirthdayTv.setText(TimeUtils.sk_time_s_long_2_str(mUser.getBirthday()));
//        mCityTv.setText(Area.getProvinceCityString(mUser.getProvinceId(), mUser.getCityId()));
        phone_tv.setText(mUser.getTelephone());
        // ActionBtn 的初始化
        if (isMyInfo) {// 如果是我自己，不显示ActionBtn
            do_next_tv.setVisibility(View.GONE);
            push_task_tv.setVisibility(View.GONE);
        } else {
            do_next_tv.setVisibility(View.VISIBLE);
            if (mFriend == null) {
                do_next_tv.setText(R.string.add_attention);
                do_next_tv.setOnClickListener(new AddAttentionListener());
            } else {
                switch (mFriend.getStatus()) {
                    case Friend.STATUS_BLACKLIST:// 在黑名单中，显示移除黑名单
                        do_next_tv.setText(R.string.remove_blacklist);
                        do_next_tv.setOnClickListener(new RemoveBlacklistListener());
                        break;
                    case Friend.STATUS_ATTENTION:// 已经是关注了，显示打招呼
                        do_next_tv.setText(R.string.say_hello);
                        do_next_tv.setOnClickListener(new SayHelloListener());
                        break;
                    case Friend.STATUS_FRIEND:// 已经是朋友了，显示发消息
                        do_next_tv.setText(R.string.send_msg);
                        do_next_tv.setOnClickListener(new SendMsgListener());
                        break;
                    default:// 其他（理论上不可能的哈，容错）
                        do_next_tv.setText(R.string.add_attention);
                        do_next_tv.setOnClickListener(new AddAttentionListener());
                        break;
                }
            }

            push_task_tv.setVisibility(View.VISIBLE);
            push_task_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(CardInfoActivity.this, TaskAddErpActivity.class);
                    mIntent.putExtra("people", name_tv.getText().toString());
                    startActivity(mIntent);
                }
            });
        }
        invalidateOptionsMenu();
    }

    private void initFriendMoreAction() {
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUser.getUserId());// 更新好友的状态
        showMenu = mFriend != null;
    }

    protected void onDestroy() {
        super.onDestroy();
        ListenerManager.getInstance().removeNewFriendListener(this);
        if (mBind) {
            unbindService(mServiceConnection);
        }
    }

    private void initView() {
        setTitle(R.string.basic_info);

        phone_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!StringUtil.isEmpty(phone_tv.getText().toString())) {
                    String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                    Pattern regex = Pattern.compile(check);
                    Matcher matcher = regex.matcher(phone_tv.getText().toString());
                    boolean isMatched = matcher.matches();
                    if (isMatched) {
                        SystemUtil.phoneAction(CardInfoActivity.this,phone_tv.getText().toString());
                    } else {
                        ViewUtil.ShowMessageTitle(CardInfoActivity.this, "手机格式不正确！");
                    }

                }
            }
        });
        mDataLoadView.setLoadingEvent(new DataLoadView.LoadingEvent() {
            @Override
            public void load() {
                loadOthersInfoFromNet();
            }
        });

        mMoreMenuView = View.inflate(getApplicationContext(), R.layout.layout_menu_person_info, null);
        mRemarkNameTv = (TextView) mMoreMenuView.findViewById(R.id.basic_info_set_remark_name);
        mRemoveBlackTv = (TextView) mMoreMenuView.findViewById(R.id.basic_info_remove_blacklist);
        mAddBlackTv = (TextView) mMoreMenuView.findViewById(R.id.basic_info_add_blacklist);
        mCancelAttentionTv = (TextView) mMoreMenuView.findViewById(R.id.basic_info_cancel_attention);
        mDeleteAllTv = (TextView) mMoreMenuView.findViewById(R.id.basic_info_delete_all);

        mRemarkNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemarkDialog(mFriend);
                closeMorePopupWindow();
            }
        });
        mRemoveBlackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBlacklistDialog(mFriend);
                closeMorePopupWindow();
            }
        });
        mAddBlackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBlacklistDialog(mFriend);
                closeMorePopupWindow();
            }
        });
        mCancelAttentionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelAttentionDialog(mFriend);
                closeMorePopupWindow();
            }
        });
        mDeleteAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAllDialog(mFriend);
                closeMorePopupWindow();
            }
        });
        mMoreWindow = new PopupWindow(mMoreMenuView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mMoreWindow.setAnimationStyle(R.style.MenuAnimationFade);
        mMoreWindow.setBackgroundDrawable(new BitmapDrawable());
        mMoreWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closeMorePopupWindow();
            }
        });

    }



    public void doSayHello() {
        final EditText editText = new EditText(this);
        editText.setMaxLines(2);
        editText.setLines(2);
        editText.setHint(R.string.say_hello_dialog_hint);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        editText.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.say_hello_dialog_title)
                .setView(editText).setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String text = editText.getText().toString().trim();
                        doSayHello(text);
                    }
                }).setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    private void doSayHello(String text) {
        if (TextUtils.isEmpty(text)) {
            text = getString(R.string.say_hello_default);
        }
        NewFriendMessage message = NewFriendMessage.createWillSendMessage(MyApplication.getInstance().mLoginUser,
                XmppMessage.TYPE_SAYHELLO, text, mUser);
        NewFriendDao.getInstance().createOrUpdateNewFriend(message);
        mXmppService.sendNewFriendMessage(mUser.getUserId(), message);
        // 提示打招呼成功
        ToastUtil.showToast(this, R.string.say_hello_succ);
    }

    /**
     * @desc:添加关注
     * @author：Administrator on 2016/3/22 20:00
     */
    private void doAddAttention() {
        if (mUser == null) {
            return;
        }
        ProgressDialogUtil.show(mProgressDialog);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", mUser.getUserId());

        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                mConfig.FRIENDS_ATTENTION_ADD, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showErrorNet(mContext);
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success && result.getData() != null) {// 接口加关注成功
                    if (result.getData().getType() == 1 || result.getData().getType() == 3) {// 单方关注成功或已经是关注的
                        // 发送推送消息
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_NEWSEE, null, mUser);
                        mXmppService.sendNewFriendMessage(mUser.getUserId(), message);
                        // 添加为关注
                        NewFriendDao.getInstance().ascensionNewFriend(message, Friend.STATUS_ATTENTION);
                        FriendHelper.addAttentionExtraOperation(mLoginUserId, mUser.getUserId());

                        // 提示加关注成功
                        ToastUtil.showToast(mContext, R.string.add_attention_succ);
                        // 更新界面
                        do_next_tv.setText(R.string.say_hello);
                        do_next_tv.setOnClickListener(new SayHelloListener());
                        // 由陌生关系变为关注了,那么右上角更多操作可以显示了
                        initFriendMoreAction();
                        // 更新名片盒
                        updateAllCardcastUi();
                        invalidateOptionsMenu();
                    } else if (result.getData().getType() == 2 || result.getData().getType() == 4) {// 已经是好友了
                        // 发送推送的消息
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_FRIEND, null, mUser);
                        mXmppService.sendNewFriendMessage(mUser.getUserId(), message);

                        // 添加为好友
                        NewFriendDao.getInstance().ascensionNewFriend(message, Friend.STATUS_FRIEND);
                        FriendHelper.addFriendExtraOperation(mLoginUserId, mUser.getUserId());

                        // 提示加好友成功
                        ToastUtil.showToast(mContext, R.string.add_friend_succ);
                        // 更新界面
                        do_next_tv.setText(R.string.send_msg);
                        do_next_tv.setOnClickListener(new SendMsgListener());
                        // 由陌生或者关注变为好友了,那么右上角更多操作可以显示了
                        initFriendMoreAction();
                        // 更新名片盒
                        updateAllCardcastUi();
                        invalidateOptionsMenu();
                    } else if (result.getData().getType() == 5) {
                        ToastUtil.showToast(mContext, R.string.add_attention_failed);
                    }
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
            }
        }, AddAttentionResult.class, params);
        addDefaultRequest(request);
    }

    @Override
    public void onNewFriendSendStateChange(String toUserId, NewFriendMessage message, int messageState) {
    }

    @Override
    public boolean onNewFriend(NewFriendMessage message) {
        return false;
    }

    private void showRemarkDialog(final Friend friend) {
        final EditText editText = new EditText(this);
        editText.setMaxLines(2);
        editText.setLines(2);
        editText.setText(friend.getShowName());
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        editText.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.set_remark_name).setView(editText)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();
                        if (input.equals(friend.getShowName())) {// 备注名没变
                            return;
                        }
                        if (!StringUtil.isNickName(input)) {// 不符合昵称
                            if (input.length() != 0) {
                                ToastUtil.showToast(mContext, R.string.remark_name_format_error);
                                return;
                            } else {// 不符合昵称，因为长度为0，但是可以做备注名操作，操作就是清除备注名
                                // 判断之前有没有备注名
                                if (TextUtils.isEmpty(friend.getRemarkName())) {// 如果没有备注名，就不需要清除
                                    return;
                                }
                            }
                        }
                        remarkFriend(friend, input);
                    }
                }).setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    private void remarkFriend(final Friend friend, final String remarkName) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", friend.getUserId());
        params.put("remarkName", remarkName);

        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.FRIENDS_REMARK,
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ProgressDialogUtil.dismiss(mProgressDialog);
                        ToastUtil.showErrorNet(mContext);
                    }
                }, new StringJsonObjectRequest.Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    friend.setRemarkName(remarkName);
                    // 更新到数据库
                    FriendDao.getInstance().setRemarkName(MyApplication.getInstance().mLoginUser.getUserId(),
                            friend.getUserId(), remarkName);

                    // 更新界面显示
                    // TODO
                    // if (TextUtils.isEmpty(remarkName)) {// 清除了备注名
                    // mRemarkNamell.setVisibility(View.GONE);
                    // } else {
                    // mRemarkNamell.setVisibility(View.VISIBLE);
                    // mRemarkNameTv.setText(remarkName);
                    // }

                    updateAllCardcastUi();
                    // 改了昵称，通知消息界面更新
                    MsgBroadcast.broadcastMsgUiUpdate(mContext);
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
                updateUI();
            }
        }, Void.class, params);
        addDefaultRequest(request);

    }

    /**
     * 取消关注
     *
     * @param friend
     */
    private void showCancelAttentionDialog(final Friend friend) {
        if (friend.getStatus() == Friend.STATUS_UNKNOW) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.prompt_title)
                .setMessage(R.string.cancel_attention_prompt)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFriend(friend, 0);
                    }
                }).setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    private void showDeleteAllDialog(final Friend friend) {
        if (friend.getStatus() == Friend.STATUS_UNKNOW) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.prompt_title)
                .setMessage(R.string.delete_all_prompt)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFriend(friend, 1);
                    }
                }).setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    /**
     * @param friend
     * @param type   0 取消关注 <br/>
     *               1、彻底删除<br/>
     */
    private void deleteFriend(final Friend friend, final int type) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", friend.getUserId());

        String url = null;
        if (type == 0) {
            url = mConfig.FRIENDS_ATTENTION_DELETE;// 取消关注
        } else {
            url = mConfig.FRIENDS_DELETE;// 删除好友
        }

        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(url, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showErrorNet(mContext);
            }
        }, new StringJsonObjectRequest.Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    if (type == 0) {
                        ToastUtil.showToast(mContext, R.string.cancel_attention_succ);
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_DELSEE, null, friend);
                        mXmppService.sendNewFriendMessage(mUser.getUserId(), message);// 解除关注
                    } else {
                        ToastUtil.showToast(mContext, R.string.delete_all_succ);
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_DELALL, null, friend);
                        mXmppService.sendNewFriendMessage(mUser.getUserId(), message);// 解除好友
                    }

                    FriendHelper.removeAttentionOrFriend(mLoginUserId, friend.getUserId());
                    updateAllCardcastUi();
                    /* 更新本界面 */
                    // 1、备注名没有了//TODO
                    // mRemarkNamell.setVisibility(View.GONE);
                    // 2、mFriend设置为null
                    mFriend = null;
                    // 右上角没有更多操作
                    showMenu = false;
                    invalidateOptionsMenu();
                    // Action Btn设置为打招呼
                    do_next_tv.setText(R.string.add_attention);
                    do_next_tv.setOnClickListener(new AddAttentionListener());
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
            }
        }, Void.class, params);
        addDefaultRequest(request);

    }

    /* 显示加入黑名单的对话框 */
    private void showBlacklistDialog(final Friend friend) {
        int messageId = 0;
        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {// 已经在黑名单，那就是移出黑名单
            messageId = R.string.remove_blacklist_prompt;
        } else if (friend.getStatus() == Friend.STATUS_ATTENTION || friend.getStatus() == Friend.STATUS_FRIEND) {
            messageId = R.string.add_blacklist_prompt;
        } else {// 其他关系（错误的状态）
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.prompt_title)
                .setMessage(messageId)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {// 已经在黑名单，那就是移出黑名单
                            removeBlacklist(friend);
                        } else if (friend.getStatus() == Friend.STATUS_ATTENTION
                                || friend.getStatus() == Friend.STATUS_FRIEND) {
                            addBlacklist(friend);
                        }

                    }
                }).setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    private void addBlacklist(final Friend friend) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", friend.getUserId());

        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.FRIENDS_BLACKLIST_ADD,
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ProgressDialogUtil.dismiss(mProgressDialog);
                        ToastUtil.showErrorNet(mContext);
                    }
                }, new StringJsonObjectRequest.Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    FriendDao.getInstance().updateFriendStatus(friend.getOwnerId(), friend.getUserId(),
                            Friend.STATUS_BLACKLIST);
                    FriendHelper.addBlacklistExtraOperation(mLoginUserId, friend.getUserId());

                    updateAllCardcastUi();

                    // Action Btn设置为打招呼
                    do_next_tv.setText(R.string.remove_blacklist);
                    do_next_tv.setOnClickListener(new RemoveBlacklistListener());

							/* 发送加入黑名单的通知 */
                    if (friend.getStatus() == Friend.STATUS_FRIEND) {// 之前是好友，需要发消息让那个人不能看我的商务圈
                        NewFriendMessage message = NewFriendMessage.createWillSendMessage(
                                MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_BLACK, null, friend);
                        mXmppService.sendNewFriendMessage(friend.getUserId(), message);// 加入黑名单
                    }

                    friend.setStatus(Friend.STATUS_BLACKLIST);
                    ToastUtil.showToast(mContext, R.string.add_blacklist_succ);
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
            }
        }, Void.class, params);
        addDefaultRequest(request);

    }

    private void removeBlacklist(final Friend friend) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", friend.getUserId());

        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<AttentionUser> request = new StringJsonObjectRequest<AttentionUser>(
                mConfig.FRIENDS_BLACKLIST_DELETE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showErrorNet(mContext);
            }
        }, new StringJsonObjectRequest.Listener<AttentionUser>() {
            @Override
            public void onResponse(ObjectResult<AttentionUser> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    int currentStatus = Friend.STATUS_UNKNOW;
                    if (result.getData() != null) {
                        currentStatus = result.getData().getStatus();
                    }
                    FriendDao.getInstance().updateFriendStatus(friend.getOwnerId(), friend.getUserId(),
                            currentStatus);
                    friend.setStatus(currentStatus);
                    updateAllCardcastUi();

                    switch (currentStatus) {
                        case Friend.STATUS_ATTENTION:
                            do_next_tv.setText(R.string.say_hello);
                            do_next_tv.setOnClickListener(new SayHelloListener());
                            NewFriendMessage message1 = NewFriendMessage.createWillSendMessage(
                                    MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_NEWSEE, null, friend);
                            mXmppService.sendNewFriendMessage(friend.getUserId(), message1);

                            FriendHelper.addAttentionExtraOperation(friend.getOwnerId(), friend.getUserId());
                            break;
                        case Friend.STATUS_FRIEND:
                            do_next_tv.setText(R.string.send_msg);
                            do_next_tv.setOnClickListener(new SendMsgListener());

                            NewFriendMessage message2 = NewFriendMessage.createWillSendMessage(
                                    MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_FRIEND, null, mUser);
                            mXmppService.sendNewFriendMessage(mUser.getUserId(), message2);
                            FriendHelper.addFriendExtraOperation(friend.getOwnerId(), friend.getUserId());
                            break;
                        default:// 其他，理论上不可能
                            do_next_tv.setText(R.string.add_attention);
                            do_next_tv.setOnClickListener(new AddAttentionListener());
                            break;
                    }

                    ToastUtil.showToast(mContext, R.string.remove_blacklist_succ);
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
            }
        }, AttentionUser.class, params);
        addDefaultRequest(request);
    }

    // 加关注
    private class AddAttentionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            doAddAttention();
        }
    }

    // 打招呼
    private class SayHelloListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            doSayHello();
        }
    }

    // 发消息
    private class SendMsgListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            MsgBroadcast.broadcastMsgUiUpdate(CardInfoActivity.this);
            MsgBroadcast.broadcastMsgNumReset(CardInfoActivity.this);
            Intent intent = new Intent(mContext, ChatActivity.class);
            mFriend.setRemarkName(userName);
            intent.putExtra(AppConstant.FRIEND, mFriend);
            startActivity(intent);
        }
    }

    // 移除黑名单
    private class RemoveBlacklistListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mFriend == null || mFriend.getStatus() != Friend.STATUS_BLACKLIST) {
                return;
            }
            removeBlacklist(mFriend);
            mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUser.getUserId());// 更新好友的状态
        }
    }
}
