package com.modular.apputils.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.core.app.Constants;
import com.core.net.ProgressDownloader;
import com.core.net.ProgressResponseBody;
import com.core.utils.CommonUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/6/7 9:37
 */
public class CommonIntentService extends IntentService {
    private static final String SERVICE_NAME = "CommonIntentService";

    /**
     * action
     */
    private static final String ACTION_DOWNLOAD_SPLASH = "action_download_splash";

    /**
     * extras
     */
    private static final String EXTRA_STRING1 = "extra_string1";

    private ProgressDownloader mDownloader;

    public CommonIntentService() {
        super(SERVICE_NAME);
    }

    public static void downloadSplash(Context context, String resUrl) {
        Intent intent = new Intent(context, CommonIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_SPLASH);
        intent.putExtra(EXTRA_STRING1, resUrl);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case ACTION_DOWNLOAD_SPLASH:
                    String resUrl = intent.getStringExtra(EXTRA_STRING1);
                    if (TextUtils.isEmpty(resUrl)) {
                        initSplashDir();
                        CommonUtil.setSharedPreferences(this, Constants.CACHE.CACHE_SPLASH_URL, "");
                        return;
                    }
                    downloadSplashImp(resUrl);
                    break;
                default:
                    break;
            }
        }
    }

    private void downloadSplashImp(final String resUrl) {
        int dotIndex = resUrl.lastIndexOf(".");
        if (dotIndex >= 0) {
            String resExtension = resUrl.substring(dotIndex);
            File splashFile = new File(Constants.SPLASH_FILE_PATH, Constants.SPLASH_FILE_NAME + resExtension);
            try {
                splashFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDownloader = new ProgressDownloader(resUrl, splashFile, new ProgressResponseBody.ProgressListener() {
                @Override
                public void onPreExecute(long contentLength) {

                }

                @Override
                public void update(long totalBytes, boolean done) {
                    if (done) {
                        if (!TextUtils.isEmpty(resUrl)) {
                            CommonUtil.setSharedPreferences(CommonIntentService.this, Constants.CACHE.CACHE_SPLASH_URL, resUrl);
                        }
                    }
                }
            });
            mDownloader.download(0L, new ProgressDownloader.DownloadCallBack() {
                @Override
                public void onFailure(Call call, IOException e) {
                    initSplashDir();
                    CommonUtil.setSharedPreferences(CommonIntentService.this, Constants.CACHE.CACHE_SPLASH_URL, "");
                    downloadSplashImp(resUrl);
                }

                @Override
                public void onResponse(Call call, Response response) {

                }
            });
        }
    }

    private void initSplashDir() {
        File directory = new File(Constants.SPLASH_FILE_PATH);
        if (!directory.exists() && !directory.isDirectory()) {
            boolean mkdirs = directory.mkdirs();
        } else {
            CommonUtil.delAllFile(Constants.SPLASH_FILE_PATH);
        }
    }

}
