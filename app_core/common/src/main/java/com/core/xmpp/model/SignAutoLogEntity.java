package com.core.xmpp.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by FANGlh on 2017/5/9.
 * function:  打卡签到or外勤签到失败缓存本地数据实体类
 */
public class SignAutoLogEntity {
    @JSONField  // @JSONField是fastjson中的一个注解。在属性头上加上这个注解中，在对对象进行json转换时，该属性，将不会参与格式化。
    private int id; // 数据中分配id
    private String aa_type; //操作类型 打卡签到or外勤签到
    private String aa_location; //当前位置
    private String aa_remark; //失败原因
    private String aa_date; //时间
    private String aa_telephone; // 手机
    @JSONField
    private int sendstatus; // 是否已经发送到服务端更新，true :是

    public SignAutoLogEntity(int id, String aa_type, String aa_location, String aa_remark, String aa_date, String aa_telephone, int sendstatus) {
        this.id = id;
        this.aa_type = aa_type;
        this.aa_location = aa_location;
        this.aa_remark = aa_remark;
        this.aa_date = aa_date;
        this.aa_telephone = aa_telephone;
        this.sendstatus = sendstatus;
    }
    public SignAutoLogEntity() {
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getAa_type() {return aa_type;}
    public void setAa_type(String aa_type) {this.aa_type = aa_type;}
    public String getAa_location() {return aa_location;}
    public void setAa_location(String aa_location) {this.aa_location = aa_location;}
    public String getAa_remark() {return aa_remark;}
    public void setAa_remark(String aa_remark) {this.aa_remark = aa_remark;}
    public String getAa_date() {return aa_date;}
    public void setAa_date(String aa_date) {this.aa_date = aa_date;}
    public String getAa_telephone() {return aa_telephone;}
    public void setAa_telephone(String aa_telephone) {this.aa_telephone = aa_telephone;}

    public int getSendstatus() {return sendstatus;}
    public void setSendstatus(int sendstatus) {this.sendstatus = sendstatus;}
    public String toString() {
        return "SignAutoLogEntity{" +
                "id='" + id  +
                ", aa_type=" + aa_type +
                ", aa_location=" + aa_location +
                ", aa_remark=" + aa_remark +
                ", aa_date=" + aa_date +
                ", aa_telephone=" + aa_telephone +
                ", sendstatus=" + sendstatus +
                '}';
    }
}
