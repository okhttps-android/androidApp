package com.uas.appworks.widget;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.core.widget.listener.EditChangeListener;
import com.uas.appworks.OA.platform.model.CharitModel;
import com.uas.appworks.R;

/**
 * Created by Bitlike on 2017/11/13.
 */

public class SelectPlayPop {

    public static void showPlay(final Activity ct, CharitModel model, OnSureListener onSureListener) {
        if (model == null) return;
        final PopupWindow window = new PopupWindow(ct);
//		window.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View view = LayoutInflater.from(ct).inflate(R.layout.pop_play_select, null);
        window.setContentView(view);
        window.getContentView().measure(0, 0);
        int height = DisplayUtil.sp2px(ct, 280);
        window.setHeight(height);
        window.setWidth(DisplayUtil.getSreechWidth(ct));
        initView(ct, window, view, model, onSureListener);
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.color.background));
        DisplayUtil.backgroundAlpha(ct, 0.4f);
        window.setTouchable(true);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setAnimationStyle(R.style.MenuAnimationFade);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
        window.showAtLocation(view, Gravity.CENTER, 0, height);
    }


    private static void initView(final Context ct, final PopupWindow window, View view, final CharitModel model, final OnSureListener onSureListener) {
        final EditText autoNumEd = (EditText) view.findViewById(R.id.autoNumEd);
        final TextView agreeNoteTv = (TextView) view.findViewById(R.id.agreeNoteTv);
        final TextView payNumTv = (TextView) view.findViewById(R.id.payNumTv);
        final Button giftBtn = (Button) view.findViewById(R.id.giftBtn);
        final CheckBox agreeCb = (CheckBox) view.findViewById(R.id.agreeCb);
        final RadioGroup selectRg = (RadioGroup) view.findViewById(R.id.selectRg);
        final RadioGroup typeRg = (RadioGroup) view.findViewById(R.id.typeRg);
        final RadioButton wxRb = (RadioButton) view.findViewById(R.id.wxRb);
        final RadioButton aliPlayRb = (RadioButton) view.findViewById(R.id.aliPlayRb);
        payNumTv.setText(StringUtil.getMessage(R.string.new_select_pay) + " " + 1 + "元");
        payNumTv.setTag(R.id.tag_key, 1d);
        autoNumEd.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                try {

                    String payStr = null;
                    if (editable != null) {
                        payStr = editable.toString();
                        if (payStr.length() > 0) {
                            selectRg.setTag(R.id.tag_key, "被动");
                            selectRg.clearCheck();
                        }
                    }
                    if (StringUtil.isEmpty(payStr)) {
                        payNumTv.setText("");
                        payNumTv.setTag(R.id.tag_key, -1d);
                    } else {
                        double num = Double.valueOf(payStr);
                        if (num > 99999) {
                            payNumTv.setTag(R.id.tag_key, -1d);
                            payNumTv.setText(R.string.not_more_pay);
                        } else {
                            payNumTv.setTag(R.id.tag_key, num);
                            payNumTv.setText(StringUtil.getMessage(R.string.new_select_pay) + " " + num);
                            int dd = payStr.indexOf(".");
                            int ll = payStr.length();
                            if (dd != -1) {
                                int position = ll - dd - 1;
                                if (position > 2) {
                                    payNumTv.setText(R.string.input_two_double);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.i("Exception e=" + e.getMessage());

                }
            }
        });
        selectRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Double num = 0d;
                if (i == R.id.oneRb) {
                    num = 1d;
                } else if (i == R.id.twoRb) {
                    num = 10d;
                } else if (i == R.id.fiveRb) {
                    num = 50d;
                } else if (i == R.id.tenRb) {
                    num = 100d;
                }

                if (num > 0) {
                    if (selectRg.getTag(R.id.tag_key) == null) {
                        autoNumEd.setText("");
                    } else {
                        selectRg.setTag(R.id.tag_key, null);
                    }
                    payNumTv.setTag(R.id.tag_key, num);
                    payNumTv.setText(StringUtil.getMessage(R.string.new_select_pay) + " " + num + "元");
                }
            }
        });
        typeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                hideKeyboard(ct, autoNumEd);
//                if (i == R.id.wxRb) {
//                    showNotLocation(ct, "暂不支持微信支付");
//                    aliPlayRb.setChecked(true);
//                    wxRb.setChecked(false);
//                }
            }
        });
        agreeNoteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentUtils.linkCommonWeb(ct, "http://lj.ubtob.com/mobile#/userAgreement");
            }
        });
        agreeCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    giftBtn.setBackgroundResource(R.drawable.ind_pass_btn_bg);
                    giftBtn.setFocusable(true);
                    giftBtn.setClickable(true);
                } else {
                    giftBtn.setBackgroundResource(R.color.hintColor);
                    giftBtn.setFocusable(false);
                    giftBtn.setClickable(false);
                }
            }
        });
        agreeCb.setChecked(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(ct, autoNumEd);
            }
        });

        giftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(ct, autoNumEd);
                if (StringUtil.getMessage(R.string.input_two_double).equals(payNumTv.getText().toString())) {
                    showNotLocation(ct, StringUtil.getMessage(R.string.input_two_double));
                    return;
                }
                if (agreeCb.isChecked()) {
                    boolean payAble = giftBtn(window, payNumTv, typeRg, model, onSureListener);
                    if (!payAble) {
                        ToastUtil.showToast(ct, R.string.not_pay_num_can);
                    }
                }
            }
        });
        view.findViewById(R.id.closeIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
    }

    /*隐藏软键盘并显示头布局*/
    private static void hideKeyboard(Context context, EditText chatEd) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(chatEd.getWindowToken(), 0);
    }

    private static synchronized boolean giftBtn(PopupWindow window, TextView numTv, RadioGroup typeRg, CharitModel model, OnSureListener onSureListener) {
        double num = 0;
        if (numTv != null) {
            num = (double) numTv.getTag(R.id.tag_key);
        }
        if (num <= 0) {
            return false;
        } else {
            int typeId = typeRg.getCheckedRadioButtonId();
            int type = Constants.FLAG.API_PAY;
            if (typeId == R.id.wxRb) {
                type = Constants.FLAG.WEIXIN_PAY;
            }
            if (onSureListener != null) {
                onSureListener.sure(num, type, model);
            }
            if (window != null && window.isShowing()) {
                window.dismiss();
            }
            return true;
        }
    }


    public interface OnSureListener {
        void sure(double num, int type, CharitModel model);
    }


    private static void showNotLocation(Context context, String message) {

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialog_not_hite_bg);
//            builder.setTitle(R.string.prompt_title)
//                    .setMessage("暂不支持微信支付")
//                    .setNegativeButton(R.string.common_sure, null).show();
//
//        } else {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.prompt_title).
                        content(message)
                .positiveText(R.string.sure).
                        autoDismiss(false).callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }
                }).build();
        dialog.show();
//        }

    }
}
