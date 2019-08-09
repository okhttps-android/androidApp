package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:51
 */
public class BusinessRecordBean {

    /**
     * name : sss
     * time : 1535904000000
     * man : 周袁
     * info : 商机手动收回
     */

    private String name;
    private long time;
    private String man;
    private String info;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMan() {
        return man;
    }

    public void setMan(String man) {
        this.man = man;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
