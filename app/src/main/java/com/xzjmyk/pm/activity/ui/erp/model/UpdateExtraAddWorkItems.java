package com.xzjmyk.pm.activity.ui.erp.model;

public class UpdateExtraAddWorkItems {
    public String wod_startdate;//起始日期
    public String wod_enddate;//截止日期
    public double wod_count;
    public int wod_id;

    public int getWod_id() {
        return wod_id;
    }

    public void setWod_id(int wod_id) {
        this.wod_id = wod_id;
    }

    public double getWod_count() {
        return wod_count;
    }

    public void setWod_count(double wod_count) {
        this.wod_count = wod_count;
    }

    public String getWod_startdate() {
        return wod_startdate;
    }

    public void setWod_startdate(String wod_startdate) {
        this.wod_startdate = wod_startdate;
    }

    public String getWod_enddate() {
        return wod_enddate;
    }

    public void setWod_enddate(String wod_enddate) {
        this.wod_enddate = wod_enddate;
    }

}