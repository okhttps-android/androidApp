package com.modular.booking.activity.utils;

import com.common.data.DateFormatUtil;

import java.util.Calendar;

/**
 * Created by Arison on 2017/11/9.
 */

public class ServiceUtils {

    public static int  getCodeDateByService(String date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateFormatUtil.getDate4StrDate(date, "yyyy-MM-dd HH:mm"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 12) {
            System.out.print("早上好");
            return 1;
        } else if (hour >= 12 && hour < 18) {
            System.out.print("下午好");
            return 2;
        } else if (hour >= 18 && hour < 24) {
            System.out.print("晚上");
            return 3;
        }
        return 0;
    }
}
