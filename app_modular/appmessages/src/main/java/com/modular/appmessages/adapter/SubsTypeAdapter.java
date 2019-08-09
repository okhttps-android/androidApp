package com.modular.appmessages.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.modular.appmessages.R;

import java.util.List;

/**
 * 未订阅页面订阅类别适配器
 * Created by RaoMeng on 2016/10/20.
 */
public class SubsTypeAdapter extends BaseAdapter {
    private List<String> keyStrings;
    private Context mContext;
    private int mSelectItem;

    public SubsTypeAdapter(Context mContext,List<String> keyStrings) {
        this.keyStrings = keyStrings;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return keyStrings.size();
    }

    @Override
    public Object getItem(int position) {
        return keyStrings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectItem(int mSelectItem){
        this.mSelectItem = mSelectItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = View.inflate(mContext, R.layout.list_subs_type,null);
            viewHolder = new ViewHolder();
            viewHolder.typeTextView = (TextView) convertView.findViewById(R.id.list_subs_type_tv);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.typeTextView.setText(keyStrings.get(position));
        if (mSelectItem == position){
            viewHolder.typeTextView.setSelected(true);
            viewHolder.typeTextView.setPressed(true);
            viewHolder.typeTextView.setTextColor(Color.RED);
            convertView.setBackgroundColor(Color.WHITE);
        }else {
            viewHolder.typeTextView.setSelected(false);
            viewHolder.typeTextView.setPressed(false);
            viewHolder.typeTextView.setTextColor(Color.BLACK);
            convertView.setBackgroundColor(Color.parseColor("#FFE2E0E0"));
        }
        return convertView;
    }

    class ViewHolder{
        TextView typeTextView;
    }
}
