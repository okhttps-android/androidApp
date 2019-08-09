package com.uas.appworks.CRM.erp.model;

/**
 * Created by Arisono on 2016/6/24.
 */
public class Business {
    /*商机编号： XXXX
    商机名称： XXX
    负责人：    XXXX
    线索来源： XXXX
    联系方式： XXXX
    备注：        XXXX*/
    private int bc_id;
    private String code;
    private String num;
    private String steps;
    private String name;
    private String leader;
    private String source;
    private String phone;
    private String note;
    private String date;
    private String currentprocess;//商机阶段
    private int type;//抢，分配
    public boolean isChecked;


    private final int BUSINESS_QIANG = 1;
    private final int BUSINESS_FENPEI = 2;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBUSINESS_QIANG() {
        return BUSINESS_QIANG;
    }

    public int getBUSINESS_FENPEI() {
        return BUSINESS_FENPEI;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getBc_id() {
        return bc_id;
    }

    public void setBc_id(int bc_id) {
        this.bc_id = bc_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCurrentprocess() {
        return currentprocess;
    }

    public void setCurrentprocess(String currentprocess) {
        this.currentprocess = currentprocess;
    }
}
