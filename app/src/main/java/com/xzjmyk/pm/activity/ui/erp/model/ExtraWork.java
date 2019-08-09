package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * @author :LiuJie 2015年7月27日 下午2:38:41
 * @注释:加班申请
 */
public class ExtraWork {
	
	private int wo_id;
	private String wo_code;
	private String wo_mankind;//人员类型
	private String wo_cop;//所属公司
	private String wo_emname;//申请人
	private String wo_emcode;
	private String wo_remark;//加班原因
	private String wo_worktask; 
	private String wo_statuscode;
	//public int wo_hour;//当天加班时数
	private String wo_auditstatus;//确认状态
	private String wo_status;//状态
	private String wo_recorder;//录入人
	private String wo_recorddate;//录入时间

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

	private String enuu;
	private String emcode;

	public String getWo_emname() {
		return wo_emname;
	}
	public void setWo_emname(String wo_emname) {
		this.wo_emname = wo_emname;
	}
	public int getWo_id() {
		return wo_id;
	}
	public void setWo_id(int wo_id) {
		this.wo_id = wo_id;
	}

	public String getWo_emcode() {
		return wo_emcode;
	}

	public void setWo_emcode(String wo_emcode) {
		this.wo_emcode = wo_emcode;
	}

	public String getWo_auditstatus() {
		return wo_auditstatus;
	}
	public void setWo_auditstatus(String wo_auditstatus) {
		this.wo_auditstatus = wo_auditstatus;
	}
	public String getWo_code() {
		return wo_code;
	}
	public void setWo_code(String wo_code) {
		this.wo_code = wo_code;
	}
	public String getWo_statuscode() {
		return wo_statuscode;
	}
	public void setWo_statuscode(String wo_statuscode) {
		this.wo_statuscode = wo_statuscode;
	}
	
	
	public String getWo_mankind() {
		return wo_mankind;
	}
	public void setWo_mankind(String wo_mankind) {
		this.wo_mankind = wo_mankind;
	}
	public String getWo_cop() {
		return wo_cop;
	}
	public void setWo_cop(String wo_cop) {
		this.wo_cop = wo_cop;
	}
	public String getWo_remark() {
		return wo_remark;
	}
	public void setWo_remark(String wo_remark) {
		this.wo_remark = wo_remark;
	}
	public String getWo_status() {
		return wo_status;
	}
	public void setWo_status(String wo_status) {
		this.wo_status = wo_status;
	}
	public String getWo_recorder() {
		return wo_recorder;
	}
	public void setWo_recorder(String wo_recorder) {
		this.wo_recorder = wo_recorder;
	}
	public String getWo_recorddate() {
		return wo_recorddate;
	}
	public void setWo_recorddate(String wo_recorddate) {
		this.wo_recorddate = wo_recorddate;
	}
	
	public String getWo_worktask() {
		return wo_worktask;
	}
	public void setWo_worktask(String wo_worktask) {
		this.wo_worktask = wo_worktask;
	}

	public class items{
		public int wod_woid;//关联ID
		public int wod_detno;//序号
		public String wod_empname;//申请人
		public String wod_type;//加班类型
		public String wod_isallday;//是否全天
		public String wod_count;//当天加班时数
		public String wod_startdate;//起始日期
		public String wod_jias1;//起始时间
		public String wod_enddate;//截止日期
		public String wod_jiax1;//截止时间
		public items() {
			// TODO Auto-generated constructor stub
		}
		public int getWod_woid() {
			return wod_woid;
		}
		public void setWod_woid(int wod_woid) {
			this.wod_woid = wod_woid;
		}
		public int getWod_detno() {
			return wod_detno;
		}
		public void setWod_detno(int wod_detno) {
			this.wod_detno = wod_detno;
		}
		public String getWod_empname() {
			return wod_empname;
		}
		public void setWod_empname(String wod_empname) {
			this.wod_empname = wod_empname;
		}
		public String getWod_type() {
			return wod_type;
		}
		public void setWod_type(String wod_type) {
			this.wod_type = wod_type;
		}
		public String getWod_isallday() {
			return wod_isallday;
		}
		public void setWod_isallday(String wod_isallday) {
			this.wod_isallday = wod_isallday;
		}
		public String getWod_count() {
			return wod_count;
		}
		public void setWod_count(String wod_count) {
			this.wod_count = wod_count;
		}
		public String getWod_startdate() {
			return wod_startdate;
		}
		public void setWod_startdate(String wod_startdate) {
			this.wod_startdate = wod_startdate;
		}
		public String getWod_jias1() {
			return wod_jias1;
		}
		public void setWod_jias1(String wod_jias1) {
			this.wod_jias1 = wod_jias1;
		}
		public String getWod_enddate() {
			return wod_enddate;
		}
		public void setWod_enddate(String wod_enddate) {
			this.wod_enddate = wod_enddate;
		}
		public String getWod_jiax1() {
			return wod_jiax1;
		}
		public void setWod_jiax1(String wod_jiax1) {
			this.wod_jiax1 = wod_jiax1;
		}
		
	}

	
	/*public int getWo_hour() {
		return wo_hour;
	}
	public void setWo_hour(int wo_hour) {
		this.wo_hour = wo_hour;
	}*/
	
}
