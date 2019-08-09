package com.uas.appworks.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessStageBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/22 16:46
 */
public class BusinessStageMenuAdapter extends BaseQuickAdapter<BusinessStageBean, BaseViewHolder> {

    public BusinessStageMenuAdapter(@Nullable List<BusinessStageBean> data) {
        super(R.layout.item_business_stage_menu, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessStageBean item) {
        int position = helper.getAdapterPosition() + 1;
        helper.setText(R.id.item_business_stage_tv, "阶段" + item.getBS_DETNO() + "：" + item.getBS_NAME());
    }
}
