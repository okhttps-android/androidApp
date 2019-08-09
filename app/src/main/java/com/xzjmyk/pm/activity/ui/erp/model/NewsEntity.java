package com.xzjmyk.pm.activity.ui.erp.model;

import java.util.List;

public class NewsEntity {
    private List<Data> data;
    
    public void setData(List<Data> data) {
        this.data = data;
    }

    public List<Data> getData() {
        return this.data;
    }

    public class Data {
//		private int RN;
//
//		private String ne_code;
//
//		private String ne_type;
//
//		private String ne_theme;
//
//		private String ne_releaser;
//
//		private String ne_releasedate;
//
//		private int ne_browsenumber;
//
//		private int ne_id;

        private int NE_ID;
        private Object NE_NKID;
        private String NE_RELEASER;//作者
        private String NE_THEME;//
        private Long NE_RELEASEDATE;//时间
        private int NE_BROWSENUMBER;//浏览量
        private String NE_TYPE;//新闻类型
        private String NE_CODE;
        private String NE_CONTENT;//响应正文
        private Object NE_FEEL;
        private Object NE_ATTACHS;
        private int NE_ISTOP;//是否公开
        private int RN;//编号
        private Object STATUS;//状态

        public int getNE_ID() {
            return NE_ID;
        }

        public void setNE_ID(int NE_ID) {
            this.NE_ID = NE_ID;
        }

        public Object getNE_NKID() {
            return NE_NKID;
        }

        public void setNE_NKID(Object NE_NKID) {
            this.NE_NKID = NE_NKID;
        }

        public String getNE_RELEASER() {
            return NE_RELEASER;
        }

        public void setNE_RELEASER(String NE_RELEASER) {
            this.NE_RELEASER = NE_RELEASER;
        }

        public String getNE_THEME() {
            return NE_THEME;
        }

        public void setNE_THEME(String NE_THEME) {
            this.NE_THEME = NE_THEME;
        }

        public Long getNE_RELEASEDATE() {
            return NE_RELEASEDATE;
        }

        public void setNE_RELEASEDATE(Long NE_RELEASEDATE) {
            this.NE_RELEASEDATE = NE_RELEASEDATE;
        }

        public int getNE_BROWSENUMBER() {
            return NE_BROWSENUMBER;
        }

        public void setNE_BROWSENUMBER(int NE_BROWSENUMBER) {
            this.NE_BROWSENUMBER = NE_BROWSENUMBER;
        }

        public String getNE_TYPE() {
            return NE_TYPE;
        }

        public void setNE_TYPE(String NE_TYPE) {
            this.NE_TYPE = NE_TYPE;
        }

        public String getNE_CODE() {
            return NE_CODE;
        }

        public void setNE_CODE(String NE_CODE) {
            this.NE_CODE = NE_CODE;
        }

        public String getNE_CONTENT() {
            return NE_CONTENT;
        }

        public void setNE_CONTENT(String NE_CONTENT) {
            this.NE_CONTENT = NE_CONTENT;
        }

        public Object getNE_FEEL() {
            return NE_FEEL;
        }

        public void setNE_FEEL(Object NE_FEEL) {
            this.NE_FEEL = NE_FEEL;
        }

        public Object getNE_ATTACHS() {
            return NE_ATTACHS;
        }

        public void setNE_ATTACHS(Object NE_ATTACHS) {
            this.NE_ATTACHS = NE_ATTACHS;
        }

        public int getNE_ISTOP() {
            return NE_ISTOP;
        }

        public void setNE_ISTOP(int NE_ISTOP) {
            this.NE_ISTOP = NE_ISTOP;
        }

        public int getRN() {
            return RN;
        }

        public void setRN(int RN) {
            this.RN = RN;
        }

        public Object getSTATUS() {
            return STATUS;
        }

        public void setSTATUS(Object STATUS) {
            this.STATUS = STATUS;
        }
    }
}
