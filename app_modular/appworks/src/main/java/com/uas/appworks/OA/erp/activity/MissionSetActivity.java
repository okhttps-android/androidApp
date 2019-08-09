package com.uas.appworks.OA.erp.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.base.SupportToolBarActivity;
import com.core.model.SelectBean;
import com.core.utils.CommonInterface;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.SwitchView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;

import java.util.ArrayList;

public class MissionSetActivity extends SupportToolBarActivity implements View.OnClickListener {
    private SwitchView auto_sv, needprocess_sv, mFaceSignSwitchView;
    private RelativeLayout distance_rl;
    private RelativeLayout time_rl;
    private RelativeLayout mFaceSignRl;
    private TextView distance_tv;
    private TextView time_tv;

    private boolean isAuto = false;//是否自动外勤
    private int distance = 100;//
    private int time = 30;
    private boolean adminStatus;
    private boolean needprocess;
    private int mFaceSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_set);
        ViewUtils.inject(this);
        adminStatus = getIntent() == null ? false : getIntent().getBooleanExtra(AppConfig.IS_ADMIN, false);
        initView();
        initEvent();
    }

    @Override
    public void onBackPressed() {
        endActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            endActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void endActivity() {
        Intent intent = new Intent();
        intent.putExtra("isAuto", auto_sv.isChecked());
        setResult(0x20, intent);
        finish();
    }

    private void initEvent() {
        auto_sv.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                isAuto = isChecked;
                updataSet();
                setClickAble(isAuto);
                PreferenceUtils.putBoolean(AppConfig.AUTO_MISSION, isAuto);
            }
        });
        needprocess_sv.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                needprocess = isChecked;
                updataSet();
                PreferenceUtils.putBoolean(AppConfig.NEED_PROCESS, needprocess);
            }
        });
        mFaceSignSwitchView.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                mFaceSign = isChecked ? 1 : 0;
                updataSet();
                PreferenceUtils.putInt(AppConfig.FACE_SIGN, mFaceSign);
            }
        });
        if (adminStatus) {
            distance_rl.setOnClickListener(this);
            time_rl.setOnClickListener(this);
        }
        auto_sv.setFocusable(adminStatus);
        auto_sv.setClickable(adminStatus);
        needprocess_sv.setFocusable(adminStatus);
        needprocess_sv.setClickable(adminStatus);
        mFaceSignSwitchView.setFocusable(adminStatus);
        mFaceSignSwitchView.setClickable(adminStatus);
    }

    private void initView() {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            findViewById(R.id.auto_rl).setVisibility(View.GONE);
        }
        auto_sv = (SwitchView) findViewById(R.id.auto_sv);
        needprocess_sv = (SwitchView) findViewById(R.id.needprocess_sv);
        mFaceSignSwitchView = findViewById(R.id.face_sign_sv);
        distance_rl = (RelativeLayout) findViewById(R.id.distance_rl);
        time_rl = (RelativeLayout) findViewById(R.id.time_rl);
        distance_tv = (TextView) findViewById(R.id.distance_tv);
        time_tv = (TextView) findViewById(R.id.time_tv);
        mFaceSignRl = findViewById(R.id.face_sign_ll);

        isAuto = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
        needprocess = PreferenceUtils.getBoolean(AppConfig.NEED_PROCESS, false);
        mFaceSign = PreferenceUtils.getInt(AppConfig.FACE_SIGN, -1);

        auto_sv.setChecked(isAuto);
        needprocess_sv.setChecked(needprocess);
        if (mFaceSign == -1) {
            mFaceSignRl.setVisibility(View.GONE);
        } else {
            mFaceSignRl.setVisibility(View.VISIBLE);
            mFaceSignSwitchView.setChecked(mFaceSign == 1);
        }

        setClickAble(isAuto);
        distance = PreferenceUtils.getInt(AppConfig.ALARM_MISSION_DISTANCE, 500);
        distance_tv.setText(distance + "m");
        time = PreferenceUtils.getInt(AppConfig.AUTO_MISSION_TIME, 20);
        time_tv.setText(time + getResources().getString(R.string.minute));
    }

    private void setClickAble(boolean isAuto) {
        distance_rl.setClickable(isAuto);
        distance_rl.setFocusable(isAuto);
        time_rl.setClickable(isAuto);
        time_rl.setFocusable(isAuto);
        setTextColor(isAuto, distance_rl, time_rl);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setTextColor(boolean isAdmain, View... view) {
        if (view != null && view.length > 0 && Build.VERSION.SDK_INT >= 9) {
            for (View v : view) {
                if (v instanceof TextView)
                    v.setAlpha(isAdmain ? 1f : 0.26f);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        String[] str;
        SelectBean bean = null;
        ArrayList<SelectBean> beans = null;

        if (view.getId() == R.id.distance_rl) {
            str = new String[]{"100m", "200m", "500m", "1000m", "1500m"};
            beans = new ArrayList<>();
            for (String e : str) {
                bean = new SelectBean();
                bean.setName(e);
                bean.setClick(false);
                beans.add(bean);
            }
            intent = new Intent(ct, SelectActivity.class);
            intent.putExtra("type", 2);
            intent.putParcelableArrayListExtra("data", beans);
            intent.putExtra("title", getResources().getString(R.string.effective_range));
            startActivityForResult(intent, 0x22);
        } else if (view.getId() == R.id.time_rl) {
            str = new String[]{"10" + getString(R.string.minute), "20" + getString(R.string.minute),
                    "30" + getString(R.string.minute), "45" + getString(R.string.minute), "60" + getString(R.string.minute)};
            beans = new ArrayList<>();
            for (String e : str) {
                bean = new SelectBean();
                bean.setName(e);
                bean.setClick(false);
                beans.add(bean);
            }
            intent = new Intent(ct, SelectActivity.class);
            intent.putExtra("type", 2);
            intent.putParcelableArrayListExtra("data", beans);
            intent.putExtra("title", getString(R.string.reserve_time));
            startActivityForResult(intent, 0x23);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || resultCode != 0x20) return;
        SelectBean bean = data.getParcelableExtra("data");
        if (bean == null || StringUtil.isEmpty(bean.getName())) {
            return;
        }
        switch (requestCode) {
            case 0x22:
                distance_tv.setText(bean.getName());
                distance = StringUtil.getFirstInt(bean.getName(), 5);
                PreferenceUtils.putInt(AppConfig.ALARM_MISSION_DISTANCE, distance);
                updataSet();
                break;
            case 0x23:
                time_tv.setText(bean.getName());
                time = StringUtil.getFirstInt(bean.getName(), 5);
                PreferenceUtils.putInt(AppConfig.AUTO_MISSION_TIME, time);
                updataSet();
                break;
        }
    }

    private void updataSet() {
        CommonInterface.getInstance().addOutSet(distance, time, isAuto, needprocess, mFaceSign, null);
    }
}
