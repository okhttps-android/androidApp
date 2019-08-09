package com.uas.appworks.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiUAS;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.model.MissionModel;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.WorkHandlerUtil;
import com.core.utils.sp.UserSp;
import com.core.widget.view.MyGridView;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.adapter.EasyBaseAdapter;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.EasyBaseModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.widget.compactcalender.CompactCalendarView;
import com.modular.apputils.widget.compactcalender.Lunar;
import com.modular.apputils.widget.compactcalender.domain.Event;
import com.modular.booking.activity.business.BBookingDetailActivity;
import com.modular.booking.activity.services.BServiceAddActivity;
import com.modular.booking.activity.services.BServicesActivity;
import com.modular.booking.activity.shares.BBSharesListActivity;
import com.modular.booking.model.BookingModel;
import com.modular.booking.model.SBListModel;
import com.uas.appworks.OA.erp.activity.AddMeetingActivity;
import com.uas.appworks.OA.erp.activity.CommonDocDetailsActivity;
import com.uas.appworks.OA.erp.activity.MeetDetailsActivity;
import com.uas.appworks.OA.erp.activity.MissionActivity;
import com.uas.appworks.OA.erp.model.MeetEntity;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.utils.ScheduleUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ScheduleActivity extends BaseNetActivity {
    private final int LOAD_MONTH = 11;
    private final int LOAD_DAY = 12;

    private CompactCalendarView compactCalendarView;
    private RecyclerView mRecyclerView;
    private TextView monthTv, lunarCalendarTv, newDayTv;

    private Date mCurrentDate;
    private Date selectDate;
    private String lastMonth;
    private String mCurrentDateStr;
    private ScheduleAdapter mScheduleAdapter;
    private ArrayList<BookingModel> mShareDatas = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            startActivity(new Intent(ct, ScheduleSearchActivity.class));
        } else if (item.getItemId() == R.id.setting) {
            startActivity(new Intent(ct, ScheduleSettingActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_schedule;
    }

    @Override
    protected void init() throws Exception {
        initAdd();
        initView();
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getSchedulerBaseUrl();
    }


    private void initAdd() {
        MyGridView addGridView = findViewById(R.id.addGridView);
        addGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch ((int) l) {
                    case 0:
                        startActivityForResult(new Intent("com.ui.erp.activity.secretary.BookingAddActivity")
                                .putExtra("whichPage", "ScheduleActivity"), 0x11);
                        break;
                    case 1:
                        startActivity(new Intent(mContext, BServicesActivity.class));
                        break;
                    case 2:
                        startActivityForResult(new Intent(ct, SchedulerCreateActivity.class), 0x11);
                        break;
                    case 3:
                        startActivityForResult(new Intent(ct, BBSharesListActivity.class).putParcelableArrayListExtra("model", mShareDatas), 0x11);
                        break;
                    case 4:
                        startActivityForResult(new Intent(ct, MissionActivity.class), 0x11);
                        break;
                    case 5:
                        Intent intent = new Intent("com.modular.form.TravelDataFormDetailActivity");
                        if (ApiUtils.getApiModel() instanceof ApiUAS) {
                            String travelCaller = CommonUtil.getSharedPreferences(ct, com.core.app.Constants.WORK_TRAVEL_CALLER_CACHE);
                            if (!TextUtils.isEmpty(travelCaller) && "FeePlease!CCSQ!new".equals(travelCaller)) {
                                intent.putExtra("caller", travelCaller);
                            }
                        }
                        intent.setAction("com.modular.form.DataFormDetailActivity");
                        startActivityForResult(intent, 0x11);
                        break;
                    case 6:
                        startActivityForResult(new Intent(ct, AddMeetingActivity.class)
                                .putExtra("whichPage", "ScheduleActivity"), 0x11);
                        break;
                }
            }
        });
        addGridView.setAdapter(new EasyBaseAdapter(ct, getAddItems()) {
            @Override
            public View bindView(View view, int position, EasyBaseModel model) {
                ViewHolder mViewHolder = null;
                if (view.getTag() == null) {
                    mViewHolder = new ViewHolder();
                    mViewHolder.iconIv = view.findViewById(R.id.iconIv);
                    mViewHolder.titleTv = view.findViewById(R.id.titleTv);
                    view.setTag(mViewHolder);
                } else {
                    mViewHolder = (ViewHolder) view.getTag();
                }
                mViewHolder.iconIv.setImageResource(model.getIconId());
                mViewHolder.titleTv.setText(model.getTitle());
                return view;
            }

            class ViewHolder {
                private ImageView iconIv;
                private TextView titleTv;
            }

            @Override
            public int getLayoutRes() {
                return R.layout.item_schedule_add_menu;
            }
        });
    }

    private List<EasyBaseModel> getAddItems() {
        List<EasyBaseModel> models = new ArrayList<>();
        models.add(new EasyBaseModel().setTitle("个人预约").setIconId(R.drawable.booking_personal));
        models.add(new EasyBaseModel().setTitle("服务预约").setIconId(R.drawable.booking_service));
        models.add(new EasyBaseModel().setTitle("新建日程").setIconId(R.drawable.ic_create_scheduler_menu));
        models.add(new EasyBaseModel().setTitle("预约共享").setIconId(R.drawable.booking_share));
        models.add(new EasyBaseModel().setTitle("外勤计划").setIconId(R.drawable.ic_create_mission_menu));
        models.add(new EasyBaseModel().setTitle("出差申请").setIconId(R.drawable.ic_create_work_out_menu));
        models.add(new EasyBaseModel().setTitle("会议申请").setIconId(R.drawable.ic_create_meeting_menu));
        return models;
    }

    private void initView() {
        mCurrentDate = new Date();
        mCurrentDateStr = DateFormatUtil.date2Str(mCurrentDate, "yyyy-MM-dd");
        compactCalendarView = findViewById(R.id.compactcalendar_view);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        monthTv = findViewById(R.id.monthTv);
        newDayTv = findViewById(R.id.newDayTv);
        lunarCalendarTv = findViewById(R.id.lunarCalendarTv);
        compactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        compactCalendarView.setIsRtl(false);
        compactCalendarView.displayOtherMonthDays(false);
        compactCalendarView.setLocale(TimeZone.getDefault(), Locale.CHINESE);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        setAdapter(null);
        changeDate(mCurrentDate);
        newDayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDayTv.setVisibility(View.GONE);
                compactCalendarView.setCurrentDate(mCurrentDate);
                changeDate(mCurrentDate);
            }
        });
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                changeDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                changeDate(firstDayOfNewMonth);
            }
        });
    }


    private void changeDate(Date date) {
        this.selectDate = date;
        String dateStr = DateFormatUtil.date2Str(date, DateFormatUtil.YMD);
        lunarCalendarTv.setText(new Lunar(date).toString());
        monthTv.setText(dateStr);
        newDayTv.setVisibility(dateStr.equals(mCurrentDateStr) ? View.GONE : View.VISIBLE);
        String ym = DateFormatUtil.date2Str(date, DateFormatUtil.YM);
        if (StringUtil.isEmpty(lastMonth) || !ym.equals(lastMonth)) {
            lastMonth = ym;
            loadByMonth(lastMonth);
            loadByDay(dateStr);
        } else {
            loadByDay(dateStr);
        }
    }

    private void loadListData(String month) {
        if (TextUtils.isEmpty(month)) return;
        month = month.replace("-", "");
        String url = com.core.app.Constants.IM_BASE_URL() + "user/appBookingList";
        Map<String, Object> params = new HashMap<>();
        params.put("token", UserSp.getInstance(MyApplication.getInstance()).getAccessToken(""));
        params.put("userid", MyApplication.getInstance().mLoginUser.getUserId());
        params.put("telephone", UserSp.getInstance(MyApplication.getInstance()).getTelephone(""));
        params.put("yearmonth", month);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x01, null, null, "post");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0x01) {
                    String result = msg.getData().getString("result");
                    JSONArray shareArray = JSON.parseArray(JSON.parseObject(result).getString("sharelist"));
                    mShareDatas.clear();
                    if (shareArray != null) {
                        for (int i = 0; i < shareArray.size(); i++) {
                            JSONObject object = shareArray.getJSONObject(i);
                            BookingModel model = new BookingModel();
                            model.setAb_address(object.getString("ab_address"));
                            model.setAb_bman(object.getString("ab_bman"));
                            model.setAb_bmanid(object.getString("ab_bmanid"));
                            model.setAd_reason(object.getString("ad_reason"));
                            model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                            model.setAb_content(object.getString("ab_content"));
                            model.setAb_endtime(object.getString("ab_endtime"));
                            model.setAb_id(object.getString("ab_id"));
                            model.setAb_latitude(object.getString("ab_latitude"));
                            model.setAb_longitude(object.getString("ab_longitude"));
                            model.setAb_recorddate(object.getString("ab_recorddate"));
                            model.setAb_recordid(object.getString("ab_recordid"));
                            model.setAb_recordman(object.getString("ab_recordman"));
                            model.setAb_sharestatus(object.getString("ab_sharestatus"));
                            model.setAb_starttime(object.getString("ab_starttime"));
                            model.setAb_type(object.getString("ab_type"));
                            model.setKind(object.getString("kind"));
                            mShareDatas.add(model);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    };

    private void loadByMonth(String month) {
        requestHttp(new Parameter.Builder()
                        .addParams("imid", MyApplication.getInstance().getLoginUserId())
                        .addParams("month", month)
                        .addParams("uasUrl", CommonUtil.getAppBaseUrl(ct))
                        .addParams("emcode", CommonUtil.getEmcode())
                        .addParams("master", CommonUtil.getMaster())
                        .addParams("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"))
                        .url("schedule/getByMonthSchedule")
                        .record(LOAD_MONTH)
                , mOnSmartHttpListener);
        loadListData(month);
    }

    private void loadByDay(String dateStr) {
        showProgress();
        requestHttp(new Parameter.Builder()
                        .addParams("imid", MyApplication.getInstance().getLoginUserId())
                        .addParams("day", dateStr)
                        .addParams("uasUrl", CommonUtil.getAppBaseUrl(ct))
                        .addParams("emcode", CommonUtil.getEmcode())
                        .addParams("master", CommonUtil.getMaster())
                        .addParams("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"))
                        .url("schedule/getByDaySchedule")
                        .record(LOAD_DAY)
                , mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            try {
                JSONObject object = JSON.parseObject(message);
                switch (what) {
                    case LOAD_MONTH:
                        handlerMonth(JSONUtil.getJSONArray(object, "data"));
                        break;
                    case LOAD_DAY:
                        handlerDay(JSONUtil.getJSONArray(object, "data"));
                        break;
                }
                dismissProgress();
            } catch (Exception e) {
                LogUtil.i("gong", "e=" + e.getMessage());
            }

        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {
            dismissProgress();
        }
    };


    private void handlerDay(JSONArray jsonArray) throws Exception {
        List<Schedule> schedules = null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC+8"));
        cal.setTime(this.selectDate);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();
        long endTime = cal.getTimeInMillis() + 24 * 60 * 60 * 1000;
        List<Schedule> systemSchedule = null;
        try {
            systemSchedule = ScheduleUtils.getSystemCalendar(MyApplication.getInstance(), startTime, endTime);
        } catch (Exception e) {

        }
        if (!ListUtils.isEmpty(jsonArray)) {
            schedules = new ArrayList<>();
            Schedule chche = null;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                chche = new Schedule(true);
                chche.setId(JSONUtil.getInt(object, "scheduleId"));
                chche.setType(JSONUtil.getText(object, "type"));
                chche.setAllDay(JSONUtil.getInt(object, "allDay"));
                chche.setRepeat(JSONUtil.getText(object, "repeat"));
                chche.setTitle(JSONUtil.getText(object, "title"));
                chche.setTag(JSONUtil.getText(object, "tag"));
                chche.setRemarks(JSONUtil.getText(object, "remarks"));
                chche.setStartTime(JSONUtil.getTime(object, "startTime"));
                chche.setEndTime(JSONUtil.getTime(object, "endTime"));
                chche.setWarnRealTime(JSONUtil.getTime(object, "warnRealTime"));
                chche.setWarnTime(JSONUtil.getInt(object, "warnTime"));
                chche.setAddress(JSONUtil.getText(object, "address"));
                chche.setStatus(JSONUtil.getText(object, "status"));
                chche.setDetails(JSONUtil.getText(object, "details"));
                schedules.add(chche);
                if (!ListUtils.isEmpty(systemSchedule)) {
                    for (Schedule e : systemSchedule) {
                        if (e.getId() == chche.getId()) {
                            systemSchedule.remove(e);
                            break;
                        }
                    }
                }
            }
            if (!ListUtils.isEmpty(systemSchedule)) {
                schedules.addAll(systemSchedule);
            }
        } else {
            schedules = systemSchedule;
        }
        setAdapter(schedules);
    }

    private void handlerMonth(JSONArray jsonArray) throws Exception {
        compactCalendarView.removeAllEvents();
        if (!ListUtils.isEmpty(jsonArray)) {
            List<Event> mEvents = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                String time = jsonArray.getString(i);
                mEvents.add(new Event(Color.argb(255, 169, 68, 65), DateFormatUtil.str2Long(time, DateFormatUtil.YMD)));
            }
            compactCalendarView.addEvents(mEvents);
        }
    }

    private void setAdapter(List<Schedule> schedules) {
        if (mScheduleAdapter == null) {
            mScheduleAdapter = new ScheduleAdapter(schedules);
            mRecyclerView.setAdapter(mScheduleAdapter);
        } else {
            mScheduleAdapter.setSchedules(schedules);
            mScheduleAdapter.notifyDataSetChanged();
        }
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Schedule> mSchedules = null;

        public void setSchedules(List<Schedule> mSchedules) {
            this.mSchedules = mSchedules;
        }

        public ScheduleAdapter(List<Schedule> mSchedules) {
            this.mSchedules = mSchedules;
        }

        @Override
        public int getItemViewType(int position) {
            return position > ListUtils.getSize(mSchedules) - 1 ? -1 : 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 1) {
                return new ViewHoder(LayoutInflater.from(ct).inflate(R.layout.item_schedule_bottom, parent, false));
            } else if (viewType == -1) {
                return new CreateViewHoder(LayoutInflater.from(ct).inflate(R.layout.item_create_schedule, parent, false));

            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHoder && ListUtils.getSize(this.mSchedules) > position) {
                ViewHoder mViewHoder = (ViewHoder) holder;
                Schedule mSchedule = this.mSchedules.get(position);
                if (mSchedule.getAllDay() == 1) {
                    mViewHoder.timeTv.setVisibility(View.VISIBLE);
                    mViewHoder.startTimeTv.setVisibility(View.GONE);
                    mViewHoder.endTimeTv.setVisibility(View.GONE);
                } else {
                    mViewHoder.timeTv.setVisibility(View.GONE);
                    mViewHoder.startTimeTv.setVisibility(View.VISIBLE);
                    mViewHoder.endTimeTv.setVisibility(View.VISIBLE);
                    String startTime = DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.HM);
                    String endTime = DateFormatUtil.long2Str(mSchedule.getEndTime(), DateFormatUtil.HM);
                    mViewHoder.startTimeTv.setText(startTime);
                    mViewHoder.endTimeTv.setText(endTime);
                }
                mViewHoder.contentTv.setText(StringUtil.isEmpty(mSchedule.getRemarks()) ? "" : mSchedule.getRemarks());
                mViewHoder.typeTv.setText("来自" + (StringUtil.isEmpty(mSchedule.getType()) ? "" : mSchedule.getType()));

                if (Schedule.TYPE_UU.equals(mSchedule.getType())) {
                    mViewHoder.tagTv.setText(StringUtil.isEmpty(mSchedule.getTag()) ? "" : mSchedule.getTag());
                    mViewHoder.tagTv.setVisibility(View.VISIBLE);
                } else {
                    mViewHoder.tagTv.setVisibility(View.GONE);
                }
                mViewHoder.itemView.setTag(mSchedule);
                mViewHoder.itemView.setOnClickListener(mOnClickListener);

            } else if (holder instanceof CreateViewHoder) {
                ((CreateViewHoder) holder).createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(new Intent(ct, SchedulerCreateActivity.class), 0x11);
                    }
                });
                ((CreateViewHoder) holder).createBookBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(new Intent("com.modular.booking.BookingListActivity").putExtra("whichPage", "ScheduleActivity"), 0x11);
                    }
                });
            }
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (view.getTag() != null && view.getTag() instanceof Schedule) {
                        Schedule mSchedule = (Schedule) view.getTag();
                        toDetailActivty(mSchedule);
                    }
                } catch (Exception e) {
                }
            }
        };

        private void toDetailActivty(Schedule mSchedule) throws Exception {
            if ("个人预约".equals(mSchedule.getType())) {
                Bundle bundle = new Bundle();
                BookingModel model = getBookingModelByString(mSchedule.getDetails());
                if (model == null) {
                    ToastUtil.showToast(ct, R.string.error_message);
                    return;
                }
                bundle.putParcelable("model", model);
                startActivity(new Intent("com.modular.booking.BookingDetailActivity")
                        .putExtras(bundle));
            } else if ("商务预约".equals(mSchedule.getType())) {
                Bundle bundle = new Bundle();
                BookingModel model = getBookingModelByString(mSchedule.getDetails());
                if (model == null) {
                    ToastUtil.showToast(ct, R.string.error_message);
                    return;
                }
                bundle.putParcelable("model", model);
                startActivity(new Intent(ct, BBookingDetailActivity.class)
                        .putExtras(bundle));
            } else if ("会议".equals(mSchedule.getType())) {
                MeetEntity meetEntity = getMeetEntity(mSchedule.getDetails());
                startActivity(new Intent(ct, MeetDetailsActivity.class)
                        .putExtra("data", meetEntity));
            } else if ("外勤".equals(mSchedule.getType())) {
                MissionModel mission = WorkHandlerUtil.handlerEntity(false, JSON.parseObject(mSchedule.getDetails()));
                mission.setStatus(5);
                startActivity(new Intent(ct, MissionActivity.class).putExtra("model", mission)
                        .putExtra("showSubmit", false));
            } else if ("出差".equals(mSchedule.getType())) {
                if (JSONUtil.validateJSONObject(mSchedule.getDetails())) {
                    JSONObject object = JSON.parseObject(mSchedule.getDetails());
                    String caller = JSONUtil.getText(object, "caller", "CALLER");
                    String status = JSONUtil.getText(object, "fp_status", "FP_STATUS");
                    int keyId = JSONUtil.getInt(object, "fp_id", "FP_ID");
                    startActivity(new Intent(mContext,
                            CommonDocDetailsActivity.class)
                            .putExtra("caller", caller)
                            .putExtra("title", "出差单")
                            .putExtra("keyValue", keyId)
                            .putExtra("status", TextUtils.isEmpty(status) ? "已提交" : status));
                }
            } else {
                startActivityForResult(new Intent(ct, SchedulerCreateActivity.class)
                        .putExtra(Constants.Intents.ENABLE, false)
                        .putExtra(Constants.Intents.MODEL, mSchedule), 0x11);
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

        @Override
        public int getItemCount() {
            return ListUtils.getSize(mSchedules);
        }

        class ViewHoder extends RecyclerView.ViewHolder {
            private TextView timeTv;
            private TextView startTimeTv;
            private TextView endTimeTv;
            private TextView contentTv;
            private TextView typeTv;
            private TextView tagTv;


            public ViewHoder(View itemView) {
                super(itemView);
                timeTv = (TextView) itemView.findViewById(R.id.timeTv);
                startTimeTv = (TextView) itemView.findViewById(R.id.startTimeTv);
                endTimeTv = (TextView) itemView.findViewById(R.id.endTimeTv);
                contentTv = (TextView) itemView.findViewById(R.id.contentTv);
                typeTv = (TextView) itemView.findViewById(R.id.typeTv);
                tagTv = (TextView) itemView.findViewById(R.id.tagTv);
            }
        }

        class CreateViewHoder extends RecyclerView.ViewHolder {
            Button createBtn, createBookBtn;

            public CreateViewHoder(View itemView) {
                super(itemView);
                createBtn = itemView.findViewById(R.id.createBtn);
                createBookBtn = itemView.findViewById(R.id.createBookBtn);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x11) {
            loadByMonth(lastMonth);
            String dayStr = TextUtils.isEmpty(monthTv.getText()) ? DateFormatUtil.long2Str(DateFormatUtil.YMD) : monthTv.getText().toString();
            loadByDay(dayStr);
        }
    }
}
