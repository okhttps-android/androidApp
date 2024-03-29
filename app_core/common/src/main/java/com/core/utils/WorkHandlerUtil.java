package com.core.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.dao.WorkLocationDao;
import com.core.model.MissionModel;
import com.core.model.OAConfig;
import com.core.model.WorkLocationModel;
import com.core.model.WorkModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.core.utils.TimeUtils.f_str_2_long;

/**
 * 班次数据处理,因为多处使用该逻辑，所以。。。。。。合并在一个工具类里面
 * Created by Bitliker on 2017/2/22.
 */

public class WorkHandlerUtil {

    /**
     * 处理班次数据
     *
     * @param object
     * @return
     * @throws Exception
     */
    public static ArrayList<WorkModel> handlerWorkData(JSONObject object, boolean isb2b) throws Exception {
        ArrayList<WorkModel> models = new ArrayList<>();
        String class1 = isb2b ? "class1" : "Class1";
        String class2 = isb2b ? "class2" : "Class2";
        String class3 = isb2b ? "class3" : "Class3";
        if (object.containsKey(class1)) {
            handlerByWork(models, object.getJSONObject(class1));
        }
        if (object.containsKey(class2)) {
            handlerByWork(models, object.getJSONObject(class2));
        }
        if (object.containsKey(class3)) {
            handlerByWork(models, object.getJSONObject(class3));
        }
        Object wd_earlytime = JSONUtil.getInt(object, "earlytime", "wd_earlytime");
        int earlytime = 0;
        if (wd_earlytime instanceof Integer)
            earlytime = (int) wd_earlytime;
        else if (wd_earlytime instanceof String) {
            earlytime = getStartAndEndByDifferHour((String) wd_earlytime, models.get(0).getWorkTime());
        }
        if (earlytime != 0) {//如果为0 说明是打卡2.0版本之前版本接口
            arrangeWork(models, earlytime);
        }
        return models;
    }


    /**
     * 处理打卡记录
     *
     * @param object
     * @param models
     * @return
     * @throws Exception
     */
    public static ArrayList<WorkModel> handlerWorkLog(JSONObject object, final ArrayList<WorkModel> models) throws Exception {
        JSONArray listdata = object.getJSONArray("listdata");
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (ListUtils.isEmpty(listdata)) {
            return models;
        }

        String[] logs = new String[listdata.size()];
        boolean[] apprecords = new boolean[listdata.size()];
        for (int i = 0; i < listdata.size(); i++) {
            logs[i] = getWorkLogTime(isB2b, listdata.getJSONObject(i));
            apprecords[i] = isAllegedlyLog(isB2b, listdata.getJSONObject(i));
        }
        Arrays.sort(logs);
        for (int i = 0; i < logs.length; i++) {
            String timeLog = logs[i];
            boolean apprecord = apprecords[i];
            for (int j = 0; j < models.size(); j++) {
                WorkModel e = models.get(j);
                if (StringUtil.isEmpty(e.getWorkStart()) || StringUtil.isEmpty(e.getWorkTime()) ||
                        StringUtil.isEmpty(e.getOffend()) || StringUtil.isEmpty(e.getOffTime()))
                    continue;
                if (enoughWork(e, timeLog)) {//属于上班打卡
                    if (apprecord)
                        models.get(j).setWorkAllegedly(timeLog);
                    else
                        models.get(j).setWorkSignin(timeLog);
                    break;
                } else if (enoughOff(e, timeLog)) {//属于下班打卡
                    if (apprecord)
                        models.get(j).setOffAllegedly(timeLog);
                    else
                        models.get(j).setOffSignin(timeLog);
                    break;
                }
            }
        }


        return models;
    }

    /*通过接口获取打卡列表*/
    private static String getWorkLogTime(boolean isB2b, JSONObject object) {
        String timeLog = null;
        if (isB2b) {
            long time = JSONUtil.getLong(object, "cl_date");
            timeLog = time == 0 ? "00:00" : DateFormatUtil.long2Str(time, "HH:mm");
        } else
            timeLog = getMinTime(object.getString("cl_time"));
        return StringUtil.isEmpty(timeLog) ? "00:00" : timeLog;
    }

