package com.modular.booking.adapter;

/**
 * Created by Arison on 2017/11/30.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.modular.booking.R;
import com.modular.booking.model.SeatsStateModel;

import java.util.ArrayList;
import java.util.List;

public class ItemFoodStateAdapter extends BaseAdapter {

    private List<SeatsStateModel> objects = new ArrayList<SeatsStateModel>();

    private Context context;
    private LayoutInflater layoutInflater;

    public ItemFoodStateAdapter(Context context,List<SeatsStateModel> data) {
        this.context = context;
        this.objects=data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public SeatsStateModel getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_food_state, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((SeatsStateModel)getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(SeatsStateModel object, ViewHolder holder) {
       holder.tvSeatsCode.setText(object.getAd_deskcode());
       holder.model=object;
       if ("0".equals(object.getAd_status())){
           holder.tvSeatsCode.setBackgroundResource(R.drawable.shape_rounds_blue);
       }else{
           holder.tvSeatsCode.setBackgroundResource(R.drawable.icon_seats_lock);
           holder.tvSeatsCode.setText(" ");
       }
    }

    public class ViewHolder {
        private TextView tvSeatsCode;
        public SeatsStateModel model;

        public ViewHolder(View view) {
            tvSeatsCode = (TextView) view.findViewById(R.id.tvSeatsCode);
        }
    }
}
