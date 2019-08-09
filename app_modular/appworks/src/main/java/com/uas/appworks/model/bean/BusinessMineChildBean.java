package com.uas.appworks.model.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/17 11:18
 */
public class BusinessMineChildBean implements MultiItemEntity {
    public static final int BUSINESS_MINE_PARENT = 1;
    public static final int BUSINESS_MINE_CHILD = 2;

    private int mItemType = -1;
    private String mCaption;
    private String mDataIndex;
    private String mValue;
    private int mId;
    private String mBcType;
    private String mStageCode;
    private String mBcCode;
    private String mBcDescription;

    public void setItemType(int itemType) {
        mItemType = itemType;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getDataIndex() {
        return mDataIndex;
    }

    public void setDataIndex(String dataIndex) {
        mDataIndex = dataIndex;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getBcType() {
        return mBcType;
    }

    public void setBcType(String bcType) {
        mBcType = bcType;
    }

    public String getStageCode() {
        return mStageCode;
    }

    public void setStageCode(String stageCode) {
        mStageCode = stageCode;
    }

    public String getBcCode() {
        return mBcCode;
    }

    public void setBcCode(String bcCode) {
        mBcCode = bcCode;
    }

    public String getBcDescription() {
        return mBcDescription;
    }

    public void setBcDescription(String bcDescription) {
        mBcDescription = bcDescription;
    }

    @Override
    public int getItemType() {
        return mItemType;
    }
}
