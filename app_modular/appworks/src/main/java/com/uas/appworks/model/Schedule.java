package com.uas.appworks.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Schedule implements Parcelable {
    public static final String TYPE_PHONE = "手机日程";
    public static final String TYPE_UU = "UU互联";
    public static final String TYPE_BOOK = "预约";

    private int id;
    private String type;//类型 （UU互联||手机日程|会议）
    private int allDay;//是否全天 （ 1.全天  0.非全天 ）
    private String repeat;//重复类型（不重复、每天重复、每周重复、每月重复）
    private String title;//标题
    private String tag;//标签（工作|会议|学习）
    private String remarks;//备注
    private long startTime;//开始时间戳
    private long endTime;//结束时间戳
    private int warnTime;  //提醒时间(单位 分钟:  如:12==12分钟   负数为不提醒)
    private long warnRealTime;//提醒精确时间（预留）
    private String address;//地点（预留）
    private String status;//状态（预留）
    private String details;//单据详情


    public Schedule(boolean formUU) {
        type = formUU ? TYPE_UU : TYPE_PHONE;
    }

    public Schedule(String type) {
        this.type = type;
    }

    protected Schedule(Parcel in) {
        id = in.readInt();
        type = in.readString();
        allDay = in.readInt();
        repeat = in.readString();
        title = in.readString();
        tag = in.readString();
        remarks = in.readString();
        startTime = in.readLong();
        endTime = in.readLong();
        warnTime = in.readInt();
        warnRealTime = in.readLong();
        address = in.readString();
        status = in.readString();
        details = in.readString();
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAllDay() {
        return allDay;
    }

    public void setAllDay(int allDay) {
        this.allDay = allDay;
    }

    public String getRepeat() {
        return repeat == null ? "" : repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag == null ? "" : tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRemarks() {
        return remarks == null ? "" : remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(int warnTime) {
        this.warnTime = warnTime;
    }

    public long getWarnRealTime() {
        return warnRealTime;
    }

    public void setWarnRealTime(long warnRealTime) {
        this.warnRealTime = warnRealTime;
    }

    public String getAddress() {
        return address == null ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int hasAlarm() {
        return warnTime > 0 ? 1 : 0;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(type);
        parcel.writeInt(allDay);
        parcel.writeString(repeat);
        parcel.writeString(title);
        parcel.writeString(tag);
        parcel.writeString(remarks);
        parcel.writeLong(startTime);
        parcel.writeLong(endTime);
        parcel.writeInt(warnTime);
        parcel.writeLong(warnRealTime);
        parcel.writeString(address);
        parcel.writeString(status);
        parcel.writeString(details);
    }
}
