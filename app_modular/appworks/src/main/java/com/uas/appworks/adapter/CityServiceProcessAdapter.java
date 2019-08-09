package com.uas.appworks.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.CityServiceProcessBean;

import java.util.ArrayList;
import java.util.List;

public class CityServiceProcessAdapter extends BaseAdapter {

    private List<CityServiceProcessBean> objects = new ArrayList<CityServiceProcessBean>();

    private Context context;
    private LayoutInflater layoutInflater;

    public CityServiceProcessAdapter(Context context, List<CityServiceProcessBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public CityServiceProcessBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_approval_node, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((CityServiceProcessBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(CityServiceProcessBean object, ViewHolder holder) {
        holder.padding.setVisibility(View.GONE);
        if (TextUtils.isEmpty(object.getTime())) {
            holder.timeTv.setText("");
            holder.valuesTv.setText("");
            holder.statusIV.setImageResource(R.drawable.daishenpi);
        } else {
            holder.timeTv.setText(object.getTime());
            holder.valuesTv.setText("已完成");
            holder.statusIV.setImageResource(R.drawable.node_finished3);
        }
        holder.handIv.setImageResource(R.drawable.defaultpic);
        holder.keyTv.setText(object.getStatus());
    }

    protected class ViewHolder {
        private View padding;
        private ImageView statusIV;
        private ImageView handIv;
        private ImageView changeUser;
        private TextView timeTv;
        private TextView keyTv;
        private TextView valuesTv;

        public ViewHolder(View view) {
            padding = (View) view.findViewById(R.id.padding);
            statusIV = (ImageView) view.findViewById(R.id.statusIV);
            handIv = (ImageView) view.findViewById(R.id.handIv);
            changeUser = (ImageView) view.findViewById(R.id.changeUser);
            timeTv = (TextView) view.findViewById(R.id.timeTv);
            keyTv = (TextView) view.findViewById(R.id.keyTv);
            valuesTv = (TextView) view.findViewById(R.id.valuesTv);
        }
    }
}
