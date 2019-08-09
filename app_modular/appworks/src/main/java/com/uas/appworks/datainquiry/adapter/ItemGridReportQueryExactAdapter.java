package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.datainquiry.bean.ReportConditionBean;

import java.util.ArrayList;
import java.util.List;

public class ItemGridReportQueryExactAdapter extends BaseAdapter {
    private List<ReportConditionBean.Property> objects = new ArrayList<ReportConditionBean.Property>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemGridReportQueryExactAdapter(Context context, List<ReportConditionBean.Property> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public ReportConditionBean.Property getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_grid_data_inquiry_exact, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((ReportConditionBean.Property) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(ReportConditionBean.Property object, ViewHolder holder) {
        holder.itemDataInquiryExactGridTv.setText(object.getDisplay());
        if (object.isState()) {
            holder.itemDataInquiryExactGridTv.setTextColor(context.getResources().getColor(R.color.data_inquiry_gird_menu_color1));
        } else {
            holder.itemDataInquiryExactGridTv.setTextColor(context.getResources().getColor(android.R.color.black));
        }
    }

    protected class ViewHolder {
        private TextView itemDataInquiryExactGridTv;

        public ViewHolder(View view) {
            itemDataInquiryExactGridTv = (TextView) view.findViewById(R.id.item_data_inquiry_exact_grid_tv);
        }
    }
}
