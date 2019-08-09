package com.uas.appme.settings.model;

/**
 * Created by FANGlh on 2017/10/12.
 * function:
 *
 * map包括sf_userid 人员ID,sf_username 姓名,sf_date 休息日,sf_companyid 公司UU,sf_companyname 公司名称
 */

public class ComRestBean {
    private String sf_userid;
    private String sf_username;
    private String sf_date;
    private String sf_companyid;
    private String sf_companyname;

    public String getSf_userid() {
        return sf_userid;
    }

    public void setSf_userid(String sf_userid) {
        this.sf_userid = sf_userid;
    }

    public String getSf_username() {
        return sf_username;
    }

    public void setSf_username(String sf_username) {
        this.sf_username = sf_username;
    }

    public String getSf_date() {
        return sf_date;
    }

    public void setSf_date(String sf_date) {
        this.sf_date = sf_date;
    }

    public String getSf_companyid() {
        return sf_companyid;
    }

    public void setSf_companyid(String sf_companyid) {
        this.sf_companyid = sf_companyid;
    }

    public String getSf_companyname() {
        return sf_companyname;
    }

    public void setSf_companyname(String sf_companyname) {
        this.sf_companyname = sf_companyname;
    }
}
