package com.me.network.app.http.logger;

import android.util.Log;

import com.me.network.app.http.interceptor.HttpLoggerInterceptor;

/**
 * @author RaoMeng
 * @describe 请求信息打印类
 * @date 2017/12/13 14:53
 */

public class HttpLogger implements HttpLoggerInterceptor.Logger {
    @Override
    public void log(String message) {
        if (message != null) {
            prinlnLongMsg("HttpLogger", message);
        }
    }

    public static void prinlnLongMsg(String TAG, String responseInfo) {
        if (responseInfo != null) {
            if (responseInfo.length() >=3000) {
                Log.v(TAG, "sb.length = " + responseInfo.length());
                int chunkCount = responseInfo.length() / 3000;     // integer division
                for (int i = 0; i <= chunkCount; i++) {
                    int max = 3000 * (i + 1);
                    if (max >= responseInfo.length()) {
                        Log.i(TAG, "【"+ i + "】" + responseInfo.substring(3000 * i));
                    } else {
                        Log.i(TAG, "【" + i+ "】" + responseInfo.substring(3000 * i, max));
                    }
                }
            } else {
                Log.d(TAG, "sb.length = " + responseInfo.length());
                Log.i(TAG, responseInfo.toString());
            }
        }

    }
}
