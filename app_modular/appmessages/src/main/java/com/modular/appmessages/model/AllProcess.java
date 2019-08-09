package com.modular.appmessages.model;

import java.io.Serializable;
import java.util.Date;
/**
 * @注释：待办事宜
 * @Administrator 2014年10月10日 下午3:22:54
 */
public class AllProcess implements Serializable{
	private static final long serialVersionUID = 9154546832037377352L;
	private int id;//ID
    private String ra_taskid;//取回复内容id
	private String taskid;//节点编号
	private String status;//当前状态
	private String mainname;//流程名称
	private String taskname;//节点名称
	private String codevalue;//单据编号
	private String type;//流程类型
	private String typecode;//流程类型码
	private String dealpersoncode;//处理人 -> em_code
	private String recorderid;//发起人ID
	private String recorder;//发起人名称
	private Date datetime;//发起时间
	private String defid;//流程版本号
	private String caller;//流程callr
	private String link;//对应URL地址
	private String master;//所属的帐套
	private String endTime; //结束时间
	private String describe="";//任务描述
	private String duration="";//持续时间
	private String ra_resourcecode;//执行人编号
	private String taskcode;//取语音
	private String attachs;//附件id


	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getAttachs() {
		return attachs;
	}
	public void setAttachs(String attachs) {
		this.attachs = attachs;
	}
	public String getRa_resourcecode() {
		return ra_resourcecode;
	}
	public void setRa_resourcecode(String ra_resourcecode) {
		this.ra_resourcecode = ra_resourcecode;
	}
	public String getTaskname() {
		return taskname;
	}
	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}
	public String getMaster() {
		return master;
	}
	public void setMaster(String master) {
		this.master = master;
	}
	public int getId() {
		return id;
	}
	public void setId(int jp_id) {
		this.id = jp_id;
	}
	public String getTaskid() {
		return taskid;
	}
	public void setTaskid(String jp_nodeid) {
		this.taskid = jp_nodeid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String jp_status) {
		this.status = jp_status;
	}
	public String getMainname() {
		return mainname;
	}
	public void setMainname(String jp_name) {
		this.mainname = jp_name;
	}
	public String getCodevalue() {
		return codevalue;
	}
	public void setCodevalue(String jp_nodename) {
		this.codevalue = jp_nodename;
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
	public String getRecorderid() {
		return recorderid;
	}
	public void setRecorderid(String jp_launcherid) {
		this.recorderid = jp_launcherid;
	}
	public String getRecorder() {
		return recorder;
	}
	public void setRecorder(String jp_launchername) {
		this.recorder = jp_launchername;
	}
	public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(Date jp_launchtime) {
		this.datetime = jp_launchtime;
	}
	public String getDefid() {
		return defid;
	}
	public void setDefid(String jp_processdefid) {
		this.defid = jp_processdefid;
	}
	public String getCaller() {
		return caller;
	}
	public void setCaller(String jp_caller) {
		this.caller = jp_caller;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String url) {
		this.link = url;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getTaskcode() {
		return taskcode;
	}
	public void setTaskcode(String taskcode) {
		this.taskcode = taskcode;
	}
	public String getRa_taskid() {
		return ra_taskid;
	}
	public void setRa_taskid(String ra_taskid) {
		this.ra_taskid = ra_taskid;
	}
	
	

}
