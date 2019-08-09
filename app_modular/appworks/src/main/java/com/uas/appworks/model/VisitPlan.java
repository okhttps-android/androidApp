package com.uas.appworks.model;

//拜访计划对象
public class VisitPlan {
    private int id;
    private String code;
    private long entryDate;
    private String customerName;
    private String projectName;
    private String customerAddress;
    private long startTime;
    private long endTime;
    private String status;//转单状态
    private String doman;
    private String domanCode;
    private String recordDay;
    private String billStatus;//单据状态

    public String getRecordDay() {
        return recordDay == null ? "" : recordDay;
    }

    public void setRecordDay(String recordDay) {
        this.recordDay = recordDay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(long entryDate) {
        this.entryDate = entryDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDoman() {
        return doman;
    }

    public void setDoman(String doman) {
        this.doman = doman;
    }

    public String getDomanCode() {
        return domanCode;
    }

    public void setDomanCode(String domanCode) {
        this.domanCode = domanCode;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }
}
