package com.modular.apputils.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.modular.apputils.R;
import com.modular.apputils.model.EasyBaseModel;

import java.util.List;

public abstract class EasyBaseAdapter<T> extends BaseAdapter {
    protected Context ct;
    private List<EasyBaseModel<T>> models;


    public EasyBaseAdapter(Context ct, List<EasyBaseModel<T>> models) {
        this.ct = ct;
        this.models = models;
    }

    public void updateModels(List<EasyBaseModel<T>> models) {
        this.models = models;
        notifyDataSetChanged();
    }

    public List<EasyBaseModel<T>> getModels() {
        return models;
    }

    @Override
    public int getCount() {
        return ListUtils.getSize(models);
    }

    @Override
    public Object getItem(int i) {
        return models.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(ct).inflate(getLayoutRes(), null);
        }
        bindView(view, i, models.get(i));
        return view;
    }


    public abstract View bindView(View view, int position, EasyBaseModel<T> model);


    public abstract @LayoutRes
    int getLayoutRes();
}
