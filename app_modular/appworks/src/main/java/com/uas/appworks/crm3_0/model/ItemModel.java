package com.uas.appworks.crm3_0.model;

/**
 * Created by Arison on 2018/9/28.
 */

public class ItemModel{

    String key;
    String value;
    String id;
    String groupId;
    ColumnModel columnModel;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ColumnModel getColumnModel() {
        return columnModel;
    }

    public void setColumnModel(ColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}