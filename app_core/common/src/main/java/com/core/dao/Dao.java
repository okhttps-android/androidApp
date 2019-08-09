package com.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.db.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitliker on 2017/7/6.
 */

public abstract class Dao<T> {


	public boolean insert(List<T> datas) {
		if (ListUtils.isEmpty(datas)) return false;
		long i = 0;
		try {
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			db.beginTransaction();
			for (int j = 0; j < datas.size(); j++) {
				T t = datas.get(j);
				i = db.insert(getTable(), null, getValues(t));
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}

	public boolean insertAndUpdate(String where, String[][] whereArgss, List<T> datas) {
		if (ListUtils.isEmpty(datas)) return false;
		long i = 0;
		try {
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			db.beginTransaction();
			for (int j = 0; j < datas.size(); j++) {
				T t = datas.get(j);
				i = db.insert(getTable(), null, getValues(t));
				if (i < 0 && !StringUtil.isEmpty(where) && whereArgss != null && whereArgss.length > j) {
					i = db.update(getTable(), getValues(t), where, whereArgss[j]);
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}

	public boolean insert(T t, String updateWhere, String[] updateWhereArgs) {
		if (t == null) return false;
		long i = 0;
		try {
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			ContentValues values = getValues(t);
			i = db.insert(getTable(), null, values);
			if (i < 0) {
				i = db.update(getTable(), values, updateWhere, updateWhereArgs);
			}
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}


	public boolean update(T t, String where, String[] whereArgs) {
		if (t == null) return false;
		long i = 0;
		try {
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			i = db.update(getTable(), getValues(t), where, whereArgs);
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}

	public List<T> query(String where, String[] whereArgs) {
		return query(null, where, whereArgs, null, null, null);
	}

	public List<T> query(String[] columns, String where, String[] whereArgs) {
		return query(columns, where, whereArgs, null, null, null);
	}

	public List<T> query(String[] columns, String where, String[] whereArgs, String orderBy) {
		return query(columns, where, whereArgs, null, null, orderBy);
	}

	public List<T> query(String[] columns, String where, String[] whereArgs, String groupBy, String having, String orderBy) {
		List<T> datas = new ArrayList<T>();
		try {
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			Cursor c = db.query(getTable(), columns, where, whereArgs, groupBy, having, orderBy);
			while (c.moveToNext()) {
				T t = getData(c);
				if (t != null) {
					datas.add(t);
				}
			}
			c.close();
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return datas;
		}
	}

	public boolean clear() {
		return delete(null, null);
	}

	public boolean delete(String where, String[] whereArgs) {
		long i = 0;
		try {
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			i = db.delete(getTable(), where, whereArgs);
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}

	protected abstract String getTable();

	protected abstract ContentValues getValues(T t) throws Exception;

	protected abstract T getData(Cursor c) throws Exception;

}
