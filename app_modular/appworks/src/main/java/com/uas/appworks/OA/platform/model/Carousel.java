package com.uas.appworks.OA.platform.model;


/**
 * Created by Bitlike on 2017/11/7.
 */

public class Carousel {
	private int id;
	private String text;
	private String imageUrl;

	public Carousel(int id, String text, String imageUrl) {
		this.id = id;
		this.text = text;
		this.imageUrl = imageUrl;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
