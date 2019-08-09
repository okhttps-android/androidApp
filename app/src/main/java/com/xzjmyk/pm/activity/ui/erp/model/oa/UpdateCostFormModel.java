package com.xzjmyk.pm.activity.ui.erp.model.oa;

import com.uas.appworks.OA.erp.model.CostFormModel;

/**
 * Created by FANGlh on 2017/6/15.
 * function:
 */

public class UpdateCostFormModel {
    private String cost_type;
    private double cost_money;
    private CostFormModel costFormModel;

    public String getCost_type() {return cost_type;}
    public void setCost_type(String cost_type) {this.cost_type = cost_type;}
    public double getCost_money() {return cost_money;}
    public void setCost_money(double cost_money) {this.cost_money = cost_money;}
    public CostFormModel getCostFormModel() {return costFormModel;}
    public void setCostFormModel(CostFormModel costFormModel) {this.costFormModel = costFormModel;}
}
