package com.common.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by Bitliker on 2017/8/11.
 */

public class CalendarUtil {

    public static void nextMonth(Calendar c) {
        if (c == null) c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
    }

    public static void preMonth(Calendar c) {
        if (c == null) c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
    }


    public static Calendar getCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        if (date != null) {
            c.setTime(date);
        }
        return c;
    }

    /*获取当前年*/
    public static int getYear() {
        return getYear(null);
    }

    /*获得指定的年，int格式*/
    public static int getYear(Date date) {
        return getCalendar(date).get(Calendar.YEAR);
    }

    /*获取当前月*/
    public static int getMonth() {
        return getMonth(null);

    }

    /*获得指定的月*/
    public static int getMonth(Date date) {
        return getCalendar(date).get(Calendar.MONTH) + 1;
    }

    /*获取当前日*/
    public static int getDay() {
        return getDay(null);
    }

    /*获得指定的日*/
    public static int getDay(Date date) {
        return getCalendar(date).get(Calendar.DAY_OF_MONTH);
    }

    /*获取当前系统时间的小时数*/
    public static int getHour() {
        return getHour(null);
    }

    /*获得指定时间的小时*/
    public static int getHour(Date date) {
        return getCalendar(date).get(Calendar.HOUR_OF_DAY);
    }

    /*获取当前系统时间的分钟数*/
    public static int getMinute() {
        return getMinute(null);
    }

    /*获得指定的分钟*/
    public static int getMinute(Date date) {
        return getCalendar(date).get(Calendar.MINUTE);
    }

    /*获得指定的秒*/
    public static int getSecond(Date date) {
        return getCalendar(date).get(Calendar.SECOND);
    }

    /*获取当前系统时间的秒数*/
    public static int getSecond() {
        Calendar calendar = new GregorianCalendar();
        return calendar.get(Calendar.SECOND);
    }

    public static int getSecondMillion() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /*获取本月第一天*/
    public static Date getFristDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTime();
    }


    // 获得本月第一天0点时间
    public static Date getTimesMonthmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Date getLastMonthStartMorning() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTimesMonthmorning());
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    /*获取当前时间前几天或后几天的日期*/
    public static Date getAddDays(int days) {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }


    public static Date getPreYearStartTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.YEAR));
        return cal.getTime();
    }

    public static Date getCurrentYearStartTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.YEAR));
        return cal.getTime();
    }

    /*当前季度的结束时间，即2012-03-31 23:59:59*/
    public static Date getCurrentQuarterEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getCurrentQuarterStartTime());
        cal.add(Calendar.MONTH, 3);
        return cal.getTime();
    }

    /*前季度的开始时间，即2012-03-31 23:59:59*/
    public static Date getPreQuarterStartTime() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -3);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        System.out.println("currentMoth" + currentMonth);
        SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = null;
        try {
            if (currentMonth >= 1 && currentMonth <= 3)
                c.set(Calendar.MONTH, 0);
            else if (currentMonth >= 4 && currentMonth <= 6)
                c.set(Calendar.MONTH, 3);
            else if (currentMonth >= 7 && currentMonth <= 9) {
                c.set(Calendar.MONTH, 6);
                System.out.println(new SimpleDateFormat("yyyyMM").format(c.getTime()));
            } else if (currentMonth >= 10 && currentMonth <= 12)
                c.set(Calendar.MONTH, 9);
            c.set(Calendar.DATE, 1);
            now = longSdf.parse(shortSdf.format(c.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return now;
    }

    /*当前季度的开始时间，即2012-03-31 23:59:59*/
    public static Date getCurrentQuarterStartTime() {
        Calendar c = Calendar.getInstance();
        // c.add(Calendar.MONTH, -3);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        System.out.println("currentMoth" + currentMonth);
        SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = null;
        try {
            if (currentMonth >= 1 && currentMonth <= 3)
                c.set(Calendar.MONTH, 0);
            else if (currentMonth >= 4 && currentMonth <= 6)
                c.set(Calendar.MONTH, 3);
            else if (currentMonth >= 7 && currentMonth <= 9) {
                c.set(Calendar.MONTH, 6);
            } else if (currentMonth >= 10 && currentMonth <= 12)
                c.set(Calendar.MONTH, 9);
            c.set(Calendar.DATE, 1);
            now = longSdf.parse(shortSdf.format(c.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return now;
    }

    /*前季度的结束时间，即2012-03-31 23:59:59*/
    public static Date getPreQuarterEndTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getPreQuarterStartTime());
        cal.add(Calendar.MONTH, 3);
        return cal.getTime();
    }

    public static String getWeek(String yyyy_MM_dd) { //yyyy-MM-dd
        return getWeek(DateFormatUtil.str2Long(yyyy_MM_dd, DateFormatUtil.YMD));
    }

    public static String getWeek(long date) { //yyyy-MM-dd
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        String src = "";

        int week = c.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case 1:
                src = "周日";
                break;
            case 2:
                src = "周一";
                break;
            case 3:
                src = "周二";
                break;
            case 4:
                src = "周三";
                break;
            case 5:
                src = "周四";
                break;
            case 6:
                src = "周五";
                break;
            case 7:
                src = "周六";
                break;

        }
        return src;
    }


    /*当前月份共有多少天*/
    public static int getCurrentDateDays(int year, int month) {
        int days = 0;
        if (month == 2) {
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                days = 29; //闰年2月有29天
            } else {
                days = 28;
            }
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            days = 30;
        } else {
            days = 31;
        }
        return days;
    }


}
