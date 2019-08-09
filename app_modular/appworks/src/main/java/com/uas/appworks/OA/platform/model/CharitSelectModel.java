package com.uas.appworks.OA.platform.model;

/**
 * Created by Bitlike on 2017/11/7.
 */

public class CharitSelectModel<T> {
	private int type;
	private String title;
	private T data;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title == null ? "" : title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
