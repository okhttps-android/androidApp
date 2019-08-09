package com.uas.appme.settings.model;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/6/8 14:46
 */
public class SystemAdminBean {
    private String mName;
    private String mMobile;
    private String mPosition;

    public SystemAdminBean() {
    }

    public SystemAdminBean(String name, String mobile, String position) {
        mName = name;
        mMobile = mobile;
        mPosition = position;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getMobile() {
        return mMobile;
    }

    public void setMobile(String mobile) {
        mMobile = mobile;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        mPosition = position;
    }
}
