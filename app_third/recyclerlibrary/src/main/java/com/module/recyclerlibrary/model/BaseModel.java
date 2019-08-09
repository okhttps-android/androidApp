package com.module.recyclerlibrary.model;

/**
 * Created by Bitliker on 2017/7/4.
 */

public class BaseModel<T> {
    private boolean select;
    private float sort;
    private String name;
    private String sub;
    private String tag;
    private T data;


    public BaseModel(boolean select, String name, String sub, String tag) {
        this.select = select;
        this.name = name;
        this.sub = sub;
        this.tag = tag;
    }

    public BaseModel(String name, String sub, String tag) {
        this.name = name;
        this.sub = sub;
        this.tag = tag;
    }

    public BaseModel(String name, String sub) {
        this.name = name;
        this.sub = sub;
    }

    public float getSort() {
        return sort;
    }

    public void setSort(float sort) {
        this.sort = sort;
    }

    public BaseModel(String name) {
        this.name = name;
    }

    public BaseModel() {
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
