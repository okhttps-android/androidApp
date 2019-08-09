package com.core.xmpp;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.core.app.MyApplication;
import com.core.broadcast.MsgBroadcast;
import com.core.model.AttentionUser;
import com.core.model.CircleMessage;
import com.core.model.Friend;
import com.core.model.Hrorgs;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.FastVolley;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sp.TableVersionSp;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.CircleMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.utils.CardcastUiUpdateUtil;

import java.util.HashMap;
import java.util.List;

/**
 * @author Dean Tao
 */
public class FriendHelper {

    /**
     * @param loginUserId   当前登陆者的Id
     * @param friendId      朋友的Id
     * @param attentionUser 从服务器上获取的关系
     */
    public static boolean updateFriendRelationship(String loginUserId, String friendId, AttentionUser attentionUser) {// 更新两者的关系，因为本地数据可能不正确
        boolean changed = false;
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, friendId);// 本地好友
        if (attentionUser == null || (attentionUser.getStatus() != Friend.STATUS_ATTENTION && attentionUser.getStatus() != Friend.STATUS_FRIEND)) {// 服务器上不存在关系
            if (friend == null) {// 本地也不存在关系
                // do no thing
            } else {// 本地存在关系
                removeAttentionOrFriend(loginUserId, friendId);// 直接删除好友(也有可能是关注关系，但是删除好友的操作包括删除关注的所有操作，所以直接删除好友)|
                changed = true;
            }
        } else {// 服务器上存在关系
            if (friend == null) {// 本地不存在关系，那么就要插入一条好友记录
                friend = new Friend();
                friend.setTimeCreate(attentionUser.getCreateTime());
                friend.setTimeSend(CalendarUtil.getSecondMillion());
                friend.setOwnerId(attentionUser.getUserId());
                friend.setUserId(attentionUser.getToUserId());
                friend.setNickName(attentionUser.getToNickName());
                friend.setRemarkName(attentionUser.getRemarkName());
                friend.setRoomFlag(0);// 0朋友 1群组
                friend.setCompanyId(attentionUser.getCompanyId());// 公司
                int status = (attentionUser.getBlacklist() == 0) ? attentionUser.getStatus() : Friend.STATUS_BLACKLIST;
                friend.setStatus(status);
                friend.setVersion(TableVersionSp.getInstance(MyApplication.getInstance()).getFriendTableVersion(loginUserId));// 更新版本
                FriendDao.getInstance().createOrUpdateFriend(friend);

                if (status == Friend.STATUS_BLACKLIST) {// 如果在黑名单中（理论上不可能）
                    // do no thing
                } else if (status == Friend.STATUS_ATTENTION) {// 如果是关注（理论上不可能）
                    addAttentionExtraOperation(loginUserId, friendId);
                } else if (status == Friend.STATUS_FRIEND) {// 如果是好友
                    addFriendExtraOperation(loginUserId, friendId);
                }
                changed = true;

            } else {
                int status = attentionUser.getBlacklist() == 0 ? attentionUser.getStatus() : Friend.STATUS_BLACKLIST;
                if (status == friend.getStatus()) {
                    // do no thing
                } else {
                    FriendDao.getInstance().updateFriendStatus(loginUserId, friendId, status);
                    if (status == Friend.STATUS_BLACKLIST) {// 如果之前在黑名单中，现在是STATUS_ATTENTION或者STATUS_FRIEND
                        if (friend.getStatus() == Friend.STATUS_ATTENTION) {
                            addAttentionExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_FRIEND) {
                            addFriendExtraOperation(loginUserId, friendId);
                        }
                    } else if (status == Friend.STATUS_ATTENTION) {// 如果之前是关注，现在是黑名单或者好友
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {
                            addBlacklistExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_FRIEND) {
                            addFriendExtraOperation(loginUserId, friendId);
                        }
                    } else if (status == Friend.STATUS_FRIEND) {
                        if (friend.getStatus() == Friend.STATUS_BLACKLIST) {
                            addBlacklistExtraOperation(loginUserId, friendId);
                        } else if (friend.getStatus() == Friend.STATUS_ATTENTION) {// 本来是好友，现在变成关注
                            // 消息表中删除
                            ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
                            // 2、更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
                            MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                            // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
                            MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                        }
                    }
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * 加入黑名单，额外需要做的操作
     */
    public static void addBlacklistExtraOperation(String loginUserId, String friendId) {
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(loginUserId, friendId);
        // 2、更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
    }

    public static void addGoodFriend(Context context, Hrorgs.Employee employee) {
        if (employee != null) {
            Friend friend = new Friend();
            friend.setTimeCreate((int) (System.currentTimeMillis() / 1000));
            friend.setTimeSend((int) (System.currentTimeMillis() / 1000));
            friend.setOwnerId(MyApplication.getInstance().getLoginUserId());
            friend.setUserId(String.valueOf(employee.em_imid));
            friend.setNickName(employee.em_name);
            friend.setPhone(employee.em_mobile);
            friend.setEmCode(employee.em_code);
            friend.setRoomFlag(0);// 0朋友 1群组
            friend.setStatus(Friend.STATUS_FRIEND);
            BaseSortModel<Friend> mode = new BaseSortModel<>();
            mode.setBean(friend);
            addGoodFriend(context, friend);
        }
    }

    /**
     * 添加常用
     * 1.拨打电话
     * 2.发送聊天信息
     */
    public static void addGoodFriend(Context context, Friend friend) {
        if (friend != null) {
            String ownId = MyApplication.getInstance().getLoginUserId();
            LogUtil.i("ownId== " + ownId);
            LogUtil.i("friendid== " + friend.getUserId());
            Friend updateFriend = FriendDao.getInstance().getFriend(ownId, friend.getUserId());
            if (updateFriend == null) {
                friend.setTimeSend( (int) (System.currentTimeMillis() / 1000));
                FriendDao.getInstance().createFriend(friend);
            } else {
                LogUtil.i("updateFriend==elseelseelse");
                int lastMagSend = updateFriend.getTimeSend();
                int defTime = ((int) System.currentTimeMillis() / 1000) - lastMagSend;
                if (defTime < 86400) {
                    FriendDao.getInstance().updateClickNum(ownId, friend.getUserId(), 10);
                } else {
                    FriendDao.getInstance().updateSendTime(ownId, friend.getUserId(), (int) (System.currentTimeMillis() / 1000));
                }
            }
        }
    }


    /**
     * 在本地数据库表中出入一条关注记录，额外需要做的操作
     */
    public static void addAttentionExtraOperation(String loginUserId, String friendId) {
        // 下载商务圈消息
        FriendHelper.downloadCircleMessage(loginUserId, friendId);
    }

    /**
     * 在本地数据库表中出入一条好友记录，额外需要做的操作
     */
    public static void addFriendExtraOperation(String loginUserId, String friendId) {
        // 下载商务圈消息
        FriendHelper.downloadCircleMessage(loginUserId, friendId);
        // 插入一条系统提示消息
        FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
    }

    /**
     * 如果关注或加好友某个人，那么就去下载他的商务圈消息
     *
     * @param loginUserId
     * @param firendId
     */
    public static void downloadCircleMessage(final String loginUserId, final String firendId) {
        final Context context = MyApplication.getInstance();
        // 先清除他的商务圈消息，容错
        CircleMessageDao.getInstance().deleteMessage(loginUserId, firendId);
        // 下载他的商务圈消息
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("userId", firendId);

        StringJsonArrayRequest<CircleMessage> request = new StringJsonArrayRequest<CircleMessage>(
                MyApplication.getInstance().getConfig().USER_CIRCLE_MESSAGE, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
            }
        }, new StringJsonArrayRequest.Listener<CircleMessage>() {
            @Override
            public void onResponse(ArrayResult<CircleMessage> result) {
                boolean success = Result.defaultParser(context, result, false);
                if (success && result.getData() != null) {
                    List<CircleMessage> list = result.getData();
                    for (int i = 0; i < list.size(); i++) {
                        // 消息完整性填充
                        list.get(i).setUserId(firendId);
                    }
                    CircleMessageDao.getInstance().addFriendMessages(loginUserId, firendId, result.getData());
                }
            }
        }, CircleMessage.class, params);
        request.setRetryPolicy(FastVolley.newDefaultRetryPolicy());
        MyApplication.getInstance().getFastVolley().addDefaultRequest(null, request);
    }

    /**
     * 移除一个关注级别的用户，可能是关注或者好友
     *
     * @param ownerId
     */
    public static void removeAttentionOrFriend(String ownerId, String friendId) {
        // 从好友表中删除
        FriendDao.getInstance().deleteFriend(ownerId, friendId);
        // 新朋友表中删除
        // NewFriendDao.getInstance().deleteNewFriend(ownerId, friendId);
        // 消息表中删除
        ChatMessageDao.getInstance().deleteMessageTable(ownerId, friendId);
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(ownerId, friendId);
        // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
        MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
    }

    // ////////////////////上面是我的主动操作/////////////////
    // ////////////////////下面是我的被动操作/////////////////

    /**
     * 被加入黑名单
     */
    public static void beAddBlacklist(String loginUserId, String friendId) {
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, friendId);// 本地好友
        if (friend == null) {// 不存在该好友
            return;
        }
        // 商务圈消息表删除
        CircleMessageDao.getInstance().deleteMessage(loginUserId, friendId);
    }

