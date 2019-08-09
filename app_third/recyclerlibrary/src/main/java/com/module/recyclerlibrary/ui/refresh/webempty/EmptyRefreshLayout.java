package com.module.recyclerlibrary.ui.refresh.webempty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshView;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshFooter;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshHeader;

/**
 * Created by Bitliker on 2017/9/19.
 */

public class EmptyRefreshLayout extends BaseRefreshLayout {

    public EmptyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected BaseRefreshView getHeader(LayoutInflater mInflater) {
        SimpleRefreshHeader baseRefreshView = new SimpleRefreshHeader(getContext(), mInflater);
        baseRefreshView.hintAll();
        return baseRefreshView;
    }


    @Override
    protected BaseRefreshView getFooter(LayoutInflater mInflater) {
        return new SimpleRefreshFooter(getContext(), mInflater);
    }
}
