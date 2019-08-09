package com.core.base.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.core.app.R;
import com.core.utils.StatusBarUtil;
import com.core.utils.ToastUtil;


public abstract class SupportToolBarFragment extends Fragment {
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    private View rootView;
    private FrameLayout contentFl;
    protected AppCompatActivity ct;
    private boolean mInited;
    private Toolbar commonToolBar;
    private TextView commonTitleTv;
    private boolean createView;

    /*防止内存重启*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        setHasOptionsMenu(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isHidden()) {
            mInited = true;
            onCreateView(savedInstanceState, createView);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!mInited && !hidden) {
            mInited = true;
            onCreateView(null, createView);
        }
    }

    /*防止内存重启出现重叠*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        createView = true;
        if (rootView != null) {//有缓存,不创建
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            createView = false;
        } else {
            rootView = inflater.inflate(R.layout.base_bar_layout, container, false);
            contentFl = rootView.findViewById(R.id.contentFl);
            int layoutId = inflateLayoutId();
            if (layoutId > 0) {
                View contentView = inflater.inflate(layoutId, container, false);
                contentFl.addView(contentView);
            }
            if (needCommonToolBar()) {
                initCommonToolbar();
            }
        }

        return rootView;
    }

    public FrameLayout getContentView() {
        return contentFl;
    }


    public void showToact(int resId) {
        ToastUtil.showToast(ct, resId, contentFl);
    }

    public void showToact(CharSequence message) {
        ToastUtil.showToast(ct, message, contentFl);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ct = (AppCompatActivity) context;
    }

    protected <T extends View> T findViewById(int id) {
        if (contentFl == null) {
            return null;
        } else {
            return contentFl.findViewById(id);
        }
    }

    //是否需要通用的toolbar
    public boolean needCommonToolBar() {
        return true;
    }

    public void initCommonToolbar() {
        ViewStub stub = rootView.findViewById(R.id.toolbarVs);
        if (stub != null) {
            stub.inflate();
            commonToolBar = rootView.findViewById(R.id.commonToolBar);
            commonTitleTv = rootView.findViewById(R.id.commonTitleTv);
            if (commonToolBar != null) {
                StatusBarUtil.immersive(ct, 0x00000000, 0.0f);
                ct.setSupportActionBar(commonToolBar);
                ct.getSupportActionBar().setDisplayShowTitleEnabled(false);
                StatusBarUtil.setPaddingSmart(ct, commonToolBar);
            }
            commonTitleTv.setText(ct != null && ct.getTitle() != null ? ct.getTitle() : "");
        }
    }


    public void setTitle(int resId) {
        if (commonTitleTv != null) {
            commonTitleTv.setText(resId);
        }
        if (ct != null && ct.getSupportActionBar() != null) {
            ct.setTitle(resId);
        }
    }

    public void setTitle(CharSequence resId) {
        if (commonTitleTv != null) {
            commonTitleTv.setText(resId);
        }
        if (ct != null && ct.getSupportActionBar() != null) {
            ct.setTitle(resId);
        }
    }

    public Toolbar getCommonToolBar() {
        return commonToolBar;
    }

    protected abstract int inflateLayoutId();

    protected abstract void onCreateView(Bundle savedInstanceState, boolean createView);

    @Override
    public void onStart() {
        super.onStart();
       // AndroidBug5497Workaround.assistActivity(ct);
    }
}
