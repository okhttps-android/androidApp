package com.core.model;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by Bitliker on 2017/2/9.
 */

public class WorkLocationModel {

    private int id;
    private int validrange;//打卡距离
    private LatLng location;
    private String shortName;//位置
    private String workaddr;//位置//地址

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValidrange() {
        return validrange;
    }

    public void setValidrange(int validrange) {
        this.validrange = validrange;
    }


    public double getLatitude() {
        return location==null?0:location.latitude;
    }



    public double getLongitude() {
        return location==null?0:location.longitude;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getWorkaddr() {
        return workaddr;
    }

    public void setWorkaddr(String workaddr) {
        this.workaddr = workaddr;
    }
}
