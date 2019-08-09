package com.uas.appworks.OA.erp.model;

/**
 * Created by FANGlh on 2016/11/1.
 */
public class WorkDailyBean {
    private int WD_ID;//单据id
    private String WD_EMP;//员工姓名
    private String WD_DATE;//日期
    private String WD_COMMENT;//日报内容
    private String WD_DEPART;//部门
    private String WD_JONAME;//职位
    private String STATUS;//状态
    private int RN; //
    private String WD_PLAN;//明日工作计划
    private String WD_EXPERIENCE;//工作心得
    private  String WD_STATUS; //准确的状态，在录入、待审批、已审核
    private String WD_CONTEXT;//已完成工作内容
    private  String WD_UNFINISHEDTASK; //未完成的项目任务
    public String getWD_UNFINISHEDTASK() {
        return WD_UNFINISHEDTASK;
    }
    public void setWD_UNFINISHEDTASK(String WD_UNFINISHEDTASK) {
        this.WD_UNFINISHEDTASK = WD_UNFINISHEDTASK;
    }

    public String getWD_CONTEXT() {
        return WD_CONTEXT;
    }

    public void setWD_CONTEXT(String WD_CONTEXT) {
        this.WD_CONTEXT = WD_CONTEXT;
    }

    public String getWD_STATUS() {
        return WD_STATUS;
    }

    public void setWD_STATUS(String WD_STATUS) {
        this.WD_STATUS = WD_STATUS;
    }

    public int getWD_ID() {
        return WD_ID;
    }

    public void setWD_ID(int WD_ID) {
        this.WD_ID = WD_ID;
    }

    public String getWD_EMP() {
        return WD_EMP;
    }

    public void setWD_EMP(String WD_EMP) {
        this.WD_EMP = WD_EMP;
    }

    public String getWD_DATE() {
        return WD_DATE;
    }

    public void setWD_DATE(String WD_DATE) {
        this.WD_DATE = WD_DATE;
    }

    public String getWD_COMMENT() {
        return WD_COMMENT;
    }

    public void setWD_COMMENT(String WD_COMMENT) {
        this.WD_COMMENT = WD_COMMENT;
    }

    public String getWD_DEPART() {
        return WD_DEPART;
    }

    public void setWD_DEPART(String WD_DEPART) {
        this.WD_DEPART = WD_DEPART;
    }

    public String getWD_JONAME() {
        return WD_JONAME;
    }

    public void setWD_JONAME(String WD_JONAME) {
        this.WD_JONAME = WD_JONAME;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public int getRN() {
        return RN;
    }

    public void setRN(int RN) {
        this.RN = RN;
    }

    public String getWD_PLAN() {
        return WD_PLAN;
    }

    public void setWD_PLAN(String WD_PLAN) {
        this.WD_PLAN = WD_PLAN;
    }

    public String getWD_EXPERIENCE() {
        return WD_EXPERIENCE;
    }

    public void setWD_EXPERIENCE(String WD_EXPERIENCE) {
        this.WD_EXPERIENCE = WD_EXPERIENCE;
    }
}