    /**
     * 别人删除了关注我
     */
    public static void beDeleteSeeNewFriend(String loginUserId, String friendId) {
        Friend friend = FriendDao.getInstance().getFriend(loginUserId, friendId);
        if (friend != null) {// 如果我也有这个好友
            if (friend.getStatus() == Friend.STATUS_FRIEND) {// 如果之前是朋友，那么现在降级为关注
                friend.setStatus(Friend.STATUS_ATTENTION);
                friend.setContent("");
                // 由好友变为关注，更新一些数据
                FriendDao.getInstance().createOrUpdateFriend(friend);
                // 消息表中删除
                ChatMessageDao.getInstance().deleteMessageTable(loginUserId, friendId);
                // 更新消息界面（消息界面可能之前存在和该用户的聊天记录，要删除掉）
                MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
                // 3、更新主界面未读数量（消息界面可能之前存在和该用户的聊天记录，要删除掉，未读数量可能改变）
                MsgBroadcast.broadcastMsgNumReset(MyApplication.getInstance());
                // 可能正在看通讯录，那么通讯录也要更新
                CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
            }
        }
    }

    /**
     * 别人彻底删除了我
     */
    public static void beDeleteAllNewFriend(String loginUserId, String friendId) {
        removeAttentionOrFriend(loginUserId, friendId);
        // 可能正在看通讯录，那么通讯录也要更新
        CardcastUiUpdateUtil.broadcastUpdateUi(MyApplication.getInstance());
    }

    /**
     * 在本地数据库表中出入一条好友记录，额外需要做的操作
     */
    public static void beAddFriendExtraOperation(String loginUserId, String friendId) {
        // 插入一条系统提示消息
        FriendDao.getInstance().addNewFriendInMsgTable(loginUserId, friendId);
        // 更新Message Ui
        MsgBroadcast.broadcastMsgUiUpdate(MyApplication.getInstance());
        // 更新Main Ui message 未读数量
        MsgBroadcast.broadcastMsgNumUpdate(MyApplication.getInstance(), true, 1);
    }

}
