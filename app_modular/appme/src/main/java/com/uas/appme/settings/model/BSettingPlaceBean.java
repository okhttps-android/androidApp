package com.uas.appme.settings.model;

import java.util.List;

/**
 * Created by ${FANGLH} on 2017/10/14.
 * Function：
 */

public class BSettingPlaceBean {

    private List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * st_companyid : 10046529
         * st_companyname : æå¡é¢çº¦1
         * st_id : 20244
         * st_imageurl : http://113.105.74.140:8081/u/0/0/201711/o/7cf6e83f6d114352b4e0bea103cc8826.png
         * st_name : å¥æç
         * st_price : 0
         * st_servicetime : 222
         * st_siid : 0
         */

        private String st_companyid;
        private String st_companyname;
        private String st_id;
        private String st_imageurl;
        private String st_name;
        private String st_price;
        private String st_servicetime;
        private String st_siid;

        public String getSt_companyid() {
            return st_companyid;
        }

        public void setSt_companyid(String st_companyid) {
            this.st_companyid = st_companyid;
        }

        public String getSt_companyname() {
            return st_companyname;
        }

        public void setSt_companyname(String st_companyname) {
            this.st_companyname = st_companyname;
        }

        public String getSt_id() {
            return st_id;
        }

        public void setSt_id(String st_id) {
            this.st_id = st_id;
        }

        public String getSt_imageurl() {
            return st_imageurl;
        }

        public void setSt_imageurl(String st_imageurl) {
            this.st_imageurl = st_imageurl;
        }

        public String getSt_name() {
            return st_name;
        }

        public void setSt_name(String st_name) {
            this.st_name = st_name;
        }

        public String getSt_price() {
            return st_price;
        }

        public void setSt_price(String st_price) {
            this.st_price = st_price;
        }

        public String getSt_servicetime() {
            return st_servicetime;
        }

        public void setSt_servicetime(String st_servicetime) {
            this.st_servicetime = st_servicetime;
        }

        public String getSt_siid() {
            return st_siid;
        }

        public void setSt_siid(String st_siid) {
            this.st_siid = st_siid;
        }
    }
}
