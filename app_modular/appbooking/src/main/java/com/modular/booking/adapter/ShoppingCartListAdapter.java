package com.modular.booking.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.modular.booking.R;
import com.modular.booking.adapter.holder.ShoppingCartItemViewHolder;
import com.modular.booking.base.BaseAdapter;
import com.modular.booking.model.ShoppingEntity;


public class ShoppingCartListAdapter extends BaseAdapter<ShoppingEntity> {

    @Override
    public int getViewLayoutId(int viewType) {
        return R.layout.layout_shopping_cart_item;
    }

    @Override
    public ShoppingCartItemViewHolder createViewHolder(View view, int viewType) {
        return new ShoppingCartItemViewHolder(view);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder holder, ShoppingEntity entity, int position) {
        if (holder instanceof ShoppingCartItemViewHolder) {
            ((ShoppingCartItemViewHolder) holder).bind(entity);
        }
    }
}