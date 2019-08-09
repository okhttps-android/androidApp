package com.uas.appworks.CRM.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arison on 2016/12/21.
 */
public class SimpleData implements Parcelable {
    
    private String left;
    private String right;
    private int groupId;//组ID
    private String group;//组名

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.left);
        dest.writeString(this.right);
        dest.writeInt(this.groupId);
        dest.writeString(this.group);
    }

    public SimpleData() {
    }

    protected SimpleData(Parcel in) {
        this.left = in.readString();
        this.right = in.readString();
        this.groupId = in.readInt();
        this.group = in.readString();
    }

    public static final Creator<SimpleData> CREATOR = new Creator<SimpleData>() {
        @Override
        public SimpleData createFromParcel(Parcel source) {
            return new SimpleData(source);
        }

        @Override
        public SimpleData[] newArray(int size) {
            return new SimpleData[size];
        }
    };
}
