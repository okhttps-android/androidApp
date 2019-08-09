package com.core.xmpp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.common.LogUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.SystemUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.model.NewFriendMessage;
import com.core.model.XmppMessage;
import com.core.xmpp.listener.AuthStateListener;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;

import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.util.StringUtils;

public class CoreService extends Service {
    static final boolean DEBUG = true;
    static final String TAG = "Xmpp CoreService";
    private static final Intent SERVICE_INTENT = new Intent();


    static {
        SERVICE_INTENT.setComponent(new ComponentName("com.xzjmyk.pm.activity",
                "com.core.xmpp.CoreService"));
    }

    public static Intent getIntent() {
        return SERVICE_INTENT;
    }

    public static Intent getIntent(Context context, String userId, String password, String nickName) {

        Intent intent = new Intent(context, CoreService.class);
        intent.putExtra(EXTRA_LOGIN_USER_ID, userId);
        intent.putExtra(EXTRA_LOGIN_PASSWORD, password);
        intent.putExtra(EXTRA_LOGIN_NICK_NAME, nickName);
        return intent;
    }

    private static final String EXTRA_LOGIN_USER_ID = "login_user_id";
    private static final String EXTRA_LOGIN_PASSWORD = "login_password";
    private static final String EXTRA_LOGIN_NICK_NAME = "login_nick_name";

    // Binder
    public class CoreServiceBinder extends Binder {
        public CoreService getService() {

            return CoreService.this;
        }
    }

    private int notifyId = 1003020303;

    public void notificationMesage(ChatMessage chatMessage) {
        int messageType = chatMessage.getType();
        boolean isNotification = PreferenceUtils.getBoolean(getApplicationContext(), Constants.IS_NOTIFICATION);//进行通知
        isNotification = SystemUtil.isBackground(getApplicationContext());
//        if (!isNotification) {
//            return;
//        }
        String content;
        switch (messageType) {
            case XmppMessage.TYPE_TEXT:
                content = chatMessage.getContent();
                break;
            case XmppMessage.TYPE_IMAGE:
                content = "收到一张新图片";
                break;
            case XmppMessage.TYPE_VOICE: 
                content = "收到一条新的语音";
                break;
            case XmppMessage.TYPE_LOCATION:
                content = "收到一个地址";
                break;
            case XmppMessage.TYPE_GIF:
                content = "收到一个动画表情";
                break;
            case XmppMessage.TYPE_VIDEO:
                content = "收到新录像";
                break;
            case XmppMessage.TYPE_FILE:
                content = "收到新文件";
                break;
            case XmppMessage.TYPE_CARD:
                content = "收到一张名片";
                break;
            case XmppMessage.TYPE_TIP:
                content = "系统消息";
                break;
            default:
                content = "不明确类型";
                break;
        }
        NotificationManager mNotificationManager = null;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        String userId = chatMessage.getFromUserId();
        Intent intent = new Intent("com.modular.main.MainActivity");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(chatMessage == null ? "新消息" : chatMessage.getFromUserName())//设置通知栏标题
                .setContentText(content)//通知内容
//                .setNumber(4) //设置通知集合的数量
                .setTicker("您有一条新的信息..") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_ALL)
                //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.drawable.uuu)//设置通知小ICON
                .setLargeIcon(BitmapFactory.decodeResource(
                getResources(), R.drawable.uuu));
              

