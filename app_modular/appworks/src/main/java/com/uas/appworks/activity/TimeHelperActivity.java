package com.uas.appworks.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUAS;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.SimpleView;
import com.core.model.MissionModel;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.WorkHandlerUtil;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.arcmenu.ArcMenu;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.modular.booking.activity.business.BBookingDetailActivity;
import com.modular.booking.model.BookingModel;
import com.uas.appworks.OA.erp.activity.CommonDocDetailsActivity;
import com.uas.appworks.OA.erp.activity.MeetDetailsActivity;
import com.uas.appworks.OA.erp.activity.MissionActivity;
import com.uas.appworks.OA.erp.model.MeetEntity;
import com.uas.appworks.R;
import com.uas.appworks.adapter.TimeHelperAdapter;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.model.bean.TimeHelperBean;
import com.uas.appworks.utils.ScheduleUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author RaoMeng
 * @describe 时间助手页面
 * @date 2018/8/21 15:02
 */
public class TimeHelperActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView {
    private static final int FLAG_GETBYDAY_SCHEDULE = 0x11;

    private ArcMenu mArcMenu;
    private static final int[] ITEM_DRAWABLES = {
            R.drawable.ic_timehelper_schedule,
            R.drawable.ic_timehelper_metting,
            R.drawable.ic_timehelper_order,
            R.drawable.ic_timehelper_outwork,
            R.drawable.ic_timehelper_trip};
    private RecyclerView mRecyclerView;
    private ImageView mBottomImageView;
    private View mGrayView;
    private List<TimeHelperBean> mTimeHelperBeans;
    private TimeHelperAdapter mTimeHelperAdapter;
    private AnimationDrawable mAnimationDrawable;
    private View mEmptyView;
    private AppCompatTextView mEmptyTextView;
    private TextView mDateTextView;
    private String mCurrentDate, mFormatDate;

    @Override
    protected int getLayout() {
        return R.layout.activity_time_helper;
    }