    private static boolean isAllegedlyLog(boolean isB2b, JSONObject object) {
        if (isB2b)
            return false;
        else if (JSONUtil.getBoolean(object, "apprecord") || "true".equals(JSONUtil.getText(object, "apprecord")))
            return true;
        return false;
    }

    /**
     * 处理自由打卡打卡列表
     *
     * @param object
     * @return
     */
    public static ArrayList<WorkModel> handlerFreeLog(JSONObject object) {
        JSONArray listdata = object.getJSONArray("listdata");
        if (ListUtils.isEmpty(listdata)) {
            return null;
        } else {
            boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;

            String timeLog = "";
            ArrayList<WorkModel> models = new ArrayList<>();
            if (listdata.size() >= 1) {
                if (isB2b) {
                    if (listdata.getJSONObject(0).containsKey("cl_date") && listdata.getJSONObject(0).get("cl_date") != null) {
                        long time = listdata.getJSONObject(0).getLong("cl_date");
                        timeLog = DateFormatUtil.long2Str(time, "HH:mm");
                    }
                } else
                    timeLog = getMinTime(listdata.getJSONObject(0).getString("cl_time"));
                WorkModel model = new WorkModel();
                model.setWorkTime(timeLog);
                models.add(model);
            }
            if (listdata.size() > 1) {
                if (isB2b) {
                    if (listdata.getJSONObject(1).containsKey("cl_date") && listdata.getJSONObject(1).get("cl_date") != null) {
                        long time = listdata.getJSONObject(1).getLong("cl_date");
                        timeLog = DateFormatUtil.long2Str(time, "HH:mm");
                    }
                } else
                    timeLog = getMinTime(listdata.getJSONObject(1).getString("cl_time"));
                WorkModel model = new WorkModel();
                model.setWorkTime(timeLog);
                models.add(model);
            }
            return models;
        }
    }

    /**
     * 处理关于班次的地址信息并保存下来
     *
     * @param object
     * @update 17/3/9
     */
    public static List<WorkLocationModel> handerLocation(JSONObject object, boolean isB2b) {
        final List<WorkLocationModel> beanList = new ArrayList<>();
        JSONArray array = object.getJSONArray("comAddressdata");
        if (!ListUtils.isEmpty(array)) {
            WorkLocationModel bean = null;
            JSONObject o = null;
            String csId = isB2b ? "cS_ID" : "CS_ID";
            String cs_latitude = isB2b ? "cS_LATITUDE" : "CS_LATITUDE";
            String cs_longitude = isB2b ? "cS_LONGITUDE" : "CS_LONGITUDE";
            String cs_shortname = isB2b ? "sHORTNAME" : "CS_SHORTNAME";
            String cs_validrange = isB2b ? "cS_VALIDRANGE" : "CS_VALIDRANGE";
            String cs_workaddr = isB2b ? "cS_WORKADDR" : "CS_WORKADDR";
            for (int i = 0; i < array.size(); i++) {
                o = array.getJSONObject(i);
                bean = new WorkLocationModel();
                int id = o.getInteger(csId);
                double latitude = o.getDouble(cs_latitude);//精度
                double longitude = o.getDouble(cs_longitude);//纬度
                LatLng location = new LatLng(latitude, longitude);
                String validrange = o.getString(cs_validrange);//打卡范围
                String shortname = o.getString(cs_shortname);//位置
                String workaddr = o.getString(cs_workaddr);//地址
                bean.setId(id);
                bean.setLocation(location);
                bean.setValidrange(Integer.valueOf(validrange));
                bean.setShortName(shortname);
                bean.setWorkaddr(workaddr);
                beanList.add(bean);
            }
            //兼容非2.0版本接口
            if (ListUtils.isEmpty(beanList)) {
                bean = new WorkLocationModel();
                bean.setId(1);
                double latitude = o.getDouble("latitude");//精度
                double longitude = o.getDouble("longitude");//纬度
                bean.setLocation(new LatLng(latitude, longitude));
                String validrange = o.getString("distance");//打卡范围
                bean.setValidrange(Integer.valueOf(validrange));
                bean.setShortName("默认位置");
                bean.setWorkaddr("默认地址");
                beanList.add(bean);
            }
            WorkLocationDao.getInstance().clearAndInsert(beanList);
        }
        return beanList;
    }

