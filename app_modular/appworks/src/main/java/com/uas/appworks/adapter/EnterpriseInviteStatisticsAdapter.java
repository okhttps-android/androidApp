package com.uas.appworks.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.common.data.DateFormatUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.InviteStatisticsBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe 企业邀请统计列表适配器
 * @date 2018/3/25 21:32
 */

public class EnterpriseInviteStatisticsAdapter extends BaseQuickAdapter<InviteStatisticsBean, BaseViewHolder> {
    private String mYear;
    private boolean mIsCurrent = false;

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {
        mYear = year;
        String currentYear = DateFormatUtil.long2Str("yyyy");
        if (currentYear != null && currentYear.equals(mYear)) {
            mIsCurrent = true;
        } else {
            mIsCurrent = false;
        }
    }

    public EnterpriseInviteStatisticsAdapter(List<InviteStatisticsBean> inviteStatisticsBeans) {
        super(R.layout.item_enterprise_invite_statistics, inviteStatisticsBeans);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, InviteStatisticsBean inviteStatisticsBean) {
        if (mIsCurrent) {
            int position = baseViewHolder.getAdapterPosition();
            String currentMonth = DateFormatUtil.long2Str("MM");
            int month = Integer.parseInt(currentMonth);
            if (month == (position + 1)) {
                baseViewHolder.setTextColor(R.id.item_invite_statistics_month_tv, mContext.getResources().getColor(R.color.red));
                baseViewHolder.setTextColor(R.id.item_invite_statistics_invite_count_tv, mContext.getResources().getColor(R.color.red));
                baseViewHolder.setTextColor(R.id.item_invite_statistics_register_count_tv, mContext.getResources().getColor(R.color.red));
            }
        }
        baseViewHolder.setText(R.id.item_invite_statistics_month_tv, inviteStatisticsBean.getMonth() + "");
        baseViewHolder.setText(R.id.item_invite_statistics_invite_count_tv, inviteStatisticsBean.getInviteCount() + "");
        baseViewHolder.setText(R.id.item_invite_statistics_register_count_tv, inviteStatisticsBean.getRegisterCount() + "");
    }
}
