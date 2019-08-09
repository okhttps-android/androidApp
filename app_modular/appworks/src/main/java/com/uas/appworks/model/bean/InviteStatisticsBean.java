package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe 邀请记录
 * @date 2018/3/27 14:44
 */

public class InviteStatisticsBean {
    private int mMonth = 0;
    private int mInviteCount = 0;
    private int mRegisterCount = 0;

    public int getMonth() {
        return mMonth;
    }

    public void setMonth(int month) {
        mMonth = month;
    }

    public int getInviteCount() {
        return mInviteCount;
    }

    public void setInviteCount(int inviteCount) {
        mInviteCount = inviteCount;
    }

    public int getRegisterCount() {
        return mRegisterCount;
    }

    public void setRegisterCount(int registerCount) {
        mRegisterCount = registerCount;
    }
}
