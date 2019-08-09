package com.uas.appworks.OA.platform.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/3/9.
 * function:
 */
public class PlatSignAddressBean {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * cs_code : 17031145
         * cs_id : 32
         * cs_innerdistance : 500
         * cs_latitude : 22.54073493419767
         * cs_longitude : 113.9531261109972
         * cs_recorddate : 1489463205872
         * cs_validrange : 300
         * cs_workaddr : 科技南五路5号英唐大厦3F
         * emcode : 1000009169
         * enuu : 10030994
         * shortname : 云幻教育科技股份有限公司
         */

        private String cs_code;
        private int cs_id;
        private int cs_innerdistance;
        private double cs_latitude;
        private double cs_longitude;
        private long cs_recorddate;
        private int cs_validrange;
        private String cs_workaddr;
        private int emcode;
        private int enuu;
        private String shortname;

        public String getCs_code() {
            return cs_code;
        }

        public void setCs_code(String cs_code) {
            this.cs_code = cs_code;
        }

        public int getCs_id() {
            return cs_id;
        }

        public void setCs_id(int cs_id) {
            this.cs_id = cs_id;
        }

        public int getCs_innerdistance() {
            return cs_innerdistance;
        }

        public void setCs_innerdistance(int cs_innerdistance) {
            this.cs_innerdistance = cs_innerdistance;
        }

        public double getCs_latitude() {
            return cs_latitude;
        }

        public void setCs_latitude(double cs_latitude) {
            this.cs_latitude = cs_latitude;
        }

        public double getCs_longitude() {
            return cs_longitude;
        }

        public void setCs_longitude(double cs_longitude) {
            this.cs_longitude = cs_longitude;
        }

        public long getCs_recorddate() {
            return cs_recorddate;
        }

        public void setCs_recorddate(long cs_recorddate) {
            this.cs_recorddate = cs_recorddate;
        }

        public int getCs_validrange() {
            return cs_validrange;
        }

        public void setCs_validrange(int cs_validrange) {
            this.cs_validrange = cs_validrange;
        }

        public String getCs_workaddr() {
            return cs_workaddr;
        }

        public void setCs_workaddr(String cs_workaddr) {
            this.cs_workaddr = cs_workaddr;
        }

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

        public String getShortname() {
            return shortname;
        }

        public void setShortname(String shortname) {
            this.shortname = shortname;
        }
    }
}
