package com.core.xmpp.model;

public class MucRoomSimple {

	private String jid;
	private String name;
	private String desc;
	private String id;
	private String userId;
	private int timeSend;

	public int getTimeSend() {
		return timeSend;
	}

	public void setTimeSend(int timeSend) {
		this.timeSend = timeSend;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
