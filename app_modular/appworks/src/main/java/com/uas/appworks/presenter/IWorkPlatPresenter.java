package com.uas.appworks.presenter;

import android.content.Context;

import com.core.base.presenter.BasePresenter;
import com.me.network.app.base.HttpParams;
import com.uas.appworks.view.WorkPlatView;


/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/9 15:41
 */

public interface IWorkPlatPresenter extends BasePresenter<WorkPlatView> {

    void uasRequest(Context context, HttpParams httpParams);

    void cityRequest(Context context, HttpParams httpParams);
}
