package com.xzjmyk.pm.activity.ui.platform.pageforms;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arison on 2017/3/7.
 */
public class PagesModel implements Parcelable {
    
    private String state;
    private String startTime;
    private String endTime;
    private String status;
    private String id;
    private String code;
    private String modelType;
    private String modeJson;//存放当前json数据


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.state);
        dest.writeString(this.startTime);
        dest.writeString(this.endTime);
        dest.writeString(this.status);
        dest.writeString(this.id);
        dest.writeString(this.code);
        dest.writeString(this.modelType);
        dest.writeString(this.modeJson);
    }

    public PagesModel() {
    }

    protected PagesModel(Parcel in) {
        this.state = in.readString();
        this.startTime = in.readString();
        this.endTime = in.readString();
        this.status = in.readString();
        this.id = in.readString();
        this.code = in.readString();
        this.modelType = in.readString();
        this.modeJson = in.readString();
    }

    public static final Creator<PagesModel> CREATOR = new Creator<PagesModel>() {
        @Override
        public PagesModel createFromParcel(Parcel source) {
            return new PagesModel(source);
        }

        @Override
        public PagesModel[] newArray(int size) {
            return new PagesModel[size];
        }
    };

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModeJson() {
        return modeJson;
    }

    public void setModeJson(String modeJson) {
        this.modeJson = modeJson;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
