package com.uas.appworks.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.db.DatabaseManager;
import com.core.model.MissionModel;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 外勤计划数据库管理
 * 对外接口
 * 1.添加或修改单个数据、添加或修改多个数据（其中所有录入不包含status）
 * 2.修改状态、修改签到时间
 * 3.查询当天数据
 * <p>
 * <p>
 * Created by Bitliker on 2016/11/28.
 */

public class MissionDao {

    private String tibleName = "mission";
    private static MissionDao instance;

    public static MissionDao getInstance() {
        if (instance == null) {
            synchronized (MissionDao.class) {
                instance = new MissionDao();
            }
        }
        return instance;
    }

    public MissionDao() {
    }


    /**
     * 修改状态
     *
     * @param id     id
     * @param status 状态值
     * @return
     */
    public boolean upStatus(int id, int status) {
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put("status", status);
            String where = "master=? and emcode=? and id=? ";
            String[] whereArgs = {master, emcode, String.valueOf(id)};
            i = db.update(tibleName, values, where, whereArgs);
        } catch (SQLException e) {
            LogUtil.i("SQLException=" + e.getMessage());
        } catch (Exception e) {
            LogUtil.i("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    //获取当天全部数据
    public List<MissionModel> queryByEnCode() {
        SQLiteDatabase db = null;
        List<MissionModel> missions = null;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return missions;
            db = DatabaseManager.getInstance().openDatabase();

            missions = getMissionModels(db, master, emcode);
        } catch (SQLException e) {
            LogUtil.i("SQLException=" + e.getMessage());
        } catch (Exception e) {
            LogUtil.i("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return missions;
        }
    }

    @NonNull
    private List<MissionModel> getMissionModels(SQLiteDatabase db, String master, String emcode) {
        String date = DateFormatUtil.long2Str(DateFormatUtil.YMD);
        String[] columns = {"id", "company",
                "companyAddr", "latitude",
                "longitude", "visittime",
                "realvisitTime", "realLeaveTime",
                "status", "distance",
                "location", "recorddate",
                "remark", "type"};
        String selection = "master =? and emcode=? and date=?";
        String[] selecttionArg = {master, emcode, date};
        Cursor cursor = db.query(tibleName, columns, selection, selecttionArg, null, null, null);
        MissionModel mission = null;
        List<MissionModel> missions = new ArrayList<>();
        while (cursor.moveToNext()) {
            mission = new MissionModel();
            mission.setId(cursor.getInt(0));
            mission.setCompanyName(cursor.getString(1));
            mission.setCompanyAddr(cursor.getString(2));
            double latitude = cursor.getDouble(3);
            double longitude = cursor.getDouble(4);
            if (latitude > 0 && longitude > 0)
                mission.setLatLng(new LatLng(latitude, longitude));
            mission.setVisitTime(cursor.getString(5));
            mission.setRealTime(cursor.getString(6));
            mission.setRealLeave(cursor.getString(7));
            mission.setStatus(cursor.getInt(8));
            mission.setDistance(cursor.getDouble(9));
            mission.setLocation(cursor.getString(10));
            mission.setRecorddate(cursor.getString(11));
            mission.setRemark(cursor.getString(12));
            mission.setType(cursor.getInt(13));
            missions.add(mission);
        }
        cursor.close();
        return missions;
    }

    public boolean updataOrCreate(MissionModel mission) {
        if (mission == null || StringUtil.isEmpty(mission.getVisitTime())) return false;
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = null;
            LogUtil.i("MissionPlanEntity");
            //对象为空或是拜访时间没有情况下不插入数据库
            values = new ContentValues();
            values.put("id", mission.getId());
            values.put("master", master);
            values.put("emcode", emcode);
            values.put("company", mission.getCompanyName());
            values.put("companyAddr", mission.getCompanyAddr());
            if (mission.getLatLng() != null) {
                values.put("latitude", mission.getLatLng().latitude);
                values.put("longitude", mission.getLatLng().longitude);
            }
            values.put("visittime", mission.getVisitTime());
            values.put("realvisitTime", mission.getRealTime());
            values.put("realLeaveTime", mission.getRealLeave());
            values.put("distance", mission.getDistance());
            values.put("location", mission.getLocation());
            values.put("remark", mission.getRemark());
            values.put("recorddate", mission.getRecorddate());
            values.put("date", getDateByString(mission.getVisitTime()));//拜访日期
            i = db.insert(tibleName, "status", values);
            if (i == -1) {
                String where = "master=? and emcode=? and id=? ";
                String[] whereArgs = {master, emcode, String.valueOf(mission.getId())};
                i = db.update(tibleName, values, where, whereArgs);
                LogUtil.i("update i=" + i);
            }
            LogUtil.i("i====" + i);
        } catch (SQLException e) {
            LogUtil.i("SQLException=" + e.getMessage());
        } catch (Exception e) {
            LogUtil.i("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    public boolean updataOrCreate(List<MissionModel> entities) {
        if (ListUtils.isEmpty(entities)) return false;
        LogUtil.i("updataOrCreate");
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();//开启事务

            ContentValues values = null;
            for (MissionModel mission : entities) {
                LogUtil.i("MissionPlanEntity");
                //对象为空或是拜访时间没有情况下不插入数据库
                if (mission == null || StringUtil.isEmpty(mission.getVisitTime())) {
                    continue;
                }
                values = new ContentValues();
                values.put("id", mission.getId());
                values.put("master", master);
                values.put("emcode", emcode);
                values.put("company", mission.getCompanyName());
                values.put("companyAddr", mission.getCompanyAddr());
                if (mission.getLatLng() != null) {
                    values.put("latitude", mission.getLatLng().latitude);
                    values.put("longitude", mission.getLatLng().longitude);
                }
                values.put("visittime", mission.getVisitTime());
                values.put("realvisitTime", mission.getRealTime());
                values.put("realLeaveTime", mission.getRealLeave());
                values.put("distance", mission.getDistance());
                values.put("location", mission.getLocation());
                values.put("remark", mission.getRemark());
                values.put("type", mission.getType());
                values.put("recorddate", mission.getRecorddate());
                values.put("date", getDateByString(mission.getVisitTime()));//拜访日期
                i = db.insert(tibleName, "status", values);
                if (i == -1) {
                    String where = "master=? and emcode=? and id=? ";
                    String[] whereArgs = {master, emcode, String.valueOf(mission.getId())};
                    i = db.update(tibleName, values, where, whereArgs);
                    LogUtil.i("update i=" + i);
                }
                LogUtil.i("i====" + i);
            }

            //删除多余数据
            List<MissionModel> dbMissions = getMissionModels(db, master, emcode);
            for (MissionModel dbMission : dbMissions) {
                boolean exist = false;
                for (MissionModel mission : entities) {
                    if (dbMission.getId() == mission.getId()) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    deleteMission(dbMission.getId() + "", db, master, emcode);
                }
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtil.i("SQLException=" + e.getMessage());
        } catch (Exception e) {
            LogUtil.i("Exception=" + e.getMessage());
        } finally {
            db.endTransaction();
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    //更新
    public boolean updata(MissionModel mission) {
        LogUtil.i("update" + JSON.toJSONString(mission));
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            if (mission.getLatLng() != null) {
                values.put("latitude", mission.getLatLng().latitude);
                values.put("longitude", mission.getLatLng().longitude);
            }
            values.put("visittime", mission.getVisitTime());
            values.put("realvisitTime", mission.getRealTime());
            values.put("realLeaveTime", mission.getRealLeave());
            values.put("status", mission.getStatus());
            String where = "master=? and emcode=? and id=? ";
            String[] whereArgs = {master, emcode, String.valueOf(mission.getId())};
            i = db.update(tibleName, values, where, whereArgs);
        } catch (SQLException e) {
            LogUtil.i("SQLException=" + e.getMessage());
        } catch (Exception e) {
            LogUtil.i("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    public boolean deleteByid(int mpd_id) {
        if (StringUtil.isEmpty(mpd_id + "")) return false;
        String id = mpd_id + "";
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            i = deleteMission(id, db, master, emcode);
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    private long deleteMission(String id, SQLiteDatabase db, String master, String emcode) {
        String where = "master=? and emcode=? and id=? ";
        String[] whereArgs = {master, emcode, id};
        long i = db.delete(tibleName, where, whereArgs);
        return i;
    }

    private String getDateByString(String date) {
        if (StringUtil.isEmpty(date)) return "";
        return TimeUtils.s_long_2_str(TimeUtils.f_str_2_long(date));
    }

    public boolean clearAll(long date) {
        LogUtil.i("clearAll");
        SQLiteDatabase db = null;
        boolean clearOk = false;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            String where = "master=? and emcode=? and date<=?";
            String[] whereArg = {master, emcode, TimeUtils.s_long_2_str(date)};
            int i = db.delete(tibleName, where, whereArg);
            clearOk = i > 0;
        } catch (SQLException e) {
            if (e != null)
                LogUtil.i("clearAll SQLException" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                LogUtil.i("clearAll Exception" + e.getMessage());
        } finally {
            DatabaseManager.getInstance().closeDatabase();
            return clearOk;
        }
    }


    private String getMaster() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String master = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu") : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
        return master;

    }

    private String getEmcode() {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        String emcode = isB2b ? CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu") : CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        return emcode;
    }

    public boolean hasMoreMision() {
        SQLiteDatabase db = null;
        boolean clearOk = false;
        try {
            String master = getMaster();
            String emcode = getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            String[] columns = {"realvisitTime,realLeaveTime"};
            String selecttion = "master =? and emcode=? and date=? and ( realvisitTime is null or realLeaveTime is null)";
            String[] selectArg = {master, emcode, TimeUtils.s_long_2_str(System.currentTimeMillis())};
            Cursor c = db.query(tibleName, columns, selecttion, selectArg, null, null, null);
            clearOk = c.moveToNext();
        } catch (SQLException e) {
            if (e != null)
                LogUtil.i("clearAll SQLException" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                LogUtil.i("clearAll Exception" + e.getMessage());
        } finally {
            DatabaseManager.getInstance().closeDatabase();
            return clearOk;
        }
    }


}