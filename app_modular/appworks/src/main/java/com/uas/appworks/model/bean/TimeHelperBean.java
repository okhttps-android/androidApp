package com.uas.appworks.model.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.common.data.DateFormatUtil;
import com.uas.appworks.model.Schedule;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/8/23 11:26
 */
public class TimeHelperBean implements Comparable<TimeHelperBean>, Parcelable {
    public static final int TYPE_TIME_HELPER_ORDER = 1;
    public static final int TYPE_TIME_HELPER_SCHEDULE = 2;
    public static final int TYPE_TIME_HELPER_MEETING = 3;
    public static final int TYPE_TIME_HELPER_OUTWORK = 4;
    public static final int TYPE_TIME_HELPER_TRIP = 5;


    /**
     * scheduleId : 7
     * imid : 110462
     * type : UU互联
     * allDay : 1
     * repeat : 不重复
     * title : 工作
     * tag : 工作
     * remarks : 工作
     * startTime : 2018-08-27 15:37:06
     * endTime : 2018-08-27 15:37:22
     * warnTime : 1
     * warnRealTime : 2018-08-27 15:37:39
     * address : null
     * status : 1
     */

    private String fromWhere = Schedule.TYPE_UU;
    private int scheduleId;
    private int imid;
    private String type;
    private int allDay;
    private String repeat;
    private String title;
    private String tag;
    private String remarks;
    private String startTime;
    private String endTime;
    private int warnTime;
    private String warnRealTime;
    private String address;
    private String detail;
    private int status;
    private int scheduleType;

    public TimeHelperBean() {

    }

    protected TimeHelperBean(Parcel in) {
        fromWhere = in.readString();
        scheduleId = in.readInt();
        imid = in.readInt();
        type = in.readString();
        allDay = in.readInt();
        repeat = in.readString();
        title = in.readString();
        tag = in.readString();
        remarks = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        warnTime = in.readInt();
        warnRealTime = in.readString();
        address = in.readString();
        detail = in.readString();
        status = in.readInt();
        scheduleType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fromWhere);
        dest.writeInt(scheduleId);
        dest.writeInt(imid);
        dest.writeString(type);
        dest.writeInt(allDay);
        dest.writeString(repeat);
        dest.writeString(title);
        dest.writeString(tag);
        dest.writeString(remarks);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeInt(warnTime);
        dest.writeString(warnRealTime);
        dest.writeString(address);
        dest.writeString(detail);
        dest.writeInt(status);
        dest.writeInt(scheduleType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimeHelperBean> CREATOR = new Creator<TimeHelperBean>() {
        @Override
        public TimeHelperBean createFromParcel(Parcel in) {
            return new TimeHelperBean(in);
        }

        @Override
        public TimeHelperBean[] newArray(int size) {
            return new TimeHelperBean[size];
        }
    };

    public String getFromWhere() {
        return fromWhere;
    }

    public void setFromWhere(String fromWhere) {
        this.fromWhere = fromWhere;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getImid() {
        return imid;
    }

    public void setImid(int imid) {
        this.imid = imid;
    }

    public String getType() {
        return type;
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
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(int warnTime) {
        this.warnTime = warnTime;
    }

    public String getWarnRealTime() {
        return warnRealTime;
    }

    public void setWarnRealTime(String warnRealTime) {
        this.warnRealTime = warnRealTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(int scheduleType) {
        this.scheduleType = scheduleType;
    }

    @Override
    public int compareTo(@NonNull TimeHelperBean timeHelperBean) {
        String thisStartTime = this.getStartTime();
        String otherStartTime = timeHelperBean.getStartTime();
        long result = DateFormatUtil.str2Long(thisStartTime, DateFormatUtil.YMD_HMS)
                - DateFormatUtil.str2Long(otherStartTime, DateFormatUtil.YMD_HMS);
        return result >= 0 ? 1 : -1;
    }
}
