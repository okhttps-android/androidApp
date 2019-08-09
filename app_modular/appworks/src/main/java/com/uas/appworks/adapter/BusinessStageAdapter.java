package com.uas.appworks.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessStageBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/17 17:04
 */
public class BusinessStageAdapter extends BaseQuickAdapter<BusinessStageBean, BaseViewHolder> {
    private int currentPos = -1;

    public BusinessStageAdapter(@Nullable List<BusinessStageBean> data) {
        super(R.layout.item_business_stage, data);
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessStageBean item) {
        int adapterPosition = helper.getAdapterPosition();
        if (adapterPosition < currentPos) {
            helper.setBackgroundRes(R.id.item_business_stage_parent_ll, R.drawable.shape_b2b_change_account_past);
            helper.setTextColor(R.id.item_business_stage_no_tv, mContext.getResources().getColor(R.color.empty_text_color));
            helper.setTextColor(R.id.item_business_stage_name_tv, mContext.getResources().getColor(R.color.dark_gray_97));
            helper.setTextColor(R.id.item_business_stage_explore_tv, mContext.getResources().getColor(R.color.dark_gray_97));
            helper.setTextColor(R.id.item_business_stage_point_tv, mContext.getResources().getColor(R.color.dark_gray_97));
            helper.setTextColor(R.id.item_business_stage_point_capiton_tv, mContext.getResources().getColor(R.color.dark_gray_97));
        } else if (adapterPosition == currentPos) {
            helper.setBackgroundRes(R.id.item_business_stage_parent_ll, R.drawable.shape_b2b_change_account_current);
            helper.setTextColor(R.id.item_business_stage_no_tv, mContext.getResources().getColor(R.color.white));
            helper.setTextColor(R.id.item_business_stage_name_tv, mContext.getResources().getColor(R.color.white));
            helper.setTextColor(R.id.item_business_stage_explore_tv, mContext.getResources().getColor(R.color.white));
            helper.setTextColor(R.id.item_business_stage_point_tv, mContext.getResources().getColor(R.color.white));
            helper.setTextColor(R.id.item_business_stage_point_capiton_tv, mContext.getResources().getColor(R.color.white));
        } else {
            helper.setBackgroundRes(R.id.item_business_stage_parent_ll, R.drawable.shape_b2b_change_account);
            helper.setTextColor(R.id.item_business_stage_no_tv, mContext.getResources().getColor(R.color.main_text_color));
            helper.setTextColor(R.id.item_business_stage_name_tv, mContext.getResources().getColor(R.color.gray_default_dark));
            helper.setTextColor(R.id.item_business_stage_explore_tv, mContext.getResources().getColor(R.color.gray_default_dark));
            helper.setTextColor(R.id.item_business_stage_point_tv, mContext.getResources().getColor(R.color.gray_default_dark));
            helper.setTextColor(R.id.item_business_stage_point_capiton_tv, mContext.getResources().getColor(R.color.gray_default_dark));
        }

        helper.setText(R.id.item_business_stage_no_tv, item.getBS_NAME());
        helper.setText(R.id.item_business_stage_name_tv, item.getBS_NAME());
        helper.setText(R.id.item_business_stage_explore_tv, "本阶段可停留" + item.getBS_DAYS() + "天");
        String bs_point = item.getBS_POINT();
        if (!TextUtils.isEmpty(bs_point)) {
            String[] split = bs_point.split("#");

            if (split != null && split.length > 0) {
                String pointStr = "";
                for (int i = 0; i < split.length; i++) {
                    pointStr += ((i + 1) + "、" + split[i] + "\n");
                }
                if (pointStr.length() > 1) {
                    pointStr = pointStr.substring(0, pointStr.length() - 1);
                }
                helper.setText(R.id.item_business_stage_point_tv, pointStr);
            }
        }

    }
}
