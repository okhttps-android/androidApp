package com.xzjmyk.pm.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.baidu.cyberplayer.utils.T;

import java.util.List;

public abstract class SimpleCustomAdapter extends BaseAdapter {
    private Context context;
    private List<T> beans;
    protected View rootView;
    private T viewHolder;

    public SimpleCustomAdapter(Context context) {
        this.context = context;
        beans = getBeans();
        viewHolder = (T) getViewHolder();
    }

    protected abstract List<T> getBeans();//

    protected abstract int count();//获取长度

    protected abstract int getTypeView();//获取视图id

    protected abstract Object getViewHolder();

    @Override
    public int getCount() {
        return count();
    }

    @Override
    public Object getItem(int i) {
        return beans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            rootView = view;
            view = LayoutInflater.from(context).inflate(getTypeView(), null);

        } else {
            viewHolder = (T) view.getTag();
        }
        doView(i, viewGroup);
        return view;
    }

    protected abstract void doView(int i, ViewGroup viewGroup);

    protected abstract void setViewHolders(Object tag);

    protected View findViewById(int id) {
        if (rootView == null) return null;
        else return rootView.findViewById(id);
    }

}
