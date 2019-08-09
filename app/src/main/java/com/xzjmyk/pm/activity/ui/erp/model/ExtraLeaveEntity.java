package com.xzjmyk.pm.activity.ui.erp.model;

public class ExtraLeaveEntity {
	
	private int sa_id;//ID
	private String sa_code;
	private String sa_appmancode;//员工编号
	private String sa_appman;//员工名称
	
	private String sa_status;
	private String sa_statuscode;
	private String sa_department;//部门
	private String sa_mankind;//人员类型
	private String sa_reason;//事由类型
	private String sa_reasonremark;//事由说明
	
	private String sa_appdate;//开始时间
	private String sa_enddate;//结束时间
	
	private String sa_recorder;//录入人
	private String sa_recorddate;//录入时间
	
	
	public int getSa_id() {
		return sa_id;
	}
	public void setSa_id(int sa_id) {
		this.sa_id = sa_id;
	}
	public String getSa_appmancode() {
		return sa_appmancode;
	}
	public void setSa_appmancode(String sa_appmancode) {
		this.sa_appmancode = sa_appmancode;
	}
	public String getSa_appman() {
		return sa_appman;
	}
	public void setSa_appman(String sa_appman) {
		this.sa_appman = sa_appman;
	}
	public String getSa_department() {
		return sa_department;
	}
	public void setSa_department(String sa_department) {
		this.sa_department = sa_department;
	}
	public String getSa_mankind() {
		return sa_mankind;
	}
	public void setSa_mankind(String sa_mankind) {
		this.sa_mankind = sa_mankind;
	}
	public String getSa_reason() {
		return sa_reason;
	}
	public void setSa_reason(String sa_reason) {
		this.sa_reason = sa_reason;
	}
	public String getSa_reasonremark() {
		return sa_reasonremark;
	}
	public void setSa_reasonremark(String sa_reasonremark) {
		this.sa_reasonremark = sa_reasonremark;
	}
	public String getSa_appdate() {
		return sa_appdate;
	}
	public void setSa_appdate(String sa_appdate) {
		this.sa_appdate = sa_appdate;
	}
	public String getSa_enddate() {
		return sa_enddate;
	}
	public void setSa_enddate(String sa_enddate) {
		this.sa_enddate = sa_enddate;
	}
	public String getSa_recorder() {
		return sa_recorder;
	}
	public void setSa_recorder(String sa_recorder) {
		this.sa_recorder = sa_recorder;
	}
	public String getSa_recorddate() {
		return sa_recorddate;
	}
	public void setSa_recorddate(String sa_recorddate) {
		this.sa_recorddate = sa_recorddate;
	}
	public String getSa_status() {
		return sa_status;
	}
	public void setSa_status(String sa_status) {
		this.sa_status = sa_status;
	}
	public String getSa_statuscode() {
		return sa_statuscode;
	}
	public void setSa_statuscode(String sa_statuscode) {
		this.sa_statuscode = sa_statuscode;
	}
	public String getSa_code() {
		return sa_code;
	}
	public void setSa_code(String sa_code) {
		this.sa_code = sa_code;
	}
	
	
	
}
