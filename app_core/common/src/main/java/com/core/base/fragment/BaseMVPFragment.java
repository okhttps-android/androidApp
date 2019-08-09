package com.core.base.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.LogUtil;
import com.core.app.R;
import com.core.base.presenter.BasePresenter;
import com.core.base.view.BaseView;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;


/**
 * @author RaoMeng
 * @describe MVP模式fragment基类
 * @date 2017/11/12 10:54
 */

public abstract class BaseMVPFragment<T extends BasePresenter> extends Fragment implements BaseView {
    protected T mPresenter;
    protected Context mContext;
    protected Bundle mBundle;

    /**
     * 是否是第一次加载
     */
    protected boolean isFirstLoad = true;

    /**
     * fragment视图View
     */
    private View mRootView;

    public CustomProgressDialog progressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mBundle = savedInstanceState.getBundle("fragment_bundle");
        } else {
            mBundle = getArguments() == null ? new Bundle() : getArguments();
        }

        mPresenter = initPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView != null) {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
            isFirstLoad = false;
        } else {
            mRootView = inflater.inflate(getLayout(), container, false);
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        LogUtil.i("onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        progressDialog = CustomProgressDialog.createDialog(mContext);
        initViews();
        initEvents();
        initDatas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBundle != null) {
            outState.putBundle("fragment_bundle", mBundle);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T $(int resId) {
        if (mRootView == null) {
            throw new NullPointerException("rootView is null");
        }
        return (T) mRootView.findViewById(resId);
    }

    /**
     * fragment进行回退
     */
    protected void onBack() {
        getFragmentManager().popBackStack();
    }

    protected void toast(String text) {
        ToastUtil.showToast(mContext, text);
    }

    protected void toast(int resId) {
        ToastUtil.showToast(mContext, resId);
    }

    /**
     * 跳转fragment
     *
     * @param tofragment
     */
    public void startFragment(Fragment tofragment) {
        startFragment(tofragment, null);
    }

    /**
     * @param tofragment 跳转的fragment
     * @param tag        fragment的标签
     */
    public void startFragment(Fragment tofragment, String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(this).add(android.R.id.content, tofragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    /**
     * [页面跳转]
     *
     * @param clz
     */
    public void startActivity(Class<?> clz) {
        startActivity(clz, null);
    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(mContext, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    /**
     * 隐式跳转
     *
     * @param action
     */
    public void startActivity(String action) {
        startActivity(action, null);
    }

    public void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        super.startActivity(intent, options);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * @param bundle
     * @param requestCode
     */
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(mContext, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    public void startActivityForResult(String action, int requestCode) {
        startActivityForResult(action, null, requestCode);
    }

    public void startActivityForResult(String action, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    protected abstract int getLayout();

    /**
     * 创建presenter
     *
     * @return
     */
    protected abstract T initPresenter();

    protected abstract void initViews();

    protected abstract void initEvents();

    protected abstract void initDatas();
}
