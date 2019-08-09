package com.core.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.common.LogUtil;
import com.core.app.R;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.db.DatabaseManager;
import com.core.model.B2BMsg;
import com.core.model.EmployeesEntity;
import com.core.model.Hrorgs;
import com.core.model.HrorgsEntity;
import com.core.model.PersonalSubscriptionBean;
import com.core.model.SubscriptionNumber;
import com.core.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author :Administrator   2016年1月6日 上午10:40:04
 * @注释:数据库管理类
 */
public class DBManager {

    public SQLiteDatabase getDb() {
        return db;
    }

    private SQLiteDatabase db;
    private static volatile DBManager instance;

    public static DBManager getInstance() {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager();
                }
            }
        }
        return instance;
    }

    ;

    public DBManager() {
        db = DatabaseManager.getInstance().openDatabase();
    }

    @Deprecated
    public DBManager(Context ct) {
        this();
    }

    public void saveB2bMsg(B2BMsg entity) {
        String sql = "replace into B2BMSG"
                + "(b2b_content,b2b_time,b2b_hasRead,b2b_master)"
                + " values (?, ?, ?, ?);";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindString(1, entity.getContent());
        sta.bindString(2, entity.getTime());
        sta.bindLong(3, entity.getHasRead());
        sta.bindString(4, entity.getMaster());
        sta.execute();
    }

    public List<B2BMsg> queryB2bList(String[] selectionArgs, String selection) {
        List<B2BMsg> entity = new ArrayList<>();
        String table = "B2BMSG";
        String groupBy = null;
        String having = null;
        String orderBy = " b2b_hasRead,b2b_time DESC";
        if (checkDbObject()) return entity;
        Cursor cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
        while (cursor.moveToNext()) {
            B2BMsg model = new B2BMsg();
            model.setId(cursor.getInt(cursor.getColumnIndex("id")));
            model.setContent(cursor.getString(cursor.getColumnIndex("b2b_content")));
            model.setHasRead(cursor.getInt(cursor.getColumnIndex("b2b_hasRead")));
            model.setTime(cursor.getString(cursor.getColumnIndex("b2b_time")));
            model.setMaster(cursor.getString(cursor.getColumnIndex("b2b_master")));
            entity.add(model);
        }
        cursor.close();

        return entity;

    }

    public void updateB2b(B2BMsg data) {
        String sql = "update B2BMSG"
                + " set b2b_hasRead=?"
                + " where id=? and b2b_master=? ;";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindLong(1, data.getHasRead());
        sta.bindLong(2, data.getId());
        sta.bindString(3, data.getMaster());
        sta.executeInsert();
    }

    public void deleteB2b(B2BMsg data) {
        String table = "B2BMSG";
        String[] whereArgs = {String.valueOf(data.getId()), data.getMaster()};
        String whereClause = "id=? and b2b_master=? ";
        if (checkDbObject()) return;
        db.delete(table, whereClause, whereArgs);
    }

    /**
     * @author Administrator
     * @功能:增加组织架构---批量
     */
    public void saveHrogrs(List<HrorgsEntity> hrorgsList) {
        String sql = "replace into hrorgs"
                + "(or_code,whichsys,or_subof,company,or_isleaf,or_name,or_id,or_flag,or_headmanname,or_headmancode,or_remark)"
                + " values (?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        db.beginTransaction();
        for (int i = 0; i < hrorgsList.size(); i++) {
            HrorgsEntity entity = hrorgsList.get(i);
            sta.bindString(1, getStringNotNull(entity.getOr_code()));
            sta.bindString(2, getStringNotNull(entity.getWhichsys()));
            sta.bindLong(3, entity.getOr_subof());
            sta.bindString(4, getStringNotNull(entity.getCompany()));
            sta.bindLong(5, entity.getOr_isleaf());
            sta.bindString(6, getStringNotNull(entity.getOr_name()));
            sta.bindLong(7, entity.getOr_id());
            sta.bindString(8, getStringNotNull(entity.getFlag()));
            sta.bindString(9, getStringNotNull(entity.getOr_headmanname()));
            sta.bindString(10, getStringNotNull(entity.getOr_headmancode()));
            sta.bindLong(11, entity.getOr_remark());
            sta.executeInsert();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private String getStringNotNull(String chche) {
        return StringUtil.isEmpty(chche) ? "" : chche;
    }

    public void updateHrogrRemark(int or_id, int or_remark, String master) {
        String sql = "update hrorgs"
                + " set or_remark=?"
                + " where or_id=? and whichsys=?;";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindLong(1, or_remark);
        sta.bindLong(2, or_id);//更新关键字段
        sta.bindString(3, master);
        sta.executeInsert();
    }

    /**
     * @author Administrator
     * @功能:更新组织架构---批量 根据or_code来更新数据
     */
    public void updateHrogrs(List<HrorgsEntity> hrorgsList) {
        String sql = "update hrorgs"
                + " set or_code=?,whichsys=?,or_subof=?,company=?,or_isleaf=?,or_name=?,or_id=?,or_flag=?"
                + " where or_code=?;";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        db.beginTransaction();
        for (int i = 0; i < hrorgsList.size(); i++) {
            HrorgsEntity entity = hrorgsList.get(i);
            sta.bindString(1, entity.getOr_code());
            sta.bindString(2, entity.getWhichsys());
            sta.bindLong(3, entity.getOr_subof());
            sta.bindString(4, entity.getCompany());
            sta.bindLong(5, entity.getOr_isleaf());
            sta.bindString(6, entity.getOr_name());
            sta.bindLong(7, entity.getOr_id());
            sta.bindString(8, entity.getFlag());
            sta.bindString(9, entity.getOr_code());//更新关键字段
            sta.executeInsert();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * @author Administrator
     * @功能:增加员工信息---批量
     */
    public void saveEmployees(List<EmployeesEntity> employeesEntities) {
        String sql = "replace into employees(EM_ID,EM_CODE,EM_NAME,EM_POSITION,EM_DEFAULTORNAME,EM_DEPART,EM_MOBILE,EM_UU,COMPANY,WHICHSYS,Em_defaultorid,Em_flag,Em_imid,EM_EMAIL) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        db.beginTransaction();
        String whichsys = CommonUtil.getMaster();
        if (StringUtil.isEmpty(whichsys))
            whichsys = MyApplication.getInstance().getString(R.string.common_noinput);
        for (int i = 0; i < employeesEntities.size(); i++) {
            EmployeesEntity entity = employeesEntities.get(i);
            sta.bindString(1, String.valueOf(entity.getEM_ID()));
            sta.bindString(2, entity.getEM_CODE() == null ? "null" : entity.getEM_CODE());
            sta.bindString(3, entity.getEM_NAME());
            sta.bindString(4, entity.getEM_POSITION());
            sta.bindString(5, entity.getEM_DEFAULTORNAME() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : entity.getEM_DEFAULTORNAME());
            sta.bindString(6, entity.getEM_DEPART());
            sta.bindString(7, entity.getEM_MOBILE() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : entity.getEM_MOBILE());
            sta.bindString(8, String.valueOf(entity.getEM_UU()));
            sta.bindString(9, entity.getCOMPANY());
            sta.bindString(10, StringUtil.isEmpty(entity.getWHICHSYS()) ? whichsys : entity.getWHICHSYS());
            sta.bindLong(11, entity.getEm_defaultorid());
            sta.bindString(12, entity.getFLAG());
            sta.bindString(13, String.valueOf(entity.getEm_IMID()));
            sta.bindString(14, StringUtil.isEmpty(entity.getEM_EMAIL()) ? MyApplication.getInstance().getString(R.string.common_noinput) : entity.getEM_EMAIL());
            sta.executeInsert();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * @author Administrator
     * @功能:增加员工信息---批量
     */
    public void updateEmployees(List<EmployeesEntity> employeesEntities) {
        String sql = "update employees"
                + " set EM_ID=?,EM_CODE=?,EM_NAME=?,EM_POSITION=?,EM_DEFAULTORNAME=?,EM_DEPART=?,EM_MOBILE=?,EM_UU=?,COMPANY=?,WHICHSYS=?,Em_defaultorid=?,Em_flag=?"
                + " where EM_CODE=?;";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        db.beginTransaction();
        for (int i = 0; i < employeesEntities.size(); i++) {
            EmployeesEntity entity = employeesEntities.get(i);
            sta.bindString(1, String.valueOf(entity.getEM_ID()));
            sta.bindString(2, entity.getEM_CODE());
            sta.bindString(3, entity.getEM_NAME());
            sta.bindString(4, entity.getEM_POSITION());
            sta.bindString(5, entity.getEM_DEFAULTORNAME() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : entity.getEM_DEFAULTORNAME());
            sta.bindString(6, entity.getEM_DEPART());
            sta.bindString(7, entity.getEM_MOBILE() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : entity.getEM_MOBILE());
            sta.bindString(8, String.valueOf(entity.getEM_UU()));
            sta.bindString(9, entity.getCOMPANY());
            sta.bindString(10, entity.getWHICHSYS());
            sta.bindLong(11, entity.getEm_defaultorid());
            sta.bindString(12, entity.getFLAG());
            sta.bindString(13, entity.getEM_CODE());//匹配关键字
            sta.executeInsert();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        // db.close();
    }

    private boolean checkDbObject() {
        if (db == null) db = DatabaseManager.getInstance().openDatabase();
        if (!db.isOpen()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @author Administrator
     * @功能:记录缓存的时间
     */
    public void saveCacheTime(Map<String, Object> data) {
        String sql = "insert into empdate"
                + "(ed_lastdate,ed_kind,ed_company,ed_whichsys)"
                + " values (?, ?, ?, ?);";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);

        sta.bindString(1, data.get("ed_lastdate").toString());
        sta.bindString(2, data.get("ed_kind").toString());
        sta.bindString(3, data.get("ed_company").toString());
        sta.bindString(4, data.get("ed_whichsys").toString());
        sta.executeInsert();

    }

    /**
     * @author Administrator
     * @功能:记录缓存的时间
     */
    public void updateCacheTime(Map<String, Object> data) {
        String sql = "update empdate"
                + " set ed_lastdate=?"
                + " where ed_kind=? and ed_company=? and ed_whichsys=?;";
        if (checkDbObject()) return;
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindString(1, data.get("ed_lastdate").toString());
        sta.bindString(2, data.get("ed_kind").toString());
        sta.bindString(3, data.get("ed_company").toString());
        sta.bindString(4, data.get("ed_whichsys").toString());
        sta.executeInsert();
    }

    /**
     * @author Administrator
     * @功能:获得上一次缓存的时间
     */
    public String select_getCacheTime(String[] selectionArgs, String where) {
        String datestr = null;
        String table = "empdate";
        String[] columns = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if (checkDbObject()) return null;
        Cursor cursor = db.query(table, columns, where, selectionArgs, groupBy, having, orderBy);
        while (cursor.moveToNext()) {
            datestr = cursor.getString(cursor.getColumnIndex("ed_lastdate"));
        }
        cursor.close();
        return datestr;
    }

    /**
     * @author Administrator
     * @功能:批量删除两个表数据
     */
    public void deleteHrogrsAndEmployees() {
        String table = "employees";
        String whereClause = "em_flag=?";
        String[] whereArgs = {"DELETE"};
        if (checkDbObject()) return;
        db.delete(table, whereClause, whereArgs);

        table = "hrorgs";
        whereClause = "or_flag=?";
        db.delete(table, whereClause, whereArgs);
    }


    /**
     * @author Administrator
     * @功能：获取组织架构的通讯录下级数据or_subof=?
     */
    public Hrorgs select_getLeafData(String[] selectionArgs, String selection, String or_id, String master) {
        Hrorgs hrorgsEntities = new Hrorgs();
        List<Hrorgs.HrorgItem> hlist = new ArrayList<Hrorgs.HrorgItem>();
        List<Hrorgs.Employee> elist = new ArrayList<Hrorgs.Employee>();
        String table = "hrorgs";
        String[] columns = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if (checkDbObject()) return hrorgsEntities;
        Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        while (cursor.moveToNext()) {
            Hrorgs.HrorgItem hrorgItem = new Hrorgs().new HrorgItem();
            hrorgItem.setOr_code(cursor.getString(cursor.getColumnIndex("or_code")));
            int id = cursor.getInt(cursor.getColumnIndex("or_id"));
            hrorgItem.setOr_id(id);
            hrorgItem.setOr_name(cursor.getString(cursor.getColumnIndex("or_name")));
            hrorgItem.setOr_isleaf(Integer.valueOf(cursor.getString(cursor.getColumnIndex("or_isleaf"))));
            hrorgItem.setOr_subof(Integer.valueOf(cursor.getString(cursor.getColumnIndex("or_subof"))));
            hrorgItem.setOr_emcount(selectEmployeesCount(id, master));
            hlist.add(hrorgItem);
        }
        table = "employees";
        columns = null;
        selection = "em_defaultorid=? and WHICHSYS=?";
        selectionArgs = new String[]{or_id, master};
        groupBy = null;
        having = null;
        orderBy = null;
        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        while (cursor.moveToNext()) {
            Hrorgs.Employee employeeItem = new Hrorgs().new Employee();
            employeeItem.setEm_code(cursor.getString(cursor.getColumnIndex("em_code")));
            employeeItem.setEm_id(cursor.getInt(cursor.getColumnIndex("em_id")));
            employeeItem.setEm_name(cursor.getString(cursor.getColumnIndex("em_name")));
            employeeItem.setEm_defaultorname(cursor.getString(cursor.getColumnIndex("em_defaultorname")));
            employeeItem.setEm_email(cursor.getString(cursor.getColumnIndex("em_email")));
            employeeItem.setEm_mobile(cursor.getString(cursor.getColumnIndex("em_mobile")));

            employeeItem.setEm_imid(Integer.valueOf(
                    cursor.getString(cursor.getColumnIndex("em_imid")) == null ? "0" :
                            cursor.getString(cursor.getColumnIndex("em_imid"))));
            elist.add(employeeItem);
        }
        // db.close();
        cursor.close();
        hrorgsEntities.setEmployees(elist);
        hrorgsEntities.setHrorgs(hlist);
        return hrorgsEntities;
    }

    public int selectEmployeesCount(int or_id, String master) {
        int catCount = 0;
//        String  table = "employees";
//        String [] columns = null;
//        String  selection = "em_defaultorid=? and WHICHSYS=?";
        String[] selectionArgs = new String[]{String.valueOf(or_id), master};
//        String groupBy = null;
//        String having = null;
//        String orderBy = null;
        //db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        if (checkDbObject()) return 0;
        Cursor cursor =
                db.rawQuery("select count(*) from employees where em_defaultorid=? and WHICHSYS=?", selectionArgs);
        if (cursor.moveToFirst()) {
            catCount = cursor.getInt(0);
        }
        cursor.close();
        LogUtil.d("count(*):" + catCount);
        return catCount;
    }


    public void deleteData(String master) {
        String table = "hrorgs";
        String[] whereArgs = {master};
        String whereClause = "whichsys=?";
        if (checkDbObject()) return;
        db.delete(table, whereClause, whereArgs);
        table = "employees";
        db.delete(table, whereClause, whereArgs);
    }

    /**
     * @author Administrator
     * @功能:获取组织架构的通讯录根数据 父节点为0的数据
     * Hrorgs连接两个表的数据模型
     */
    public HrorgsEntity select_getRootData(String[] selectionArgs, String selection) {
        try {
            HrorgsEntity hrorgsEntities = null;
            String table = "hrorgs";
            String[] columns = null;
            //String[] selectionArgs = new String[]{key};
            String groupBy = null;
            String having = null;
            String orderBy = null;
            if (checkDbObject()) return hrorgsEntities;
            Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            while (cursor.moveToNext()) {
                hrorgsEntities = new HrorgsEntity();
                hrorgsEntities.setOr_subof(Integer.valueOf(cursor.getString(cursor.getColumnIndex("or_subof"))));
                hrorgsEntities.setOr_code(cursor.getString(cursor.getColumnIndex("or_code")));
                hrorgsEntities.setFlag(cursor.getString(cursor.getColumnIndex("or_flag")));
                hrorgsEntities.setOr_name(cursor.getString(cursor.getColumnIndex("or_name")));
                hrorgsEntities.setOr_id(cursor.getInt(cursor.getColumnIndex("or_id")));
                hrorgsEntities.setOr_isleaf(Integer.valueOf(cursor.getString(cursor.getColumnIndex("or_isleaf"))));
                hrorgsEntities.setCompany(cursor.getString(cursor.getColumnIndex("company")));
                hrorgsEntities.setOr_headmancode(cursor.getString(cursor.getColumnIndex("or_headmancode")));
                hrorgsEntities.setOr_headmanname(cursor.getString(cursor.getColumnIndex("or_headmanname")));
                hrorgsEntities.setWhichsys(cursor.getString(cursor.getColumnIndex("whichsys")));
            }
            cursor.close();
            return hrorgsEntities;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * @author Administrator
     * @功能:获取组织架构的通讯录根数据 父节点为0的数据
     * Hrorgs连接两个表的数据模型
     */
    public List<HrorgsEntity> queryHrorgList(String[] selectionArgs, String selection) {
        List<HrorgsEntity> hrorgsEntities = new ArrayList<>();
        String table = "hrorgs";
        String[] columns = null;
        //String[] selectionArgs = new String[]{key};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if (checkDbObject()) return hrorgsEntities;
        Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        while (cursor.moveToNext()) {
            HrorgsEntity hrorgsEntity = new HrorgsEntity();
            hrorgsEntity.setOr_subof(Integer.valueOf(cursor.getString(cursor.getColumnIndex("or_subof"))));
            hrorgsEntity.setOr_code(cursor.getString(cursor.getColumnIndex("or_code")));
            hrorgsEntity.setFlag(cursor.getString(cursor.getColumnIndex("or_flag")));
            hrorgsEntity.setOr_name(cursor.getString(cursor.getColumnIndex("or_name")));
            hrorgsEntity.setOr_id(cursor.getInt(cursor.getColumnIndex("or_id")));
            hrorgsEntity.setOr_isleaf(Integer.valueOf(cursor.getString(cursor.getColumnIndex("or_isleaf"))));
            hrorgsEntity.setCompany(cursor.getString(cursor.getColumnIndex("company")));
            hrorgsEntity.setOr_headmancode(cursor.getString(cursor.getColumnIndex("or_headmancode")));
            hrorgsEntity.setOr_headmanname(cursor.getString(cursor.getColumnIndex("or_headmanname")));
            hrorgsEntity.setWhichsys(cursor.getString(cursor.getColumnIndex("whichsys")));
            hrorgsEntity.setOr_remark(cursor.getInt(cursor.getColumnIndex("or_remark")));
            hrorgsEntities.add(hrorgsEntity);
        }
        cursor.close();
        return hrorgsEntities;
    }

    /**
     * @author Administrator
     * @功能:获取某员工的信息
     */
    public synchronized List<EmployeesEntity> select_getEmployee(String[] selectionArgs, String selection) {
        List<EmployeesEntity> entity = new ArrayList<EmployeesEntity>();
        String table = "employees";
        String groupBy = null;
        String having = null;
        String orderBy = null;
        Cursor cursor = null;
        try {
            if (checkDbObject())
                db = DatabaseManager.getInstance().openDatabase();
            cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
            while (cursor.moveToNext()) {
                EmployeesEntity model = new EmployeesEntity();
                model.setEM_CODE(cursor.getString(cursor.getColumnIndex("em_code")));
                model.setEm_defaultorid(cursor.getInt(cursor.getColumnIndex("em_defaultorid")));
                model.setEM_DEFAULTORNAME(cursor.getString(cursor.getColumnIndex("em_defaultorname")));
                model.setEM_DEPART(cursor.getString(cursor.getColumnIndex("em_depart")));
                model.setFLAG(cursor.getString(cursor.getColumnIndex("em_flag")));
                model.setEM_ID(cursor.getInt(cursor.getColumnIndex("em_id")));
                model.setEM_MOBILE(cursor.getString(cursor.getColumnIndex("em_mobile")));
                model.setEM_EMAIL(cursor.getString(cursor.getColumnIndex("em_email")));
                model.setEM_NAME(cursor.getString(cursor.getColumnIndex("em_name")));
                model.setEM_POSITION(cursor.getString(cursor.getColumnIndex("em_position")));
                model.setEM_UU(cursor.getString(cursor.getColumnIndex("em_uu")));
                model.setWHICHSYS(cursor.getString(cursor.getColumnIndex("whichsys")));
                model.setEm_IMID(Integer.valueOf(cursor.getString(
                        cursor.getColumnIndex("em_imid")) == null ? "0" : cursor.getString(
                        cursor.getColumnIndex("em_imid"))
                ));
                entity.add(model);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();
            return entity;
        }
    }

    /**
     * 通过部门id获取对应员工数量
     *
     * @param defaulid id集合  用","隔开
     * @return
     * @createby gongpengming 2017/2/16
     */
    public int getEmployeeNumber(String defaulid) {
        int count = 0;
        try {
            String master = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master");
            if (checkDbObject())
                db = DatabaseManager.getInstance().openDatabase();
            String sql = "SELECT * FROM EMPLOYEES WHERE WHICHSYS=\'" + master + "\' AND EM_DEFAULTORID IN(" + defaulid + ") ";
            Cursor cursor = db.rawQuery(sql, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception e) {

        }
        return count;
    }

    /**
     * @author Administrator
     * @功能:获取某员工的信息
     */
    public EmployeesEntity selectForEmployee(String[] selectionArgs, String selection) {
        String table = "employees";
        String groupBy = null;
        String having = null;
        String orderBy = null;
        for (String e : selectionArgs) {
            Log.i("todo", "e=" + e);
        }
        EmployeesEntity model = null;
        if (checkDbObject()) return model;
        Cursor cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);

        Log.i("todo", "e数据为空");
        while (cursor.moveToNext()) {
            Log.i("todo", "cursor.moveToNext()");
            model = new EmployeesEntity();
            model.setEM_CODE(cursor.getString(cursor.getColumnIndex("em_code")));
            model.setEm_defaultorid(cursor.getInt(cursor.getColumnIndex("em_defaultorid")));
            model.setEM_DEFAULTORNAME(cursor.getString(cursor.getColumnIndex("em_defaultorname")));
            model.setEM_DEPART(cursor.getString(cursor.getColumnIndex("em_depart")));
            model.setFLAG(cursor.getString(cursor.getColumnIndex("em_flag")));
            model.setEM_ID(cursor.getInt(cursor.getColumnIndex("em_id")));
            model.setEM_MOBILE(cursor.getString(cursor.getColumnIndex("em_mobile")));
            model.setEM_NAME(cursor.getString(cursor.getColumnIndex("em_name")));
            model.setEM_POSITION(cursor.getString(cursor.getColumnIndex("em_position")));
            model.setEM_UU(cursor.getString(cursor.getColumnIndex("em_uu")));
            model.setWHICHSYS(cursor.getString(cursor.getColumnIndex("whichsys")));
            model.setEm_IMID(Integer.valueOf(cursor.getString(cursor.getColumnIndex("em_imid")) == null ? "0" : cursor.getString(cursor.getColumnIndex("em_imid"))));
        }
        cursor.close();
        Log.i("todo", "cursor.moveToNext()||||||");
        return model;
    }

    /**************************************************************************
     * ************* 未订阅表AllSubs操作  ***************************************
     *************************************************************************/
  /*  public void saveToAllSubs(SubscriptionNumber subscriptionNumber) {
        String saveSql = "replace into AllSubs(subs_id,subs_title,subs_kind,subs_status,subs_type,subs_master,subs_username) values (?,?,?,?,?,?,?)";
        SQLiteStatement sqLiteStatement = db.compileStatement(saveSql);
        sqLiteStatement.bindLong(1, subscriptionNumber.getId());
        sqLiteStatement.bindString(2, subscriptionNumber.getTitle());
        sqLiteStatement.bindString(3, subscriptionNumber.getKind());
        sqLiteStatement.bindLong(4, subscriptionNumber.getStatus());
        sqLiteStatement.bindString(5, subscriptionNumber.getType());
        sqLiteStatement.bindString(6, subscriptionNumber.getMaster());
        sqLiteStatement.bindString(7, subscriptionNumber.getUsername());
        sqLiteStatement.bindLong(8, subscriptionNumber.getRemoved());
        sqLiteStatement.execute();
    }*/
    public void saveListToAllSubs(List<SubscriptionNumber> subscriptionNumbers) {
        String saveSql = "replace into AllSubs(subs_id,subs_title,subs_kind,subs_status,subs_type,subs_master,subs_username,subs_removed,subs_img) values (?,?,?,?,?,?,?,?,?)";
        if (checkDbObject()) return;
        try {
            SQLiteStatement sqLiteStatement = db.compileStatement(saveSql);
            for (int i = 0; i < subscriptionNumbers.size(); i++) {
                SubscriptionNumber subscriptionNumber = subscriptionNumbers.get(i);
                sqLiteStatement.bindLong(1, subscriptionNumber.getId());
                sqLiteStatement.bindString(2, subscriptionNumber.getTitle());
                sqLiteStatement.bindString(3, subscriptionNumber.getKind());
                sqLiteStatement.bindLong(4, subscriptionNumber.getStatus());
                sqLiteStatement.bindString(5, subscriptionNumber.getType());
                sqLiteStatement.bindString(6, subscriptionNumber.getMaster());
                sqLiteStatement.bindString(7, subscriptionNumber.getUsername());
                sqLiteStatement.bindLong(8, subscriptionNumber.getRemoved());
                sqLiteStatement.bindBlob(9, subscriptionNumber.getImg());

                sqLiteStatement.execute();
            }
        } catch (Exception e) {

        }

    }

    public List<SubscriptionNumber> queryFromAllSubs(String[] selectionArgs, String selection) {
        List<SubscriptionNumber> subscriptionNumbers = new ArrayList<SubscriptionNumber>();
        String table = "AllSubs";
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if (checkDbObject()) {
            return subscriptionNumbers;
        }
        try {
            Cursor cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
            while (cursor.moveToNext()) {
                SubscriptionNumber subscriptionNumber = new SubscriptionNumber();
                subscriptionNumber.setId(cursor.getInt(cursor.getColumnIndex("subs_id")));
                subscriptionNumber.setTitle(cursor.getString(cursor.getColumnIndex("subs_title")));
                subscriptionNumber.setKind(cursor.getString(cursor.getColumnIndex("subs_kind")));
                subscriptionNumber.setStatus(cursor.getInt(cursor.getColumnIndex("subs_status")));
                subscriptionNumber.setType(cursor.getString(cursor.getColumnIndex("subs_type")));
                subscriptionNumber.setMaster(cursor.getString(cursor.getColumnIndex("subs_master")));
                subscriptionNumber.setUsername(cursor.getString(cursor.getColumnIndex("subs_username")));
                subscriptionNumber.setRemoved(cursor.getInt(cursor.getColumnIndex("subs_removed")));
                subscriptionNumber.setImg(cursor.getBlob(cursor.getColumnIndex("subs_img")));
                subscriptionNumbers.add(subscriptionNumber);
            }
            cursor.close();

        } catch (Exception e) {

        }

        return subscriptionNumbers;
    }


    public void updateAllSubs(SubscriptionNumber subscriptionNumber) {
        String sql = "update AllSubs"
                + " set subs_status=?, subs_removed=? "
                + " where subs_id=? and subs_master=? and subs_username=? ;";
        if (checkDbObject()) {
            return;
        }
        try {
            SQLiteStatement sta = db.compileStatement(sql);
            sta.bindLong(1, subscriptionNumber.getStatus());
            sta.bindLong(2, subscriptionNumber.getRemoved());
            sta.bindLong(3, subscriptionNumber.getId());
            sta.bindString(4, subscriptionNumber.getMaster());
            sta.bindString(5, subscriptionNumber.getUsername());
            sta.executeInsert();
        } catch (Exception e) {

        }

    }

    public void updateListAllSubs(List<SubscriptionNumber> subscriptionNumbers) {
        String sql = "update AllSubs"
                + " set subs_status=?, subs_removed=? "
                + " where subs_id=? and subs_master=? and subs_username=? ;";
        if (checkDbObject()) {
            return;
        }
        try {
            SQLiteStatement sta = db.compileStatement(sql);

            for (int i = 0; i < subscriptionNumbers.size(); i++) {
                SubscriptionNumber subscriptionNumber = subscriptionNumbers.get(i);
                sta.bindLong(1, subscriptionNumber.getStatus());
                sta.bindLong(2, subscriptionNumber.getRemoved());
                sta.bindLong(3, subscriptionNumber.getId());
                sta.bindString(4, subscriptionNumber.getMaster());
                sta.bindString(5, subscriptionNumber.getUsername());
                sta.executeInsert();
            }
        } catch (Exception e) {

        }

    }

  /*  public void deleteFromAllSubs(SubscriptionNumber subscriptionNumber) {
        String table = "AllSubs";
        String[] whereArgs = {String.valueOf(subscriptionNumber.getId()), subscriptionNumber.getMaster(), subscriptionNumber.getUsername()};
        String whereClause = "subs_id=? and subs_master=? and subs_username=? ";
        db.delete(table, whereClause, whereArgs);
    }*/

 /*   public void deleteListFromAllSubs(List<SubscriptionNumber> subscriptionNumbers){
        for (int i = 0; i < subscriptionNumbers.size(); i++) {
            SubscriptionNumber subscriptionNumber = subscriptionNumbers.get(i);
            String table = "AllSubs";
            String[] whereArgs = {String.valueOf(subscriptionNumber.getId()), subscriptionNumber.getMaster(), subscriptionNumber.getUsername()};
            String whereClause = "subs_id=? and subs_master=? and subs_username=? ";
            db.delete(table, whereClause, whereArgs);
        }
    }*/

    //删除当前账号当前账套的所有订阅
    public void deleteMasterAllSubs(String master, String username) {
        String table = "AllSubs";
        String[] whereArgs = {master, username};
        String whereClause = "subs_master=? and subs_username=? ";
        if (checkDbObject()) {
            return;
        }
        try {
            db.delete(table, whereClause, whereArgs);
        } catch (Exception e) {

        }
    }

    /**************************************************************************
     * ************* 已订阅表MySubs操作  ***************************************
     *************************************************************************/
    public void saveToMySubs(PersonalSubscriptionBean personalSubscriptionBean) {
        String saveSql = "replace into MySubs(subs_id,subs_title,subs_kind,subs_type,subs_applied,subs_master,subs_username,subs_img) values (?,?,?,?,?,?,?,?)";
        try {
            SQLiteStatement sqLiteStatement = db.compileStatement(saveSql);
            sqLiteStatement.bindLong(1, personalSubscriptionBean.getNUM_ID());
            sqLiteStatement.bindString(2, personalSubscriptionBean.getTITLE());
            sqLiteStatement.bindString(3, personalSubscriptionBean.getKIND());
            sqLiteStatement.bindString(4, personalSubscriptionBean.getTYPE());
            sqLiteStatement.bindLong(5, personalSubscriptionBean.getISAPPLED());
            sqLiteStatement.bindString(6, personalSubscriptionBean.getMASTER());
            sqLiteStatement.bindString(7, personalSubscriptionBean.getUSERNAME());
            sqLiteStatement.bindBlob(8, personalSubscriptionBean.getIMG());
            sqLiteStatement.execute();
        }catch (Exception e){

        }

    }

    public void saveListToMySubs(List<PersonalSubscriptionBean> personalSubscriptionBeans) {
        String saveSql = "replace into MySubs(subs_id,subs_title,subs_kind,subs_type,subs_applied,subs_master,subs_username,subs_img) values (?,?,?,?,?,?,?,?)";
        if (checkDbObject()){
            return;
        }
        try {
            SQLiteStatement sqLiteStatement = db.compileStatement(saveSql);
            for (int i = 0; i < personalSubscriptionBeans.size(); i++) {
                PersonalSubscriptionBean personalSubscriptionBean = personalSubscriptionBeans.get(i);
                sqLiteStatement.bindLong(1, personalSubscriptionBean.getNUM_ID());
                sqLiteStatement.bindString(2, personalSubscriptionBean.getTITLE());
                sqLiteStatement.bindString(3, personalSubscriptionBean.getKIND());
                sqLiteStatement.bindString(4, personalSubscriptionBean.getTYPE());
                sqLiteStatement.bindLong(5, personalSubscriptionBean.getISAPPLED());
                sqLiteStatement.bindString(6, personalSubscriptionBean.getMASTER());
                sqLiteStatement.bindString(7, personalSubscriptionBean.getUSERNAME());
                sqLiteStatement.bindBlob(8, personalSubscriptionBean.getIMG());
                sqLiteStatement.execute();
            }
        }catch (Exception e){

        }

    }

    public List<PersonalSubscriptionBean> queryFromMySubs(String[] selectionArgs, String selection) {
        List<PersonalSubscriptionBean> personalSubscriptionBeans = new ArrayList<PersonalSubscriptionBean>();
        String table = "MySubs";
        String groupBy = null;
        String having = null;
        String orderBy = null;
        if (checkDbObject()) {
            return personalSubscriptionBeans;
        }
        try {
            Cursor cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
            while (cursor.moveToNext()) {
                PersonalSubscriptionBean personalSubscriptionBean = new PersonalSubscriptionBean();
                personalSubscriptionBean.setNUM_ID(cursor.getInt(cursor.getColumnIndex("subs_id")));
                personalSubscriptionBean.setTITLE(cursor.getString(cursor.getColumnIndex("subs_title")));
                personalSubscriptionBean.setKIND(cursor.getString(cursor.getColumnIndex("subs_kind")));
                personalSubscriptionBean.setTYPE(cursor.getString(cursor.getColumnIndex("subs_type")));
                personalSubscriptionBean.setISAPPLED(cursor.getInt(cursor.getColumnIndex("subs_applied")));
                personalSubscriptionBean.setMASTER(cursor.getString(cursor.getColumnIndex("subs_master")));
                personalSubscriptionBean.setUSERNAME(cursor.getString(cursor.getColumnIndex("subs_username")));
                personalSubscriptionBean.setIMG(cursor.getBlob(cursor.getColumnIndex("subs_img")));
                personalSubscriptionBeans.add(personalSubscriptionBean);
            }
            cursor.close();
        }catch (Exception e){

        }

        return personalSubscriptionBeans;
    }


  /*  public void updateMySubs(PersonalSubscriptionBean personalSubscriptionBean) {
        String sql = "update MySubs"
                + " set subs_applied=?"
                + " where subs_id=? and subs_master=? and subs_username=? ;";
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindLong(1, personalSubscriptionBean.getISAPPLED());
        sta.bindLong(2, personalSubscriptionBean.getNUM_ID());
        sta.bindString(3, personalSubscriptionBean.getMASTER());
        sta.bindString(4, personalSubscriptionBean.getUSERNAME());
        sta.executeInsert();
    }*/

    public void deleteFromMySubs(PersonalSubscriptionBean personalSubscriptionBean) {
        String table = "MySubs";
        String[] whereArgs = {String.valueOf(personalSubscriptionBean.getNUM_ID()), personalSubscriptionBean.getMASTER(), personalSubscriptionBean.getUSERNAME()};
        String whereClause = "subs_id=? and subs_master=? and subs_username=? ";
        if (checkDbObject()) {
            return;
        }
        try {
            db.delete(table, whereClause, whereArgs);
        }catch (Exception e){

        }
    }

    /*public void deleteListFromMySubs(List<PersonalSubscriptionBean> personalSubscriptionBeans){
        for (int i = 0; i < personalSubscriptionBeans.size(); i++) {
            PersonalSubscriptionBean personalSubscriptionBean = personalSubscriptionBeans.get(i);
            String table = "MySubs";
            String[] whereArgs = {String.valueOf(personalSubscriptionBean.getNUM_ID()), personalSubscriptionBean.getMASTER(), personalSubscriptionBean.getUSERNAME()};
            String whereClause = "subs_id=? and subs_master=? and subs_username=? ";
            db.delete(table, whereClause, whereArgs);
        }
    }*/

    public void deleteMasterMySubs(String master, String username) {
        String table = "MySubs";
        String[] whereArgs = {master, username};
        String whereClause = "subs_master=? and subs_username=? ";
        if (checkDbObject()) {
            return;
        }
        try {
            db.delete(table, whereClause, whereArgs);
        }catch (Exception e){

        }
    }


    /**
     * close database
     */
    public void closeDB() {
        DatabaseManager.getInstance().closeDatabase();
    }
}
