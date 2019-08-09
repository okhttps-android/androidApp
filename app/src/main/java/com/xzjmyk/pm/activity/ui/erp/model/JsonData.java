package com.xzjmyk.pm.activity.ui.erp.model;
/**
 * @author :LiuJie 2015年9月7日 下午1:54:44
 * @注释:解析Json数据实体类
 */
public class JsonData {
	/**
	 * @author LiuJie
	 * @功能:dfind 公司
	 */
   public class Commpany{
    	public String co_code;
    	public String  co_name;
    	public String  co_shortname;
		public String getCo_code() {
			return co_code;
		}
		public void setCo_code(String co_code) {
			this.co_code = co_code;
		}
		public String getCo_name() {
			return co_name;
		}
		public void setCo_name(String co_name) {
			this.co_name = co_name;
		}
		public String getCo_shortname() {
			return co_shortname;
		}
		public void setCo_shortname(String co_shortname) {
			this.co_shortname = co_shortname;
		}
    	
    }
}
