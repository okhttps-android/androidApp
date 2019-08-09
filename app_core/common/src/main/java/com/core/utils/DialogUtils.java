package com.core.utils;

import android.app.Dialog;
import android.widget.PopupWindow;


public class DialogUtils {
    
    public static boolean isDialogShowing(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            return true;
        }
        return false;
    }

    public static boolean isDialogShowing(PopupWindow dialog) {
        if (dialog != null && dialog.isShowing()) {
            return true;
        }
        return false;
    }
}
