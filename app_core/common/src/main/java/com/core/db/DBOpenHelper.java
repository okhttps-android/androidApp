package com.core.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.common.LogUtil;
import com.core.app.Constants;

/**
 * @author :LiuJie 时间: 2015年3月10日 上午10:29:40
 * @注释:建立数据库
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    //TODO  上一个发布版本是89
    private static final int DATABASE_VERSION = 90;
    public static String dataBaseName = "erp.db";
    public Context context;

    private static DBOpenHelper mInstance;

    public static DBOpenHelper getInstance(Context ct) {
        if (mInstance == null) {
            synchronized (DBOpenHelper.class) {
                if (mInstance == null) {
                    mInstance = new DBOpenHelper(ct);
                }
            }
        }
        return mInstance;
    }

    private DBOpenHelper(Context ct) {
        super(ct, dataBaseName, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.SQL_HRORGS_CREATETABLE);
        db.execSQL(Constants.SQL_EMPLOYEES_CREATETABLE);
        db.execSQL(Constants.SQL_empdate_CREATETABLE);
        db.execSQL(Constants.SQL_B2B_MSG);
        db.execSQL(Constants.SUB_MESSAGE);//
        db.execSQL(Constants.TABLE_ALL_SUBSCRIPTION);//未订阅列表
        db.execSQL(Constants.TABLE_MY_SUBSCRIPTION);//已订阅列表
        try {
            //发布版本注意
            db.execSQL(Constants.TABLE_CONTANCTS);//通讯录新表-合并数据源
            db.execSQL(Constants.WORKTIBLE);//创建OA签到记录表
            db.execSQL(Constants.TABLE_MISSION);//外勤计划
            db.execSQL(Constants.WORK_LOCATION);//
            db.execSQL(Constants.EM_ERPNEWS);//
            db.execSQL(Constants.CREATE_BANNER);//
            db.execSQL(Constants.AUTO_LOG);   //监控日志表
//            db.execSQL(Constants.TRACK_POINT); //运行轨迹，实时定位点记录表
            db.execSQL(Constants.TABLE_HISTORICAL_RECORD);//数据查询历史记录表
            db.execSQL(Constants.TABLE_UUHELPER_RECORD);//UU助手
            db.execSQL(Constants.TABLE_TOPCONTACTS_RECORD);//常用联系人
            db.execSQL(Constants.STEPRANKING_FIRST); //用于存贮每天UU运动排名第一相关的数据表
        } catch (Exception e) {

        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.d("onUpgrade()");
        deleteTable(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.d("onDowngrade()");
        deleteTable(db);
        onCreate(db);
    }

    private void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS HRORGS");
        db.execSQL("DROP TABLE IF EXISTS EMPLOYEES");
        db.execSQL("DROP TABLE IF EXISTS empdate");
        db.execSQL("DROP TABLE IF EXISTS B2BMSG");
        db.execSQL("DROP TABLE IF EXISTS Signin");
        db.execSQL("DROP TABLE IF EXISTS SubscriptionMessage");
        db.execSQL("DROP TABLE IF EXISTS submessage");
        db.execSQL("DROP TABLE IF EXISTS AllSubs");
        db.execSQL("DROP TABLE IF EXISTS MySubs");
        db.execSQL("DROP TABLE IF EXISTS mission");
        db.execSQL("DROP TABLE IF EXISTS error_mag");
        db.execSQL("DROP TABLE IF EXISTS Signin");
        db.execSQL("DROP TABLE IF EXISTS signing");
        db.execSQL("DROP TABLE IF EXISTS workdata");
        db.execSQL("DROP TABLE IF EXISTS work_location");
        db.execSQL("DROP TABLE IF EXISTS em_erpnews");
        db.execSQL("DROP TABLE IF EXISTS step");
        db.execSQL("DROP TABLE IF EXISTS SignAutoLog");
        db.execSQL("DROP TABLE IF EXISTS tbl_contacts");
//        db.execSQL("DROP TABLE IF EXISTS trackPoint");
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.HistoricalRecordTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.UUHelperTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.TopContactsTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS StepRankingFirst");
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db); // 每次打开数据库之后首先被执行
    }
}
