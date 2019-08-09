package com.me.message.interfacepack;

import android.app.Activity;
import android.content.Context;

/**
 * 统一规范消息组件的调用
 * 方向：底部，中部，顶部
 * 监听事件:
 * 样式：
 * 第一步：构建指定参数的公共接口
 * 第二步：通过builder模式传入大量自定义化参数的接口
 */
public interface BaseToast {
    /**
     *  与Activity生命周期无关，则传null
      * @desc:Activity 生命周期绑定的消息提示
      * @author：FANGLH
      */


    /**
     *最Base的Toast,默认的位置 duration == 1 ,默认的方形
     * Context：可传可不传
     * text：toast的内容
     */
    void show(Context ct, String text);
    void showAtTop(Context ct, String text);
    void showAtCenter(Context ct, String text);
    void showAtBottom(Context ct, String text);
    void showAtNotiftion(Context ct, Class<? extends Activity> clazz, String title, String text);

    /**
     * 自定义布局
     */

}