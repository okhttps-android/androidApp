package com.xzjmyk.pm.activity.ui.erp.model;

/**
 * Created by FANGlh on 2016/10/28.
 */
public class NoticeData {
    private int NO_ID;

    private String NO_TITLE;

    private String NO_APPROVER;

    private Long NO_APPTIME;

    private String NO_INFOTYPE;

    private String NO_EMERGENCY;

    private Object NO_KEYWORD;//公告

    private String NO_CONTENT;

    private int NO_ISTOP;

    private int NO_ISREAD;//

    private int NO_ISPUBLIC;

    private int RN;
    private Object NO_RECIPIENTID;

    private Object STATUS;

    public Object getNO_RECIPIENTID() {
        return NO_RECIPIENTID;
    }

    public void setNO_RECIPIENTID(Object NO_RECIPIENTID) {
        this.NO_RECIPIENTID = NO_RECIPIENTID;
    }

    public int getNO_ID() {
        return NO_ID;
    }

    public void setNO_ID(int NO_ID) {
        this.NO_ID = NO_ID;
    }

    public String getNO_TITLE() {
        return NO_TITLE;
    }

    public void setNO_TITLE(String NO_TITLE) {
        this.NO_TITLE = NO_TITLE;
    }

    public String getNO_APPROVER() {
        return NO_APPROVER;
    }

    public void setNO_APPROVER(String NO_APPROVER) {
        this.NO_APPROVER = NO_APPROVER;
    }

    public Long getNO_APPTIME() {
        return NO_APPTIME;
    }

    public void setNO_APPTIME(Long NO_APPTIME) {
        this.NO_APPTIME = NO_APPTIME;
    }

    public String getNO_INFOTYPE() {
        return NO_INFOTYPE;
    }

    public void setNO_INFOTYPE(String NO_INFOTYPE) {
        this.NO_INFOTYPE = NO_INFOTYPE;
    }

    public String getNO_EMERGENCY() {
        return NO_EMERGENCY;
    }

    public void setNO_EMERGENCY(String NO_EMERGENCY) {
        this.NO_EMERGENCY = NO_EMERGENCY;
    }

    public Object getNO_KEYWORD() {
        return NO_KEYWORD;
    }

    public void setNO_KEYWORD(Object NO_KEYWORD) {
        this.NO_KEYWORD = NO_KEYWORD;
    }

    public String getNO_CONTENT() {
        return NO_CONTENT;
    }

    public void setNO_CONTENT(String NO_CONTENT) {
        this.NO_CONTENT = NO_CONTENT;
    }

    public int getNO_ISTOP() {
        return NO_ISTOP;
    }

    public void setNO_ISTOP(int NO_ISTOP) {
        this.NO_ISTOP = NO_ISTOP;
    }

    public int getNO_ISREAD() {
        return NO_ISREAD;
    }

    public void setNO_ISREAD(int NO_ISREAD) {
        this.NO_ISREAD = NO_ISREAD;
    }

    public int getNO_ISPUBLIC() {
        return NO_ISPUBLIC;
    }

    public void setNO_ISPUBLIC(int NO_ISPUBLIC) {
        this.NO_ISPUBLIC = NO_ISPUBLIC;
    }

    public int getRN() {
        return RN;
    }

    public void setRN(int RN) {
        this.RN = RN;
    }

    public Object getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(Object STATUS) {
        this.STATUS = STATUS;
    }
}
