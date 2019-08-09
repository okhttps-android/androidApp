package com.uas.appworks.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.CommonFormBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/19 17:42
 */
public class CommonFormDetailAdapter extends BaseMultiItemQuickAdapter<CommonFormBean, BaseViewHolder> {

    public CommonFormDetailAdapter(@Nullable List<CommonFormBean> data) {
        super(data);

        addItemType(CommonFormBean.COMMON_FORM_GRAY_LINE, R.layout.item_common_gray_line);
        addItemType(CommonFormBean.COMMON_FORM_CONTENT_ITEM, R.layout.item_common_form_detail);
    }

    @Override
    protected void convert(BaseViewHolder helper, CommonFormBean commonFormBean) {
        if (helper != null) {
            switch (helper.getItemViewType()) {
                case CommonFormBean.COMMON_FORM_GRAY_LINE:
                    bindGroupViewHolder(helper, commonFormBean);
                    break;
                case CommonFormBean.COMMON_FORM_CONTENT_ITEM:
                    bindSubViewHolder(helper, commonFormBean);
                    break;
            }
        }
    }

    private void bindSubViewHolder(BaseViewHolder helper, CommonFormBean commonFormBean) {
        helper.setText(R.id.item_common_form_detail_caption, commonFormBean.getCaption());
        helper.setText(R.id.item_common_form_detail_value, commonFormBean.getValue());
    }

    private void bindGroupViewHolder(BaseViewHolder helper, CommonFormBean commonFormBean) {

    }
}
