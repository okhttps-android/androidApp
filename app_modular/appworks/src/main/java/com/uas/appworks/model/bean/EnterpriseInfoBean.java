package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe 邀请注册企业信息
 * @date 2018/3/26 14:12
 */

public class EnterpriseInfoBean {
    private String mEnuu; //注册企业UU号
    private String mEnName; //企业名称
    private String mEnTel;//企业联系电话
    private String mEnAddress;//企业地址
    private String mAdminName;//管理员
    private String mEnCorporation;//法人
    private String mDate;//注册时间
    private String mInviteEnName;//邀请企业名
    private String mInviteUserName;//邀请用户名

    public String getEnuu() {
        return mEnuu;
    }

    public void setEnuu(String enuu) {
        mEnuu = enuu;
    }

    public String getEnName() {
        return mEnName;
    }

    public void setEnName(String enName) {
        mEnName = enName;
    }

    public String getEnTel() {
        return mEnTel;
    }

    public void setEnTel(String enTel) {
        mEnTel = enTel;
    }

    public String getEnAddress() {
        return mEnAddress;
    }

    public void setEnAddress(String enAddress) {
        mEnAddress = enAddress;
    }

    public String getAdminName() {
        return mAdminName;
    }

    public void setAdminName(String adminName) {
        mAdminName = adminName;
    }

    public String getEnCorporation() {
        return mEnCorporation;
    }

    public void setEnCorporation(String enCorporation) {
        mEnCorporation = enCorporation;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getInviteEnName() {
        return mInviteEnName;
    }

    public void setInviteEnName(String inviteEnName) {
        mInviteEnName = inviteEnName;
    }

    public String getInviteUserName() {
        return mInviteUserName;
    }

    public void setInviteUserName(String inviteUserName) {
        mInviteUserName = inviteUserName;
    }
}
