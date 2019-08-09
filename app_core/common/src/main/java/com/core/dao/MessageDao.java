package com.core.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.db.DatabaseManager;
import com.core.model.MessageModel;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitliker on 2017/3/2.
 */

public class MessageDao {
    private final String TABLE_NAME = "em_erpnews";
    private static MessageDao instance;

    public static MessageDao getInstance() {
        if (instance == null) {
            synchronized (MessageDao.class) {
                instance = new MessageDao();
            }
        }
        return instance;
    }

    private MessageDao() {

    }

    /**
     * 事务创建或是更新，注意 如果该id已经存在，就更新，更新时候不更新状态，要到upStatus更新状态
     *
     * @param models
     * @return
     */
    public boolean createOrinstart(List<MessageModel> models) {
        return createOrinstart(models, false);
    }

    /**
     * 事务创建或是更新，注意 如果该id已经存在，就更新，更新时候不更新状态，要到upStatus更新状态
     *
     * @param models
     * @return
     */
    public boolean createOrinstart(List<MessageModel> models, boolean isFirst) {
        //插入之前先获取以前的数据，有就更新没有就
        if (ListUtils.isEmpty(models)) return false;
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            StringBuilder idBuilder = new StringBuilder();
            for (MessageModel e : models)
                idBuilder.append(e.getId() + ",");
            StringUtil.removieLast(idBuilder);
            List<MessageModel> oldList = getEqualId(db, idBuilder.toString(), master, emcode);
            db.beginTransaction();//开始事物
            ContentValues values = null;
            for (MessageModel m : models) {
                values = getValues(m, master, emcode);
                if (ListUtils.isEmpty(oldList)) {
                    i = db.insert(TABLE_NAME, null, values);
                } else {
                    boolean isUpdate = false;
                    for (MessageModel e : oldList) {
                        if (e.getId() == m.getId()) {//更新
                            String where = "master=? and emcode=? and id=?";
                            String[] whereArgs = {master, emcode, String.valueOf(m.getId())};
                            if (!isFirst) {
                                if (!StringUtil.isEmpty(m.getReadTime()) && !StringUtil.isEmpty(m.getLastTime())
                                        && m.getReadTime().compareTo(m.getLastTime()) < 0)
                                    values.put("isReaded", 0);//不更新状态
                                else
                                    values.remove("isReaded");//不更新状态
                            } else {
                                values.put("isReaded", 0);//不更新状态
                            }
                            i = db.update(TABLE_NAME, values, where, whereArgs);
                            isUpdate = true;
                            break;
                        }
                    }
                    if (!isUpdate)
                        i = db.insert(TABLE_NAME, null, values);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (android.database.SQLException e) {
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }


    /**
     * 通过部门id获取对应员工数量
     *
     * @param defaulid id集合  用","隔开
     * @return
     * @createby gongpengming 2017/2/16
     */
    public List<MessageModel> getEqualId(SQLiteDatabase db, String defaulid, String master, String emcode) {
        List<MessageModel> models = new ArrayList<>();
        try {
            String sql = "SELECT * FROM em_erpnews WHERE master=\'" + master + "\'" + " AND emcode='" + emcode + "\'" + "AND id IN(" + defaulid + ") ";
            Cursor cursor = db.rawQuery(sql, null);
            models = getMessage(cursor, 0);
            cursor.close();
        } catch (Exception e) {

        }
        return models;
    }

    /**
     * 修改单个
     *
     * @param id       消息id
     * @param isReaded
     * @return
     */
    public boolean upStatus(int id, String type, boolean isReaded) {
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("isReaded", isReaded ? 1 : 0);
            String readTime = TimeUtils.f_long_2_str(System.currentTimeMillis());
//            values.put("readTime", readTime);
            String where = "master=? and emcode=? and id=? ";
            if (id == 3) {
                values.put("count", 0);
            }
            String[] whereArgs = {master, emcode, String.valueOf(id)};
            i = db.update(TABLE_NAME, values, where, whereArgs);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {

        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }

    /**
     * 专门为未读的第二层做未读修改
     *
     * @param type
     * @param isReaded
     * @return
     */
    public boolean unReadForTwoFloor(int id, String type, boolean isReaded) {
        if (StringUtil.isEmpty(type)) return false;
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("isReaded", false);
            String where = "master=? and emcode=? and id=?";
            String[] whereArgs = {master, emcode, String.valueOf(id)};
            //先更新子
            i = db.update(TABLE_NAME, values, where, whereArgs);
            where = "master=? and emcode=? and hierarchy=? and type=?";
            String[] whereArgs2 = {master, emcode, String.valueOf(0), type};
            //更新父
            values.put("readTime", "");
            i = db.update(TABLE_NAME, values, where, whereArgs2);
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }


    /**
     * 通过类型修改状态为已读未读
     *
     * @param type     类型
     * @param isReaded 是否阅读
     * @return
     */
    public boolean upStatusByType(String type, boolean isReaded) {
        if (StringUtil.isEmpty(type)) return false;
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String readTime = DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS);
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put("isReaded", isReaded ? 1 : 0);
            if (isReaded)
                values.put("readTime", readTime);
            else
                values.put("readTime", "");
            String where = "master=? and emcode=? and type=?";
            String[] whereArgs = {master, emcode, type};
            i = db.update(TABLE_NAME, values, where, whereArgs);
            if ("kpi".equals(type)) {
                values.put("subTitle", "");
                where = "master=? and emcode=? and type=? and hierarchy=?";
                String[] whereArgs2 = {master, emcode, type, String.valueOf(0)};
                db.update(TABLE_NAME, values, where, whereArgs2);
            }
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }
    }


    /**
     * 获取第一层数据
     *
     * @return
     */
    public List<MessageModel> queryFirstFloor() {
        SQLiteDatabase db = null;
        List<MessageModel> messageModels = null;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return messageModels;
            db = DatabaseManager.getInstance().openDatabase();
            String[] columns = {"id", "count", "title", "subTitle", "time", "type", "isReaded", "readTime","caller","keyValue"};
//TODO
            String selection = "hierarchy=? and master=? and emcode=?  and  isReaded!=? ";
            String[] selectArgs = {String.valueOf(0), master, emcode, String.valueOf(3)};
//            String selection = "hierarchy=? and master=? and emcode=?   ";
//            String[] selectArgs = {String.valueOf(0), master, emcode};
            Cursor c = db.query(TABLE_NAME, columns, selection, selectArgs, null, null, null);
            messageModels = getMessage(c, 0);
            c.close();
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return messageModels;
        }
    }

    /**
     * 获取第二层数据 ,通过类型
     *
     * @return
     */
    public List<MessageModel> queryByType(String type) {
        SQLiteDatabase db = null;
        int hierarchy = 1;
        Boolean platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        List<MessageModel> messageModels = null;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return messageModels;
            db = DatabaseManager.getInstance().openDatabase();
            String[] columns = {"id", "count", "title", "subTitle", "time", "type", "isReaded", "readTime","caller","keyValue"};
            Cursor c = null;
            String selection;
            if (platform) {
                selection = "hierarchy=? and master=? and emcode=?";
                String[] selectArgs = {String.valueOf(hierarchy), master, emcode};
                c = db.query(TABLE_NAME, columns, selection, selectArgs, null, null, null);
            } else {
                selection = "type=? and hierarchy=? and master=? and emcode=?";
                String[] selectArgs = {type, String.valueOf(hierarchy), master, emcode};
                c = db.query(TABLE_NAME, columns, selection, selectArgs, null, null, null);
            }
            messageModels = getMessage(c, hierarchy);
            c.close();
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return messageModels;
        }
    }

