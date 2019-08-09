package com.uas.appworks.activity;

import android.Manifest;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.model.SelectBean;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.SwitchView;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.widget.VeriftyDialog;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.utils.ScheduleUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class SchedulerCreateActivity extends BaseNetActivity {

    private final int SUBMIT = 0x11;
    private final int DELETE = 0x12;
    private final int UPDATE = 0x13;

    private final String[] warns = {"不提醒", "开始时", "提前5分钟", "提前15分钟", "提前30分钟", "提前1小时", "提前一天"};
    private final String[] warns2 = {"不提醒", "当天8点", "当天9点", "提前一天8点", "提前一天9点"};
    private final String[] types = {"工作", "学习", "娱乐", "运动", "约会", "纪念日"};
    private final String[] repeats = {"不重复", "每天重复", "每周重复", "每月重复"};

    private EditText contentEd;
    private SwitchView allDaySv;
    private TextView startTimeTv, endTimeTv, warnTimeTv, repeatTv, typeTv;
    private boolean submiting;//正在提交
    private boolean isEnable;//是否可以编辑
    private boolean isUpdate;//是否更新
    private Schedule mSchedule;//当前的日程对象
    private PopupWindow setWindow;

    private boolean canTouchTag;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scheduler_create;
    }

    @Override
    protected void init() throws Exception {
        canTouchTag = true;
        isEnable = getIntent().getBooleanExtra(Constants.Intents.ENABLE, true);
        mSchedule = getIntent().getParcelableExtra(Constants.Intents.MODEL);
        if (mSchedule == null) {
            initSchedule();
        } else {
            if (!TextUtils.isEmpty(mSchedule.getTag())) {
                canTouchTag = false;
            }
            if (mSchedule.getStartTime() == 0) {
                mSchedule.setStartTime(System.currentTimeMillis());
            }
            if (mSchedule.getEndTime() == 0) {
                mSchedule.setEndTime(System.currentTimeMillis());
            }
        }
        setTitle(isEnable ? R.string.create_scheduler : R.string.scheduler_detail);
        findById();
        initView();
        updateEnable(isEnable);
        if (!canTouchTag) {
            typeTv.setClickable(false);
        }
    }

    private void initSchedule() {
        String warnTime = PreferenceUtils.getString(PreferenceUtils.Constants.DEF_WARN_TIME);
        mSchedule = new Schedule(true);
        mSchedule.setStartTime(System.currentTimeMillis() + 1000 * 5 * 60);
        mSchedule.setEndTime(System.currentTimeMillis() + 1000 * 10 * 60);
        mSchedule.setTag(types[0]);
        mSchedule.setRepeat(PreferenceUtils.getString(ct, PreferenceUtils.Constants.DEF_REPEAT_TIME, repeats[0]));
        mSchedule.setWarnTime(getWarnForText(warnTime, 0));
        mSchedule.setType(getString(R.string.app_name));
    }

    private void initView() {
        contentEd.setText(TextUtils.isEmpty(mSchedule.getRemarks()) ? "" : mSchedule.getRemarks());
        repeatTv.setText(TextUtils.isEmpty(mSchedule.getRepeat()) ? "" : mSchedule.getRepeat());
        typeTv.setText(TextUtils.isEmpty(mSchedule.getTag()) ? "" : mSchedule.getTag());

        allDaySv.setChecked(mSchedule.getAllDay() == 1);
        String format = allDaySv.isChecked() ? DateFormatUtil.YMD : DateFormatUtil.YMD_HM;
        startTimeTv.setText(DateFormatUtil.long2Str(mSchedule.getStartTime(), format));
        endTimeTv.setText(DateFormatUtil.long2Str(mSchedule.getEndTime(), format));
        String warnTime = "";
        switch (mSchedule.getWarnTime()) {
            case 0:
                warnTime = warns[1];
                break;
            case 5:
                warnTime = warns[2];
                break;
            case 15:
                warnTime = warns[3];
                break;
            case 30:
                warnTime = warns[4];
                break;
            case 60:
                warnTime = warns[5];
                break;
            case 60 * 24:
                warnTime = warns[6];
                break;
            case 8:
                warnTime = warns2[1];
                break;
            case 9:
                warnTime = warns2[2];
                break;
            case 18:
                warnTime = warns2[3];
                break;
            case 19:
                warnTime = warns2[4];
                break;
            default:
                if (mSchedule.getWarnTime() < 0) {
                    warnTime = warns2[0];
                }
        }
        warnTimeTv.setText(warnTime);
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getSchedulerBaseUrl();
    }

    private void findById() {
        contentEd = findViewById(R.id.contentEd);
        allDaySv = findViewById(R.id.allDaySv);
        startTimeTv = findViewById(R.id.startTimeTv);
        endTimeTv = findViewById(R.id.endTimeTv);
        warnTimeTv = findViewById(R.id.warnTimeTv);
        repeatTv = findViewById(R.id.repeatTv);
        typeTv = findViewById(R.id.typeTv);
        allDaySv.setChecked(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_input_ok, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isEnable) {
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.complete).setVisible(true);
        } else {
            setTitle(R.string.scheduler_detail);
            menu.findItem(R.id.complete).setVisible(false);
            menu.findItem(R.id.edit).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.complete) {
            save();
        }
        if (item.getItemId() == R.id.edit) {
            if (mSchedule == null || mSchedule.getType().equals(Schedule.TYPE_PHONE)) {
                showToast("手机日程请在系统日历上修改！！！");
            } else if (mSchedule.getType().equals(Schedule.TYPE_BOOK)) {
                showToast("预约类型请进入小秘书界面操作！！！");
                new VeriftyDialog.Builder(ct)
                        .setTitle(getString(R.string.app_name))
                        .setContent("预约类型请进入小秘书界面操作！！！")
                        .build(new VeriftyDialog.OnDialogClickListener() {
                            @Override
                            public void result(boolean clickSure) {
                                if (clickSure) {
                                    startActivity(new Intent("com.modular.booking.BookingListActivity"));
                                }
                            }
                        });
            } else {
                showPopupWindow();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.7f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.pop_edit_schedule_activity, null);
        viewContext.findViewById(R.id.editTv).setOnClickListener(mOnClickListener);
        viewContext.findViewById(R.id.deleteTv).setOnClickListener(mOnClickListener);
        viewContext.findViewById(R.id.cancel_tv).setOnClickListener(mOnClickListener);
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }

    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    private void updateEnable(boolean isEnable) {
        updateEnable(isEnable, startTimeTv, endTimeTv, warnTimeTv, repeatTv, typeTv);
    }

    private void updateEnable(boolean isEnable, View... views) {
        if (views != null && views.length > 0) {
            for (View v : views) {
                v.setEnabled(isEnable);
                v.setClickable(isEnable);
                v.setFocusable(isEnable);
                v.setOnClickListener(isEnable ? mOnClickListener : null);
            }
        }
        contentEd.setEnabled(isEnable);
        contentEd.setFocusable(isEnable);

        allDaySv.setEnabled(isEnable);
        allDaySv.setClickable(isEnable);
        allDaySv.setFocusable(isEnable);
        allDaySv.setOnCheckedChangeListener(isEnable ? mOnCheckedChangeListener : null);
        if (isEnable) {
            contentEd.setFocusableInTouchMode(true);
            contentEd.requestFocus();
            contentEd.setSelection(contentEd.getText().length());
            CommonUtil.openKeybord(contentEd, this);
        }
    }

    private SwitchView.OnCheckedChangeListener mOnCheckedChangeListener = new SwitchView.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(View view, boolean isChecked) {
            if (isChecked) {
                mSchedule.setStartTime(DateFormatUtil.str2Long(
                        DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.YMD) + " 00:00:01"
                        , DateFormatUtil.YMD_HMS));
                mSchedule.setEndTime(DateFormatUtil.str2Long(
                        DateFormatUtil.long2Str(mSchedule.getEndTime(), DateFormatUtil.YMD) + " 23:59:59"
                        , DateFormatUtil.YMD_HMS));
                mSchedule.setWarnTime(8);
                mSchedule.setWarnRealTime(mSchedule.getStartTime());
                warnTimeTv.setText(warns2[1]);
            } else {
                mSchedule.setWarnTime(0);
                mSchedule.setWarnRealTime(mSchedule.getStartTime());
                warnTimeTv.setText(warns[1]);
            }
            mSchedule.setAllDay(isChecked ? 1 : 0);
            String format = mSchedule.getAllDay() == 1 ? DateFormatUtil.YMD : DateFormatUtil.YMD_HM;
            startTimeTv.setText(DateFormatUtil.long2Str(mSchedule.getStartTime(), format));
            endTimeTv.setText(DateFormatUtil.long2Str(mSchedule.getEndTime(), format));
        }
    };
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.editTv) {
                closePopupWindow();
                isEnable = true;
                isUpdate = true;
                updateEnable(isEnable);
                invalidateOptionsMenu();
            } else if (view.getId() == R.id.deleteTv) {
                closePopupWindow();
                deleteFormSc();
            } else if (view.getId() == R.id.cancel_tv) {
                closePopupWindow();
            } else if (view == startTimeTv) {
                showTimeSelect(true);
            } else if (view == endTimeTv) {
                showTimeSelect(false);
            } else if (warnTimeTv == view) {
                ArrayList<SelectBean> beans = new ArrayList<>();
                String[] datas = allDaySv.isChecked() ? warns2 : warns;
                SelectBean bean = null;
                for (String e : datas) {
                    bean = new SelectBean();
                    bean.setName(e);
                    beans.add(bean);
                }
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putParcelableArrayListExtra("data", beans);
                intent.putExtra("title", "提醒时间");
                startActivityForResult(intent, 0x11);
            } else if (repeatTv == view) {
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
                intent.putExtra("title", "是否重复");
                startActivityForResult(intent, 0x12);
            } else if (typeTv == view) {
                ArrayList<SelectBean> beans = new ArrayList<>();
                SelectBean bean = null;
                for (String e : types) {
                    bean = new SelectBean();
                    bean.setName(e);
                    bean.setClick(e.contains("默认") ? true : false);
                    beans.add(bean);
                }
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putParcelableArrayListExtra("data", beans);
                intent.putExtra("title", "日程类型");
                startActivityForResult(intent, 0x13);
            }
        }
    };

    private void save() {
        if (TextUtils.isEmpty(contentEd.getText())) {
            showToast("请添加备注！！");
            return;
        }
        if (submiting) {
            showToast("当前正在处理，请稍等！！");
            return;
        }
        submiting = true;
        submit();
    }

    private void showTimeSelect(final boolean isStart) {
        DateTimePicker picker = new DateTimePicker(this, allDaySv.isChecked() ? DateTimePicker.YEAR_MONTH_DAY : DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                long time = DateFormatUtil.str2Long(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00", DateFormatUtil.YMD_HMS);
                String format = mSchedule.getAllDay() == 1 ? DateFormatUtil.YMD : DateFormatUtil.YMD_HM;
                if (isStart) {
                    mSchedule.setStartTime(time);
                    startTimeTv.setText(DateFormatUtil.long2Str(time, format));
                } else {
                    mSchedule.setEndTime(time);
                    endTimeTv.setText(DateFormatUtil.long2Str(time, format));
                }
            }
        });
        picker.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            SelectBean b = data.getParcelableExtra("data");
            if (b != null) {
                switch (requestCode) {
                    case 0x11:
                        warnTimeTv.setText(b.getName());
                        break;
                    case 0x12:
                        repeatTv.setText(b.getName());
                        mSchedule.setRepeat(b.getName());
                        break;
                    case 0x13:
                        typeTv.setText(b.getName());
                        mSchedule.setTag(b.getName());
                        break;

                }
            }
        }
    }

    private void submit() {
        if (mSchedule.getStartTime() >= mSchedule.getEndTime()) {
            showToast("结束时间必须大于开始时间！！");
            submiting = false;
            return;
        }
        showProgress();
        String url = isUpdate ? "schedule/updateSchedule" : "schedule/saveSchedule";
        mSchedule.setRemarks(contentEd.getText().toString());
        mSchedule.setTitle(contentEd.getText().toString());
        long minth = 60 * 1000;

        int warnTime = -1;
        long warnRealTime = 0;
        String warnStr = warnTimeTv.getText().toString();

        if (mSchedule.getAllDay() == 1) {
            if (warnStr.equals(warns2[1])) {
                warnTime = 8;
                warnRealTime = DateFormatUtil.str2Long(DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.YMD) + " 08:00:00", DateFormatUtil.YMD_HMS);
            } else if (warnStr.equals(warns2[2])) {
                warnTime = 9;
                warnRealTime = DateFormatUtil.str2Long(DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.YMD) + " 09:00:00", DateFormatUtil.YMD_HMS);
            } else if (warnStr.equals(warns2[3])) {
                warnTime = 18;
                warnRealTime = DateFormatUtil.str2Long(DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.YMD) + " 08:00:00", DateFormatUtil.YMD_HMS) - minth * 24 * 60;
            } else if (warnStr.equals(warns2[4])) {
                warnTime = 19;
                warnRealTime = DateFormatUtil.str2Long(DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.YMD) + " 09:00:00", DateFormatUtil.YMD_HMS) - minth * 24 * 60;
            }
        } else {
            warnTime = getWarnForText(warnStr, warnTime);
            warnRealTime = mSchedule.getStartTime() - warnTime * minth;
        }
        mSchedule.setWarnRealTime(warnRealTime);
        mSchedule.setWarnTime(warnTime);
        Parameter.Builder mBuilder = new Parameter.Builder()
                .mode(Method.POST)
                .url(url)
                .addParams("imid", MyApplication.getInstance().getLoginUserId())
                .addParams("type", mSchedule.getType())
                .addParams("allDay", mSchedule.getAllDay())
                .addParams("repeat", mSchedule.getRepeat())
                .addParams("title", mSchedule.getTitle())
                .addParams("tag", mSchedule.getTag())
                .addParams("remarks", mSchedule.getRemarks())
                .addParams("startTime", mSchedule.getStartTime() / 1000)
                .addParams("endTime", mSchedule.getEndTime() / 1000)
                .addParams("warnTime", mSchedule.getWarnTime())
                .addParams("warnRealTime", mSchedule.getWarnRealTime() / 1000)
                .addParams("address", mSchedule.getAddress())
                .addParams("status", mSchedule.getStatus())
                .addParams("phone", MyApplication.getInstance().mLoginUser.getTelephone())
                .record(isUpdate ? UPDATE : SUBMIT);
        if (isUpdate) {
            mBuilder.addParams("scheduleId", mSchedule.getId());
        }
        requestHttp(mBuilder, mOnSmartHttpListener);
    }

    private int getWarnForText(String text, int def) {
        int warnTime = def;
        if (text.equals(warns[1])) {
            warnTime = 0;
        } else if (text.equals(warns[2])) {
            warnTime = 5;
        } else if (text.equals(warns[3])) {
            warnTime = 15;
        } else if (text.equals(warns[4])) {
            warnTime = 30;
        } else if (text.equals(warns[5])) {
            warnTime = 60;
        } else if (text.equals(warns[6])) {
            warnTime = 60 * 24;
        }
        return warnTime;
    }

    private void deleteSchedule() {
        if (submiting) {
            showToast("当前正在处理，请稍等！！");
            return;
        }
        submiting = true;
        showProgress();
        requestHttp(new Parameter.Builder()
                        .mode(Method.POST)
                        .addParams("scheduleId", mSchedule.getId())
                        .url("schedule/deleteSchedule")
                        .record(DELETE)
                , mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            LogUtil.i("gong", "onSuccess=" + message);
            dismissProgress();
            JSONObject object = JSON.parseObject(message);
            switch (what) {
                case SUBMIT:
                    if (JSONUtil.getBoolean(object, "success")) {
                        Toast.makeText(ct, "提交成功！！", Toast.LENGTH_LONG).show();
                        int id = JSONUtil.getInt(object, "data");
                        mSchedule.setId(id);
                        save2System(false);
                        setResult(0x11);
                        onBackPressed();
                    }
                    break;
                case UPDATE:
                    if (JSONUtil.getBoolean(object, "success")) {
                        Toast.makeText(ct, "更新成功！！", Toast.LENGTH_LONG).show();
                        int id = JSONUtil.getInt(object, "data");
                        mSchedule.setId(id);
                        save2System(true);
                        setResult(0x11);
                        onBackPressed();
                    }
                    break;
                case DELETE:
                    if (JSONUtil.getBoolean(object, "success")) {
                        Toast.makeText(ct, "删除成功！！", Toast.LENGTH_LONG).show();
                        int id = JSONUtil.getInt(object, "data");
                        mSchedule.setId(id);
                        deleteFormSystem();
                    }
                    break;
            }
            submiting = false;
        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            submiting = false;
            dismissProgress();
            LogUtil.i("gong", "onFailure=" + message);

        }
    };

    private void deleteFormSystem() {
        requestPermission(Manifest.permission.WRITE_CALENDAR, new Runnable() {
            @Override
            public void run() {
                boolean has = ScheduleUtils.hasSystemCalendar(ct, mSchedule.getId());
                if (has) {
                    new VeriftyDialog.Builder(ct)
                            .setTitle(getString(R.string.app_name))
                            .setCanceledOnTouchOutside(false)
                            .setShowCancel(true)
                            .setContent("删除成功，是否同步删除系统日程！！")
                            .build(new VeriftyDialog.OnDialogClickListener() {
                                @Override
                                public void result(boolean clickSure) {
                                    if (clickSure) {
                                        deleteFormSystem(mSchedule);
                                    } else {
                                        setResult(0x11);
                                        onBackPressed();
                                    }
                                }
                            });
                } else {
                    setResult(0x11);
                    onBackPressed();
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                showToast(R.string.not_system_permission);
            }
        });

    }

    private void deleteFormSc() {


        new VeriftyDialog.Builder(ct)
                .setTitle(getString(R.string.app_name))
                .setCanceledOnTouchOutside(false)
                .setShowCancel(true)
                .setContent("是否确定删除该日程？")
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                        if (clickSure) {
                            deleteSchedule();
                        }
                    }
                });
    }

    private void deleteFormSystem(final Schedule mSchedule) {
        requestPermission(Manifest.permission.WRITE_CALENDAR, new Runnable() {
            @Override
            public void run() {
                ScheduleUtils.deleteSystemCalendar(ct, mSchedule.getId());
                setResult(0x11);
                onBackPressed();
            }
        }, new Runnable() {
            @Override
            public void run() {
                showToast(R.string.not_system_permission);
            }
        });
    }

    private void save2System(final boolean isUpdate) {
        boolean needSaveSystem = PreferenceUtils.getBoolean(PreferenceUtils.Constants.SAVE_SYSTEM_SCHEDULE, true);
        if (needSaveSystem) {
            requestPermission(Manifest.permission.WRITE_CALENDAR, new Runnable() {
                @Override
                public void run() {
                    saveScheduleSystem(isUpdate, mSchedule);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    showToast(R.string.not_system_permission);
                }
            });

        }
    }

    private void saveScheduleSystem(boolean isUpdate, Schedule mSchedule) {
        Intent schedule = new Intent();
        schedule.setAction("action_schedule");
        sendBroadcast(schedule);
        if (ScheduleUtils.checkCalendarAccount(ct) > 0 || ScheduleUtils.addCalendarAccount(ct) > 0) {
            if (isUpdate) {
                ScheduleUtils.updateSystemCalendar(ct, mSchedule);
            } else {
                ScheduleUtils.addCalendarEvent(ct, mSchedule);
            }
        } else {
            showToast("添加到系统日历失败！！");
        }
    }
}
