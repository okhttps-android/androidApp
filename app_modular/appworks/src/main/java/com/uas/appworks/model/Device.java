package com.uas.appworks.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bitlike on 2018/3/19.
 */

public class Device implements Parcelable {
    private String code;
    private String name;
    private String lineCode;//线别
    private String centerCode;//部门
    private String centerName;//部门
    private String workshop;//使用车间


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getCenterCode() {
        return centerCode;
    }

    public void setCenterCode(String centerCode) {
        this.centerCode = centerCode;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.name);
        dest.writeString(this.lineCode);
        dest.writeString(this.centerCode);
        dest.writeString(this.centerName);
        dest.writeString(this.workshop);
    }

    public Device() {
    }

    protected Device(Parcel in) {
        this.code = in.readString();
        this.name = in.readString();
        this.lineCode = in.readString();
        this.centerCode = in.readString();
        this.centerName = in.readString();
        this.workshop = in.readString();
    }

    public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
