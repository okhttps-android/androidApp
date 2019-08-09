package com.xzjmyk.pm.activity.ui.erp.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.core.utils.helper.AvatarHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.model.SellHonorBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/10/11 15:19
 */
public class SellHonorAdapter extends BaseQuickAdapter<SellHonorBean, BaseViewHolder> {

    public SellHonorAdapter(@Nullable List<SellHonorBean> data) {
        super(R.layout.item_list_sell_honor, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SellHonorBean item) {
        ImageView headImg = (ImageView) helper.getView(R.id.sell_honor_head_iv);
        AvatarHelper.getInstance().display(
                item.getImid(), headImg, true, true);

        helper.setText(R.id.sell_honor_name_tv, item.getName());
        helper.setText(R.id.sell_honor_money_tv, item.getId() + "万元");
        helper.setText(R.id.sell_honor_content_tv, item.getDesc());
        helper.setText(R.id.sell_honor_position_tv, item.getPosition());
        helper.setText(R.id.sell_honor_branch_tv, item.getDepart());
    }
}
