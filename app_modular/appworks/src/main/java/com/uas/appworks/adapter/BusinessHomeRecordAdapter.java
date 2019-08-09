package com.uas.appworks.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.common.data.DateFormatUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessRecordBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:50
 */
public class BusinessHomeRecordAdapter extends BaseQuickAdapter<BusinessRecordBean, BaseViewHolder> {

    public BusinessHomeRecordAdapter(@Nullable List<BusinessRecordBean> data) {
        super(R.layout.item_business_manage_record, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessRecordBean item) {
        helper.setText(R.id.business_manage_record_name_tv, item.getName());
        try {
            if (item.getTime() != 0) {
                helper.setText(R.id.business_manage_record_date_tv, DateFormatUtil.long2Str(item.getTime(), DateFormatUtil.YMD));
            }
        } catch (Exception e) {

        }
        helper.setText(R.id.business_manage_record_msg_tv, item.getInfo());
        helper.setText(R.id.business_manage_record_man_tv, item.getMan());
    }
}
