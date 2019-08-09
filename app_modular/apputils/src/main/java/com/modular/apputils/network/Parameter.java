package com.modular.apputils.network;


import android.support.annotation.IntDef;

import com.alibaba.fastjson.JSON;
import com.me.network.app.http.Method;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bitliker on 2017/9/29.
 */

public class Parameter {

    public static final String MEDIA_TYPE_XWWW = "application/x-www-form-urlencoded;charset=utf-8";
    public static final String MEDIA_TYPE_JSON = "application/json;charset=utf-8";
    public static final String MEDIA_TYPE_PLAIN = "text/plain;charset=utf-8";
    public static final String MEDIA_TYPE_STREAM = "application/octet-stream;charset=utf-8";
    public static final String MEDIA_TYPE_MULTIPART = "multipart/form-data;charset=utf-8";


    private int mode = Method.GET;//请求模式
    private String url;//请求的url
    private String mediaType = MEDIA_TYPE_JSON;
    private Map<String, Object> params;
    private Map<String, Object> headers;
    private Tags tag;
    private boolean autoProgress = false;
    private boolean showLog = false;
    private boolean saveLog;


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    private Parameter() {
        tag = new Tags();
        params = new HashMap<>();
        headers = new HashMap<>();
    }

    public boolean isSaveLog() {
        return saveLog;
    }

    public boolean showLog() {
        return showLog;
    }

    public boolean autoProgress() {
        return autoProgress;
    }

    public Map<String, Object> getHeaders() {
        return headers == null ? new HashMap<String, Object>() : headers;
    }

    public Map<String, Object> getParams() {
        return params == null ? new HashMap<String, Object>() : params;
    }

    public Tags getTag() {
        return tag == null ? new Tags() : tag;
    }

    public int getMode() {
        return mode == 0 ? Method.GET : mode;
    }


    public String getUrl() {
        return url;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static class Builder {

        private Parameter request;

        public Builder(Parameter request) {
            this.request = request;
        }

        public Builder() {
            this.request = new Parameter();
        }


        public Builder mode(@Duration int mode) {
            this.request.mode = mode;
            return this;
        }

        public Builder url(String url) {
            if (url != null) {
                this.request.url = url;
            }
            return this;
        }

        public Builder mediaType(String mediaType) {
            if (mediaType != null) {
                this.request.mediaType = mediaType;
            }
            return this;
        }

        public Builder headers(Map<String, Object> header) {
            if (header != null) {
                this.request.headers = header;
            }
            return this;
        }

        public Builder addHeaders(String key, Object value) {
            this.request.getHeaders().put(key, value);
            return this;
        }

        public Builder params(Map<String, Object> params) {
            if (params != null) {
                this.request.params = params;
            }
            return this;
        }

        public Builder addParams(String key, Object value) {
            this.request.getParams().put(key, value);
            return this;
        }

        public Builder saveLog(boolean saveLog) {
            this.request.saveLog = saveLog;
            return this;
        }

        public Builder addSuperParams(String key, Object value) {
            if (!this.request.getParams().containsKey(key)) {
                return addParams(key, value);
            }
            return this;
        }

        public Builder addSuperHeaders(String key, Object value) {
            if (!this.request.getHeaders().containsKey(key)) {
                return addHeaders(key, value);
            }
            return this;
        }

        public Builder tag(Object tag) {
            if (tag != null) {
                this.request.getTag().tag(tag);
            }
            return this;
        }

        public Builder record(int code) {
            this.request.getTag().record(code);
            return this;
        }

        public Builder autoProgress(boolean autoProgress) {
            this.request.autoProgress = autoProgress;
            return this;
        }

        public Builder showLog(boolean showLog) {
            this.request.showLog = showLog;
            return this;
        }


        public Builder addTag(int key, Object values) {
            if (values != null) {
                this.request.getTag().put(key, values);
            }
            return this;
        }

        public Parameter builder() {
            return request;
        }


        public Parameter bulid() {
            return request;
        }
    }


    @IntDef({Method.GET, Method.POST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }


}
