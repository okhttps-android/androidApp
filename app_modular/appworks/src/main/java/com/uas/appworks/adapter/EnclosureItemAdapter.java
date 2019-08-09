package com.uas.appworks.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.CityIndustryEnclosureBean;

import java.util.ArrayList;
import java.util.List;

public class EnclosureItemAdapter extends BaseAdapter {
    private List<CityIndustryEnclosureBean> objects = new ArrayList<CityIndustryEnclosureBean>();

    private Context context;
    private LayoutInflater layoutInflater;
    private OnEnclosureItemClickListener mOnEnclosureItemClickListener;

    public EnclosureItemAdapter(Context context, List<CityIndustryEnclosureBean> objects) {
        this.context = context;
        this.objects = objects;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setOnEnclosureItemClickListener(OnEnclosureItemClickListener onEnclosureItemClickListener) {
        mOnEnclosureItemClickListener = onEnclosureItemClickListener;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public CityIndustryEnclosureBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_enclosure_layout, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((CityIndustryEnclosureBean) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(CityIndustryEnclosureBean object, ViewHolder holder, final int position) {
        holder.itemEnclosureTv.setText(Html.fromHtml("&nbsp&nbsp&nbsp&nbsp&nbsp " + "<u>" + object.getFp_name() + "</u>"));
        holder.itemEnclosureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEnclosureItemClickListener != null) {
                    mOnEnclosureItemClickListener.onEnclosureClick(v, position);
                }
            }
        });
    }

    protected class ViewHolder {
        private TextView itemEnclosureTv;

        public ViewHolder(View view) {
            itemEnclosureTv = (TextView) view.findViewById(R.id.item_enclosure_tv);
        }
    }

    public interface OnEnclosureItemClickListener {
        void onEnclosureClick(View view, int position);
    }

}
