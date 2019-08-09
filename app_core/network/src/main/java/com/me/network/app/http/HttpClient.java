package com.me.network.app.http;


import com.me.network.app.http.impl.RetrofitImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Arison
 * @desc:使用构建者设计模式封装
 */
public class HttpClient {

    private String baseUrl;
    private Map<String, Object> params = new HashMap<>();// 请求参数
    private Map<String, Object> headers = new HashMap<>();//请求头
    private Object postBody;

    private long retryTimeout = 5;
    private long connectTimeout;
    private long readTimeout;
    private long writeTimeout;

    private int method;// 方法
    private boolean isSyn;// 是否是同步
    private int cacheType;// 缓存类型
    private long cacheTime;//缓存时间
    private File cacheFile;//缓存文件路径
    private long cacheFileSize;//缓存文件大小
    private int maxRetryCount;// 最大重试次数
    private boolean isDebug;// 是否开启打印日志
    private Builder mBuilder;

    private HttpBase httpBase;

    private int what = -1;

    public HttpClient(Builder builder) {
        super();
        setBuilder(builder);
    }

    private void setBuilder(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.params = builder.params;
        this.headers = builder.headers;
        this.postBody = builder.postBody;
        this.retryTimeout = builder.retryTimeout;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.method = builder.method;
        this.isSyn = builder.isSyn;
        this.cacheType = builder.cacheType;
        this.cacheTime = builder.cacheTime;
        this.cacheFile = builder.cacheFile;
        this.cacheFileSize = builder.cacheFileSize;
        this.maxRetryCount = builder.maxRetryCount;
        this.isDebug = builder.isDebug;
        this.httpBase = builder.httpBase;
    }


    private static HttpClient instance;

    public static HttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClient.class) {
                if (instance == null) {
                    instance = newInstance(new Builder());
                }
            }
        }
        return instance;
    }

    public static HttpClient getInstance(Builder builder) {
        if (instance == null) {
            synchronized (HttpClient.class) {
                if (instance == null) {
                    instance = newInstance(builder);
                }
            }
        }
        instance.setBuilder(builder);
        return instance;
    }

    public static HttpClient newInstance(Builder builder) {
        instance = new HttpClient(builder);

        return instance;
    }


    public Builder newBuilder() {
        return new Builder(this);
    }


    public static class Builder {

        private String baseUrl;
        private Map<String, Object> params = new HashMap<>();// 请求参数
        private Map<String, Object> headers = new HashMap<>();//
        private Object postBody;

        private long connectTimeout;
        private long readTimeout;
        private long writeTimeout;
        private int method;// 方法
        private boolean isSyn;// 是否是同步
        private int cacheType;// 缓存类型
        private long cacheTime;//缓存时间
        private File cacheFile;//缓存文件路径
        private long cacheFileSize;//缓存文件大小
        private int maxRetryCount;// 最大重试次数
        private long retryTimeout = 5;//重试间隔时间
        private boolean isDebug;// 是否开启打印日志
        private HttpBase httpBase;//具体的网络请求类
        private int what = -1;

        //默认的参数
        public Builder() {
            this.method = Method.GET;
            this.headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        }

        public Builder(String url) {
            this.baseUrl = url;
        }

        public Builder(HttpClient httpClient) {
            this.baseUrl = httpClient.getBaseUrl();
            this.params = httpClient.getParams();
            this.headers = httpClient.getHeaders();
            this.postBody = httpClient.getPostBody();
            this.method = httpClient.getMethod();
            this.connectTimeout = httpClient.getConnectTimeout();
            this.readTimeout = httpClient.getReadTimeout();
            this.retryTimeout = httpClient.getRetryTimeout();
            this.writeTimeout = httpClient.getWriteTimeout();
            this.isSyn = httpClient.isSyn();
            this.isDebug = httpClient.isDebug();
            this.cacheType = httpClient.getCacheType();
            this.cacheTime = httpClient.getCacheTime();
            this.cacheFileSize = httpClient.getCacheFileSize();
            this.cacheFile = httpClient.getCacheFile();
            this.maxRetryCount = httpClient.getMaxRetryCount();
            this.httpBase = httpClient.Api();
            this.what = httpClient.what;
        }

        public Builder url(String url) {
            this.baseUrl = url;
            return this;
        }

        public Builder addParams(Map<String, Object> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public Builder add(String key, Object value) {
            if (value == null) return this;
            this.params.put(key, value);
            return this;
        }

        public Builder addHeaders(Map<String, Object> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public Builder header(String key, Object value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder postBody(Object postBody) {
            this.postBody = postBody;
            return this;
        }

        public Builder retryTimeout(long time) {
            this.retryTimeout = time;
            return this;
        }

        public Builder connectTimeout(long time) {
            this.connectTimeout = time;
            return this;
        }

        public Builder readTimeout(long time) {
            this.readTimeout = time;
            return this;
        }

        public Builder writeTimeout(long time) {
            this.writeTimeout = time;
            return this;
        }

        public Builder cacheType(int cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder cacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        public Builder cacheFile(File cacheFile) {
            this.cacheFile = cacheFile;
            return this;
        }

        public Builder cacheFileSize(long cacheFileSize) {
            this.cacheFileSize = cacheFileSize;
            return this;
        }

        public Builder maxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public Builder isDebug(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder method(int method) {
            this.method = method;
            return this;
        }

        public Builder syn(boolean isSyn) {
            this.isSyn = isSyn;
            return this;
        }

        public Builder httpBase(HttpBase hb) {
            this.httpBase = hb;
            return this;
        }

        public Builder what(int what) {
            this.what = what;
            return this;
        }

        public HttpClient build() {
            HttpClient client = build(false);
            return client;
        }

        public HttpClient build(boolean isSingle) {
            if (isSingle) {
                return getInstance(this);
            } else {
                return newInstance(this);
            }
        }
    }


    public String getBaseUrl() {
        return baseUrl;
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

    public boolean isSyn() {
        return isSyn;
    }

    public int getCacheType() {
        return cacheType;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public int getWhat() {
        return what;
    }

    public HttpBase Api() {
        if (httpBase != null) {
            httpBase.setBuilder(this);
            httpBase.initClient();
        } else {
            httpBase = RetrofitImpl.getInstance();
            httpBase.setBuilder(this);
            httpBase.initClient();
        }
        return httpBase;
    }

    public Builder getmBuilder() {
        return mBuilder;
    }

    public void setmBuilder(Builder mBuilder) {
        setBuilder(mBuilder);
        this.mBuilder = mBuilder;
    }


    public long getRetryTimeout() {
        return retryTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }


    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }


    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public long getCacheFileSize() {
        return cacheFileSize;
    }

    public void setCacheFileSize(long cacheFileSize) {
        this.cacheFileSize = cacheFileSize;
    }

}
