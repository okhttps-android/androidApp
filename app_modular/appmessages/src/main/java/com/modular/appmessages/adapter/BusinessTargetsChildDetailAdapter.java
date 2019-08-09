package com.modular.appmessages.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.modular.appmessages.R;
import com.modular.appmessages.model.BusinessStatisticsBean;

import java.util.ArrayList;
import java.util.List;

public class BusinessTargetsChildDetailAdapter extends BaseAdapter {
    private Context context;
    private List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean> objects = new ArrayList<BusinessStatisticsBean.TargetsBean.TargetDetailsBean>();
    private LayoutInflater layoutInflater;

    public BusinessTargetsChildDetailAdapter(Context context, List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public BusinessStatisticsBean.TargetsBean.TargetDetailsBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_business_targets_child_detail, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((BusinessStatisticsBean.TargetsBean.TargetDetailsBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(BusinessStatisticsBean.TargetsBean.TargetDetailsBean object, ViewHolder holder) {
        holder.itemBusinessTargetsChildKeyTv.setText(object.getDetailName());
        holder.itemBusinessTargetsChildValueTv.setText(object.getDetailValue());
    }

    protected class ViewHolder {
        private TextView itemBusinessTargetsChildKeyTv;
        private TextView itemBusinessTargetsChildValueTv;

        public ViewHolder(View view) {
            itemBusinessTargetsChildKeyTv = (TextView) view.findViewById(R.id.item_business_targets_child_key_tv);
            itemBusinessTargetsChildValueTv = (TextView) view.findViewById(R.id.item_business_targets_child_value_tv);
        }
    }
}
