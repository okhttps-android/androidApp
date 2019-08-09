package com.xzjmyk.pm.activity.ui.message;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.ListUtils;
import com.common.preferences.PreferenceUtils;
import com.common.thread.ThreadPool;
import com.common.ui.CameraUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.base.view.AndroidBug5497Workaround2;
import com.core.broadcast.MsgBroadcast;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.model.User;
import com.core.model.XmppMessage;
import com.core.net.http.ViewUtil;
import com.core.net.volley.FastVolley;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.ReceiptManager;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.downloader.Downloader;
import com.core.xmpp.downloader.UploadEngine;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;
import com.core.xmpp.utils.flie.FileUtils;
import com.core.xmpp.widget.ChatBottomView;
import com.core.xmpp.widget.ChatContentView;
import com.core.xmpp.widget.PullDownListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appcontact.R;
import com.uas.appcontact.db.TopContactsDao;
import com.uas.applocation.UasLocationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * 聊天主界面
 *
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.xzjmyk.pm.activity.ui.message
 * @作者:王阳
 * @创建时间: 2015年10月9日 下午2:48:05
 * @描述: TODO
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容: TODO 修改聊天，点击用户头像，加入黑名单，返回，还可以和加入黑名单的用户聊天的bug
 */
public class ChatActivity extends SupportToolBarActivity
        implements ChatContentView.MessageEventListener, ChatBottomView.ChatBottomListener, ChatMessageListener {


    @SuppressWarnings("unused")
    private TextView mAuthStateTipTv;
    private ImageView tv_none;
    private ImageView tv_delete;
    private LinearLayout botton_ll;
    private ChatContentView mChatContentView;
    private ChatBottomView mChatBottomView;
    private AudioManager mAudioManager = null;
    private String mLoginUserId;
    private String mLoginNickName;
    private Friend mFriend;// 存储所有的当前聊天对象
    private List<ChatMessage> mChatMessages;// 存储聊天消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    // Log.i("push", msg.getData().getString("result"));
                    break;
            }
        }
    };
    private CoreService mService;
    private List<Friend> mBlackList;

    private boolean mHasSend = false;// 有没有发送过消息，发送过需要更新界面
    private static final int REQUEST_CODE_SELECT_FILE = 4;
    private static final int REQUEST_CODE_SELECT_Locate = 5;
    private static final int REQUEST_CODE_SELECT_CARD = 6;
    private ChatMessage instantMessage;//转发消息传过来的message
    private String instantFilePath;//转发文件传过来的path
    private FastVolley mFastVolley;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        AndroidBug5497Workaround2.assistActivity(this);
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        mLoginNickName = MyApplication.getInstance().mLoginUser.getNickName();
        if (savedInstanceState != null) {
            mFriend = (Friend) savedInstanceState.getSerializable(AppConstant.EXTRA_FRIEND);
        } else if (getIntent() != null) {
            mFriend = (Friend) getIntent().getSerializableExtra(AppConstant.EXTRA_FRIEND);
        }
        mAudioManager = (AudioManager) getSystemService(android.app.Service.AUDIO_SERVICE);
        mChatMessages = new ArrayList<>();
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + mLoginUserId
                + File.separator + Environment.DIRECTORY_MUSIC);
        initView();
        // 表示已读
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, mFriend.getUserId());
        loadDatas(true);
        ListenerManager.getInstance().addChatMessageListener(this);
        bindService(CoreService.getIntent(), mConnection, Context.BIND_AUTO_CREATE);

        instantMessage = getIntent().getParcelableExtra(Constants.INSTANT_MESSAGE);
        instantFilePath = getIntent().getStringExtra(Constants.INSTANT_MESSAGE_FILE);//只有转发文件才会有
        IntentFilter filter = new IntentFilter(Constants.CHAT_MESSAGE_DELETE_ACTION);
        registerReceiver(broadcastReceiver, filter);
        addPhone2Friend();
    }

    private void addPhone2Friend() {
        if (mFriend != null && TextUtils.isEmpty(mFriend.getPhone())) {
            LogUtil.i("mFriend != null && TextUtils.isEmpty(mFriend.getPhone())");
            ThreadPool.getThreadPool().addTask(new Runnable() {
                @Override
                public void run() {
                    Intent intent = getIntent();
                    String phone = null;
                    if (intent != null) {
                        phone = intent.getStringExtra("phone");
                    }
                    if (!TextUtils.isEmpty(mFriend.getEmCode())) {
                        String whichsys = CommonUtil.getMaster();
                        String[] selectionArgs = {mFriend.getEmCode(), whichsys};
                        String selection = "em_code=? and whichsys=? ";
                        EmployeesEntity emModel = DBManager.getInstance().selectForEmployee(selectionArgs, selection);
                        if (emModel != null && !TextUtils.isEmpty(emModel.getEM_MOBILE())) {
                            phone = emModel.getEM_MOBILE();
                        }
                    }
                    LogUtil.i("phone=" + phone);
                    if (!TextUtils.isEmpty(phone)) {
                        mFriend.setPhone(phone);
                    }
                }
            });
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_FALG, 10000) == 1) {//当广播为删除一个信息时候
                if (mChatContentView != null) {
                    int position = intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, 10000);
                    if (position == 10000) {
                        return;
                    }
                    position = Math.min(position, ListUtils.getSize(mChatMessages) - 1);
                    ChatMessage message = mChatMessages.get(position);
                    boolean isSuccess = ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message);
                    if (isSuccess) {

                        mChatMessages.remove(position);
                        mChatContentView.notifyDataSetInvalidated(true);
                        MsgBroadcast.broadcastMsgUiUpdate(mContext);
                    } else {
                        Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {//当广播为删除一个信息时候
                if (botton_ll != null) {
                    botton_ll.setVisibility(View.VISIBLE);
                }
                if (mChatBottomView != null) {
                    mChatBottomView.setVisibility(View.GONE);
                }

            }

        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(AppConstant.EXTRA_FRIEND, mFriend);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((CoreService.CoreServiceBinder) service).getService();

        }
    };

    private void initView() {
        String remarkName = mFriend.getRemarkName();
        if (remarkName == null) {
           // setTitle(mFriend.getNickName());
            setTitle(mFriend.getNickName());
        } else {
            //setTitle(mFriend.getRemarkName());
            setTitle(mFriend.getRemarkName());
        }
        findViewById(R.id.root_view);
        mAuthStateTipTv = (TextView) findViewById(R.id.auth_state_tip);
        mChatContentView = (ChatContentView) findViewById(R.id.chat_content_view);
        mChatContentView.setToUserId(mFriend.getUserId());
        tv_delete = (ImageView) findViewById(R.id.tv_delete);
        tv_none = (ImageView) findViewById(R.id.tv_none);
        mChatContentView.setData(mChatMessages);
        botton_ll = (LinearLayout) findViewById(R.id.botton_ll);
        mChatContentView.setMessageEventListener(this);
        mChatContentView.setRefreshListener(new PullDownListView.RefreshingListener() {
            @Override
            public void onHeaderRefreshing() {
                loadDatas(false);
            }
        });
        mChatBottomView = (ChatBottomView) findViewById(R.id.chat_bottom_view);
        mChatBottomView.setChatBottomListener(this);
        tv_none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (botton_ll != null) {
                    botton_ll.setVisibility(View.GONE);
                }
                if (mChatBottomView != null) {
                    mChatBottomView.setVisibility(View.VISIBLE);
                }
                mChatContentView.setShowCB(false);
                mChatContentView.notifyDataSetChanged();
            }
        });
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (botton_ll != null) {
                    botton_ll.setVisibility(View.GONE);
                }
                if (mChatBottomView != null) {
                    mChatBottomView.setVisibility(View.VISIBLE);
                }
                //点击删除记录时候
                onDeleteMore();
            }
        });
        
        //resetSendMsgRl(mChatContentView);
    }

    private void onDeleteMore() {
        List<Integer> ints = mChatContentView.getInts();
        ArrayList<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < ints.size(); i++) {
            messages.add(mChatMessages.get(ints.get(i)));
        }
        boolean isSuccess = ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), messages);
        if (isSuccess) {
            mChatMessages.removeAll(messages);
            mChatContentView.notifyDataSetInvalidated(true);
            MsgBroadcast.broadcastMsgUiUpdate(mContext);
//            mChatContentView.notifyDataSetChanged();
        }
    }

    private void doBack() {
        if (mHasSend) {
            MsgBroadcast.broadcastMsgUiUpdate(mContext);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        doBack();
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatBottomView.recordCancel();
        if (mFastVolley != null)
            mFastVolley.cancelAll(AppConstant.FRIEND);
        ListenerManager.getInstance().removeChatMessageListener(this);
        unbindService(mConnection);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onPause() {
        mChatContentView.reset();
        Log.i("Arison", "onPause()");
        PreferenceUtils.putBoolean(this, Constants.IS_NOTIFICATION, true);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBlackList = FriendDao.getInstance().getAllBlacklists(mLoginUserId);
        instantChatMessage();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Arison", "chatActivity onResume()");
        PreferenceUtils.putBoolean(getApplicationContext(), Constants.IS_NOTIFICATION, false);//不进行通知
        Log.i("Arison", "isNotify" + PreferenceUtils.getBoolean(getApplicationContext(), Constants.IS_NOTIFICATION));

    }

    /**
     * 转发消息
     */
    private void instantChatMessage() {
        if (instantMessage != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    int messageType = instantMessage.getType();
                    if (messageType == XmppMessage.TYPE_TEXT) {// 转发文字

                        sendText(instantMessage.getContent());
                    } else if (messageType == XmppMessage.TYPE_IMAGE) {// 转发图片
                        if (instantMessage.getFromUserId().equals(mLoginUserId)) {
                            sendImage(new File(instantMessage.getFilePath()));

                        } else {
                            File file = ImageLoader.getInstance().getDiscCache().get(instantMessage.getContent());
                            if (file == null || !file.exists()) {// 文件不存在，那么就表示需要重新下载
                                Toast.makeText(ChatActivity.this, "图片还没有下载,稍等一会哦", Toast.LENGTH_SHORT).show();
                            } else {
                                sendImage(file);
                            }
                        }
                    } else if (messageType == XmppMessage.TYPE_VOICE) {// 转发语音
                        // if(instantMessage.getFromUserId().equals(mLoginUserId)){
                        sendVoice(instantMessage.getFilePath(), instantMessage.getTimeLen());
                        // }else{
                        // }
                    } else if (messageType == XmppMessage.TYPE_LOCATION) {// 转发地址
                        sendLocate(Double.parseDouble(instantMessage.getLocation_x()),
                                Double.parseDouble(instantMessage.getLocation_y()), instantMessage.getContent());

                    } else if (messageType == XmppMessage.TYPE_VIDEO) {// 转发视频
                        sendVideo(new File(instantMessage.getFilePath()));
                    } else if (messageType == XmppMessage.TYPE_FILE && instantFilePath != null) {//转发文件
                        File file = new File(instantFilePath);
                        if (file.exists()) {
                            sendFile(file);
                        } else {
                            Toast.makeText(ChatActivity.this, "文件解析错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                    /*
                     * else if(messageType==XmppMessage.TYPE_CARD){
					 * sendCard(instantMessage.getObjectId()); }
					 */
                    instantMessage = null;
                }
            }, 1000);

        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /***************
     * ChatContentView的回调
     ***************************************/
    @Override
    public void onMyAvatarClick() {
        Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
        intent.putExtra(AppConstant.EXTRA_USER_ID, mLoginUserId);
        startActivity(intent);
    }

    @Override
    public void onFriendAvatarClick(String friendUserId) {
        Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
        intent.putExtra(AppConstant.EXTRA_USER_ID, friendUserId);
        startActivity(intent);
    }

    @Override
    public void onFriendAvatarLongClick(String friendUserId) {

    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {//点击消息

    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {//张点击

    }

    @Override
    public void onEmptyTouch() {
        mChatBottomView.reset();
    }

    @Override
    public void onSendAgain(ChatMessage message) {
        if (interprect(message)) {
            return;
        }
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                UploadEngine.uploadImFile(mFriend.getUserId(), message, mUploadResponse);
            } else {
              sendMessageByService( message);
            }
        } else {
            sendMessageByService(message);
        }
        // mService.sendChatMessage(mFriend.getUserId(), chatMessage);
    }

    /**
     * 拦截发送的消息
     *
     * @param message
     */
    public boolean interprect(ChatMessage message) {
        int len = 0;
        if (mBlackList != null) {
            for (Friend friend : mBlackList) {
                if (friend.getUserId().equals(mFriend.getUserId())) {
                    Toast.makeText(mContext, "已经加入黑名单,无法发送消息", Toast.LENGTH_SHORT).show();
                    len++;
                }
            }
        }
        Log.d("wang", "....kkkkk");
        if (len != 0) {
            // finish();
            ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, mFriend.getUserId(),
                    message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
            return true;
        }
        return false;
    }

    /***************
     * ChatBottomView的回调
     ***************************************/

    private void sendMessage(final ChatMessage message) {
        if (interprect(message)) {
            return;
        }
        Log.i("wang", "send message:" + JSON.toJSONString(message));
        mHasSend = true;
        Log.d("roamer", "开始发送消息,ChatBottomView的回调 sendmessage");
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                Log.d("roamer", "去更新服务器的数据");
                UploadEngine.uploadImFile(mFriend.getUserId(), message, mUploadResponse);

            } else {
                Log.d("roamer", "sendChatMessage....");
                sendMessageByService( message);
            }
        } else {
            Log.d("roamer", "sendChatMessage");

            sendMessageByService( message);
            //进行百度推送
            sendPushTask(mLoginUserId, mFriend.getUserId(), message.getContent());

        }
        TopContactsDao.api().addGoodFriend(mFriend);
    }

    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {
        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            sendMessageByService(message);

        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            for (int i = 0; i < mChatMessages.size(); i++) {
                ChatMessage msg = mChatMessages.get(i);
                if (message.get_id() == msg.get_id()) {
                    msg.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                            message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
                    mChatContentView.notifyDataSetInvalidated(false);
                    break;
                }
            }
        }
    };

    private void sendMessageByService(ChatMessage message){
        if (mService!=null&&mFriend!=null){
            mService.sendChatMessage(mFriend.getUserId(), message);
        }
    }
    /**
     * 停止播放聊天的录音
     */
    @Override
    public void stopVoicePlay() {
        mChatContentView.stopPlayVoice();
    }

    @Override
    public void sendText(String text) {
        Log.d("wang", "sendText");
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent(text);
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);


    }

    @Override
    public void sendGif(String text) {
        Log.d("wang", "sendgif");
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_GIF);
        message.setContent(text);
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendVoice(String filePath, int timeLen) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VOICE);
        message.setContent("");
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setTimeLen(timeLen);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);

        sendMessage(message);

    }

    public void sendImage(File file) {
        if (!file.exists()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setContent("");
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendVideo(File file) {
        if (!file.exists()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VIDEO);
        message.setContent("");
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    public void sendFile(File file) {
        if (!file.exists()) {
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_FILE);
        message.setContent("");
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        Log.d("roamer", "开始发送文件");
        sendMessage(message);
    }

    public void sendLocate(double latitude, double longitude, String address) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_LOCATION);
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        message.setLocation_x(latitude + "");
        message.setLocation_y(longitude + "");
        message.setContent(address);
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    private String objectId;

    //TODO 发送卡片
    public void sendCard(String ObjectId) {
        this.objectId = ObjectId;
        //TODO 选择名片用户
        Intent intent = new Intent("com.modular.main.SelectCardActivity");
        startActivityForResult(intent, REQUEST_CODE_SELECT_CARD);
    }


    @Override
    public void clickPhoto() {
        Log.d("roamer", "clickphoto");
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickCamera() {
        Log.d("roamer", "clickCamera");
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickVideo() {
        Intent intent = new Intent("com.modular.im.LocalVideoActivity");
        intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDE0);
    }

    @Override
    public void clickAudio() {//语音通话
//    	Intent intent=new Intent(ChatActivity.this,Main.class);
//    	Log.d("wang","usid.getUserId..."+mFriend.getUserId());
//    	intent.putExtra(Constants.AUDIO_PHONENUMBER, mFriend.getUserId());
//    	intent.putExtra(Constants.IS_AUDIO_OR_VIDEO, true);//true为语音
//    	startActivity(intent);

    }

    @Override
    public void clickVideoChat() {//视频通话
//    	Intent intent=new Intent(ChatActivity.this,Main.class);
//    	Log.d("wang","usid.getUserId..."+mFriend.getUserId());
//    	intent.putExtra(Constants.AUDIO_PHONENUMBER, mFriend.getUserId());
//    	intent.putExtra(Constants.IS_AUDIO_OR_VIDEO, false);//true为语音
//    	startActivity(intent);
    }

    @Override
    public void clickFile() {
        // Intent intent = new Intent(mContext, MemoryFileManagement.class);
        // startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    public void clickLocation() {
        Intent intent = new Intent("com.modular.im.SendBaiDuLocate");
        startActivityForResult(intent, REQUEST_CODE_SELECT_Locate);
    }

    @Override
    public void clickCard() {
        sendCard(mLoginUserId);
    }

    /**
     * 新消息到来
     */
    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (isGroupMsg) {
            return false;
        }
        if (mFriend.getUserId().compareToIgnoreCase(fromUserId) == 0) {// 是该人的聊天消息
            Log.i("wang", "单聊界面，新消息到来：" + JSON.toJSONString(message));
            mChatMessages.add(message);
            mChatContentView.notifyDataSetInvalidated(true);
            return true;
        }
        return false;
    }

    @Override
    public void onMessageSendStateChange(int messageState, int msg_id) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage msg = mChatMessages.get(i);
            if (msg_id == msg.get_id()) {
                msg.setMessageState(messageState);
                mChatContentView.notifyDataSetInvalidated(false);
                break;
            }
        }
    }

    private int mMinId = 0;
    private int mPageSize = 20;
    private boolean mHasMoreData = true;

    private void loadDatas(final boolean scrollToBottom) {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).get_id();
        } else {
            mMinId = 0;
        }
        List<ChatMessage> chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                mFriend.getUserId(), mMinId, mPageSize);
        if (chatLists == null || chatLists.size() <= 0) {
            mHasMoreData = false;
        } else {
            long currentTime = System.currentTimeMillis() / 1000;

            for (int i = 0; i < chatLists.size(); i++) {
                ChatMessage message = chatLists.get(i);
                if (message.isMySend() && message.getMessageState() == ChatMessageListener.MESSAGE_SEND_ING) {// 如果是我发的消息，有时候在消息发送中，直接退出了程序，此时消息发送状态可能使用是发送中，
                    if (currentTime - message.getTimeSend() > ReceiptManager.MESSAGE_DELAY / 1000) {
                        ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                                message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
                        message.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    }
                }
                mChatMessages.add(0, message);
                Log.i("table", "cardId=" + message.getCardId());
            }
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatContentView.notifyDataSetInvalidated(scrollToBottom);
                mChatContentView.headerRefreshingCompleted();
                if (!mHasMoreData) {
                    mChatContentView.setNeedRefresh(false);
                }
            }
        }, 1);
    }

    /***********************
     * 拍照和选择照片
     **********************/
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private Uri mNewPhotoUri;

    private static final int REQUEST_CODE_SELECT_VIDE0 = 3;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("roamer", "进入到activityResult...");
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                Log.d("roamer", "拍照返回...");
                if (mNewPhotoUri != null) {
                    sendImage(new File(mNewPhotoUri.getPath()));
                } else {
                    // ToastUtil.showToast(this,
                    // R.string.c_take_picture_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                Log.d("roamer", "选择了一张图片...");
                if (data != null && data.getData() != null) {
                    sendImage(new File(CameraUtil.getImagePathFromUri(this, data.getData())));
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_VIDE0 && resultCode == Activity.RESULT_OK) {// 选择视频的返回
            if (data == null) {
                return;
            }
            String filePath = data.getStringExtra(AppConstant.EXTRA_FILE_PATH);
            if (TextUtils.isEmpty(filePath)) {
                ToastUtil.showToast(this, R.string.select_failed);
                return;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                ToastUtil.showToast(this, R.string.select_failed);
                return;
            }
            sendVideo(file);
        } else if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            String filePath = null;
            if (data != null) {
                // Get the URI of the selected file
                final Uri uri = data.getData();
                Log.i(TAG, "Uri = " + uri.toString());
                try {
                    // Get the file path from the URI
                    filePath = FileUtils.getPath(this, uri);
                } catch (Exception e) {
                    Log.e("roamer", "File select error", e);
                }
            }
            // String filePath = data.getStringExtra(AppConstant.FILE_PAT_NAME);
            if (TextUtils.isEmpty(filePath)) {
                ToastUtil.showToast(this, R.string.select_failed);
                return;
            }
            File file = new File(filePath);
            Log.d("roamer", file.getAbsolutePath());
            if (!file.exists()) {
                ToastUtil.showToast(this, R.string.select_failed);
                return;
            }
            sendFile(file);
        } else if (requestCode == REQUEST_CODE_SELECT_Locate && resultCode == Activity.RESULT_OK) {
            double latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
            double longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
            String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)) {
                sendLocate(latitude, longitude, address);
            } else {
                ToastUtil.showToast(mContext, "请把定位开启!");
            }
        } else if (requestCode == REQUEST_CODE_SELECT_CARD && resultCode == Activity.RESULT_OK) {//选择名片返回
            Friend card = (Friend) data.getSerializableExtra("card");
            ChatMessage message = new ChatMessage();

            message.setType(XmppMessage.TYPE_CARD);
            message.setContent(card.getNickName());
            message.setFromUserName(mLoginNickName);
            message.setObjectId(card.getUserId());
            message.setFromUserId(mLoginUserId);
            message.setMySend(true);
            message.setTimeSend(CalendarUtil.getSecondMillion());
//            message.setObjectId(objectId);
            mChatMessages.add(message);
            sendMessage(message);
            mChatContentView.notifyDataSetInvalidated(true);
        }
    }

    private void loadCardInfo(String userId, final ChatMessage message) {
        //TODO 发送名片时候 访问网络获取性别
        mFastVolley = MyApplication.getInstance().getFastVolley();
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("userId", userId);
        Log.i("LoginInfo", "发送网络数据：userid=" + userId);
        StringJsonObjectRequest<User> request = new StringJsonObjectRequest<User>(mConfig.USER_GET_URL,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        message.setContent(-1 + "");//   当失败时候显示保密
                        // 0表示女，1表示男
                        sendMessage(message);
                    }
                }, new StringJsonObjectRequest.Listener<User>() {

            @Override
            public void onResponse(ObjectResult<User> result) {

                boolean success = Result.defaultParser(mContext, result, true);
                if (success && result.getData() != null) {
                    User mUser = result.getData();
                    message.setContent(mUser.getSex() + "");//   成功时候显示性别
                    // 0表示女，1表示男
                    sendMessage(message);
                } else {
                    message.setContent(-1 + "");//   当失败时候显示保密
                    // 0表示女，1表示男
                    sendMessage(message);
                }
            }
        }, User.class, params);
        mFastVolley.addDefaultRequest(AppConstant.FRIEND, request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_muc_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.room_info) {
            Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
            intent.putExtra(AppConstant.EXTRA_USER_ID, mFriend.getUserId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * @desc:推送给百度服务
     * @author：Administrator on 2016/3/14 19:37
     */
    public void sendPushTask(String from, String to, String body) {
        //http://113.105.74.140:8092/tigase/notify?from=100118&to=100254&body=rrrrr&ts=1
        //http://192.168.253.244:8092/tigase/notify
        String url = "http://113.105.74.140:8092/tigase/notify";
        Map<String, Object> params = new HashMap<String, Object>();
//        int[] str;
//        params.put("text", str);
        params.put("from", from);
        params.put("to", to);
        params.put("body", body);
        params.put("ts", 1);
        Log.i("push", "推送百度 from:" + from + "to:" + to + "body:" + body);
        ViewUtil.httpSendRequest(this, url, params, mHandler, null, 5, null, null, "get");
    }


    private void resetSendMsgRl(View  rlContent){

        final View decorView=getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect=new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = getScreenHeight();
                int heightDifference = screenHeight - rect.bottom;//计算软键盘占有的高度  = 屏幕高度 - 视图可见高度
                RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) rlContent.getLayoutParams();
                layoutParams.setMargins(0,0,0,heightDifference);//设置rlContent的marginBottom的值为软键盘占有的高度即可
                rlContent.requestLayout();
            }
        });
    }


    private int getScreenHeight(){
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        return  height;
    }
}
