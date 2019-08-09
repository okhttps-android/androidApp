package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Employees implements Parcelable {
    private int em_id;
    private String em_code;
    private String em_name;
    private String em_depart;
    private String em_position;
    private String em_defaultorname;
    private boolean isClick=false;
    public Employees(){}

    public int getEm_id() {
        return em_id;
    }

    public void setEm_id(int em_id) {
        this.em_id = em_id;
    }

    public String getEm_code() {
        return em_code;
    }

    public void setEm_code(String em_code) {
        this.em_code = em_code;
    }

    public String getEm_name() {
        return em_name;
    }

    public void setEm_name(String em_name) {
        this.em_name = em_name;
    }

    public String getEm_depart() {
        return em_depart;
    }

    public void setEm_depart(String em_depart) {
        this.em_depart = em_depart;
    }

    public String getEm_position() {
        return em_position;
    }

    public void setEm_position(String em_position) {
        this.em_position = em_position;
    }

    public String getEm_defaultorname() {
        return em_defaultorname;
    }

    public void setEm_defaultorname(String em_defaultorname) {
        this.em_defaultorname = em_defaultorname;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    protected Employees(Parcel in) {
        em_id = in.readInt();
        em_code = in.readString();
        em_name = in.readString();
        em_depart = in.readString();
        em_position = in.readString();
        em_defaultorname = in.readString();
        isClick = in.readByte() != 0;
    }

    public static final Creator<Employees> CREATOR = new Creator<Employees>() {
        @Override
        public Employees createFromParcel(Parcel in) {
            return new Employees(in);
        }

        @Override
        public Employees[] newArray(int size) {
            return new Employees[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(em_id);
        parcel.writeString(em_code);
        parcel.writeString(em_name);
        parcel.writeString(em_depart);
        parcel.writeString(em_position);
        parcel.writeString(em_defaultorname);
        parcel.writeByte((byte) (isClick ? 1 : 0));
    }
}
