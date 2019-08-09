package com.me.network.app.base;

/**
 * @author RaoMeng
 * @describe 回调接口
 * @date 2017/11/9 14:47
 */

public interface HttpCallback<T> {

    void onSuccess(int flag, T t) throws Exception;

    void onFail(int flag, String failStr) throws Exception;
}
