package com.xzjmyk.pm.activity.ui.erp.model;


//人员轨迹实体类
public class Employeetrack {
//	public Integer id;
	public String et_emcode; // 人员编号
	public String et_longitude; // 经度
	public String et_latitude; // 纬度
	public String et_date; // 时间
	public String et_position; // 位置
//	public Integer et_isUploadv;// 是否上传到服务器
     
	public Employeetrack(String et_emcode, String et_longitude,
			String et_latitude, String et_date, String et_position
			) {
		super();
//		this.id = id;
		this.et_emcode = et_emcode;
		this.et_longitude = et_longitude;
		this.et_latitude = et_latitude;
		this.et_date = et_date;
		this.et_position = et_position;
//		this.et_isUpload = et_isUpload;
	}

//	public Integer getId() {
//		return id;
//	}
//
//	public void setId(Integer id) {
//		this.id = id;
//	}

	public String getEt_emcode() {
		return et_emcode;
	}

	public void setEt_emcode(String et_emcode) {
		this.et_emcode = et_emcode;
	}

	public String getEt_longitude() {
		return et_longitude;
	}

	public void setEt_longitude(String et_longitude) {
		this.et_longitude = et_longitude;
	}

	public String getEt_latitude() {
		return et_latitude;
	}

	public void setEt_latitude(String et_latitude) {
		this.et_latitude = et_latitude;
	}

	public String getEt_date() {
		return et_date;
	}

	public void setEt_date(String et_date) {
		this.et_date = et_date;
	}

	public String getEt_position() {
		return et_position;
	}

	public void setEt_position(String et_position) {
		this.et_position = et_position;
	}

//	public Integer getEt_isUpload() {
//		return et_isUpload;
//	}
//
//	public void setEt_isUpload(Integer et_isUpload) {
//		this.et_isUpload = et_isUpload;
//	}
//	
	

}
