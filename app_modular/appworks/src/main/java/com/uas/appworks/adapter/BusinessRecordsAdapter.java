package com.uas.appworks.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessFollowBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/25 11:24
 */
public class BusinessRecordsAdapter extends BaseQuickAdapter<BusinessFollowBean, BaseViewHolder> {

    public BusinessRecordsAdapter(@Nullable List<BusinessFollowBean> data) {
        super(R.layout.item_business_records, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessFollowBean item) {
        helper.setText(R.id.business_records_doman_tv, item.getDoman());
        helper.setText(R.id.business_records_dotime_tv, item.getDotime());
        helper.setText(R.id.business_records_stage_tv, item.getGeneration()
                + "->" + item.getNextgeneration() + "(" + item.getType() + ")");
        String remarkbf = item.getRemarkbf();
        String remarkdt = item.getRemarkdt();
        String remark = "";
        if (!TextUtils.isEmpty(remarkbf)) {
            remark = remarkbf + ";";
        }
        remark += remarkdt;
        helper.setText(R.id.business_records_remark_tv, remark);
    }
}
