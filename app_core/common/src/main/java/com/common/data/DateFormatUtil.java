package com.common.data;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Bitliker on 2017/8/11.
 */

public class DateFormatUtil {
    public static final String YMD = "yyyy-MM-dd";
    public static final String YM = "yyyy-MM";
    public static final String YMD_HMS = "yyyy-MM-dd HH:mm:ss";
    public static final String YMD_HM = "yyyy-MM-dd HH:mm";
    public static final String HM = "HH:mm";
    public static final String MD = "MM-dd";


    public static SimpleDateFormat getFormat(String format) {
        return new SimpleDateFormat(format);
    }

    public static String long2Str(long timemillis, String format) {
        SimpleDateFormat s = new SimpleDateFormat(format);
        return s.format(new Date(timemillis));
    }

    public static String long2Str(String format) {
        SimpleDateFormat s = new SimpleDateFormat(format);
        return s.format(new Date(System.currentTimeMillis()));
    }

    public static String date2Str(Date date, String format) {
        if (date == null) return "";
        SimpleDateFormat s = new SimpleDateFormat(format);
        return s.format(date);
    }

    public static Date str2date(String str, String format) {
        return new Date(str2Long(str, format));
    }

    public static long str2Long(String time, String format) {
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        SimpleDateFormat s = new SimpleDateFormat(format);
        try {
            return s.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String formatChange(String data, String format) {
        if (data == null || data.length() <= 0) {
            return "";
        } else {
            SimpleDateFormat df = new SimpleDateFormat(format);
            try {
                Date d = df.parse(data);
                return df.format(d);
            } catch (ParseException e) {
                return "";
            }
        }
    }


    /**
     * add by gongtao
     * <p>
     * 将Date类型的日期格式 转换为 符合要求的 String日期格式
     * </P>
     *
     * @param date
     * @param format
     * @return
     */
    public static String getStrDate4Date(Date date, String format) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat df = new SimpleDateFormat(format);
            return df.format(date);
        }
    }

