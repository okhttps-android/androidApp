package com.core.net.http.http;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by Bitliker on 2017/4/7.
 */

public interface OnHttpResultListener extends Serializable {

    /**
     * 接口返回
     *
     * @param what    请求的what
     * @param isJSON  是否是json数据
     * @param message 返回内容
     * @param bundle  返回内容,里面包含请求接口时候上传上去的参数
     */
    void result(int what, boolean isJSON, String message, Bundle bundle);

    /**
     * 请求错误返回
     *
     * @param what    请求what
     * @param message 错误信息
     */
    void error(int what, String message, Bundle bundle);
}
