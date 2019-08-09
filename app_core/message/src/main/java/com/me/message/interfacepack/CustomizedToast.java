package com.me.message.interfacepack;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;

/**
 * 统一规范消息组件的调用
 * 方向：底部，中部，顶部
 * 监听事件:
 * 样式：
 * 第一步：构建指定参数的公共接口
 * 第二步：通过builder模式传入大量自定义化参数的接口
 * Created by Arison on 2017/5/25.
 * messageColor:字体颜色，不设置传-1
 * backgroundcolor：字体背景颜色，不设置传-1
 */
public interface CustomizedToast {
    /**
      * @desc:Activity 生命周期绑定的消息提示，与Activity生命周期无关则传null
      * @author：FANGLH
      */


    /**
     * 自定义的的Toast, 圆角型
     * Context：可传可不传
     * text：toast的内容
     * messageColor：字体颜色 不设置传-1
     * backgroundcolor：字体背景的颜色 不设置传-1
     * duration：toast的时间长短 1：LONG，else：SHORT
     */
    void show(Context ct, String text, int messageColor, int backgroundcolor, int duration);
    void showAtTop(Context ct, String text, int messageColor, int backgroundcolor, int duration);
    void showAtCenter(Context ct, String text, int messageColor, int backgroundcolor, int duration);
    void showAtBottom(Context ct, String text, int messageColor, int backgroundcolor, int duration);


    /**
     * 自定义的通知栏提醒
     * @param ct
     * @param clazz
     * @param title
     * @param text
     * @param pictureid  通知栏显示的图片，不设置默认则传入null
     */
    void showAtNotiftion(Context ct, Class<? extends Activity> clazz, String title, String text, int pictureid);


    /**
     * 自定义布局Toast，只做显示，暂不加入点击事件
     * @param ct
     * @param text
     * @param resource   自定义的布局 ：R.layout.xxx
     * @param location  Toast弹出的位置：Gravity.BOTTOM
     */
    void showCuslayoutToast(Context ct, String text, @LayoutRes int resource, int location);

    /**
     * 简单的图片+text Toast 方式
     * @param ct
     * @param text
     * @param pictureid
     * @param duration
     * @param messageColor
     * @param location
     */
    void showWithPicuure(Context ct, String text, int pictureid, int duration, int messageColor, int location);

    /**
     * 自定义动画 支持底部弹出，自下而上。
     * @param ct
     * @param text
     * @param duration
     * @param location
     */
    void showMiuiText(Context ct, String text, int duration, int location);
}