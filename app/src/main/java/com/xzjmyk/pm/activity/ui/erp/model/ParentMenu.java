package com.xzjmyk.pm.activity.ui.erp.model;

import java.util.List;

public class ParentMenu {
	
    private String title;
    private List<ChildMenu> childMenus;
    
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<ChildMenu> getChildMenus() {
		return childMenus;
	}
	public void setChildMenus(List<ChildMenu> childMenus) {
		this.childMenus = childMenus;
	}
    
    
}