    /**
     * add by gongtao 计算指定日期时间之间的时间差
     *
     * @param beginStr 开始日期字符串
     * @param endStr   结束日期字符串
     * @param f        时间差的形式0-秒,1-分种,2-小时,3--天 日期时间字符串格式:yyyyMMddHHmmss
     */
    public static int getInterval(String beginStr, String endStr, int f) {
        int hours = 0;
        try {
            Date beginDate = getFormat(YMD_HMS).parse(beginStr);
            Date endDate = getFormat(YMD_HMS).parse(endStr);
            long millisecond = endDate.getTime() - beginDate.getTime(); // 日期相减获取日期差X(单位:毫秒)
            /**
             * Math.abs((int)(millisecond/1000)); 绝对值 1秒 = 1000毫秒
             * millisecond/1000 --> 秒 millisecond/1000*60 - > 分钟
             * millisecond/(1000*60*60) -- > 小时 millisecond/(1000*60*60*24) -->
             * 天
             * */
            switch (f) {
                case 0: // second
                    return (int) (millisecond / 1000);
                case 1: // minute
                    return (int) (millisecond / (1000 * 60));
                case 2: // hour
                    return (int) (millisecond / (1000 * 60 * 60));
                case 3: // day
                    return (int) (millisecond / (1000 * 60 * 60 * 24));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hours;
    }

    /**
     * add by lipp
     * <p>
     * 获取起始日期前或后天数的日期
     * </P>
     *
     * @param starttime 起始日期 格式：yyyy-MM-dd
     * @param days
     * @return
     * @throws ParseException
     */
    public static Date getStartDateInterval(String starttime, int days) {
        // 格式化起始时间 yyyyMMdd
        Date startDate = null;
        try {
            startDate = getFormat(YMD).parse(starttime);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar startTime = Calendar.getInstance();
        startTime.clear();
        startTime.setTime(startDate);
        startTime.add(Calendar.DAY_OF_YEAR, days);
        return startTime.getTime();
    }

    /**
     * add by lipp
     * <p>
     * 获取起始日期和结束日期之间的天数
     * </P>
     *
     * @param beginStr 起始日期
     * @param endStr   结束日期
     * @param format   根据 日期参数的格式，传对应的SimpleDateFormat格式
     * @return 天数
     */
    public static int getDaysInterval(String beginStr, String endStr,
                                      SimpleDateFormat format) {
        try {
            Date beginDate = format.parse(beginStr);
            Date endDate = format.parse(endStr);
            long millisecond = endDate.getTime() - beginDate.getTime(); // 日期相减获取日期差X(单位:毫秒)
            return (int) (millisecond / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 根据指定日期,来运算加减乘
     *
     * @param date
     * @param format
     * @param value  add(new Date(),"yyyy-MM-dd HH:mm:ss",-1 * 1 * 60 * 60 * 1000);
     */
    public static String add(Date date, String format, long value) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        long newValue = date.getTime() + value;
        return df.format(new Date(newValue));
    }

    /**
     * add by gongtao
     * <p>
     * 将字符串类型的日期格式 转换为 符合要求的 Date类型的日期格式
     * </P>
     *
     * @param date
     * @param format
     * @return
     */
    public static Date getDate4StrDate(String date, String format) {
        if (date == null || date.trim().equals("")) {
            return null;
        } else {
            SimpleDateFormat df = new SimpleDateFormat(format);
            try {
                return df.parse(date);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    /**
     * @param beginDate
     * @param endDate
     * @param f         时间差的形式0:秒,1:分种,2:小时,3:天
     * @return
     */
    public static int getDifferenceNum(Date beginDate, Date endDate, int f) {
        int result = 0;
        if (beginDate == null || endDate == null) {
            return 0;
        }
        try {
            // 日期相减获取日期差X(单位:毫秒)
            long millisecond = endDate.getTime() - beginDate.getTime();
            /**
             * Math.abs((int)(millisecond/1000)); 绝对值 1秒 = 1000毫秒
             * millisecond/1000 --> 秒 millisecond/1000*60 - > 分钟
             * millisecond/(1000*60*60) -- > 小时 millisecond/(1000*60*60*24) -->
             * 天
             * */
            switch (f) {
                case 0: // second
                    return (int) (millisecond / 1000);
                case 1: // minute
                    return (int) (millisecond / (1000 * 60));
                case 2: // hour
                    return (int) (millisecond / (1000 * 60 * 60));
                case 3: // day
                    return (int) (millisecond / (1000 * 60 * 60 * 24));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取两个时间点所相差的时间戳
     *
     * @param start 开始时间 HH:mm
     * @param end   结束时间 HH:mm
     * @return 时间长度差（s）
     */
    public static int getDifferSS(String start, String end) throws Exception {
        String startTime = long2Str(YMD) + " " + start + ":00";
        String endTime = null;
        if (start.compareTo(end) > 0) {
            endTime = long2Str(System.currentTimeMillis() + (24 * 60 * 60 * 1000), YMD) + " " + end + ":00";
        } else
            endTime = long2Str(YMD) + " " + end + ":00";
        return (int) ((str2Long(endTime, YMD_HMS) - str2Long(startTime, YMD_HMS)) / 1000);
    }


    public static long hhmm2Long(String hhMM) {
        if (StringUtil.isEmpty(hhMM)) return 0;
        String che = long2Str(DateFormatUtil.YMD) + " " + hhMM + ":00";//当天下班时间
        return DateFormatUtil.str2Long(che, DateFormatUtil.YMD_HMS);
    }

    /**
     * date2比date1多的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2)   //同一年
        {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)    //闰年
                {
                    timeDistance += 366;
                } else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        } else    //不同年
        {
            System.out.println("判断day2 - day1 : " + (day2 - day1));
            return day2 - day1;
        }
    }
}
