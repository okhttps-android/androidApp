package com.modular.apputils.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.modular.apputils.R;

@Deprecated
public class EmptyRecyclerView extends RecyclerView {

    private View emptyView;
    private static final String TAG = "EmptyRecyclerView";

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            Log.i(TAG, "onItemRangeInserted" + itemCount);
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };
    private LayoutInflater mInflater;

    public EmptyRecyclerView(Context context) {
        super(context);
        initEmptyView(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEmptyView(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
        initEmptyView(context);
    }

    private void initEmptyView(Context context) {
        mInflater = LayoutInflater.from(context);
        emptyView = mInflater.inflate(R.layout.common_empty_view, null);
        post(new Runnable() {
            @Override
            public void run() {
                ((ViewGroup) getRootView()).addView(emptyView);
                emptyView.setVisibility(GONE);
            }
        });
    }



    private void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible =
                    getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }


    //设置没有内容时，提示用户的空布局
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}
