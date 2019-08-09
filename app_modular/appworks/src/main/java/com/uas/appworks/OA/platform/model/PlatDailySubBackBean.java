package com.uas.appworks.OA.platform.model;

/**
 * Created by FANGlh on 2017/3/8.
 * function:
 */
public class PlatDailySubBackBean {

    /**
     * data : {"emcode":1000009169,"enuu":10030994,"wd_code":"WD17031648","wd_comment":"三八16.36","wd_date":1488962328221,"wd_experience":"1三八16.35","wd_id":45,"wd_plan":"三八16.35","wd_status":"在录入","wd_statuscode":"ENTERING"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * emcode : 1000009169
         * enuu : 10030994
         * wd_code : WD17031648
         * wd_comment : 三八16.36
         * wd_date : 1488962328221
         * wd_experience : 1三八16.35
         * wd_id : 45
         * wd_plan : 三八16.35
         * wd_status : 在录入
         * wd_statuscode : ENTERING
         */

        private int emcode;
        private int enuu;
        private String wd_code;
        private String wd_comment;
        private long wd_date;
        private String wd_experience;
        private int wd_id;
        private String wd_plan;
        private String wd_status;
        private String wd_statuscode;

        public int getEmcode() {
            return emcode;
        }

        public void setEmcode(int emcode) {
            this.emcode = emcode;
        }

        public int getEnuu() {
            return enuu;
        }

        public void setEnuu(int enuu) {
            this.enuu = enuu;
        }

        public String getWd_code() {
            return wd_code;
        }

        public void setWd_code(String wd_code) {
            this.wd_code = wd_code;
        }

        public String getWd_comment() {
            return wd_comment;
        }

        public void setWd_comment(String wd_comment) {
            this.wd_comment = wd_comment;
        }

        public long getWd_date() {
            return wd_date;
        }

        public void setWd_date(long wd_date) {
            this.wd_date = wd_date;
        }

        public String getWd_experience() {
            return wd_experience;
        }

        public void setWd_experience(String wd_experience) {
            this.wd_experience = wd_experience;
        }

        public int getWd_id() {
            return wd_id;
        }

        public void setWd_id(int wd_id) {
            this.wd_id = wd_id;
        }

        public String getWd_plan() {
            return wd_plan;
        }

        public void setWd_plan(String wd_plan) {
            this.wd_plan = wd_plan;
        }

        public String getWd_status() {
            return wd_status;
        }

        public void setWd_status(String wd_status) {
            this.wd_status = wd_status;
        }

        public String getWd_statuscode() {
            return wd_statuscode;
        }

        public void setWd_statuscode(String wd_statuscode) {
            this.wd_statuscode = wd_statuscode;
        }
    }
}
