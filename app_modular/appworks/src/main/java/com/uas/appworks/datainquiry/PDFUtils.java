package com.uas.appworks.datainquiry;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by RaoMeng on 2017/8/16.
 */
public class PDFUtils {
    /**
     * 下载pdf文件
     *
     * @param pdfUrl   文件地址
     * @param mHandler
     */
    public static void downloadPDF(String pdfUrl, final Handler mHandler) {
        if (pdfUrl == null) {
            Message message = Message.obtain();
            message.what = Constants.CONSTANT.DOWNLOAD_FAILED;
            message.obj = "下载地址为空";
            mHandler.sendMessage(message);
            return;
        }
//        try {
//            pdfUrl = URLEncoder.encode(pdfUrl, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        Request mRequest = new Request.Builder().url(pdfUrl).build();
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .connectTimeout(60 * 1000, TimeUnit.SECONDS)
                .readTimeout(60 * 1000, TimeUnit.SECONDS)
                .build();
        mOkHttpClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(Constants.CONSTANT.DOWNLOAD_FAILED);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                InputStream inputStream = null;
                byte[] buffer = new byte[2048];
                int length = 0;
                FileOutputStream fileOutputStream = null;
                try {
                    if (response.body() != null) {
                        String responseStr = response.body().string();
                        JSONObject responseObject = new JSONObject(responseStr);
                        if (!responseObject.isNull("success") && !responseObject.optBoolean("success")) {
                            Message message = Message.obtain();
                            message.what = Constants.CONSTANT.DOWNLOAD_FAILED;
                            message.obj = responseObject.optString("message");
                            mHandler.sendMessage(message);
                            return;
                        }
                        boolean overload = responseObject.optBoolean("overload");
                        int pageSize = -1;
                        if (!responseObject.isNull("pageSize")) {
                            pageSize = responseObject.optInt("pageSize");
                        }
                        if (overload) {
                            mHandler.sendEmptyMessage(Constants.CONSTANT.PDF_OVERLOAD);
                        } else if (pageSize == 0) {
                            Message message = Message.obtain();
                            message.what = Constants.CONSTANT.DOWNLOAD_FAILED;
                            message.obj = "报表文件内容为空";
                            mHandler.sendMessage(message);
                        } else {
                            File directory = new File(Constants.CONSTANT.PDF_FILE_PATH);
                            if (!directory.exists() && !directory.isDirectory()) {
                                boolean mkdirs = directory.mkdirs();
                            } else {
                                delAllFile(Constants.CONSTANT.PDF_FILE_PATH);
                            }
                            String printData = responseObject.optString("data");
                            if (TextUtils.isEmpty(printData)) {
                                mHandler.sendEmptyMessage(Constants.CONSTANT.DOWNLOAD_FAILED);
                            } else {
                                inputStream = getStringStream(printData);
                                if (inputStream != null) {
                                    int available = inputStream.available();
                                    File file = new File(Constants.CONSTANT.PDF_FILE_PATH + "/", Constants.CONSTANT.PDF_FILE_NAME);
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    fileOutputStream = new FileOutputStream(file);
                                    int count = 0;
                                    while ((length = inputStream.read(buffer)) != -1) {
                                        fileOutputStream.write(buffer, 0, length);
                                        count += length;
                                        int progress = (count * 100) / available;
                                        Message message = Message.obtain();
                                        message.what = Constants.CONSTANT.DOWNLOAD_PROGRESS;
                                        message.obj = progress;
                                        mHandler.sendMessage(message);
                                    }
                                    fileOutputStream.flush();
                                    mHandler.sendEmptyMessage(Constants.CONSTANT.DOWNLOAD_SUCCESS);
                                }
                            }
                        }
                    } else {
                        mHandler.sendEmptyMessage(Constants.CONSTANT.DOWNLOAD_FAILED);
                    }

                } catch (Exception e) {
                    mHandler.sendEmptyMessage(Constants.CONSTANT.DOWNLOAD_FAILED);
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }
            }
        });
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    //删除文件夹
    //param folderPath 文件夹完整绝对路径
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream getStringStream(String sInputString) {
        if (!TextUtils.isEmpty(sInputString)) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(Base64.decode(sInputString, Base64.DEFAULT));
                return tInputStringStream;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    public static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }


    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
