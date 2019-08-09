package com.uas.appworks.datainquiry.bean;

/**
 * Created by RaoMeng on 2017/9/2.
 */
public class DataInquiryTotalBean {
    private String mField;
    private String mCaption;
    private double mTotal;

    public String getField() {
        return mField;
    }

    public void setField(String field) {
        mField = field;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public double getTotal() {
        return mTotal;
    }

    public void setTotal(double total) {
        mTotal = total;
    }
}
