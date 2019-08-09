package com.core.base.presenter;

import android.content.Context;

import com.core.base.model.SimpleModel;
import com.core.base.view.SimpleView;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/5/4 9:47
 */
public class SimplePresenter implements ISimplePresenter, HttpCallback<String> {
    private SimpleModel mSimpleModel;
    private SimpleView mSimpleView;

    @Override
    public void attachView(SimpleView view) {
        mSimpleView = view;
        mSimpleModel = new SimpleModel();
    }

    @Override
    public void detachView() {
        mSimpleView = null;
    }

    @Override
    public void httpRequest(Context context, String host, HttpParams httpParams) {
        if (mSimpleView != null) {
            mSimpleView.showLoading(null);
        }

        if (mSimpleModel != null) {
            mSimpleModel.httpRequest(context, host, httpParams, this);
        }
    }

    @Override
    public void onSuccess(int what, String result) {
        if (mSimpleView != null) {
            mSimpleView.hideLoading();
            mSimpleView.requestSuccess(what, result);
        }
    }

    @Override
    public void onFail(int what, String failStr) {
        if (mSimpleView != null) {
            mSimpleView.hideLoading();
            mSimpleView.requestError(what, failStr);
        }
    }
}
