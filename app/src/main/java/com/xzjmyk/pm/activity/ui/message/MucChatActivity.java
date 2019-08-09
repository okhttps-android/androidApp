package com.xzjmyk.pm.activity.ui.message;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.CalendarUtil;
import com.common.preferences.PreferenceUtils;
import com.common.ui.CameraUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.base.view.AndroidBug5497Workaround2;
import com.core.broadcast.MsgBroadcast;
import com.core.model.Friend;
import com.core.model.User;
import com.core.model.XmppMessage;
import com.core.net.http.ViewUtil;
import com.core.net.volley.FastVolley;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.ReceiptManager;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.listener.MucListener;
import com.core.xmpp.model.ChatMessage;
import com.core.xmpp.model.MucRoom;
import com.core.xmpp.model.MucRoomMember;
import com.core.xmpp.widget.ChatBottomView;
import com.core.xmpp.widget.ChatContentView;
import com.core.xmpp.widget.PullDownListView;
import com.ipaulpro.afilechooser.FileUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.applocation.UasLocationHelper;
import com.uas.appme.other.activity.BasicInfoActivity;
import com.uas.appme.other.activity.RoomInfoActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.circle.SendBaiDuLocate;
import com.xzjmyk.pm.activity.ui.me.LocalVideoActivity;
import com.xzjmyk.pm.activity.util.im.Constants;
import com.xzjmyk.pm.activity.util.im.downloader.Downloader;
import com.xzjmyk.pm.activity.util.im.helper.UploadEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天主界面
 */
