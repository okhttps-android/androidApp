package com.core.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.CalendarUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.UUID;

/**
 * #define CALL_CENTER_USERID @"10000" //系统消息<br/>
 * #define FRIEND_CENTER_USERID @"10001" //新朋友 <br/>
 * #define BLOG_CENTER_USERID @"10003" //商务圈 <br/>
 * #define TEST_CENTER_USERID @"10004" //面试中心<br/>
 * 朋友中心
 * 
 */
@DatabaseTable
public class NewFriendMessage extends XmppMessage implements Cloneable, Serializable {

	private static final long serialVersionUID = -4231369003725583507L;

	public NewFriendMessage() {
	}

	public NewFriendMessage(String jsonData) {
		parserJsonData(jsonData);
	}

	@DatabaseField(generatedId = true)
	private int _id;

	@DatabaseField(canBeNull = false)
	private String ownerId; // 这个消息是属于哪个用户的

	@DatabaseField(canBeNull = false)
	private String userId; // 此新朋友消息针对的是哪个用户（一定是别人，不是自己）

	@DatabaseField
	private String nickName;// 此新朋友消息针对的是哪个用户（一定是别人，不是自己）

	@DatabaseField
	private String content;// (打招呼的内容)

	@DatabaseField
	private boolean isRead;

	@DatabaseField
	private int companyId;// 此新朋友消息针对的是哪个用户,他的公司Id（一定是别人，不是自己）

	/* 下面3个只用于xmpp通讯时，生成json消息。在接受时，会自动转为上面的有效消息，所以不应该作为其他用途，不作为判断依据，不写入数据库 */
	private String fromUserId;
	private String fromUserName;
	private int fromCompanyId;

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public int getFromCompanyId() {
		return fromCompanyId;
	}

	public void setFromCompanyId(int fromCompanyId) {
		this.fromCompanyId = fromCompanyId;
	}

	@Override
	public NewFriendMessage clone() {
		NewFriendMessage n = null;
		try {
			n = (NewFriendMessage) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return n;
	}

	private void parserJsonData(String jsonData) {
		try {
			JSONObject jObject = JSON.parseObject(jsonData);
			userId = getStringValueFromJSONObject(jObject, "fromUserId");
			nickName = getStringValueFromJSONObject(jObject, "fromUserName");
			companyId = getIntValueFromJSONObject(jObject, "fromCompanyId");
			type = getIntValueFromJSONObject(jObject, "type");
			timeSend = getIntValueFromJSONObject(jObject, "timeSend");
			content = getStringValueFromJSONObject(jObject, "content");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * fromUserId, fromUserName, fromCompanyId, timeSend, content, type,
	 * 
	 * @return
	 */
	public String toJsonString() {
		String msg = "";
		JSONObject object = new JSONObject();
		object.put("fromUserId", this.fromUserId);
		object.put("fromUserName", this.fromUserName);
		object.put("fromCompanyId", this.fromCompanyId);
		object.put("type", this.type);
		object.put("timeSend", this.timeSend);
		if (!TextUtils.isEmpty(this.content)) {
			object.put("content", this.content);
		}
		msg = object.toString();
		return msg;
	}

	/**
	 * 
	 * @param fromUser
	 *            应该是当前登陆的User
	 * @param type
	 * @param content
	 * @param toUserId
	 * @param toNickName
	 * @param toCompanyId
	 * @param status
	 *            ，此状态主要用于更新朋友关系。 发送加关注、加好友 此状态有效<br/>
	 *            发送打招呼 、解除关注、解除好友此状态无效，填Integer.MIN_VALUE<br/>
	 *            下面几个重载方法都遵循此原则<br/>
	 * @return
	 */
	public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, String toUserId, String toNickName, int toCompanyId) {
		String packetId = UUID.randomUUID().toString().replace("-", "");
		NewFriendMessage message = new NewFriendMessage();
		message.setPacketId(packetId);
		// 首先是传输协议的字段，
		message.setFromUserId(fromUser.getUserId());
		message.setFromUserName(fromUser.getNickName());
		message.setFromCompanyId(fromUser.getCompanyId());
		message.setTimeSend(CalendarUtil.getSecondMillion());
		message.setType(type);
		message.setContent(content);

		// 本地数据库状态
		message.setOwnerId(fromUser.getUserId());
		message.setUserId(toUserId);
		message.setNickName(toNickName);
		message.setCompanyId(toCompanyId);
		message.setRead(true);
		message.setMySend(true);

		return message;
	}

	public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, User toUser) {
		return createWillSendMessage(fromUser, type, content, toUser.getUserId(), toUser.getNickName(), toUser.getCompanyId());
	}

	public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, Friend toFriend) {
		return createWillSendMessage(fromUser, type, content, toFriend.getUserId(), toFriend.getNickName(), toFriend.getCompanyId());
	}

	public static NewFriendMessage createWillSendMessage(User fromUser, int type, String content, NewFriendMessage existMessage) {
		return createWillSendMessage(fromUser, type, content, existMessage.getUserId(), existMessage.getNickName(), existMessage.getCompanyId());
	}
}
