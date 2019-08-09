package com.xzjmyk.pm.activity.ui.erp.model;

import java.util.Date;

public class Tasks {
	private String resourcecode;
	private String resourcename;
	private Date taskdate;
	private String recorder;
	private String description;
	private String name;
	private int id;

	public String getResourcecode() {
		return resourcecode;
	}

	public void setResourcecode(String resourcecode) {
		this.resourcecode = resourcecode;
	}

	public String getResourcename() {
		return resourcename;
	}

	public void setResourcename(String resourcename) {
		this.resourcename = resourcename;
	}

	public Date getTaskdate() {
		return taskdate;
	}

	public void setTaskdate(Date taskdate) {
		this.taskdate = taskdate;
	}

	public String getRecorder() {
		return recorder;
	}

	public void setRecorder(String recorder) {
		this.recorder = recorder;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
