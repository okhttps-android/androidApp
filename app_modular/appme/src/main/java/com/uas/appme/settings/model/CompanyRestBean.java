package com.uas.appme.settings.model;

/**
 * Created by FANGlh on 2017/10/12.
 * function:
 *
 *
 * map包括sc_companyid 公司UU,sc_companyname 公司名称,sc_date 休息日
 */

public class CompanyRestBean  {
    private String sc_companyid;
    private String sc_companyname;
    private String sc_date;

    public String getSc_companyid() {
        return sc_companyid;
    }

    public void setSc_companyid(String sc_companyid) {
        this.sc_companyid = sc_companyid;
    }

    public String getSc_companyname() {
        return sc_companyname;
    }

    public void setSc_companyname(String sc_companyname) {
        this.sc_companyname = sc_companyname;
    }

    public String getSc_date() {
        return sc_date;
    }

    public void setSc_date(String sc_date) {
        this.sc_date = sc_date;
    }
}
