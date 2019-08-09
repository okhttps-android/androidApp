package com.modular.apptasks.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apptasks.util.AlarmUtil;
import com.uas.appworks.model.Schedule;

/**
 * 日程后台计算控制器
 */
public class SchedulePresenter {
    private static SchedulePresenter instance;

    public static SchedulePresenter getInstance() {
        if (instance == null) {
            synchronized (SchedulePresenter.class) {
                if (instance == null) {
                    instance = new SchedulePresenter();
                }
            }
        }
        return instance;
    }


    protected HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            String baseUrl = CommonUtil.getSchedulerBaseUrl();
            if (!StringUtil.isEmpty(baseUrl)) {
                httpClient = new HttpClient.Builder(baseUrl).isDebug(true)
                        .connectTimeout(5000)
                        .readTimeout(5000).build();
            }
        }
        return httpClient;
    }

    public void startSchedule() {
        getHttpClient().Api().send(new HttpClient.Builder()
                .url("schedule/byDaySchedule")
                .add("imid", MyApplication.getInstance().getLoginUserId())
                .add("day", DateFormatUtil.long2Str(DateFormatUtil.YMD))
                .build(), new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    JSONObject object = JSON.parseObject(o.toString());
                    handlerDay(JSONUtil.getJSONArray(object, "data"));
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Object t) {

            }
        }));
    }

    private void handlerDay(JSONArray jsonArray) throws Exception {
        if (!ListUtils.isEmpty(jsonArray)) {
//            Date date = new Date();
//            Calendar cal = Calendar.getInstance();
//            cal.setTimeZone(TimeZone.getTimeZone("UTC+8"));
//            cal.setTime(date);
//            cal.set(Calendar.HOUR, 0);
//            cal.set(Calendar.SECOND, 1);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.MILLISECOND, 0);
//            long startTime = cal.getTimeInMillis();
//            long endTime = cal.getTimeInMillis() + 24 * 60 * 60 * 1000;
//            List<Schedule> systemSchedule = ScheduleUtils.getSystemCalendar(MyApplication.getInstance(), startTime, endTime);
//            for (Schedule e : systemSchedule) {
//                LogUtil.i("gong", "e=" + JSON.toJSONString(e));
//            }
            long thisTime = System.currentTimeMillis();
            int needIndex = -1;
            long needTime = -1;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                long time = JSONUtil.getTime(object, "warnRealTime");
                if (time > thisTime) {
                    if (needTime < 0 || needTime > time) {
                        needTime = time;
                        needIndex = i;
                    }
                }
            }
            if (needIndex >= 0 && needIndex < jsonArray.size()) {
                Schedule mSchedule = getScheduleByObject(jsonArray.getJSONObject(needIndex));
                //获取到符合提醒的日程
                AlarmUtil.cancelAlarm(AlarmUtil.ID_SCHEDULE, AlarmUtil.ACTION_SCHEDULE);
                AlarmUtil.startAlarm(AlarmUtil.ID_SCHEDULE, AlarmUtil.ACTION_SCHEDULE, mSchedule.getWarnRealTime(), mSchedule);
            }
        }
    }


    private Schedule getScheduleByObject(JSONObject object) {
        Schedule chche = new Schedule(true);
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
        return chche;
    }
}
