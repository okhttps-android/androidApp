package com.modular.apputils.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.modular.apputils.R;

import java.io.Serializable;

public class VeriftyDialog extends DialogFragment {
    private final String KEY_TITLE = "title";
    private final String KEY_CONTENT = "content";
    private final String KEY_CANCEL_TEXT = "cancelText";
    private final String KEY_SURE_TEXT = "sureText";
    private final String KEY_GRAVITY = "gravity";
    private final String KEY_CANCELED_ON_TOUCH_OUTSIDE = "canceledOnTouchOutside";
    private final String KEY_LISTENER = "Listener";
    private final String KEY_ANIMATIONS_STYLE = "AnimationsStyle";
    private final String KEY_DIALOG_STYLE = "DialogStyle";
    private final String KEY_SHOW_CANCEL = "showCancel";

    private TextView titleTv;
    private TextView messageTv;
    private TextView cancelTv;
    private TextView sureTv;
    private OnDialogClickListener dialogClickListener;


    public static VeriftyDialog newInstance(Bundle args) {
        VeriftyDialog fragment = new VeriftyDialog();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            //设置弹框的占屏宽
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.common_prompt_dialog, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleTv = (TextView) view.findViewById(R.id.title_tv);
        messageTv = (TextView) view.findViewById(R.id.message_tv);
        cancelTv = (TextView) view.findViewById(R.id.goto_tv);
        sureTv = (TextView) view.findViewById(R.id.sure_tv);
        initView();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int animationsStyle = -1;
        int dialogStyle = -1;
        int gravity = -1;
        boolean canceledOnTouchOutside = true;
        Bundle args = getArguments();
        if (args != null) {
            gravity = args.getInt(KEY_GRAVITY, -1);
            animationsStyle = args.getInt(KEY_ANIMATIONS_STYLE, -1);
            dialogStyle = args.getInt(KEY_DIALOG_STYLE, -1);
            canceledOnTouchOutside = args.getBoolean(KEY_CANCELED_ON_TOUCH_OUTSIDE, true);
        }
        Dialog dialog = new Dialog(getActivity(), dialogStyle == -1 ? R.style.PromptDialogStyle : dialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.setCancelable(canceledOnTouchOutside);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_round_bg));
        if (animationsStyle > 0) {
            dialog.getWindow().setWindowAnimations(animationsStyle);
        }
        Window window = dialog.getWindow();
        window.setGravity(gravity);
        return dialog;
    }

    private void initView() {
        String title = getString(R.string.app_name);
        String content = "";
        String cancelText = getString(R.string.common_cancel);
        String sureText = getString(R.string.common_sure);
        boolean showCancel = true;
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(KEY_TITLE, title);
            content = args.getString(KEY_CONTENT, content);
            cancelText = args.getString(KEY_CANCEL_TEXT, cancelText);
            sureText = args.getString(KEY_SURE_TEXT, sureText);
            showCancel = args.getBoolean(KEY_SHOW_CANCEL, true);
            if (args.getSerializable(KEY_LISTENER) != null && args.getSerializable(KEY_LISTENER) instanceof OnDialogClickListener) {
                dialogClickListener = (OnDialogClickListener) args.getSerializable(KEY_LISTENER);
            }
        }
        titleTv.setText(title);
        messageTv.setText(content);
        sureTv.setText(sureText);
        if (showCancel) {
            cancelTv.setVisibility(View.VISIBLE);
            cancelTv.setOnClickListener(mOnClickListener);
            cancelTv.setText(cancelText);
        } else {
            cancelTv.setVisibility(View.GONE);
        }
        sureTv.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (dialogClickListener != null) {
                dialogClickListener.result(view == sureTv);
            }
            dismiss();
        }
    };

    public static class Builder {

        private final String KEY_SHOW_CANCEL = "showCancel";
        private final String KEY_TITLE = "title";
        private final String KEY_CONTENT = "content";
        private final String KEY_CANCEL_TEXT = "cancelText";
        private final String KEY_SURE_TEXT = "sureText";
        private final String KEY_GRAVITY = "gravity";
        private final String KEY_CANCELED_ON_TOUCH_OUTSIDE = "canceledOnTouchOutside";
        private final String KEY_LISTENER = "Listener";
        private final String KEY_ANIMATIONS_STYLE = "AnimationsStyle";
        private final String KEY_DIALOG_STYLE = "DialogStyle";

        private String title;
        private String content;
        private String cancelText;
        private String sureText;
        private int gravity;
        private int animationsStyle;
        private int dialogStyle;
        private boolean canceledOnTouchOutside;
        private boolean showCancel;
        private Context ct;


        public Builder(Context ct) {
            this.ct = ct;
            this.showCancel = true;
            this.canceledOnTouchOutside = true;
        }

        public Builder setShowCancel(boolean showCancel) {
            this.showCancel = showCancel;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder setDialogStyle(int dialogStyle) {
            this.dialogStyle = dialogStyle;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setAnimationsStyle(int animationsStyle) {
            this.animationsStyle = animationsStyle;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setCancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }

        public Builder setSureText(String sureText) {
            this.sureText = sureText;
            return this;
        }

        public VeriftyDialog build(OnDialogClickListener mOnClickListener) {
            Bundle args = new Bundle();
            args.putString(KEY_TITLE, title);
            args.putString(KEY_CONTENT, content);
            args.putString(KEY_CANCEL_TEXT, cancelText);
            args.putString(KEY_SURE_TEXT, sureText);
            args.putString(KEY_SURE_TEXT, sureText);
            args.putBoolean(KEY_CANCELED_ON_TOUCH_OUTSIDE, canceledOnTouchOutside);
            args.putInt(KEY_GRAVITY, gravity);
            args.putInt(KEY_ANIMATIONS_STYLE, animationsStyle);
            args.putInt(KEY_DIALOG_STYLE, dialogStyle);
            args.putBoolean(KEY_SHOW_CANCEL, showCancel);
            if (mOnClickListener != null) {
                args.putSerializable(KEY_LISTENER, mOnClickListener);
            }
            VeriftyDialog mVeriftyDialog = VeriftyDialog.newInstance(args);
            if (ct != null && ct instanceof FragmentActivity) {
                mVeriftyDialog.show(((FragmentActivity) ct).getSupportFragmentManager(), "prompt");
            }
            return mVeriftyDialog;
        }


    }

    public interface OnDialogClickListener extends Serializable {
        void result(boolean clickSure);
    }
}