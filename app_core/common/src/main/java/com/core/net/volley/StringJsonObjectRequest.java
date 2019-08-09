package com.core.net.volley;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.common.hmac.HmacUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 通过字符串参数集，请求json参数，并序列号为JsonModel对象
 *
 * @param <T>
 * @author dty
 */
public class StringJsonObjectRequest<T> extends Request<String> {

    private Listener<T> mListener;
    private Class<T> mClazz;
    private Map<String, String> mParams;
    private boolean mGzipEnable = false;
    private boolean mHmacEnable = false;

    /**
     * 请求方式post
     *
     * @param url      url地址
     * @param listener
     */
    public StringJsonObjectRequest(String url, ErrorListener errorListener, Listener<T> listener, Class<T> clazz, Map<String, String> params) {
        this(Method.POST, url, errorListener, listener, clazz, params);
    }

    /**
     * @param method   请求方式，post或者get
     * @param url      url地址
     * @param listener
     */
    public StringJsonObjectRequest(int method, String url, ErrorListener errorListener, Listener<T> listener, Class<T> clazz,
                                   Map<String, String> params) {
        super(method, url, errorListener);
        mListener = listener;
        mClazz = clazz;
        mParams = params;
        if (method == Method.GET) {
            spliceGetUrl();
        }
    }

    /**
     * @param method   请求方式，post或者get   是否启用Hmac加密
     * @param url      url地址
     * @param listener
     */
    public StringJsonObjectRequest(int method, String url, ErrorListener errorListener, Listener<T> listener, Class<T> clazz,
                                   Map<String, String> params, boolean mHmacEnable) {
        super(method, url, errorListener);
        mListener = listener;
        mClazz = clazz;
        mParams = params;
        this.mHmacEnable = mHmacEnable;
        if (method == Method.GET) {
            spliceGetUrl();
        }
    }

    public void setGzipEnable(boolean eanble) {
        mGzipEnable = eanble;
    }

    /**
     * @注释：是否加密
     */
    public void setmHmacEnable(boolean mHmacEnable) {
        this.mHmacEnable = mHmacEnable;
    }

    /* Post 参数设置 */
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if (getMethod() != Method.POST && getMethod() != Method.PUT) {
            return null;
        }
        if (FastVolley.DEBUG) {
            Log.d(FastVolley.TAG, "url:" + getUrl());
            if (mParams != null) {
                for (String key : mParams.keySet()) {
                    Log.d(FastVolley.TAG, "key:" + key + " ------  " + "value:" + mParams.get(key));
                }
            }
        }
        return mParams;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mGzipEnable) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Charset", "UTF-8");
            headers.put("Content-Type", "application/json");
            headers.put("Accept-Encoding", "gzip,deflate");
            if (mParams != null) {
                headers.put("Cookie", "JSESSIONID=" + mParams.get("sessionId"));
                Log.i("downloadCompanysContact", "JSESSIONID=" + mParams.get("sessionId"));
            }

            //headers.put("Accept-Encoding", "gzip,deflate");
            return headers;
        } else {
            return super.getHeaders();
        }
    }

    /* Get 参数拼接 */
    private void spliceGetUrl() {
        if (mParams != null && mParams.size() > 0) {
            String url = getUrl();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            if (url != null && !url.contains("?")) {
                url += "?";
            }
            String param = url;
            for (String key : mParams.keySet()) {
                param += (key + "=" + mParams.get(key) + "&");
            }

            if (mHmacEnable) {//是否加密
                param += "_timestamp=" + System.currentTimeMillis();
                param += "&_signature=" + HmacUtils.encode(param);
                Log.i("HmacHttp", "url:" + param);
            } else {
                param = param.substring(0, param.length() - 1);// 去掉最后一个&
            }
            setUrl(param);
        }
    }

    @Override
    protected void deliverResponse(String arg0) {
        Log.i("deliverResponse", "result deliverResponse:" + arg0);
        if (mListener == null) {
            return;
        }
        if (FastVolley.DEBUG) {
            Log.d(FastVolley.TAG, "StringJsonRequest deliverResponse:" + arg0);
        }
      //  MyApplication.getInstance().isNetworkActive()

        if (TextUtils.isEmpty(arg0)) {
            deliverError(new VolleyError(new NetworkError()));
            return;
        }
        ObjectResult<T> result = new ObjectResult<T>();
        result.setResultData(arg0);
        try {
            JSONObject jsonObject = JSON.parseObject(arg0);//解析json有异常的风险
            result.setResultCode(jsonObject.getIntValue(Result.RESULT_CODE));
            result.setResultMsg(jsonObject.getString(Result.RESULT_MSG));
//			开始解析数据
            if (!mClazz.equals(Void.class)) {
                String data = jsonObject.getString(Result.DATA);
                if (!TextUtils.isEmpty(data)) {
                    if (mClazz.equals(String.class) || mClazz.getSuperclass().equals(Number.class)) {// String
                        result.setData(castValue(mClazz, data));
                    } else {
                        result.setData(JSON.parseObject(data, mClazz));
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("exception", "JSON解析异常");
        }

        mListener.onResponse(result);
    }

    private T castValue(Class<T> clazz, String data) {
        try {
            Constructor<T> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(data);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Log.i("parseNetworkResponse", "result parseNetworkResponse:" + new String(response.data));
        String parsed;
        try {
            if (mGzipEnable) {
                parsed = getRealString(response.data);
            } else {
                parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            }
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    private int getShort(byte[] data) {
        return (data[0] << 8) | data[1] & 0xFF;
    }

    /**
     * GZip解压缩
     */
    private String getRealString(byte[] data) {
        byte[] h = new byte[2];
        h[0] = (data)[0];
        h[1] = (data)[1];
        int head = getShort(h);
        boolean t = head == 0x1f8b;
        InputStream in;
        StringBuilder sb = new StringBuilder();
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            if (t) {
                in = new GZIPInputStream(bis);
            } else {
                in = bis;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                sb.append(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public interface Listener<T> {
        void onResponse(ObjectResult<T> result);
    }

}
