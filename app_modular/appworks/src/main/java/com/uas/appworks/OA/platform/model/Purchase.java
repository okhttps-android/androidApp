package com.uas.appworks.OA.platform.model;

/**
 * Created by Bitlike on 2018/1/16.
 */

public class Purchase {
    private int id;
    private boolean canInput;//是否可以输入（从表里面，如果false 该从表所有输入框不能输入）
    private String customer;//客户（主表）
    private String address;//收货地址（主表）
    private String code;//订单号（主表）     编号（从表）
    private String time;//单据时间（主表）
    private String remarks;//备注（主表）  产品（从表）
    private String total;//金额（主表）    规格型号（从表）
    private String date;//交货日期（从表）
    private String number;//数量（从表）
    private String remarksInput;//备注（从表）
    private String amount;//
    private String currency;//币别（主表）
    private String unit;//单位（从表，跟在单价后面的）
    private String price;//单价

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUnit() {
        return unit==null?"PCS":unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCurrency() {
        return currency==null?"":currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount==null?"0":amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isCanInput() {
        return canInput;
    }

    public void setCanInput(boolean canInput) {
        this.canInput = canInput;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRemarksInput() {
        return remarksInput;
    }

    public void setRemarksInput(String remarksInput) {
        this.remarksInput = remarksInput;
    }
}
