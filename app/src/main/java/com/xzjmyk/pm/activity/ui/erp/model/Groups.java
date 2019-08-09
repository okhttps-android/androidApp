package com.xzjmyk.pm.activity.ui.erp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :LiuJie 2015年11月27日 上午10:52:39
 * @注释:本地联系人用户组
 */
public class Groups {
	private String name;
	private List<GroupsChilders> childers=new ArrayList<GroupsChilders>();

	public class GroupsChilders {
		private String or_id;
		private String or_name;

		public String getOr_id() {
			return or_id;
		}

		public void setOr_id(String or_id) {
			this.or_id = or_id;
		}

		public String getOr_name() {
			return or_name;
		}

		public void setOr_name(String or_name) {
			this.or_name = or_name;
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<GroupsChilders> getChilders() {
		return childers;
	}

	public void setChilders(List<GroupsChilders> childers) {
		this.childers = childers;
	}

}
