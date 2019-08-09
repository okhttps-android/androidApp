package com.uas.appworks.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.modular.apputils.model.BillGroupModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

public class BillListDetailsAdapter extends BaseAdapter {
    private Context ct;
    private List<BillGroupModel> groupModels;
    private List<BillGroupModel.BillModel> mShowBillModels;
    private List<BillGroupModel.BillModel> mUpdateBillModels;

    public BillListDetailsAdapter(Context ct, List<BillGroupModel> groupModels) {
        this.ct = ct;
        this.groupModels = groupModels;
        changeBillModel();
    }

    public void updateGroupModels(List<BillGroupModel> groupModels) {
        this.groupModels = groupModels;
        changeBillModel();
    }

    public BillGroupModel getBillGroupModel(int groupIndex) {
        if (ListUtils.isEmpty(groupModels)) {
            return null;
        } else {
            for (BillGroupModel billGroupModel : groupModels) {
                if (billGroupModel.getGroupIndex() == groupIndex) {
                    return billGroupModel;
                }
            }
            return null;
        }
    }

    public List<BillGroupModel.BillModel> getUpdateBillModels() {
        return mUpdateBillModels;
    }

    public List<BillGroupModel> getGroupModels() {
        return groupModels;
    }

    /**
     * 当外界的因素引起mBillGroupModels变化时候，通过遍历将mBillGroupModels转成mShowBillModels进行显示
     */
    private void changeBillModel() {
        if (mShowBillModels == null) {
            mShowBillModels = new ArrayList<>();
        } else {
            mShowBillModels.clear();
        }
        if (mUpdateBillModels == null) {
            mUpdateBillModels = new ArrayList<>();
        } else {
            mUpdateBillModels.clear();
        }
        for (int i = 0; i < groupModels.size(); i++) {
            BillGroupModel e = groupModels.get(i);
            if (e != null && e.getShowBillFields() != null && !e.getShowBillFields().isEmpty()) {
                mShowBillModels.addAll(e.getShowBillFields());
            }
            if (e != null && e.getUpdateBillFields() != null && !e.getUpdateBillFields().isEmpty()) {
                mUpdateBillModels.addAll(e.getUpdateBillFields());
            }
        }
        LogUtil.i("gong", "mShowBillModels=" + JSON.toJSONString(mShowBillModels));
    }


    @Override
    public int getCount() {
        return mShowBillModels == null ? 0 : mShowBillModels.size();
    }

    @Override
    public Object getItem(int i) {
        return mShowBillModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder mViewHolder = null;
        if (view == null) {
            view = LayoutInflater.from(ct).inflate(R.layout.item_customer_details_3_0, null);
            mViewHolder = new ViewHolder();
            mViewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            mViewHolder.captionTv = (TextView) view.findViewById(R.id.captionTv);
            mViewHolder.valuesTv = (TextView) view.findViewById(R.id.valuesTv);
            view.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) view.getTag();
        }
        BillGroupModel.BillModel field = mShowBillModels.get(position);
        mViewHolder.captionTv.setText(field.getCaption());
        mViewHolder.valuesTv.setText(field.getValue());
        if (field.getGroupIndex() != 0 && position >= 1 && field.getGroupIndex() > mShowBillModels.get(position - 1).getGroupIndex()) {
            mViewHolder.tvTitle.setVisibility(View.VISIBLE);
            String mGroupName = null;
            if (ListUtils.getSize(groupModels) > field.getGroupIndex()) {
                mGroupName = groupModels.get(field.getGroupIndex()).getGroup();
            }
            mViewHolder.tvTitle.setText(TextUtils.isEmpty(mGroupName) ? "" : mGroupName);
        } else {
            mViewHolder.tvTitle.setVisibility(View.GONE);
        }
        return view;
    }


    class ViewHolder {
        private TextView tvTitle;
        private TextView captionTv;
        private TextView valuesTv;


    }
}