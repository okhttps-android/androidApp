package com.modular.booking.adapter;

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

import com.modular.booking.R;
import com.modular.booking.model.ItemsSelectType1;

import java.util.ArrayList;
import java.util.List;

public class ItemPopListAdapter extends BaseAdapter {

    private List<ItemsSelectType1> objects = new ArrayList<ItemsSelectType1>();
    private int selectId=7;
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
            convertView.setBackgroundResource(android.R.color.white);
        }
        initializeViews((ItemsSelectType1)getItem(position), (ViewHolder) convertView.getTag(),position);
        return convertView;
    }

    private void initializeViews(ItemsSelectType1 object, ViewHolder holder,int position) {
       // holder.tvItemName.setSelected(object.isSelected());
        holder.tvItemName.setText(object.getName());
//       if (selectId==position) {
//           holder.checkBox.setChecked(true);
//       }else{
//           holder.checkBox.setChecked(false);
//       }
       holder.checkBox.setFocusable(false);
       holder.checkBox.setClickable(false);
    }

    public class ViewHolder {
//        private LinearLayout llPopTop;
        public TextView tvItemName;
        public CheckBox checkBox;

        public ViewHolder(View view) {
//            llPopTop = (LinearLayout) view.findViewById(R.id.ll_pop_top);
            tvItemName = (TextView) view.findViewById(R.id.tv_item_name);
            checkBox= (CheckBox) view.findViewById(R.id.cb_select);
        }
    }
}
