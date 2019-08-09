package com.xzjmyk.pm.activity.ui.erp.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author :LiuJie 2015年6月17日 下午4:03:48
 * @注释:
 */
public class LogsEntity  implements  Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ml_id;
	private Date ml_date;
	private String ml_man;
	private String ml_content;
	private String ml_result;
	private String ml_search;
	private String sql;
	private List<keyColumns> keyColumns;
	private String table;

	public int getMl_id() {
		return ml_id;
	}

	public void setMl_id(int ml_id) {
		this.ml_id = ml_id;
	}

	public Date getMl_date() {
		return ml_date;
	}

	public void setMl_date(Date ml_date) {
		this.ml_date = ml_date;
	}

	public String getMl_man() {
		return ml_man;
	}

	public void setMl_man(String ml_man) {
		this.ml_man = ml_man;
	}

	public String getMl_content() {
		return ml_content;
	}

	public void setMl_content(String ml_content) {
		this.ml_content = ml_content;
	}

	public String getMl_result() {
		return ml_result;
	}

	public void setMl_result(String ml_result) {
		this.ml_result = ml_result;
	}

	public String getMl_search() {
		return ml_search;
	}

	public void setMl_search(String ml_search) {
		this.ml_search = ml_search;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<keyColumns> getKeyColumns() {
		return keyColumns;
	}

	public void setKeyColumns(List<keyColumns> keyColumns) {
		this.keyColumns = keyColumns;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	public class keyColumns {

	}

}
