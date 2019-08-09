package com.uas.appworks.datainquiry.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 */
public class GridMenuReportStatisticsBean implements Serializable {

    /**
     * modelName : A
     * list : [{"title":"XXX","caller":"XXX","reportName":"XXX"}]
     */

    private String modelName;
    private List<ListBean> list;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean implements Serializable{
        /**
         * title : XXX
         * caller : XXX
         * reportName : XXX
         */

        private String title;
        private String caller;
        private String reportName;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public String getReportName() {
            return reportName;
        }

        public void setReportName(String reportName) {
            this.reportName = reportName;
        }
    }
}
