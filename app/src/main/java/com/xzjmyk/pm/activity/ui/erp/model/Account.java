package com.xzjmyk.pm.activity.ui.erp.model;

import java.io.Serializable;



public class Account implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;
	
	private String username;

	private String password;
	
	private String  userId;
    
	//private boolean isChecked;
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

//	public boolean isChecked() {
//		return isChecked;
//	}
//
//	public void setChecked(boolean isChecked) {
//		this.isChecked = isChecked;
//	}


	
	
}
