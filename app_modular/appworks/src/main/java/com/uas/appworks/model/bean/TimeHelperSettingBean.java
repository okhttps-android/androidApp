package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/8/28 8:58
 */
public class TimeHelperSettingBean {
    private String mName;
    private String mConfig;
    private int mChecked;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getConfig() {
        return mConfig;
    }

    public void setConfig(String config) {
        mConfig = config;
    }

    public int getChecked() {
        return mChecked;
    }

    public void setChecked(int checked) {
        mChecked = checked;
    }
}
