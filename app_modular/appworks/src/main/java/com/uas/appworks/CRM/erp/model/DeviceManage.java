package com.uas.appworks.CRM.erp.model;

import android.os.Bundle;

/**
 * Created by Bitlike on 2017/11/22.
 */

public class DeviceManage {
    private int id;
    private int reId;
    private String name;
    private Class cazz;
    private Bundle bundle;
    private int request;


    public int getRequest() {
        return request;
    }

    public DeviceManage setRequest(int request) {
        this.request = request;
        return this;
    }

    public DeviceManage(int id, int reId, String name) {
        this.id = id;
        this.reId = reId;
        this.name = name;
    }

    public DeviceManage(int reId, String name) {
        this.reId = reId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReId() {
        return reId;
    }

    public void setReId(int reId) {
        this.reId = reId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getCazz() {
        return cazz;
    }

    public DeviceManage setCazz(Class cazz) {
        this.cazz = cazz;
        return this;
    }

    public Bundle getBundle() {
        return bundle;
    }


    public DeviceManage setBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public DeviceManage addString(String key, String values) {
        if (this.bundle == null) {
            this.bundle = new Bundle();
        }
        this.bundle.putString(key, values);
        return this;
    }
}
