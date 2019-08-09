package com.core.dao.historical;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.core.db.CommonCursorWrapper;
import com.core.db.DatabaseManager;
import com.core.db.DatabaseTables;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/18.
 * 数据查询功能历史记录数据表操作类
 */
public class HistoricalRecordDao {
    private static HistoricalRecordDao instance;

    private HistoricalRecordDao() {

    }

    public static HistoricalRecordDao getInstance() {
        if (instance == null) {
            synchronized (HistoricalRecordDao.class) {
                if (instance == null) {
                    instance = new HistoricalRecordDao();
                }
            }
        }
        return instance;
    }

    /**
     * 获取ContentValues用于处理数据
     *
     * @param historicalRecordBean
     * @return
     */
    private ContentValues getContentValues(HistoricalRecordBean historicalRecordBean) {
        ContentValues values = new ContentValues();

        values.put(DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID, historicalRecordBean.getSchemeId());
        values.put(DatabaseTables.HistoricalRecordTable.Cols.SCHEME_NAME, historicalRecordBean.getSchemeName());
        values.put(DatabaseTables.HistoricalRecordTable.Cols.SEARCH_FIELD, historicalRecordBean.getSearchField());

        return values;
    }

    /**
     * 通过方案ID获取历史记录
     *
     * @param schemeId
     * @return
     */
    public List<HistoricalRecordBean> getHistoricalRecordBeansById(String schemeId) {
        CommonCursorWrapper cursor = queryHistoricalRecord(DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID + " = ?",
                new String[]{schemeId});

        List<HistoricalRecordBean> historicalRecordBeans = new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                historicalRecordBeans.add(cursor.getHistoricalRecord());
                cursor.moveToNext();
            }
        } catch (Exception e) {

        } finally {
            cursor.close();
        }

        return historicalRecordBeans;
    }

    public void saveToHistoricalRecord(HistoricalRecordBean historicalRecordBean) {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();

        deleteFromHistoricalRecordByField(historicalRecordBean.getSchemeId(), historicalRecordBean.getSearchField());

        ContentValues values = getContentValues(historicalRecordBean);
        sqLiteDatabase.insert(DatabaseTables.HistoricalRecordTable.NAME, null, values);

        DatabaseManager.getInstance().closeDatabase();
    }

    public void deleteFromHistoricalRecordById(String schemeId) {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();

        sqLiteDatabase.delete(DatabaseTables.HistoricalRecordTable.NAME,
                DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID + " = ?",
                new String[]{schemeId});

        DatabaseManager.getInstance().closeDatabase();
    }

    public void deleteFromHistoricalRecordByField(String schemeId, String searchField) {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();

        sqLiteDatabase.delete(DatabaseTables.HistoricalRecordTable.NAME,
                DatabaseTables.HistoricalRecordTable.Cols.SEARCH_FIELD + " = ? and "
                        + DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID + " = ?",
                new String[]{searchField, schemeId});

        DatabaseManager.getInstance().closeDatabase();
    }

    public void updateHistoricalRecord(HistoricalRecordBean historicalRecordBean) {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();

        ContentValues values = getContentValues(historicalRecordBean);
        sqLiteDatabase.update(DatabaseTables.HistoricalRecordTable.NAME, values,
                DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID + " = ?",
                new String[]{historicalRecordBean.getSchemeId()});

        DatabaseManager.getInstance().closeDatabase();
    }

    /**
     * 获取CursorWrapper以更方便的操作Cursor
     *
     * @param whereCause
     * @param whereArgs
     * @return
     */
    public CommonCursorWrapper queryHistoricalRecord(String whereCause, String[] whereArgs) {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();

        Cursor cursor = sqLiteDatabase.query(DatabaseTables.HistoricalRecordTable.NAME, null,
                whereCause,
                whereArgs,
                null, null, "_id DESC");

//        DatabaseManager.getInstance().closeDatabase();

        return new CommonCursorWrapper(cursor);
    }
}
