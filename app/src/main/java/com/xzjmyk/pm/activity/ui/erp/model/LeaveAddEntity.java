package com.xzjmyk.pm.activity.ui.erp.model;


/**
 * @author :LiuJie 2015年7月14日 下午1:47:33
 * @注释:请假单实体
 */
public class LeaveAddEntity {
    private String va_vacationtype;//假期类型
    private String va_startime;//开始时间
    private String va_remark;//请假原因
    private String va_endtime;//结束时间
    private String enuu;
    private String emcode;

    public String getVa_vacationtype() {
        return va_vacationtype;
    }

    public void setVa_vacationtype(String va_vacationtype) {
        this.va_vacationtype = va_vacationtype;
    }

    public String getVa_startime() {
        return va_startime;
    }

    public void setVa_startime(String va_startime) {
        this.va_startime = va_startime;
    }

    public String getVa_remark() {
        return va_remark;
    }

    public void setVa_remark(String va_remark) {
        this.va_remark = va_remark;
    }

    public String getVa_endtime() {
        return va_endtime;
    }

    public void setVa_endtime(String va_endtime) {
        this.va_endtime = va_endtime;
    }

    public String getEnuu() {
        return enuu;
    }

    public void setEnuu(String enuu) {
        this.enuu = enuu;
    }

    public String getEmcode() {
        return emcode;
    }

    public void setEmcode(String emcode) {
        this.emcode = emcode;
    }
}

