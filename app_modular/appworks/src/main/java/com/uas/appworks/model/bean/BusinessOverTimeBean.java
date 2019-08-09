package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:51
 */
public class BusinessOverTimeBean {

    /**
     * stepName : 立项评估
     * name : 啥地方
     * lastetime : 1516961433000
     * man : 周袁
     */

    private String stepName;
    private String name;
    private long lastetime;
    private String man;

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastetime() {
        return lastetime;
    }

    public void setLastetime(long lastetime) {
        this.lastetime = lastetime;
    }

    public String getMan() {
        return man;
    }

    public void setMan(String man) {
        this.man = man;
    }
}
