package com.modular.apputils.utils;

import android.app.Activity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.MyApplication;
import com.core.widget.view.model.SelectAimModel;
import com.modular.apputils.R;

/**
 * Created by Bitliker on 2017/5/3.
 */

public class PopupWindowHelper {

    public static void showAlart(final Activity ct, String title, String message, String sureText, final OnSelectListener listener) {
        final PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(R.layout.item_select_alert_pop, null);
        window.setContentView(view);
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.pop_round_bg));
        DisplayUtil.backgroundAlpha(ct, 0.4f);
        window.setTouchable(true);
        setPopupWindowHW(ct, window);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        TextView title_tv = (TextView) view.findViewById(R.id.title_tv);
        TextView message_tv = (TextView) view.findViewById(R.id.message_tv);
        TextView sure_tv = (TextView) view.findViewById(R.id.sure_tv);
        title_tv.setText(StringUtil.isEmpty(title) ? ct.getString(R.string.common_dialog_title) : title);
        message_tv.setText(StringUtil.isEmpty(message) ? "" : message);
        if (!TextUtils.isEmpty(sureText)) {
            sure_tv.setText(sureText);
        }
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
        view.findViewById(R.id.goto_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.select(false);
                window.dismiss();
            }
        });
        sure_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.select(true);
                window.dismiss();
            }
        });
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public static void showAlart(final Activity ct, String title, String message, final OnSelectListener listener) {
        showAlart(ct, title, message, null, listener);
    }

    /*选择地址后回调修改窗口*/
    public static void create(Activity ct, String title, SelectAimModel model, OnClickListener onClickListener) {
        create(ct, title, model, onClickListener, null);
    }

    public static PopupWindow create(Activity ct, String title, SelectAimModel model, OnClickListener onClickListener, OnFindLikerListener onFindLikerListener) {
        PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(R.layout.item_select_aim_pop, null);
        window.setContentView(view);
        setPopupWindowHW(ct, window);
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.pop_round_bg));
        DisplayUtil.backgroundAlpha(ct, 0.4f);
        viewEvent(ct, window, view, title, model, onClickListener, onFindLikerListener);
        window.setTouchable(true);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        return window;
    }

    private static void viewEvent(final Activity ct,
                                  final PopupWindow window,
                                  View view,
                                  String title,
                                  final SelectAimModel model,
                                  final OnClickListener onClickListener,
                                  final OnFindLikerListener onFindLikerListener
    ) {
        TextView title_tv = (TextView) view.findViewById(R.id.title_tv);
        final EditText company_et = (EditText) view.findViewById(R.id.company_et);
        TextView address_tv = (TextView) view.findViewById(R.id.address_tv);
        title = StringUtil.isEmpty(title) ? ct.getString(R.string.perfect_company_name) : title;
        String company = (model == null || StringUtil.isEmpty(model.getName())) ? "" : model.getName();
        String address = (model == null || StringUtil.isEmpty(model.getAddress())) ? "" : model.getAddress();
        title_tv.setText(title);
        company_et.setText(company);
        Editable etext = company_et.getText();
        Selection.setSelection(etext, etext.length());
        address_tv.setText(MyApplication.getInstance().getString(R.string.detailed_address) + "  " + address);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
        if (onFindLikerListener != null) {
            view.findViewById(R.id.select_erp).setVisibility(View.VISIBLE);
            view.findViewById(R.id.select_erp).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onFindLikerListener != null) {
                        onFindLikerListener.click(StringUtil.getTextRexHttp(company_et));
                    }
                }
            });
        } else {
            view.findViewById(R.id.select_erp).setVisibility(View.GONE);
        }
        view.findViewById(R.id.goto_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBack(null, model, onClickListener);
                window.dismiss();
            }
        });
        view.findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String company = company_et.getText().toString();
                setBack(company, model, onClickListener);
                window.dismiss();
            }
        });

    }

    private static void setBack(String company, SelectAimModel model, OnClickListener onClickListener) {
        if (model == null) {
            model = new SelectAimModel();
            model.setAddress("");
        }
        if (!StringUtil.isEmpty(company))
            model.setName(company);
        if (onClickListener != null)
            onClickListener.result(model);
    }

    private static int getWidth(Activity ct) {
        DisplayMetrics dm = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) (dm.widthPixels * (0.8));
    }

    public static void setPopupWindowHW(PopupWindow window) {
        window.getContentView().measure(0, 0);
        window.setHeight(window.getContentView().getMeasuredHeight() + 30);
        window.setWidth(window.getContentView().getMeasuredWidth() + DisplayUtil.dip2px(MyApplication.getInstance(), 10f));
    }

    public static void setPopupWindowHW(Activity ct, PopupWindow window) {
        window.getContentView().measure(0, 0);
        window.setHeight(window.getContentView().getMeasuredHeight() + 30);
        window.setWidth(getWidth(ct));
    }


    public interface OnClickListener {
        void result(SelectAimModel model);
    }

    public interface OnSelectListener {
        void select(boolean selectOk);
    }

    public interface OnFindLikerListener {
        void click(String licker);
    }
}
