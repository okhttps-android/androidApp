package com.xzjmyk.pm.activity.ui.erp.entity;

/**
 * Created by FANGlh on 2017/2/10.
 * function:登入登出被杀死时间表
 */
public class InAndExitTimeEntity {
    private String loginin_time;
    private String loginexit_time;
    private String killed_time;

    public String getLoginin_time() {
        return loginin_time;
    }

    public void setLoginin_time(String loginin_time) {
        this.loginin_time = loginin_time;
    }

    public String getLoginexit_time() {
        return loginexit_time;
    }

    public void setLoginexit_time(String loginexit_time) {
        this.loginexit_time = loginexit_time;
    }

    public String getKilled_time() {
        return killed_time;
    }

    public void setKilled_time(String killed_time) {
        this.killed_time = killed_time;
    }
}
