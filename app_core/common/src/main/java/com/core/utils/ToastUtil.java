package com.core.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.core.app.R;
import com.core.base.BaseToolBarActivity;
import com.core.base.SupportToolBarActivity;
import com.core.widget.crouton.Crouton;
import com.core.widget.crouton.Style;

public class ToastUtil {

    /**
     * 设置EditText的字数限制
     *
     * @param mTextEdit
     * @param maxTextNum 最大字符数
     */
    public static void addEditTextNumChanged(final Context context, final EditText mTextEdit, final int maxTextNum) {

        mTextEdit.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private boolean isEdit = true;
            private int selectionStart;
            private int selectionEnd;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                selectionStart = mTextEdit.getSelectionStart();
                selectionEnd = mTextEdit.getSelectionEnd();
                Log.i("gongbiao1", "" + selectionStart);
                if (temp.length() > maxTextNum) {
                    Toast toast = Toast.makeText(context, "只能输入" + maxTextNum + "个字符哦", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    TextView tv = new TextView(context);
                    tv.setText("只能输入" + maxTextNum + "个字符哦");
                    tv.setTextColor(Color.RED);
                    toast.setView(tv);
                    toast.show();
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionStart;
                    mTextEdit.setText(s);
                    mTextEdit.setSelection(tempSelection);
                }
            }
        });
    }

    public static void showErrorNet(Context context) {
        if (context == null) {
            return;
        }
        showToast(context, R.string.net_exception);
    }

    public static void showErrorData(Context context) {
        if (context == null) {
            return;
        }
        showToast(context, R.string.data_exception);
    }

    public static void showToast(Context context, CharSequence message) {
        if (context == null || TextUtils.isEmpty(message)) {
            return;
        }
        ViewGroup vg = null;
        if (context instanceof SupportToolBarActivity) {
            vg = ((SupportToolBarActivity) context).getContentFl();
        }else if (context instanceof BaseToolBarActivity) {
            vg = ((BaseToolBarActivity) context).getContentFl();
        }
        if (vg != null) {
            showToast(context, message, vg);
        } else {
           Toast.makeText(context, message,Toast.LENGTH_SHORT).show();
        }


    }

    public static void showToast(Context context, int resId) {
        if (context == null) {
            return;
        }
        showToast(context,context.getString(resId));
    }

    public static void showToast(Context context, CharSequence message, ViewGroup viewGroup) {
        if (context == null || TextUtils.isEmpty(message)) {
            return;
        }
        Crouton.makeText((Activity) context, message, Style.holoGreenLight, 2000, viewGroup).show();
    }

    public static void showToast(Context context, int resId, ViewGroup viewGroup) {
        if (context == null) {
            return;
        }
        Crouton.makeText((Activity) context, context.getString(resId), Style.holoGreenLight, 2000, viewGroup).show();
    }

}