public class MucChatActivity extends SupportToolBarActivity
        implements ChatContentView.MessageEventListener, ChatBottomView.ChatBottomListener, ChatMessageListener, MucListener {

    @SuppressWarnings("unused")
    private TextView mAuthStateTipTv;
    private ChatContentView mChatContentView;
    private ChatBottomView mChatBottomView;
    private AudioManager mAudioManager = null;
    private String mLoginUserId;
    private String mLoginNickName;
    private List<ChatMessage> mChatMessages;// 存储聊天消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 5) {
            } else {
            }

        }
    };
    private LinearLayout botton_ll;
    private CoreService mService;
    private boolean mHasSend = false;// 有没有发送过消息，发送过需要更新界面
    private FastVolley mFastVolley;
    private String mUseId;// 当前聊天对象的UserId
    private String mNickName;// 当前聊天对象的昵称（房间就是房间名称）
    private boolean isGroupChat;// 是否是群聊
    private static String MUC_ChAT_ACT = "MucChatActivity";
    private boolean isError = false;
    public static final int RETURN_CODE = 108;
    private Friend mFriend;
    private ChatMessage instantMessage;
    private String instantFilePath;// 转发文件传过来的path
    private ImageView tv_none;
    private ImageView tv_delete;
    private String[] noticeFriendList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        AndroidBug5497Workaround2.assistActivity(this);
        Bundle bundle = null;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else if (getIntent() != null) {
            bundle = getIntent().getExtras();
        }
        if (bundle != null) {
            mUseId = bundle.getString(AppConstant.EXTRA_USER_ID);
            mNickName = bundle.getString(AppConstant.EXTRA_NICK_NAME);
            isGroupChat = bundle.getBoolean(AppConstant.EXTRA_IS_GROUP_CHAT, false);
            setTitle(mNickName);
            noticeFriendList = bundle.getStringArray(Constants.GROUP_JOIN_NOTICE);// 获得加入群新朋友的列表
        }
        if (TextUtils.isEmpty(mUseId) || TextUtils.isEmpty(mNickName)) {
            isError = true;
            return;
        }
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        mLoginNickName = MyApplication.getInstance().mLoginUser.getNickName();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mChatMessages = new ArrayList<>();
        mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUseId);
        Downloader.getInstance().init(MyApplication.getInstance().mAppDir + File.separator + mLoginUserId
                + File.separator + Environment.DIRECTORY_MUSIC);
        initView();
        // 表示已读
        FriendDao.getInstance().markUserMessageRead(mLoginUserId, mUseId);
        loadDatas(true);
        ListenerManager.getInstance().addChatMessageListener(this);
        ListenerManager.getInstance().addMucListener(this);
        bindService(CoreService.getIntent(), mConnection, BIND_AUTO_CREATE);
        instantMessage = getIntent().getParcelableExtra(Constants.INSTANT_MESSAGE);
        instantFilePath = getIntent().getStringExtra(Constants.INSTANT_MESSAGE_FILE);// 只有转发文件才会有
        IntentFilter filter = new IntentFilter(Constants.CHAT_MESSAGE_DELETE_ACTION);
        registerReceiver(broadcastReceiver, filter);

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("wang", "接收到广播");
            if (intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_FALG, 10000) == 1) {//当广播为删除一个信息时候
                if (mChatContentView != null) {
                    int position = intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, 10000);
                    if (position == 10000) {
                        return;
                    }
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

    /**
     * 给新加入群的小伙伴们发通知
     */
    private void sendNoticeJoinNewFriend() {
        if (noticeFriendList != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendNotice(getString(R.string.new_join_son_to_chating));
                    noticeFriendList = null;// 防止重复发送提示消息
                }
            }, 1000);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppConstant.EXTRA_USER_ID, mUseId);
        outState.putString(AppConstant.EXTRA_NICK_NAME, mNickName);
        outState.putBoolean(AppConstant.EXTRA_IS_GROUP_CHAT, isGroupChat);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((CoreService.CoreServiceBinder) service).getService();
            if (mService != null && isGroupChat) {
                Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, mUseId);
                if (friend != null) {
                    mService.joinMucChat(mUseId, mLoginNickName, friend.getTimeSend());
                }
            }
        }
    };

    private void initView() {
        mAuthStateTipTv = (TextView) findViewById(R.id.auth_state_tip);
        mChatContentView = (ChatContentView) findViewById(R.id.chat_content_view);
        mChatContentView.setToUserId(mUseId);
        mChatContentView.setRoomNickName(mNickName);
        mChatContentView.setData(mChatMessages);
        tv_delete = (ImageView) findViewById(R.id.tv_delete);
        tv_none = (ImageView) findViewById(R.id.tv_none);
        mChatContentView.setMessageEventListener(this);
        mChatContentView.setRefreshListener(new PullDownListView.RefreshingListener() {
            @Override
            public void onHeaderRefreshing() {
                loadDatas(false);
            }
        });
        botton_ll = (LinearLayout) findViewById(R.id.botton_ll);
        mChatBottomView = (ChatBottomView) findViewById(R.id.chat_bottom_view);
        mChatBottomView.setChatBottomListener(this);
        mChatBottomView.isMuc(true);
        if (mFriend != null) {
            mChatBottomView.roomId(mFriend.getRoomId());
        }
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
                //TODO 确定删除选择聊天记录
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

    public void updateUi(String roomName) {
        if (TextUtils.isEmpty(roomName)) {
            Log.d("wang", "mNickName");
            setTitle(mNickName);
        } else {
            Log.d("wang", "roomName");
            setTitle(roomName);
        }
    }

    @Override
    protected void onResume() {
        PreferenceUtils.putBoolean(this, Constants.IS_NOTIFICATION, false);//不进行通知
        super.onResume();

        /*
         * String roomName = MyApplication.getInstance().roomName;
		 * updateUi(roomName);
		 */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_muc_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.room_info) {
            Intent intent = new Intent(this, RoomInfoActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, mUseId);
            if (mNickName != null && mNickName.length() > 0)
                intent.putExtra(RoomInfoActivity.ROOM_NAME, mNickName);
            startActivityForResult(intent, 0x11);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        super.onDestroy();
        if (isError) {
            return;
        }
        if (mFastVolley != null)
            mFastVolley.cancelAll(MUC_ChAT_ACT);
        mChatBottomView.recordCancel();
        ListenerManager.getInstance().removeChatMessageListener(this);
        ListenerManager.getInstance().removeMucListener(this);
        unbindService(mConnection);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onPause() {
        if (isError) {
            super.onPause();
            return;
        }
        PreferenceUtils.putBoolean(this, Constants.IS_NOTIFICATION, true);
        mChatContentView.reset();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instantChatMessage();
        sendNoticeJoinNewFriend();

    }

    /**
     * 转发消息
     */
    private void instantChatMessage() {
        if (instantMessage != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    int messageType = instantMessage.getType();
                    if (messageType == XmppMessage.TYPE_TEXT) {// 转发文字

                        sendText(instantMessage.getContent());
                    } else if (messageType == XmppMessage.TYPE_IMAGE) {// 转发图片
                        if (instantMessage.getFromUserId().equals(mLoginUserId)) {
                            sendImage(new File(instantMessage.getFilePath()));

                        } else {
                            File file = ImageLoader.getInstance().getDiscCache().get(instantMessage.getContent());
                            if (file == null || !file.exists()) {// 文件不存在，那么就表示需要重新下载
                                Toast.makeText(MucChatActivity.this, "图片还没有下载,稍等一会哦", Toast.LENGTH_SHORT).show();
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
                    } else if (messageType == XmppMessage.TYPE_FILE && instantFilePath != null) {// 转发文件
                        File file = new File(instantFilePath);
                        if (file.exists()) {
                            sendFile(file);
                        } else {
                            Toast.makeText(MucChatActivity.this, "文件解析错误", Toast.LENGTH_SHORT).show();
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
        if (isError) {
            return super.onKeyDown(keyCode, event);
        }
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
        Intent intent = new Intent(mContext, BasicInfoActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, mLoginUserId);
        startActivity(intent);
    }

    @Override
    public void onFriendAvatarClick(String friendUserId) {
        Intent intent = new Intent(mContext, BasicInfoActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, friendUserId);
        startActivity(intent);
    }

    @Override
    public void onFriendAvatarLongClick(String friendUserId) {
        mChatBottomView.addEditView("@" + friendUserId.trim());
    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {
    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {
    }

    @Override
    public void onEmptyTouch() {
        mChatBottomView.reset();
    }

    @Override
    public void onSendAgain(ChatMessage message) {
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                UploadEngine.uploadImFile(mUseId, message, mUploadResponse);
            } else {
                send(message);
            }
        } else {
            send(message);
        }
        // mService.sendChatMessage(mFriend.getUserId(), chatMessage);
    }

    /***************
     * ChatBottomView的回调
     ***************************************/

    private void send(ChatMessage message) {
        if (isGroupChat) {// 群聊
            mService.sendMucChatMessage(mUseId, message);
        } else {// 单聊
            mService.sendChatMessage(mUseId, message);
        }
    }

    private void sendMessage(ChatMessage message) {
        if (mFriend != null && mFriend.getRoomTalkTime() > (System.currentTimeMillis() / 1000)) {
            mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUseId);// 重新去加载一遍数据
            if (mFriend != null && mFriend.getRoomTalkTime() > (System.currentTimeMillis() / 1000)) {
                ToastUtil.showToast(mContext, "你已经被禁言，不能发言");
                return;
            }
        }
        mHasSend = true;
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        if (isGroupChat && !TextUtils.isEmpty(mFriend.getRoomMyNickName()) && message.getType() != XmppMessage.TYPE_CARD) {
            message.setFromUserName(mFriend.getRoomMyNickName());
        }
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mUseId, message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                UploadEngine.uploadImFile(mUseId, message, mUploadResponse);
            } else {
                send(message);
            }
        } else {
            send(message);
        }
        loadMembers(message.getContent());//百度推送，过滤本用户
    }

    //把信息转发百度推送
    public void sendPushTask(String from, String to, String body, int[] str) {
        String url = "http://113.105.74.140:8092/tigase/push";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("text", JSON.toJSONString(str));
        params.put("body", body);
        params.put("roomid", mFriend.getRoomId());
        Log.i("push", "推送百度 from:" + from + "to:" + to + "body:" + body + "text:" +
                JSON.toJSONString(str));
        ViewUtil.httpSendRequest(this, url, params, mHandler, null, 5, null, null, "get");
    }

    private void loadMembers(final String body) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mFriend.getRoomId());
        Log.d("wang", "mAccessToken::" + MyApplication.getInstance().mAccessToken + "roomId" + mFriend.getRoomId());
        StringJsonObjectRequest<MucRoom> request = new StringJsonObjectRequest<MucRoom>(MyApplication.getInstance().getConfig().ROOM_GET, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(mContext);

            }
        }, new StringJsonObjectRequest.Listener<MucRoom>() {
            @Override
            public void onResponse(ObjectResult<MucRoom> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success && result.getData() != null) {
                    List<MucRoomMember> members = result.getData().getMembers();
                    int[] str = new int[members.size() - 1];
                    for (int i = 0, j = 0; i < members.size(); i++) {
                        if (!members.get(i).getUserId().equals(MucChatActivity.this.mLoginUserId)) {
                            str[j] = Integer.parseInt(members.get(i).getUserId());
                            j++;
                        }
                    }
                    //TODO 获取完群组成员id后发送百度推送
                    sendPushTask(mLoginUserId, mFriend.getUserId(), body, str);
                } else {
                    //TODO 当获取群成员失败时候 重新获取 直到获取成功
                    loadMembers(body);
                }
            }
        }, MucRoom.class, params);
        if (mFastVolley == null)
            mFastVolley = MyApplication.getInstance().getFastVolley();
        mFastVolley.addDefaultRequest(MUC_ChAT_ACT, request);
    }


    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {
        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            send(message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            for (int i = 0; i < mChatMessages.size(); i++) {
                ChatMessage msg = mChatMessages.get(i);
                if (message.get_id() == msg.get_id()) {
                    msg.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mUseId, message.get_id(),
                            ChatMessageListener.MESSAGE_SEND_FAILED);
                    mChatContentView.notifyDataSetInvalidated(false);
                    break;
                }
            }
        }

    };

    @Override
    public void stopVoicePlay() {
        mChatContentView.stopPlayVoice();
    }

    /**
     * 新加入群发送通知
     *
     * @param text
     */
    private void sendNotice(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TIP);
        message.setContent(text);
        message.setFromUserName(mLoginNickName);
        message.setFromUserId(mLoginUserId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        mChatMessages.add(message);
        mChatContentView.notifyDataSetInvalidated(true);
        sendMessage(message);
    }

    @Override
    public void sendText(String text) {
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
        Intent intent = new Intent(this, SelectCardActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_CARD);
    }

    @Override
    public void clickPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickCamera() {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickVideo() {
        Intent intent = new Intent(mContext, LocalVideoActivity.class);
        intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDE0);
    }

    @Override
    public void clickAudio() {
    }

    @Override
    public void clickVideoChat() {
        // TODO Auto-generated method stub

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
        Intent intent = new Intent(mContext, SendBaiDuLocate.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_Locate);
    }

    @Override
    public void clickCard() {
        sendCard(mLoginUserId);
    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (isGroupMsg != isGroupChat) {
            return false;
        }
        if (mUseId.compareToIgnoreCase(fromUserId) == 0) {// 是该人的聊天消息
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
        List<ChatMessage> chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId, mUseId, mMinId,
                mPageSize);
        if (chatLists == null || chatLists.size() <= 0) {
            mHasMoreData = false;
        } else {
            long currentTime = System.currentTimeMillis() / 1000;

            for (int i = 0; i < chatLists.size(); i++) {
                ChatMessage message = chatLists.get(i);
                if (message.isMySend() && message.getMessageState() == ChatMessageListener.MESSAGE_SEND_ING) {// 如果是我发的消息，有时候在消息发送中，直接退出了程序，此时消息发送状态可能使用是发送中，
                    if (currentTime - message.getTimeSend() > ReceiptManager.MESSAGE_DELAY / 1000) {
                        ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mUseId, message.get_id(),
                                ChatMessageListener.MESSAGE_SEND_FAILED);
                        message.setMessageState(ChatMessageListener.MESSAGE_SEND_FAILED);
                    }
                }
                mChatMessages.add(0, message);
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
    private static final int REQUEST_CODE_SELECT_FILE = 4;
    private static final int REQUEST_CODE_SELECT_Locate = 5;
    private static final int REQUEST_CODE_SELECT_CARD = 6;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0x9088) {
            Intent intent = new Intent();
            setResult(0x11, intent);
            finish();
        } else if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == RESULT_OK) {
                if (mNewPhotoUri != null) {
                    sendImage(new File(mNewPhotoUri.getPath()));
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                    return;
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    sendImage(new File(CameraUtil.getImagePathFromUri(this, data.getData())));
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_VIDE0 && resultCode == RESULT_OK) {// 选择视频的返回
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
        } else if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
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
            if (!file.exists()) {
                ToastUtil.showToast(this, R.string.select_failed);
                return;
            }
            sendFile(file);
        } else if (requestCode == REQUEST_CODE_SELECT_Locate && resultCode == RESULT_OK) {
            double latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
            double longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
            String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)) {
                sendLocate(latitude, longitude, address);
            } else {
                ToastUtil.showToast(mContext, "请把定位开启!");
            }
        } else if (requestCode == REQUEST_CODE_SELECT_CARD && resultCode == RESULT_OK) {//选择名片返回
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
        } else if (requestCode == RETURN_CODE && resultCode == RETURN_CODE) {
            String nameReturn = data.getStringExtra("NickName");
            String userId = data.getStringExtra("UserId");   //获取到的userid
            mChatBottomView.addEditView(nameReturn);
        }
    }

    private void loadCardInfo(String userId, final ChatMessage message) {
        //TODO 发送名片时候 访问网络获取性别
        if (mFastVolley == null)
            mFastVolley = MyApplication.getInstance().getFastVolley();
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
        mFastVolley.addDefaultRequest(MUC_ChAT_ACT, request);
    }

    /****************************
     * MUC Listener
     ********************/
    @Override
    public void onDeleteMucRoom(String toUserId) {
        if (toUserId != null && toUserId.equals(mUseId)) {
            ToastUtil.showToast(mContext, "房间 " + mNickName + " 已被删除");
            finish();
        }
    }

    @Override
    public void onMyBeDelete(String toUserId) {
        if (toUserId != null && toUserId.equals(mUseId)) {
            ToastUtil.showToast(mContext, "你被踢出了房间：" + mNickName);
            finish();
        }
    }

    @Override
    public void onNickNameChange(String toUserId, String changedUserId, String changedName) {
        if (toUserId != null && toUserId.equals(mUseId)) {
            if (changedUserId.equals(mLoginUserId)) {
                mFriend.setRoomMyNickName(changedName);
                mChatContentView.setRoomNickName(changedName);
            }
            mChatMessages.clear();
            loadDatas(true);
        }
    }

    @Override
    public void onMyVoiceBanned(String toUserId, int time) {
        if (toUserId != null && toUserId.equals(mUseId)) {
            mFriend.setRoomTalkTime(time);
        }
    }

}
