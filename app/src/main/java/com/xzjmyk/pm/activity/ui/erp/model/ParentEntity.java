package com.xzjmyk.pm.activity.ui.erp.model;

import java.util.ArrayList;

/**
 * 
 * @author Apathy����
 * 
 *         ��������ʵ��
 * 
 * */

public class ParentEntity {

	private int groupColor;

	private String groupName;
	private String seletedTV;
	//分组隐藏
	private boolean isHide;

	private ArrayList<ChildEntity> childs=new ArrayList<ChildEntity>();

	
	/* ==========================================================
	 * ======================= get method =======================
	 * ========================================================== */
	
	public int getGroupColor() {
		return groupColor;
	}

	public String getGroupName() {
		return groupName;
	}

	public ArrayList<ChildEntity> getChilds() {
		return childs;
	}
	
	/* ==========================================================
	 * ======================= set method =======================
	 * ========================================================== */

	public void setGroupColor(int groupColor) {
		this.groupColor = groupColor;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setChilds(ArrayList<ChildEntity> childs) {
		this.childs = childs;
	}

	public String getSeletedTV() {
		return seletedTV;
	}

	public void setSeletedTV(String seletedTV) {
		this.seletedTV = seletedTV;
	}

	public boolean isHide() {
		return isHide;
	}

	public void setHide(boolean isHide) {
		this.isHide = isHide;
	}

	 
}
