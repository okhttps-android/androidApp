package com.xzjmyk.pm.activity.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.common.data.StringUtil;
import com.core.app.AppConfig;
import com.core.db.DBOpenHelper;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.ui.erp.entity.InAndExitTimeEntity;

import java.util.ArrayList;
import java.util.List;


 

public class InAndExitTimeDao {
    private static InAndExitTimeDao instance;
    private DBOpenHelper helper = null;
    private String tableName = "InAndExitTime";

    public static InAndExitTimeDao getInstance(){
        if (instance == null){
            instance = new InAndExitTimeDao();
        }
        return instance;
    }

    private InAndExitTimeDao(){
        helper =DBOpenHelper.getInstance(MyApplication.getInstance());
    }
    //获取全部时间数据
    public List<InAndExitTimeEntity> queryAll(){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<InAndExitTimeEntity> beans = null;

        try {
            db = helper.getReadableDatabase();
            String[] columns = {"loginin_time","loginexit_time","killed_time"};
            String selection = null;
            String[] selecttionArg = null;
            cursor = db.query(tableName, columns, selection, selecttionArg, null, null, null);
            InAndExitTimeEntity bean = null;
            beans = new ArrayList<>();

            while (cursor.moveToNext()){
                bean = new InAndExitTimeEntity();
                bean.setLoginin_time(cursor.getString(0));
                bean.setLoginexit_time(cursor.getString(1));
                bean.setKilled_time(cursor.getString(2));
                beans.add(bean);
            }
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
        } finally {
            if (db != null)
                db.close();
            if (cursor != null)
                cursor.close();
            return beans;
        }
    }
    //插入一个时间数据
    public boolean insert(InAndExitTimeEntity bean) {
        SQLiteDatabase db = null;
        long i = 0;
        try {
            db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("loginin_time", bean.getLoginin_time());
            values.put("loginexit_time", bean.getLoginexit_time());
            values.put("killed_time", bean.getKilled_time());
            i = db.insert(tableName, null, values);
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                db.close();
            return i > 0;
        }
    }

    private void show(String str) {
        if (StringUtil.isEmpty(str) || !AppConfig.DEBUG) return;
        Log.i("Loginin_exit_time", str);
    }
}
