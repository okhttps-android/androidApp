package com.uas.appworks.model;

import android.content.Context;

import com.core.base.model.BaseModel;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.uas.appworks.model.bean.WorkMenuBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/9 14:51
 */

public interface IWorkPlatModel extends BaseModel {

    void uasRequest(Context context, HttpParams httpParams, HttpCallback workCallback);

    void cityRequest(Context context, HttpParams httpParams, HttpCallback workCallback);

    List<WorkMenuBean> getWorkData();
}
