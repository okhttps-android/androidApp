package com.xzjmyk.pm.activity.ui.erp.model;
/**
 * @author :LiuJie 2015年6月10日 下午2:43:07
 * @注释:订单详细列表实体类
 */
public class DetailColumns {
	
	private String caption;

	private String dataIndex;

	private int width;

	private String type;
	
	private String render;

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return this.caption;
	}

	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}

	public String getDataIndex() {
		return this.dataIndex;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return this.width;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public String getRender() {
		return render;
	}

	public void setRender(String render) {
		this.render = render;
	}
	
	

}