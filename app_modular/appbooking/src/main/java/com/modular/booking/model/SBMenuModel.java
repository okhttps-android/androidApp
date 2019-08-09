package com.modular.booking.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 服務预约
 * Created by Arison on 2017/9/27.
 */

public class SBMenuModel implements Parcelable {
    
    private int icon;
    private String code;
    private String title;
    private int descColor;
    private String desc;
    private String url;
    private String data;
    private boolean isBooking=false;//是否可以预约

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDescColor() {
        return descColor;
    }

    public void setDescColor(int descColor) {
        this.descColor = descColor;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isBooking() {
        return isBooking;
    }

    public void setBooking(boolean booking) {
        isBooking = booking;
    }

    public SBMenuModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.icon);
        dest.writeString(this.code);
        dest.writeString(this.title);
        dest.writeInt(this.descColor);
        dest.writeString(this.desc);
        dest.writeString(this.url);
        dest.writeString(this.data);
        dest.writeByte(this.isBooking ? (byte) 1 : (byte) 0);
    }

    protected SBMenuModel(Parcel in) {
        this.icon = in.readInt();
        this.code = in.readString();
        this.title = in.readString();
        this.descColor = in.readInt();
        this.desc = in.readString();
        this.url = in.readString();
        this.data = in.readString();
        this.isBooking = in.readByte() != 0;
    }

    public static final Creator<SBMenuModel> CREATOR = new Creator<SBMenuModel>() {
        @Override
        public SBMenuModel createFromParcel(Parcel source) {
            return new SBMenuModel(source);
        }

        @Override
        public SBMenuModel[] newArray(int size) {
            return new SBMenuModel[size];
        }
    };
}
