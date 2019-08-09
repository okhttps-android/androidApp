package com.uas.appworks.OA.erp.model;

/**
 * Created by FANGlh on 2017/6/19.
 * function:
 */

public class CostTypeSingleBean {

    /**
     * fk_name : 餐费
     * fk_desc : 公司每月餐费
     */

    private String fk_name;
    private String fk_desc;

    public String getFk_name() {
        return fk_name;
    }

    public void setFk_name(String fk_name) {
        this.fk_name = fk_name;
    }

    public String getFk_desc() {
        return fk_desc;
    }

    public void setFk_desc(String fk_desc) {
        this.fk_desc = fk_desc;
    }
}
