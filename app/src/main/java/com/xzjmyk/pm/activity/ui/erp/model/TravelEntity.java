package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * @author :LiuJie 2015年7月27日 下午3:17:06
 * @注释:出差申请单
 */
public class TravelEntity {
	private int fp_id;
	private String fp_code;
	private String fp_type;//人员类型
	private String fp_kind;// 单据类型
	//private String fp_pleaseman;// 申请人
	private String fp_department;// 部门
	//private String fp_v1;// 出差地点
	private String fp_v3;// 出差线路----出差事由
	private String fp_v6;// 考勤
	private String fp_recorddate;// 录入日期
	private String fp_status;// 状态
	private String fp_statuscode;// 状态码
	private String fp_recordman;// 录入人
	
	private String  FP_PEOPLE2;//人员编号
	private Integer FP_N6;//预计天数
    private String fp_prestartdate;
    private String fp_preenddate;

	private String enuu;
	private String emcode;

	//	private  Integer fp_n2;//交通费
//	private Integer fp_n3;//住宿费
//	private Integer fp_n4;//公关费
//	private Integer fp_pleaseamount;//合计
//	


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
	
	public String getFP_PEOPLE2() {
		return FP_PEOPLE2;
	}

	public void setFP_PEOPLE2(String fP_PEOPLE2) {
		FP_PEOPLE2 = fP_PEOPLE2;
	}

	public Integer getFP_N6() {
		return FP_N6;
	}

	public void setFP_N6(Integer fP_N6) {
		FP_N6 = fP_N6;
	}

	public String getFp_prestartdate() {
		return fp_prestartdate;
	}

	public void setFp_prestartdate(String fp_prestartdate) {
		this.fp_prestartdate = fp_prestartdate;
	}

	public String getFp_preenddate() {
		return fp_preenddate;
	}

	public void setFp_preenddate(String fp_preenddate) {
		this.fp_preenddate = fp_preenddate;
	}

	
	

	

	public String getFp_department() {
		return fp_department;
	}

	public void setFp_department(String fp_department) {
		this.fp_department = fp_department;
	}

//	public Integer getFp_n2() {
//		return fp_n2;
//	}
//
//	public void setFp_n2(Integer fp_n2) {
//		this.fp_n2 = fp_n2;
//	}
//
//	public Integer getFp_n3() {
//		return fp_n3;
//	}
//
//	public void setFp_n3(Integer fp_n3) {
//		this.fp_n3 = fp_n3;
//	}
//
//	public Integer getFp_n4() {
//		return fp_n4;
//	}
//
//	public void setFp_n4(Integer fp_n4) {
//		this.fp_n4 = fp_n4;
//	}
//
//	public Integer getFp_pleaseamount() {
//		return fp_pleaseamount;
//	}
//
//	public void setFp_pleaseamount(Integer fp_pleaseamount) {
//		this.fp_pleaseamount = fp_pleaseamount;
//	}

	public String getFp_v3() {
		return fp_v3;
	}

	public void setFp_v3(String fp_v3) {
		this.fp_v3 = fp_v3;
	}

	public String getFp_v6() {
		return fp_v6;
	}

	public void setFp_v6(String fp_v6) {
		this.fp_v6 = fp_v6;
	}

	public String getFp_recorddate() {
		return fp_recorddate;
	}

	public void setFp_recorddate(String fp_recorddate) {
		this.fp_recorddate = fp_recorddate;
	}

	public String getFp_status() {
		return fp_status;
	}

	public void setFp_status(String fp_status) {
		this.fp_status = fp_status;
	}

	public String getFp_recordman() {
		return fp_recordman;
	}

	public void setFp_recordman(String fp_recordman) {
		this.fp_recordman = fp_recordman;
	}

	public int getFp_id() {
		return fp_id;
	}

	public void setFp_id(int fp_id) {
		this.fp_id = fp_id;
	}

	public String getFp_statuscode() {
		return fp_statuscode;
	}

	public void setFp_statuscode(String fp_statuscode) {
		this.fp_statuscode = fp_statuscode;
	}

	public String getFp_code() {
		return fp_code;
	}

	public void setFp_code(String fp_code) {
		this.fp_code = fp_code;
	}

	public String getFp_type() {
		return fp_type;
	}

	public void setFp_type(String fp_type) {
		this.fp_type = fp_type;
	}

	public String getFp_kind() {
		return fp_kind;
	}

	public void setFp_kind(String fp_kind) {
		this.fp_kind = fp_kind;
	}
	
	

	


	


	//主表的明细记录
	public class items{
		public int fpd_detno;
		public int fpd_fpid;
		public String fpd_date1;
		public String fpd_date2;
		
		public items() {
			// TODO Auto-generated constructor stub
		}
		public String getFpd_date1() {
			return fpd_date1;
		}
		public void setFpd_date1(String fpd_date1) {
			this.fpd_date1 = fpd_date1;
		}
		public String getFpd_date2() {
			return fpd_date2;
		}
		public void setFpd_date2(String fpd_date2) {
			this.fpd_date2 = fpd_date2;
		}
		public int getFpd_fpid() {
			return fpd_fpid;
		}
		public void setFpd_fpid(int fpd_fpid) {
			this.fpd_fpid = fpd_fpid;
		}
		public int getFpd_detno() {
			return fpd_detno;
		}
		public void setFpd_detno(int fpd_detno) {
			this.fpd_detno = fpd_detno;
		}
		
		
		
		
	}

}
