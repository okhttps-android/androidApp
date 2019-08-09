package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/22 15:20
 */
public class ChangeStageBean {
    private String name;//字段名
    private int isRequired;//必填
    private String value;//编辑值
    private String stageKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(int isRequired) {
        this.isRequired = isRequired;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStageKey() {
        return stageKey;
    }

    public void setStageKey(String stageKey) {
        this.stageKey = stageKey;
    }

    @Override
    public String toString() {
        return "ChangeStageBean{" +
                "name='" + name + '\'' +
                ", isRequired=" + isRequired +
                ", value='" + value + '\'' +
                ", stageKey='" + stageKey + '\'' +
                '}';
    }
}
