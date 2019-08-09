package com.uas.appworks.model.bean;

import java.io.Serializable;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/15 16:36
 */

public class B2BCompanyBean implements Serializable {

    /**
     * id : 83573
     * enuu : 1000001
     * businessCode : 8888888888
     * name : 深圳市优软科技有限公司
     */

    private int id;
    private String enuu;
    private String businessCode;
    private String name;
    private boolean isSelected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnuu() {
        return enuu;
    }

    public void setEnuu(String enuu) {
        this.enuu = enuu;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
