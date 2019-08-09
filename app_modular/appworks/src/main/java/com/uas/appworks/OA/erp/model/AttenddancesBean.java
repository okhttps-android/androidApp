package com.uas.appworks.OA.erp.model;

import java.util.List;

/**
 * Created by FANGlh on 2017/2/17.
 * function:
 */
public class AttenddancesBean {

    /**
     * datas : [{"atime":"0","noncount":"0","emcode":"U0305","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"3","nday":"0","outcount":"0","emname":"吕全明","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0316","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"4","nday":"0","outcount":"0","emname":"刘杰","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0718","qjdaty":"1.84","ychuqin":"23","latecount":"0","achuqin":"2.16","nday":"0","outcount":"0","emname":"黄耀鹏","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0736","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"4","nday":"0","outcount":"0","emname":"龚鹏明","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0740","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"4","nday":"0","outcount":"0","emname":"周西","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0747","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"4","nday":"0","outcount":"0","emname":"方龙海","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0757","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"4","nday":"0","outcount":"0","emname":"饶猛","overtime":"0","outdays":"0","earlycount":"0"},{"atime":"0","noncount":"0","emcode":"U0766","qjdaty":"0","ychuqin":"23","latecount":"0","achuqin":"4","nday":"0","outcount":"0","emname":"李洋洋","overtime":"0","outdays":"0","earlycount":"0"}]
     * sessionId : 5BFD48DFDCC6C3D36E952E545B801224
     * success : true
     */

    private String sessionId;
    private boolean success;
    private List<DatasBean> datas;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<DatasBean> getDatas() {
        return datas;
    }

    public void setDatas(List<DatasBean> datas) {
        this.datas = datas;
    }

    public static class DatasBean {
        /**
         * achuqin : 0
         * atime : 0                //实际工时
         * earlycount : 0           //早退
         * emcode : 1122
         * emname : 彬彬            // 姓名
         * latecount : 0           //迟到
         * nday : 0
         * noncount : 0            //旷工
         * outcount : 0
         * outdays : 0
         * overtime : 0
         * qjdaty : 0
         * ychuqin : 0
         */

        private String atime;
        private String noncount;
        private String emcode;
        private String qjdaty;
        private String ychuqin;
        private String latecount;
        private String achuqin;
        private String nday;
        private String outcount;
        private String emname;
        private String overtime;
        private String outdays;
        private String earlycount;

        public String getAtime() {
            return atime;
        }

        public void setAtime(String atime) {
            this.atime = atime;
        }

        public String getNoncount() {
            return noncount;
        }

        public void setNoncount(String noncount) {
            this.noncount = noncount;
        }

        public String getEmcode() {
            return emcode;
        }

        public void setEmcode(String emcode) {
            this.emcode = emcode;
        }

        public String getQjdaty() {
            return qjdaty;
        }

        public void setQjdaty(String qjdaty) {
            this.qjdaty = qjdaty;
        }

        public String getYchuqin() {
            return ychuqin;
        }

        public void setYchuqin(String ychuqin) {
            this.ychuqin = ychuqin;
        }

        public String getLatecount() {
            return latecount;
        }

        public void setLatecount(String latecount) {
            this.latecount = latecount;
        }

        public String getAchuqin() {
            return achuqin;
        }

        public void setAchuqin(String achuqin) {
            this.achuqin = achuqin;
        }

        public String getNday() {
            return nday;
        }

        public void setNday(String nday) {
            this.nday = nday;
        }

        public String getOutcount() {
            return outcount;
        }

        public void setOutcount(String outcount) {
            this.outcount = outcount;
        }

        public String getEmname() {
            return emname;
        }

        public void setEmname(String emname) {
            this.emname = emname;
        }

        public String getOvertime() {
            return overtime;
        }

        public void setOvertime(String overtime) {
            this.overtime = overtime;
        }

        public String getOutdays() {
            return outdays;
        }

        public void setOutdays(String outdays) {
            this.outdays = outdays;
        }

        public String getEarlycount() {
            return earlycount;
        }

        public void setEarlycount(String earlycount) {
            this.earlycount = earlycount;
        }
    }
}
