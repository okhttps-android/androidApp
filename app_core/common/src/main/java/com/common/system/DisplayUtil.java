package com.common.system;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DisplayUtil {

    public static final int voice_view_max_width = 165;// dp
    public static final int voice_view_min_width = 30;// dp
    public static final float voice_max_length = 30;// 声音最长可以表现为多少毫秒（实际本程序是60s,但是如果这里是60s的话，当时间很短，就没啥差别

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getVoiceViewWidth(Context context, int seconds) {
        if (seconds >= voice_max_length) {
            return dip2px(context, voice_view_max_width);
        }
        final int dpLen = (int) ((seconds / voice_max_length) * (voice_view_max_width - voice_view_min_width)) + voice_view_min_width;
        return dip2px(context, dpLen);
    }


    public static int getSreechWidth(Activity ct) {
        DisplayMetrics metric = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public static void backgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    /*设置添加屏幕的背景透明度*/
    public static void backgroundAlpha(Context context, float bgAlpha) {
        if (context instanceof Activity) {
            backgroundAlpha((Activity) context, bgAlpha);
        }
    }

}
