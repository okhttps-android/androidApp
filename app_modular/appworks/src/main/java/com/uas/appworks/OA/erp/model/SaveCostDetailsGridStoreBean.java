package com.uas.appworks.OA.erp.model;

/**
 * Created by FANGlh on 2017/6/14.
 * function: 费用报销单保存，明细从表实体类
 */
public class SaveCostDetailsGridStoreBean {
    private String fpd_d1;
    private String fpd_d7;
    private double fpd_total;

    public String getFpd_d7() {
        return fpd_d7;
    }

    public void setFpd_d7(String fpd_d7) {
        this.fpd_d7 = fpd_d7;
    }

    public String getFpd_d1() {
        return fpd_d1;
    }

    public void setFpd_d1(String fpd_d1) {
        this.fpd_d1 = fpd_d1;
    }

    public double getFpd_total() {
        return fpd_total;
    }

    public void setFpd_total(double fpd_total) {
        this.fpd_total = fpd_total;
    }
}
