package com.uas.appworks.datainquiry.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/21.
 */
public class ReportConditionBean implements Serializable {
    private String mField;
    private String mTitle;
    private String mType;
    private String mReadOnly;
    private List<Property> mProperties;

    public String getField() {
        return mField;
    }

    public void setField(String field) {
        mField = field;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getReadOnly() {
        return mReadOnly;
    }

    public void setReadOnly(String readOnly) {
        mReadOnly = readOnly;
    }

    public List<Property> getProperties() {
        return mProperties;
    }

    public void setProperties(List<Property> properties) {
        mProperties = properties;
    }

    public static class Property implements Serializable {
        private String mDisplay = "";
        private String mValue = "";
        private boolean mState = false;

        public String getDisplay() {
            return mDisplay;
        }

        public void setDisplay(String display) {
            mDisplay = display;
        }

        public String getValue() {
            return mValue;
        }

        public void setValue(String value) {
            mValue = value;
        }

        public boolean isState() {
            return mState;
        }

        public void setState(boolean state) {
            mState = state;
        }
    }
}
