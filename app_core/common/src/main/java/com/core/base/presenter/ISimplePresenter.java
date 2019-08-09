package com.core.base.presenter;

import android.content.Context;

import com.core.base.view.SimpleView;
import com.me.network.app.base.HttpParams;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/5/4 9:48
 */
public interface ISimplePresenter extends BasePresenter<SimpleView> {

    void httpRequest(Context context, String host, HttpParams httpParams);

}
