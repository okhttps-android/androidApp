package com.uas.appworks.OA.platform.model;

/**
 * Created by Bitlike on 2017/11/7.
 */

public class CharitModel {

    private int id;
    private int orgId;
    private String area;
    private String proSummary;
    private String target;
    private String totalAmount;//捐款总额
    private String name;
    private String logo;
    private String mobileImg;
    private String listImg;
    private String orgName;
    private String overdue;

    public CharitModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getArea() {
        return area == null ? "" : area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getProSummary() {
        return proSummary == null ? "" : proSummary;
    }

    public void setProSummary(String proSummary) {
        this.proSummary = proSummary;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public String getListImageUrl() {
        return this.listImg == null ? (this.mobileImg == null ? this.logo : this.mobileImg) : this.listImg;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOverdue() {
        return overdue;
    }

    public boolean isEnded() {
        return overdue == null ? true : (overdue.equals("已结束"));
    }

    public void setOverdue(String overdue) {
        this.overdue = overdue;
    }

    public String getMobileImg() {
        return mobileImg;
    }

    public void setMobileImg(String mobileImg) {
        this.mobileImg = mobileImg;
    }

    public String getListImg() {
        return listImg;
    }

    public void setListImg(String listImg) {
        this.listImg = listImg;
    }
}
