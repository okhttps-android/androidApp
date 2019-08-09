package com.common.file;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.common.LogUtil;

import java.io.File;

/**
 * 下载类
 *
 * @author RaoMeng
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("DefaultLocale")
public class DownloadUtil {
    static String FilePath;
    static BroadcastReceiver receiver;

    /**
     * @param context 上下文场景
     * @param url     下载文件的地址
     * @param path    SD卡保存的路径 如："/MyDownload",自动在SD下创建该目录。
     */
    public static void DownloadFile(Context context, String url, String path) {
 
         /*
         * 注册广播监听下载完成
         */
        receiver = new DownloadCompleteReceiver();
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        /**
         * 先检测SD卡是否存在
         */
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
         /*
         * 创建文件夹
         */
        String file = Environment.getExternalStorageDirectory().getPath() + path;
        File files = new File(file);
        if (files == null || !files.exists()) {
            files.mkdir();
        }
         /*
         * 截取文件名
         */
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        try {
            fileName=fileName.split("&")[3].split("=")[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        //fileName ="UU.apk";
         /*
         *系统下载服务类
         */
        DownloadManager downManager = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);
        DownloadManager.Request down = new DownloadManager.Request(Uri.parse(url));
        down.setShowRunningNotification(true);
        //在通知栏显示
        down.setVisibleInDownloadsUi(true);
        //输出目录
        down.setDestinationInExternalPublicDir(path + "/", fileName);
        //文件路径
        FilePath = file + "/" + fileName;
        LogUtil.d("文件下载路径："+FilePath);
        //加入下载队列执行
        downManager.enqueue(down);
    }

    public static void unregisterReceiver(Context context) {
        context.unregisterReceiver(receiver);
    }

    ;

    /**
     * 监听下载完成
     *
     * @author Administrator
     */
    public static class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //获取文件路径
                File files = new File(FilePath);
                //打开这个文件
                Intent openFile = FileUtils.getFileIntent(files);
                context.startActivity(openFile);
            }
        }
    }


}