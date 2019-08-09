package com.core.widget.view.selectcalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.data.DateFormatUtil;
import com.core.app.R;

import java.util.Date;
import java.util.Set;

/**
 * Created by pengminggong on 2016/9/28.
 */

/**
 * @desc:此类暂时过渡，需要删除并优化---详情请见
 * @author：Arison on 2017/1/18
 */
public class CalendarDateFragmet extends Fragment {
    private SelectCalendarActivity activity;
    private OACalendarView calendarView;

    /**
     * @param date 显示月份
     * @return
     */
    public static CalendarDateFragmet getInstance(Date date, int day) {
        CalendarDateFragmet fragment = new CalendarDateFragmet();
        Bundle bundle = new Bundle();
        bundle.putSerializable("DATA", date);
        bundle.putInt("day", day);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SelectCalendarActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_calendar, container, false);
        calendarView = (OACalendarView) view.findViewById(R.id.calender);
        Date date = (Date) getArguments().getSerializable("DATA");
        int day = getArguments().getInt("day");
        calendarView.setCurDate(date);
        calendarView.setDownIndex(day);
        //判断是否是当月
        calendarView.getCurDate();
        if (activity != null) {
            activity.setListener(DateFormatUtil.date2Str(date,"yyyyMM"), new SelectCalendarActivity.OnTaskChangeListener() {
                @Override
                public void onChange(Set<Integer> in) {
                    calendarView.setDecoratDays(in);
                }
            });
        }
        calendarView.setDateListener(new OACalendarView.OnSelectDateListener() {
            @Override
            public void result(Date date) {
                activity.setClickDay(date);
            }
        });
        return view;
    }

    public void setDownDay(int downDay) {
        calendarView.setDownIndex(downDay);
    }
}
