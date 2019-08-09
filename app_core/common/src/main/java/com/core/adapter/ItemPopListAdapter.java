package com.core.adapter;

/**
 * Created by Arison on 2017/11/8.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.core.app.R;

import java.util.ArrayList;
import java.util.List;

public class ItemPopListAdapter extends BaseAdapter {

    private List<ItemsSelectType1> objects = new ArrayList<ItemsSelectType1>();
    private int selectId=0;
    private Context context;
    private LayoutInflater layoutInflater;

    public int getSelectId() {
        return selectId;
    }

    public void setSelectId(int selectId) {
        this.selectId = selectId;
    }

    public ItemPopListAdapter(Context context, List<ItemsSelectType1> data) {
        this.context = context;
        this.objects=data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public ItemsSelectType1 getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_pop_list_select, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        if (selectId==position) {
            convertView.setBackgroundResource(R.color.me_menu_item_press);
        }else{
            convertView.setBackgroundResource(android.R.color.transparent);
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag(),position);
        return convertView;
    }

    private void initializeViews(ItemsSelectType1 object, ViewHolder holder,int position) {
        holder.tvItemName.setText(object.getName());
        holder.checkBox.setFocusable(false);
        holder.checkBox.setClickable(false);
        holder.model=object;
    }

    public class ViewHolder {
        public TextView tvItemName;
        public CheckBox checkBox;
        public ItemsSelectType1 model;

        public ViewHolder(View view) {
            tvItemName = view.findViewById(R.id.tv_item_name);
            checkBox= view.findViewById(R.id.cb_select);
        }
    }
}
