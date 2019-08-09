package com.modular.appmessages.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.modular.appmessages.R;
import com.modular.appmessages.model.BusinessStatisticsBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe 商业统计指标列表适配器
 * @date 2017/11/3 13:47
 */

public class BusinessTargetsAdapter extends RecyclerView.Adapter<BusinessTargetsAdapter.BusinessTargetsViewHolder> {
    private Context mContext;
    private List<BusinessStatisticsBean.TargetsBean> mTargetsBeen;
    private OnItemClickListener mOnItemClickListener;

    public BusinessTargetsAdapter(Context context, List<BusinessStatisticsBean.TargetsBean> targetsBeen) {
        mContext = context;
        mTargetsBeen = targetsBeen;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public BusinessTargetsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View businessTargetView = LayoutInflater.from(mContext).inflate(R.layout.item_business_targets, parent, false);
        return new BusinessTargetsViewHolder(businessTargetView);
    }

    @Override
    public void onBindViewHolder(BusinessTargetsViewHolder holder, final int position) {
        holder.nameTextView.setText(mTargetsBeen.get(position).getTargetName());

        if (mOnItemClickListener != null) {
            holder.nameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTargetsBeen.size();
    }

    public static class BusinessTargetsViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;

        public BusinessTargetsViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.item_business_targets_name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
