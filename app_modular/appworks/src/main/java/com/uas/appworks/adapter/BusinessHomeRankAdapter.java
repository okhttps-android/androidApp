package com.uas.appworks.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessRankBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:50
 */
public class BusinessHomeRankAdapter extends BaseQuickAdapter<BusinessRankBean, BaseViewHolder> {

    public BusinessHomeRankAdapter(@Nullable List<BusinessRankBean> data) {
        super(R.layout.item_business_manage_rank, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessRankBean item) {
        int adapterPosition = helper.getAdapterPosition();
        if (adapterPosition == 0) {
            helper.setTextColor(R.id.item_business_rank_no_tv, mContext.getResources().getColor(R.color.business_rank_no1));
        } else if (adapterPosition == 1) {
            helper.setTextColor(R.id.item_business_rank_no_tv, mContext.getResources().getColor(R.color.business_rank_no2));
        } else if (adapterPosition == 2) {
            helper.setTextColor(R.id.item_business_rank_no_tv, mContext.getResources().getColor(R.color.business_rank_no3));
        } else {
            helper.setTextColor(R.id.item_business_rank_no_tv, mContext.getResources().getColor(R.color.business_rank_no4));
        }
        helper.setText(R.id.item_business_rank_no_tv, (adapterPosition + 1) + "");

        helper.setText(R.id.business_manage_rank_name_tv, item.getName());
        helper.setText(R.id.business_manage_rank_bnum_tv, item.getBnum()+"");
        helper.setText(R.id.business_manage_rank_winnum_tv, item.getSnun()+"");
        helper.setText(R.id.business_manage_rank_rate_tv, item.getSrates()+"");
    }
}
