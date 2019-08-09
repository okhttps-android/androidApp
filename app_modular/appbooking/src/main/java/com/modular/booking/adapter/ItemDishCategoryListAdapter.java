package com.modular.booking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.modular.booking.R;
import com.modular.booking.model.ProductCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arison on 2018/1/24.
 */

public class ItemDishCategoryListAdapter extends BaseAdapter {
   
    private List<ProductCategory> productCategories=new ArrayList<>();
    private Context ct;
    private LayoutInflater layoutInflater;
    private int selectIndex=0;
    public ItemDishCategoryListAdapter(Context ct,List<ProductCategory> datas){
        this.ct=ct;
        this.productCategories=datas;  
        this.layoutInflater = LayoutInflater.from(ct);
        
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
    }

    @Override
    public int getCount() {
        return productCategories.size();
    }

    @Override
    public ProductCategory getItem(int i) {
        return productCategories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_dish_catagory_list, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((ProductCategory)getItem(position), (ViewHolder) convertView.getTag(),position);

        if (position == selectIndex) {
            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#f3f3f3"));
        }
        return convertView;
    }

    private void initializeViews(ProductCategory object, ViewHolder holder,int position) {
        holder.tvName.setText(object.getName());
        holder.modle=object;
    }

    protected class ViewHolder {
        private TextView tvName;
        private RelativeLayout rlLayout;
        private ProductCategory modle;

        public ViewHolder(View view) {
            tvName = view.findViewById(R.id.tv_name);
            rlLayout=view.findViewById(R.id.rl_dish_top);
        }
    }
}
