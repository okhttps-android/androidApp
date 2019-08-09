package com.uas.appworks.datainquiry.bean;

/**
 * Created by RaoMeng on 2017/9/14.
 */

public class ReportQuerySearchBean {
    private int mColor;
    private GridMenuReportStatisticsBean.ListBean mListBean;

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public GridMenuReportStatisticsBean.ListBean getListBean() {
        return mListBean;
    }

    public void setListBean(GridMenuReportStatisticsBean.ListBean listBean) {
        mListBean = listBean;
    }
}
