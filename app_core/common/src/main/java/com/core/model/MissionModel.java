package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by Bitliker on 2016/11/24.
 */
public class MissionModel implements Parcelable {
    private int id;    //id
    private String code;//编号（非人员编号）
    private String companyName;//公司名字
    private String companyAddr;//公司地址
    private String date;//日期  yyyy-MM-dd
    private String visitTime;//预计到达时间  yyyy-MM-dd HH:mm:ss
    private String realTime;//实际到达时间  yyyy-MM-dd HH:mm:ss
    private String realLeave;//实际离开时间  yyyy-MM-dd HH:mm:ss
    private LatLng latLng;//选择地址的经纬度
    private int visitcount;//拜访次数
    private String remark;//备注
    private int  type;//类型    1.半天   2.一天
    private int status;//状态 0.初始化（可提交）   (不可提交==》)1.已提交  2.签退  3.再签到 4.商机传过来的

    //update by 2016/12/19
    private double distance;
    private String location;
    private String recorddate;


    public MissionModel() {
    }


    protected MissionModel(Parcel in) {
        id = in.readInt();
        code = in.readString();
        companyName = in.readString();
        companyAddr = in.readString();
        date = in.readString();
        visitTime = in.readString();
        realTime = in.readString();
        realLeave = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        visitcount = in.readInt();
        remark = in.readString();
        type = in.readInt();
        status = in.readInt();
        distance = in.readDouble();
        location = in.readString();
        recorddate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(code);
        dest.writeString(companyName);
        dest.writeString(companyAddr);
        dest.writeString(date);
        dest.writeString(visitTime);
        dest.writeString(realTime);
        dest.writeString(realLeave);
        dest.writeParcelable(latLng, flags);
        dest.writeInt(visitcount);
        dest.writeString(remark);
        dest.writeInt(type);
        dest.writeInt(status);
        dest.writeDouble(distance);
        dest.writeString(location);
        dest.writeString(recorddate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MissionModel> CREATOR = new Creator<MissionModel>() {
        @Override
        public MissionModel createFromParcel(Parcel in) {
            return new MissionModel(in);
        }

        @Override
        public MissionModel[] newArray(int size) {
            return new MissionModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddr() {
        return companyAddr;
    }

    public void setCompanyAddr(String companyAddr) {
        this.companyAddr = companyAddr;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public String getRealTime() {
        return realTime;
    }

    public void setRealTime(String realTime) {
        this.realTime = realTime;
    }

    public String getRealLeave() {
        return realLeave;
    }

    public void setRealLeave(String realLeave) {
        this.realLeave = realLeave;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getVisitcount() {
        return visitcount;
    }

    public void setVisitcount(int visitcount) {
        this.visitcount = visitcount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getType() {
        return type;
    }

    public void setType(int tyep) {
        this.type = tyep;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRecorddate() {
        return recorddate;
    }

    public void setRecorddate(String recorddate) {
        this.recorddate = recorddate;
    }
}
