package com.uas.appworks.model;

/**
 * Created by Bitlike on 2018/2/28.
 */

public class DeviceMatch {
    //机型、机型设备列表名称、现有匹配设备数量、欠缺设备数量,需求数量
    private String code, name, existqty, lackqty,spec,needqty;


    public String getNeedqty() {
        return needqty;
    }

    public void setNeedqty(String needqty) {
        this.needqty = needqty;
    }

    public String getSpec() {
        return spec==null?"":spec;
    }

    public String getCode() {
        return code == null ? "" : code;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExistqty() {
        return existqty == null ? "" : existqty;
    }

    public void setExistqty(String existqty) {
        this.existqty = existqty;
    }

    public String getLackqty() {
        return lackqty == null ? "" : lackqty;
    }

    public void setLackqty(String lackqty) {
        this.lackqty = lackqty;
    }
}
