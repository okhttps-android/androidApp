package com.xzjmyk.pm.activity.util.im.downloader;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

/**
 * 该程序音频和视频的下载器,仿照ImageLoader的超级简化版
 */
public class Downloader {

    private String mFileDir;
    private DownloaderEngine mEngine;
    private Handler mMainHandler;

    private Downloader() {
        mEngine = new DownloaderEngine();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    private static Downloader instance;

    public static Downloader getInstance() {
        if (instance == null) {
            synchronized (Downloader.class) {
                if (instance == null) {
                    instance = new Downloader();
                }
            }
        }
        return instance;
    }

    public synchronized void init(String fileDir) {
        mFileDir = fileDir;
    }

    public void addDownload(String uri) {
        addDownload(uri, new ViewAware(null), null, null);
    }

    public void addDownload(String uri, DownloadListener listener) {
        addDownload(uri, new ViewAware(null), listener, null);
    }

    public void addDownload(String uri, DownloadProgressListener listener) {
        addDownload(uri, new ViewAware(null), null, listener);
    }

    public void addDownload(String uri, DownloadListener listener, DownloadProgressListener listener2) {
        addDownload(uri, new ViewAware(null), listener, listener2);
    }

    public void addDownload(String uri, ProgressBar progressBar, DownloadListener listener) {
        addDownload(uri, new ViewAware(progressBar), listener, null);
    }

    public void addDownload(String uri, ProgressBar progressBar, DownloadListener listener, DownloadProgressListener listener2) {
        addDownload(uri, new ViewAware(progressBar), listener, listener2);
    }

    public void addDownload(String uri, ViewAware viewAware, DownloadListener listener, DownloadProgressListener listener2) {
        if (viewAware == null) {
            throw new IllegalArgumentException();
        }
        if (listener == null) {
            listener = mEmptyListener;
        }

        if (TextUtils.isEmpty(uri)) {
            mEngine.cancelDisplayTaskFor(viewAware);
            listener.onFailed(uri, new FailReason(FailReason.FailType.URI_EMPTY, new NullPointerException()), viewAware.getWrappedView());
            return;
        }

        mEngine.prepareDisplayTaskFor(viewAware, uri);

        listener.onStarted(uri, viewAware.getWrappedView());

        // 从本地查找
        File localFile = getFile(uri);
        if (localFile.exists()) {
            listener.onComplete(uri, localFile.getPath(), viewAware.getWrappedView());
            return;
        }

        // 开始下载
        DownLoadingInfo downLoadingInfo = new DownLoadingInfo(uri, viewAware, mEngine.getLockForUri(uri), listener, listener2);
        DownloadTask task = new DownloadTask(mEngine, downLoadingInfo, mMainHandler);

        mEngine.submit(task);
    }

    private DownloadListener mEmptyListener = new DownloadListener() {

        @Override
        public void onFailed(String uri, FailReason failReason, View view) {
        }

        @Override
        public void onCancelled(String uri, View view) {
        }

        @Override
        public void onStarted(String uri, View view) {
        }

        @Override
        public void onComplete(String uri, String filePath, View view) {
        }
    };

    public String getDir() {
        return mFileDir;
    }

    public File getFile(String uri) {
        String name = fileNameGenerator(uri);
        File file = new File(mFileDir, name);
        return file;
    }

    public File getTempFile(String uri) {
        String name = fileNameGenerator(uri) + ".temp";
        File file = new File(mFileDir, name);
        return file;
    }

    public String fileNameGenerator(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        int lastIndex = url.lastIndexOf("/");
        if (lastIndex == -1) {
            return url;
        }
        return url.substring(lastIndex, url.length());
    }

    public void pause(View view) {
        mEngine.pause();
    }

    public void resume(View view) {
        mEngine.resume();
    }

    public void stop(View view) {
        mEngine.stop();
    }
}
