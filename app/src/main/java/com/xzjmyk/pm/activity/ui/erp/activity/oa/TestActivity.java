package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.app.Activity;
import android.os.Bundle;

import com.common.LogUtil;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.view.calenderlist.DatePickerController;
import com.xzjmyk.pm.activity.ui.erp.view.calenderlist.DayPickerView;


public class TestActivity extends Activity implements DatePickerController {

    private DayPickerView dayPickerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        dayPickerView = (DayPickerView) findViewById(R.id.pickerView);
        dayPickerView.initDraw(this);
    }


    @Override
    public int getMaxYear() {
        return 2019;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        LogUtil.i(year + "-" + month + "-" + day);
    }




}
