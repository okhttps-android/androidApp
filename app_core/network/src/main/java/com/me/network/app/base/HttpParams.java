package com.me.network.app.base;

import java.util.HashMap;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 网络请求参数
 * @date 2018/1/3 15:18
 */

public class HttpParams {
    public static final long CONNECT_TIME_OUT = 30;
    public static final long READ_TIME_OUT = 30;
    public static final long WRITE_TIME_OUT = 30;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 回调标记
     */
    private int flag;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * postBody
     */
    private Object postBody;

    /**
     * 请求头
     */
    private Map<String, Object> headers;

    /**
     * 请求方式
     */
    private int method;

    /**
     *
     */

    private long connectTimeOut = CONNECT_TIME_OUT;

    private long readTimeOut = READ_TIME_OUT;

    private long writeTimeOut = WRITE_TIME_OUT;


    public HttpParams(Builder builder) {
        this.url = builder.url;
        this.flag = builder.flag;
        this.params = builder.params;
        this.headers = builder.headers;
        this.postBody = builder.postBody;
        this.method = builder.method;
        this.connectTimeOut = builder.connectTimeOut;
        this.readTimeOut = builder.readTimeOut;
        this.writeTimeOut = builder.writeTimeOut;
    }

    public String getUrl() {
        return url;
    }

    public int getFlag() {
        return flag;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Object getPostBody() {
        return postBody;
    }

    public int getMethod() {
        return method;
    }

    public long getConnectTimeOut() {
        return connectTimeOut;
    }

    public long getReadTimeOut() {
        return readTimeOut;
    }

    public long getWriteTimeOut() {
        return writeTimeOut;
    }

    public static class Builder {
        private String url;
        private int flag;
        private Map<String, Object> params = new HashMap<>();
        private Map<String, Object> headers = new HashMap<>();
        private Object postBody;
        private int method;
        private long connectTimeOut = CONNECT_TIME_OUT;
        private long readTimeOut = READ_TIME_OUT;
        private long writeTimeOut = WRITE_TIME_OUT;

        public Builder url(String val) {
            this.url = val;
            return this;
        }

        public Builder flag(int val) {
            this.flag = val;
            return this;
        }

        public Builder method(int httpMethod) {
            this.method = httpMethod;
            return this;
        }

        public Builder setParams(Map<String, Object> params) {
            if (this.params == null) {
                this.params = new HashMap<>();
            }
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public Builder addParam(String key, Object value) {
            if (this.params == null) {
                this.params = new HashMap<>();
            }
            this.params.put(key, value);
            return this;
        }

        public Builder setHeaders(Map<String, Object> headers) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public Builder addHeader(String key, Object value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }

        public Builder postBody(Object postBody) {
            this.postBody = postBody;
            return this;
        }

        public Builder connectTimeOut(long seconds) {
            this.connectTimeOut = seconds;
            return this;
        }

        public Builder readTimeOut(long seconds) {
            this.readTimeOut = seconds;
            return this;
        }

        public Builder writeTimeOut(long seconds) {
            this.writeTimeOut = seconds;
            return this;
        }

        public HttpParams build() {
            return new HttpParams(this);
        }
    }
}