    /**
     * 判断该时间是否下班打卡时间
     *
     * @param e    对应班次
     * @param time 打卡时间
     * @return
     */
    private static boolean enoughOff(WorkModel e, String time) {
        if (time.compareTo(e.getOffStart()) >= 0 && time.compareTo(e.getOffend()) <= 0 && time.compareTo(e.getWorkTime()) > 0)
            return true;
        return false;
    }


    /**
     * 判断该时间是否上班打卡时间
     *
     * @param e    对应班次
     * @param time 打卡时间
     * @return
     */
    private static boolean enoughWork(WorkModel e, String time) {
        if (!StringUtil.isEmpty(e.getWorkSignin())) return false;
        if (time.compareTo(e.getWorkStart()) >= 0 && time.compareTo(e.getWorkend()) <= 0 && time.compareTo(e.getOffTime()) < 0)
            return true;//小于上班时间
        return false;
    }

    /**
     * 填装上班时间、上班结束时间   下班时间、下班开始时间
     *
     * @param models
     * @param object
     */
    private static void handlerByWork(ArrayList<WorkModel> models, JSONObject object) {
        String wd_onduty = object.getString("wd_onduty");
        String wd_offduty = object.getString("wd_offduty");
        String start = object.getString("wd_onbeg");
        String end = object.getString("wd_offend");
        if (StringUtil.isEmpty(wd_onduty) || StringUtil.isEmpty(wd_offduty))
            return;
        WorkModel model = new WorkModel();
        model.setWorkTime(wd_onduty);
        if (!StringUtil.isEmpty(start))
            model.setWorkStart(start);
        String rangeTime = null;
        try {
            //添加上班结束时间
            rangeTime = getStartAndEndTime(true, wd_onduty);
            if (!StringUtil.isEmpty(rangeTime)) {
                model.setWorkend(rangeTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.setOffTime(wd_offduty);
        if (!StringUtil.isEmpty(end))
            model.setOffend(end);
        try {
            //添加下班结束开始
            rangeTime = getStartAndEndTime(false, wd_offduty);
            if (!StringUtil.isEmpty(rangeTime)) {
                model.setOffStart(rangeTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        models.add(model);
    }


    /**
     * 获取考勤高级设置
     *
     * @param o
     */
    public static void handlerWorkSet(JSONObject o) throws Exception {
        int nonclass = getIntByJson(o, "nonclass");     //矿工时间
        int earlyoff = getIntByJson(o, "earlyoff");     //早退时间
        int overlatetime = getIntByJson(o, "overlatetime"); //严重迟到时间
        int latetime = getIntByJson(o, "latetime");    //迟到时间
        OAConfig.needValidateFace = JSONUtil.getBoolean(o, "needValidateFace");    //迟到时间
        if (nonclass != 0)
            OAConfig.nonclass = nonclass;
        if (earlyoff != 0)
            OAConfig.earlyoff = earlyoff;
        if (overlatetime != 0)
            OAConfig.overlatetime = overlatetime;
        if (latetime != 0)
            OAConfig.latetime = latetime;
        OAConfig.loadWorkSeted = true;//是否下拉过高级考勤数据
        OAConfig.autosign = getIntByJson(o, "autosign") == 1;     //是否自动考勤
    }


    /**
     * 获取上班结束时间和下班开始时间
     * 截取矿工时间
     *
     * @param isWork   是否上班
     * @param workTime 时间 (上班时间||下班时间)
     * @return 服务器没有给的时间
     */
    private static String getStartAndEndTime(boolean isWork, String workTime) throws Exception {
        //兼容打卡2.0版本之前版本
        if (OAConfig.nonclass == 0)
            OAConfig.nonclass = 90;
        long dayTime = f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + workTime + ":00");//获取当前天的workTime
        long deffTime = isWork ? getLongTimeByMinth(OAConfig.nonclass) : -getLongTimeByMinth(OAConfig.nonclass);
        long time = deffTime + dayTime;
        return DateFormatUtil.long2Str(time, "HH:mm");
    }

    private static long getLongTimeByMinth(int minth) {
        return minth * 1000 * 60;
    }

    /**
     * 整理班次，将上班开始时间和下班结束时间加上去
     *
     * @param models
     * @param earlytime
     */
    private static void arrangeWork(ArrayList<WorkModel> models, int earlytime) {
        if (ListUtils.isEmpty(models)) return;
        for (int i = 0; i < models.size(); i++) {
            String nextWorkStartTime = "";
            String lastOffEndTime = "";
            if (!(i == models.size() - 1)) {//如果当前是不最后一个
                nextWorkStartTime = models.get(i + 1).getWorkTime();
            }
            //TODO 判断是否是跨天
            boolean isNextDay = false;
            if (i != 0) {//如果不是第一个
                if (models.get(0).getWorkTime().compareTo(models.get(i).getOffTime()) > 0)
                    isNextDay = true;
                lastOffEndTime = models.get(i - 1).getOffend();
                models.get(i).setWorkStart(getWorkStart(lastOffEndTime));
            } else {//如果是第一个
                if (models.get(i).getWorkTime().compareTo(models.get(i).getOffTime()) > 0)
                    isNextDay = true;
                models.get(i).setWorkStart(getMinTimeByDiffer(models.get(i).getWorkTime(), -earlytime * 60));
            }
            //如果没有下一个班次，就设置结束时间为
            models.get(i).setOffend(getOffEnd(models.get(i).getOffTime(), nextWorkStartTime));
            if (StringUtil.isEmpty(models.get(i).getOffend()))
                models.get(i).setOffend(isNextDay ? models.get(0).getWorkStart() : "24:00");
            models.get(i).setNextDay(isNextDay);
        }
    }


    /**
     * 获取开始时间
     *
     * @param start 上一个班次结束时间
     * @return
     */
    private static String getWorkStart(String start) {
        if (StringUtil.isEmpty(start)) {
            //TODO 数据为空情况
            return null;
        } else {
            String time = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + start + ":00";
            long currentTime = TimeUtils.f_str_2_long(time);
            return getMinTime(TimeUtils.f_long_2_str(currentTime + 1000 * 60));
        }
    }

    /**
     * 计算最后打卡时间
     *
     * @param offTime
     * @return
     */
    private static String getOffEnd(String offTime, String nextWorkStartTime) {
        if (StringUtil.isEmpty(nextWorkStartTime)) {//没有下一个班次
            return nextWorkStartTime;
        } else if (nextWorkStartTime.compareTo(offTime) < 0) {
            return "24:00";
        } else {
            String time = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + offTime + ":00";
            long currentTime = TimeUtils.f_str_2_long(time);
            time = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + nextWorkStartTime + ":00";
            long currentTime2 = TimeUtils.f_str_2_long(time);
            currentTime += (currentTime2 - currentTime) / 2;//两个时间的
            return getMinTime(TimeUtils.f_long_2_str(currentTime));
        }
    }

    /**
     * 通过计算获取时间差，
     *
     * @param workTime 班次时间点（上班 下班）
     * @param differ   距离班次的分钟数（如果是减请在调用时候在differ面前加上 -）
     * @return
     */
    private static String getMinTimeByDiffer(String workTime, int differ) {
        String time = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + workTime + ":00";
        long currentTime = TimeUtils.f_str_2_long(time) + differ * 1000 * 60;
        return getMinTime(TimeUtils.f_long_2_str(currentTime));
    }

    private static int getStartAndEndByDifferHour(String start, String end) throws Exception {
        if (StringUtil.isEmpty(start) || StringUtil.isEmpty(end)) return 0;
        String time1 = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + start + ":00";
        String time2 = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + end + ":00";
        long timeMillis = TimeUtils.f_str_2_long(time2) - TimeUtils.f_str_2_long(time1);
        return (int) (timeMillis / (1000 * 3600));
    }

    /**
     * 通过yyyy-MM-dd HH:mm:ss 提取  HH:mm
     *
     * @param time
     * @return
     */
    private static String getMinTime(String time) {
        if (StringUtil.isEmpty(time)) {
            return "";
        } else {
            return DateFormatUtil.long2Str(f_str_2_long(time), "HH:mm");
        }
    }

    public static int getIntByJson(JSONObject object, String key) {
        if (object.containsKey(key))
            return object.getIntValue(key);
        else return 0;
    }

    /**
     * 通过获取到的数据对外勤进行添加数据
     *
     * @param isB2b
     * @param object
     * @return
     */
    public static MissionModel handlerEntity(boolean isB2b, JSONObject object) {
        MissionModel entity = new MissionModel();
        try {
            long mpd_actdate = JSONUtil.getLong(object, "MPD_ACTDATE", "mp_firsttime");
            long mpd_outdate = JSONUtil.getLong(object, "MPD_OUTDATE", "mp_lasttime");
            if (mpd_actdate != 0)
                entity.setRealTime(TimeUtils.f_long_2_str(mpd_actdate));
            if (mpd_outdate != 0)
                entity.setRealLeave(TimeUtils.f_long_2_str(mpd_outdate));
        } catch (Exception e) {
        }
        String mpd_recorddate;//录入时间
        String mp_visittime;//预计到达时间
        String mpd_kind;//半天
        if (isB2b) {
            mpd_recorddate = TimeUtils.f_long_2_str(object.getLongValue("mpd_recorddate"));
            mp_visittime = TimeUtils.f_long_2_str(object.getLongValue("mpd_arrivedate"));
            mpd_kind = object.getString("mpd_kind");
        } else {
            mpd_recorddate = object.getString("MPD_RECORDDATE");
            mp_visittime = object.getString("MPD_ARRIVEDATE");
            mpd_kind = object.getString("MPD_KIND");
        }
        double longitude = object.getDoubleValue(isB2b ? "md_longitude" : "MD_LONGITUDE");
        double latitude = object.getDoubleValue(isB2b ? "md_latitude" : "MD_LATITUDE");
        if (longitude > 0 && latitude > 0) {
            entity.setLatLng(new LatLng(latitude, longitude));
        }
        entity.setVisitTime(mp_visittime);
        entity.setRecorddate(mpd_recorddate);
        entity.setType(getType(mpd_kind));
        entity.setDistance(object.getDoubleValue(isB2b ? "mpd_distance" : "MPD_DISTANCE"));
        entity.setId(object.getInteger(isB2b ? "mpd_id" : "MPD_ID"));
        entity.setCompanyName(object.getString(isB2b ? "mpd_company" : "MPD_COMPANY"));
        entity.setCompanyAddr(object.getString(isB2b ? "mpd_address" : "MPD_ADDRESS"));
        entity.setRemark(object.getString(isB2b ? "mpd_remark" : "MPD_REMARK"));
        entity.setLocation(object.getString(isB2b ? "mpd_location" : "MPD_LOCATION"));
        entity.setStatus(1);
        return entity;
    }

    private static int getType(String str) {
        if (StringUtil.isEmpty(str)) return 1;
        return "全天".equals(str) ? 2 : 1;
    }
}
