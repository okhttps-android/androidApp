package com.uas.appworks.OA.erp.model;

import android.text.TextUtils;

import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class WorkLogs {

    private long workTimes;
    private String date;
    private String week;
    private List<Shift> shifts;
    private boolean isWorkDate;
    private int late, early;

    public WorkLogs() {
        shifts = new ArrayList<>();
    }

    public int getLate() {
        return late;
    }

    public void setLate(int late) {
        this.late = late;
    }

    public int getEarly() {
        return early;
    }

    public void setEarly(int early) {
        this.early = early;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if (!TextUtils.isEmpty(date)) {
            workTimes = DateFormatUtil.str2Long(date, DateFormatUtil.YMD);
            this.date = DateFormatUtil.long2Str(workTimes, "MM月dd日");
            this.week = CalendarUtil.getWeek(workTimes);
        }
    }

    public long getWorkTimes() {
        return workTimes;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public boolean isWorkDate() {
        return isWorkDate;
    }

    public void setWorkDate(boolean workDate) {
        isWorkDate = workDate;
    }


    public void addShift(String work, String wSign, String off, String oSign, boolean wApprecord, boolean offApprecord) {
        //只要有一个不为空就可以
        if (!TextUtils.isEmpty(work) || !TextUtils.isEmpty(wSign) || !TextUtils.isEmpty(off) || !TextUtils.isEmpty(oSign)) {
            Shift shift = new Shift();

            if (!TextUtils.isEmpty(work)) {
                shift.work = DateFormatUtil.long2Str(DateFormatUtil.str2Long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + work, DateFormatUtil.YMD_HM) + late * 60000, DateFormatUtil.HM);
            } else {
                shift.work = work;
            }

            if (!TextUtils.isEmpty(off)) {
                shift.off = DateFormatUtil.long2Str(DateFormatUtil.str2Long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + off, DateFormatUtil.YMD_HM) - early * 60000, DateFormatUtil.HM);
                LogUtil.i("gong"," shift.off="+ shift.off);
            } else {
                shift.off = off;
            }
            shift.wSign = wSign.trim();
            shift.oSign = oSign.trim();
            shift.offApprecord = offApprecord;
            shift.wApprecord = wApprecord;
            shifts.add(shift);
        }

    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public class Shift {
        public String work;
        public String wSign;
        public String off;
        public String oSign;
        public boolean offApprecord;
        public boolean wApprecord;
    }

}
