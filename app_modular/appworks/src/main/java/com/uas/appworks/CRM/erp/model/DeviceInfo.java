package com.uas.appworks.CRM.erp.model;

/**
 * Created by Bitlike on 2017/12/19.
 */

public class DeviceInfo {
    private boolean isForm;
    private String title;
    private String caption;
    private String field;
    private String values;

    public DeviceInfo(boolean isForm, String title, String caption, String field, String values) {
        this.isForm = isForm;
        this.caption = caption;
        this.field = field;
        this.values = values;
        this.title = title;
    }

    public boolean getIsForm() {
        return isForm;
    }

    public void setIsForm(boolean isForm) {
        this.isForm = isForm;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
