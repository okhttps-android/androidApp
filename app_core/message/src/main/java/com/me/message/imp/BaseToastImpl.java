package com.me.message.imp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.me.message.interfacepack.BaseToast;
import com.me.message.utils.Utils;

/**
 * 暂时做成工具类，后面调整灵活切换，这里不设置颜色
 * Created by Arison on 2017/5/25.
 * 自定义工具实现，主要是位置，其他默认
 */
public class BaseToastImpl implements BaseToast {
    
    private static BaseToastImpl instance;
    
    public static BaseToastImpl getInstance(){
        if (instance==null){
            synchronized (BaseToastImpl.class){
                if (instance==null){
                    instance=new BaseToastImpl();
                }
            }
        }
        return  instance;
    }
    @Override
    public void show(Context ct, String text) {
        if (ct == null) ct = Utils.getContext();
        Toast.makeText(ct,text,Toast.LENGTH_LONG).show();
    }

    @Override
    public void showAtTop(Context ct, String text) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        //设置Toast的位置
        toast.setGravity(Gravity.TOP, toast.getXOffset()/2, toast.getYOffset()/5);
        toast.show();
    }

    @Override
    public void showAtCenter(Context ct, String text) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        //设置Toast的位置
        toast.setGravity(Gravity.CENTER, toast.getXOffset()/2, toast.getYOffset()/2);
        toast.show();
    }

    @Override
    public void showAtBottom(Context ct, String text) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        //设置Toast的位置
        toast.setGravity(Gravity.BOTTOM, toast.getXOffset()/2, toast.getYOffset()/5);
        toast.show();
    }

    @Override
    public void showAtNotiftion(Context ct, Class<? extends Activity> clazz, String title, String text) {
        if (ct == null) ct = Utils.getContext();
        NotificationManager mNotificationManager = (NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ct.getApplicationContext());
        builder.setSmallIcon(0);
        builder.setLargeIcon(BitmapFactory.decodeResource(ct.getResources(),0)); // 设置下拉列表中的图标(大图标)
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);
        builder.setContentTitle(title);
        builder.setContentText(text);
        Intent intent = new Intent(ct, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(ct.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotificationManager.notify(1000, builder.build());
    }
}
