package com.xzjmyk.pm.activity.ui.erp.adapter.oa;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.core.widget.view.selectcalendar.OACalendarView;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Bitliker on 2017/4/19.
 */

public class OACalenderViewPagerAdapter extends PagerAdapter {
    public static final int MAX_NUM = 30;
    private Context mContext;
    private SparseArray<OACalendarView> mViews;
    private Date[] date = new Date[MAX_NUM];

    public OACalenderViewPagerAdapter(Context context) {
        this.mContext = context;
        this.mViews = new SparseArray<>();
        setDate();
    }
    public OACalenderViewPagerAdapter(Context context,Date date) {
        this.mContext = context;
        this.mViews = new SparseArray<>();
        setDate(date);
    }

    @Override
    public int getCount() {
        return MAX_NUM;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViews.get(position) == null) {
            OACalendarView calendarView = new OACalendarView(mContext);
            Date date = this.date[position];
            calendarView.setCurDate(date);
            mViews.put(position, calendarView);
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public SparseArray<OACalendarView> getmViews() {
        return mViews;
    }

    private void setDate() {
        //当前天在 MAX_PAGER/2 位置
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        for (int i = 0; i < MAX_NUM; i++) {
            c.setTime(date);
            c.add(Calendar.MONTH, i - (MAX_NUM / 2));
            c.set(Calendar.DAY_OF_MONTH, 1);
            this.date[i] = c.getTime();
        }
    }

    public void setDate(Date date) {
        //当前天在 MAX_PAGER/2 位置
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < MAX_NUM; i++) {
            c.setTime(date);
            c.add(Calendar.MONTH, i - (MAX_NUM / 2));
            this.date[i] = c.getTime();
        }
    }
}
