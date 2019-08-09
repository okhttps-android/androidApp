package com.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.db.DatabaseManager;
import com.core.model.WorkLocationModel;
import com.core.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Bitliker on 2017/2/9.
 */

public class WorkLocationDao {


    private final String TIBLE_NAME = "work_location";
    private static WorkLocationDao instance;

    public static WorkLocationDao getInstance() {
        if (instance == null) {
            synchronized (WorkLocationDao.class) {
                instance = new WorkLocationDao();
            }
        }
        return instance;
    }

    public WorkLocationDao() {
    }


    //获取当天全部数据
    public List<WorkLocationModel> queryByEnCode() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<WorkLocationModel> beans = null;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();

            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return beans;
            db = DatabaseManager.getInstance().openDatabase();
            String[] columns = {"id", "latitude",
                    "longitude", "shortName",
                    "workaddr", "validrange"};
            String selection = "master =? and emcode=? ";
            String[] selecttionArg = {master, emcode};
            cursor = db.query(TIBLE_NAME, columns, selection, selecttionArg, null, null, null);
            WorkLocationModel bean = null;
            beans = new ArrayList<>();
            while (cursor.moveToNext()) {
                bean = new WorkLocationModel();
                bean.setId(cursor.getInt(0));
                double latitude = cursor.getDouble(1);
                double longitude = cursor.getDouble(2);
                if (latitude > 0 && longitude > 0){
                    bean.setLocation(new LatLng(latitude, longitude));
                }else{
                    bean.setLocation(new LatLng(0, 0));
                }
                bean.setShortName(cursor.getString(3));
                bean.setWorkaddr(cursor.getString(4));
                bean.setValidrange(cursor.getInt(5));
                beans.add(bean);
            }
            if (cursor != null)
                cursor.close();
        } catch (android.database.SQLException e) {
            LogUtil.i("SQLException=" + e.getMessage());
        } catch (Exception e) {
            LogUtil.i("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return beans;
        }
    }

    public boolean clearAndInsert(List<WorkLocationModel> entities) {
        SQLiteDatabase db = null;
        long i = 0;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();//开启事务
            //清空表数据
            db.execSQL("DELETE FROM " + TIBLE_NAME);
            //添加数据
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            ContentValues values = null;
            for (WorkLocationModel e : entities) {
                //对象为空或是拜访时间没有情况下不插入数据库
                if (e == null) {
                    continue;
                }
                values = new ContentValues();
                values.put("id", e.getId());
                values.put("master", master);
                values.put("emcode", emcode);
                values.put("validrange", e.getValidrange());
                values.put("innerdistance", 1);
                values.put("latitude", e.getLatitude());
                values.put("longitude", e.getLongitude());
                values.put("shortName", e.getShortName());
                values.put("workaddr", e.getWorkaddr());
                i = db.insert(TIBLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            if (e != null)
                LogUtil.i("clearAll SQLException" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                LogUtil.i("clearAll Exception" + e.getMessage());
        } finally {
            db.endTransaction();
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }
}
