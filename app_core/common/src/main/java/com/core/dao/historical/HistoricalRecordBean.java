package com.core.dao.historical;

/**
 * Created by RaoMeng on 2017/8/18.
 */
public class HistoricalRecordBean {
    private int mId;
    private String mSchemeId;
    private String mSchemeName;
    private String mSearchField;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getSchemeId() {
        return mSchemeId;
    }

    public void setSchemeId(String schemeId) {
        mSchemeId = schemeId;
    }

    public String getSchemeName() {
        return mSchemeName;
    }

    public void setSchemeName(String schemeName) {
        mSchemeName = schemeName;
    }

    public String getSearchField() {
        return mSearchField;
    }

    public void setSearchField(String searchField) {
        mSearchField = searchField;
    }
}
