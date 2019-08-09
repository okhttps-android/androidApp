package com.modular.appmessages.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongpm on 2016/6/3.
 */
public class SubscriptionMessage implements Parcelable{

    private int ID_;
    private int NUM_ID_;        //订阅id
    private int INSTANCE_ID_;   //实例id
    private String CREATEDATE_; //创建时间
    private String TITLE_;
    private String SON_TITLE_;  //合计标题
    private int STATUS_;      //阅读状态
    private int EMP_ID_;
    private int RN;

    public SubscriptionMessage(){}


    protected SubscriptionMessage(Parcel in) {
        ID_ = in.readInt();
        NUM_ID_ = in.readInt();
        INSTANCE_ID_ = in.readInt();
        CREATEDATE_ = in.readString();
        TITLE_ = in.readString();
        SON_TITLE_ = in.readString();
        STATUS_ = in.readInt();
        EMP_ID_ = in.readInt();
        RN = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID_);
        dest.writeInt(NUM_ID_);
        dest.writeInt(INSTANCE_ID_);
        dest.writeString(CREATEDATE_);
        dest.writeString(TITLE_);
        dest.writeString(SON_TITLE_);
        dest.writeInt(STATUS_);
        dest.writeInt(EMP_ID_);
        dest.writeInt(RN);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubscriptionMessage> CREATOR = new Creator<SubscriptionMessage>() {
        @Override
        public SubscriptionMessage createFromParcel(Parcel in) {
            return new SubscriptionMessage(in);
        }

        @Override
        public SubscriptionMessage[] newArray(int size) {
            return new SubscriptionMessage[size];
        }
    };

    public int getID_() {
        return ID_;
    }

    public void setID_(int ID_) {
        this.ID_ = ID_;
    }

    public int getNUM_ID_() {
        return NUM_ID_;
    }

    public void setNUM_ID_(int NUM_ID_) {
        this.NUM_ID_ = NUM_ID_;
    }

    public int getINSTANCE_ID_() {
        return INSTANCE_ID_;
    }

    public void setINSTANCE_ID_(int INSTANCE_ID_) {
        this.INSTANCE_ID_ = INSTANCE_ID_;
    }

    public String getCREATEDATE_() {
        return CREATEDATE_;
    }

    public void setCREATEDATE_(String CREATEDATE_) {
        this.CREATEDATE_ = CREATEDATE_;
    }

    public String getTITLE_() {
        return TITLE_;
    }

    public void setTITLE_(String TITLE_) {
        this.TITLE_ = TITLE_;
    }

    public String getSON_TITLE_() {
        return SON_TITLE_;
    }

    public void setSON_TITLE_(String SON_TITLE_) {
        this.SON_TITLE_ = SON_TITLE_;
    }

    public int getSTATUS_() {
        return STATUS_;
    }

    public void setSTATUS_(int STATUS_) {
        this.STATUS_ = STATUS_;
    }

    public int getEMP_ID_() {
        return EMP_ID_;
    }

    public void setEMP_ID_(int EMP_ID_) {
        this.EMP_ID_ = EMP_ID_;
    }

    public int getRN() {
        return RN;
    }

    public void setRN(int RN) {
        this.RN = RN;
    }
}
