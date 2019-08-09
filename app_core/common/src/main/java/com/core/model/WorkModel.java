package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSON;

/**
 * 班次的模型,
 * Created by Bitliker on 2016/12/12.
 */
public class WorkModel implements Parcelable {
    private int id;//编号
    private String workStart;//上班开始时间
    private String workTime;//上班时间
    private String workend; //上班计算结束时间
    private String workSignin; //上班签到时间
    private String workAllegedly; //上班申诉时间
    private boolean workAlarm;//上班提醒


    private String offStart;//下班计算开始时间
    private String offTime;//下班时间
    private String offend;//下班结束时间
    private String offSignin;//下班签到时间
    private String offAllegedly;//下班申诉时间
    private boolean offAlarm;//下班提醒

    private boolean leaveAlarm;//离开提醒
    //判断是否隔天的班次
    private boolean isNextDay = false;

    public WorkModel() {

    }

    public WorkModel(int id, String workStart, String workTime,
                     String workend, String workSignin,
                     String workAllegedly,
                     boolean workAlarm,
                     String offStart,
                     String offTime,
                     String offend,
                     String offSignin,
                     String offAllegedly,
                     boolean offAlarm,
                     boolean leaveAlarm,boolean isNextDay
    ) {
        this.id = id;
        this.workStart = workStart;
        this.workTime = workTime;
        this.workend = workend;
        this.workSignin = workSignin;
        this.workAllegedly = workAllegedly;
        this.workAlarm = workAlarm;
        this.offStart = offStart;
        this.offTime = offTime;
        this.offend = offend;
        this.offSignin = offSignin;
        this.offAllegedly = offAllegedly;
        this.offAlarm = offAlarm;
        this.leaveAlarm = leaveAlarm;
        this.isNextDay = isNextDay;
    }

    protected WorkModel(Parcel in) {
        id = in.readInt();
        workStart = in.readString();
        workTime = in.readString();
        workend = in.readString();
        workSignin = in.readString();
        workAllegedly = in.readString();
        workAlarm = in.readByte() != 0;
        offStart = in.readString();
        offTime = in.readString();
        offend = in.readString();
        offSignin = in.readString();
        offAllegedly = in.readString();
        offAlarm = in.readByte() != 0;
        leaveAlarm = in.readByte() != 0;
        isNextDay = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(workStart);
        dest.writeString(workTime);
        dest.writeString(workend);
        dest.writeString(workSignin);
        dest.writeString(workAllegedly);
        dest.writeByte((byte) (workAlarm ? 1 : 0));
        dest.writeString(offStart);
        dest.writeString(offTime);
        dest.writeString(offend);
        dest.writeString(offSignin);
        dest.writeString(offAllegedly);
        dest.writeByte((byte) (offAlarm ? 1 : 0));
        dest.writeByte((byte) (leaveAlarm ? 1 : 0));
        dest.writeByte((byte) (isNextDay ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WorkModel> CREATOR = new Creator<WorkModel>() {
        @Override
        public WorkModel createFromParcel(Parcel in) {
            return new WorkModel(in);
        }

        @Override
        public WorkModel[] newArray(int size) {
            return new WorkModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int noId) {
        this.id = noId;
    }

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkTime() {
        return workTime==null?"":workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }

    public String getWorkend() {
        return workend;
    }

    public void setWorkend(String workend) {
        this.workend = workend;
    }

    public String getWorkSignin() {
        return workSignin;
    }

    public void setWorkSignin(String workSignin) {
        this.workSignin = workSignin;
    }

    public String getWorkAllegedly() {
        return workAllegedly;
    }

    public void setWorkAllegedly(String workAllegedly) {
        this.workAllegedly = workAllegedly;
    }

    public boolean isWorkAlarm() {
        return workAlarm;
    }

    public void setWorkAlarm(boolean workAlarm) {
        this.workAlarm = workAlarm;
    }

    public String getOffStart() {
        return offStart;
    }

    public void setOffStart(String offStart) {
        this.offStart = offStart;
    }

    public String getOffTime() {
        return offTime;
    }

    public void setOffTime(String offTime) {
        this.offTime = offTime;
    }

    public String getOffend() {
        return offend;
    }

    public void setOffend(String offend) {
        this.offend = offend;
    }

    public String getOffSignin() {
        return offSignin;
    }

    public void setOffSignin(String offSignin) {
        this.offSignin = offSignin;
    }

    public String getOffAllegedly() {
        return offAllegedly;
    }

    public void setOffAllegedly(String offAllegedly) {
        this.offAllegedly = offAllegedly;
    }

    public boolean isOffAlarm() {
        return offAlarm;
    }

    public void setOffAlarm(boolean offAlarm) {
        this.offAlarm = offAlarm;
    }

    public boolean isLeaveAlarm() {
        return leaveAlarm;
    }

    public void setLeaveAlarm(boolean leaveAlarm) {
        this.leaveAlarm = leaveAlarm;
    }

    public boolean isNextDay() {
        return isNextDay;
    }

    public void setNextDay(boolean nextDay) {
        isNextDay = nextDay;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
