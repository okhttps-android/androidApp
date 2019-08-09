package com.core.dao;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.common.data.StringUtil;
import com.core.db.DatabaseManager;
import com.core.xmpp.model.TrackPointEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/9/13.
 * function:   //运行轨迹存贮本地、删除、查询Dao类
 +"latitude double(13,10)," //纬度
 +"longitude double(13,10)," //经度
 +"timestamp varchar(50)," //实时定位时的时间戳
 +"type varchar(10)," //类型 run、walk
 +"startTime varchar(50),"  //开始定位后全部默认为开始时间：yyyy-MM-dd HH:mm
 +"endTime varchar(50)"  //未点击结束前都为空 ，点击结束后取当前时间 点击格式：yyyy-MM-dd HH:mm
 */

public class TrackPointDao {
    private final String tableName = "trackPoint";
    private static TrackPointDao instance;

    public static TrackPointDao getInstance() {
        if (instance == null) {
            synchronized (TrackPointDao.class) {
                instance = new TrackPointDao();
            }
        }
        return instance;
    }

    public TrackPointDao() {
    }
    /**
     * 实时新增定位点
     * @param trackPointEntity
     */
    public  void addOnePoint(TrackPointEntity trackPointEntity){
        SQLiteDatabase Db = DatabaseManager.getInstance().openDatabase();
        try {
            Db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("latitude", trackPointEntity.getLatitude());
            values.put("longitude", trackPointEntity.getLongitude());
            values.put("timestamp", trackPointEntity.getTimestamp());
            values.put("type", trackPointEntity.getType());
            values.put("startTime", trackPointEntity.getStartTime());
            values.put("endTime", trackPointEntity.getEndTime());
            Db.insert(tableName, null, values);
            Db.setTransactionSuccessful();
            Db.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (Db != null)
                DatabaseManager.getInstance().closeDatabase();
        }
    }

    /**
     * 根据类型和开始时间查询所有定位点数
     * @param type
     * @param startTime
     * @return
     */
    public  List<TrackPointEntity> searchOneRecord(String type,String startTime){
        List<TrackPointEntity> tList = new ArrayList<>();
        SQLiteDatabase Db = DatabaseManager.getInstance().openDatabase();
        try {
            if (StringUtil.isEmpty(type) || StringUtil.isEmpty(startTime)) return tList;
            Db = DatabaseManager.getInstance().openDatabase();
            String[] colums = {"id","latitude","longitude","timestamp","type","startTime","endTime"};
            Cursor cursor = null;
            String selection;

            selection = "type=? and startTime=?";
            String[] selectArgs = {type,startTime};
            cursor = Db.query(tableName,colums,selection,selectArgs,null,null,null);
            tList = getTrackPoint(cursor);
            cursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (Db != null)
                DatabaseManager.getInstance().closeDatabase();
            return tList;
        }
    }

    /**
     *把查询到的点存贮进列表中
     * @param cursor
     * @return
     */
    private List<TrackPointEntity> getTrackPoint(Cursor cursor){
        List<TrackPointEntity> trackPointEntityList = new ArrayList<>();
        TrackPointEntity trackPointEntity;
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
            String endTime = cursor.getString(cursor.getColumnIndex("startTime"));
            trackPointEntity = new TrackPointEntity(id,latitude,longitude,timestamp,type,startTime,endTime);
            trackPointEntityList.add(trackPointEntity);
        }
        return trackPointEntityList;
    }


    /**
     * 删除某一条运行轨迹
     * @param type
     * @param startTime
     * @return
     */
    public boolean deleteOneRecord(String type,String startTime){
        if (StringUtil.isEmpty(type) || StringUtil.isEmpty(startTime)) return false;
        SQLiteDatabase db = null;
        long i = 0;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            String where = "type=? and startTime=?";
            String[] whereArgs = {type,startTime};
            i = db.delete(tableName,where,whereArgs);
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }
}
