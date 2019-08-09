package com.core.net.http.http;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiModel;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 封装的http请求，以对象编程，防止出现扩展时候修改过多地方
 * 函数参数不应该多于三个，如果必须，应该考虑以对象封装参数
 * Created by Bitliker on 2017/4/7.
 */

public class OAHttpHelper extends Handler {
    private static OAHttpHelper instance;
    private ApiModel apiModel;


    public ApiModel getApiModel() {
        return apiModel == null ? apiModel=ApiUtils.getApiModel() : apiModel;
    }

    private OAHttpHelper() {
        //在主线程里创建Handler
        super(Looper.getMainLooper());
    }

    public static OAHttpHelper getInstance() {
        if (instance == null) {
            synchronized (OAHttpHelper.class) {
                instance = new OAHttpHelper();
            }
        }
        return instance;
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            OnHttpResultListener listener = (OnHttpResultListener) msg.getData().getSerializable("listener");
            if (listener == null) return;
            Bundle bundle = msg.getData();
            if (bundle == null) return;
            String message = bundle.getString("result");
            int code = msg.getData().getInt("code");
            if (code == 200 && !StringUtil.isEmpty(message)) {//返回正确数据
                listener.result(msg.what, JSONUtil.validate(message), message, bundle);
            } else {
                int what = msg.getData().getInt("what");
                listener.error(what, message, bundle);
            }
        } catch (NullPointerException e) {
            if (e != null) LogUtil.d("handleMessage NullPointerException" + e.getMessage());
        } catch (ClassCastException e) {
            if (e != null) LogUtil.d("handleMessage ClassCastException" + e.getMessage());
        } catch (Exception e) {
            if (e != null) LogUtil.d("handleMessage Exception" + e.getMessage());
        }
    }

    /**
     * 唯一对外接口
     *
     * @param request  请求体
     * @param listener 回调监听器
     */
    public void requestHttp(Request request, OnHttpResultListener listener) {
        if (request == null) new NullPointerException("Request is null");
        try {
	        String role = CommonUtil.getUserRole();
	        if (role.equals("3")) {
		        loadB2B(request, listener);
	        } else if (role.equals("2")) {
		        loadERP(request, listener);
	        }
        } catch (Exception e) {
            if (e != null)
                LogUtil.d(e.getMessage());
        }
    }

    /**
     * 唯一对外接口
     *
     * @param request  请求体
     * @param listener 回调监听器
     */
    public void requestHttp(Request request, int dataSource, OnHttpResultListener listener) {
        if (request == null) new NullPointerException("Request is null");
        try {
            if (dataSource == 1) {
                loadIM(request, listener);
            } else {
                if (getApiModel() instanceof ApiPlatform)
                    loadB2B(request, listener);
                else loadERP(request, listener);
            }

        } catch (Exception e) {
            if (e != null)
                LogUtil.d(e.getMessage());
        }
    }

    private void loadIM(Request request, OnHttpResultListener listener) throws Exception {
        String url = Constants.IM_BASE_URL() + request.getUrl();
        Bundle bundle = getBundle(request);
        if (listener != null)
            bundle.putSerializable("listener", listener);
        Message message = Message.obtain();
        message.setData(bundle);
        Request.Mode mode = getMode(request);
        ViewUtil.httpSendRequest(BaseConfig.getContext(), url, request.getParam(), this, null, request.getWhat(), message, bundle, mode == Request.Mode.GET ? "get" : "post");
    }

    private void loadERP(Request request, OnHttpResultListener listener) throws Exception {
        LinkedHashMap<String, Object> headers = getHeaders(request);
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(BaseConfig.getContext(), "sessionId"));
        String url = CommonUtil.getAppBaseUrl(BaseConfig.getContext()) + request.getUrl();
        Bundle bundle = getBundle(request);
        if (listener != null)
            bundle.putSerializable("listener", listener);
        Message message = Message.obtain();
        message.setData(bundle);
        Request.Mode mode = getMode(request);
        ViewUtil.httpSendRequest(BaseConfig.getContext(), url, request.getParam(), this, headers, request.getWhat(), message, bundle, mode == Request.Mode.GET ? "get" : "post");
    }

    private void loadB2B(Request request, OnHttpResultListener listener) throws Exception {
        Map<String, Object> param = getParam(request);
        param.put("enuu", CommonUtil.getSharedPreferences(BaseConfig.getContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getEmcode( ));
        LinkedHashMap<String, Object> headers = getHeaders(request);
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        Bundle bundle = getBundle(request);
        if (listener != null)
            bundle.putSerializable("listener", listener);
        Message message = Message.obtain();
        message.setData(bundle);
        int what = request.getWhat();
        Request.Mode mode = getMode(request);
        ViewUtil.httpSendRequest(BaseConfig.getContext(), request.getUrl(), param, this, headers, what, message, bundle, mode == Request.Mode.GET ? "get" : "post");
    }

    private Map<String, Object> getParam(Request request) throws Exception {
        Map<String, Object> param = request.getParam();
        if (param == null)
            param = new HashMap<>();
        return param;
    }

    private LinkedHashMap<String, Object> getHeaders(Request request) throws Exception {
        LinkedHashMap<String, Object> headers = request.getHeaders();
        if (headers == null)
            headers = new LinkedHashMap<>();
        return headers;
    }

    private Bundle getBundle(Request request) throws Exception {
        Bundle bundle = request.getBundle();
        if (bundle == null)
            bundle = new Bundle();
        return bundle;
    }

    private Request.Mode getMode(Request request) throws Exception {
        Request.Mode mode = request.getMode();
        if (mode == null) mode = Request.Mode.POST;
        return mode;
    }


}
