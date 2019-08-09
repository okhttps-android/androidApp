package com.uas.appme.settings.model;

import com.common.data.StringUtil;

/**
 * Created by Bitliker on 2017/10/13.
 */

public class BRest {

	private String _id;
	private String userId;
	private String username;
	private String companyid;
	private String companyname;
	private String date;
	private int type;//0为默认商家、1为人员

	public BRest(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username == null ? "" : username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCompanyid() {
		return companyid;
	}

	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

	public String getCompanyname() {
		return companyname;
	}

	public void setCompanyname(String companyname) {
		this.companyname = companyname;
	}

	public String getDate() {
		return date == null ? "" : date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void update(BRest model) {
		this.set_id(model.get_id());
		this.setCompanyid(model.getCompanyid());
		this.setUsername(model.getUsername());
		this.setUserId(model.getUserId());
		this.setCompanyname(model.getCompanyname());
		if (!StringUtil.isEmpty(model.getDate())) {
			this.setDate(model.getDate());
		}
	}
}
