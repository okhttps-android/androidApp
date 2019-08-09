package com.modular.booking.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arison on 2017/11/30.
 */

public class SeatsStateModel implements Parcelable {
//    "ad_companyid":"10002",
//            "ad_deskcode":"C10",
//            "ad_id":"132",
//            "ad_status":"0"

    private String ad_companyid;
    private String ad_deskcode;
    private String ad_id;
    private String ad_status;//0代表空闲  1代表是锁定
    private String ad_bookid;

    public String getAd_companyid() {
        return ad_companyid;
    }

    public void setAd_companyid(String ad_companyid) {
        this.ad_companyid = ad_companyid;
    }

    public String getAd_deskcode() {
        return ad_deskcode;
    }

    public void setAd_deskcode(String ad_deskcode) {
        this.ad_deskcode = ad_deskcode;
    }

    public String getAd_id() {
        return ad_id;
    }

    public void setAd_id(String ad_id) {
        this.ad_id = ad_id;
    }

    public String getAd_status() {
        return ad_status;
    }

    public void setAd_status(String ad_status) {
        this.ad_status = ad_status;
    }

    public String getAd_bookid() {
        return ad_bookid;
    }

    public void setAd_bookid(String ad_bookid) {
        this.ad_bookid = ad_bookid;
    }

    public SeatsStateModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ad_companyid);
        dest.writeString(this.ad_deskcode);
        dest.writeString(this.ad_id);
        dest.writeString(this.ad_status);
        dest.writeString(this.ad_bookid);
    }

    protected SeatsStateModel(Parcel in) {
        this.ad_companyid = in.readString();
        this.ad_deskcode = in.readString();
        this.ad_id = in.readString();
        this.ad_status = in.readString();
        this.ad_bookid = in.readString();
    }

    public static final Creator<SeatsStateModel> CREATOR = new Creator<SeatsStateModel>() {
        @Override
        public SeatsStateModel createFromParcel(Parcel source) {
            return new SeatsStateModel(source);
        }

        @Override
        public SeatsStateModel[] newArray(int size) {
            return new SeatsStateModel[size];
        }
    };
}
