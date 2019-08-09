package com.modular.appmessages.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bitliker on 2016/11/21.
 */

public class SubMessage implements Parcelable {
    private boolean isRead = false;
    private int id;//Id   连接时候使用
    private String title;//主标题
    private String subTitle;//副标题
    private int status;//0 时间,1 头,2 副,3空文件
    private String date;//yyyy-mm-dd
    private long createTime;//创建时间  与date关联
    private int numId;//numid   连接时候使用
    private int instanceId;//instanceId   连接时候使用


    public SubMessage() {
    }


    protected SubMessage(Parcel in) {
        isRead = in.readByte() != 0;
        id = in.readInt();
        title = in.readString();
        subTitle = in.readString();
        status = in.readInt();
        date = in.readString();
        createTime = in.readLong();
        numId = in.readInt();
        instanceId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(subTitle);
        dest.writeInt(status);
        dest.writeString(date);
        dest.writeLong(createTime);
        dest.writeInt(numId);
        dest.writeInt(instanceId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubMessage> CREATOR = new Creator<SubMessage>() {
        @Override
        public SubMessage createFromParcel(Parcel in) {
            return new SubMessage(in);
        }

        @Override
        public SubMessage[] newArray(int size) {
            return new SubMessage[size];
        }
    };

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getNumId() {
        return numId;
    }

    public void setNumId(int numId) {
        this.numId = numId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }
}
