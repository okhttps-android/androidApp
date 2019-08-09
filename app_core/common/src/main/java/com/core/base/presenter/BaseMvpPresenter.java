package com.core.base.presenter;

import android.os.Bundle;

import com.core.base.view.MvpView;

import java.lang.ref.WeakReference;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 14:48
 */
public class BaseMvpPresenter<V extends MvpView> implements MvpPresenter<V> {

    private WeakReference<V> mViewRef;

    protected V getMvpView() {
        return mViewRef.get();
    }

    protected boolean isViewAttached() {
        return mViewRef != null && getMvpView() != null;
    }

    @Override
    public void onMvpAttachView(V view, Bundle savedInstanceState) {
        mViewRef = new WeakReference<V>(view);

    }

    @Override
    public void onMvpStart() {

    }

    @Override
    public void onMvpResume() {

    }

    @Override
    public void onMvpPause() {

    }

    @Override
    public void onMvpStop() {

    }

    @Override
    public void onMvpDetachView(boolean retainInstance) {

        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    @Override
    public void onMvpDestroy() {

    }

    @Override
    public void onMvpSaveInstanceState(Bundle savedInstanceState) {

    }
}
