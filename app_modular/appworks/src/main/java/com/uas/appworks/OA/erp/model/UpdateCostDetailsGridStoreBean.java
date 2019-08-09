package com.uas.appworks.OA.erp.model;

/**
 * Created by FANGlh on 2017/6/14.
 * function: 费用报销单保存，明细从表实体类
 */
public class UpdateCostDetailsGridStoreBean {
    private String fpd_d1;
    private String fpd_d7;
    private double fpd_total;
    private int fpd_id;
    private int fpd_fpid;

    public String getFpd_d7() {
        return fpd_d7;
    }

    public void setFpd_d7(String fpd_d7) {
        this.fpd_d7 = fpd_d7;
    }

    public int getFpd_fpid() {
        return fpd_fpid;
    }

    public void setFpd_fpid(int fpd_fpid) {
        this.fpd_fpid = fpd_fpid;
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

    public int getFpd_id() {
        return fpd_id;
    }

    public void setFpd_id(int fpd_id) {
        this.fpd_id = fpd_id;
    }
}
