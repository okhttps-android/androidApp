package com.xzjmyk.pm.activity.ui.erp.model;


/**
 * @author :LiuJie 2015年7月14日 下午1:47:33
 * @注释:请假单实体
 */
public class LeaveEntity {
	//va_code;//单据编号
	private int va_id;
	private String va_code;
	private String va_holidaytype;//请假类型
	private String va_vacationtype;//假期类型
	private String va_emcode;//请假人
	private String va_emname;
	private String va_mankind;//人员类型
	//va_department;//部门
	//va_position;//职位
	//va_mankind;
	private String va_statuscode;//状态码
	private String va_status;//状态
	private float va_alldays;//请假天数
	private String va_date;//录入时间
	private float va_alltimes;//请假时数
	private String va_recordor;//录入人
	private String va_startime;//开始时间
	private String va_remark;//请假原因
	private String va_endtime;//结束时间
	private String enuu;
	private String emcode;

	public String getEnuu() {
		return enuu;
	}

	public void setEnuu(String enuu) {
		this.enuu = enuu;
	}

	public String getEmcode() {
		return emcode;
	}

	public void setEmcode(String emcode) {
		this.emcode = emcode;
	}

	public String getVa_holidaytype() {
		return va_holidaytype;
	}
	public void setVa_holidaytype(String va_holidaytype) {
		this.va_holidaytype = va_holidaytype;
	}
	public String getVa_vacationtype() {
		return va_vacationtype;
	}
	public void setVa_vacationtype(String va_vacationtype) {
		this.va_vacationtype = va_vacationtype;
	}
	public String getVa_emcode() {
		return va_emcode;
	}
	public void setVa_emcode(String va_emcode) {
		this.va_emcode = va_emcode;
	}
	public float getVa_alldays() {
		return va_alldays;
	}
	public void setVa_alldays(Float va_alldays) {
		this.va_alldays = va_alldays;
	}
	public String getVa_date() {
		return va_date;
	}
	public void setVa_date(String va_date) {
		this.va_date = va_date;
	}
	public float getVa_alltimes() {
		return va_alltimes;
	}
	public void setVa_alltimes(Float va_alltimes) {
		this.va_alltimes = va_alltimes;
	}
	public String getVa_recordor() {
		return va_recordor;
	}
	public void setVa_recordor(String va_recordor) {
		this.va_recordor = va_recordor;
	}
	public String getVa_startime() {
		return va_startime;
	}
	public void setVa_startime(String va_startime) {
		this.va_startime = va_startime;
	}
	public String getVa_remark() {
		return va_remark;
	}
	public void setVa_remark(String va_remark) {
		this.va_remark = va_remark;
	}
	public String getVa_endtime() {
		return va_endtime;
	}
	public void setVa_endtime(String va_endtime) {
		this.va_endtime = va_endtime;
	}
	public int getVa_id() {
		return va_id;
	}
	public void setVa_id(int va_id) {
		this.va_id = va_id;
	}
	public String getVa_statuscode() {
		return va_statuscode;
	}
	public void setVa_statuscode(String va_statuscode) {
		this.va_statuscode = va_statuscode;
	}
	public String getVa_status() {
		return va_status;
	}
	public void setVa_status(String va_status) {
		this.va_status = va_status;
	}
	public String getVa_code() {
		return va_code;
	}
	public void setVa_code(String va_code) {
		this.va_code = va_code;
	}
	public String getVa_mankind() {
		return va_mankind;
	}
	public void setVa_mankind(String va_mankind) {
		this.va_mankind = va_mankind;
	}
	public String getVa_emname() {
		return va_emname;
	}
	public void setVa_emname(String va_emname) {
		this.va_emname = va_emname;
	}

	
}

