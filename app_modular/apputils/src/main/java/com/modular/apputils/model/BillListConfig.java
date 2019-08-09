package com.modular.apputils.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BillListConfig implements Parcelable {
    private boolean isMe;//是否是我的
    private String mTitle;//标题
    private String mCaller;
    private String mCondition;//条件语句
    private int showItemNum;//显示条目
    private boolean needForward;//是否需要专题数据

    public BillListConfig() {
    }

    public BillListConfig(BillListConfig mBillListConfig) {
        this.isMe = mBillListConfig.isMe;
        this.mTitle = mBillListConfig.mTitle;
        this.mCaller = mBillListConfig.mCaller;
        this.mCondition = mBillListConfig.mCondition;
        this.showItemNum = mBillListConfig.showItemNum;
        this.needForward = mBillListConfig.needForward;
    }


    protected BillListConfig(Parcel in) {
        needForward = in.readByte() != 0;
        isMe = in.readByte() != 0;
        mTitle = in.readString();
        mCaller = in.readString();
        mCondition = in.readString();
        showItemNum = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (needForward ? 1 : 0));
        parcel.writeByte((byte) (isMe ? 1 : 0));
        parcel.writeString(mTitle);
        parcel.writeString(mCaller);
        parcel.writeString(mCondition);
        parcel.writeInt(showItemNum);
    }

    public static final Creator<BillListConfig> CREATOR = new Creator<BillListConfig>() {
        @Override
        public BillListConfig createFromParcel(Parcel in) {
            return new BillListConfig(in);
        }

        @Override
        public BillListConfig[] newArray(int size) {
            return new BillListConfig[size];
        }
    };

    public void setMe(boolean me) {
        isMe = me;
    }

    public boolean isMe() {
        return isMe;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getCaller() {
        return mCaller;
    }

    public void setCaller(String mCaller) {
        this.mCaller = mCaller;
    }

    public String getCondition() {
        return mCondition;
    }

    public void setCondition(String mCondition) {
        this.mCondition = mCondition;
    }

    public int getShowItemNum() {
        return showItemNum;
    }

    public void setShowItemNum(int showItemNum) {
        this.showItemNum = showItemNum;
    }

    public boolean isNeedForward() {
        return needForward;
    }

    public void setNeedForward(boolean needForward) {
        this.needForward = needForward;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
