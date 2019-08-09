package com.uas.appworks.OA.erp.model;

/**
 * Created by FANGlh on 2017/6/12.
 * function:费用报销单消费明细Model
 */

public class CostFormModel {
    private String cost_type;
    private double cost_money;
    private String remark;
    private CostFormModel costFormModel;
    private int fpd_id;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getFpd_id() {return fpd_id;}
    public void setFpd_id(int fpd_id) {this.fpd_id = fpd_id;}
    public String getCost_type() {return cost_type;}
    public void setCost_type(String cost_type) {this.cost_type = cost_type;}
    public double getCost_money() {return cost_money;}
    public void setCost_money(double cost_money) {this.cost_money = cost_money;}
    public CostFormModel getCostFormModel() {return costFormModel;}
    public void setCostFormModel(CostFormModel costFormModel) {this.costFormModel = costFormModel;}

}
