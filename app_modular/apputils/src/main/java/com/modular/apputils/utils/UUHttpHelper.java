package com.modular.apputils.utils;

import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;

/**
 * 便捷的网络封装
 */
public class UUHttpHelper {
    private HttpClient mHttpClient;

    public UUHttpHelper(String baseUrl) {
        initHttpConfig(baseUrl);
    }

    private void initHttpConfig(String baseUrl) {
        if (!StringUtil.isEmpty(baseUrl)) {
            mHttpClient = new HttpClient.Builder(baseUrl).isDebug(true)
                    .connectTimeout(5000)
                    .readTimeout(5000).build();
        }
    }

    public void requestHttp(Parameter.Builder builder, final OnSmartHttpListener onHttpListener) {
        if (mHttpClient != null && builder != null) {
            HttpClient.Builder httpBuilder = new HttpClient.Builder();
            final Parameter parameter = builder.builder();
            httpBuilder.addParams(parameter.getParams())
                    .addHeaders(parameter.getHeaders())
                    .method(parameter.getMode())
                    .url(parameter.getUrl())
                    .isDebug(false);
            HttpClient mRequest = httpBuilder.build();
            mHttpClient.Api().send(mRequest, new ResultSubscriber<>(new Result2Listener<Object>() {
                @Override
                public void onResponse(Object o) {
                    if (onHttpListener != null) try {
                        Tags tags = parameter.getTag();
                        int record = 0;
                        if (tags != null) {
                            record = tags.getRecord();
                        }
                        String message = o.toString();
                        onHttpListener.onSuccess(record, message, tags);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Object t) {
                    if (onHttpListener != null) try {
                        Tags tags = parameter.getTag();
                        int record = 0;
                        if (tags != null) {
                            record = tags.getRecord();
                        }
                        String message = t.toString();
                        if (parameter.showLog()) {
                            LogUtil.prinlnLongMsg("SmartHttp", "onFailure=" + message);
                        }
                        onHttpListener.onFailure(record, message, tags);
                    } catch (Exception e) {
                        LogUtil.i("oooo=" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }));
        }
    }

    public void requestCompanyHttp(Parameter.Builder builder, final OnSmartHttpListener onHttpListener) {
        if (builder != null) {
            String sessionId = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId");
            String emCode = CommonUtil.getEmcode();
            builder.addSuperParams("sessionId", sessionId);
            builder.addSuperParams("master", CommonUtil.getMaster());
            builder.addSuperParams("sessionUser", emCode);
            builder.addSuperHeaders("sessionUser", emCode);
            builder.addSuperHeaders("Cookie", "JSESSIONID=" + sessionId);
            requestHttp(builder, onHttpListener);
        }
    }

}
