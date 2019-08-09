package com.core.base.model;

import android.content.Context;
import android.text.TextUtils;

import com.core.app.R;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/5/4 9:52
 */
public class SimpleModel implements ISimpleModel {

    @Override
    public void httpRequest(Context context, String host, HttpParams httpParams, HttpCallback httpCallback) {
        final int what = httpParams.getFlag();
        if (!CommonUtil.isNetWorkConnected(context)) {
            if (httpCallback != null) {
                try {
                    httpCallback.onFail(what, context.getString(R.string.networks_out));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ToastUtil.showToast(context, R.string.networks_out);
            }
            return;
        }
        if (TextUtils.isEmpty(host)) {
            if (httpCallback != null) {
                try {
                    httpCallback.onFail(what, context.getString(R.string.host_null_please_login_retry));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ToastUtil.showToast(context, R.string.host_null_please_login_retry);
            }
            return;
        }
        HttpRequest.getInstance().sendRequest(host, httpParams, httpCallback);
    }

}
