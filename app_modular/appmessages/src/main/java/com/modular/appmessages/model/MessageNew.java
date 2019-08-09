package com.modular.appmessages.model;

/**
 * 新UI的消息对象
 * Created by Bitlike on 2018/5/2.
 */

public class MessageNew<T> {
    private  String bindTopId;//置顶对应id
    private int type;//0.头文件  1.正常数据
    private int imageId;//头像图片(本地资源)
    private int unRedNum;//未阅读的个数
    private long lastTime;//最后一条消息时间
    private String imageUrl;//头像网址
    private String title;//标题
    private String summary;//简介
    private String remindAble;//是否显示提醒
    private String remindMessage;//提醒的信息
    private Class cazz;
    private T t;

    public String getBindTopId() {
        return bindTopId;
    }

    public void setBindTopId(String bindTopId) {
        this.bindTopId = bindTopId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getUnRedNum() {
        return unRedNum;
    }

    public void setUnRedNum(int unRedNum) {
        this.unRedNum = unRedNum;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRemindAble() {
        return remindAble;
    }

    public void setRemindAble(String remindAble) {
        this.remindAble = remindAble;
    }

    public String getRemindMessage() {
        return remindMessage;
    }

    public void setRemindMessage(String remindMessage) {
        this.remindMessage = remindMessage;
    }

    public Class getCazz() {
        return cazz;
    }

    public void setCazz(Class cazz) {
        this.cazz = cazz;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
