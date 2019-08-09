package com.modular.apputils.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.core.base.BaseActivity;
import com.core.base.EasyFragment;

/**
 * 时间：2017/10/30 17:18
 * 功能介绍：viewpager 懒加载
 */
public abstract class ViewPagerLazyFragment extends EasyFragment {

    private boolean isVisible = false;//当前Fragment是否可见
    private boolean isInitView = false;//是否与View建立起映射关系
    private boolean isFirstLoad = true;//是否是第一次加载数据

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getmRootView() != null) {
            if (getmRootView().getParent() != null) {
                ((ViewGroup) getmRootView().getParent()).removeView(getmRootView());
            }
        }
        isInitView = true;
        lazyLoadData();
        return getmRootView();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            isVisible = true;
            lazyLoadData();
        } else {
            isVisible = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void lazyLoadData() {
        if (!isFirstLoad || !isVisible || !isInitView) {
            onCreateView(null,false);
        } else {
            LazyData();
            isFirstLoad = false;
        }
    }

    public void showProgress() {
        if (ct instanceof BaseActivity && ((BaseActivity) ct).progressDialog != null) {
            ((BaseActivity) ct).progressDialog.show();
        }
    }

    public void dismissProgress() {
        if (ct instanceof BaseActivity && ((BaseActivity) ct).progressDialog != null && ((BaseActivity) ct).progressDialog.isShowing()) {
            ((BaseActivity) ct).progressDialog.dismiss();
        }
    }

    /**
     * 加载要显示的数据
     */
    protected abstract void LazyData();




    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {

    }

}
