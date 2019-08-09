package com.me.message.imp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.support.annotation.LayoutRes;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.me.message.interfacepack.CustomizedToast;
import com.me.message.supertoasts.R;
import com.me.message.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by FANGlh on 2017/8/10.
 * function: 自定义工具实现，主要有颜色，动画效果,自定义布局，自定义通知设置图片
 */

public class ToastCustomizedImpl implements CustomizedToast {
    private static ToastCustomizedImpl instance;
    public static ToastCustomizedImpl getInstance(){
        if (instance==null){
            synchronized (ToastCustomizedImpl.class){
                if (instance==null){
                    instance=new ToastCustomizedImpl();
                }
            }
        }
        return  instance;
    }

    @Override
    public void show(Context ct, String text,int messageColor, int backgroundcolor,int duration) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        if (duration == 1)
            toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        else
            toast = Toast.makeText(ct, text, Toast.LENGTH_SHORT);
        View view = toast.getView();
        TextView message=((TextView) view.findViewById(android.R.id.message));
        if (backgroundcolor != -1)
            message.setBackgroundResource(backgroundcolor);
        if (messageColor != -1)
            message.setTextColor(messageColor);
        toast.show();
    }

    @Override
    public void showAtTop(Context ct, String text,int messageColor, int backgroundcolor,int duration) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        if (duration == 1)
            toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        else
            toast = Toast.makeText(ct, text, Toast.LENGTH_SHORT);
        //设置Toast的位置
        toast.setGravity(Gravity.TOP, toast.getXOffset()/2, toast.getYOffset()/5);
        View view = toast.getView();
        TextView message=((TextView) view.findViewById(android.R.id.message));
        if (backgroundcolor != -1)
            message.setBackgroundResource(backgroundcolor);
        if (messageColor != -1)
            message.setTextColor(messageColor);
        toast.show();
    }

    @Override
    public void showAtCenter(Context ct, String text,int messageColor, int backgroundcolor,int duration) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        if (duration == 1)
            toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        else
            toast = Toast.makeText(ct, text, Toast.LENGTH_SHORT);
        //设置Toast的位置
        toast.setGravity(Gravity.CENTER, toast.getXOffset()/2, toast.getYOffset()/2);

        View view = toast.getView();
        TextView message=((TextView) view.findViewById(android.R.id.message));
        if (backgroundcolor != -1)
            message.setBackgroundResource(backgroundcolor);
        if (messageColor != -1)
            message.setTextColor(messageColor);
        toast.show();
    }

    @Override
    public void showAtBottom(Context ct, String text,int messageColor, int backgroundcolor,int duration) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        if (duration == 1)
            toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        else
            toast = Toast.makeText(ct, text, Toast.LENGTH_SHORT);
        //设置Toast的位置
        toast.setGravity(Gravity.BOTTOM, toast.getXOffset()/2, toast.getYOffset()/5);
        View view = toast.getView();
        TextView message=((TextView) view.findViewById(android.R.id.message));
        if (backgroundcolor != -1)
            message.setBackgroundResource(backgroundcolor);
        if (messageColor != -1)
            message.setTextColor(messageColor);
//        message.setTextSize(18);
        toast.show();
    }

    @Override
    public void showAtNotiftion(Context ct,Class<? extends Activity> clazz, String title, String text, int pictureid) {
        if (ct == null) ct = Utils.getContext();
        NotificationManager mNotificationManager = (NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ct.getApplicationContext());
        builder.setSmallIcon(pictureid);
        builder.setLargeIcon(BitmapFactory.decodeResource(ct.getResources(),pictureid)); // 设置下拉列表中的图标(大图标)
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setAutoCancel(true);
        builder.setContentTitle(title);
        builder.setContentText(text);
        Intent intent = new Intent(ct, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(ct.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotificationManager.notify(1000, builder.build());
    }

    @Override
    public void showCuslayoutToast(Context ct, String text,@LayoutRes int resource,int location) {
        if (ct == null) ct = Utils.getContext();
        View toastRoot = LayoutInflater.from(ct).inflate(resource, null);
        Toast toast=new Toast(ct.getApplicationContext());
        toast.setGravity(location, toast.getXOffset()/2, toast.getYOffset()/5);
        toast.setView(toastRoot);
        TextView tv=(TextView)toastRoot.findViewById(R.id.TextViewInfo);  //TODO 这里的控件监听事件和传统一致，但是得单独写，不能用工具类实现
        tv.setText(text);
        toast.show();
    }

    @Override
    public void showWithPicuure(Context ct, String text, int pictureid, int duration, int messageColor,int location) {
        if (ct == null) ct = Utils.getContext();
        Toast toast = null;
        if (duration == 1)
            toast = Toast.makeText(ct, text, Toast.LENGTH_LONG);
        else
            toast = Toast.makeText(ct, text, Toast.LENGTH_SHORT);
        toast.setGravity(location, 0, 0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(ct);
        imageCodeProject.setImageResource(pictureid);
        toastView.addView(imageCodeProject, 0);
        View view = toast.getView();
        TextView message=((TextView) view.findViewById(android.R.id.message));
        if (messageColor != -1)
            message.setTextColor(messageColor);
        toast.show();
    }

    @Override
    public void showMiuiText(Context context, String text, int duration,int location) {

        final WindowManager mWdm;
        final View mToastView;
        WindowManager.LayoutParams mParams;
        Timer mTimer;
        boolean mShowTime;//记录Toast的显示长短类型
        final boolean[] mIsShow = new boolean[1];//记录当前Toast的内容是否已经在显示
        if (duration == 1)
            mShowTime = true;//记录Toast的显示长短类型
        else
            mShowTime = false;
        mIsShow[0] = false;//记录当前Toast的内容是否已经在显示
        mWdm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //通过Toast实例获取当前android系统的默认Toast的View布局
        mToastView = Toast.makeText(context, text, Toast.LENGTH_SHORT).getView();
        mTimer = new Timer();

        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.windowAnimations = R.style.anim_view;//设置进入退出动画效果
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mParams.gravity = location;
        mParams.y = 250;


        if(!mIsShow[0]){//如果Toast没有显示，则开始加载显示
            mIsShow[0] = true;
            mWdm.addView(mToastView, mParams);//将其加载到windowManager上
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mWdm.removeView(mToastView);
                    mIsShow[0] = false;
                }
            }, (long)(mShowTime ? 3500 : 2000));
        }

        mToastView.showContextMenu();
    }


}
