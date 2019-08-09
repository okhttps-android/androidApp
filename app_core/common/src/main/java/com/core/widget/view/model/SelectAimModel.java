package com.core.widget.view.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;
import com.common.data.StringUtil;

/**
 * Created by Bitliker on 2017/1/12.
 */

public class SelectAimModel implements Parcelable {
    private boolean isFirst;
    private int type;//1.空数据   2.数据库数据  3.地图数据
    private LatLng latLng;
    private String name;
    private String address;
    private String time;
    private String object;//存放json数据
    private int times;

    public SelectAimModel() {
    }


    protected SelectAimModel(Parcel in) {
        isFirst = in.readByte() != 0;
        type = in.readInt();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        name = in.readString();
        address = in.readString();
        time = in.readString();
        object = in.readString();
        times = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isFirst ? 1 : 0));
        dest.writeInt(type);
        dest.writeParcelable(latLng, flags);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(time);
        dest.writeString(object);
        dest.writeInt(times);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SelectAimModel> CREATOR = new Creator<SelectAimModel>() {
        @Override
        public SelectAimModel createFromParcel(Parcel in) {
            return new SelectAimModel(in);
        }

        @Override
        public SelectAimModel[] newArray(int size) {
            return new SelectAimModel[size];
        }
    };

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return StringUtil.isEmpty(name) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return StringUtil.isEmpty(address) ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }
}
