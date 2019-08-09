package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * @注释：配置标信息类
 * 
 */
public class MyDbInfo {
	private static String TableNames[] = {
		"users",
		"contents"
	};//表名

	private static String FieldNames[][] = {
			{"id","username","password","name","age","sex","birthday","phone","address","image","linksId"},
			{"id","contentNum","content","commitTime","clickNum","userID"}
	};//字段名
	
	private static String FieldTypes[][] = {
			{"INTEGER PRIMARY KEY AUTOINCREMENT","TEXT","TEXT","TEXT","INTEGER","TEXT","TEXT","TEXT","TEXT","TEXT","INTEGER"},
			{"INTEGER PRIMARY KEY AUTOINCREMENT","TEXT","TEXT","TEXT", "INTEGER","INTEGER"}
	};//字段类型
	
	public MyDbInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public static String[] getTableNames() {
		return TableNames;
	}
	
	public static String[][] getFieldNames() {
		return FieldNames;
	}
	
	public static String[][] getFieldTypes() {
		return FieldTypes;
	}

	public static void setTableNames(String[] tableNames) {
		TableNames = tableNames;
	}

	public static void setFieldNames(String[][] fieldNames) {
		FieldNames = fieldNames;
	}

	public static void setFieldTypes(String[][] fieldTypes) {
		FieldTypes = fieldTypes;
	}
	
	
}
