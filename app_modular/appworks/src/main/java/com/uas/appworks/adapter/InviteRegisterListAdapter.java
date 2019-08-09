package com.uas.appworks.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.RegisterListBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe 邀请注册明细列表适配器
 * @date 2018/3/25 17:40
 */

public class InviteRegisterListAdapter extends BaseQuickAdapter<RegisterListBean, BaseViewHolder> {

    public InviteRegisterListAdapter(Context context, List<RegisterListBean> registerListBeans) {
        super(R.layout.item_invite_register_list, registerListBeans);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, RegisterListBean registerListBean) {
        baseViewHolder.setText(R.id.invite_register_list_company_name_tv, registerListBean.getEnName());
        baseViewHolder.setText(R.id.invite_register_list_linkman_tv, registerListBean.getLinkman());
        baseViewHolder.setText(R.id.invite_register_list_phone_tv, registerListBean.getPhone());
        baseViewHolder.setText(R.id.invite_register_list_invite_date_tv, registerListBean.getInviteDate());
        baseViewHolder.setText(R.id.invite_register_list_invite_name_tv, registerListBean.getInviteName());

        if (TextUtils.isEmpty(registerListBean.getRegisterDate())) {
            baseViewHolder.setText(R.id.invite_register_list_register_date_tv, "");
            baseViewHolder.setGone(R.id.invite_register_list_register_date_ll, false);
        } else {
            baseViewHolder.setText(R.id.invite_register_list_register_date_tv, registerListBean.getRegisterDate());
            baseViewHolder.setGone(R.id.invite_register_list_register_date_ll, true);
        }

        if (registerListBean.getState() == RegisterListBean.STATE_REGISTER) {
            baseViewHolder.setText(R.id.invite_register_list_state_tv, R.string.str_registered);
            baseViewHolder.setTextColor(R.id.invite_register_list_state_tv, mContext.getResources().getColor(R.color.blue));
        } else if (registerListBean.getState() == RegisterListBean.STATE_UNREGISTER) {
            baseViewHolder.setText(R.id.invite_register_list_state_tv, R.string.str_unregistered);
            baseViewHolder.setTextColor(R.id.invite_register_list_state_tv, mContext.getResources().getColor(R.color.red));
        } else {
            baseViewHolder.setVisible(R.id.invite_register_list_state_tv, false);
        }
    }
}
