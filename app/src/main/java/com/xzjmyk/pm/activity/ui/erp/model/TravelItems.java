package com.xzjmyk.pm.activity.ui.erp.model;

public class TravelItems {
	//private int RN;
	public int fpd_detno;
	public int fpd_fpid;
	public int fpd_id;

	public String fpd_date1;
	public String fpd_date2;
	
	public String FPD_D6;//目的地
	public String FPD_D4;//接谈对象
	public String fpd_d2;//具体工作项目及目标

	public String Fpd_location;

	public String getFpd_location() {
		return Fpd_location;
	}

	public void setFpd_location(String fpd_location) {
		Fpd_location = fpd_location;
	}
	/*public int getRN() {
		return RN;
	}

	public void setRN(int rN) {
		RN = rN;
	}*/

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

	public String getFPD_D6() {
		return FPD_D6;
	}

	public void setFPD_D6(String fPD_D6) {
		FPD_D6 = fPD_D6;
	}

	public String getFPD_D4() {
		return FPD_D4;
	}

	public void setFPD_D4(String fPD_D4) {
		FPD_D4 = fPD_D4;
	}

	public String getFpd_d2() {
		return fpd_d2;
	}

	public void setFpd_d2(String fpd_d2) {
		this.fpd_d2 = fpd_d2;
	}

	public int getFpd_id() {
		return fpd_id;
	}

	public void setFpd_id(int fpd_id) {
		this.fpd_id = fpd_id;
	}
}
