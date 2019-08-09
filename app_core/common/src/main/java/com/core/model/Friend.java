package com.core.model;

import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.core.app.MyApplication;
import com.core.xmpp.dao.FriendDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class Friend implements Serializable {
    private static final long serialVersionUID = -6859528031175998594L;
    public static final String ID_SYSTEM_MESSAGE = "10000";// 系统消息ID
    public static final String ID_NEW_FRIEND_MESSAGE = "10001";// 新朋友消息 ID
    public static final String ID_BLOG_MESSAGE = "10002";// 商务圈消息ID
    public static final String ID_INTERVIEW_MESSAGE = "10004";// 面试中心ID（用于职位、初试、面试的推送）
    public static final String ID_MUC_ROOM = "10005";// 群聊管理ID（群聊房间的推送）

    public static final String ID_ERP_PROCESS = "10006";// 待办事宜
    public static final String ID_ERP_TASK = "10007";// 我的任务
    public static final String ID_ERP_ZHIHUI = "10008";// 我的知会

    public static final String ID_ERP_NEWS = "10009";// 我的新闻
    public static final String ID_ERP_NOTICE = "100010";// 我的通知
    public static final String ID_ERP_GONGGAO = "100011";// 我的公告

    public static final String NICKNAME_SYSTEM_MESSAGE = "系统消息";// 系统消息ID
    public static final String NICKNAME_NEW_FRIEND_MESSAGE = "新朋友消息";// 新朋友消息
    public static final String NICKNAME_BLOG_MESSAGE = "商务圈消息";// 商务圈消息ID
    public static final String NICKNAME_INTERVIEW_MESSAGE = "面试中心";// 面试中心ID

    public static final String NICKNAME_ERP_PROCESS = "待审批流程";// 待办事宜
    public static final String NICKNAME_ERP_TASK = "我的任务";// 我的任务
    public static final String NICKNAME_ERP_ZHIHUI = "通知公告";// 我的知会
    public static final String NICKNAME_ERP_NEWS = "新闻";// 新闻
    public static final String NICKNAME_ERP_NOTICE = "通知";// 通知
    public static final String NICKNAME_ERP_GONGGAO = "公告";// 公告

    // -1:黑名单；0：陌生人；1:单方关注；2:互为好友；8:显示系统号；9:非显示系统号
    public static final int STATUS_NO_SHOW_SYSTEM = 9;// 非显示系统号
    public static final int STATUS_SYSTEM = 8;// 显示系统号

    public static final int STATUS_FRIEND = 2;// 好友
    public static final int STATUS_ATTENTION = 1;// 关注
    public static final int STATUS_UNKNOW = 0;// 陌生人(不可能出现在好友表，只可能在新朋友消息表)

    public static final int STATUS_BLACKLIST = -1;// 黑名单
    public static final int STATUS_SELF = 9999;// 本人，特殊状态，在数据库中没有，在UI层判断是不是当前登陆者本人，显示控制不同表现

    @DatabaseField(generatedId = true)
    private int _id;

    private boolean isFriend = false;
    @DatabaseField(canBeNull = false)
    private String ownerId; // 属于哪个用户的id

    @DatabaseField(canBeNull = false)
    private String userId; // 用户id或者聊天室id

    @DatabaseField(canBeNull = false)
    @JSONField(name = "nickname")
    private String nickName;// 用户昵称或者聊天室名称

    @DatabaseField
    private String description;// 签名

    @DatabaseField
    private int timeCreate;// 创建好友关系的时间

    @DatabaseField(defaultValue = "0")
    private int unReadNum; // 未读消息数量

    @DatabaseField
    private String content;// 最后一条消息内容

    @DatabaseField
    private int type;// 最后一条消息类型

    @DatabaseField
    private int timeSend;// 最后一条消息发送时间

    @DatabaseField(defaultValue = "0")
    private int roomFlag;// 0朋友 1群组

    @DatabaseField(defaultValue = "0")
    private int companyId; // 0表示不是公司

    @DatabaseField
    private int status;// -1:黑名单；0：陌生人；1:单方关注；2:互为好友；8:系统号；9:非显示系统号

    @DatabaseField
    private String privacy;// 隐私(现用于记录邮箱)

    @DatabaseField
    private String remarkName;// 备注

    @DatabaseField
    private int clickNum;// 联系次数（当联系次数大于10时候，放到常用联系人中）

    @DatabaseField
    private int version;// 本地表的版本

    @DatabaseField
    private String roomId;// 仅仅当roomFlag==1，为群组的时候才有效

    @DatabaseField
    private String roomCreateUserId;// 仅仅当roomFlag==1，为群组的时候才有效

    @DatabaseField
    private String roomMyNickName;// 我在这个房间的昵称

    @DatabaseField
    private int roomTalkTime;// wi在这个房间的禁言时间

    @DatabaseField
    private String emCode;

    @DatabaseField
    private String phone;//手机号码

    @DatabaseField
    private String depart;//部门

    @DatabaseField
    private String position;//职位
    private int sex = 1;//性别  1.女  2.男


    public int getClickNum() {
        return clickNum;
    }

    public void setClickNum(int clickNum) {
        this.clickNum = clickNum;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getEmCode() {
        return emCode;
    }

    public void setEmCode(String emCode) {
        this.emCode = emCode;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomCreateUserId() {
        return roomCreateUserId;
    }

    public void setRoomCreateUserId(String roomCreateUserId) {
        this.roomCreateUserId = roomCreateUserId;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(int timeCreate) {
        this.timeCreate = timeCreate;
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(int timeSend) {
        this.timeSend = timeSend;
    }

    public int getRoomFlag() {
        return roomFlag;
    }

    public void setRoomFlag(int roomFlag) {
        this.roomFlag = roomFlag;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getRoomMyNickName() {
        return roomMyNickName;
    }

    public void setRoomMyNickName(String roomMyNickName) {
        this.roomMyNickName = roomMyNickName;
    }

    public int getRoomTalkTime() {
        return roomTalkTime;
    }

    public void setRoomTalkTime(int roomTalkTime) {
        this.roomTalkTime = roomTalkTime;
    }

    /* 快捷方法，获取在好友列表中显示的名称 */
    public String getShowName() {
        if (!TextUtils.isEmpty(remarkName)) {
            return remarkName.trim();
        } else if (!TextUtils.isEmpty(nickName)) {
            return nickName.trim();
        } else {
            return "";
        }
    }

    /**
     * 快捷方法
     *
     * @param userId
     * @param nickName
     * @return
     */
    public static String getShowName(String userId, String nickName) {
        User loginUser = MyApplication.getInstance().mLoginUser;
        if (loginUser == null || TextUtils.isEmpty(loginUser.getUserId())) {
            return nickName;
        }
        String showName = FriendDao.getInstance().getRemarkName(loginUser.getUserId(), userId);
        if (TextUtils.isEmpty(showName)) {
            showName = nickName;
        }
        return showName;
    }

}
