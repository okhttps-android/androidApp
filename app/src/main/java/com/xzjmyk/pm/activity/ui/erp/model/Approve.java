package com.xzjmyk.pm.activity.ui.erp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author :LiuJie 2015年6月25日 上午9:45:00
 * @注释:审批日志
 */
public class Approve implements  Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String JP_NODEID;

	private String LAUNCHERNAME;

	private Date LAUNCHTIME;
	private String DEALTIME;
	private String NODENAME;
	private String RESULT;
	private String REAMRK;
	private String DEALMAN;

	private String STATUS;

	public void setJP_NODEID(String JP_NODEID) {
		this.JP_NODEID = JP_NODEID;
	}

	public String getJP_NODEID() {
		return this.JP_NODEID;
	}

	public void setLAUNCHERNAME(String LAUNCHERNAME) {
		this.LAUNCHERNAME = LAUNCHERNAME;
	}

	public String getLAUNCHERNAME() {
		return this.LAUNCHERNAME;
	}

	public void setLAUNCHTIME(Date LAUNCHTIME) {
		this.LAUNCHTIME = LAUNCHTIME;
	}

	public Date getLAUNCHTIME() {
		return this.LAUNCHTIME;
	}

	public void setNODENAME(String NODENAME) {
		this.NODENAME = NODENAME;
	}

	public String getNODENAME() {
		return this.NODENAME;
	}

	public void setDEALMAN(String DEALMAN) {
		this.DEALMAN = DEALMAN;
	}

	public String getDEALMAN() {
		return this.DEALMAN;
	}

	public void setSTATUS(String STATUS) {
		this.STATUS = STATUS;
	}

	public String getSTATUS() {
		return this.STATUS;
	}

	public String getRESULT() {
		return RESULT;
	}

	public void setRESULT(String rESULT) {
		RESULT = rESULT;
	}

	public String getREAMRK() {
		return REAMRK;
	}

	public void setREAMRK(String rEAMRK) {
		REAMRK = rEAMRK;
	}

	public String getDEALTIME() {
		return DEALTIME;
	}

	public void setDEALTIME(String dEALTIME) {
		DEALTIME = dEALTIME;
	}

	
	
}