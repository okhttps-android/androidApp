package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * 图像实体类
 */
public class ImageInfo {

	public String imageMsg;		//菜单标题
	public String caller;
	public int imageId;			//logo图片资源
	public int bgId;
	public int badgeCount;//背景图片资源
    public  ImageInfo(){
    	
    }
	public ImageInfo(String msg, int imid,int bgid,int badgeid,String caller) {
		imageId =imid;
		imageMsg = msg;
		bgId =bgid;
		badgeCount=badgeid;
		this.caller=caller;
	}
//	public ImageInfo(String msg, int imid,int bgid,int badgeid,String caller,String title) {
//		imageId =imid;
//		imageMsg = msg;
//		bgId =bgid;
//		badgeCount=badgeid;
//		this.caller=caller;
//	}

	public String getImageMsg() {
		return imageMsg;
	}

	public String getCaller() {
		return caller;
	}

	public int getImageId() {
		return imageId;
	}

	public int getBgId() {
		return bgId;
	}

	public int getBadgeCount() {
		return badgeCount;
	}

	public void setImageMsg(String imageMsg) {
		this.imageMsg = imageMsg;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public void setBgId(int bgId) {
		this.bgId = bgId;
	}

	public void setBadgeCount(int badgeCount) {
		this.badgeCount = badgeCount;
	}
	
	
	
	
}
