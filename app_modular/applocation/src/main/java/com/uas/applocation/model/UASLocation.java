package com.uas.applocation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.baidu.mapapi.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class UASLocation implements Parcelable {
    public static final int TYPE_BAIDU = 1;
    public static final int TYPE_NATIVE = 2;

    private boolean locationOk;
    private int type;//采用定位类型
    private double latitude;//纬度
    private double longitude;//经度
    private double gpsLatitude;//原始经纬度
    private double gpsLongitude;//原始经纬度
    private String country;// 国家
    private String province;// 省份
    private String cityName;// 城市
    private String district;// 街道
    private String name;//位置信息
    private String address;//详细地址
    private String remarks;//备注

    public UASLocation(int type) {
        this.type = type;
    }


    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("locationOk", locationOk);
        map.put("type", type);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("gpsLatitude", gpsLatitude);
        map.put("gpsLongitude", gpsLongitude);
        map.put("country", country);
        map.put("province", province);
        map.put("cityName", cityName);
        map.put("district", district);
        map.put("name", name);
        map.put("address", address);
        map.put("remarks", remarks);
        return map.toString();
    }

    protected UASLocation(Parcel in) {
        locationOk = in.readInt() == 1;
        type = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        gpsLatitude = in.readDouble();
        gpsLongitude = in.readDouble();
        country = in.readString();
        province = in.readString();
        cityName = in.readString();
        district = in.readString();
        name = in.readString();
        address = in.readString();
        remarks = in.readString();
    }

    public static final Creator<UASLocation> CREATOR = new Creator<UASLocation>() {
        @Override
        public UASLocation createFromParcel(Parcel in) {
            return new UASLocation(in);
        }

        @Override
        public UASLocation[] newArray(int size) {
            return new UASLocation[size];
        }
    };

    public boolean isLocationOk() {
        return locationOk;
    }

    public void setLocationOk(boolean locationOk) {
        this.locationOk = locationOk;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getGpsLatitude() {
        return gpsLatitude==0?latitude:gpsLatitude;
    }

    public void setGpsLatitude(double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public double getGpsLongitude() {
        return gpsLongitude==0?longitude:gpsLongitude;
    }

    public void setGpsLongitude(double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(locationOk ? 1 : 0);
        parcel.writeInt(type);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeDouble(gpsLatitude);
        parcel.writeDouble(gpsLongitude);
        parcel.writeString(country);
        parcel.writeString(province);
        parcel.writeString(cityName);
        parcel.writeString(district);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(remarks);
    }

    public void clear() {
        locationOk = false;
        longitude = latitude = gpsLongitude = gpsLatitude = -1;
        remarks = name = address = district = cityName = province = country = "";
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }


    public boolean isLegalAble() {
        return latitude * longitude != 0 && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(address);
    }
}



