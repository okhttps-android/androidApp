package com.uas.appworks.crm3_0.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.crm3_0.model.ItemModel;

import java.util.List;

/**
 * Created by Arison on 2018/9/14.
 */

public class DynamicAdapter extends BaseAdapter {

    private Context context;
    private List<Object> list;


    public DynamicAdapter(Context context, List<Object> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        int ret = 0;
        Object object = list.get(position);
        //通过数据类型的不同类来设置item的类型
        if (object instanceof String) {
            ret = 0;
        } else if (object instanceof ItemModel) {
            ret = 1;
        }
        return ret;
    }

    
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        int ret = 0;
        if (list != null) {
            ret = list.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == 0) {
            convertView = bindHead(position, convertView, parent);
        } else if (getItemViewType(position) == 1) {
            convertView = bindContent(position, convertView, parent);
        }
        return convertView;
    }

    private View bindContent(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_dynamic_content, null);
            holder = new ViewHolder();
            holder.key =   convertView.findViewById(R.id.tv_key);
            holder.value =  convertView.findViewById(R.id.tv_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object object = list.get(position);
        if (object instanceof ItemModel) {
           ItemModel model= (ItemModel) object;
               holder.key.setText(model.getKey());
               holder.value.setText(model.getValue());
               holder.columnModel=model;
        }
        return convertView;
    }

    private View bindHead(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_dynamic_head, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } 
//        else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        Object object = list.get(position);
//        if (object instanceof ListDynamicItemActivity.ItemModel) {
//            ListDynamicItemActivity.ItemModel model= (ListDynamicItemActivity.ItemModel) object;
//            holder.key.setText(model.getKey());
//            holder.value.setText(model.getValue());
//        }
        return convertView;
    }


   public class ViewHolder {
       public  TextView key;
       public  TextView value;
       public  String id;
       public  ItemModel columnModel;
    }
}
