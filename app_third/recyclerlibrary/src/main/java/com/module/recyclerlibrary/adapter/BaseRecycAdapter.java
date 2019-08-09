package com.module.recyclerlibrary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.module.recyclerlibrary.model.BaseModel;

import java.util.List;

/**
 * Created by Bitliker on 2017/9/19.
 */

public abstract class BaseRecycAdapter<T> extends RecyclerView.Adapter<BaseRecycAdapter.ViewHodler> {

    protected Context ct;
    protected List<BaseModel<T>> datas;

    public BaseRecycAdapter(Context ct) {
        this(ct, null);
    }

    public BaseRecycAdapter(Context ct, List<BaseModel<T>> datas) {
        if (ct == null) {
            new NullPointerException("ct cannot be null");
        }
        this.ct = ct;
        this.datas = datas;
    }

    @Override
    public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }


    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    public abstract void onBindViewHolder(ViewHodler holder, int position);

    public abstract void onCreateViewHolder(View view, int viewType);

    private LayoutInflater mInflater;

    private LayoutInflater getmInflater() {
        return mInflater == null ? (mInflater = LayoutInflater.from(ct)) : mInflater;
    }


    protected abstract class ViewHodler extends RecyclerView.ViewHolder {
        private ViewHodler(View itemView) {
            super(itemView);
        }

        public ViewHodler(int layoutId, ViewGroup parent) {
            this(getmInflater().inflate(layoutId, parent, false));
        }
    }
}
