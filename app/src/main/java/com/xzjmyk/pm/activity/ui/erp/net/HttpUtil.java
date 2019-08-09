package com.xzjmyk.pm.activity.ui.erp.net;

import android.os.Looper;
import android.util.Log;

import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ObjectUtils;
import com.common.data.StringUtil;
import com.common.hmac.HmacUtils;
import com.common.thread.ThreadUtil;
import com.xzjmyk.pm.activity.util.OpenFilesUtils;
import com.core.net.http.http.OAHttpHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import static com.loopj.android.http.RequestParams.APPLICATION_JSON;


/**
 * @author :Administrator   2015年12月16日 上午11:09:31
 * @注释:网络请求HttpClient4.3--Android
 */
@SuppressWarnings("deprecation")
public class HttpUtil {

    public static Response sendGetRequest(
            String url,
            Map<String, Object> params,
            LinkedHashMap<String, Object> headers,
            boolean sign) throws Exception {
        SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
        DefaultHttpClient httpClient = MyHttpClient.getNewHttpClient();
        HttpResponse response = null;
        try {
            StringBuilder buf = new StringBuilder(url);
            if (url.indexOf("?") == -1)
                buf.append("?");
            else if (!url.endsWith("&"))
                buf.append("&");
            if (params != null && !params.isEmpty()) {
                Set<Entry<String, Object>> entrys = params.entrySet();
                for (Map.Entry<String, Object> entry : entrys) {
                    if (!ObjectUtils.isEquals(entry.getValue(), null)) {
                        buf.append(entry.getKey())
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue().toString(), "utf-8"))
                                .append("&");
                    }
                }
            }
            if (sign) {
                buf.append("_timestamp=").append(System.currentTimeMillis());
                String message = buf.toString();
                buf.append("&_signature=").append(HmacUtils.encode(message));
            } else
                buf.deleteCharAt(buf.length() - 1);
            HttpGet httpGet = new HttpGet(buf.toString());
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            if (headers != null) {
                for (String key : headers.keySet()) {
                    System.out.println("add header:" + key + " value:" + headers.get(key).toString());
                    httpGet.addHeader(key, headers.get(key).toString());
                }
            }
            response = httpClient.execute(httpGet);
            return Response.getResponse(response);
        } finally {

        }
    }


    public static Response sendPostRequest(
            String url,
            Map<String, Object> params,
            LinkedHashMap<String, Object> headers,
            boolean sign) throws Exception {
        if (url.contains("sms/send")) {
            return httpPostWithJSON(url, JSONUtil.map2JSON(params));

        }

        SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
        DefaultHttpClient httpClient = MyHttpClient.getNewHttpClient();
        HttpResponse response = null;
        if (sign) {
            url += (url.indexOf("?") == -1 ? "?" : "&") + "_timestamp="
                    + Long.valueOf("1441180144");
            url += "&_signature=" + HmacUtils.encode(url);
        }
        System.out.println(url);
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if (params != null && !params.isEmpty()) {
                Set<Entry<String, Object>> entrys = params.entrySet();
                for (Map.Entry<String, Object> entry : entrys) {
                    if (entry.getValue() != null)
                        //去掉了百分号替换方法
                        nvps.add(new BasicNameValuePair(entry.getKey(),
                                URLDecoder.decode(URLEncoder.encode(entry.getValue().toString(), "utf-8"), "utf-8")));
                    else
                        nvps.add(new BasicNameValuePair(entry.getKey(), URLDecoder.decode("", "utf-8")));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            if (headers != null) {
                for (String key : headers.keySet()) {
                    httpPost.addHeader(key, headers.get(key).toString());
                }
            }
            response = httpClient.execute(httpPost);
            return Response.getResponse(response);
        } finally {
        }
    }

    public static Response httpPostWithJSON(String url, String json) throws Exception {
        // 将JSON进行UTF-8编码,以便传输中文
        String encoderJson = URLEncoder.encode(json, HTTP.UTF_8);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
        StringEntity se = new StringEntity(encoderJson);
//        se.setContentType(HTTP.CONTENT_TYPE_TEXT_JSON);
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
        httpPost.setEntity(se);
        HttpResponse response = httpClient.execute(httpPost);
        return Response.getResponse(response);
    }

    public static Response sendPostRequest(
            String url,
            LinkedHashMap<String, Object> headers,
            String bodyString,
            boolean sign) throws Exception {
        SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
        DefaultHttpClient httpClient = MyHttpClient.getNewHttpClient();
        HttpResponse response = null;
        if (sign) {
            url += (url.indexOf("?") == -1 ? "?" : "&") + "_timestamp="
                    + Long.valueOf("1441180144");
            url += "&_signature=" + HmacUtils.encode(url);
        }
        HttpPost httpPost = new HttpPost(url);
        try {
            if (headers != null) {
                for (String key : headers.keySet()) {
                    System.out.println("add header:" + key + " value:" + headers.get(key).toString());
                    httpPost.setHeader(key, headers.get(key).toString());
                }
            }
            if (bodyString != null) {
                httpPost.setEntity(new StringEntity(bodyString, "UTF-8"));
            }
            response = httpClient.execute(httpPost);
            return Response.getResponse(response);
        } finally {

        }
    }

    public static class Response {
        public int statusCode;
        public String responseText;

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseText() {
            return responseText;
        }

        public Response(HttpResponse response) throws Exception {
            this.statusCode = response.getStatusLine().getStatusCode();
            this.responseText = HttpUtil.read2String(response.getEntity()
                    .getContent());
        }

        public static Response getResponse(HttpResponse response)
                throws Exception {
            if (response != null) {
                return new Response(response);
            }
            return null;
        }
    }

    /**
     * @author Administrator
     * @功能:待封装
     */
    public static String read2String(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 1024];
        int len = 0;
        try {
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
        } catch (OutOfMemoryError e) {
            Log.i("result", "内存溢出异常");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.i("result", "其它异常");
            e.printStackTrace();
            return null;
        } finally {
            outSteam.close();
            inStream.close();
        }
        return new String(outSteam.toByteArray(), "UTF-8");
    }

    /**
     * @author Administrator
     * @功能:HttpUrlConnection download
     */
    public static String download(String httpurl, String path) {
        try {
            URL url = new URL(httpurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setReadTimeout(50 * 1000);
            connection.setConnectTimeout(50 * 1000);
            connection.connect();
            int file_leng = connection.getContentLength();
            System.out.println("file length---->" + file_leng);
            InputStream bin = connection.getInputStream();
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                System.out.println("下载了： " + len * 100 / file_leng + "%\n");
            }
            bin.close();
            out.close();
            System.out.println("end:" + new SimpleDateFormat("MM-dd:HH:mm:ss:SS").format(new Date()));
            return path;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.out.println("下载超时");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void download(final String httpurl, final String path, final String fileName,final OpenFilesUtils.OnFileLoadListener onFileLoadListener) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ThreadUtil.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    onFileLoadResult(downloadAsyn(httpurl, path, fileName, onFileLoadListener), onFileLoadListener);
                }
            });
        } else {
            onFileLoadResult(downloadAsyn(httpurl, path, fileName, onFileLoadListener), onFileLoadListener);
        }
    }

    private static void onFileLoadResult(final String filePath,    final OpenFilesUtils.OnFileLoadListener onFileLoadListener) {
        boolean ok;
        if (!StringUtil.isEmpty(filePath) && new File(filePath).exists()) {
            ok = true;
        } else {
            ok = false;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (ok) {
                onFileLoadListener.onSuccess(new File(filePath));
            } else {
                onFileLoadListener.onFailure(filePath);
            }
        } else {
            final boolean finalOk = ok;
            OAHttpHelper.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    if (finalOk) {
                        onFileLoadListener.onSuccess(new File(filePath));
                    } else {
                        onFileLoadListener.onFailure(filePath);
                    }
                }
            });
        }


    }


    private static String downloadAsyn(String httpurl, String path, String fileName, final OpenFilesUtils.OnFileLoadListener onFileLoadListener) {
        URL url = null;
        try {
            url = new URL(httpurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            boolean useHttps = httpurl.startsWith("https");
            if (useHttps) {
                SSLContext sscontext = SSLContext.getInstance("SSL");
                sscontext.init(null, SSLUtil.trustAllCerts, new java.security.SecureRandom());
                javax.net.ssl.SSLSocketFactory ssf = sscontext.getSocketFactory();
                HttpsURLConnection https = (HttpsURLConnection) connection;
                https.setSSLSocketFactory(ssf);
                https.setHostnameVerifier(SSLUtil.DO_NOT_VERIFY);
            }
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setReadTimeout(50 * 1000);
            connection.setConnectTimeout(50 * 1000);
            connection.connect();

            final int file_leng = connection.getContentLength();
            InputStream bin = connection.getInputStream();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(path + "/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                //TODO 下载过程
                final int finalLen = len;
                OAHttpHelper.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onFileLoadListener.onLoadIng(finalLen * 100, file_leng);
                    }
                });
            }
            bin.close();
            out.close();
            return path + "/" + fileName;
        } catch (MalformedURLException e) {
            if (e != null) {
                return e.getMessage();
            }
        } catch (IOException e) {
            if (e != null) {
                return e.getMessage();
            }
        } catch (NoSuchAlgorithmException e) {
            if (e != null) {
                return e.getMessage();
            }
        } catch (KeyManagementException e) {
            if (e != null) {
                return e.getMessage();
            }
        } catch (Exception e) {
            if (e != null) {
                return e.getMessage();
            }
        }
        return "";
    }

}
