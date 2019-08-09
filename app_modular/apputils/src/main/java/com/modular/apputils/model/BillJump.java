package com.modular.apputils.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

//跳转对象
public class BillJump implements Parcelable {
    private int id;
    private Class mJumpClass;//跳转的界面对象
    private String mTitle;//跳转的标题
    private String mField;//点击的字段
    private String mCaption;//点击的字段标题
    private HashMap<String,String> mParam;


    protected BillJump(Parcel in) {
        id = in.readInt();
        mTitle = in.readString();
        mField = in.readString();
        mCaption = in.readString();
        mParam = (HashMap<String, String>) in.readSerializable();
        mJumpClass = (Class) in.readSerializable();
    }

    public static final Creator<BillJump> CREATOR = new Creator<BillJump>() {
        @Override
        public BillJump createFromParcel(Parcel in) {
            return new BillJump(in);
        }

        @Override
        public BillJump[] newArray(int size) {
            return new BillJump[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(mTitle);
        parcel.writeString(mField);
        parcel.writeString(mCaption);
        parcel.writeSerializable(mParam);
        parcel.writeSerializable(mJumpClass);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Class getJumpClass() {
        return mJumpClass;
    }

    public void setJumpClass(Class mJumpClass) {
        this.mJumpClass = mJumpClass;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getField() {
        return mField==null?"":mField;
    }

    public void setField(String mField) {
        this.mField = mField;
    }

    public String getCaption() {
        return mCaption==null?"":mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public HashMap<String, String> getParam() {
        return mParam;
    }

    public void setParam(HashMap<String, String> mParam) {
        this.mParam = mParam;
    }
}
