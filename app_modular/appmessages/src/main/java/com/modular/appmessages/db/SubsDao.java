package com.modular.appmessages.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConfig;
import com.core.app.MyApplication;
import com.core.db.DatabaseManager;
import com.core.utils.CommonUtil;
import com.modular.appmessages.model.SubMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitliker on 2016/11/21.
 */

public class SubsDao {
    private static SubsDao instance;

    public static SubsDao getInstance() {
        if (instance == null) {
            synchronized (SubsDao.class) {
                instance = new SubsDao();
            }
        }
        return instance;
    }

    private SubsDao() {


    }


    public boolean createOrUpdata(List<SubMessage> messages) {
        if (ListUtils.isEmpty(messages)) return false;
        String date = messages.get(0).getDate();
        deleteByDate(date);
        return insertAll(messages);
    }


    //1.添加所有
    private boolean insertAll(List<SubMessage> messages) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            ContentValues values = null;
            for (SubMessage e : messages) {
                values = new ContentValues();
                values.put("master", master);
                values.put("emcode", emcode);
                values.put("id", e.getId());
                values.put("numId", e.getNumId());
                values.put("instanceId", e.getInstanceId());
                values.put("title", e.getTitle());
                values.put("subTitle", e.getSubTitle());
                values.put("status", e.getStatus());
                values.put("isRead", e.isRead() ? -1 : 0);//未阅读是0
                values.put("date", e.getDate());
                values.put("createTime", e.getCreateTime());
                db.insert("submessage", null, values);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return false;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    //查找按日期  yyyy-MM-dd
    public List<SubMessage> queryByDate(String date) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            String[] columns = {"id", "numId", "instanceId", "title", "subTitle", "status", "date", "createTime", "isRead"};
            String selection = "master=? and emcode=? and  date=?";
            String[] selectionArg = {master, emcode, date};
            cursor = db.query("submessage", columns, selection, selectionArg, null, null, null);
            List<SubMessage> messages = new ArrayList<>();
            SubMessage message = null;
            while (cursor.moveToNext()) {
                message = new SubMessage();
                message.setId(cursor.getInt(0));
                message.setNumId(cursor.getInt(1));
                message.setInstanceId(cursor.getInt(2));
                message.setTitle(cursor.getString(3));
                message.setSubTitle(cursor.getString(4));
                message.setStatus(cursor.getInt(5));
                message.setDate(cursor.getString(6));
                message.setCreateTime(cursor.getLong(7));
                message.setRead(cursor.getInt(8) != 0);//0是未读  -1是已读
                messages.add(message);
            }
            return messages;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return null;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    public boolean deleteTime(SubMessage message) {
        if (message == null) return false;
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            String where = "master=? and emcode=? and status=? and  date=?";
            String[] whereArg = {master, emcode, String.valueOf(message.getStatus()), message.getDate()};
            int i = db.delete("submessage", where, whereArg);
            if (i > 0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return false;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return false;
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }

    }

    //删除
    public boolean deleteByMessage(SubMessage message) {
        if (message == null) return false;
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            String where = "master=? and emcode=? and id=? and  date=?";
            String[] whereArg = {master, emcode, String.valueOf(message.getId()), message.getDate()};
            int i = db.delete("submessage", where, whereArg);
            if (i > 0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return false;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return false;
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    //删除
    private boolean deleteByDate(String date) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            String where = "master=? and emcode=?  and  date=?";
            String[] whereArg = {master, emcode, date,};
            int i = db.delete("submessage", where, whereArg);
            if (i > 0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return false;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return false;
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    public boolean upIsReadAndStatus(SubMessage message) {
        if (message == null) return false;
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            String where = "master=? and emcode=? and id=? and  date=?";
            String[] whereArg = {master, emcode, String.valueOf(message.getId()), message.getDate()};
            ContentValues values = new ContentValues();
            values.put("isRead", message.isRead() ? -1 : 0);//未阅读是0
            values.put("status", message.getStatus());//未阅读是0
            int i = db.update("submessage", values, where, whereArg);
            if (i > 0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return false;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return false;
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    public boolean upIsReadByMessage(List<SubMessage> messages) {
        if (ListUtils.isEmpty(messages)) return false;
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            String emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
            String where = "master=? and emcode=? and id=? and  date=?";
            ContentValues values = null;
            for (SubMessage e : messages) {
                String[] whereArg = {master, emcode, String.valueOf(e.getId()), e.getDate()};
                values = new ContentValues();
                values.put("isRead", e.isRead() ? -1 : 0);//未阅读是0
                db.update("submessage", values, where, whereArg);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            show("SQLException=" + e.getMessage());
            return false;
        } catch (Exception e) {
            show("Exception=" + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
        }
    }


    private void show(String str) {
        if (!AppConfig.DEBUG || StringUtil.isEmpty(str)) return;
        Log.i("gongpengming", str);
    }
}
