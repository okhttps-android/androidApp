package com.common.file;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.UUID;

/**
 * im模块中使用到的关于文件保存位置工具类
 *
 */
public class CacheFileUtil {
    //文件缓存目录
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_ADUIO = 2;
    private static final int TYPE_VIDEO = 3;


    private static String getFilePath(Context context, String environment) {
        File file = context.getExternalFilesDir(environment);
        if (file != null) {
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    public static String getAppdirPath(Context context) {
        return getFilePath(context, null);
    }

    public static String getFilesdirPath(Context context) {
        return getFilePath(context, Environment.DIRECTORY_DOWNLOADS);
    }

    public static String getPicturesdirPath(Context context) {
        return getFilePath(context, Environment.DIRECTORY_PICTURES);

    }

    public static String getVideosdirPath(Context context) {
        return getFilePath(context, Environment.DIRECTORY_MOVIES);
    }

    public static String getVoicesdirPath(Context context) {
        return getFilePath(context, Environment.DIRECTORY_MUSIC);
    }


    /**
     * {@link #TYPE_IMAGE}<br/>
     * {@link #TYPE_ADUIO}<br/>
     * {@link #TYPE_VIDEO} <br/>
     *
     * @param type
     * @return
     */
    private static String getPublicFilePath(Context ct, int type) {
        String fileDir = null;
        String fileSuffix = null;
        switch (type) {
            case TYPE_ADUIO:
                fileDir = getVoicesdirPath(ct);
                fileSuffix = ".mp3";
                break;
            case TYPE_VIDEO:
                fileDir = getVideosdirPath(ct);
                fileSuffix = ".mp4";
                break;
            case TYPE_IMAGE:
                fileDir = getPicturesdirPath(ct);
                fileSuffix = ".jpg";
                break;
        }
        if (fileDir == null) {
            return null;
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return fileDir + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + fileSuffix;
    }

    /**
     * {@link #TYPE_ADUIO}<br/>
     * {@link #TYPE_VIDEO} <br/>
     *
     * @param type
     * @return
     */
    private static String getPrivateFilePath(Context ct, int type, String userId) {
        String fileDir = null;
        String fileSuffix = null;
        switch (type) {
            case TYPE_ADUIO:
                fileDir = getAppdirPath(ct) + File.separator + userId + File.separator + Environment.DIRECTORY_MUSIC;
                fileSuffix = ".mp3";
                break;
            case TYPE_VIDEO:
                fileDir = getAppdirPath(ct) + File.separator + userId + File.separator + Environment.DIRECTORY_MOVIES;
                fileSuffix = ".mp4";
                break;
        }
        if (fileDir == null) {
            return null;
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return null;
            }
        }
        return fileDir + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + fileSuffix;
    }

    public static String getRandomImageFilePath(Context ct) {
        return getPublicFilePath(ct, TYPE_IMAGE);
    }

    public static String getRandomAudioFilePath(Context ct, String userId) {
        if (!TextUtils.isEmpty(userId)) {
            return getPrivateFilePath(ct, TYPE_ADUIO, userId);
        } else {
            return getPublicFilePath(ct, TYPE_ADUIO);
        }
    }

    public static String getRandomAudioAmrFilePath(Context ct, String userId) {
        String filePath = null;
        if (!TextUtils.isEmpty(userId)) {
            filePath = getPrivateFilePath(ct, TYPE_ADUIO, userId);
        } else {
            filePath = getPublicFilePath(ct, TYPE_ADUIO);
        }
        if (!TextUtils.isEmpty(filePath)) {
            return filePath.replace(".mp3", ".amr");
        } else {
            return null;
        }
    }

    public static String getRandomVideoFilePath(Context ct, String userId) {
        if (!TextUtils.isEmpty(userId)) {
            return getPrivateFilePath(ct, TYPE_VIDEO, userId);
        } else {
            return getPublicFilePath(ct, TYPE_VIDEO);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static void createFileDir(String fileDir) {
        File fd = new File(fileDir);
        if (!fd.exists()) {
            fd.mkdirs();
        }
    }

    /**
     * @param fullName
     */
    public static void delFile(String fullName) {
        File file = new File(fullName);
        if (file.exists()) {
            if (file.isFile()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除文件夹里面的所有文件
     *
     * @param path String 文件夹路径 如 /sdcard/data/
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            System.out.println(path + tempList[i]);
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]); // 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]); // 再删除空文件夹
            }
        }
    }

    /**
     * 删除文件夹
     * <p>
     * String 文件夹路径及名称 如/sdcard/data/
     * String
     *
     * @return boolean
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            System.out.println("删除文件夹操作出错");
            e.printStackTrace();
        }
    }

}
