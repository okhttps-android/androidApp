package com.core.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.core.base.BaseActivity;
import com.core.base.presenter.MvpPresenter;
import com.core.base.view.MvpView;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 15:11
 */
public abstract class MvpBaseActivity<P extends MvpPresenter> extends BaseActivity implements MvpView {
    protected P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        mPresenter = initPresenter();

        if (mPresenter != null) {
            mPresenter.onMvpAttachView(this, savedInstanceState);
        }

        initView();

        initEvent();

        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter != null) {
            mPresenter.onMvpStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.onMvpResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.onMvpPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.onMvpStop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter != null) {
            mPresenter.onMvpSaveInstanceState(outState);
        }
    }

    protected abstract int getLayout();

    protected abstract void initView();

    protected abstract P initPresenter();

    protected abstract void initEvent();

    protected abstract void initData();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onMvpDetachView(false);
            mPresenter.onMvpDestroy();
        }
    }
}
