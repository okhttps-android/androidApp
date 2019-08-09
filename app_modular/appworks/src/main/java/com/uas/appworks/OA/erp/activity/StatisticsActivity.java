package com.uas.appworks.OA.erp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.baidu.android.pushservice.PushManager;
import com.common.preferences.PreferenceUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.app.AppConstant;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.fragment.AttendanceFragment;
import com.uas.appworks.OA.erp.fragment.AttendancesFragment;

public class StatisticsActivity extends BaseActivity implements View.OnClickListener {

    private View attendances_tag;
    private View attendance_tag;
    private TextView attendances_tv;
    private TextView attendance_tv;


    private boolean isAttendance = false;//是否显示个人考勤

    private AttendanceFragment attendanceFragment;
    private AttendancesFragment attendancesFragment;
    private Fragment mLastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ViewUtils.inject(this);
        initview();
    }

    private void initview() {
        attendances_tag = findViewById(R.id.attendances_tag);
        attendance_tag = findViewById(R.id.attendance_tag);
        attendances_tv = (TextView) findViewById(R.id.attendances_tv);
        attendance_tv = (TextView) findViewById(R.id.attendance_tv);

        attendances_tv.setOnClickListener(this);
        attendance_tv.setOnClickListener(this);
        if (attendancesFragment == null) {
            attendancesFragment = new AttendancesFragment();
        }
        changeFragment(attendancesFragment, "attendancesFragment");

        PreferenceUtils.putInt(AppConstant.NEW_FUNCTION_NOTICE, 1);
        PushManager.resumeWork(MyApplication.getInstance());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.attendances_tv){
            if (!isAttendance) return;
            chaneTAG(false);
        }else if (view.getId() == R.id.attendance_tv){
            if (isAttendance) return;
            chaneTAG(true);
        }
    }

    //设置标签
    private void chaneTAG(boolean isAttendance) {
        this.isAttendance = isAttendance;
        if (isAttendance) {
            attendance_tv.setTextColor(getResources().getColor(R.color.darkorange));
            attendances_tv.setTextColor(getResources().getColor(R.color.dimgrey));
            attendance_tag.setBackgroundResource(R.color.darkorange);
            attendances_tag.setBackgroundResource(R.color.item_line);
            if (attendanceFragment == null) {
                attendanceFragment = new AttendanceFragment();
            }
            changeFragment(attendanceFragment, "attendanceFragment");
        } else {
            attendance_tv.setTextColor(getResources().getColor(R.color.dimgrey));
            attendances_tv.setTextColor(getResources().getColor(R.color.darkorange));
            attendance_tag.setBackgroundResource(R.color.item_line);
            attendances_tag.setBackgroundResource(R.color.darkorange);
            if (attendancesFragment == null) {
                attendancesFragment = new AttendancesFragment();
            }
            changeFragment(attendancesFragment, "attendancesFragment");
        }
    }

    private void changeFragment(Fragment addFragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开始事物
        if (mLastFragment == addFragment) {
            return;
        }
        if (mLastFragment != null && mLastFragment != addFragment) {// 如果最后一次加载的不是现在要加载的Fragment，那么僵最后一次加载的移出
            fragmentTransaction.detach(mLastFragment);
        }
        if (addFragment == null) {
            return;
        }
        if (!addFragment.isAdded())// 如果还没有添加，就加上
            fragmentTransaction.add(R.id.content_fl, addFragment, tag);
        if (addFragment.isDetached())
            fragmentTransaction.attach(addFragment);
        mLastFragment = addFragment;
        fragmentTransaction.commitAllowingStateLoss();
    }

}
