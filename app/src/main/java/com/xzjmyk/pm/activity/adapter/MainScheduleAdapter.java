package com.xzjmyk.pm.activity.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.common.data.DateFormatUtil;
import com.uas.appworks.model.bean.TimeHelperBean;
import com.xzjmyk.pm.activity.R;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/12 20:50
 */
public class MainScheduleAdapter extends BaseQuickAdapter<TimeHelperBean, BaseViewHolder> {

    public MainScheduleAdapter(@Nullable List<TimeHelperBean> data) {
        super(R.layout.item_main_schedule, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TimeHelperBean item) {
        String startTime = item.getStartTime();
        try {
            long startLong = DateFormatUtil.str2Long(item.getStartTime(), DateFormatUtil.YMD_HMS);
            startTime = DateFormatUtil.long2Str(startLong, DateFormatUtil.HM);
        } catch (Exception e) {
            startTime = "";
        }
        helper.setText(R.id.main_schedule_time_tv, startTime);

        helper.setText(R.id.main_schedule_content_tv, item.getRemarks());
    }
}
