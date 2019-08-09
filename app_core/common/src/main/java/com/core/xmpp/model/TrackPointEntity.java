package com.core.xmpp.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by FANGlh on 2017/9/13.
 * function:
 */

public class TrackPointEntity  {
    /**
     +"latitude varchar(50)," //纬度
     +"longitude varchar(50)," //经度
     +"timestamp varchar(50)," //实时定位时的时间戳
     +"type varchar(10)," //类型 run、walk
     +"startTime varchar(50),"  //开始定位后全部默认为开始时间：yyyy-MM-dd HH:mm
     +"endTime varchar(50)"  //未点击结束前都为空 点击格式：yyyy-MM-dd HH:mm
     */
    @JSONField  // @JSONField是fastjson中的一个注解。在属性头上加上这个注解中，在对对象进行json转换时，该属性，将不会参与格式化。
    private int id; // 数据中分配id
    private double latitude;
    private double longitude;
    private String timestamp;
    private String type;
    private String startTime;
    private String endTime;

    public TrackPointEntity(int id,double latitude, double longitude, String timestamp, String type, String startTime, String endTime) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}
    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}
    public String getTimestamp() {return timestamp;}
    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public String getStartTime() {return startTime;}
    public void setStartTime(String startTime) {this.startTime = startTime;}
    public String getEndTime() {return endTime;}
    public void setEndTime(String endTime) {this.endTime = endTime;}
    @Override
    public String toString() {
        return "TrackPointEntity{" +
                "id='" + id  +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp='" + timestamp + '\'' +
                ", type='" + type + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
