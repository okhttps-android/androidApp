package com.uas.appworks.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessMineChildBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 19:50
 */
public class BusinessMineAdapter extends BaseMultiItemQuickAdapter<BusinessMineChildBean, BaseViewHolder> {

    public BusinessMineAdapter(List<BusinessMineChildBean> businessMineChildBeans) {
        super(businessMineChildBeans);
        addItemType(BusinessMineChildBean.BUSINESS_MINE_PARENT, R.layout.item_business_mine_parent);
        addItemType(BusinessMineChildBean.BUSINESS_MINE_CHILD, R.layout.item_business_mine_child);
    }

    public void bindGroupViewHolder(BaseViewHolder helper, BusinessMineChildBean businessMineChildBean) {

    }

    public void bindSubViewHolder(BaseViewHolder helper, BusinessMineChildBean businessMineChildBean) {
        helper.setText(R.id.business_mine_child_caption_tv, businessMineChildBean.getCaption());
        helper.setText(R.id.business_mine_child_value_tv, businessMineChildBean.getValue());
    }

    @Override
    protected void convert(BaseViewHolder helper, BusinessMineChildBean businessMineChildBean) {
        if (helper != null) {
            switch (helper.getItemViewType()) {
                case BusinessMineChildBean.BUSINESS_MINE_PARENT:
                    bindGroupViewHolder(helper, businessMineChildBean);
                    break;
                case BusinessMineChildBean.BUSINESS_MINE_CHILD:
                    bindSubViewHolder(helper, businessMineChildBean);
                    break;
            }
        }
    }
}
