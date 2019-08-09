package com.xzjmyk.pm.activity.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.common.data.StringUtil;
import com.core.app.AppConfig;
import com.core.db.DatabaseManager;
import com.xzjmyk.pm.activity.ui.erp.entity.UUStepDataEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/2/10.
 * function:
 */
public class UUStepDataDao {
    private static UUStepDataDao instance;

    private String tableName = "uustep_data";

    public  static UUStepDataDao getInstance(){
        if (instance == null){
            instance = new UUStepDataDao();
        }
        return instance;
    }

    private UUStepDataDao(){

    }

    //获取全部步数数据
    public List<UUStepDataEntity> queryALL(){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<UUStepDataEntity> beans = null;
        try {

            db = DatabaseManager.getInstance().openDatabase();
            String[] columns = {"date", "stepnumbers"};
            String selection = null;
            String[] selecttionArg = null;
            cursor = db.query(tableName, columns, selection, selecttionArg, null, null, null);
            UUStepDataEntity bean = null;
            beans = new ArrayList<>();
            while (cursor.moveToNext()) {
                bean = new UUStepDataEntity();
                bean.setDate(cursor.getString(0));
                bean.setStepnumbers(cursor.getInt(1));
                beans.add(bean);
            }
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            if (cursor != null)
                cursor.close();
            return beans;
        }

    }

    //添加一条数据
    public boolean insert(UUStepDataEntity bean){
        SQLiteDatabase db = null;
        long i = 0;
        try {

            db =DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put("date",bean.getDate());
            values.put("stepnumbers",bean.getStepnumbers());
            Log.i("insert", "插入一条是数据");
            i =db.insert(tableName,null,values);
            Log.i("insertii","插入一条是数据i=="+i);
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    //更新一条数据
    public void update(UUStepDataEntity bean){
        SQLiteDatabase db = null;
        try {
            db =DatabaseManager.getInstance().openDatabase();
            Cursor cursor = db.query(tableName,null,null,null,null,null,null,null);
            if (cursor.moveToNext()){
                do{
                    String date = cursor.getString(cursor.getColumnIndex("date"));
                    if (bean.getDate().equals(date)){
                        ContentValues values = new ContentValues();
                        values.put("stepnumbers", bean.getStepnumbers());
                        String bean_date = bean.getDate();
                        db.update(tableName,values,"date = ?",new String[]{bean_date});
                    }
                }while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
        }
    }


    //查询一条数据是否存在
    public int query(UUStepDataEntity bean){
        SQLiteDatabase db = null;
        int queryStepnums = 0;
        try {
            db =DatabaseManager.getInstance().openDatabase();
            Cursor cursor = db.query(tableName,null,null,null,null,null,null,null);
            if (cursor.moveToNext()){
                do{
                    String date = cursor.getString(cursor.getColumnIndex("date"));
                    if (bean.getDate().equals(date)){
                        queryStepnums = cursor.getInt(cursor.getColumnIndex("date"));
                    }

                }while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
        }
        if (queryStepnums >= 0){
            return queryStepnums;
        }else {
            return -1;
        }
    }
    private void show(String str) {
        if (StringUtil.isEmpty(str) || !AppConfig.DEBUG) return;
    }
}
