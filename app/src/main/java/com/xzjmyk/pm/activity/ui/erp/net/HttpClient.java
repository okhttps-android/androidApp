package com.xzjmyk.pm.activity.ui.erp.net;

import android.util.Log;

import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.hmac.HmacUtils;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Deprecated
public class HttpClient {

    /**
     * 发送GET请求
     *
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public String sendGetRequest(String url, Map<String, String> params)
            throws Exception {
        String result = null;
        DefaultHttpClient httpclient = MyHttpClient.getNewHttpClient();
        ;
        boolean sign = true;
        StringBuilder buf = new StringBuilder(url);
        Set<Entry<String, String>> entrys = null;
        if (params != null && !params.isEmpty()) {
            if (buf.indexOf("?") == -1)
                buf.append("?");
            entrys = params.entrySet();
            for (Entry<String, String> entry : entrys) {
                buf.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .append("&");
            }
            if (sign) {
                // 加时间戳，保持相同请求每次签名均不一样
                buf.append("_timestamp=").append(System.currentTimeMillis());
                String message = buf.toString();
                // 对请求串进行签名
                buf.append("&_signature=").append(HmacUtils.encode(message));
            } else
                buf.deleteCharAt(buf.length() - 1);
        }
        HttpGet httpGet = new HttpGet(buf.toString());
        httpclient.getParams().setParameter(
                CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        httpGet.setHeader("Cookie", "JSESSIONID=" + params.get("sessionId"));
//        if (!TextUtils.isEmpty(MyApplication.getInstance().getJSESSION_B2B())) {
//            httpGet.addHeader("Cookie", "JSESSIONID=" + MyApplication.getInstance().getJSESSION_B2B());
//        }
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
        HttpResponse response = httpclient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == 200) {
            String temp = EntityUtils.toString(response.getEntity());
            if (temp.length() > 0) {
                result = temp.trim().toString();
            } else {
                result = "201";
            }
        } else {
            result = null;
        }
        return result;
    }


    public String sendPostRequest(String url, Map<String, String> params)
            throws IOException {
        // SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
        Log.i("HTTP", "post url=" + url);
        String result = null;
        HttpResponse response = null;
        DefaultHttpClient httpclient = MyHttpClient.getNewHttpClient();
        ;
        boolean sign = true;
        if (sign) {
            url += (url.indexOf("?") == -1 ? "?" : "&") + "_timestamp="
                    + System.currentTimeMillis();
            url += "&_signature=" + HmacUtils.encode(url);
        }
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if (params != null && !params.isEmpty()) {
                Set<Entry<String, String>> entrys = params.entrySet();
                for (Entry<String, String> entry : entrys) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), URLDecoder
                            .decode(entry.getValue(), "UTF-8")));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            httpPost.setHeader("Content-Type",
                    "application/x-www-form-urlencoded; charset=utf-8");
            httpPost.setHeader("Cookie",
                    "JSESSIONID=" + params.get("sessionId"));
//            httpPost.addHeader("Cookie", "JSESSIONID="
//                    + MyApplication.getInstance().getJSESSION_B2B());
            response = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.getStatusLine().getStatusCode() == 200
                || response.getStatusLine().getStatusCode() == 207) {
            String temp = EntityUtils.toString(response.getEntity());
            if (temp.length() > 0) {
                result = temp.trim().toString();
            } else {
                result = "207";
            }
            //b2b cookie save
            if (StringUtil.isEmpty(url)) return null;
            try {
                if (url.contains(ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().login)) {
                    String b2bCookie = response.getHeaders("Set-Cookie")[0].getValue() + ";" + response.getHeaders("Set-Cookie")[1].getValue();
                    LogUtil.d("HttpClient", "B2B Cookie:" + b2bCookie);
                    ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().setCookie(b2bCookie);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            CookieStore cookieStore = httpclient.getCookieStore();
            for (int i = 0; i < cookieStore.getCookies().size(); i++) {
                if ("218.17.158.219".equals(cookieStore.getCookies().get(i)
                        .getDomain())) {
//                    PreferenceUtils.putLong(MyApplication.getInstance(), "WebViewCommActivity_time", System.currentTimeMillis());
                    MyApplication.cookieERP = cookieStore.getCookies().get(i);
                } else {
                    MyApplication.cookieERP = cookieStore.getCookies().get(i);
//                    PreferenceUtils.putLong(MyApplication.getInstance(), "WebViewCommActivity_time", System.currentTimeMillis());
                }
            }

        } else {
            result = prinInfo(response, response.getStatusLine().getStatusCode());
        }
        return result;
    }

    public String prinInfo(HttpResponse response, int branch)
            throws IllegalStateException, IOException {
        InputStream stream = response.getEntity().getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream,
                "UTF-8"));
        StringBuffer bufer = new StringBuffer();
        String line;
        while (null != (line = br.readLine())) {
            bufer.append(line).append("\n");
        }
        System.out.println("响应码：" + branch + "响应正文：" + bufer.toString());
        return bufer.toString();
    }
}
