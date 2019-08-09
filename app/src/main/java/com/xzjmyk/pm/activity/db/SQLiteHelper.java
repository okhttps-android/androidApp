package com.xzjmyk.pm.activity.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.core.model.Friend;
import com.core.model.User;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.xzjmyk.pm.activity.R;
import com.core.model.AuthCode;
import com.core.model.Company;

import com.core.model.MyPhoto;
import com.core.model.VideoFile;
import com.core.model.CircleMessage;
import com.core.model.NewFriendMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;


public class SQLiteHelper extends OrmLiteSqliteOpenHelper {
    public static final String DATABASE_NAME = "shiku.db";
    private static final int DATABASE_VERSION = 44;

    // public static final String DATABASE_PATH = Config.SDCARD_PATH +
    // File.separator + "shiku" + File.separator + "shiku.db";
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connSource) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connSource, int oldVersion, int newVersion) {
        try {
            dropTable(connSource);
            createTableIfNotExists(connSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void dropTable(ConnectionSource connSource) throws SQLException {
        TableUtils.dropTable(connSource, Company.class, true);
        TableUtils.dropTable(connSource, User.class, true);
        TableUtils.dropTable(connSource, Friend.class, true);
        TableUtils.dropTable(connSource, NewFriendMessage.class, true);
        TableUtils.dropTable(connSource, VideoFile.class, true);
        TableUtils.dropTable(connSource, AuthCode.class, true);
        TableUtils.dropTable(connSource, MyPhoto.class, true);
        TableUtils.dropTable(connSource, CircleMessage.class, true);
        TableUtils.dropTable(connSource, CircleMessage.class, true);
    }

    private void createTableIfNotExists(ConnectionSource connSource) throws SQLException {
        TableUtils.createTableIfNotExists(connSource, Company.class);
        TableUtils.createTableIfNotExists(connSource, User.class);
        TableUtils.createTableIfNotExists(connSource, Friend.class);
        TableUtils.createTableIfNotExists(connSource, NewFriendMessage.class);
        TableUtils.createTableIfNotExists(connSource, VideoFile.class);
        TableUtils.createTableIfNotExists(connSource, AuthCode.class);
        TableUtils.createTableIfNotExists(connSource, MyPhoto.class);
        TableUtils.createTableIfNotExists(connSource, CircleMessage.class);
        TableUtils.createTableIfNotExists(connSource, CircleMessage.class);
    }

    private void version2Update(ConnectionSource connSource) {
        try {
            TableUtils.createTableIfNotExists(connSource, Company.class);
            TableUtils.createTableIfNotExists(connSource, User.class);
            TableUtils.createTableIfNotExists(connSource, Friend.class);
            TableUtils.createTableIfNotExists(connSource, NewFriendMessage.class);
            TableUtils.createTableIfNotExists(connSource, VideoFile.class);
            TableUtils.createTableIfNotExists(connSource, AuthCode.class);
            TableUtils.createTableIfNotExists(connSource, MyPhoto.class);
            TableUtils.createTableIfNotExists(connSource, CircleMessage.class);
            TableUtils.createTableIfNotExists(connSource, CircleMessage.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果在data目录下没有该项目数据库，则拷贝数据库
     */
    public static void copyDatabaseFile(Context context) {
        File dbFile = context.getDatabasePath(SQLiteHelper.DATABASE_NAME);
        if (dbFile.exists()) {
            return;
        }
        File parentFile = dbFile.getParentFile();
        if (!parentFile.exists()) {
            try {
                parentFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InputStream in = null;
        FileOutputStream out = null;
        try {
            dbFile.createNewFile();
            in = context.getResources().openRawResource(R.raw.shiku);
            int size = in.available();
            byte buf[] = new byte[size];
            in.read(buf);
            out = new FileOutputStream(dbFile);
            out.write(buf);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
