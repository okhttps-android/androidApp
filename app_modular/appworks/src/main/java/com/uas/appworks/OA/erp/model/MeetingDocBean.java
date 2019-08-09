package com.uas.appworks.OA.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongpm on 2016/7/25.
 */
public class MeetingDocBean implements Parcelable {
    private int md_id;
    private String md_fileno;
    private String md_recorder;
    private String md_recorderdate;
    private String md_status;
    private String md_title;
    private String md_mtname;
    private String md_meetingname;
    private String md_meetingcode;
    private String md_mrcode;
    private String md_mrname;
    private String md_starttime;
    private String md_statuscode;
    private String md_endtime;
    private String md_group;
    private String md_attachs;
    private String md_contents;
    private String md_groupid;

    public MeetingDocBean() {
    }

    public int getMd_id() {
        return md_id;
    }

    public void setMd_id(int md_id) {
        this.md_id = md_id;
    }

    public String getMd_fileno() {
        return md_fileno;
    }

    public void setMd_fileno(String md_fileno) {
        this.md_fileno = md_fileno;
    }

    public String getMd_recorder() {
        return md_recorder;
    }

    public void setMd_recorder(String md_recorder) {
        this.md_recorder = md_recorder;
    }

    public String getMd_recorderdate() {
        return md_recorderdate;
    }

    public void setMd_recorderdate(String md_recorderdate) {
        this.md_recorderdate = md_recorderdate;
    }

    public String getMd_status() {
        return md_status;
    }

    public void setMd_status(String md_status) {
        this.md_status = md_status;
    }

    public String getMd_title() {
        return md_title;
    }

    public void setMd_title(String md_title) {
        this.md_title = md_title;
    }

    public String getMd_mtname() {
        return md_mtname;
    }

    public void setMd_mtname(String md_mtname) {
        this.md_mtname = md_mtname;
    }

    public String getMd_meetingname() {
        return md_meetingname;
    }

    public void setMd_meetingname(String md_meetingname) {
        this.md_meetingname = md_meetingname;
    }

    public String getMd_meetingcode() {
        return md_meetingcode;
    }

    public void setMd_meetingcode(String md_meetingcode) {
        this.md_meetingcode = md_meetingcode;
    }

    public String getMd_mrcode() {
        return md_mrcode;
    }

    public void setMd_mrcode(String md_mrcode) {
        this.md_mrcode = md_mrcode;
    }

    public String getMd_mrname() {
        return md_mrname;
    }

    public void setMd_mrname(String md_mrname) {
        this.md_mrname = md_mrname;
    }

    public String getMd_starttime() {
        return md_starttime;
    }

    public void setMd_starttime(String md_starttime) {
        this.md_starttime = md_starttime;
    }

    public String getMd_statuscode() {
        return md_statuscode;
    }

    public void setMd_statuscode(String md_statuscode) {
        this.md_statuscode = md_statuscode;
    }

    public String getMd_endtime() {
        return md_endtime;
    }

    public void setMd_endtime(String md_endtime) {
        this.md_endtime = md_endtime;
    }

    public String getMd_group() {
        return md_group;
    }

    public void setMd_group(String md_group) {
        this.md_group = md_group;
    }

    public String getMd_attachs() {
        return md_attachs;
    }

    public void setMd_attachs(String md_attachs) {
        this.md_attachs = md_attachs;
    }

    public String getMd_contents() {
        return md_contents;
    }

    public void setMd_contents(String md_contents) {
        this.md_contents = md_contents;
    }

    public String getMd_groupid() {
        return md_groupid;
    }

    public void setMd_groupid(String md_groupid) {
        this.md_groupid = md_groupid;
    }

    protected MeetingDocBean(Parcel in) {
        md_id = in.readInt();
        md_fileno = in.readString();
        md_recorder = in.readString();
        md_recorderdate = in.readString();
        md_status = in.readString();
        md_title = in.readString();
        md_mtname = in.readString();
        md_meetingname = in.readString();
        md_meetingcode = in.readString();
        md_mrcode = in.readString();
        md_mrname = in.readString();
        md_starttime = in.readString();
        md_statuscode = in.readString();
        md_endtime = in.readString();
        md_group = in.readString();
        md_attachs = in.readString();
        md_contents = in.readString();
        md_groupid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(md_id);
        dest.writeString(md_fileno);
        dest.writeString(md_recorder);
        dest.writeString(md_recorderdate);
        dest.writeString(md_status);
        dest.writeString(md_title);
        dest.writeString(md_mtname);
        dest.writeString(md_meetingname);
        dest.writeString(md_meetingcode);
        dest.writeString(md_mrcode);
        dest.writeString(md_mrname);
        dest.writeString(md_starttime);
        dest.writeString(md_statuscode);
        dest.writeString(md_endtime);
        dest.writeString(md_group);
        dest.writeString(md_attachs);
        dest.writeString(md_contents);
        dest.writeString(md_groupid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MeetingDocBean> CREATOR = new Creator<MeetingDocBean>() {
        @Override
        public MeetingDocBean createFromParcel(Parcel in) {
            return new MeetingDocBean(in);
        }

        @Override
        public MeetingDocBean[] newArray(int size) {
            return new MeetingDocBean[size];
        }
    };
}
