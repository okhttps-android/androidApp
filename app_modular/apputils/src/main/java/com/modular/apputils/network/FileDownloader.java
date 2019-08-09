package com.modular.apputils.network;

import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.common.LogUtil;
import com.core.app.MyApplication;
import com.core.net.ProgressResponseBody;
import com.me.network.app.http.ssl.TrustAllCerts;
import com.me.network.app.http.ssl.TrustAllHostnameVerifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Bitlike on 2018/3/6.
 */

public class FileDownloader {
    private String url;
    private OkHttpClient client;
    protected File destination;
    private Call call;
    private OnDownloaderListener onDownloaderListener;

    public FileDownloader(String url, OnDownloaderListener onDownloaderListener) {
        this(url, null, onDownloaderListener);

    }

    public FileDownloader(String url, File destination, final OnDownloaderListener onDownloaderListener) {
        this.url = url;
        this.destination = destination;
        this.onDownloaderListener = onDownloaderListener;
        client = getProgressClient(new ProgressResponseBody.ProgressListener() {
            private long contentLength;

            @Override
            public void onPreExecute(long contentLength) {
                this.contentLength = contentLength;
            }

            @Override
            public void update(long totalBytes, boolean done) {
                if (onDownloaderListener != null) {
                    onDownloaderListener.onProgress(this.contentLength, totalBytes);
                    if (done) {
                        onDownloaderListener.onSuccess(FileDownloader.this.destination);
                    }
                }
            }
        });
    }

    //每次下载需要新建新的Call对象
    private Call newCall(long startPoints) {
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startPoints + "-")//断点续传要用到的，指示下载的区间
                .build();
        return client.newCall(request);
    }

    public OkHttpClient getProgressClient(final ProgressResponseBody.ProgressListener progressListener) {
        // 拦截器，用上ProgressResponseBody
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        };

        return new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())// 信任所有证书
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .build();
    }

    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    // startsPoint指定开始下载的点
    public void download(final long startsPoint) {
        call = newCall(startsPoint);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onDownloaderListener != null) {
                    onDownloaderListener.onFailure(e != null ? e.getMessage() : "IOException");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                save(response, startsPoint);
            }
        });
    }

    public void pause() {
        if (call != null) {
            call.cancel();
        }
    }


    private File getDestinationFile(Response response) throws IOException {
        String disposition = response.headers().get("Content-Disposition");
        if (!TextUtils.isEmpty(disposition) && disposition.contains("filename=")) {
            String[] fileNames = disposition.split("filename=");
            if (fileNames != null && fileNames.length > 1) {
                String fileName = fileNames[1];
                if (!TextUtils.isEmpty(fileName)) {
                    fileName= fileName.substring(1,fileName.length()-1);
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/uu/download";
                        File file = new File(path);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        file = new File(path + "/" + fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        } else {
                            if (onDownloaderListener != null) {
                                onDownloaderListener.onSuccess(file);
                            }
                            return null;
                        }
                        return file;
                    } else {
                        Toast.makeText(MyApplication.getInstance(), "请先插入SD卡", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        if (onDownloaderListener != null) {
            onDownloaderListener.onFailure("file is null");
        }
        return null;
    }


    protected void save(Response response, long startsPoint) {
        if (this.destination == null) {
            try {
                this.destination = getDestinationFile(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.destination == null) {
            return;
        }
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(destination, "rwd");
            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            channelOut = randomAccessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startsPoint, body.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnDownloaderListener {
        void onProgress(long allProress, long progress);

        void onSuccess(File file);

        void onFailure(String exception);
    }

}
