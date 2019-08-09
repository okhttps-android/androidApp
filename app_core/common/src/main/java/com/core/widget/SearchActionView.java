package com.core.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.app.R;
import com.core.utils.CommonUtil;

/**
 * @author RaoMeng
 * @describe 带搜索框以及右侧菜单的actionview
 * @date 2017/12/22 10:17
 */

public class SearchActionView extends FrameLayout {
    private ImageView mBackImageView, mMenuImageView, mVoiceImageView;
    private OnVoiceCompleteListener mOnVoiceCompleteListener;
    private OnEnterActionListener mOnEnterActionListener;
    private OnMenuClickListener mOnMenuClickListener;
    private OnTextChangedListener mOnTextChangedListener;
    private ClearEditText mSearchEditText;

    public void setOnVoiceCompleteListener(OnVoiceCompleteListener onVoiceCompleteListener) {
        mOnVoiceCompleteListener = onVoiceCompleteListener;
    }

    public void setOnEnterActionListener(OnEnterActionListener onEnterActionListener) {
        mOnEnterActionListener = onEnterActionListener;
    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    public void setOnTextChangedListener(OnTextChangedListener onTextChangedListener) {
        mOnTextChangedListener = onTextChangedListener;
    }

    public void setMenuImage(int menuImage) {
        try {
            mMenuImageView.setImageResource(menuImage);
            mMenuImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnMenuClickListener != null) {
                        mOnMenuClickListener.onMenuClick(view);
                    }
                }
            });
        } catch (Exception e) {

        }

    }

    public SearchActionView(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public SearchActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SearchActionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_search_action, this);

        mBackImageView = (ImageView) findViewById(R.id.search_action_back_iv);
        mMenuImageView = (ImageView) findViewById(R.id.search_action_menu_iv);
        mVoiceImageView = (ImageView) findViewById(R.id.search_action_voice_iv);
        mSearchEditText = (ClearEditText) findViewById(R.id.search_action_search_et);

        mBackImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() != null) {
                    ((Activity) getContext()).finish();
                    ((Activity) getContext()).overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
                }
            }
        });

        mVoiceImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.getVoiceText(context, mSearchEditText, new com.core.interfac.OnVoiceCompleteListener() {
                    @Override
                    public void onVoiceComplete(String text) {
                        if (mOnVoiceCompleteListener != null) {
                            mOnVoiceCompleteListener.onVoiceComplete(text);
                        }
                    }
                });
            }
        });

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mOnTextChangedListener != null) {
                    mOnTextChangedListener.onTextChanged(editable.toString());
                }
            }
        });
    }

    public void setSearchHint(String hint) {
        mSearchEditText.setHint(hint);
    }

    public void setSearchHint(int hint) {
        mSearchEditText.setHint(hint);
    }

    public String getText() {
        return mSearchEditText.getText().toString();
    }

    public interface OnVoiceCompleteListener {
        void onVoiceComplete(String text);
    }

    public interface OnEnterActionListener {
        void onEnterAction();
    }

    public interface OnMenuClickListener {
        void onMenuClick(View view);
    }

    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }
}