    /**
     * 删除整个类型的数据，包括第一第二层
     *
     * @param type
     * @return
     */
    public boolean deleteBytype(String type) {
        if (StringUtil.isEmpty(type)) return false;
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            String where = "master=? and emcode=? and type=?";
            String[] whereArgs = {master, emcode, type};
            //TODO 由于后台没有按要求把推送过的数据过滤  所以这里使用更新操作
//            ContentValues values = new ContentValues();
//            values.put("isReaded", 3);
//            values.put("readTime", readTime);
//            db.update(TABLE_NAME, values, where, whereArgs);
            i = db.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }

    }


    //先插入到第二层，再更新第一层
    public boolean instartSignin(String message, String subTitle) {
        String time = TimeUtils.f_long_2_str(System.currentTimeMillis());
        MessageModel model = new MessageModel();
        model.setType("kpi");
        model.setTitle(message);
        model.setSubTitle(subTitle);
        model.setTime(time);
        model.setLastTime(time);
        SQLiteDatabase db = null;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            db.beginTransaction();
            model.setHierarchy(1);
            model.setId(getSigninId());
            ContentValues values = getValues(model, master, emcode);
            //插入第二层
            db.insert(TABLE_NAME, null, values);
            //插入第一层
            String[] columns = {"count"};
            String selection = "master=? and emcode=? and hierarchy=0 and type='kpi'";
            String[] args = {master, emcode};
            Cursor c = db.query(TABLE_NAME, columns, selection, args, null, null, null);
            model.setId(model.getFirstFloorId());
            model.setHierarchy(0);
            model.setTitle("考勤提醒");
            model.setSubTitle(message);
            if (c.moveToNext()) {//存在，更新
                int count = c.getInt(c.getColumnIndex("count"));
                model.setCount(count + 1);
                values = getValues(model, master, emcode);
                String where = "master=? and emcode=? and hierarchy=0 and type='kpi'";
                db.update(TABLE_NAME, values, where, args);
            } else {//不存在，插入
                values = getValues(model, master, emcode);
                values.put("count", 1);
                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (android.database.SQLException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return true;
        }

    }


    public void updateLastMessageByType(String type) {
        //更新完成后，查看最后一条状态
        SQLiteDatabase db = null;
        try {
            db = DatabaseManager.getInstance().openDatabase();
            boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
            String select = null;
            if (isB2b) select = "master=? and emcode=? and hierarchy=? and isReaded= 0";
            else select = "master=? and emcode=? and type=? and hierarchy=? and isReaded= 0";
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            String[] selectArgs = {master, emcode, type, String.valueOf(1)};
            String[] columns = {"subTitle", "time"};
            Cursor c = db.query(TABLE_NAME, columns, select,
                    isB2b ? new String[]{master, emcode, String.valueOf(1)} : selectArgs, null, null, "time desc");
            String subTitle = "";
            String time = "";
            if (c.moveToFirst()) {
                subTitle = c.getString(c.getColumnIndex("subTitle"));
                time = c.getString(c.getColumnIndex("time"));
            }
            c.close();
            LogUtil.d("subTitle=" + subTitle);
            ContentValues values = new ContentValues();
            values.put("subTitle", subTitle);
            values.put("time", time);
            String where = null;
            if (isB2b)
                where = "master=? and emcode=? and hierarchy=?";
            else where = "master=? and emcode=? and type=? and hierarchy=?";
            String[] whereArgs = {master, emcode, type, String.valueOf(0)};
            db.update(TABLE_NAME, values, where, isB2b ? new String[]{master, emcode, String.valueOf(0)} : whereArgs);
        } catch (Exception e) {

        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    /**
     * 删除某条消息：第二层的type都是一样的,所以得从id上删除
     *
     * @param mag_id
     * @return
     */
    public boolean deleteByid(int mag_id) {
        if (StringUtil.isEmpty(mag_id + "")) return false;
        String id = mag_id + "";
        SQLiteDatabase db = null;
        long i = 0;
        try {
            String master = CommonUtil.getMaster();
            String emcode = CommonUtil.getEmcode();
            if (StringUtil.isEmpty(master) || StringUtil.isEmpty(emcode)) return false;
            db = DatabaseManager.getInstance().openDatabase();
            String where = "master=? and emcode=? and id=?";
            String[] whereArgs = {master, emcode, id};
            i = db.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception e) {
        } finally {
            if (db != null)
                DatabaseManager.getInstance().closeDatabase();
            return i > 0;
        }

    }

    private List<MessageModel> getMessage(Cursor c, int hierarchy) {
        List<MessageModel> messageModels = new ArrayList<>();
        MessageModel model;
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("id"));
            int count = c.getInt(c.getColumnIndex("count"));
            String title = c.getString(c.getColumnIndex("title"));
            String subTitle = c.getString(c.getColumnIndex("subTitle"));
            String time = c.getString(c.getColumnIndex("time"));
            String type = c.getString(c.getColumnIndex("type"));
            int isReaded = c.getInt(c.getColumnIndex("isReaded"));
            String readTime = c.getString(c.getColumnIndex("readTime"));
            int readStatus = c.getInt(c.getColumnIndex("isReaded"));
            String caller = c.getString(c.getColumnIndex("caller"));
            int keyValue = c.getInt(c.getColumnIndex("keyValue"));
            model = new MessageModel(id, hierarchy, count, title, subTitle, time, type, (isReaded == 1 || isReaded == 3), readStatus, readTime);
           model.setCaller(caller);
           model.setKeyValue(keyValue);
            messageModels.add(model);
        }
        return messageModels;
    }

    private ContentValues getValues(MessageModel m, String master, String emcode) {
        ContentValues values = new ContentValues();
        values.put("id", m.getId());
        values.put("master", master);
        values.put("emcode", emcode);
        values.put("count", m.getCount());
        values.put("isReaded", m.isReaded() ? 1 : 0);
        values.put("type", m.getType());
        values.put("time", m.getTime());
        values.put("title", m.getTitle());
        values.put("subTitle", m.getSubTitle());
        values.put("hierarchy", m.getHierarchy());
        values.put("caller", m.getCaller());
        values.put("keyValue", m.getKeyValue());
        return values;
    }


    public int getSigninId() {
        int id = PreferenceUtils.getInt("signinId", 1000) + 1;
        PreferenceUtils.putInt("signinId", id);
        return id;
    }
}
