package com.modular.apputils.model;

import android.support.annotation.DrawableRes;

public class EasyBaseModel<T> {
    private int id;
    private String title;
    private String subTitle;
    private @DrawableRes int iconId;
    private String iconUrl;
    private T data;

    public int getId() {
        return id;
    }

    public  EasyBaseModel<T> setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public  EasyBaseModel<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public  EasyBaseModel<T> setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public int getIconId() {
        return iconId;
    }

    public  EasyBaseModel<T> setIconId(int iconId) {
        this.iconId = iconId;
        return this;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public  EasyBaseModel<T> setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public T getData() {
        return data;
    }

    public  EasyBaseModel<T> setData(T data) {
        this.data = data;
        return this;
    }
}
