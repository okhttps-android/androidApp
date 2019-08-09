package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.base.EasyFragment;
import com.core.utils.time.wheel.DatePicker;

import java.util.Calendar;

/**
 * Created by gongpm on 2016/7/14.
 */
public class OutofficePlayFragment extends EasyFragment implements View.OnClickListener {

    private TextView rili_tv;
    private TextView played_num, played_tag, unplay_num, unplay_tag;
    private TextView range_tv;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_outofficeplay;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        initView();
    }

    private void initView() {
        rili_tv = (TextView) findViewById(R.id.rili_tv);
        rili_tv.setOnClickListener(this);
        findViewById(R.id.played).setOnClickListener(this);
        findViewById(R.id.unplay).setOnClickListener(this);
        played_num = (TextView) findViewById(R.id.played_num);
        played_tag = (TextView) findViewById(R.id.played_tag);
        unplay_num = (TextView) findViewById(R.id.unplay_num);
        unplay_tag = (TextView) findViewById(R.id.unplay_tag);
        range_tv = (TextView) findViewById(R.id.range_tv);
        range_tv.setOnClickListener(this);
        findViewById(R.id.spread_tv).setOnClickListener(this);


    }

    //显示
    private void showPickerDalog() {
        DatePicker picker = new DatePicker(getActivity());
        picker.setRange(2000, 2030);
        Calendar calendar = Calendar.getInstance();
        picker.setSelectedItem(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
            @Override
            public void onDatePicked(String year, String month, String day) {
                rili_tv.setText(year + "-" + month + "-" + day);
            }
        });
        picker.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rili_tv:
                showPickerDalog();
                break;
            case R.id.played:
                played_num.setTextColor(getResources().getColor(R.color.yellow_home));
                played_tag.setTextColor(getResources().getColor(R.color.yellow_home));
                unplay_num.setTextColor(getResources().getColor(R.color.text_hine));
                unplay_tag.setTextColor(getResources().getColor(R.color.text_hine));
                break;
            case R.id.unplay:
                played_num.setTextColor(getResources().getColor(R.color.text_hine));
                played_tag.setTextColor(getResources().getColor(R.color.text_hine));
                unplay_num.setTextColor(getResources().getColor(R.color.yellow_home));
                unplay_tag.setTextColor(getResources().getColor(R.color.yellow_home));
                break;
            case R.id.range_tv:

                break;
            case R.id.spread_tv:

                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_outoffice, menu);
        menu.getItem(0).setTitle("团队足迹");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.title:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
