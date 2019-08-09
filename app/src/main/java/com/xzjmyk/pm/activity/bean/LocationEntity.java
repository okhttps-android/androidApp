package com.xzjmyk.pm.activity.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bitliker on 2016/12/23.
 */

public class LocationEntity implements Parcelable {
    private boolean locationOk;//是否定位成功
    private double longitude;//经度
    private double latitude;//纬度
    private String location;//位置信息
    private String address;//详细地址
    private String province;// 省份
    private String cityName;// 城市
    private String district;// 街道
    private String errorMessage;//定位错误信息

    public LocationEntity() {
    }

    public void clear() {
        locationOk = false;//是否定位成功
        longitude = 0;//经度
        latitude = 0;//纬度
        location = "";//位置信息
        address = "";//详细地址
        province = "";// 省份
        cityName = "";// 城市
        district = "";// 街道
        errorMessage = "";//定位错
    }

    protected LocationEntity(Parcel in) {
        locationOk = in.readByte() != 0;
        longitude = in.readDouble();
        latitude = in.readDouble();
        location = in.readString();
        address = in.readString();
        province = in.readString();
        cityName = in.readString();
        district = in.readString();
        errorMessage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (locationOk ? 1 : 0));
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(location);
        dest.writeString(address);
        dest.writeString(province);
        dest.writeString(cityName);
        dest.writeString(district);
        dest.writeString(errorMessage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationEntity> CREATOR = new Creator<LocationEntity>() {
        @Override
        public LocationEntity createFromParcel(Parcel in) {
            return new LocationEntity(in);
        }

        @Override
        public LocationEntity[] newArray(int size) {
            return new LocationEntity[size];
        }
    };

    public boolean isLocationOk() {
        return locationOk;
    }

    public void setLocationOk(boolean locationOk) {
        this.locationOk = locationOk;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
