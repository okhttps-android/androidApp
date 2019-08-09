package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe 注册明细
 * @date 2018/3/26 17:32
 */

public class RegisterListBean {
    public final static int STATE_REGISTER = 0x01;
    public final static int STATE_UNREGISTER = 0x02;

    private String mEnName;
    private String mLinkman;
    private String mPhone;
    private String mInviteDate;
    private String mRegisterDate;
    private String mInviteName;
    private int mState;
    private String mJson;

    public String getEnName() {
        return mEnName;
    }

    public void setEnName(String enName) {
        mEnName = enName;
    }

    public String getLinkman() {
        return mLinkman;
    }

    public void setLinkman(String linkman) {
        mLinkman = linkman;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getInviteDate() {
        return mInviteDate;
    }

    public void setInviteDate(String inviteDate) {
        mInviteDate = inviteDate;
    }

    public String getRegisterDate() {
        return mRegisterDate;
    }

    public void setRegisterDate(String registerDate) {
        mRegisterDate = registerDate;
    }

    public String getInviteName() {
        return mInviteName;
    }

    public void setInviteName(String inviteName) {
        mInviteName = inviteName;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public String getJson() {
        return mJson;
    }

    public void setJson(String json) {
        mJson = json;
    }
}
