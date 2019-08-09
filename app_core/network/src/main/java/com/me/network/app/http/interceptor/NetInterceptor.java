package com.me.network.app.http.interceptor;

import com.me.network.app.http.HttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetInterceptor implements Interceptor {

    private HttpClient builder;

    public NetInterceptor() {
        super();
    }

    public NetInterceptor(HttpClient builder) {
        this.builder = builder;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> postParam = new HashMap<>();
        //添加公共Header,公共参数
        if (builder != null) {
            headers = builder.getHeaders();
            params = builder.getParams();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    request = request.newBuilder()
                            .addHeader(entry.getKey(), String.valueOf(entry.getValue()))
                            .build();
                }
            }
            request = request.newBuilder()
                    .removeHeader("User-Agent")
                    .removeHeader("Accept-Encoding")
                    .build();

            if (params != null) {
                //get请求    添加公共参数
                if (request.method().equals("GET")) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        HttpUrl httpUrl = request.url().newBuilder()
                                .addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()))
                                .build();
                        postParam.put(entry.getKey(), String.valueOf(entry.getValue()));
                        request = request.newBuilder().url(httpUrl).build();

                    }
                }
                if (request.method().equals("POST")) {
                    if (request.body() instanceof FormBody) {
                        FormBody.Builder bodyBuilder = new FormBody.Builder();
                        FormBody formBody = (FormBody) request.body();
                        for (int i = 0; i < formBody.size(); i++) {
                            postParam.put(formBody.encodedName(i), formBody.encodedValue(i));
                            bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
                        }
                        for (Map.Entry<String, Object> entry : params.entrySet()) {
                            postParam.put(entry.getKey(), String.valueOf(entry.getValue()));
                            formBody = bodyBuilder
                                    .addEncoded(entry.getKey(), String.valueOf(entry.getValue()))
                                    .build();
                        }
                        request = request.newBuilder().post(formBody).build();
                    }

                }
            }
        }

        Response response = chain.proceed(request);
        if (response.body() != null && response.body().contentType() != null) {
            MediaType mediaType = response.body().contentType();
            String content = response.body().string();
            ResponseBody responseBody = ResponseBody.create(mediaType, content);
            return response.newBuilder().body(responseBody).build();
        } else {
            return response;
        }
    }

    public void setBuilder(HttpClient builder) {
        this.builder = builder;
    }

}
