package com.uas.appme.settings.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appme.R;
import com.uas.appme.settings.model.SystemAdminBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/6/8 14:55
 */
public class SystemAdminAdapter extends BaseQuickAdapter<SystemAdminBean, BaseViewHolder> {

    public SystemAdminAdapter(@Nullable List<SystemAdminBean> systemAdminBeans) {
        super(R.layout.item_list_system_admin, systemAdminBeans);
    }

    @Override
    protected void convert(BaseViewHolder helper, SystemAdminBean item) {
        helper.setText(R.id.list_system_admin_name_tv, item.getName());
        helper.setText(R.id.list_system_admin_mobile_tv, item.getMobile());
        helper.setText(R.id.list_system_admin_position_tv, item.getPosition());
    }
}
