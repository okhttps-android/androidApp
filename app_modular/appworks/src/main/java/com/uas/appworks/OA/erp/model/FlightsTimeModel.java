package com.uas.appworks.OA.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bitliker on 2017/1/20.
 */

public class FlightsTimeModel implements Parcelable {

    private String wd_ondutyone;//    开始时间1
    private String wd_offdutyone;  //   结束时间1
    private String wd_ondutytwo;  //  开始时间2
    private String wd_offdutytwo;  //   结束时间2
    private String wd_ondutythree;   //  开始时间3
    private String wd_offdutythree;  //   结束时间3


    private int earlyTime;//最早时间  小时

    public FlightsTimeModel() {

    }


    protected FlightsTimeModel(Parcel in) {
        wd_ondutyone = in.readString();
        wd_offdutyone = in.readString();
        wd_ondutytwo = in.readString();
        wd_offdutytwo = in.readString();
        wd_ondutythree = in.readString();
        wd_offdutythree = in.readString();
        earlyTime = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(wd_ondutyone);
        dest.writeString(wd_offdutyone);
        dest.writeString(wd_ondutytwo);
        dest.writeString(wd_offdutytwo);
        dest.writeString(wd_ondutythree);
        dest.writeString(wd_offdutythree);
        dest.writeInt(earlyTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FlightsTimeModel> CREATOR = new Creator<FlightsTimeModel>() {
        @Override
        public FlightsTimeModel createFromParcel(Parcel in) {
            return new FlightsTimeModel(in);
        }

        @Override
        public FlightsTimeModel[] newArray(int size) {
            return new FlightsTimeModel[size];
        }
    };

    public String getWd_ondutyone() {
        return wd_ondutyone;
    }

    public void setWd_ondutyone(String wd_ondutyone) {
        this.wd_ondutyone = wd_ondutyone;
    }

    public String getWd_offdutyone() {
        return wd_offdutyone;
    }

    public void setWd_offdutyone(String wd_offdutyone) {
        this.wd_offdutyone = wd_offdutyone;
    }

    public String getWd_ondutytwo() {
        return wd_ondutytwo;
    }

    public void setWd_ondutytwo(String wd_ondutytwo) {
        this.wd_ondutytwo = wd_ondutytwo;
    }

    public String getWd_offdutytwo() {
        return wd_offdutytwo;
    }

    public void setWd_offdutytwo(String wd_offdutytwo) {
        this.wd_offdutytwo = wd_offdutytwo;
    }

    public String getWd_ondutythree() {
        return wd_ondutythree;
    }

    public void setWd_ondutythree(String wd_ondutythree) {
        this.wd_ondutythree = wd_ondutythree;
    }

    public String getWd_offdutythree() {
        return wd_offdutythree;
    }

    public void setWd_offdutythree(String wd_offdutythree) {
        this.wd_offdutythree = wd_offdutythree;
    }



    public int getEarlyTime() {
        return earlyTime;
    }



    public void setEarlyTime(int earlyTime) {
        this.earlyTime = earlyTime;
    }



}
