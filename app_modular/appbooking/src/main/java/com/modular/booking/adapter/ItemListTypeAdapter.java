package com.modular.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.modular.booking.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arison on 2017/9/11.
 */

public class ItemListTypeAdapter extends BaseAdapter {

    private List<JSONObject> objects = new ArrayList<JSONObject>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemListTypeAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public List<JSONObject> getObjects() {
        return objects;
    }

    public void setObjects(List<JSONObject> objects) {
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_type, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews( getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(JSONObject object, ViewHolder holder) {
         holder.tvNameValue.setText(object.getString("ad_bman"));
         holder.tvCompanyValue.setText(object.getString("ad_bcompany"));
         holder.tvStatus.setText(object.getString("ad_confirmstatus"));
         if ("未确认".equals(object.getString("ad_confirmstatus"))){
             holder.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
         }else if ("已确认".equals(object.getString("ad_confirmstatus"))){
             holder.tvStatus.setTextColor(context.getResources().getColor(R.color.titleBlue));
         }
         if (!StringUtil.isEmpty(object.getString("ad_reason"))&&!"null".equals(object.getString("ad_reason"))){
             holder.tvReasonValue.setText(object.getString("ad_reason"));
             holder.tvReason.setVisibility(View.VISIBLE);
             holder.tvReasonValue.setVisibility(View.VISIBLE);
         }else{
             holder.tvReason.setVisibility(View.GONE);
             holder.tvReasonValue.setVisibility(View.GONE);
         }
    }

    protected class ViewHolder {
        private TextView tvName;
        private TextView tvNameValue;
        private TextView tvStatus;
        private TextView tvCompany;
        private TextView tvCompanyValue;
        private TextView tvReason;
        private TextView tvReasonValue;
        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvNameValue = (TextView) view.findViewById(R.id.tvNameValue);
            tvStatus = (TextView) view.findViewById(R.id.tv_status);
            tvCompany = (TextView) view.findViewById(R.id.tvCompany);
            tvCompanyValue = (TextView) view.findViewById(R.id.tvCompanyValue);

            tvReason =(TextView) view.findViewById(R.id.tvReason);
            tvReasonValue =(TextView) view.findViewById(R.id.tvReasonValue);
        }
    }
}

