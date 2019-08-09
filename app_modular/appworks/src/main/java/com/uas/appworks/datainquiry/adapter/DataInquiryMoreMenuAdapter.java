package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.widget.CircleTextView;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询九宫格【更多】菜单适配器
 */
public class DataInquiryMoreMenuAdapter extends BaseAdapter {
    private List<String> objects = new ArrayList<String>();

    private int mColor = -1;

    private Context context;
    private LayoutInflater layoutInflater;

    public DataInquiryMoreMenuAdapter(Context context, List<String> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public String getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry_more_menu, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((String) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(String object, ViewHolder holder) {
        holder.itemDataInquiryMoreMenuTitle.setText(object);
        holder.itemDataInquiryMoreMenuIcon.setText(object.substring(0, 1));
        if (mColor != -1) {
            holder.itemDataInquiryMoreMenuIcon.setMyBackgroundColor(context.getResources().getColor(mColor));
        }
    }

    protected class ViewHolder {
        private CircleTextView itemDataInquiryMoreMenuIcon;
        private TextView itemDataInquiryMoreMenuTitle;

        public ViewHolder(View view) {
            itemDataInquiryMoreMenuIcon = (CircleTextView) view.findViewById(R.id.item_data_inquiry_more_menu_icon);
            itemDataInquiryMoreMenuTitle = (TextView) view.findViewById(R.id.item_data_inquiry_more_menu_title);
        }
    }
}
