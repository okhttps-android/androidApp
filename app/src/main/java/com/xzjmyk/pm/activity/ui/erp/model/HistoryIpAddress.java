/**
 * 
 */
package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * @author LiuJie
 * 记录用户输入的历史记录
 */
public class HistoryIpAddress {
	
	private String ipString;

	public HistoryIpAddress() {
	}
	public HistoryIpAddress(String string) {
		ipString=string;
	}

	public String getIpString() {
		return ipString;
	}

	public void setIpString(String ipString) {
		this.ipString = ipString;
	}
	
	

}
