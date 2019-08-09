package com.xzjmyk.pm.activity.bean.oa;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongpm on 2016/7/26.
 */
public class MeetUserEntity implements Parcelable {
    private String imId;
    private String name;
    private String emCode;
    private String company;
    private int emId;
    private boolean isClick = false;

    public MeetUserEntity() {
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getImId() {
        return imId;
    }

    public void setImId(String imId) {
        this.imId = imId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmCode() {
        return emCode;
    }

    public void setEmCode(String emCode) {
        this.emCode = emCode;
    }

    public int getEmId() {
        return emId;
    }

    public void setEmId(int emId) {
        this.emId = emId;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    protected MeetUserEntity(Parcel in) {
        imId = in.readString();
        name = in.readString();
        emCode = in.readString();
        company = in.readString();
        emId = in.readInt();
        isClick = in.readByte() != 0;
    }

    public static final Creator<MeetUserEntity> CREATOR = new Creator<MeetUserEntity>() {
        @Override
        public MeetUserEntity createFromParcel(Parcel in) {
            return new MeetUserEntity(in);
        }

        @Override
        public MeetUserEntity[] newArray(int size) {
            return new MeetUserEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imId);
        parcel.writeString(name);
        parcel.writeString(emCode);
        parcel.writeString(company);
        parcel.writeInt(emId);
        parcel.writeByte((byte) (isClick ? 1 : 0));
    }
}
