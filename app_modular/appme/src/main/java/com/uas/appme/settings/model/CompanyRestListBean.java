package com.uas.appme.settings.model;

import java.util.List;

/**
 * Created by ${FANGLH} on 2017/10/14.
 * Function：
 */

public class CompanyRestListBean {
    private List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private String sc_companyid;
        private String sc_companyname;
        private String sc_date;
        private String sc_id;

        public String getSc_companyid() {
            return sc_companyid;
        }

        public void setSc_companyid(String sc_companyid) {
            this.sc_companyid = sc_companyid;
        }

        public String getSc_companyname() {
            return sc_companyname;
        }

        public void setSc_companyname(String sc_companyname) {
            this.sc_companyname = sc_companyname;
        }

        public String getSc_date() {
            return sc_date;
        }

        public void setSc_date(String sc_date) {
            this.sc_date = sc_date;
        }

        public String getSc_id() {
            return sc_id;
        }

        public void setSc_id(String sc_id) {
            this.sc_id = sc_id;
        }
    }
}
