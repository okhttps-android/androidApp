package com.core.utils;

import android.content.Context;

import com.core.app.MyApplication;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

/**
 * 语音输入提示框
 * Created by Bitliker on 2017/6/12.
 */

public class RecognizerDialogUtil {

    public static void showRecognizerDialog(Context context, RecognizerDialogListener recognizerDialogListener) {
        if (context == null)
            context = MyApplication.getInstance();
        RecognizerDialog dialog = new RecognizerDialog(context, null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setListener(recognizerDialogListener);
        dialog.show();
    }
}