        if (!"不明确类型".equals(content)) {
            mNotificationManager.notify(notifyId, mBuilder.build());
        }
    }

    private CoreServiceBinder mBinder;

    /* 当前登陆用户的基本属性 */
    private String mLoginUserId;
    @SuppressWarnings("unused")
    private String mLoginNickName;
    private String mLoginPassword;

    private SmackAndroid mSmackAndroid;// Smack唯一

    private XmppConnectionManager mConnectionManager;// 唯一
    private ReceiptManager mReceiptManager;// 唯一
    private XChatManager mXChatManager;// 唯一
    private XMucChatManager mXMucChatManager;// 唯一

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter("ALARMSERVICE_DESTROY");
        mSmackAndroid = SmackAndroid.init(this);
        mBinder = new CoreServiceBinder();
        if (CoreService.DEBUG) {
            Log.d(CoreService.TAG, "CoreService OnCreate");
        }

		/*IntentFilter filter = new IntentFilter(Constants.GROUP_JOIN_NOTICE_ACTION);
        registerReceiver(broadcastReceiver, filter);*/
    }


    @Override
    public IBinder onBind(Intent intent) {// 绑定服务只是为了提供一些外部调用的方法
        if (CoreService.DEBUG) {
            Log.d(CoreService.TAG, "CoreService onBind");
        }
        return mBinder;
    }

    private void initConnection() {
        mConnectionManager = new XmppConnectionManager(this, mNotifyConnectionListener);
        // mConnection.addPacketListener(packetListener, packetFilter);
        /* 添加Provider */
        // addProvider();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        if (mSmackAndroid != null) {
            mSmackAndroid.onDestroy();
            mSmackAndroid = null;
        }

        if (CoreService.DEBUG) {
            Log.d(CoreService.TAG, "CoreService onDestroy");
        }
    }

    private void release() {
        if (mConnectionManager != null) {
            mConnectionManager.release();
            mConnectionManager = null;
        }
        mReceiptManager = null;
        mXChatManager = null;
        mXMucChatManager = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (CoreService.DEBUG) {
            Log.d(CoreService.TAG, "CoreService onStartCommand:" + flags);
        }
        if (intent != null) {
            mLoginUserId = intent.getStringExtra(EXTRA_LOGIN_USER_ID);
            mLoginPassword = intent.getStringExtra(EXTRA_LOGIN_PASSWORD);
            mLoginNickName = intent.getStringExtra(EXTRA_LOGIN_NICK_NAME);
        }
        if (CoreService.DEBUG) {
            Log.e(CoreService.TAG, "登陆Xmpp账户为   mLoginUserId：" + mLoginUserId + "   mPassword：" + mLoginPassword);
        }

        if (mConnectionManager != null) {
            // 因为Xmpp可能在后台重启，重启时读取的是保存在SharePreference中的Host和Port。 如果程序启动的时候，从服务器获取的Host和Port和本地不一致，那么就需要重新创建一个新的XmppConnection，并且重置使用前一个XmppConnection的所有对象
            String xmppHost = MyApplication.getInstance().getConfig().XMPPHost;// 当前的host
            int xmppPort = MyApplication.getInstance().getConfig().XMPP_PORT;// 当前的port
            if (mConnectionManager.getHost() == null || mConnectionManager.getPort() == 0 || !mConnectionManager.getHost().equals(xmppHost)
                    || mConnectionManager.getPort() != xmppPort) {
                release();
                if (CoreService.DEBUG) {
                    Log.e(CoreService.TAG, "重新创建ConnectionManager");
                }
            }
        }

        if (mConnectionManager == null) {
            initConnection();
        }

        if (!TextUtils.isEmpty(mLoginUserId) && !TextUtils.isEmpty(mLoginPassword)) {
            mConnectionManager.login(mLoginUserId, mLoginPassword);
        }
        // return START_REDELIVER_INTENT;//根据flags判断在登陆的时候是否需要验证Token过期信息
        return START_NOT_STICKY;
    }

    private NotifyConnectionListener mNotifyConnectionListener = new NotifyConnectionListener() {
        @Override
        public void notifyConnectionClosedOnError(Exception arg0) {
            if (CoreService.DEBUG)
                Log.e(CoreService.TAG, "连接异常断开");
            LogUtil.i("arg0="+arg0.getMessage());
            ListenerManager.getInstance().notifyAuthStateChange(AuthStateListener.AUTH_STATE_NOT);
        }

        @Override
        public void notifyConnectionClosed() {
            if (CoreService.DEBUG)
                Log.e(CoreService.TAG, "连接断开");
            LogUtil.i("notifyConnectionClosed=");
            ListenerManager.getInstance().notifyAuthStateChange(AuthStateListener.AUTH_STATE_NOT);
        }

        @Override
        public void notifyConnected(XMPPConnection arg0) {
            if (CoreService.DEBUG)
                Log.e(CoreService.TAG, "Xmpp已经连接");
            ListenerManager.getInstance().notifyAuthStateChange(AuthStateListener.AUTH_STATE_SUCCESS);
        }

        @Override
        public void notifyAuthenticated(XMPPConnection arg0) {
            if (CoreService.DEBUG)
                Log.e(CoreService.TAG, "Xmpp已经认证");
            String connectionUserName = StringUtils.parseName(arg0.getUser());
            if (connectionUserName==null) return;
            if (!connectionUserName.equals(mLoginUserId)) {
                if (CoreService.DEBUG) {
                    Log.e(CoreService.TAG, "Xmpp登陆账号不匹配，重新登陆");
                }
                mConnectionManager.login(mLoginUserId, mLoginPassword);// 重新登陆
            } else {
                init();
                Log.e("roamer", "初始化");
                ListenerManager.getInstance().notifyAuthStateChange(AuthStateListener.AUTH_STATE_SUCCESS);// 通知登陆成功
            }
        }

        @Override
        public void notifyConnectting() {
            ListenerManager.getInstance().notifyAuthStateChange(AuthStateListener.AUTH_STATE_ING);
        }
    };

    public boolean isAuthenticated() {
        if (mConnectionManager != null && mConnectionManager.isAuthenticated()) {
            return true;
        }
        return false;
    }

    // PacketListener packetListener = new PacketListener() {
    // @Override
    // public void processPacket(Packet arg0) throws NotConnectedException {
    // Log.d("roamer", "PacketListener");
    // }
    // };
    //
    // PacketFilter packetFilter = new PacketFilter() {
    // @Override
    // public boolean accept(Packet arg0) {
    // if (arg0 instanceof Message) {
    // return false;
    // } else {
    // return true;
    // }
    // }
    // };

    public void logout() {
        if (CoreService.DEBUG)
            Log.e(CoreService.TAG, "Xmpp登出");
        if (mConnectionManager != null) {
            mConnectionManager.logout();
        }
        stopSelf();
    }

    private void init() {
        Log.d("roamer", "这里会初始化消息各种");
        if (!isAuthenticated()) {
            return;
        }

		/* 消息回执管理 */
        if (mReceiptManager == null) {
            mReceiptManager = new ReceiptManager(mConnectionManager.getConnection());
        } else {
            mReceiptManager.reset();
        }

        // 初始化消息处理
        if (mXChatManager == null) {
            mXChatManager = new XChatManager(this, mConnectionManager.getConnection());
        } else {
            mXChatManager.reset();
        }

        /**
         * TODO 群聊的暂时不加 做的时候要考虑<br/>
         * 1、每次连接被断开，加入的房间是不是就退出了，如果是，那么每次都需要在这里new一个新的对象。 <br/>
         * 2、XmppDomain可能因为重启导致改变
         */
        if (mXMucChatManager == null) {
            mXMucChatManager = new XMucChatManager(this, mConnectionManager.getConnection());
        } else {
            // 重启导致Domain可能不一致
            // String xmppDomain=Config.getXmppDoMain(this);//当前的host
            // if(mXMucChatManager.getMucNickName(roomJid))
            mXMucChatManager.reset();
        }
        //		mConnectionManager.presenceOnline();
        /* 获取离线消息 */
        mConnectionManager.handOfflineMessage();
    }

    // private void addProvider() {
    // ProviderManager providerManager = ProviderManager.getInstance();
    // providerManager.addIQProvider("query", "jabber:iq:jixiong:RoomIQHandler", new MucIQProvider());
    // }

    /**
     * 发送聊天消息
     *
     * @param toUserId
     * @param chatMessage
     */
    public void sendChatMessage(String toUserId, ChatMessage chatMessage) {

        if (mXChatManager == null) {
            if (CoreService.DEBUG)
                Log.d(CoreService.TAG, "mXChatManager==null");
        }
        if (!isAuthenticated()) {
            if (CoreService.DEBUG)
                Log.d(CoreService.TAG, "isAuthenticated==false");
        }

        if (mReceiptManager == null) {
            if (CoreService.DEBUG)
                Log.d(CoreService.TAG, "mReceiptManager==null");
        }

        if (mXChatManager == null || !isAuthenticated() || mReceiptManager == null) {
            ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, toUserId, chatMessage.get_id(),
                    ChatMessageListener.MESSAGE_SEND_FAILED);
        } else {
            mReceiptManager.addWillSendMessage(toUserId, chatMessage, ReceiptManager.SendType.NORMAL);
            mXChatManager.sendMessage(toUserId, chatMessage);
        }
    }

    /**
     * @param toUserId
     * @param message
     * @return
     */
    public void sendNewFriendMessage(String toUserId, NewFriendMessage message) {
        if (mXChatManager == null || !isAuthenticated() || mReceiptManager == null) {
            ListenerManager.getInstance().notifyNewFriendSendStateChange(toUserId, message, ChatMessageListener.MESSAGE_SEND_FAILED);
        } else {
            mReceiptManager.addWillSendMessage(toUserId, message, ReceiptManager.SendType.PUSH_NEW_FRIEND);
            mXChatManager.sendMessage(toUserId, message);
        }
    }

    /* 群聊的外部接口 */
    public boolean isMucEnable() {
        return isAuthenticated() && mXMucChatManager != null;
    }

    public String createMucRoom(String myNickName, String roomName, String roomSubject, String roomDesc) {
        if (isMucEnable()) {
            return mXMucChatManager.createMucRoom(myNickName, roomName, roomSubject, roomDesc);
        }
        return null;
    }

    public void invite(String roomId, String userId, String reason) {
        if (isMucEnable()) {
            mXMucChatManager.invite(roomId, userId, reason);
        }
    }

    // 踢人
    public boolean kickParticipant(String roomJid, String nickName) {
        if (isMucEnable()) {
            return mXMucChatManager.kickParticipant(roomJid, nickName);
        }
        return false;
    }

    public void sendMucChatMessage(String toUserId, ChatMessage chatMessage) {
        if (mXMucChatManager == null) {
            if (CoreService.DEBUG)
                Log.d(CoreService.TAG, "mXMucChatManager==null");
        }
        if (!isAuthenticated()) {
            if (CoreService.DEBUG)
                Log.d(CoreService.TAG, "isAuthenticated==false");
        }

        if (mReceiptManager == null) {
            if (CoreService.DEBUG)
                Log.d(CoreService.TAG, "mReceiptManager==null");
        }

        if (mXMucChatManager == null || !isAuthenticated() || mReceiptManager == null) {
            ListenerManager.getInstance().notifyMessageSendStateChange(mLoginUserId, toUserId, chatMessage.get_id(),
                    ChatMessageListener.MESSAGE_SEND_FAILED);
        } else {
            mReceiptManager.addWillSendMessage(toUserId, chatMessage, ReceiptManager.SendType.NORMAL);
            mXMucChatManager.sendMessage(toUserId, chatMessage);
        }

    }

    /* 加入群聊 */
    public void joinMucChat(String toUserId, String nickName, int lastSeconds) {
        if (isMucEnable()) {
            mXMucChatManager.joinMucChat(toUserId, nickName, lastSeconds);
        }
    }

    public void exitMucChat(String toUserId) {
        if (isMucEnable()) {
            mXMucChatManager.exitMucChat(toUserId);
        }
    }
}
