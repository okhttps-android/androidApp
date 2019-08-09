package com.uas.appworks.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.common.data.JSONUtil;

/**
 * 客户绑定的子单据对象
 */
public class CustomerBindBill implements Parcelable {
    private int id;
    private String name;
    private String subName;
    private String address;
    private String status;
    private String date;
    private float longitude;
    private float latitude;

    private String type;
    private String code;
    private String domancode;

    public CustomerBindBill() {
    }

    protected CustomerBindBill(Parcel in) {
        id = in.readInt();
        name = in.readString();
        subName = in.readString();
        address = in.readString();
        status = in.readString();
        type = in.readString();
        code = in.readString();
        domancode = in.readString();
        date = in.readString();
        longitude = in.readFloat();
        latitude = in.readFloat();
    }

    public static final Creator<CustomerBindBill> CREATOR = new Creator<CustomerBindBill>() {
        @Override
        public CustomerBindBill createFromParcel(Parcel in) {
            return new CustomerBindBill(in);
        }

        @Override
        public CustomerBindBill[] newArray(int size) {
            return new CustomerBindBill[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDomancode() {
        return domancode;
    }

    public void setDomancode(String domancode) {
        this.domancode = domancode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(subName);
        parcel.writeString(address);
        parcel.writeString(status);
        parcel.writeString(type);
        parcel.writeString(code);
        parcel.writeString(domancode);
        parcel.writeString(date);
        parcel.writeFloat(longitude);
        parcel.writeFloat(latitude);
    }
}
