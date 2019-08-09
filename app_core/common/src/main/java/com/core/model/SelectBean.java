package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by pengminggong on 2016/9/29.
 */
public class SelectBean implements Parcelable {


    private int id;//数字索引
    private int index;//数字索引
    private String name;//选项索引
    private String showName;//选项索引
    private String fields;//联动字段
    private boolean isClick = false;//是否已被选择（默认非选择）
    private String object;//选择的内容详细信息 T
    private String json;//方式时候返回选择的jsonObject
    private String dbfinds;

    public String getDbfinds() {
        return dbfinds;
    }

    public void setDbfinds(String dbfinds) {
        this.dbfinds = dbfinds;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowName() {
        return TextUtils.isEmpty(showName) ? name : showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean isClick) {
        this.isClick = isClick;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.index);
        dest.writeString(this.name);
        dest.writeString(this.showName);
        dest.writeString(this.fields);
        dest.writeByte(this.isClick ? (byte) 1 : (byte) 0);
        dest.writeString(this.object);
        dest.writeString(this.json);
        dest.writeString(this.dbfinds);
    }

    public SelectBean() {
    }

    public SelectBean(String name) {
        this.name = name;
    }

    protected SelectBean(Parcel in) {
        this.id = in.readInt();
        this.index = in.readInt();
        this.name = in.readString();
        this.showName = in.readString();
        this.fields = in.readString();
        this.isClick = in.readByte() != 0;
        this.object = in.readString();
        this.json = in.readString();
        this.dbfinds = in.readString();
    }

    public static final Creator<SelectBean> CREATOR = new Creator<SelectBean>() {
        @Override
        public SelectBean createFromParcel(Parcel source) {
            return new SelectBean(source);
        }

        @Override
        public SelectBean[] newArray(int size) {
            return new SelectBean[size];
        }
    };
}
