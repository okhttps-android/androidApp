package com.xzjmyk.pm.activity.util.im;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.CalendarUtil;
import com.common.data.StringUtil;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.model.Friend;
import com.core.model.XmppMessage;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.xmpp.CoreService;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.model.Area;
import com.core.xmpp.model.ChatMessage;
import com.core.xmpp.model.MucRoom;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.bean.oa.MeetUserEntity;
import com.xzjmyk.pm.activity.util.im.helper.UploadEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 创建群组
 * Created by gongp on 2016/8/9.
 */
public class CreateRoomUtil {
    private static final String TAG = "CreateRoomUtil";
    private static CreateRoomUtil instance = null;
    private CoreService mXmppService = null;
    private List<MeetUserEntity> entities = null;
    private String roomName = null;
    private String doc = null;
    private Context context = null;
    private String roomJid = null;

    private CreateRoomUtil() {
    }

    public static CreateRoomUtil getInstance() {
        if (instance == null) {
            synchronized (CreateRoomUtil.class) {
                instance = new CreateRoomUtil();
            }
        }
        return instance;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((CoreService.CoreServiceBinder) service).getService();
            //当获取绑定成功
            if (entities != null && entities.size() > 0 && roomName != null && doc != null && context != null) {
                createGroupChat(context, roomName, null, doc, entities);
            }
        }
    };
    //发送消息到好友
    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {
        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            mXmppService.sendChatMessage(roomJid, message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
        }
    };

    private void createGroupChat(final Context context, final String roomName, String roomSubject, String roomDesc, final List<MeetUserEntity> inviteUsers) {
        final List<String> list = new ArrayList<>();
        for (int i = 0; i < inviteUsers.size(); i++) {
            list.add(inviteUsers.get(i).getImId());
        }
        AppConfig mConfig = MyApplication.getInstance().getConfig();
        String nickName = MyApplication.getInstance().mLoginUser.getNickName();
        //创建群组

        try {
            roomJid = mXmppService.createMucRoom(nickName, roomName, roomSubject, roomDesc);
        } catch (NullPointerException e) {
            roomJid = mXmppService.createMucRoom(nickName, roomName, "", roomDesc);
        }
        if (TextUtils.isEmpty(roomJid)) {
            ToastUtil.showToast(context, R.string.create_room_failed);
            result(false, null);
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("jid", roomJid);
        params.put("name", roomName);
        params.put("desc", roomDesc);
        params.put("countryId", String.valueOf(Area.getDefaultCountyId()));// 国家Id
        Area area = Area.getDefaultProvince();
        if (area != null) {
            params.put("provinceId", String.valueOf(area.getId()));// 省份Id
        }
        area = Area.getDefaultCity();
        params.put("category", 0 + "");
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));// 城市Id
            area = Area.getDefaultDistrict(area.getId());
            if (area != null) {
                params.put("areaId", String.valueOf(area.getId()));// 城市Id
            }
        }
        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        params.put("text", JSON.toJSONString(list));
        StringJsonObjectRequest<MucRoom> request = new StringJsonObjectRequest<MucRoom>(mConfig.ROOM_ADD,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        result(false, null);
                    }
                }, new StringJsonObjectRequest.Listener<MucRoom>() {
            @Override
            public void onResponse(final ObjectResult<MucRoom> result) {
                boolean parserResult = Result.defaultParser(context, result, true);
                if (parserResult && result.getData() != null) {
                    list.add(0, MyApplication.getInstance().mLoginUser.getUserId());
                    RoomImageUtil.getInstance().uploadAvatar(context, list, roomJid, new RoomImageUtil.UpImageListener() {
                        @Override
                        public void callBack(boolean isOk, String id) {
                            joinRoom(context, result.getData(), id);
                        }
                    });//上传头像
                    //TODO 先关闭


                }
            }
        }, MucRoom.class, params);
        String HASHCODE = Integer.toHexString(this.hashCode()) + "@";
        MyApplication.getInstance().getFastVolley().addDefaultRequest(HASHCODE, request);
    }

    /**
     * 加入群
     *
     * @param context
     * @param room    群对象
     */
    private void joinRoom(final Context context, final MucRoom room, final String id) {
        final String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        AppConfig mConfig = MyApplication.getInstance().getConfig();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", room.getId());
        if (room.getUserId() == loginUserId)
            params.put("type", "1");
        else
            params.put("type", "2");
        StringJsonArrayRequest<Void> request = new StringJsonArrayRequest<Void>(
                mConfig.ROOM_JOIN, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(context);
            }
        }, new StringJsonArrayRequest.Listener<Void>() {
            @Override
            public void onResponse(ArrayResult<Void> result) {
                boolean success = Result.defaultParser(context, result, true);
                if (success) {
                    Friend friend = new Friend();// 将房间也存为好友
                    friend.setOwnerId(loginUserId);
                    friend.setUserId(room.getJid());
                    if (StringUtil.isEmpty(id))
                        friend.setRoomCreateUserId(room.getUserId());
                    else
                        friend.setRoomCreateUserId(id);
                    friend.setNickName(room.getName());
                    friend.setDescription(room.getDesc());
                    friend.setRoomFlag(1);
                    friend.setRoomId(room.getId());
                    // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
                    friend.setTimeSend(CalendarUtil.getSecondMillion());
                    friend.setStatus(Friend.STATUS_FRIEND);
                    FriendDao.getInstance().createOrUpdateFriend(friend);//创建或者更新好友...
                    List<Friend> rooms = FriendDao.getInstance().getAllRooms(loginUserId);
//                    sendMucChatMessage();//发送一条信息到群里去
                    result(true, roomJid);
                } else {
                    result(false, null);
                }
            }
        }, Void.class, params);
        MyApplication.getInstance().getFastVolley().addDefaultRequest(TAG, request);
    }

    //建群后发送消息
    private void sendMucChatMessage() {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent("大家注意按时去开会");
        message.setFromUserName(MyApplication.getInstance().mLoginUser.getNickName());
        message.setFromUserId(MyApplication.getInstance().mLoginUser.getUserId());
        message.setTimeSend(CalendarUtil.getSecondMillion());
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        ChatMessageDao.getInstance().saveNewSingleChatMessage(MyApplication.getInstance().mLoginUser.getUserId(), roomJid, message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                UploadEngine.uploadImFile(roomJid, message, mUploadResponse);
            } else {
                send(message);
            }
        } else {
            send(message);
        }
    }

    private void result(boolean isOk, String jid) {
        if (listener != null)
            listener.result(isOk, jid);
    }

    private void send(ChatMessage message) {
        mXmppService.sendMucChatMessage(roomJid, message);
        //TODO 完成返回 应该清除内存中的变量

    }

    /**
     * 回收变量
     */
    private void recycle() {
        if (context != null) {
            context.unbindService(mServiceConnection);
            context = null;
        }
        if (entities != null)
            entities = null;
        if (roomName != null)
            roomName = null;
        if (doc != null)
            doc = null;
        if (roomJid != null)
            roomJid = null;
        if (mXmppService != null)
            mXmppService = null;
    }

    //创建群(不获取成功回调)
    public void createRoom(Context context, List<MeetUserEntity> entities, String name, String doc) {
        this.entities = entities;
        this.roomName = name;
        this.doc = doc;
        this.context = context;
        context.bindService(CoreService.getIntent(), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //创建群(要求获取成功回调)
    public void createRoom(Context context, List<MeetUserEntity> entities, String name, String doc, OnCreateRoomListener listener) {
        this.listener = listener;
        createRoom(context, entities, name, doc);
    }

    private OnCreateRoomListener listener = null;

    public interface OnCreateRoomListener {
        void result(boolean isOk, String jid);
    }
}
