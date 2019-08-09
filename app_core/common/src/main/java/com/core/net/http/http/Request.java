package com.core.net.http.http;

import android.os.Bundle;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求参数
 * Created by Bitliker on 2017/3/7.
 */

public class Request {

    private int what;
    private String url;
    private Mode mode;
    private Map<String, Object> param;
    private LinkedHashMap<String, Object> headers;
    private Bundle bundle;

    public Mode getMode() {
        return mode;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public int getWhat() {
        return what;
    }

    public LinkedHashMap<String, Object> getHeaders() {
        return headers;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public String getUrl() {
        return url;
    }

    private Request() {
    }

    public static class Bulider {
        private Request request;

        public Bulider() {
            request = new Request();
        }

        public Bulider setMode(Mode mode) {
            request.mode = mode;
            return this;
        }

        public Bulider setUrl(String url) {
            request.url = url;
            return this;
        }

        public Bulider setParam(Map<String, Object> param) {
            request.param = param;
            return this;
        }

        public Bulider addParam(String key, String value) {
            if (request.param == null) {
                request.param = new HashMap<>();
            }
            request.param.put(key, value);
            return this;
        }

        public Bulider setHeaders(LinkedHashMap<String, Object> headers) {
            request.headers = headers;
            return this;
        }

        public Bulider addHeader(String key, String value) {
            if (request.headers == null) {
                request.headers = new LinkedHashMap<>();
            }
            request.headers.put(key, value);
            return this;
        }

        public Bulider setBundle(Bundle bundle) {
            request.bundle = bundle;
            return this;
        }

        public Bulider setWhat(int what) {
            request.what = what;
            return this;
        }

        public Request bulid() {
            return request;
        }
    }


    public enum Mode {
        GET, POST
    }
}
