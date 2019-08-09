package com.core.base.presenter;

import android.os.Bundle;

import com.core.base.view.MvpView;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 15:03
 */
public interface MvpPresenter<V extends MvpView> {

    void onMvpAttachView(V view, Bundle savedInstanceState);

    void onMvpStart();

    void onMvpResume();

    void onMvpPause();

    void onMvpStop();

    void onMvpDetachView(boolean retainInstance);

    void onMvpDestroy();

    void onMvpSaveInstanceState(Bundle savedInstanceState);
}
