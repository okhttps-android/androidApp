package com.uas.appme.other.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 帐套
 * 
 * @author yingp
 * @date 2012-7-17 15:24:33
 */
public class Master implements  Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7479469727810677867L;
	private int ma_id;
	private String ma_user;//英文帐套
	private String ma_pwd;
	private String ma_name;
	private String ma_man;
	private Date ma_time;
	private String ma_language;
	private String ma_function;//中文帐套
	private Integer ma_type = 3;// 类型,0--集团中心,1--资料中心,2--子集团,3--营运中心
	private Integer ma_pid = 0;// 上级集团ID
	private String ma_soncode;
	private Integer ma_uu;// 帐套绑定UU号
	private List<Master> children;

	public int getMa_id() {
		return ma_id;
	}

	public void setMa_id(int ma_id) {
		this.ma_id = ma_id;
	}

	public String getMa_user() {
		return ma_user;
	}

	public void setMa_user(String ma_user) {
		this.ma_user = ma_user;
	}

	public String getMa_pwd() {
		return ma_pwd;
	}

	public void setMa_pwd(String ma_pwd) {
		this.ma_pwd = ma_pwd;
	}

	public String getMa_name() {
		return ma_name;
	}

	public void setMa_name(String ma_name) {
		this.ma_name = ma_name;
	}

	public String getMa_language() {
		return ma_language;
	}

	public void setMa_language(String ma_language) {
		this.ma_language = ma_language;
	}

	public String getMa_man() {
		return ma_man;
	}

	public void setMa_man(String ma_man) {
		this.ma_man = ma_man;
	}

	public Date getMa_time() {
		return ma_time;
	}

	public void setMa_time(Date ma_time) {
		this.ma_time = ma_time;
	}

	public String getMa_function() {
		return ma_function;
	}

	public void setMa_function(String ma_function) {
		this.ma_function = ma_function;
	}

	public Integer getMa_type() {
		return ma_type;
	}

	public void setMa_type(Integer ma_type) {
		this.ma_type = ma_type;
	}

	public Integer getMa_pid() {
		return ma_pid;
	}

	public void setMa_pid(Integer ma_pid) {
		this.ma_pid = ma_pid;
	}

	public String getMa_soncode() {
		return ma_soncode;
	}

	public void setMa_soncode(String ma_soncode) {
		this.ma_soncode = ma_soncode;
	}

	public Integer getMa_uu() {
		return ma_uu;
	}

	public void setMa_uu(Integer ma_uu) {
		this.ma_uu = ma_uu;
	}

	public List<Master> getChildren() {
		return children;
	}

	public void setChildren(List<Master> children) {
		this.children = children;
	}
}
