package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

public class HrorgsEntity implements Parcelable {

    private String or_code;

    private String whichsys;

    private int or_subof;

    private String company;

    private int or_isleaf;

    private String or_name;

    private int or_id;


    private String flag;
    //领导人编号
    private String or_headmancode;
    //领导人名字
    private String or_headmanname;
    //1代表是创建管理群 ,null代表没有
    private int or_remark;

public HrorgsEntity(){}

    protected HrorgsEntity(Parcel in) {
        or_code = in.readString();
        whichsys = in.readString();
        or_subof = in.readInt();
        company = in.readString();
        or_isleaf = in.readInt();
        or_name = in.readString();
        or_id = in.readInt();
        flag = in.readString();
        or_headmancode = in.readString();
        or_headmanname = in.readString();
        or_remark = in.readInt();
    }

    public static final Creator<HrorgsEntity> CREATOR = new Creator<HrorgsEntity>() {
        @Override
        public HrorgsEntity createFromParcel(Parcel in) {
            return new HrorgsEntity(in);
        }

        @Override
        public HrorgsEntity[] newArray(int size) {
            return new HrorgsEntity[size];
        }
    };

    public String getOr_code() {
        return this.or_code;
    }

    public void setOr_code(String or_code) {
        this.or_code = or_code;
    }

    public String getWhichsys() {
        return this.whichsys;
    }

    public void setWhichsys(String whichsys) {
        this.whichsys = whichsys;
    }

    public int getOr_subof() {
        return this.or_subof;
    }

    public void setOr_subof(int or_subof) {
        this.or_subof = or_subof;
    }

    public String getCompany() {
        return this.company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getOr_isleaf() {
        return this.or_isleaf;
    }

    public void setOr_isleaf(int or_isleaf) {
        this.or_isleaf = or_isleaf;
    }

    public String getOr_name() {
        return this.or_name;
    }

    public void setOr_name(String or_name) {
        this.or_name = or_name;
    }

    public int getOr_id() {
        return this.or_id;
    }

    public void setOr_id(int or_id) {
        this.or_id = or_id;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }


    public String getOr_headmanname() {
        return or_headmanname;
    }

    public void setOr_headmanname(String or_headmanname) {
        this.or_headmanname = or_headmanname;
    }

    public String getOr_headmancode() {
        return or_headmancode;
    }

    public void setOr_headmancode(String or_headmancode) {
        this.or_headmancode = or_headmancode;
    }

    public int getOr_remark() {
        return or_remark;
    }

    public void setOr_remark(int or_remark) {
        this.or_remark = or_remark;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(or_code);
        parcel.writeString(whichsys);
        parcel.writeInt(or_subof);
        parcel.writeString(company);
        parcel.writeInt(or_isleaf);
        parcel.writeString(or_name);
        parcel.writeInt(or_id);
        parcel.writeString(flag);
        parcel.writeString(or_headmancode);
        parcel.writeString(or_headmanname);
        parcel.writeInt(or_remark);
    }
}