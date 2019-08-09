package com.uas.appworks.OA.platform.model;

/**
 * Created by Bitlike on 2017/11/13.
 */

public class JoinModel {
	private int  id;
	private int type;
	private float amount;
	private String name;
	private String time;
	private String status;
	private String sub;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name==null?"":name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time==null?"":time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status==null?"":status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSub() {
		return sub==null?"":sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}
}
