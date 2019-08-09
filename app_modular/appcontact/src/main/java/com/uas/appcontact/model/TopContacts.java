package com.uas.appcontact.model;

/**
 * Created by Bitlike on 2018/1/23.
 */

public class TopContacts {

    private String userId;
    private String ownerId;
    private String phone;
    private String name;
    private String emCode;
    private long lastTime;
    private int status;//1,常用联系人  0，联系人


    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmCode() {
        return emCode;
    }

    public void setEmCode(String emCode) {
        this.emCode = emCode;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
