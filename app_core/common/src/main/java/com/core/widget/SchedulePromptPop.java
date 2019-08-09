package com.core.widget;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.system.DisplayUtil;
import com.core.app.R;

public class SchedulePromptPop extends PopupWindow {
    private Activity ct;
    private String content;

    public SchedulePromptPop(Activity ct, String content) {
        super(ct);
        this.ct = ct;
        this.content = content;

    }


    public void showPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.pop_prompt_schedule, null);
        setContentView(viewContext);
        setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.pop_round_bg));
        setTouchable(true);
        getContentView().measure(0, 0);
        setHeight(getContentView().getMeasuredHeight() + 30);
        setWidth(getWidth(ct));
        setOutsideTouchable(false);
        setFocusable(true);
        TextView titleTv = viewContext.findViewById(R.id.titleTv);
        TextView contentTv = viewContext.findViewById(R.id.contentTv);
        contentTv.setText(TextUtils.isEmpty(content) ? "" : content);
        viewContext.findViewById(R.id.sureTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePopupWindow();
            }
        });
        setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
        DisplayUtil.backgroundAlpha(ct, 0.6f);
        showAtLocation(viewContext, Gravity.CENTER, 0, 0);
    }

    private static int getWidth(Activity ct) {
        DisplayMetrics dm = new DisplayMetrics();
        ct.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return (int) (dm.widthPixels * (0.8));
    }

    private void closePopupWindow() {
        dismiss();
        if (ct != null) {
            DisplayUtil.backgroundAlpha(ct, 1f);
        }
    }
}
