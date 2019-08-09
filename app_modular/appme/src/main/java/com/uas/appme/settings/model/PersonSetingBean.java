package com.uas.appme.settings.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/10/11.
 * function:
 */

public class PersonSetingBean   {

    private List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * sm_companyid : 201
         * sm_companyname : 北大医院
         * sm_email : 587@163.om
         * sm_id : 10002
         * sm_level : 医师
         * sm_sex : 1
         * sm_stid : 10002
         * sm_stname : 眼科
         * sm_telephone : 13910000002
         * sm_userid : 2
         * sm_username : 张四
         */

        private String sm_companyid;
        private String sm_companyname;
        private String sm_email;
        private String sm_id;
        private String sm_level;
        private String sm_sex;
        private String sm_stid;
        private String sm_stname;
        private String sm_telephone;
        private String sm_userid;
        private String sm_username;

        public String getSm_companyid() {
            return sm_companyid;
        }

        public void setSm_companyid(String sm_companyid) {
            this.sm_companyid = sm_companyid;
        }

        public String getSm_companyname() {
            return sm_companyname;
        }

        public void setSm_companyname(String sm_companyname) {
            this.sm_companyname = sm_companyname;
        }

        public String getSm_email() {
            return sm_email;
        }

        public void setSm_email(String sm_email) {
            this.sm_email = sm_email;
        }

        public String getSm_id() {
            return sm_id;
        }

        public void setSm_id(String sm_id) {
            this.sm_id = sm_id;
        }

        public String getSm_level() {
            return sm_level;
        }

        public void setSm_level(String sm_level) {
            this.sm_level = sm_level;
        }

        public String getSm_sex() {
            return sm_sex;
        }

        public void setSm_sex(String sm_sex) {
            this.sm_sex = sm_sex;
        }

        public String getSm_stid() {
            return sm_stid;
        }

        public void setSm_stid(String sm_stid) {
            this.sm_stid = sm_stid;
        }

        public String getSm_stname() {
            return sm_stname;
        }

        public void setSm_stname(String sm_stname) {
            this.sm_stname = sm_stname;
        }

        public String getSm_telephone() {
            return sm_telephone;
        }

        public void setSm_telephone(String sm_telephone) {
            this.sm_telephone = sm_telephone;
        }

        public String getSm_userid() {
            return sm_userid;
        }

        public void setSm_userid(String sm_userid) {
            this.sm_userid = sm_userid;
        }

        public String getSm_username() {
            return sm_username;
        }

        public void setSm_username(String sm_username) {
            this.sm_username = sm_username;
        }
    }
}
