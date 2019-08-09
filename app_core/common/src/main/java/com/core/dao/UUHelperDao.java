package com.core.dao;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.core.app.MyApplication;
import com.core.db.DatabaseManager;
import com.core.db.DatabaseTables;
import com.core.model.UUHelperModel;

import java.util.List;

/**
 * Created by Bitliker on 2017/9/8.
 */

public class UUHelperDao extends Dao<UUHelperModel> {
	private static UUHelperDao instance;

	public static UUHelperDao getInstance() {
		UUHelperDao inst = instance;
		if (inst == null) {
			synchronized (UUHelperDao.class) {
				inst = instance;
				if (inst == null) {
					inst = new UUHelperDao();
					instance = inst;
				}
			}
		}
		return inst;
	}

	private UUHelperDao() {

	}

	@Override
	protected String getTable() {
		return DatabaseTables.UUHelperTable.NAME;
	}

	@Override
	protected ContentValues getValues(UUHelperModel model) throws Exception {
		ContentValues values = new ContentValues();
		values.put(DatabaseTables.UUHelperTable.Cols.TIME_SEND, model.getTimeSend());
		values.put(DatabaseTables.UUHelperTable.Cols.USER_ID, MyApplication.getInstance().getLoginUserId());
		values.put(DatabaseTables.UUHelperTable.Cols.IMAGE_URL, model.getImageUrl());
		values.put(DatabaseTables.UUHelperTable.Cols.ICON_URL, model.getIconUrl());
		values.put(DatabaseTables.UUHelperTable.Cols.LINK_URL, model.getLinkUrl());
		values.put(DatabaseTables.UUHelperTable.Cols.CONTENT, model.getContent());
		values.put(DatabaseTables.UUHelperTable.Cols.READED, model.isReaded());
		values.put(DatabaseTables.UUHelperTable.Cols.DATE, model.getDate());
		values.put(DatabaseTables.UUHelperTable.Cols.TITLE, model.getTitle());
		values.put(DatabaseTables.UUHelperTable.Cols.TYPE, model.getType());
		return values;
	}

	@Override
	protected UUHelperModel getData(Cursor c) throws Exception {
		int _id = c.getInt(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.ID));
		long timeSend = c.getLong(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.TIME_SEND));
		String imageUrl = c.getString(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.IMAGE_URL));
		String iconUrl = c.getString(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.ICON_URL));
		String linkUrl = c.getString(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.LINK_URL));
		String content = c.getString(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.CONTENT));
		String date = c.getString(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.DATE));
		String title = c.getString(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.TITLE));
		int type = c.getInt(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.TYPE));
		boolean readed = c.getInt(c.getColumnIndex(DatabaseTables.UUHelperTable.Cols.READED)) > 0;
		return new UUHelperModel(timeSend, date, type)
				.setId(_id)
				.setIconUrl(iconUrl)
				.setImageUrl(imageUrl)
				.setLinkUrl(linkUrl)
				.setContent(content)
				.setTitle(title)
				.setReaded(readed);
	}


	public boolean saveData(UUHelperModel uuHelperItem) {
		return insert(uuHelperItem, null, null);
	}

	public List<UUHelperModel> getAllModels() {
		String orderBy = DatabaseTables.UUHelperTable.Cols.DATE + " asc";
		String where = DatabaseTables.UUHelperTable.Cols.USER_ID + " =?";
		String[] whereArgs = {MyApplication.getInstance().getLoginUserId()};
		return query(null, where, whereArgs, orderBy);
	}

	public boolean deleteData(int id) {
		if (id == -1) {
			return clear();
		} else {
			return delete("id=?", new String[]{String.valueOf(id)});
		}
	}

	public boolean updateRead(int id) {
		long i = 0;
		try {
			String where = DatabaseTables.UUHelperTable.Cols.ID + " =?";
			String[] whereArgs = {String.valueOf(id)};
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			ContentValues values = new ContentValues();
			values.put(DatabaseTables.UUHelperTable.Cols.READED, 1);
			i = db.update(getTable(), values, where, whereArgs);
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}

	public boolean updateRead(boolean readed) {
		long i = 0;
		try {
			String where = null;
			String[] whereArgs = null;
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			ContentValues values = new ContentValues();
			values.put(DatabaseTables.UUHelperTable.Cols.READED, readed ? 1 : 0);
			i = db.update(getTable(), values, where, whereArgs);
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i > 0;
		}
	}
}
