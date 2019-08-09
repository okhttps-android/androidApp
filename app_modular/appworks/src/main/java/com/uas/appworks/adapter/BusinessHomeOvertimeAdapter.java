package com.uas.appworks.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.common.data.DateFormatUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessOverTimeBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:50
 */
public class BusinessHomeOvertimeAdapter extends BaseQuickAdapter<BusinessOverTimeBean, BaseViewHolder> {

    public BusinessHomeOvertimeAdapter(@Nullable List<BusinessOverTimeBean> data) {
        super(R.layout.item_business_manage_overtime, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessOverTimeBean item) {
        helper.setText(R.id.business_manage_overtime_name_tv, item.getName());
        try {
            helper.setText(R.id.business_manage_overtime_date_tv, DateFormatUtil.long2Str(item.getLastetime(), DateFormatUtil.YMD));
        } catch (Exception e) {
        }
        helper.setText(R.id.business_manage_overtime_stage_tv, item.getStepName());
        helper.setText(R.id.business_manage_overtime_man_tv, item.getMan());
    }
}
