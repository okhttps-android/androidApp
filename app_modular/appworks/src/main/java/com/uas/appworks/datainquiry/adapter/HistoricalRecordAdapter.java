package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.dao.historical.HistoricalRecordBean;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询历史记录列表适配器
 */
public class HistoricalRecordAdapter extends BaseAdapter {

    private List<HistoricalRecordBean> objects = new ArrayList<HistoricalRecordBean>();

    private Context context;
    private LayoutInflater layoutInflater;

    public HistoricalRecordAdapter(Context context, List<HistoricalRecordBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public List<HistoricalRecordBean> getObjects() {
        return objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public HistoricalRecordBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_historical_record, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((HistoricalRecordBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(HistoricalRecordBean object, ViewHolder holder) {
        holder.itemHistoricalRecordTv.setText(object.getSearchField());
    }

    protected class ViewHolder {
        private TextView itemHistoricalRecordTv;

        public ViewHolder(View view) {
            itemHistoricalRecordTv = (TextView) view.findViewById(R.id.item_historical_record_tv);
        }
    }
}
