
package com.core.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.app.R;


/**
 * @author :Arison 2015年11月16日 上午11:39:52
 * @注释:自定义的进度条对话框
 */
public class CustomProgressDialog extends Dialog {
    @SuppressWarnings("unused")
    private Context context = null;
    private static CustomProgressDialog customProgressDialog = null;

    public CustomProgressDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void show() {
        if (this != null)
            try {
                super.show();
            } catch (Exception e) {
                Log.i("gongpengming", "Exception =" + (e == null ? "空的" : e.getMessage()));
            }
    }

    @Override
    public void dismiss() {
        if (this != null)
            try {
                super.dismiss();
            } catch (Exception e) {
                Log.i("gongpengming", "Exception =" + (e == null ? "空的" : e.getMessage()));
            }
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static CustomProgressDialog createDialog(Context context) {
        customProgressDialog = new CustomProgressDialog(context, R.style.CustomProgressDialog);
        customProgressDialog.setContentView(R.layout.customprogressdialog);
        customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        //customProgressDialog.setCancelable(false);
        return customProgressDialog;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (customProgressDialog == null) {
            return;
        }
        ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.loadingImageView);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
    }

    public CustomProgressDialog setTitile(String strTitle) {
        return customProgressDialog;
    }

    public CustomProgressDialog setMessage(String strMessage) {
        TextView tvMsg = (TextView) customProgressDialog.findViewById(R.id.id_tv_loadingmsg);
        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }
        return customProgressDialog;
    }
}
