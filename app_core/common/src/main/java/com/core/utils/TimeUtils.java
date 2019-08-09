package com.core.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.core.app.R;
import com.common.data.DateFormatUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 该类废弃食用，所有功能移到DateFormatUtil里面，新加的功能需要到DateFormatUtil类里面去添加
 */
@SuppressWarnings("deprecation")
@Deprecated
public class TimeUtils {

    public static long f_str_2_long(String dateString) {
        try {
            Date d = DateFormatUtil.getFormat(DateFormatUtil.YMD_HMS).parse(dateString);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String s_long_2_str(long timestamp) {
        if (timestamp == 0) return "";
        return DateFormatUtil.getFormat(DateFormatUtil.YMD).format(new Date(timestamp));// yyyy-mm-dd
    }

    public static String f_long_2_str(long timestamp) {
        if (timestamp == 0) return "";
        return DateFormatUtil.getFormat(DateFormatUtil.YMD_HMS).format(new Date(timestamp)); //yyyy-MM-dd HH:mm:ss
    }


    /**
     * 获取友好的时间显示
     *
     * @param time 秒级别的时间戳
     * @return
     */
    public static String getFriendlyTimeDesc(Context context, int time) {
        if (time == 0) return "";
        String desc = "";
        Date timeDate = new Date(time * 1000L);
        Date nowDate = new Date();
        long delaySeconds = nowDate.getTime() / 1000 - time;// 相差的秒数
        SimpleDateFormat friendly_format1 = DateFormatUtil.getFormat(DateFormatUtil.HM);
        SimpleDateFormat friendly_format2 = DateFormatUtil.getFormat("MM-dd HH:mm");
        if (delaySeconds < 10) {// 小于10秒，显示刚刚
            desc = context.getString(R.string.friendly_time_just_now);// 显示刚刚
        } else if (delaySeconds <= 60) {// 小于1分钟，显示如“25秒前”
            desc = delaySeconds + context.getString(R.string.friendly_time_before_seconds);
        } else if (delaySeconds < 60 * 30) {// 小于30分钟，显示如“25分钟前”
            desc = (delaySeconds / 60) + context.getString(R.string.friendly_time_before_minute);
        } else if (delaySeconds < 60 * 60 * 24) {// 小于1天之内
            if (nowDate.getDay() - timeDate.getDay() == 0) {// 同一天
                desc = friendly_format1.format(timeDate);
            } else {// 前一天
                desc = context.getString(R.string.friendly_time_yesterday) + " " + friendly_format1.format(timeDate);
            }
        } else if (delaySeconds < 60 * 60 * 24 * 2) {// 小于2天之内
            if (nowDate.getDay() - timeDate.getDay() == 1 || nowDate.getDay() - timeDate.getDay() == -6) {// 昨天
                desc = context.getString(R.string.friendly_time_yesterday) + " " + friendly_format1.format(timeDate);
            } else {// 前天
                desc = context.getString(R.string.friendly_time_before_yesterday) + " " + friendly_format1.format(timeDate);
            }
        } else if (delaySeconds < 60 * 60 * 24 * 3) {// 小于三天
            if (nowDate.getDay() - timeDate.getDay() == 2 || nowDate.getDay() - timeDate.getDay() == -5) {// 前天
                desc = context.getString(R.string.friendly_time_before_yesterday) + " " + friendly_format1.format(timeDate);
            }
        }
        if (TextUtils.isEmpty(desc)) {
            desc = friendly_format2.format(timeDate);
        }
        return desc;
    }

    public static long getSpecialBeginTime(TextView textView, long time) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (time > currentTime) {
            time = currentTime;
        }
        textView.setText(DateFormatUtil.long2Str(time * 1000, DateFormatUtil.YMD));
        return time;
    }


}
