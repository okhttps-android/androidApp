package com.uas.appworks.model.bean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/17 13:43
 */

public class CityIndustryMenuBean {

    /**
     * st_name : 报销
     * st_id : 7
     * serves : [{"sv_url":"jsps/scm/sale/quotation.jsp","sv_name":"费用报销","sv_id":6,"sv_logourl":{"platform":"https://dfs.ubtob.com/group1/M00/61/8A/CgpkyFoNf5mAcxxqAAAKDVA-qto774.png","mobile":"https://dfs.ubtob.com/group1/M00/61/8A/CgpkyFoNXIuAQ4p2AAAEH_Dv4bk568.png"}}]
     */

    private String st_name;
    private int st_id;
    private List<ServesBean> serves;

    public String getSt_name() {
        return st_name;
    }

    public void setSt_name(String st_name) {
        this.st_name = st_name;
    }

    public int getSt_id() {
        return st_id;
    }

    public void setSt_id(int st_id) {
        this.st_id = st_id;
    }

    public List<ServesBean> getServes() {
        return serves;
    }

    public void setServes(List<ServesBean> serves) {
        this.serves = serves;
    }

    public static class ServesBean {
        /**
         * sv_url : jsps/scm/sale/quotation.jsp
         * sv_name : 费用报销
         * sv_id : 6
         * sv_logourl : {"platform":"https://dfs.ubtob.com/group1/M00/61/8A/CgpkyFoNf5mAcxxqAAAKDVA-qto774.png","mobile":"https://dfs.ubtob.com/group1/M00/61/8A/CgpkyFoNXIuAQ4p2AAAEH_Dv4bk568.png"}
         */

        private String sv_url;
        private String sv_name;
        private int sv_id;
        private String sv_desc;
        private SvLogourlBean sv_logourl;

        public String getSv_url() {
            return sv_url;
        }

        public void setSv_url(String sv_url) {
            this.sv_url = sv_url;
        }

        public String getSv_name() {
            return sv_name;
        }

        public void setSv_name(String sv_name) {
            this.sv_name = sv_name;
        }

        public int getSv_id() {
            return sv_id;
        }

        public void setSv_id(int sv_id) {
            this.sv_id = sv_id;
        }

        public String getSv_desc() {
            return sv_desc;
        }

        public void setSv_desc(String sv_desc) {
            this.sv_desc = sv_desc;
        }

        public SvLogourlBean getSv_logourl() {
            return sv_logourl;
        }

        public void setSv_logourl(SvLogourlBean sv_logourl) {
            this.sv_logourl = sv_logourl;
        }

        public static class SvLogourlBean {
            /**
             * platform : https://dfs.ubtob.com/group1/M00/61/8A/CgpkyFoNf5mAcxxqAAAKDVA-qto774.png
             * mobile : https://dfs.ubtob.com/group1/M00/61/8A/CgpkyFoNXIuAQ4p2AAAEH_Dv4bk568.png
             */

            private String platform;
            private String mobile;

            public String getPlatform() {
                return platform;
            }

            public void setPlatform(String platform) {
                this.platform = platform;
            }

            public String getMobile() {
                return mobile;
            }

            public void setMobile(String mobile) {
                this.mobile = mobile;
            }
        }
    }
}
