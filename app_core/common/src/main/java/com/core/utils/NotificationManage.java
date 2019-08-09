package com.core.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.app.R;

/**
 * 通知工具类
 * Created by pengminggong on 2016/10/27.
 */
public class NotificationManage {
    private long minutes2time = 2 * 60000;

    /**
     * 显示通知栏
     *
     * @param context
     * @param icon    标题图标资源id
     * @param title   标题
     * @param content 内容
     * @param clazz   点击跳转的界面
     */
    private void sendNotification(Context context, int icon, String title, String content, Class<? extends Activity> clazz) {
        sendNotification(context, icon, title, null, content, clazz);
    }

    private void sendNotification(Context context, int icon, String title, String group, String content, Class<? extends Activity> clazz) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext());
//        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setTicker(content);
        builder.setSmallIcon(icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.uuu));
        if (!StringUtil.isEmpty(group))
            builder.setGroup(group);
        Intent intent = new Intent(context, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotificationManager.notify(1000, builder.build());
    }


    public void sendNotification(Context context, int action, Class<? extends Activity> clazz) {
        SharedPreferences sharePrefer = context.getSharedPreferences("SIGNIN", Context.MODE_APPEND);
        long oldTime = sharePrefer.getLong("SIGNIN_TIME", -1);
        if (System.currentTimeMillis() - oldTime > minutes2time) {//如果当前时间大于上次时间2分钟
            sharePrefer.edit().putLong("SIGNIN_TIME", System.currentTimeMillis());
            sendNotification(context, R.drawable.uuu, context.getString(R.string.common_dialog_title), context.getResources().getString(action), clazz);
        } else {
            //无效
        }
    }

    public void sendNotification(Context context, String action, Class<? extends Activity> clazz) {
        SharedPreferences sharePrefer = MyApplication.getInstance().getSharedPreferences("SIGNIN", Context.MODE_APPEND);
        long oldTime = sharePrefer.getLong("SIGNIN_TIME", -1);
        if (System.currentTimeMillis() - oldTime > minutes2time) {//如果当前时间大于上次时间2分钟
            sharePrefer.edit().putLong("SIGNIN_TIME", System.currentTimeMillis());
            sendNotification(context, R.drawable.uuu, context.getString(R.string.common_dialog_title), action, clazz);
        }
    }

    //update
    public static void sendUUHelperNotif(String content) {
        Context context = MyApplication.getInstance().getApplicationContext();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext());
        builder.setSmallIcon(R.drawable.uuu);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.uuu));
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);
        builder.setContentTitle("UU小助手");
        builder.setContentText(content);
        builder.setGroup("UU小助手");
        Intent intent = new Intent("com.modular.appmessage.UUHelperActivity");
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotificationManager.notify(1000, builder.build());
        //发送本地广播
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AppConstant.UPDATA_UUHELPER));

    }


}
