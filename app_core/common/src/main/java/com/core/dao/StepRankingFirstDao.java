package com.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.core.db.DatabaseManager;
import com.core.model.StepRankingFirstBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/11/3.
 * function:// 用于存贮每天UU运动排名第一的数据表
 *
 *
 + "date TEXT,"
 + "my_userid TEXT,"
 + "my_rank TEXT, "
 + "my_steps TEXT, "
 + "f_userid TEXT, "
 + "f_name TEXT,"
 *
 *  Ranking First Info  --- RFI
 */

public class StepRankingFirstDao {


    /**
     * 添加一条新记录
     *
     * @param bean
     */
    public static void addNewRFIData(StepRankingFirstBean bean){
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();

        try{
            stepDb.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("date", bean.getDate());
            values.put("my_userid", bean.getMy_userid());
            values.put("my_rank", bean.getMy_rank());
            values.put("my_steps", bean.getMy_steps());
            values.put("f_userid", bean.getF_userid());
            values.put("f_name", bean.getF_name());

            stepDb.insert("StepRankingFirst", null, values);
            stepDb.setTransactionSuccessful();
            stepDb.endTransaction();
        }catch (SQLException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (stepDb != null)
                DatabaseManager.getInstance().closeDatabase();
        }
    }

    /**
     * 查询所有的记录
     *
     * @return
     */
    public List<StepRankingFirstBean> getAllRFIDatas() {
        List<StepRankingFirstBean> dataList = new ArrayList<>();
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = null;
        try {
            cursor = stepDb.rawQuery("select * from StepRankingFirst", null);
            stepDb.beginTransaction();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String my_userid = cursor.getString(cursor.getColumnIndex("my_userid"));
                String my_rank = cursor.getString(cursor.getColumnIndex("my_rank"));
                String my_steps = cursor.getString(cursor.getColumnIndex("my_steps"));
                String f_userid = cursor.getString(cursor.getColumnIndex("f_userid"));
                String f_name = cursor.getString(cursor.getColumnIndex("f_name"));
                StepRankingFirstBean entity = new StepRankingFirstBean(id,date, my_userid, my_rank, my_steps, f_userid, f_name);
                dataList.add(entity);
            }
            stepDb.setTransactionSuccessful();
            stepDb.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.i("fanglh",e.toString());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭数据库
            if (cursor != null){
                cursor.close();
            }
            if (stepDb != null){
                DatabaseManager.getInstance().closeDatabase();
            }
            return dataList;
        }
    }

    /**
     * 更新当前的第一名数据
     * @param bean
     */
    public void updateCurDateRFI(StepRankingFirstBean bean){
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        try {
            stepDb.beginTransaction();
            values.put("_id",bean.get_id());
            values.put("my_userid",bean.getMy_userid());
            values.put("my_rank",bean.getMy_rank());
            values.put("my_steps",bean.getMy_steps());
            values.put("f_userid",bean.getF_userid());
            values.put("f_name",bean.getF_name());
            stepDb.update("StepRankingFirst", values, "date=?", new String[]{bean.getDate()});
            stepDb.setTransactionSuccessful();
            stepDb.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (stepDb != null)
                DatabaseManager.getInstance().closeDatabase();
        }
    }

    /**
     *  删除该条bean 数据
     * @param date
     */
    public void deleteOneRFIData(String date){
        //todo 这里要不要做一下不可删除当前 日期的数据呢
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        try {
            stepDb.beginTransaction();
            stepDb.delete("StepRankingFirst", "date=?", new String[]{date});
            stepDb.setTransactionSuccessful();
            stepDb.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (stepDb != null)
                DatabaseManager.getInstance().closeDatabase();
        }
    }


    /**
     * 根据日期查询记录
     *
     * @param curDate
     * @return
     */
    public StepRankingFirstBean getCurDataByDate(String curDate) {

        StepRankingFirstBean bean = null;
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        try {
            stepDb.beginTransaction();

            Cursor cursor = stepDb.query("StepRankingFirst", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String date = curDate;
                if (curDate.equals(date)) {
                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String my_userid = cursor.getString(cursor.getColumnIndexOrThrow("my_userid"));
                    String my_rank = cursor.getString(cursor.getColumnIndexOrThrow("my_rank"));
                    String my_steps = cursor.getString(cursor.getColumnIndexOrThrow("my_steps"));
                    String f_userid = cursor.getString(cursor.getColumnIndexOrThrow("f_userid"));
                    String f_name = cursor.getString(cursor.getColumnIndexOrThrow("f_name"));
                    bean = new StepRankingFirstBean(id,date, my_userid,my_rank,my_steps,f_userid,f_name);
                    //跳出循环
                    break;
                }
            }
            stepDb.setTransactionSuccessful();
            stepDb.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (stepDb != null)
                DatabaseManager.getInstance().closeDatabase();
        }
        return bean;
    }
 }
