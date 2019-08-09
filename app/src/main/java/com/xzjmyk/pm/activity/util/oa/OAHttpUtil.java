package com.xzjmyk.pm.activity.util.oa;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.xzjmyk.pm.activity.ui.erp.model.oa.OAModel;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * OA首页接口方面请求和处理方法处理工具
 * Created by Bitliker on 2017/3/28.
 */

public class OAHttpUtil {
    /*获取任务列表*/
    public Set<Integer> getTaskList(Date curDate, List<OAModel> models) {
        Set<Integer> taskList = new HashSet<>();
        if (ListUtils.isEmpty(models)) return taskList;
        for (OAModel e : models) {
            int day = e.getDay();
            if (day != -1)
                taskList.add(day);
        }
        return taskList;
    }

    /*获取OAmodel列表*/
    public List<OAModel> getModelByJSON(JSONObject object, String key, boolean isCrm, boolean isMe) throws Exception {
        List<OAModel> models = new ArrayList<>();
        if (StringUtil.isEmpty(key)) return models;
        JSONObject o = JSONUtil.getJSONObject(object, key);
        if (o == null) return models;
        if (!isCrm) {
            models.addAll(getModelByJSON(o, isMe, OAModel.OA_TYPE_TASK, "task"));//添加任务日程
            models.addAll(getModelByJSON(o, isMe, OAModel.OA_TYPE_TASK, "schedule"));//添加任务日程
            models.addAll(getModelByJSON(o, isMe, OAModel.OA_TYPE_MISSION, "outPlan", "outplan"));//外勤计划
        }
        models.addAll(getModelByJSON(o, isMe, OAModel.OA_TYPE_RECORD, "visitRecord"));//拜访报告
        return models;
    }


    private List<OAModel> getModelByJSON(JSONObject object, boolean isMe, int type, String... keys) throws Exception {
        JSONArray array = JSONUtil.getJSONArray(object, keys);
        List<OAModel> models = new ArrayList<>();
        if (ListUtils.isEmpty(array)) return models;
        OAModel model = null;
        JSONObject o = null;
        for (int i = 0; i < array.size(); i++) {
            o = array.getJSONObject(i);
            if (o == null) continue;
            model = new OAModel();
            model.setType(type);
            model.setMe(isMe);
            model.setStartdate(getTimeMillis(o, "vr_startdate", "startdate", "mpd_actdate", "mp_firsttime", "vr_visittime"));
            model.setEnddate(getTimeMillis(o, "enddate", "vr_enddate", "mpd_outdate", "mp_lasttime", "vr_visitend"));
            //录入时间作为选择时间，如果没有录入，使用实际到达时间或是预计到达时间或开始时间
            model.setRecorddate(getTimeMillis(o, "recorddate", "vr_recorddate", "vr_date", "mpd_arrivedate", "startdate", "mpd_actdate"));
            model.setTitle(JSONUtil.getText(o, "mpd_company", "name", "vr_cuname", "taskname", "custname"));
            model.setRemark(JSONUtil.getText(o, "mpd_remark", "vr_nichestep", "vr_detail", "detail", "description"));
            model.setAddress(JSONUtil.getText(o, "vr_cuaddress", "mpd_address", "address"));
            model.setRecorder(JSONUtil.getText(o, "vr_emname", "recorder", "mp_recorder"));
            //外勤计划的实行人就是录入人
            model.setHandler(JSONUtil.getText(o, "handler", "visitman", "vr_emname", "doman"));
            model.setStatus(JSONUtil.getText(o, "mpd_status", "status"));
            model.setJson(o.toString());
            if (type == OAModel.OA_TYPE_MISSION) {
                model.setKind(JSONUtil.getText(o, "mpd_kind"));
            }
            if (canShow(model))
                models.add(model);
        }
        if (!ListUtils.isEmpty(models)) {
            Collections.sort(models, new Comparator<OAModel>() {
                @Override
                public int compare(OAModel lhs, OAModel rhs) {
                    return (int) (rhs.getRecorddate() - lhs.getRecorddate());
                }
            });
        }
        return models;
    }

    private long getTimeMillis(JSONObject object, String... keys) {
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            return JSONUtil.getLong(object, keys);
        }
        String timeStr = JSONUtil.getText(object, keys);
        if (!StringUtil.isEmpty(timeStr))
            return DateFormatUtil.str2Long(timeStr, DateFormatUtil.YMD_HMS);
        return 0;
    }

    public boolean canShow(OAModel e) {
        String emcode = CommonUtil.getEmcode();
        if (e.isMission() && e.getStatus().equals("已完成"))
            return false;
        else if (e.isTask() && "待确认".equals(e.getStatus()) && emcode.equals(e.getStringByJson("domancode")))
            return false;
        return true;
    }

    public boolean canShow(OAModel e, Date curDate) {
        return StringUtil.isInclude(e.getFilterTime(), DateFormatUtil.getFormat(DateFormatUtil.YMD).format(curDate));
    }

    public boolean isTaskOk(OAModel e) {
        if (e.isVisitRecord() || "已拜访".equals(e.getStatus()) || "已完成".equals(e.getStatus()) || isMissionOk(e))
            return true;
        return false;
    }

    public boolean isMissionOk(OAModel e) {
        if (!e.isMission()) return false;
        //当天以前的内容
        if (e.isTadayBefore() && e.getStartdate() > 0 && e.getEnddate() > 0 && e.getStartdate() != e.getEnddate()) {
            return true;
        } else {
            String status = e.getStatus();
            if ("签退".equals(status)) {
                if (e.isAllDay()) {
                    long five = DateFormatUtil.str2Long(DateFormatUtil.long2Str("yyyy-MM-dd") + " 17:00:00", "yyyy-MM-dd HH:mm:ss");
                    if (five < System.currentTimeMillis()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}