    @Override
    protected void initView() {
        setTitle(getString(R.string.title_time_helper));

        mArcMenu = $(R.id.time_helper_arcmenu);
        mDateTextView = $(R.id.time_helper_date_tv);
        mRecyclerView = $(R.id.time_helper_rv);
        mBottomImageView = $(R.id.time_helper_bottom_iv);
        mBottomImageView.setImageResource(R.drawable.animlist_time_helper_icon);
        mAnimationDrawable = (AnimationDrawable) mBottomImageView.getDrawable();

        mGrayView = $(R.id.time_helper_gray_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mTimeHelperBeans = new ArrayList<>();
        mTimeHelperAdapter = new TimeHelperAdapter(mTimeHelperBeans);
        mRecyclerView.setAdapter(mTimeHelperAdapter);

        mEmptyView = View.inflate(this, R.layout.common_empty_view, null);
        mEmptyTextView = mEmptyView.findViewById(R.id.emptyTv);
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initEvent() {
        mArcMenu.setOnMenuClickListener(new ArcMenu.OnMenuClickListener() {
            @Override
            public void onMenuClick(boolean isExpanded) {
                if (isExpanded) {
                    mGrayView.setVisibility(View.VISIBLE);
                } else {
                    mGrayView.setVisibility(View.GONE);
                }
            }
        });

        mGrayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        mBottomImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mTimeHelperAdapter.setOnTimeClickListener(new TimeHelperAdapter.OnTimeClickListener() {
            @Override
            public void onTimeClick(int position) {
                TimeHelperBean timeHelperBean = mTimeHelperBeans.get(position);

                Schedule schedule = getSchedule(timeHelperBean, true);
                try {
                    toDetailActivty(schedule);
                } catch (Exception e) {
                    startActivityForResult(new Intent(mContext, SchedulerCreateActivity.class)
                            .putExtra(Constants.Intents.ENABLE, false)
                            .putExtra(Constants.Intents.MODEL, schedule), 0x33);
                }
//                startActivityForResult(new Intent(mContext, SchedulerCreateActivity.class)
//                        .putExtra(Constants.Intents.ENABLE, false)
//                        .putExtra(Constants.Intents.MODEL, schedule), 0x33);
            }
        });

        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] dates = mFormatDate.split("-");
                if (dates != null && dates.length >= 3) {
                    showDateDialog(dates[0], dates[1], dates[2]);
                }
            }
        });
    }

    private Schedule getSchedule(TimeHelperBean timeHelperBean, boolean isUU) {
        Schedule schedule = new Schedule(timeHelperBean.getFromWhere());
        schedule.setId(timeHelperBean.getScheduleId());
        schedule.setType(timeHelperBean.getType());
        schedule.setAllDay(timeHelperBean.getAllDay());
        schedule.setRepeat(timeHelperBean.getRepeat());
        schedule.setTitle(timeHelperBean.getTitle());
        schedule.setTag(timeHelperBean.getTag());
        schedule.setRemarks(timeHelperBean.getRemarks());
        schedule.setStartTime(DateFormatUtil.str2Long(timeHelperBean.getStartTime(), DateFormatUtil.YMD_HMS));
        schedule.setEndTime(DateFormatUtil.str2Long(timeHelperBean.getEndTime(), DateFormatUtil.YMD_HMS));
        schedule.setWarnTime(timeHelperBean.getWarnTime());
        schedule.setWarnRealTime(DateFormatUtil.str2Long(timeHelperBean.getWarnRealTime(), DateFormatUtil.YMD_HMS));
        schedule.setAddress(timeHelperBean.getAddress());
        schedule.setDetails(timeHelperBean.getDetail());
        schedule.setStatus(timeHelperBean.getStatus() + "");

        return schedule;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mAnimationDrawable.start();
    }

    @Override
    protected void initData() {
        //添加弹出按钮
        final int itemCount = ITEM_DRAWABLES.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);

            final int position = i;
            mArcMenu.addItem(item, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGrayView.setVisibility(View.GONE);
                    switch (position) {
                        case 0:
                            startActivityForResult(new Intent(ct, SchedulerCreateActivity.class)
                                    .putExtra(Constants.Intents.ENABLE, true), 0x03);
                            break;
                        case 1:
                            startActivityForResult(
                                    new Intent("com.modular.oa.AddMeetingActivity"), 0x01);
                            break;
                        case 2:
                            startActivityForResult(
                                    new Intent("com.modular.booking.BookingListActivity")
                                            .putExtra("whichPage", "timeHelper"), 0x02);
                            break;
                        case 3:
                            Intent intent = new Intent();
                            boolean is = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
                            if (ApiUtils.getApiModel() instanceof ApiPlatform || is) {
                                intent = new Intent("com.modular.work.MissionActivity");
                                intent.putExtra("flag", 1);
                            } else {
                                intent = new Intent("com.modualr.appworks.OutofficeActivity");
                            }
                            boolean isAdmin = PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false);
                            //上传管理员状态
                            intent.putExtra(AppConfig.IS_ADMIN, isAdmin);
                            startActivityForResult(intent, 0x04);
                            break;
                        case 4:
                            intent = new Intent();
                            if (ApiUtils.getApiModel() instanceof ApiUAS) {
                                intent.setAction("com.modular.form.TravelDataFormDetailActivity");
                                String travelCaller = CommonUtil.getSharedPreferences(TimeHelperActivity.this, com.core.app.Constants.WORK_TRAVEL_CALLER_CACHE);
                                if (!TextUtils.isEmpty(travelCaller) && "FeePlease!CCSQ!new".equals(travelCaller)) {
                                    intent.putExtra("caller", travelCaller);
                                }
                            } else {
                                intent.setAction("com.modular.form.DataFormDetailActivity");
                            }
                            startActivityForResult(intent, 0x05);
                            break;
                    }
                }
            });
        }

        mCurrentDate = DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy年MM月dd日");
        mFormatDate = DateFormatUtil.long2Str(System.currentTimeMillis(), DateFormatUtil.YMD);
        String currentWeek = CalendarUtil.getWeek(mFormatDate);
        mDateTextView.setText(mCurrentDate + "（" + currentWeek + "）");

        getByDaySchedule(mFormatDate);
    }

    private void getByDaySchedule(String day) {
        mPresenter.httpRequest(mContext,
                "https://mobile.ubtob.com:8443/"
//                "http://192.168.253.130:8080/"
                ,
                new HttpParams.Builder()
                        .url("schedule/schedule/getByDaySchedule")
                        .method(Method.GET)
                        .flag(FLAG_GETBYDAY_SCHEDULE)
                        .addParam("imid", MyApplication.getInstance().getLoginUserId())
                        .addParam("day", day)
                        .addParam("uasUrl", CommonUtil.getAppBaseUrl(this))
                        .addParam("emcode", CommonUtil.getEmcode())
                        .addParam("master", CommonUtil.getMaster())
                        .addParam("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"))
                        .build());
    }

    @Override
    public void requestSuccess(int what, Object object) {
        try {
            String result = object.toString();
            switch (what) {
                case FLAG_GETBYDAY_SCHEDULE:
                    if (JSONUtil.validate(result)) {
                        LogUtil.prinlnLongMsg("getbyday", result);
                        mTimeHelperBeans.clear();
                        JSONObject resultObject = JSON.parseObject(result);
                        JSONArray dataArray = resultObject.getJSONArray("data");
                        if (dataArray != null && dataArray.size() > 0) {
                            long currentTimeMillis = System.currentTimeMillis();
                            int progress = -1;

                            Calendar cal = Calendar.getInstance();
                            cal.setTimeZone(TimeZone.getTimeZone("UTC+8"));
                            cal.setTime(new Date(DateFormatUtil.str2Long(mFormatDate, DateFormatUtil.YMD)));
                            cal.set(Calendar.HOUR, 0);
                            cal.set(Calendar.SECOND, 1);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            long startcal = cal.getTimeInMillis();
                            long endcal = cal.getTimeInMillis() + 24 * 60 * 60 * 1000;
                            List<Schedule> systemSchedule = ScheduleUtils.getSystemCalendar(MyApplication.getInstance(), startcal, endcal);

                            for (int i = 0; i < dataArray.size(); i++) {
                                JSONObject dataObject = dataArray.getJSONObject(i);
                                if (dataObject != null) {
                                    TimeHelperBean timeHelperBean = new TimeHelperBean();
                                    Object scheduleId = dataObject.get("scheduleId");
                                    if (scheduleId == null) {
                                        timeHelperBean.setScheduleId(-1);
                                    } else {
                                        timeHelperBean.setScheduleId(JSONUtil.getInt(dataObject, "scheduleId"));
                                    }
                                    timeHelperBean.setImid(JSONUtil.getInt(dataObject, "imid"));
                                    timeHelperBean.setType(JSONUtil.getText(dataObject, "type"));
                                    timeHelperBean.setAllDay(JSONUtil.getInt(dataObject, "allDay"));
                                    timeHelperBean.setRepeat(JSONUtil.getText(dataObject, "repeat"));
                                    timeHelperBean.setTitle(JSONUtil.getText(dataObject, "title"));
                                    timeHelperBean.setTag(JSONUtil.getText(dataObject, "tag"));
                                    timeHelperBean.setRemarks(JSONUtil.getText(dataObject, "remarks"));

                                    String startTime = JSONUtil.getText(dataObject, "startTime");
                                    timeHelperBean.setStartTime(startTime);
                                    long startLong = DateFormatUtil.str2Long(startTime, DateFormatUtil.YMD_HMS);
                                    if (currentTimeMillis > startLong) {
                                        progress++;
                                    }

                                    timeHelperBean.setEndTime(JSONUtil.getText(dataObject, "endTime"));
                                    timeHelperBean.setWarnTime(JSONUtil.getInt(dataObject, "warnTime"));
                                    timeHelperBean.setWarnRealTime(JSONUtil.getText(dataObject, "warnRealTime"));
                                    timeHelperBean.setAddress(JSONUtil.getText(dataObject, "address"));
                                    timeHelperBean.setStatus(JSONUtil.getInt(dataObject, "status"));
                                    timeHelperBean.setDetail(JSONUtil.getText(dataObject, "details"));
                                    int genre = JSONUtil.getInt(dataObject, "genre");
                                    timeHelperBean.setScheduleType(genre);
                                    if (genre == 1) {
                                        timeHelperBean.setFromWhere(Schedule.TYPE_BOOK);
                                    } else if (genre == 2) {
                                        timeHelperBean.setFromWhere(Schedule.TYPE_UU);
                                    }

                                    mTimeHelperBeans.add(timeHelperBean);

                                    if (!ListUtils.isEmpty(systemSchedule)) {
                                        for (Schedule e : systemSchedule) {
                                            if (e.getId() == timeHelperBean.getScheduleId()) {
                                                systemSchedule.remove(e);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (systemSchedule != null) {
                                for (int i = 0; i < systemSchedule.size(); i++) {
                                    Schedule schedule = systemSchedule.get(i);
                                    TimeHelperBean timeHelperBean = new TimeHelperBean();
                                    timeHelperBean.setScheduleId(schedule.getId());
                                    timeHelperBean.setImid(0);
                                    timeHelperBean.setType(schedule.getType());
                                    timeHelperBean.setAllDay(schedule.getAllDay());
                                    timeHelperBean.setRepeat(schedule.getRepeat());
                                    timeHelperBean.setTitle(schedule.getTitle());
                                    timeHelperBean.setTag(schedule.getTag());
                                    timeHelperBean.setRemarks(schedule.getRemarks());

                                    String startTime = DateFormatUtil.long2Str(schedule.getStartTime(), DateFormatUtil.YMD_HMS);
                                    timeHelperBean.setStartTime(startTime);
                                    long startLong = schedule.getStartTime();
                                    if (currentTimeMillis > startLong) {
                                        progress++;
                                    }

                                    timeHelperBean.setEndTime(DateFormatUtil.long2Str(schedule.getEndTime(), DateFormatUtil.YMD_HMS));
                                    timeHelperBean.setWarnTime(schedule.getWarnTime());
                                    timeHelperBean.setWarnRealTime(DateFormatUtil.long2Str(schedule.getWarnRealTime(), DateFormatUtil.YMD_HMS));
                                    timeHelperBean.setAddress(schedule.getAddress());
                                    timeHelperBean.setStatus(0);
                                    timeHelperBean.setDetail("");
                                    timeHelperBean.setScheduleType(2);
                                    timeHelperBean.setFromWhere(Schedule.TYPE_PHONE);

                                    mTimeHelperBeans.add(timeHelperBean);
                                }
                            }
                            Collections.sort(mTimeHelperBeans);

                            mTimeHelperAdapter.setTimeProgress(progress, 50);

                            mTimeHelperAdapter.notifyDataSetChanged();
                            if (mTimeHelperBeans.size() == 0) {
                                mEmptyTextView.setText("您今日的日程为空");
                                mTimeHelperAdapter.setEmptyView(mEmptyView);
                            }
                        } else {
                            mTimeHelperAdapter.notifyDataSetChanged();
                            mEmptyTextView.setText("您今日的日程为空");
                            mTimeHelperAdapter.setEmptyView(mEmptyView);
                        }
                    }

                    break;
            }
        } catch (Exception e) {
            LogUtil.prinlnLongMsg("getbyday", e.toString());
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        try {
            switch (what) {
                case FLAG_GETBYDAY_SCHEDULE:
                    toast(errorMsg);
                    break;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_time_helper, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.anim_activity_back_bottom_in, R.anim.anim_activity_back_top_out);
            return true;
        } else if (item.getItemId() == R.id.menu_time_helper_setting) {
            startActivityForResult(TimeHelperSettingActivity.class, null, 0x11);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 || requestCode == 0x02 || requestCode == 0x03 || requestCode == 0x04 || requestCode == 0x05 || requestCode == 0x44) {
            getByDaySchedule(mFormatDate);
        } else if ((requestCode == 0x33 || requestCode == 0x11) && resultCode == 0x11) {
            getByDaySchedule(mFormatDate);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_activity_back_bottom_in, R.anim.anim_activity_back_top_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_activity_back_bottom_in, R.anim.anim_activity_back_top_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnimationDrawable.stop();
    }

    private void toDetailActivty(Schedule mSchedule) throws Exception {
        if ("个人预约".equals(mSchedule.getType())) {
            Bundle bundle = new Bundle();
            BookingModel model = getBookingModelByString(mSchedule.getDetails());
            if (model == null) {
                ToastUtil.showToast(ct, R.string.error_message);
                return;
            }
            bundle.putParcelable("model", model);
            startActivityForResult(new Intent("com.modular.booking.BookingDetailActivity")
                    .putExtras(bundle), 0x44);
        } else if ("商务预约".equals(mSchedule.getType())) {
            Bundle bundle = new Bundle();
            BookingModel model = getBookingModelByString(mSchedule.getDetails());
            if (model == null) {
                ToastUtil.showToast(ct, R.string.error_message);
                return;
            }
            bundle.putParcelable("model", model);
            startActivityForResult(new Intent(mContext, BBookingDetailActivity.class)
                    .putExtras(bundle), 0x44);
        } else if ("会议".equals(mSchedule.getType())) {
            MeetEntity meetEntity = getMeetEntity(mSchedule.getDetails());
            startActivityForResult(new Intent(ct, MeetDetailsActivity.class)
                    .putExtra("data", meetEntity), 0x44);
        } else if ("外勤".equals(mSchedule.getType())) {
            MissionModel mission = WorkHandlerUtil.handlerEntity(false, JSON.parseObject(mSchedule.getDetails()));
            mission.setStatus(5);
            startActivityForResult(new Intent(ct, MissionActivity.class).putExtra("model", mission)
                    .putExtra("showSubmit", false), 0x44);
        } else if ("出差".equals(mSchedule.getType())) {
            if (JSONUtil.validateJSONObject(mSchedule.getDetails())) {
                JSONObject object = JSON.parseObject(mSchedule.getDetails());
                String caller = JSONUtil.getText(object, "caller", "CALLER");
                String status = JSONUtil.getText(object, "fp_status", "FP_STATUS");
                int keyId = JSONUtil.getInt(object, "fp_id", "FP_ID");
                startActivityForResult(new Intent(mContext,
                        CommonDocDetailsActivity.class)
                        .putExtra("caller", caller)
                        .putExtra("title", "出差单")
                        .putExtra("keyValue", keyId)
                        .putExtra("status", TextUtils.isEmpty(status) ? "已提交" : status), 0x44);
            }
        } else {
            startActivityForResult(new Intent(ct, SchedulerCreateActivity.class)
                    .putExtra(Constants.Intents.ENABLE, false)
                    .putExtra(Constants.Intents.MODEL, mSchedule), 0x33);
        }
    }

    private MeetEntity getMeetEntity(String jsonStr) {
        MeetEntity meetEntity = new MeetEntity();
        if (JSONUtil.validateJSONObject(jsonStr)) {
            JSONObject object = JSON.parseObject(jsonStr);
            String ma_code = JSONUtil.getText(object, "ma_code", "MA_CODE");//编号
            String ma_recorder = JSONUtil.getText(object, "ma_recorder", "MA_RECORDER");//发起人
            String ma_starttime = JSONUtil.getText(object, "ma_starttime", "MA_STARTTIME");//发起时间
            String ma_endtime = JSONUtil.getText(object, "ma_endtime", "MA_ENDTIME");//结束时间
            String ma_theme = JSONUtil.getText(object, "ma_theme", "MA_THEME");//主题
            String ma_remark = JSONUtil.getText(object, "ma_remark", "MA_REMARK");
            String ma_mrname = JSONUtil.getText(object, "ma_mrname", "MA_MRNAME");//会议地点
            String ma_mrcode = JSONUtil.getText(object, "ma_mrcode", "MA_MRCODE");//会议地点
            int ma_id = JSONUtil.getInt(object, "ma_id", "MA_ID");
            String ma_tag = JSONUtil.getText(object, "ma_tag", "MA_TAG");
            String status = JSONUtil.getText(object, "status", "STATUS");
            String ma_stage = JSONUtil.getText(object, "ma_stage", "MA_STAGE");
            String ma_type = JSONUtil.getText(object, "ma_type", "MA_TYPE");
            meetEntity.setMa_code(ma_code);
            meetEntity.setMa_recorder(ma_recorder);
            meetEntity.setMa_starttime(ma_starttime);
            meetEntity.setMa_endtime(ma_endtime);
            meetEntity.setMa_theme(ma_theme);
            meetEntity.setMa_remark(ma_remark);
            meetEntity.setMa_mrname(ma_mrname);
//            meetEntity.setMa_code(ma_mrcode);
            meetEntity.setMa_id(ma_id);
            meetEntity.setMa_tag(ma_tag);
            meetEntity.setStatus(status);
            meetEntity.setMa_stage(ma_stage);
            meetEntity.setMa_type(ma_type);
        }
        return meetEntity;
    }

    private BookingModel getBookingModelByString(String json) throws Exception {
        JSONArray array = JSON.parseArray(json);
        if (!ListUtils.isEmpty(array)) {
            JSONObject object = array.getJSONObject(0);
            BookingModel model = new BookingModel();
            model.setAb_id(JSONUtil.getText(object, "ab_id"));
            model.setAb_bman(JSONUtil.getText(object, "ab_bman"));
            model.setAb_bmanid(JSONUtil.getText(object, "ab_bmanid"));
            model.setAb_starttime(JSONUtil.getText(object, "ab_starttime"));
            model.setAb_endtime(JSONUtil.getText(object, "ab_endtime"));
            model.setAb_recorddate(JSONUtil.getText(object, "ab_recorddate"));
            model.setAb_recordid(JSONUtil.getText(object, "ab_recordid"));
            model.setAb_recordman(JSONUtil.getText(object, "ab_recordman"));
            model.setAb_content(JSONUtil.getText(object, "ab_content"));
            model.setAb_confirmstatus(JSONUtil.getText(object, "ab_confirmstatus"));
            model.setAb_sharestatus(JSONUtil.getText(object, "ab_sharestatus"));
            model.setAb_address(JSONUtil.getText(object, "ab_address"));
            model.setAb_longitude(JSONUtil.getText(object, "ab_longitude"));
            model.setAb_latitude(JSONUtil.getText(object, "ab_latitude"));
            model.setAb_type(JSONUtil.getText(object, "ab_type"));
            model.setAb_reason(JSONUtil.getText(object, "ab_reason"));
            model.setAd_reason(JSONUtil.getText(object, "ab_reason"));
            return model;
        }
        return null;
    }

    private void showDateDialog(String year, String month, String day) {
        OASigninPicker picker = new OASigninPicker((Activity) mContext, 2000, 2030);
        picker.setRange(2030, 12, 31);
        try {
            picker.setSelectedItem(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        } catch (Exception e) {
            picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        }
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                mCurrentDate = year + "年" + month + "月" + day + "日";
                mFormatDate = year + "-" + month + "-" + day;

                String currentWeek = CalendarUtil.getWeek(mFormatDate);
                mDateTextView.setText(mCurrentDate + "（" + currentWeek + "）");

                getByDaySchedule(mFormatDate);
            }
        });
        picker.show();
    }
}
