package com.modular.appmessages.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.core.widget.MyListView;
import com.modular.appmessages.R;
import com.modular.appmessages.model.BusinessStatisticsBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/3 15:37
 */

public class BusinessTargetsDetailAdapter extends RecyclerView.Adapter<BusinessTargetsDetailAdapter.BusinessTargetsDetailViewHolder> {
    private Context mContext;
    private List<List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean>> mTargetDetailsBeen;
    private BusinessTargetsChildDetailAdapter mBusinessTargetsChildDetailAdapter;

    public BusinessTargetsDetailAdapter(Context context, List<List<BusinessStatisticsBean.TargetsBean.TargetDetailsBean>> targetDetailsBeen) {
        mContext = context;
        mTargetDetailsBeen = targetDetailsBeen;
    }

    @Override
    public BusinessTargetsDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_business_targets_detail, parent, false);
        return new BusinessTargetsDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusinessTargetsDetailViewHolder holder, int position) {
        mBusinessTargetsChildDetailAdapter = new BusinessTargetsChildDetailAdapter(mContext, mTargetDetailsBeen.get(position));
        holder.mMyListView.setAdapter(mBusinessTargetsChildDetailAdapter);
    }

    @Override
    public int getItemCount() {
        return mTargetDetailsBeen.size();
    }

    public static class BusinessTargetsDetailViewHolder extends RecyclerView.ViewHolder {
        private MyListView mMyListView;

        public BusinessTargetsDetailViewHolder(View itemView) {
            super(itemView);
            mMyListView = (MyListView) itemView.findViewById(R.id.item_business_targets_detail_lv);
        }
    }
}
