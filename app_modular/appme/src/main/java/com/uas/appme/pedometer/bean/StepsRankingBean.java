package com.uas.appme.pedometer.bean;

import java.util.List;

/**
 * Created by FANGlh on 2017/9/21.
 * function: 当天排行榜步数实体类
 */

public class StepsRankingBean {


    private List<AttrankBean> attrank;
    private List<ToalrankBean> toalrank;
    private List<Pricelist> pricelist;

    public List<Pricelist> getPricelist() {
        return pricelist;
    }

    public void setPricelist(List<Pricelist> pricelist) {
        this.pricelist = pricelist;
    }

    public List<AttrankBean> getAttrank() {
        return attrank;
    }

    public void setAttrank(List<AttrankBean> attrank) {
        this.attrank = attrank;
    }

    public List<ToalrankBean> getToalrank() {
        return toalrank;
    }

    public void setToalrank(List<ToalrankBean> toalrank) {
        this.toalrank = toalrank;
    }

    public static class AttrankBean {
        /**
         * as_date : 2017-09-22 00:00:00.0
         * as_id : 6
         * "as_prise": "2",
         * as_userid : 109079
         * as_username : 陈爱平
         * as_uusteps : 50
         * rank : 1
         */

        private String as_date;
        private String as_id;
        private String as_prise;
        private String as_userid;
        private String as_username;
        private String as_uusteps;
        private int rank;
        private Boolean prised;


        public Boolean getPrised() {
            return prised;
        }

        public void setPrised(Boolean prised) {
            this.prised = prised;
        }

        public String getAs_prise() {
            return as_prise;
        }

        public void setAs_prise(String as_prise) {
            this.as_prise = as_prise;
        }

        public String getAs_date() {
            return as_date;
        }

        public void setAs_date(String as_date) {
            this.as_date = as_date;
        }

        public String getAs_id() {
            return as_id;
        }

        public void setAs_id(String as_id) {
            this.as_id = as_id;
        }

        public String getAs_userid() {
            return as_userid;
        }

        public void setAs_userid(String as_userid) {
            this.as_userid = as_userid;
        }

        public String getAs_username() {
            return as_username;
        }

        public void setAs_username(String as_username) {
            this.as_username = as_username;
        }

        public String getAs_uusteps() {
            return as_uusteps;
        }

        public void setAs_uusteps(String as_uusteps) {
            this.as_uusteps = as_uusteps;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    public static class ToalrankBean {
        /**
         * as_date : 2017-09-22 00:00:00.0
         * as_id : 6
         * as_prise: "2",
         * as_userid : 109079
         * as_username : 陈爱平
         * as_uusteps : 50
         * rank : 1
         */

        private String as_date;
        private String as_id;
        private String as_prise;
        private String as_userid;
        private String as_username;
        private String as_uusteps;
        private int rank;
        private Boolean prised;


        public Boolean getPrised() {
            return prised;
        }

        public void setPrised(Boolean prised) {
            this.prised = prised;
        }

        public String getAs_prise() {
            return as_prise;
        }

        public void setAs_prise(String as_prise) {
            this.as_prise = as_prise;
        }

        public String getAs_date() {
            return as_date;
        }

        public void setAs_date(String as_date) {
            this.as_date = as_date;
        }

        public String getAs_id() {
            return as_id;
        }

        public void setAs_id(String as_id) {
            this.as_id = as_id;
        }

        public String getAs_userid() {
            return as_userid;
        }

        public void setAs_userid(String as_userid) {
            this.as_userid = as_userid;
        }

        public String getAs_username() {
            return as_username;
        }

        public void setAs_username(String as_username) {
            this.as_username = as_username;
        }

        public String getAs_uusteps() {
            return as_uusteps;
        }

        public void setAs_uusteps(String as_uusteps) {
            this.as_uusteps = as_uusteps;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    public static class Pricelist{
        /**
         * "ap_userid": "109805"
         */
        private String ap_userid;

        public String getAp_userid() {
            return ap_userid;
        }

        public void setAp_userid(String ap_userid) {
            this.ap_userid = ap_userid;
        }
    }
}
