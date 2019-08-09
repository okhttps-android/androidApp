package com.uas.appme.pedometer.bean;

import java.util.List;

/**
 * Created by FANGlh on 2017/9/22.
 * function: 个人历史周/月步数
 */

public class PersonalStepBean  {

    private List<MonthStepsBean> monthSteps;
    private List<WeekStepsBean> weekSteps;

    public List<MonthStepsBean> getMonthSteps() {
        return monthSteps;
    }

    public void setMonthSteps(List<MonthStepsBean> monthSteps) {
        this.monthSteps = monthSteps;
    }

    public List<WeekStepsBean> getWeekSteps() {
        return weekSteps;
    }

    public void setWeekSteps(List<WeekStepsBean> weekSteps) {
        this.weekSteps = weekSteps;
    }

    public static class MonthStepsBean {
        /**
         * as_date : 2017-09-21
         * as_uusteps : 21
         */

        private String as_date;
        private String as_uusteps;

        public String getAs_date() {
            return as_date;
        }

        public void setAs_date(String as_date) {
            this.as_date = as_date;
        }

        public String getAs_uusteps() {
            return as_uusteps;
        }

        public void setAs_uusteps(String as_uusteps) {
            this.as_uusteps = as_uusteps;
        }
    }

    public static class WeekStepsBean {
        /**
         * as_date : 2017-09-21
         * as_uusteps : 21
         */

        private String as_date;
        private String as_uusteps;

        public String getAs_date() {
            return as_date;
        }

        public void setAs_date(String as_date) {
            this.as_date = as_date;
        }

        public String getAs_uusteps() {
            return as_uusteps;
        }

        public void setAs_uusteps(String as_uusteps) {
            this.as_uusteps = as_uusteps;
        }
    }
}
