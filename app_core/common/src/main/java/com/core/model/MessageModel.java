package com.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.common.data.StringUtil;

/**
 * 消息数据库实体类
 * Created by Bitliker on 2017/3/2.
 */

public class MessageModel implements Parcelable {
    private int id;
    private int hierarchy;//层级
    private int count;
    private String title;
    private String subTitle;
    private String time;
    private String lastTime;
    private String type;
    private boolean isReaded;
    private int readStatus;//阅读状态
    private String readTime;

    private String caller;
    private int keyValue;

    public MessageModel() {
    }

    public MessageModel(int id, int hierarchy, int count, String title, String subTitle, String time, String type, boolean isReaded, int readStatus, String readTime) {
        this.id = id;
        this.hierarchy = hierarchy;
        this.count = count;
        this.title = title;
        this.subTitle = subTitle;
        this.time = time;
        this.type = type;
        this.isReaded = isReaded;
        this.readStatus = readStatus;
        this.readTime = readTime;
    }

    protected MessageModel(Parcel in) {
        id = in.readInt();
        hierarchy = in.readInt();
        count = in.readInt();
        title = in.readString();
        subTitle = in.readString();
        time = in.readString();
        lastTime = in.readString();
        type = in.readString();
        isReaded = in.readByte() != 0;
        readStatus = in.readInt();
        readTime = in.readString();
        caller = in.readString();
        keyValue = in.readInt();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(hierarchy);
        dest.writeInt(count);
        dest.writeString(title);
        dest.writeString(subTitle);
        dest.writeString(time);
        dest.writeString(lastTime);
        dest.writeString(type);
        dest.writeByte((byte) (isReaded ? 1 : 0));
        dest.writeInt(readStatus);
        dest.writeString(readTime);
        dest.writeString(caller);
        dest.writeInt(keyValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MessageModel> CREATOR = new Creator<MessageModel>() {
        @Override
        public MessageModel createFromParcel(Parcel in) {
            return new MessageModel(in);
        }

        @Override
        public MessageModel[] newArray(int size) {
            return new MessageModel[size];
        }
    };


    public int getId() {
        if (id == 0) {
            return getFirstFloorId();
        }
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReaded() {
        return isReaded;
    }

    public void setReaded(boolean readed) {
        isReaded = readed;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public int getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(int keyValue) {
        this.keyValue = keyValue;
    }

    /**
     * 1crm/CRM提醒
     * 2note/通知公告
     * 3kpi/考勤提醒
     * 4meeting/会议提醒
     * 5process/审批知会
     * 6task/任务提醒
     * 7job/稽核提醒
     * 8b2b/b2b提醒
     * 9system/知会消息
     * 10common/普通知会
     *
     * @return
     */

    public int getFirstFloorId() {
        if (StringUtil.isEmpty(type)) {
            return (int) ((Math.random() * 10) * 30);
        }
        int firstFloorId = 0;
        if (type.contentEquals("crm")) {
            firstFloorId = 1;
        } else if (type.contentEquals("note"))
            firstFloorId = 2;
        else if (type.contentEquals("kpi"))//考勤提醒
            firstFloorId = 3;
        else if (type.contentEquals("meeting"))
            firstFloorId = 4;
        else if (type.contentEquals("process"))
            firstFloorId = 5;
        else if (type.contentEquals("task"))
            firstFloorId = 6;
        else if (type.contentEquals("job"))
            firstFloorId = 7;
        else if (type.contentEquals("b2b"))
            firstFloorId = 8;
        else if (type.contentEquals("system"))
            firstFloorId = 9;
        else if (type.contentEquals("common"))
            firstFloorId = 10;

        return firstFloorId;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }
}
