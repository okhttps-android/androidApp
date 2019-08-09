package com.uas.appme.pedometer.bean;

/**
 * Created by FANGlh on 2017/9/25.
 * function:  通用折线实体类，将X,Y轴数据以列表形式存贮
 */

public class ComPolylineBean  {
    private String xValue;
    private int yValue;

    public ComPolylineBean(String xValue, int yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public String getxValue() {
        return xValue;
    }

    public void setxValue(String xValue) {
        this.xValue = xValue;
    }

    public int getyValue() {
        return yValue;
    }

    public void setyValue(int yValue) {
        this.yValue = yValue;
    }

    @Override
    public String toString() {
        return "ComPolylineBean{" +
                "xValue='" + xValue + '\'' +
                ", yValue='" + yValue + '\'' +
                '}';
    }
}
