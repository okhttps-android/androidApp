package com.uas.appme.settings.model;

import java.util.List;

/**
 * Created by ${FANGLH} on 2017/10/14.
 * Function：
 */

public class EmployeeRestListBean {
    public List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private String sf_companyid;
        private String sf_companyname;
        private String sf_date;
        private String sf_id;
        private String sf_userid;
        private String sf_username;

        public String getSf_companyid() {
            return sf_companyid;
        }

        public void setSf_companyid(String sf_companyid) {
            this.sf_companyid = sf_companyid;
        }

        public String getSf_companyname() {
            return sf_companyname;
        }

        public void setSf_companyname(String sf_companyname) {
            this.sf_companyname = sf_companyname;
        }

        public String getSf_date() {
            return sf_date;
        }

        public void setSf_date(String sf_date) {
            this.sf_date = sf_date;
        }

        public String getSf_id() {
            return sf_id;
        }

        public void setSf_id(String sf_id) {
            this.sf_id = sf_id;
        }

        public String getSf_userid() {
            return sf_userid;
        }

        public void setSf_userid(String sf_userid) {
            this.sf_userid = sf_userid;
        }

        public String getSf_username() {
            return sf_username;
        }

        public void setSf_username(String sf_username) {
            this.sf_username = sf_username;
        }
    }
}
