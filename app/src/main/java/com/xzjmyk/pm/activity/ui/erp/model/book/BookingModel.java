package com.xzjmyk.pm.activity.ui.erp.model.book;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 预约列表实体类
 * Created by Arison on 2017/6/27.
 */
public class BookingModel implements Parcelable {
    
    private String ab_address;
    private String ab_bman;
    private String ab_bmanid;
    private String ab_confirmstatus;
    private String ab_content;
    private String ab_endtime;
    private String ab_id;
    private String ab_latitude;
    private String ab_longitude;
    private String ab_recorddate;
    private String ab_recordid;
    private String ab_recordman;
    private String ab_sharestatus;
    private String ab_starttime;
    private String ab_type;

    public String getAb_type() {
        return ab_type;
    }

    public void setAb_type(String ab_type) {
        this.ab_type = ab_type;
    }

    public void setAb_address(String ab_address) {
        this.ab_address = ab_address;
    }

    public String getAb_address() {
        return ab_address;
    }

    public void setAb_bman(String ab_bman) {
        this.ab_bman = ab_bman;
    }

    public String getAb_bman() {
        return ab_bman;
    }

    public void setAb_bmanid(String ab_bmanid) {
        this.ab_bmanid = ab_bmanid;
    }

    public String getAb_bmanid() {
        return ab_bmanid;
    }

    public void setAb_confirmstatus(String ab_confirmstatus) {
        this.ab_confirmstatus = ab_confirmstatus;
    }

    public String getAb_confirmstatus() {
        return ab_confirmstatus;
    }

    public void setAb_content(String ab_content) {
        this.ab_content = ab_content;
    }

    public String getAb_content() {
        return ab_content;
    }

    public void setAb_endtime(String ab_endtime) {
        this.ab_endtime = ab_endtime;
    }

    public String getAb_endtime() {
        return ab_endtime;
    }

    public void setAb_id(String ab_id) {
        this.ab_id = ab_id;
    }

    public String getAb_id() {
        return ab_id;
    }

    public void setAb_latitude(String ab_latitude) {
        this.ab_latitude = ab_latitude;
    }

    public String getAb_latitude() {
        return ab_latitude;
    }

    public void setAb_longitude(String ab_longitude) {
        this.ab_longitude = ab_longitude;
    }

    public String getAb_longitude() {
        return ab_longitude;
    }

    public String getAb_recorddate() {
        return ab_recorddate;
    }

    public void setAb_recorddate(String ab_recorddate) {
        this.ab_recorddate = ab_recorddate;
    }

    public void setAb_recordid(String ab_recordid) {
        this.ab_recordid = ab_recordid;
    }

    public String getAb_recordid() {
        return ab_recordid;
    }

    public void setAb_recordman(String ab_recordman) {
        this.ab_recordman = ab_recordman;
    }

    public String getAb_recordman() {
        return ab_recordman;
    }

    public void setAb_sharestatus(String ab_sharestatus) {
        this.ab_sharestatus = ab_sharestatus;
    }

    public String getAb_sharestatus() {
        return ab_sharestatus;
    }

    public void setAb_starttime(String ab_starttime) {
        this.ab_starttime = ab_starttime;
    }

    public String getAb_starttime() {
        return ab_starttime;
    }

    public BookingModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ab_address);
        dest.writeString(this.ab_bman);
        dest.writeString(this.ab_bmanid);
        dest.writeString(this.ab_confirmstatus);
        dest.writeString(this.ab_content);
        dest.writeString(this.ab_endtime);
        dest.writeString(this.ab_id);
        dest.writeString(this.ab_latitude);
        dest.writeString(this.ab_longitude);
        dest.writeString(this.ab_recorddate);
        dest.writeString(this.ab_recordid);
        dest.writeString(this.ab_recordman);
        dest.writeString(this.ab_sharestatus);
        dest.writeString(this.ab_starttime);
        dest.writeString(this.ab_type);
    }

    protected BookingModel(Parcel in) {
        this.ab_address = in.readString();
        this.ab_bman = in.readString();
        this.ab_bmanid = in.readString();
        this.ab_confirmstatus = in.readString();
        this.ab_content = in.readString();
        this.ab_endtime = in.readString();
        this.ab_id = in.readString();
        this.ab_latitude = in.readString();
        this.ab_longitude = in.readString();
        this.ab_recorddate = in.readString();
        this.ab_recordid = in.readString();
        this.ab_recordman = in.readString();
        this.ab_sharestatus = in.readString();
        this.ab_starttime = in.readString();
        this.ab_type = in.readString();
    }

    public static final Creator<BookingModel> CREATOR = new Creator<BookingModel>() {
        @Override
        public BookingModel createFromParcel(Parcel source) {
            return new BookingModel(source);
        }

        @Override
        public BookingModel[] newArray(int size) {
            return new BookingModel[size];
        }
    };
}
