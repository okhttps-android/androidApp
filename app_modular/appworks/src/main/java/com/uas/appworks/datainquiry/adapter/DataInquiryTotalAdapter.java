package com.uas.appworks.datainquiry.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.datainquiry.bean.DataInquiryTotalBean;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DataInquiryTotalAdapter extends BaseAdapter {
    private List<DataInquiryTotalBean> objects = new ArrayList<DataInquiryTotalBean>();
    private Handler mHandler;

    private Context context;
    private LayoutInflater layoutInflater;
    private NumberFormat mNumberFormat;

    public DataInquiryTotalAdapter(Context context, List<DataInquiryTotalBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
        mNumberFormat = NumberFormat.getInstance();
        mNumberFormat.setMaximumFractionDigits(2);
    }

    public List<DataInquiryTotalBean> getObjects() {
        return objects;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public DataInquiryTotalBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry_total, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((DataInquiryTotalBean) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(DataInquiryTotalBean object, ViewHolder holder, final int position) {
        holder.dataInquiryItemTotalCaption.setText(object.getCaption() + ":");
        holder.dataInquiryItemTotalValue.setText(mNumberFormat.format(object.getTotal()));

        holder.dataInquiryItemTotalDeleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                objects.remove(position);
                notifyDataSetChanged();
                if (objects.size() == 0 && mHandler != null) {
                    mHandler.sendEmptyMessage(0x88);
                }
            }
        });
    }

    protected class ViewHolder {
        private TextView dataInquiryItemTotalCaption;
        private TextView dataInquiryItemTotalValue;
        private ImageView dataInquiryItemTotalDeleteIv;

        public ViewHolder(View view) {
            dataInquiryItemTotalCaption = (TextView) view.findViewById(R.id.data_inquiry_item_total_caption);
            dataInquiryItemTotalValue = (TextView) view.findViewById(R.id.data_inquiry_item_total_value);
            dataInquiryItemTotalDeleteIv = (ImageView) view.findViewById(R.id.data_inquiry_item_total_delete_iv);
        }
    }
}
