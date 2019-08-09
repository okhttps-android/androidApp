package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.common.data.StringUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;

public class FlightsDateActivity extends BaseActivity {
    @ViewInject(R.id.checkBox1)
    private CheckBox checkBox1;
    @ViewInject(R.id.checkBox2)
    private CheckBox checkBox2;
    @ViewInject(R.id.checkBox3)
    private CheckBox checkBox3;
    @ViewInject(R.id.checkBox4)
    private CheckBox checkBox4;
    @ViewInject(R.id.checkBox5)
    private CheckBox checkBox5;
    @ViewInject(R.id.checkBox6)
    private CheckBox checkBox6;
    @ViewInject(R.id.checkBox7)
    private CheckBox checkBox7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flights_date);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        findViewById(R.id.click_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        String day = getIntent().getStringExtra("day");
        if (!StringUtil.isEmpty(day)) {
            if (StringUtil.isInclude(day, "1"))
                checkBox1.setChecked(true);
            if (StringUtil.isInclude(day, "2"))
                checkBox2.setChecked(true);
            if (StringUtil.isInclude(day, "3"))
                checkBox3.setChecked(true);
            if (StringUtil.isInclude(day, "4"))
                checkBox4.setChecked(true);
            if (StringUtil.isInclude(day, "5"))
                checkBox5.setChecked(true);
            if (StringUtil.isInclude(day, "6"))
                checkBox6.setChecked(true);
            if (StringUtil.isInclude(day, "7"))
                checkBox7.setChecked(true);
        }


    }

    private void save() {
        StringBuilder builder = new StringBuilder();
        if (checkBox1.isChecked()) {

            builder.append(1 + ",");
        }
        if (checkBox2.isChecked()) {

            builder.append(2 + ",");
        }
        if (checkBox3.isChecked()) {
            builder.append(3 + ",");
        }
        if (checkBox4.isChecked()) {
            builder.append(4 + ",");
        }
        if (checkBox5.isChecked()) {
            builder.append(5 + ",");
        }
        if (checkBox6.isChecked()) {
            builder.append(6 + ",");
        }
        if (checkBox7.isChecked()) {
            builder.append(7 + ",");
        }
        if (builder.length() > 0)
            StringUtil.removieLast(builder);
        Intent intent = new Intent();
        intent.putExtra("data", builder.toString());
        setResult(0x20, intent);
        finish();
    }

}
