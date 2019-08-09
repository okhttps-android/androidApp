package com.uas.appcontact.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.app.MyApplication;
import com.core.dao.Dao;
import com.core.db.DatabaseTables;
import com.core.model.Friend;
import com.core.model.Hrorgs;
import com.core.utils.sortlist.BaseSortModel;
import com.core.xmpp.dao.FriendDao;
import com.uas.appcontact.model.TopContacts;

import java.util.List;

/**
 * Created by Bitlike on 2018/1/23.
 */

public class TopContactsDao extends Dao<TopContacts> {
    @Override
    protected String getTable() {
        return DatabaseTables.TopContactsTable.NAME;
    }

    @Override
    protected ContentValues getValues(TopContacts topContacts) throws Exception {
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.TopContactsTable.Cols.USER_ID, topContacts.getUserId());
        values.put(DatabaseTables.TopContactsTable.Cols.OWNER_ID, topContacts.getOwnerId());
        values.put(DatabaseTables.TopContactsTable.Cols.PHONE, topContacts.getPhone());
        values.put(DatabaseTables.TopContactsTable.Cols.NAME, topContacts.getName());
        values.put(DatabaseTables.TopContactsTable.Cols.EM_CODE, topContacts.getEmCode());
        values.put(DatabaseTables.TopContactsTable.Cols.LAST_TIME, topContacts.getLastTime());
        values.put(DatabaseTables.TopContactsTable.Cols.STATUS, topContacts.getStatus());
        return values;
    }

    @Override
    protected TopContacts getData(Cursor c) throws Exception {
        TopContacts topContacts = new TopContacts();
        topContacts.setUserId(c.getString(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.USER_ID)));
        topContacts.setOwnerId(c.getString(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.OWNER_ID)));
        topContacts.setPhone(c.getString(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.PHONE)));
        topContacts.setName(c.getString(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.NAME)));
        topContacts.setEmCode(c.getString(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.EM_CODE)));
        topContacts.setLastTime(c.getLong(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.LAST_TIME)));
        topContacts.setStatus(c.getInt(c.getColumnIndex(DatabaseTables.TopContactsTable.Cols.STATUS)));
        return topContacts;
    }


    private static TopContactsDao api;

    public static TopContactsDao api() {
        if (api == null) {
            synchronized (TopContactsDao.class) {
                if (api == null) {
                    api = new TopContactsDao();
                }
            }
        }
        return api;
    }

    private TopContactsDao() {

    }


    public void addGoodFriend(Friend friend) {
        if (friend != null) {
            TopContacts topContacts = new TopContacts();
            String ownerId = MyApplication.getInstance().getLoginUserId();
            String userId = String.valueOf(friend.getUserId());
            topContacts.setUserId(userId);
            topContacts.setOwnerId(ownerId);
            topContacts.setName(friend.getNickName());
            topContacts.setEmCode(friend.getEmCode());
            topContacts.setPhone(friend.getPhone());
            topContacts.setLastTime(System.currentTimeMillis());
            addGoodFriend(topContacts);
        }
    }


    public void addGoodFriend(Hrorgs.Employee employee) {
        if (employee != null) {
            TopContacts topContacts = new TopContacts();
            String ownerId = MyApplication.getInstance().getLoginUserId();
            String userId = String.valueOf(employee.em_imid);
            topContacts.setUserId(userId);
            topContacts.setOwnerId(ownerId);
            topContacts.setName(employee.em_name);
            topContacts.setEmCode(employee.em_code);
            topContacts.setPhone(employee.em_mobile);
            topContacts.setLastTime(System.currentTimeMillis());
            addGoodFriend(topContacts);
        }
    }

    /**
     * 添加常用
     * 1.拨打电话
     * 2.发送聊天信息
     */
    public void addGoodFriend(TopContacts topContacts) {
        String where = DatabaseTables.TopContactsTable.Cols.OWNER_ID + "=? and "
                + DatabaseTables.TopContactsTable.Cols.USER_ID + "=? ";
        String[] whereAge = new String[]{topContacts.getOwnerId(), topContacts.getUserId()};
        List<TopContacts> dbContacts = query(where, whereAge);
        if (ListUtils.isEmpty(dbContacts)) {
            boolean b = insert(topContacts, where, whereAge);
            LogUtil.i("insert=" + b);
        } else {
            TopContacts dbContact = dbContacts.get(0);
            if (dbContact != null && (System.currentTimeMillis() - dbContact.getLastTime())< (86400000)) {
                topContacts.setStatus(1);
            }
            LogUtil.i("dbContact=" + JSON.toJSONString(dbContact));
            boolean b = update(topContacts, where, whereAge);
            LogUtil.i("update=" + b);
        }
    }
    public List<TopContacts> getTopContacts(){
        String owerId= MyApplication.getInstance().getLoginUserId();
        String where = DatabaseTables.TopContactsTable.Cols.OWNER_ID + "=? ";
//                + " and " +DatabaseTables.TopContactsTable.Cols.STATUS + "=? ";
        String[] whereAge = new String[]{owerId/*, String.valueOf(1)*/};
        LogUtil.i("owerId="+owerId);
        LogUtil.i("where="+where);
        return query(where,whereAge);
    }
}
