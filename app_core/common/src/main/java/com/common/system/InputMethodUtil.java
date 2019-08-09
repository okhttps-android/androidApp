package com.common.system;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 键盘控制方法
 * Created by Bitliker on 2017/8/10.
 */
public class InputMethodUtil {

    public static void hideInput(Context ct, EditText text_edit) {
        InputMethodManager imm = (InputMethodManager) ct.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text_edit.getWindowToken(), 0);
    }
}
