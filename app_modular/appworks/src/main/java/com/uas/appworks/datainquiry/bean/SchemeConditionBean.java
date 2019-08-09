package com.uas.appworks.datainquiry.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/17.
 */
public class SchemeConditionBean implements Serializable, Comparable<SchemeConditionBean> {
    private String mPosition;
    private String mCaption;
    private String mField;
    private String mType;
    private int mWidth;
    private boolean mAppCondition;
    private String mDefaultValue;
    private List<Property> mProperties;
    private String mTable;

    public String getTable() {
        return mTable;
    }

    public void setTable(String table) {
        mTable = table;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        mPosition = position;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        mDefaultValue = defaultValue;
    }

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

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public boolean isAppCondition() {
        return mAppCondition;
    }

    public void setAppCondition(boolean appCondition) {
        mAppCondition = appCondition;
    }

    public List<Property> getProperties() {
        return mProperties;
    }

    public void setProperties(List<Property> properties) {
        mProperties = properties;
    }

    @Override
    public int compareTo(SchemeConditionBean o) {
        String currentPostion = this.getPosition().substring(4);
        String comPosition = o.getPosition().substring(4);
        int currentPos = 0;
        int comPos = 0;
        try {
            currentPos = Integer.parseInt(currentPostion);
            comPos = Integer.parseInt(comPosition);
        } catch (Exception e) {

        }

        return currentPos - comPos;
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
