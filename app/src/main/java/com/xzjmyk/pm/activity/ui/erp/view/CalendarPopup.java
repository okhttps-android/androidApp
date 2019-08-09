package com.xzjmyk.pm.activity.ui.erp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.xzjmyk.pm.activity.R;

/**
 * Created by gongpengming on 2016/9/5.
 */
public class CalendarPopup extends LinearLayout {
    public CalendarPopup(Context context) {
        this(context, null);
    }
    public CalendarPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.calender_popup, this, false);
        ListView list_yeas = (ListView) findViewById(R.id.list_yeas);
        ListView list_month = (ListView) findViewById(R.id.list_month);
        ListView list_day = (ListView) findViewById(R.id.list_day);
        ListView list_hh = (ListView) findViewById(R.id.list_hh);
        ListView list_mm = (ListView) findViewById(R.id.list_mm);
    }


    public class Builder {
        Context ct;

        public Builder(Context ct) {
            this.ct = ct;
        }

        public CalendarPopup builder() {
            return new CalendarPopup(ct);
        }

        /**
         * 设置开始时间
         *
         * @param data yyyy-HH-mm
         * @return
         */
        public Builder setStartData(String data) {

            return this;
        }

        /**
         * 设置日期停止时间时间
         *
         * @param data yyyy-HH-mm
         * @return
         */
        public Builder setEndData(String data) {
            return this;
        }

    }


}
