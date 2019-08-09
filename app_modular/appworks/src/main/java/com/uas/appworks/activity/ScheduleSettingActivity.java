package com.uas.appworks.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.preferences.PreferenceUtils;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.SwitchView;
import com.uas.appworks.R;

import java.util.ArrayList;

public class ScheduleSettingActivity extends BaseActivity {
    private final String[] warns = {"不提醒", "开始时", "提前5分钟", "提前15分钟", "提前30分钟", "提前1小时", "提前一天"};
    private final String[] repeats = {"不重复(默认)", "每天重复", "每周重复", "每月重复"};

    private TextView warmTimeTv;
    private TextView repeatTv;
    private SwitchView saveSystemSv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_setting);
        initView();
    }

    private void initView() {
        saveSystemSv = findViewById(R.id.saveSystemSv);
        warmTimeTv = (TextView) findViewById(R.id.warmTimeTv);
        repeatTv = (TextView) findViewById(R.id.repeatTv);

        String warnTime = PreferenceUtils.getString(PreferenceUtils.Constants.DEF_WARN_TIME);
        if (!TextUtils.isEmpty(warnTime)) {
            warmTimeTv.setText(warnTime);
        }
        String repeat = PreferenceUtils.getString(PreferenceUtils.Constants.DEF_REPEAT_TIME);
        if (!TextUtils.isEmpty(repeat)) {
            repeatTv.setText(repeat);
        }
        findViewById(R.id.warmTimeRl).setOnClickListener(mOnClickListener);
        findViewById(R.id.repeatRl).setOnClickListener(mOnClickListener);
        saveSystemSv.setChecked(PreferenceUtils.getBoolean(PreferenceUtils.Constants.SAVE_SYSTEM_SCHEDULE, true));
        saveSystemSv.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                PreferenceUtils.putBoolean(PreferenceUtils.Constants.SAVE_SYSTEM_SCHEDULE, isChecked);
                if (isChecked) {
                    showToast("创建日程时会将日程同步到系统日历中！！");
                } else {
                    showToast("创建日程时不会将日程同步到系统日历中！！");
                }
            }
        });
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.warmTimeRl) {
                ArrayList<SelectBean> beans = new ArrayList<>();
                String[] datas = warns;
                SelectBean bean = null;
                for (String e : datas) {
                    bean = new SelectBean();
                    bean.setName(e);
                    bean.setClick(e.contains("默认") ? true : false);
                    beans.add(bean);
                }
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putParcelableArrayListExtra("data", beans);
                intent.putExtra("title", "默认提示时间");
                startActivityForResult(intent, 0x11);
            } else if (R.id.repeatRl == view.getId()) {
                ArrayList<SelectBean> beans = new ArrayList<>();
                SelectBean bean = null;
                for (String e : repeats) {
                    bean = new SelectBean();
                    bean.setName(e);
                    bean.setClick(e.contains("默认") ? true : false);
                    beans.add(bean);
                }
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putParcelableArrayListExtra("data", beans);
                intent.putExtra("title", getString(R.string.select_approvel_people));
                startActivityForResult(intent, 0x12);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            SelectBean b = data.getParcelableExtra("data");
            if (b != null) {
                switch (requestCode) {
                    case 0x11:
                        PreferenceUtils.putString(PreferenceUtils.Constants.DEF_WARN_TIME, b.getName());
                        warmTimeTv.setText(b.getName());
                        break;
                    case 0x12:
                        PreferenceUtils.putString(PreferenceUtils.Constants.DEF_REPEAT_TIME, b.getName());
                        repeatTv.setText(b.getName());
                        break;
                }
            }
        }
    }

}
