package com.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.core.db.DatabaseManager;
import com.core.xmpp.model.SignAutoLogEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/5/9.
 * function:
 */
public class SignAutoLogDao {
    /**
     * 添加一条新记录
     *
     * @param signAutoLogEntity
     */
    public static void addNewData(SignAutoLogEntity signAutoLogEntity) {
        //TODO 获取db的方式统一使用这个，以后的方法里面使用到db的也用这个方式获取，不然有可能出现闪退
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        try {
            stepDb.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("aa_type", signAutoLogEntity.getAa_type());
            values.put("aa_location", signAutoLogEntity.getAa_location());
            values.put("aa_remark", signAutoLogEntity.getAa_remark());
            values.put("aa_date", signAutoLogEntity.getAa_date());
            values.put("aa_telephone", signAutoLogEntity.getAa_telephone());
            values.put("sendstatus", "0");
            stepDb.insert("SignAutoLog", null, values);
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
     * 查询所有的记录
     *
     * @return
     */
    public List<SignAutoLogEntity> getAllDatas() {
        List<SignAutoLogEntity> dataList = new ArrayList<>();
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        Cursor  cursor = null;
        try {
            cursor = stepDb.rawQuery("select * from SignAutoLog", null);
            stepDb.beginTransaction();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String aa_type = cursor.getString(cursor.getColumnIndex("aa_type"));
                String aa_location = cursor.getString(cursor.getColumnIndex("aa_location"));
                String aa_remark = cursor.getString(cursor.getColumnIndex("aa_remark"));
                String aa_date = cursor.getString(cursor.getColumnIndex("aa_date"));
                String aa_telephone = cursor.getString(cursor.getColumnIndex("aa_telephone"));
                int sendstatus = cursor.getInt(cursor.getColumnIndex("sendstatus"));
                SignAutoLogEntity entity = new SignAutoLogEntity(id, aa_type, aa_location, aa_remark, aa_date, aa_telephone, sendstatus);
                dataList.add(entity);
            }
            stepDb.setTransactionSuccessful();
            stepDb.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
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
     * 更新数据:当某条日志提交服务器后进行本地状态的更新 将sendstatus set 为 1；
     *
     * @param signAutoLogEntity
     */
    public void updateCurData(List<SignAutoLogEntity> signAutoLogEntity) {
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        try {
            stepDb.beginTransaction();
            for (int i = 0; i < signAutoLogEntity.size(); i++) {
                values.put("sendstatus",1);
                stepDb.update("SignAutoLog", values, "_id=?", new String[]{signAutoLogEntity.get(i).getId() + ""});
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
    }

    /**
     * 删除 sendstatus = 1 的数据
     */
    public void cleanLocalData(){
//        List<SignAutoLogEntity> signAutoLogEntity = getAllDatas();
//        if (signAutoLogEntity.size() < 50) return;
        SQLiteDatabase stepDb = DatabaseManager.getInstance().openDatabase();
        try {
            stepDb.beginTransaction();
            stepDb.delete("SignAutoLog", "sendstatus=?", new String[]{"1"});
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
}
