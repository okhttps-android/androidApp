package com.uas.appworks.utils;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.uas.appworks.model.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ScheduleUtils {
    private static final String DEF_EMAIL = "uu@usoftchina.com";
    private static final String NAME = "UU互联";

    /**
     * 检查是否存在指定账号
     * 存在则返回账户id，否则返回-1
     * 没有权限返回-2
     */
    public static int checkCalendarAccount(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            //TODO 没有权限
            return -2;
        }
        String selection = CalendarContract.Calendars.ACCOUNT_NAME + " =? ";
        String[] selectionArgs = new String[]{DEF_EMAIL};
        Cursor userCursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                null, selection, selectionArgs, null);
        try {
            if (userCursor == null)//查询返回空值
                return -1;
            int count = userCursor.getCount();
            if (count > 0) {//存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
     * 向系统日历添加一个账号
     *
     * @param context
     * @return
     */
    public static long addCalendarAccount(Context context) {
        String accountType = "com.android.exchange";
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, DEF_EMAIL);
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, DEF_EMAIL);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, accountType);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);
        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, DEF_EMAIL)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }


    /**
     * 添加日程事件和提醒
     *
     * @param context
     * @param mSchedule
     * @return ：
     * -1： 获取账户id失败直接返回，添加日历事件失败
     * -2： 没有获取到权限
     * -3： 添加日历事件失败直接返回
     * -4： 添加闹钟提醒失败直接返回
     */
    public static long addCalendarEvent(Context context, Schedule mSchedule) {
        // 获取日历账户的id
        int calId = checkCalendarAccount(context);
        if (calId < 0) {
            // 获取账户id失败直接返回，添加日历事件失败
            if (addCalendarAccount(context) == -1) {
                return -1;
            }
        }
        //开始添加日程事件
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.CALENDAR_ID, calId);//选择插入账户的id
        event.put(CalendarContract.Events.SELF_ATTENDEE_STATUS, mSchedule.getId());//选择插入账户的id
        event.put(CalendarContract.Events.TITLE, mSchedule.getTitle());//添加标题
        event.put(CalendarContract.Events.DESCRIPTION, mSchedule.getRemarks());//添加描述|备注
        event.put(CalendarContract.Events.EVENT_LOCATION, mSchedule.getAddress());//事件地点
//      event.put(CalendarContract.Events.RDATE, mSchedule.getRepeat());//重事件
        event.put(CalendarContract.Events.EXDATE, mSchedule.getTag());//
