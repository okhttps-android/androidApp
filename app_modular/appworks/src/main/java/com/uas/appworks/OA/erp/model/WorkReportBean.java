package com.uas.appworks.OA.erp.model;

/**
 * @author RaoMeng
 * @describe 工作汇报列表实体类
 * @date 2017/10/19 17:05
 */

public class WorkReportBean {

    /**
     * WM_ID : 3202
     * emp : 饶猛
     * date : 2017-10-19
     * comment : 测试
     * WM_DEPART : 采购部
     * WM_JONAME : D1
     * WM_PLAN : null
     * WM_EXPERIENCE : null
     * WM_CONTEXT :
     * STATUS : 待审批
     * WM_STATUS : 已提交
     * WM_UNFINISHEDTASK :
     * WM_MONTH : 10
     * WM_STARTTIME : 2017-10-01
     * WM_ENDTIME : 2017-10-31
     * RN : 1
     */

    private int reportId;
    private String emp;
    private String date;
    private String comment;
    private String depart;
    private String joname;
    private String plan;
    private String experience;
    private String context;
    private String approvalStatus;
    private String reportStatus;
    private String unfinishedTask;
    private String entryDate;
    private String weekDays;
    private String serial;
    private String startTime;
    private String endTime;
    private int RN;

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getEmp() {
        return emp;
    }

    public void setEmp(String emp) {
        this.emp = emp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getJoname() {
        return joname;
    }

    public void setJoname(String joname) {
        this.joname = joname;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getUnfinishedTask() {
        return unfinishedTask;
    }

    public void setUnfinishedTask(String unfinishedTask) {
        this.unfinishedTask = unfinishedTask;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(String weekDays) {
        this.weekDays = weekDays;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getRN() {
        return RN;
    }

    public void setRN(int RN) {
        this.RN = RN;
    }
}
