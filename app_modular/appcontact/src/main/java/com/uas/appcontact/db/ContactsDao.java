package com.uas.appcontact.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.db.DatabaseManager;
import com.core.utils.CommonUtil;
import com.uas.appcontact.model.contacts.ContactsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arison on 2017/7/18.
 */

public class ContactsDao {
    
    private static  ContactsDao instance;
    public static ContactsDao getInstance() {
        if (instance == null) {
            synchronized (ContactsDao.class) {
                if (instance == null) {
                    instance = new ContactsDao();
                }
            }
        }
        return instance;
    }
    
    
    public synchronized void save(List<ContactsModel> models){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String sql="insert into tbl_contacts (tf_name,tf_whichSys,tf_company,tf_phone,tf_email,tf_type,tf_imId,tf_ownerId) values (?,?,?,?,?,?,?,?);";
        if (ListUtils.isEmpty(models))return;
        SQLiteStatement sta = db.compileStatement(sql);
        db.beginTransaction();
        for (int i=0;i<models.size();i++){
            ContactsModel  model=models.get(i);
            sta.bindString(1, getStringNotNull(model.getName()));
            sta.bindString(2, getStringNotNull(model.getWhichsys()));
            sta.bindString(3, getStringNotNull(model.getCompany()));
            sta.bindString(4, getStringNotNull(model.getPhone()));
            sta.bindString(5, getStringNotNull(model.getEmail()));
            sta.bindLong(6, model.getType());
            sta.bindString(7, getStringNotNull(model.getImid()));
            sta.bindString(8,getStringNotNull(model.getOwnerId()));
            sta.executeInsert();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        DatabaseManager.getInstance().closeDatabase();
    }
    

    //select * from tbl_contacts a where id=(select min(id) from tbl_contacts where tf_phone=a.tf_phone) and (tf_phone<>'未填写' and tf_phone<>'') and tf_ownerId='' and (tf_whichSys="" or tf_whichSys="" )
    /**
      * @desc:UU+企业架构+本地通讯录  已去重
      * @author：Arison on 2017/8/1
      */
    public List<ContactsModel> find(){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String master= CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_master");
        if (StringUtil.isEmpty(master)) {
            master="";
        }
        String ownerId=MyApplication.getInstance().mLoginUser.getUserId();
        LogUtil.d("Test","master:"+master+" ownerId:"+ownerId);
        List<ContactsModel> models=new ArrayList<>();
//        Cursor cursor =db.rawQuery("select * from tbl_contacts a where id=" +
//                "(select min(id) from tbl_contacts where tf_phone=a.tf_phone and tf_ownerId=? ) and (tf_phone<> ? and tf_phone<> ?) " +
//                "   ", new String[]{ownerId,"未填写"," "});

        String sql="select * from tbl_contacts where id  in (select id from (select tf_phone,max(id) id from tbl_contacts  where (tf_phone<> ? and tf_phone<> ?) group by tf_phone)) and tf_ownerId=?";
//        Cursor cursor =db.rawQuery("select * from tbl_contacts a where id=" +
//                "(select min(id) from tbl_contacts where tf_phone=a.tf_phone and tf_ownerId=? ) and (tf_phone<> ? and tf_phone<> ?) " +
//                "  and (tf_whichSys=? or tf_whichSys=? ) ", new String[]{ownerId,"未填写"," ",master,""});
        Cursor cursor =db.rawQuery(sql,new String[]{"未填写"," ",ownerId});
        while (cursor.moveToNext()) {
            ContactsModel model=new ContactsModel();
            model.setName(cursor.getString(cursor.getColumnIndex("tf_name")));
            model.setWhichsys(cursor.getString(cursor.getColumnIndex("tf_whichSys")));
            model.setCompany(cursor.getString(cursor.getColumnIndex("tf_company")));
            model.setPhone(cursor.getString(cursor.getColumnIndex("tf_phone")));
            model.setEmail(cursor.getString(cursor.getColumnIndex("tf_email")));
            model.setType(cursor.getInt(cursor.getColumnIndex("tf_type")));
            model.setOwnerId(cursor.getString(cursor.getColumnIndex("tf_ownerId")));
            model.setImid(cursor.getString(cursor.getColumnIndex("tf_imId")));
            models.add(model);
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return models;
    }

    /**
      * @desc:UU好友+企业架构人员  已去重
      * @author：Arison on 2017/8/1
      */
    public  List<ContactsModel> findByShare(){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String master= CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_master");
        if (StringUtil.isEmpty(master)) {
            master="";
        }
        String ownerId=MyApplication.getInstance().mLoginUser.getUserId();
        LogUtil.d("Test","master:"+master+" ownerId:"+ownerId);
        List<ContactsModel> models=new ArrayList<>();
        Cursor cursor =db.rawQuery("select * from tbl_contacts a where id=" +
                "(select min(id) from tbl_contacts where tf_phone=a.tf_phone and tf_ownerId=? ) and (tf_phone<> ? and tf_phone<> ?) " +
                "  and (tf_whichSys=? or tf_whichSys=? ) and tf_type<> ? ", new String[]{ownerId,"未填写"," ",master,"","3"});
        while (cursor.moveToNext()) {
            ContactsModel model=new ContactsModel();
            model.setName(cursor.getString(cursor.getColumnIndex("tf_name")));
            model.setCompany(cursor.getString(cursor.getColumnIndex("tf_company")));
            model.setWhichsys(cursor.getString(cursor.getColumnIndex("tf_whichSys")));
            model.setPhone(cursor.getString(cursor.getColumnIndex("tf_phone")));
            model.setEmail(cursor.getString(cursor.getColumnIndex("tf_email")));
            model.setType(cursor.getInt(cursor.getColumnIndex("tf_type")));
            model.setImid(cursor.getString(cursor.getColumnIndex("tf_imId")));
            model.setOwnerId(cursor.getString(cursor.getColumnIndex("tf_ownerId")));
            models.add(model);
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return models;
    }

    public  List<ContactsModel> find(String key){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String master= CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_master");
        if (StringUtil.isEmpty(master)) {
            master="";
        }
        List<ContactsModel> models=new ArrayList<>();
        String ownerId=MyApplication.getInstance().mLoginUser.getUserId();
        if (StringUtil.isEmpty(key)){
            key="%%";
        }else{
            key="%"+key+"%";
        }
        String sql="(select min(id) from tbl_contacts where tf_phone=a.tf_phone) and (tf_phone<>'未填写' and tf_phone<>'')";
        Cursor cursor =db.rawQuery("select * from tbl_contacts a where id=" +
                "(select min(id) from tbl_contacts where tf_phone=a.tf_phone and tf_ownerId=? ) and (tf_phone<> ? and tf_phone<> ?) " +
                "and (tf_name like ? or tf_phone like ? ) and (tf_whichSys=? or tf_whichSys=? ) ", 
                new String[]{ownerId,"未填写"," ",key,key,master,""});
        while (cursor.moveToNext()) {
            ContactsModel model=new ContactsModel();
            model.setName(cursor.getString(cursor.getColumnIndex("tf_name")));
            model.setCompany(cursor.getString(cursor.getColumnIndex("tf_company")));
            model.setWhichsys(cursor.getString(cursor.getColumnIndex("tf_whichSys")));
            model.setPhone(cursor.getString(cursor.getColumnIndex("tf_phone")));
            model.setEmail(cursor.getString(cursor.getColumnIndex("tf_email")));
            model.setType(cursor.getInt(cursor.getColumnIndex("tf_type")));
            model.setImid(cursor.getString(cursor.getColumnIndex("tf_imId")));
            model.setOwnerId(cursor.getString(cursor.getColumnIndex("tf_ownerId")));
            models.add(model);
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return models;
    }
    
    //uPDATE tbl_contacts set tf_type=1 where tf_ownerId='100263' and tf_whichSys='USOFTSYS' and tf_imId='109791'
    public  void update(ContactsModel model){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String sql = "update tbl_contacts"
                + " set tf_type=?"
                + " where tf_ownerId=? and tf_whichSys=? and tf_imId=?;";
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindLong(1, model.getType());
        sta.bindString(2, model.getOwnerId());
        sta.bindString(3, model.getWhichsys());
        sta.bindString(4, model.getImid());
        long row=  sta.executeInsert();
        DatabaseManager.getInstance().closeDatabase();
    }

    //针对本地通讯录  model的字段为空
    public  void update(ContactsModel model,String imId){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String sql = "update tbl_contacts"
                + " set tf_type=? , tf_imId=? "
                + " where tf_ownerId=? and tf_whichSys=? and tf_imId=? and tf_phone=?;";
        SQLiteStatement sta = db.compileStatement(sql);
        sta.bindLong(1, model.getType());
        sta.bindString(2, model.getImid());
        sta.bindString(3, model.getOwnerId());
        sta.bindString(4, model.getWhichsys());
        sta.bindString(5, imId);//0  
        sta.bindString(6, model.getPhone());
        long row=  sta.executeInsert();
        DatabaseManager.getInstance().closeDatabase();
    }
    
    public  void delete(){
        SQLiteDatabase db=  DatabaseManager.getInstance().openDatabase();
        String table = "tbl_contacts";
        String whereClause = "tf_type!=2";
        int row= db.delete(table, whereClause, null);
        DatabaseManager.getInstance().closeDatabase();
    }

    private String getStringNotNull(String chche) {
        return StringUtil.isEmpty(chche) ? "" : chche;
    }
}
