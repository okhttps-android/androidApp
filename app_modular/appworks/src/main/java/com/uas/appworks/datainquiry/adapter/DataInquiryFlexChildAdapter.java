package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.datainquiry.bean.DataInquiryFlexBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询列表伸缩菜单子适配器
 */
public class DataInquiryFlexChildAdapter extends BaseAdapter {

    private List<DataInquiryFlexBean.RowBean> objects = new ArrayList<DataInquiryFlexBean.RowBean>();

    private Context context;
    private LayoutInflater layoutInflater;

    public DataInquiryFlexChildAdapter(Context context, List<DataInquiryFlexBean.RowBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public DataInquiryFlexBean.RowBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry_child, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((DataInquiryFlexBean.RowBean) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(DataInquiryFlexBean.RowBean object, ViewHolder holder, int position) {
        if (position == getCount() - 1 && getCount() > 2) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(holder.itemDataInquiryChildCaptionTv1.getLayoutParams());
            layoutParams.setMargins(0, 0, 0, 2);
            holder.itemDataInquiryChildCaptionTv1.setLayoutParams(layoutParams);
        }
        if (object.getRowChildBeans().size() == 1) {
            holder.itemDataInquiryChildLl2.setVisibility(View.GONE);

            String caption = object.getRowChildBeans().get(0).getCaption();
            if ("设备编号".equals(caption)) {
                holder.itemDataInquiryChildValueTv1.setTextColor(context.getResources().getColor(R.color.md_material_blue_600));
            } else {
                holder.itemDataInquiryChildValueTv1.setTextColor(context.getResources().getColor(R.color.data_inquiry_value_textcolor));
            }
            holder.itemDataInquiryChildCaptionTv1.setText(caption + ":");
            holder.itemDataInquiryChildValueTv1.setText(object.getRowChildBeans().get(0).getValue());
        } else if (object.getRowChildBeans().size() == 2) {
            holder.itemDataInquiryChildLl2.setVisibility(View.VISIBLE);

            String caption0 = object.getRowChildBeans().get(0).getCaption();
            if ("设备编号".equals(caption0)) {
                holder.itemDataInquiryChildValueTv1.setTextColor(context.getResources().getColor(R.color.md_material_blue_600));
            } else {
                holder.itemDataInquiryChildValueTv1.setTextColor(context.getResources().getColor(R.color.data_inquiry_value_textcolor));
            }
            holder.itemDataInquiryChildCaptionTv1.setText(caption0 + ":");
            holder.itemDataInquiryChildValueTv1.setText(object.getRowChildBeans().get(0).getValue());

            String caption1 = object.getRowChildBeans().get(1).getCaption();
            if ("设备编号".equals(caption1)) {
                holder.itemDataInquiryChildValueTv2.setTextColor(context.getResources().getColor(R.color.md_material_blue_600));
            } else {
                holder.itemDataInquiryChildValueTv2.setTextColor(context.getResources().getColor(R.color.data_inquiry_value_textcolor));
            }
            holder.itemDataInquiryChildCaptionTv2.setText(caption1 + ":");
            holder.itemDataInquiryChildValueTv2.setText(object.getRowChildBeans().get(1).getValue());
        }
    }

    protected class ViewHolder {
        private LinearLayout itemDataInquiryChildLl1;
        private LinearLayout itemDataInquiryChildLl2;
        private TextView itemDataInquiryChildCaptionTv1;
        private TextView itemDataInquiryChildValueTv1;
        private TextView itemDataInquiryChildCaptionTv2;
        private TextView itemDataInquiryChildValueTv2;

        public ViewHolder(View view) {
            itemDataInquiryChildLl1 = (LinearLayout) view.findViewById(R.id.item_data_inquiry_child_ll1);
            itemDataInquiryChildLl2 = (LinearLayout) view.findViewById(R.id.item_data_inquiry_child_ll2);
            itemDataInquiryChildCaptionTv1 = (TextView) view.findViewById(R.id.item_data_inquiry_child_caption_tv1);
            itemDataInquiryChildValueTv1 = (TextView) view.findViewById(R.id.item_data_inquiry_child_value_tv1);
            itemDataInquiryChildCaptionTv2 = (TextView) view.findViewById(R.id.item_data_inquiry_child_caption_tv2);
            itemDataInquiryChildValueTv2 = (TextView) view.findViewById(R.id.item_data_inquiry_child_value_tv2);
        }
    }
}
