package com.uas.appworks.presenter;

import android.content.Context;

import com.core.app.Constants;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.uas.appworks.model.WorkPlatModel;
import com.uas.appworks.view.WorkPlatView;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/9 14:27
 */

public class WorkPlatPresenter implements IWorkPlatPresenter, HttpCallback<String> {
    private WorkPlatView mWorkView;
    private WorkPlatModel mWorkModel;

    @Override
    public void attachView(WorkPlatView view) {
        mWorkView = view;
        mWorkModel = new WorkPlatModel();
    }

    @Override
    public void uasRequest(Context context, HttpParams httpParams) {
        if (mWorkView != null) {
            mWorkView.showLoading(null);
        }

        mWorkModel.uasRequest(context, httpParams, this);
    }

    @Override
    public void cityRequest(Context context, HttpParams httpParams) {
        if (mWorkView != null) {
            mWorkView.showLoading(null);
        }

        mWorkModel.cityRequest(context, httpParams, this);
    }

    @Override
    public void detachView() {
        mWorkView = null;
    }


    @Override
    public void onSuccess(int what, String s) {
        if (mWorkView != null) {
            mWorkView.hideLoading();
            if (what == Constants.LOAD_WORK_MENU_CACHE) {
                mWorkView.requestSuccess(what, mWorkModel.getWorkData());
            } else {
                mWorkView.requestSuccess(what, s);
            }
        }
    }

    @Override
    public void onFail(int what, String failStr) {
        if (mWorkView != null) {
            mWorkView.hideLoading();
            mWorkView.requestError(what, failStr);
        }
    }

}
