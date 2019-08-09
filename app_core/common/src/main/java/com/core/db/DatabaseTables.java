package com.core.db;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据库表
 */
public interface DatabaseTables {
    interface HistoricalRecordTable {
        String NAME = "historical_record";

        interface Cols {
            String SCHEME_ID = "scheme_id";
            String SCHEME_NAME = "scheme_name";
            String SEARCH_FIELD = "search_field";
        }
    }

    interface UUHelperTable {
        String NAME = "uu_helper";

        interface Cols {
            String ID = "id";
            String USER_ID = "userId";
            String TIME_SEND = "timeSend";
            String DATE = "date";
            String IMAGE_URL = "imageUrl";
            String ICON_URL = "iconUrl";
            String LINK_URL = "linkUrl";
            String CONTENT = "content";
            String READED = "readed";
            String TITLE = "title";
            String TYPE = "type";
        }
    }
    interface TopContactsTable {
        String NAME = "top_contacts";
        interface Cols {
            String USER_ID = "userId";
            String OWNER_ID = "ownerId";
            String PHONE = "phone";
            String NAME = "name";
            String EM_CODE = "emCode";
            String LAST_TIME = "lastTime";
            String STATUS = "status";
        }
    }
}
