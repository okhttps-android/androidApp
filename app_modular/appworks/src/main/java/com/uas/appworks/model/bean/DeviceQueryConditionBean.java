package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/12/19 10:31
 */

public class DeviceQueryConditionBean {
    private String mCaption;
    private String mField;
    private String mType;

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getField() {
        return mField;
    }

    public void setField(String field) {
        mField = field;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }
}
