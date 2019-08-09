package com.xzjmyk.pm.activity.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.common.data.StringUtil;
import com.core.app.AppConfig;

import java.util.List;

/**
 * Created by Bitliker on 2016/12/12.
 */

public class CommonDao<T> {

    public CommonDao() {

    }

    /**
     * 使用事务插入数据
     *
     * @param nullColumn 为null行
     * @param values     插入数据
     * @return
     */
    private long insert(SQLiteDatabase db, String tableName, String nullColumn, List<ContentValues> values) {
        long i = 0;
        try {
            db.beginTransaction();
            for (ContentValues value : values)
                i = db.insert(tableName, nullColumn, value);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            if (e != null)
                log("insert SQLException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                log("insert Exception=" + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
            return i;
        }
    }

    /**
     * 不使用事务插入数据
     *
     * @param nullColumn 为null行
     * @param values     插入数据
     * @return
     */
    private long insert(SQLiteDatabase db, String tableName, String nullColumn, ContentValues values) {
        long i = 0;
        try {
            i = db.insert(tableName, nullColumn, values);
        } catch (SQLException e) {
            if (e != null)
                log("insert SQLException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                log("insert Exception=" + e.getMessage());
        } finally {
            db.close();
            return i;
        }
    }


    /**
     * 删除数据
     *
     * @param where    索引
     * @param whereArg 索引值
     * @return
     */
    private long delete(SQLiteDatabase db, String tableName, String where, String[] whereArg) {
        long i = 0;
        try {
            i = db.delete(tableName, where, whereArg);
        } catch (SQLException e) {
            if (e != null)
                log("delete SQLException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                log("delete Exception=" + e.getMessage());
        } finally {
            db.close();
            return i;
        }
    }

    /**
     * 更新数据
     *
     * @param values   数据值
     * @param where    索引
     * @param whereArg 索引值
     * @return
     */
    private long update(SQLiteDatabase db, String tableName, ContentValues values, String where, String[] whereArg) {
        long i = 0;
        try {
            i = db.update(tableName, values, where, whereArg);
        } catch (SQLException e) {
            if (e != null)
                log("update SQLException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                log("update Exception=" + e.getMessage());
        } finally {
            db.close();
            return i;
        }
    }


    /**
     * 封装的查询
     *
     * @param selection   选择的索引
     * @param selectionAg 索引值
     * @return
     */
    private Cursor query(SQLiteDatabase db, String tableName, String[] columns, String selection, String[] selectionAg) {
        Cursor c = null;
        try {
            c = db.query(tableName, columns, selection, selectionAg, null, null, null);
        } catch (SQLException e) {
            if (e != null)
                log("query SQLException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                log("query Exception=" + e.getMessage());
        } finally {
            db.close();
            return c;
        }
    }


    private void log(String message) {
        try {
            if (!AppConfig.DEBUG || StringUtil.isEmpty(message)) return;
            Log.i("gongpengming", message);
        } catch (Exception e) {
            if (e != null)
                Log.i("gongpengming", "show Exception" + e.getMessage());
        }
    }
}
