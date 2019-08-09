package com.core.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.core.dao.historical.HistoricalRecordBean;

/**
 * @author RaoMeng
 * @describe CursorWrapper封装类
 * @date 2017/12/12 10:17
 */

public class CommonCursorWrapper extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public CommonCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public HistoricalRecordBean getHistoricalRecord() {
        int id = getInt(getColumnIndex("_id"));
        String schemeId = getString(getColumnIndex(DatabaseTables.HistoricalRecordTable.Cols.SCHEME_ID));
        String schemeName = getString(getColumnIndex(DatabaseTables.HistoricalRecordTable.Cols.SCHEME_NAME));
        String searchField = getString(getColumnIndex(DatabaseTables.HistoricalRecordTable.Cols.SEARCH_FIELD));

        HistoricalRecordBean historicalRecordBean = new HistoricalRecordBean();
        historicalRecordBean.setId(id);
        historicalRecordBean.setSchemeId(schemeId);
        historicalRecordBean.setSchemeName(schemeName);
        historicalRecordBean.setSearchField(searchField);

        return historicalRecordBean;
    }
}
