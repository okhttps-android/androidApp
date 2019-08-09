package com.modular.apputils.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.common.LogUtil;
import com.core.app.R;
import com.core.app.MyApplication;
import com.modular.apputils.activity.PDFViewActivity;

import java.io.File;


/**
 * Created by FANGLH on 2017/7/11.
 * Function：如打开视频文件、word、Excel、pdf等格式
 * HOW : eg 在外界面直接调用方法 openCommonFils(Context ct,File currentPath)
 * <p>
 * <p>
 * 关于实现附件文件点击打开显示调用工具逻辑
 * 1、在values目录下定义后缀数组文件fileendings，包括Image、Audio、Video、Package、WebText、Text、Excel、PPT、PDF等
 * 2、实现用于检查要打开的文件的后缀是否在遍历后缀数组中，然后再进行下一句逻辑
 * 3、定义OpenFilesUtils工具类（目前可实现11中大文件类型），只需传输File参数即可，然后通过返回的Intent打开文件
 * 4、通过调用OpenFiles类返回的Intent，打开相应的文件，方法都写在了公用方法了，具体打开某一附件只需要调用openCommonFils（）方法就可以了，参数是上下文+File文件对象
 */

public class OpenFilesUtils {

    //定义用于检查要打开的文件的后缀是否在遍历后缀数组中，调用下面的方法之前必须先用这个方法判断
    public static boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd))
                return true;
        }
        return false;
    }

    //android获取一个用于打开HTML文件的intent
    public static Intent getHtmlFileIntent(File file) {
        Uri uri = Uri.parse(file.toString()).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(file.toString()).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    //android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    //android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/pdf");


        return intent;
    }

    private static Intent openFileByTBS(Context context, String filepath) {
        return new Intent(context, PDFViewActivity.class)
                .putExtra("filepath", filepath);
    }

    //android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "text/plain");
        return intent;
    }

    //android获取一个用于打开音频文件的intent
    public static Intent getAudioFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    //android获取一个用于打开视频文件的intent
    public static Intent getVideoFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "video/*");
        return intent;
    }


    //android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }


    //android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    //android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    //android获取一个用于打开PPT文件的intent
    public static Intent getPPTFileIntent(File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //android获取一个用于打开apk文件的intent
    public static Intent getApkFileIntent(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        return intent;
    }

    /**
     * 直接调用就可以了
     *
     * @param ct
     * @param currentPath
     */
    public static void openCommonFils(Context ct, File currentPath) {
        try {
            LogUtil.i("currentPath=" + currentPath.getPath());
            LogUtil.i("getName=" + currentPath.getName());
            if (currentPath != null && currentPath.isFile()) {
                String fileName = currentPath.getName();
                LogUtil.i("fileName=" + fileName);
                Intent intent;
                if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingImage))) {
                    intent = getImageFileIntent(currentPath);
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingWebText))) {
                    intent = getHtmlFileIntent(currentPath);
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingPackage))) {
                    intent = getApkFileIntent(currentPath);
                    ct.startActivity(intent);

                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingAudio))) {
                    intent = getAudioFileIntent(currentPath);
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingVideo))) {
                    intent = getVideoFileIntent(currentPath);
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingText))) {
                    LogUtil.i("fileEndingTextfileEndingTextfileEndingText" );
                    intent = getTextFileIntent(currentPath);
                    LogUtil.i("intent is null"+(intent==null) );
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingPdf))) {
                    intent = openFileByTBS(ct, currentPath.getPath());
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingWord))) {
                    intent = getWordFileIntent(currentPath);
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingExcel))) {
                    intent = getExcelFileIntent(currentPath);
                    ct.startActivity(intent);
                } else if (checkEndsWithInStringArray(fileName, ct.getResources().
                        getStringArray(R.array.fileEndingPPT))) {
                    intent = getPPTFileIntent(currentPath);
                    ct.startActivity(intent);
                } else {
                    Toast.makeText(ct, "无法打开，请安装相应的软件！", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ct, "对不起，这不是文件！", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(ct, "无法打开，请安装相应的阅读应用后打开文件！", Toast.LENGTH_LONG).show();
        }
    }

    /***
     * 下载附件方法，这里需要开启线程，用服务调用或者调用广播都可以
     * @param ct
     * @param downloadUrl
     * @param fileName
     */
    public static String downLoadFile(Context ct, String downloadUrl, String fileName) {
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("uu/download/", fileName);
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) ct.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);

        //返回后再調用openCommonFils
        return "uu/download/" + fileName;
    }

    private DownloadManager downloadManager;
    private BroadcastReceiver dreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };


    //暫時不用
    public static void downLoadFile2(Context ct, String downloadUrl, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setAllowedOverRoaming(false);
        //漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(downloadUrl));
        request.setMimeType(mimeString);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir("uu/download/", fileName);
        //request.setDestinationInExternalFilesDir(),也可以自己制定下载路径

        //将下载请求加入下载队列
        DownloadManager downloadManager = (DownloadManager) ct.getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法

        //注册广播接收者，监听下载状态
//        ct.registerReceiver(ct.dreceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //暫時不用
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    showFileToast(">>>浏览暂停");
                case DownloadManager.STATUS_PENDING:
                    showFileToast(">>>浏览出现延迟");
                case DownloadManager.STATUS_RUNNING:
                    showFileToast(">>>正在生成附件浏览");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    showFileToast(">>>正在打开文件");
                    //下载完成安装克定义自动执行打开文件操作
                    break;
                case DownloadManager.STATUS_FAILED:
                    showFileToast(">>>浏览失败");
                    break;
            }
        }
    }


    public static void showFileToast(String word) {
        Toast.makeText(MyApplication.getInstance(), word, Toast.LENGTH_LONG).show();
    }


}
