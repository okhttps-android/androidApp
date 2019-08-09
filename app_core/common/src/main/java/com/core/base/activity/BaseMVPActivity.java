package com.core.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.core.base.BaseActivity;
import com.core.base.presenter.BasePresenter;
import com.core.base.view.BaseView;


/**
 * @author RaoMeng
 * @describe MVP模式Activity基类
 * @date 2017/11/9 14:20
 */

public abstract class BaseMVPActivity<T extends BasePresenter> extends BaseActivity implements BaseView {
    protected T mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        initView();

        mPresenter = initPresenter();

        if (mPresenter != null) {
            mPresenter.attachView(this);
        }

        initEvent();

        initData();
    }

    protected abstract int getLayout();

    protected abstract void initView();

    protected abstract T initPresenter();

    protected abstract void initEvent();

    protected abstract void initData();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

}
