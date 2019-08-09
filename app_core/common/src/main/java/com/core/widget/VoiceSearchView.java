package com.core.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.core.app.R;
import com.core.interfac.OnVoiceCompleteListener;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by BitLiker on 2017/5/15.
 */

public class VoiceSearchView extends RelativeLayout {
    private ClearEditText search_edit;
    private OnVoiceCompleteListener mOnVoiceCompleteListener;
    private OnEnterActionListener mOnEnterActionListener;

    public VoiceSearchView(Context context) {
        this(context, null);
    }

    public VoiceSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.seach_include, this);
        search_edit = (ClearEditText) findViewById(R.id.search_edit);
        findViewById(R.id.voice_search_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVoice();
            }
        });

        search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (mOnEnterActionListener != null) {
                        mOnEnterActionListener.onEnterAction();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void setHineText(String hine) {
        search_edit.setHint(hine);
    }

    public void setHineText(int hint) {
        search_edit.setHint(hint);
    }

    public void setText(String hine) {
        search_edit.setText(hine);
    }

    public Editable getText() {
        return search_edit.getText();
    }

    private void btnVoice() {
        RecognizerDialog dialog = new RecognizerDialog(getContext(), null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                String result = search_edit.getText().toString() + getPlaintext(text);
                search_edit.setText(result);

                if (mOnVoiceCompleteListener != null && b) {
                    mOnVoiceCompleteListener.onVoiceComplete(result);
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        dialog.show();

    }

    public void setOnVoiceCompleteListener(OnVoiceCompleteListener onVoiceCompleteListener) {
        mOnVoiceCompleteListener = onVoiceCompleteListener;
    }

    public void setOnEnterActionListener(OnEnterActionListener onEnterActionListener) {
        mOnEnterActionListener = onEnterActionListener;
    }

    public void addTextChangedListener(TextWatcher watcher) {
        search_edit.addTextChangedListener(watcher);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener watcher) {
        search_edit.setOnEditorActionListener(watcher);
    }

    public ClearEditText getSearch_edit() {
        return search_edit;
    }

    public void setSearch_edit(ClearEditText search_edit) {
        this.search_edit = search_edit;
    }

    public static String getPlaintext(String stringInfo) {
        Pattern p = Pattern.compile("[.,，？！。\"\\?!:']");//增加对应的标点

        Matcher m = p.matcher(stringInfo);

        String first = m.replaceAll(""); //把英文标点符号替换成空，即去掉英文标点符号

        p = Pattern.compile(" {2,}");//去除多余空格

        m = p.matcher(first);

        String second = m.replaceAll(" ");

        String nulltext = "";
        if (!TextUtils.isEmpty(second)) {
            return second;
        } else {
            return nulltext;
        }
    }

    public interface OnEnterActionListener {
        void onEnterAction();
    }
}