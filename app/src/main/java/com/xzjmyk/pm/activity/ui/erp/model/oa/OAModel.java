package com.xzjmyk.pm.activity.ui.erp.model.oa;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.utils.TimeUtils;

import java.util.Calendar;

/**
 * Created by Bitliker on 2017/3/28.
 */

public class OAModel {
    //关于OA类型常量表
    public static final int OA_TYPE_TASK = 0x11;//任务，日程
    public static final int OA_TYPE_MISSION = 0x12;//外勤单
    public static final int OA_TYPE_RECORD = 0x13;//拜访报告

    private boolean isMe;//是否是我的单，可能是下属的
    private int type;//类型
    private long startdate;
    private long enddate;
    private long recorddate;//录入时间
    private String title;
    private String remark;//备注、目的
    private String address;//
    private String recorder;//录入人
    private String status;
    private String  kind;
    private String handler;
    private String json;

   public boolean isAllDay(){
       return kind==null?false:(kind.equals("全天"));
   }
    public void setKind(String kind) {
        this.kind = kind;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public long getStartdate() {
        return startdate;
    }

    public void setStartdate(long startdate) {
        this.startdate = startdate;
    }

    public long getEnddate() {
        return enddate;
    }

    public void setEnddate(long enddate) {
        this.enddate = enddate;
    }

    public long getRecorddate() {
        return recorddate;
    }

    public void setRecorddate(long recorddate) {
        this.recorddate = recorddate;
    }

    public String getTitle() {
        return StringUtil.isEmpty(title) ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return StringUtil.isEmpty(remark) ? "" : remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAddress() {
        return StringUtil.isEmpty(address) ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRecorder() {
        if (StringUtil.isEmpty(recorder))
            return "";
        return recorder;
    }

    public void setRecorder(String recorder) {
        this.recorder = recorder;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    //在Oa界面适配器中使用的
    public boolean isTask() {
        return type == OA_TYPE_TASK;
    }

    public boolean isMission() {
        return type == OA_TYPE_MISSION;
    }

    public boolean isVisitRecord() {
        return type == OA_TYPE_RECORD;
    }

    public String getTitle2Remark() {
        return getTitle() + (isTask() ? "" : "  " + getRemark());
    }

    public String getStatus() {
        return StringUtil.isEmpty(status) ? "" : status;
    }


    public String getAddress2Time() {
        return isTask() ? getTime2Str("yyyy-MM-dd HH:mm") : getAddress();
    }

    /*唯一判断是否当天时间以前的*/
    public boolean isTadayBefore() {
        return getFilterTime().compareTo(DateFormatUtil.long2Str(DateFormatUtil.YMD)) < 0;
    }


    public String getHandler() {
        if (StringUtil.isEmpty(handler))
            return StringUtil.isEmpty(recorder) ? "" : recorder;
        return handler;
    }

    public String getStringByJson(String... keys) {
        if (keys == null || keys.length <= 0 || StringUtil.isEmpty(json) || !JSONUtil.validate(json))
            return "";
        JSONObject object = JSON.parseObject(json);
        return JSONUtil.getText(object, keys);

    }

    public int getDay() {
        Calendar c = Calendar.getInstance();
        long time = getTime(recorddate, startdate, enddate);
        if (time == 0) {
            return -1;
        }
        c.setTimeInMillis(time);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public String getTime2Str(String formart) {
        StringBuilder builder = new StringBuilder(" ");
        if (startdate > 0) {
            builder.append(DateFormatUtil.long2Str(startdate, formart));
            if (enddate > startdate)
                builder.append("--" + DateFormatUtil.long2Str(enddate, formart));
        }
        return builder.toString();
    }

    /*获取显示时间，yyyy-MM-dd*/
    public String getFilterTime() {
        long time = 0;
        if (recorddate > 0)
            time = recorddate;
        else if (enddate > 0)
            time = enddate;
        else time = startdate;
        return TimeUtils.s_long_2_str(time);
    }

    private long getTime(long... times) {
        if (times == null) return 0;
        for (long e : times) if (e != 0) return e;
        return 0;
    }
}
