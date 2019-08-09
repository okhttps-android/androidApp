package com.uas.appworks.OA.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongpm on 2016/7/25.
 */
public class MeetEntity implements Parcelable {
    private String ma_code;//编号
    private String ma_recorder;//发起人
    private String ma_starttime;//发起时间
    private String ma_endtime;//结束时间
    private String ma_theme;//主题
    private String ma_remark;
    private String ma_mrname;//会议地点
    private String ma_mrcode;//会议地点
    private int ma_id;
    private String ma_tag;
    private String status;
    private String ma_stage;
    private String ma_type;

    public MeetEntity() {
    }

    public String getMa_code() {
        return ma_code;
    }

    public void setMa_code(String ma_code) {
        this.ma_code = ma_code;
    }

    public String getMa_recorder() {
        return ma_recorder;
    }

    public void setMa_recorder(String ma_recorder) {
        this.ma_recorder = ma_recorder;
    }

    public String getMa_starttime() {
        return ma_starttime;
    }

    public void setMa_starttime(String ma_starttime) {
        this.ma_starttime = ma_starttime;
    }

    public String getMa_endtime() {
        return ma_endtime;
    }

    public void setMa_endtime(String ma_endtime) {
        this.ma_endtime = ma_endtime;
    }

    public String getMa_theme() {
        return ma_theme;
    }

    public void setMa_theme(String ma_theme) {
        this.ma_theme = ma_theme;
    }

    public String getMa_remark() {
        return ma_remark;
    }

    public void setMa_remark(String ma_remark) {
        this.ma_remark = ma_remark;
    }

    public String getMa_mrname() {
        return ma_mrname;
    }

    public void setMa_mrname(String ma_mrname) {
        this.ma_mrname = ma_mrname;
    }

    public String getMa_mrcode() {
        return ma_mrcode;
    }

    public void setMa_mrcode(String ma_mrcode) {
        this.ma_mrcode = ma_mrcode;
    }

    public int getMa_id() {
        return ma_id;
    }

    public void setMa_id(int ma_id) {
        this.ma_id = ma_id;
    }

    public String getMa_tag() {
        return ma_tag;
    }

    public void setMa_tag(String ma_tag) {
        this.ma_tag = ma_tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMa_stage() {
        return ma_stage;
    }

    public void setMa_stage(String ma_stage) {
        this.ma_stage = ma_stage;
    }

    public String getMa_type() {
        return ma_type;
    }

    public void setMa_type(String ma_type) {
        this.ma_type = ma_type;
    }

    protected MeetEntity(Parcel in) {
        ma_code = in.readString();
        ma_recorder = in.readString();
        ma_starttime = in.readString();
        ma_endtime = in.readString();
        ma_theme = in.readString();
        ma_remark = in.readString();
        ma_mrname = in.readString();
        ma_mrcode = in.readString();
        ma_id = in.readInt();
        ma_tag = in.readString();
        status = in.readString();
        ma_stage = in.readString();
        ma_type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ma_code);
        dest.writeString(ma_recorder);
        dest.writeString(ma_starttime);
        dest.writeString(ma_endtime);
        dest.writeString(ma_theme);
        dest.writeString(ma_remark);
        dest.writeString(ma_mrname);
        dest.writeString(ma_mrcode);
        dest.writeInt(ma_id);
        dest.writeString(ma_tag);
        dest.writeString(status);
        dest.writeString(ma_stage);
        dest.writeString(ma_type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MeetEntity> CREATOR = new Creator<MeetEntity>() {
        @Override
        public MeetEntity createFromParcel(Parcel in) {
            return new MeetEntity(in);
        }

        @Override
        public MeetEntity[] newArray(int size) {
            return new MeetEntity[size];
        }
    };
}
