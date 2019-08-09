package com.uas.appworks.datainquiry.bean;

/**
 * Created by RaoMeng on 2017/9/14.
 */

public class DataInquirySearchBean {
    private int mColor;
    private GridMenuDataInquiryBean.QueryScheme mQueryScheme;

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public GridMenuDataInquiryBean.QueryScheme getQueryScheme() {
        return mQueryScheme;
    }

    public void setQueryScheme(GridMenuDataInquiryBean.QueryScheme queryScheme) {
        mQueryScheme = queryScheme;
    }
}
