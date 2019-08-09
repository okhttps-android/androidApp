package com.core.widget.view.oacalender;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.core.app.R;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pengminggong on 2016/9/28.
 */

public class CalendarFragmet extends Fragment {
    private MyCalendarView calendarView;
    private Date date;

    /**
     * @param date 显示月份
     * @return
     */
    public static CalendarFragmet getInstance(Date date) {
        CalendarFragmet fragment = new CalendarFragmet();
        Bundle bundle = new Bundle();
        bundle.putSerializable("DATA", date);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = (MyCalendarView) view.findViewById(R.id.calender);
        date = (Date) getArguments().getSerializable("DATA");
        calendarView.setCurDate(date);
        Set<Integer> dada = new HashSet<>();
        calendarView.setDecoratDays(dada);
        calendarView.setDateListener(new MyCalendarView.OnSelectDateListener() {
            @Override
            public void result(boolean isClickAgen, Date date) {
                CalendarFragmet.this.date=date;
                if (onDateListener != null)
                    onDateListener.result(isClickAgen,date);
            }
        });
        return view;
    }

    public Date getDate() {
        return date;
    }

    public void setonDecoratDay(Set<Integer> decoratDays) {
        if (calendarView != null) {
            calendarView.setDecoratDays(decoratDays);
        }
    }

    public void setOnDateListener(OnDateListener onDateListener) {
        this.onDateListener = onDateListener;
    }

    private OnDateListener onDateListener;

    public interface OnDateListener {
        void result(boolean isClickAgen, Date date);
    }
}
