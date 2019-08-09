package com.modular.apptasks.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;

import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.core.base.BaseActivity;
import com.modular.apptasks.receiver.AutoTaskReceiver;
import com.uas.appworks.model.Schedule;

/**
 * AlarmManager.ELAPSED_REALTIME 表示闹钟在手机睡眠状态下不可用，该状态下闹钟使用相对时间（相对于系统启动开始），状态值为3；
 * AlarmManager.ELAPSED_REALTIME_WAKEUP 表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟也使用相对时间，状态值为2；
 * AlarmManager.RTC 表示闹钟在睡眠状态下不可用，该状态下闹钟使用绝对时间，即当前系统时间，状态值为1；
 * AlarmManager.RTC_WAKEUP 表示闹钟在睡眠状态下会唤醒系统并执行提示功能，该状态下闹钟使用绝对时间，状态值为0；
 * AlarmManager.POWER_OFF_WAKEUP 表示闹钟在手机关机状态下也能正常进行提示功能，所以是5个状态中用的最多的状态之一，该状态下闹钟也是用绝对时间，状态值为4；不过本状态好像受SDK版本影响，某些版本并不支持；
 * Created by Bitliker on 2017/8/7.
 */

public class AlarmUtil {

    /*打卡定时任务*/
    public static final int ID_WORK = 101;
    public static final String ACTION_WORK = "action_work";

    /*外勤定时任务*/
    public static final int ID_MISSION = 102;
    public static final String ACTION_MISSION = "action_mission";

    public static final int ID_SCHEDULE = 103;
    public static final String ACTION_SCHEDULE = "action_schedule";
    public static final String PARCELABLE = "Parcelable";


    public static void startAlarm(int id, String action, long nextAlarmTimeMillis) {
        startAlarm(id, action, nextAlarmTimeMillis, null);
    }

    /**
     * 使用闹钟模式开启定时任务，定时回调
     *
     * @param id                  用于标识，取消
     * @param action              开启任务后回调的action
     * @param nextAlarmTimeMillis 下一次回调任务的时间
     */
    public static void startAlarm(int id, String action, long nextAlarmTimeMillis, Parcelable mParcelable) {
        if (nextAlarmTimeMillis < System.currentTimeMillis() || action == null || action.length() <= 0) {
            return;
        }
        LogUtil.i("nextAlarmTimeMillis=" + DateFormatUtil.long2Str(nextAlarmTimeMillis, DateFormatUtil.YMD_HMS));
        LogUtil.i("action=" + action);
        AlarmManager manager = (AlarmManager) BaseConfig.getContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP
                    , nextAlarmTimeMillis
                    , getPendingIntent(id, action,mParcelable));
//        } else {
//            manager.set(AlarmManager.RTC_WAKEUP
//                    , nextAlarmTimeMillis
//                    , getPendingIntent(id, action,mParcelable));
        }
    }

    public static void cancelAlarm(int id, String action) {
        AlarmManager manager = (AlarmManager) BaseConfig.getContext().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getPendingIntent(id, action, null));
    }

    private static PendingIntent getPendingIntent(int id, String action, Parcelable mParcelable) {
        Intent intent = new Intent(BaseConfig.getContext(), AutoTaskReceiver.class);
        intent.setAction(action);
        if (mParcelable != null) {
            intent.putExtra(PARCELABLE, mParcelable);
            if (mParcelable instanceof Schedule){
                intent.putExtra(BaseActivity.CONTENT,"您的日程["+((Schedule)mParcelable).getRemarks()+"]即将开始");
            }
        }
        return PendingIntent.getBroadcast(BaseConfig.getContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
