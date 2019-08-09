package com.modular.booking.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.modular.booking.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Arison on 2017/11/7.
 */
@Deprecated
public class SelectSimpleAdapter extends SimpleAdapter {
    private int mResource;
    private Context context;
    private Activity activity;
    private List<? extends Map<String, ?>> mData;

    public SelectSimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource) {
        super(context, data, resource, null, null);
        this.mResource = resource;
        this.context=context;
        this.activity=(Activity) context;
        this.mData = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        LayoutInflater layoutInflater =activity.getLayoutInflater();
        View view = layoutInflater.inflate(mResource, null);
        TextView text = (TextView) view.findViewById(R.id.tv_item_name);
        text.setText(mData.get(position).get("item_name").toString());
        if (position == 2) {
            text.setTextColor(activity.getResources().getColor(R.color.red));
            text.setSelected(true);
        }

        return view;
    }
}
