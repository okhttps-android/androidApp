package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uas.appworks.R;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/23 17:52
 */

public class CityIndustryNewsListAdapter extends RecyclerView.Adapter<CityIndustryNewsListAdapter.MyViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;

    public CityIndustryNewsListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_city_industry_main_news, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