//      event.put(CalendarContract.Events.DISPLAY_COLOR, Color.GREEN);//显示颜色
        event.put(CalendarContract.Events.EVENT_COLOR, Color.GREEN);//颜色
        event.put(CalendarContract.Events.ORGANIZER, DEF_EMAIL);//事件所有者的名称
        event.put(CalendarContract.Events.LAST_DATE, System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);//事件重复日期
        event.put(CalendarContract.Events.DTSTART, mSchedule.getStartTime());//日程开始时间
        event.put(CalendarContract.Events.DTEND, mSchedule.getEndTime());//日程结束时间时间
        event.put(CalendarContract.Events.HAS_ALARM, mSchedule.hasAlarm());//设置是否有闹钟提醒
        event.put(CalendarContract.Events.ORIGINAL_ALL_DAY, mSchedule.getAllDay());//是否全天
        event.put(CalendarContract.Events.ALL_DAY, mSchedule.getAllDay());//是否全天
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());  //这个是时区，必须有，
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // 没有 权限
            return -2;
        }
        //添加事件
        Uri newEvent = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, event);
        if (newEvent == null) {
            // 添加日历事件失败直接返回
            return -3;
        }
        if (mSchedule.hasAlarm() > 0) {
            // 如果需要提醒的情况下
            //事件提醒的设定
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
            // 提前10分钟有提醒
            values.put(CalendarContract.Reminders.MINUTES, mSchedule.getWarnTime());//提前时间提醒
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, values);
            if (uri == null) {
                // 添加闹钟提醒失败直接返回
                return -4;
            }
        }
        return 1;
    }

    /**
     * 获取到系统的对应账户的日程
     *
     * @param context
     * @return
     */
    public static List<Schedule> getSystemCalendar(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            //TODO 没有权限
            return null;
        }
        String selection = CalendarContract.Events.ORGANIZER + " =? ";
        String[] selectionArgs = new String[]{DEF_EMAIL};
        Cursor userCursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                null, selection, selectionArgs, null);
        try {
            if (userCursor == null)//查询返回空值
                return null;
            List<Schedule> mSchedules = new ArrayList<>();
            while (userCursor.moveToNext()) {
                mSchedules.add(getSchedule(userCursor));
            }

        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
        return null;
    }

    /**
     * 获取系统日程事件，依据系统日历获取，如获取2018-11-11的日程时候传 开始时间：2018-11-11 00:00:01  结束时间：2018-11-12 00:00:00
     * @param context
     * @param startTime 开始时间节点
     * @param endTime  结束时间节点
     * @return  1.系统日程的开始时间在 以上时间节点之内的
     *          2.系统日程的结束时间在 以上时间节点之内的
     *          3.以上时间段在系统日程开始时间和结束时间范围之内
     */
    public static List<Schedule> getSystemCalendar(Context context, long startTime, long endTime) {
        List<Schedule> mSchedules = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return mSchedules;
        }
        String selection = CalendarContract.Events.CAN_PARTIALLY_UPDATE + " =? "
                + "and ( ("
                + CalendarContract.Events.DTSTART + " >=? and " + CalendarContract.Events.DTSTART + " <=? ) or ("
                + CalendarContract.Events.DTEND + " >=? and " + CalendarContract.Events.DTEND + " <=? ) or ( "
                + CalendarContract.Events.DTSTART + " <? and " + CalendarContract.Events.DTEND + " >?"
                + " ))";
        String[] selectionArgs = new String[]{String.valueOf(0)
                , String.valueOf(startTime), String.valueOf(endTime)
                , String.valueOf(startTime), String.valueOf(endTime)
                , String.valueOf(startTime), String.valueOf(endTime)
        };
        Log.i("gong", "selection=" + selection);
        Log.i("gong", "startTime=" + startTime);
        Log.i("gong", "endTime=" + endTime);
        Cursor userCursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                null, selection, selectionArgs, null);
        try {
            if (userCursor == null)//查询返回空值
                return null;
            mSchedules = new ArrayList<>();
            while (userCursor.moveToNext()) {
                mSchedules.add(getSchedule(userCursor));
            }

        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
            return mSchedules;
        }
    }

    /**
     * 删除对应的日程
     *
     * @param context
     * @param id      id
     * @return
     */
    public static long deleteSystemCalendar(Context context, int id) {
        int deleteOk = -1;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return deleteOk;
        }
        try {
            String where = CalendarContract.Events.ORGANIZER + " =? and " + CalendarContract.Events.SELF_ATTENDEE_STATUS + " =? ";
            String[] whereArgs = new String[]{DEF_EMAIL, String.valueOf(id)};
            deleteOk = context.getContentResolver().delete(CalendarContract.Events.CONTENT_URI, where, whereArgs);
        } finally {
            return deleteOk;
        }
    }

    public static boolean hasSystemCalendar(Context context, int id) {
        boolean has = false;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            String where = CalendarContract.Events.ORGANIZER + " =? and " + CalendarContract.Events.SELF_ATTENDEE_STATUS + " =? ";
            String[] whereArgs = new String[]{DEF_EMAIL, String.valueOf(id)};
            Cursor c = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                    null, where, whereArgs, null);
            has = c != null && c.getCount() > 0;
        } finally {
            return has;
        }
    }

    /**
     * 更新系统日程
     *
     * @param context
     * @param mSchedule 必须有id
     * @return
     */
    public static long updateSystemCalendar(Context context, Schedule mSchedule) {
        deleteSystemCalendar(context, mSchedule.getId());
        return addCalendarEvent(context, mSchedule);
    }

    private static void showlog(Cursor cursor) {
        if (cursor == null) return;
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            StringBuilder builder = new StringBuilder(cursor.getColumnName(i) + "::");
            switch (cursor.getType(i)) {
                case Cursor.FIELD_TYPE_BLOB:
                    builder.append(cursor.getBlob(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    builder.append(cursor.getFloat(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    builder.append(cursor.getInt(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    builder.append(cursor.getString(i));
                    break;
            }
            Log.i("gong", builder.toString());
        }

    }

    private static Schedule getSchedule(Cursor cursor) {
        int calendar_id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
        int _id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.SELF_ATTENDEE_STATUS));
        int event_color = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.EVENT_COLOR));
        int has_alarm = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.HAS_ALARM));
        int allDay = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ORIGINAL_ALL_DAY));

        long last_date = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.LAST_DATE));
        long startTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
        long endTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));

        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
        String remarks = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
        String address = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
        String rdate = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RDATE));
        String organizer = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.ORGANIZER));
        String tag = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EXDATE));
        if (TextUtils.isEmpty(remarks)) {
            remarks = title;
        }
        Schedule mSchedule = new Schedule(false);
        mSchedule.setId(_id);
        mSchedule.setAllDay(allDay);
        mSchedule.setStartTime(startTime);
        mSchedule.setEndTime(endTime);
        mSchedule.setTitle(title);
        mSchedule.setRemarks(remarks);
        mSchedule.setAddress(address);
        mSchedule.setRepeat(rdate);
        mSchedule.setStatus(organizer);
        mSchedule.setTag(tag);
//        showlog(cursor);
        Log.i("gong", JSON.toJSONString(mSchedule));
        Log.i("gong", "_________________________________");
        return mSchedule;
    }
}
