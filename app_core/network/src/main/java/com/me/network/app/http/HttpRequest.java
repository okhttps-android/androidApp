package com.me.network.app.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.me.network.app.base.BaseApplication;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;

import base.android.com.network.R;

/**
 * @author RaoMeng
 * @describe 发起网络请求
 * @date 2018/1/4 10:59
 */

public class HttpRequest {

    private static HttpRequest instance;

    private static HttpClient mHttpClient;

    private HttpRequest() {

    }

    public static final HttpRequest getInstance() {
        if (instance == null) {
            synchronized (HttpRequest.class) {
                if (instance == null) {
                    instance = new HttpRequest();
                }
            }
        }
        return instance;
    }

    /**
     * 发送请求
     *
     * @param httpParams  参数
     * @param callback    网络回调
     * @param requestHost 接口host
     */
    public void sendRequest(final String requestHost, final HttpParams httpParams, final HttpCallback callback) {

        if (validHttpParams(requestHost, httpParams, callback)) return;

        mHttpClient.Api().send(getBuild(httpParams), getResult(httpParams, callback));
    }

    public void uploadFile(final String requestHost, final HttpParams httpParams, final HttpCallback callback) {

        if (validHttpParams(requestHost, httpParams, callback)) return;

        mHttpClient.Api().uploads(getBuild(httpParams), getResult(httpParams, callback));
    }

    private ResultSubscriber<Object> getResult(final HttpParams httpParams, final HttpCallback callback) {
        return new ResultSubscriber<Object>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object response) {
                try {
                    callback.onSuccess(httpParams.getFlag(), response == null ? "" : response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Object t) {
                String failMsg = "服务器异常";
                if (t != null) {
                    String result = t.toString();
                    try {
                        Log.e("exceptionInfo", result);
                        JSONObject resultObject = JSON.parseObject(result);
                        failMsg = resultObject.getString("exceptionInfo");
                        if (TextUtils.isEmpty(failMsg) || "(null)".equals(failMsg) || "null".equals(failMsg)) {
                            failMsg = resultObject.getString("message");
                            if (TextUtils.isEmpty(failMsg) || "(null)".equals(failMsg) || "null".equals(failMsg)) {
                                failMsg = result;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("exceptionInfoE", result);
                        if (result.length() < 40) {
                            failMsg = result;
                        }
                    }
                }
                try {
                    if (failMsg.length() > 40) {
                        failMsg = "服务器异常";
                    }
                    callback.onFail(httpParams.getFlag(), failMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private com.me.network.app.http.HttpClient getBuild(HttpParams httpParams) {
        return new com.me.network.app.http.HttpClient.Builder()
                .url(httpParams.getUrl())
                .connectTimeout(httpParams.getConnectTimeOut())
                .writeTimeout(httpParams.getWriteTimeOut())
                .readTimeout(httpParams.getReadTimeOut())
                .addHeaders(httpParams.getHeaders())
                .addParams(httpParams.getParams())
                .postBody(httpParams.getPostBody())
                .method(httpParams.getMethod())
                .build();
    }

    private boolean validHttpParams(String requestHost, HttpParams httpParams, HttpCallback callback) {
        if (httpParams == null) {
            throw new IllegalArgumentException("httpParams can not be NULL");
        }
        if (requestHost == null) {
            throw new IllegalArgumentException("requestHost can not be NULL");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback can not be NULL");
        }
        if (httpParams.getUrl() == null) {
            throw new IllegalArgumentException("requestUrl can not be NULL");
        }

        if (!isNetWorkConnected(BaseApplication.getInstance())) {
            try {
                callback.onFail(httpParams.getFlag(), BaseApplication.getInstance().getString(R.string.networks_out));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mHttpClient = new HttpClient.Builder(requestHost).isDebug(true).build();
        return false;
    }

    /**
     * 检测网络是否可用
     *
     * @param context
     * @return
     */
    private boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission")
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
