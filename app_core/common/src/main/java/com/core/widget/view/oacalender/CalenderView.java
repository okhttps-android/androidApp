package com.core.widget.view.oacalender;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.common.data.DateFormatUtil;
import com.core.base.BaseActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Created by Bitliker on 2017/1/9.
 */

public class CalenderView extends ViewPager {
    private CalendarFragmet fragmet;
    private final int MAX_PAGER = 30;
    private final String MONTH_TAG = "yyyyMM";

    private Date[] date = new Date[MAX_PAGER];

    public CalenderView(Context context) {
        this(context, null);
    }

    public CalenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof BaseActivity) {
            initDate();
            init(context);
        } else {
        }
    }


    private void init(Context context) throws ClassCastException {
        ViewAdapter adapter = new ViewAdapter(((BaseActivity) context).getSupportFragmentManager());
        setAdapter(adapter);
        setCurrentItem(MAX_PAGER / 2);
        this.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (onMonthChangeListener != null) {
                    String yyyyMM = DateFormatUtil.long2Str(CalenderView.this.date[position].getTime(), MONTH_TAG);
                    onMonthChangeListener.selected(yyyyMM);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initDate() {
        //当天在 MAX_PAGER/2 位置
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        for (int i = 0; i < MAX_PAGER; i++) {
            int month = i - (MAX_PAGER / 2);
            c.setTime(date);
            if (month != 0) {
                c.add(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, 1);
            }
            this.date[i] = c.getTime();
        }
    }

    class ViewAdapter extends FragmentPagerAdapter {

        public ViewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CalendarFragmet.getInstance(date[position]);
        }

        @Override
        public int getCount() {
            return MAX_PAGER;
        }


        @Override
        public void setPrimaryItem(ViewGroup container, final int position, Object object) {
            fragmet = (CalendarFragmet) object;
            fragmet.setOnDateListener(new CalendarFragmet.OnDateListener() {
                @Override
                public void result(boolean isClickAgen, Date date) {
                    CalenderView.this.date[position] = date;
                    if (onDateChangeListener != null)
                        onDateChangeListener.selected(isClickAgen, date);
                }
            });
            super.setPrimaryItem(container, position, object);
        }
    }

    /**
     * 设置有任务日期
     *
     * @param decoratDays
     */
    public void setonDecoratDay(Set<Integer> decoratDays) {
        if (fragmet != null)
            fragmet.setonDecoratDay(decoratDays);
    }

    public void setOnDateChangeListener(OnDateSelectListener onDateChangeListener) {
        this.onDateChangeListener = onDateChangeListener;
    }

    public void setOnMonthChangeListener(OnMonthChangeListener onMonthChangeListener) {
        this.onMonthChangeListener = onMonthChangeListener;
    }


    private OnDateSelectListener onDateChangeListener;

    public interface OnDateSelectListener {
        void selected(boolean isClickAgen, Date date);

    }

    private OnMonthChangeListener onMonthChangeListener;

    public interface OnMonthChangeListener {
        void selected(String yyyyMM);
    }
}
