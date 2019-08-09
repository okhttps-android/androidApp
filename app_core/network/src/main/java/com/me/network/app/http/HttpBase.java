package com.me.network.app.http;


import rx.Subscriber;

public abstract class HttpBase {

    public HttpClient mbuilder;

    /**
     * 设置全局builder
     * 初始化全局参数
     *
     * @param client
     */
    public void setBuilder(HttpClient client) {
        this.mbuilder = client;
    }

    /**
     * 初始化具体的网络请求客户端
     * 初始化全局参数
     * 比如Okhttp,Retrofit,Volley,HttpClient,HttpUrlConnection
     */
    public abstract void initClient();


    /**
     * @param builder
     * @param s       rxjava
     */
    public void send(HttpClient builder, Subscriber<Object> s) {
        if (builder.getMethod() == Method.GET) {
            get(builder, s);
        }
        if (builder.getMethod() == Method.POST) {
            post(builder, s);
        }
        if (builder.getMethod() == Method.POSTBODY) {
            postBody(builder, s);
        }
    }

    /**
     * @desc:上传功能 支持多文件上传
     * @author：Arison on 2017/5/17
     */
    public void uploads(HttpClient builder, Subscriber<Object> s) {
    }

    public abstract void get(HttpClient builder, Subscriber<Object> s);

    public abstract void post(HttpClient builder, Subscriber<Object> s);

    public abstract void postBody(HttpClient builder, Subscriber<Object> s);

}
