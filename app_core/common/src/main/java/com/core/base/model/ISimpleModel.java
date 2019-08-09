package com.core.base.model;

import android.content.Context;

import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/5/4 9:53
 */
public interface ISimpleModel extends BaseModel {

    void httpRequest(Context context, String host, HttpParams httpParams, HttpCallback httpCallback);

}
