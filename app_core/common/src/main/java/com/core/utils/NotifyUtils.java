package com.core.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.common.data.StringUtil;
import com.core.widget.crouton.Crouton;

/**
 * Created by Arison on 2017/8/28.
 */

public class NotifyUtils {


    public static Crouton crouton;

    /**
     * 弹出Toast消息
     * <p>
     * update for LiuJie
     *
     * @param msg
     * @author RaoMeng
     */
    public static void ToastMessage(Context cont, String msg) {
        if (StringUtil.isEmpty(msg)) return;
        if (cont instanceof Activity) {
            ToastUtil.showToast(cont,msg);

//            if (crouton != null) {
//                crouton.cancel();
//                crouton = Crouton.makeText((Activity) cont, msg, Style.holoGreenLight, 1000,);
//                crouton.show();
//            } else {
//                crouton = Crouton.makeText((Activity) cont, msg, Style.holoGreenLight, 1000);
//                crouton.show();
//            }
        } else {
            Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
        }

    }
}
