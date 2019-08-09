package com.xzjmyk.pm.activity.ui.erp.model;


import java.io.Serializable;
import java.util.Date;



public class Schedule implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

private int id;

  private int jp_nodeid;

  private int jp_launcherid;

  private int jp_processdefid;

  private String jp_status;

  private String jp_name;

  private String jp_nodename;

  private String jp_codevalue;

  private String type;

  private String typecode;
	
  private String dealpersoncode;

  private String jp_launchername;
	
  private String jp_caller;

  private Date jp_launchtime;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getJp_nodeid() {
		return jp_nodeid;
	}
	public void setJp_nodeid(int jp_nodeid) {
		this.jp_nodeid = jp_nodeid;
	}
	public int getJp_launcherid() {
		return jp_launcherid;
	}
	public void setJp_launcherid(int jp_launcherid) {
		this.jp_launcherid = jp_launcherid;
	}
	public int getJp_processdefid() {
		return jp_processdefid;
	}
	public void setJp_processdefid(int jp_processdefid) {
		this.jp_processdefid = jp_processdefid;
	}
	public String getJp_status() {
		return jp_status;
	}
	public void setJp_status(String jp_status) {
		this.jp_status = jp_status;
	}
	public String getJp_name() {
		return jp_name;
	}
	public void setJp_name(String jp_name) {
		this.jp_name = jp_name;
	}
	public String getJp_nodename() {
		return jp_nodename;
	}
	public void setJp_nodename(String jp_nodename) {
		this.jp_nodename = jp_nodename;
	}
	public String getJp_codevalue() {
		return jp_codevalue;
	}
	public void setJp_codevalue(String jp_codevalue) {
		this.jp_codevalue = jp_codevalue;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTypecode() {
		return typecode;
	}
	public void setTypecode(String typecode) {
		this.typecode = typecode;
	}
	public String getDealpersoncode() {
		return dealpersoncode;
	}
	public void setDealpersoncode(String dealpersoncode) {
		this.dealpersoncode = dealpersoncode;
	}
	public String getJp_launchername() {
		return jp_launchername;
	}
	public void setJp_launchername(String jp_launchername) {
		this.jp_launchername = jp_launchername;
	}
	public String getJp_caller() {
		return jp_caller;
	}
	public void setJp_caller(String jp_caller) {
		this.jp_caller = jp_caller;
	}
	public Date getJp_launchtime() {
		return jp_launchtime;
	}
	public void setJp_launchtime(Date jp_launchtime) {
		this.jp_launchtime = jp_launchtime;
	}
	  
	  
}
