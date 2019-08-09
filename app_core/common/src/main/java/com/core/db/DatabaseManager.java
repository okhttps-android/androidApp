package com.core.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.core.app.MyApplication;

import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private static SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }
   
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            initializeInstance(DBOpenHelper.getInstance(MyApplication.getInstance()));
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1 ) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if (mOpenCounter.get()>=1) {
            if (mOpenCounter.decrementAndGet() == 0) {
                mDatabase.close();
            }
        }
    }
}