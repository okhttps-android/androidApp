package com.uas.appworks.model.bean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/16 16:12
 */

public class B2BDetailListBean {
    public final static int TYPE_DETAIL_TEXT = 11;
    public final static int TYPE_DETAIL_TEXT_WHITE = 12;
    public final static int TYPE_DETAIL_EDIT = 13;
    public final static int TYPE_DETAIL_OPTION = 14;

    public final static int EDIT_TYPE_DECIMAL = 21;
    public final static int EDIT_TYPE_NUMBER = 22;
    public final static int EDIT_TYPE_TEXT = 23;

    private int mItemType = TYPE_DETAIL_TEXT;
    private String mCaption;
    private String mValue;
    private String mUnit;
    private String mFlag;
    private String mType;
    private int mEditType = EDIT_TYPE_TEXT;
    private List<String> mOptions;

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public int getItemType() {
        return mItemType;
    }

    public void setItemType(int itemType) {
        mItemType = itemType;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }

    public String getFlag() {
        return mFlag;
    }

    public void setFlag(String flag) {
        mFlag = flag;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public int getEditType() {
        return mEditType;
    }

    public void setEditType(int editType) {
        mEditType = editType;
    }

    public List<String> getOptions() {
        return mOptions;
    }

    public void setOptions(List<String> options) {
        mOptions = options;
    }
}
