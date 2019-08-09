package com.uas.appworks.adapter;

import android.content.Context;
import android.view.View;

import com.common.data.ListUtils;
import com.core.utils.CommonUtil;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

public class CustomerVisitBillAdapter extends BillAdapter {

    public CustomerVisitBillAdapter(Context ct, List<BillGroupModel> mBillGroupModels, OnAdapterListener mOnAdapterListener) {
        super(ct, mBillGroupModels, mOnAdapterListener);
    }


    @Override
    public void bindInputView(InputViewHolder mInputViewHolder, BillGroupModel.BillModel model, int position) throws Exception {
        super.bindInputView(mInputViewHolder, model, position);
        if (model.getCaption().equals("公司名称") || "mpd_company".equals(model.getField())) {
            //选择类型
            mInputViewHolder.valuesEd.setHint("请选择");
            mInputViewHolder.valuesEd.setFocusable(false);
            mInputViewHolder.valuesEd.setClickable(true);
            mInputViewHolder.selectIv.setVisibility(View.VISIBLE);
            mInputViewHolder.valuesEd.setTag(R.id.tag, position);
            mInputViewHolder.valuesEd.setTag(R.id.tag2, model);
            mInputViewHolder.valuesEd.setOnClickListener(this);
        }
    }

    protected void addGroups(int mGroupIndex) {
        BillGroupModel mBillGroupModel = mBillGroupModels.get(mGroupIndex);
        mBillGroupModel.setLastInType(false);
        BillGroupModel newBillGroupModel = new BillGroupModel();
        newBillGroupModel.setForm(mBillGroupModel.isForm());
        newBillGroupModel.setGroup(mBillGroupModel.getGroup());
        newBillGroupModel.setDeleteAble(true);
        newBillGroupModel.setLastInType(true);
        for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
            BillGroupModel.BillModel mBillModel = new BillGroupModel.BillModel(e);
            switch (mBillModel.getField()) {
                case "mpd_personnum"://人员编号
                    mBillModel.setDefValue("");
                    mBillModel.setValue("");
                    mBillModel.setReadOnly("F");
                    break;
                case "mpd_personname":
                    mBillModel.setDefValue("");
                    mBillModel.setValue("");
                    mBillModel.setReadOnly("F");
                    break;
                case "mpd_type":
                    mBillModel.setDefValue("");
                    mBillModel.setValue("");
                    mBillModel.setReadOnly("F");
                    break;
            }
            newBillGroupModel.addShow(mBillModel);
        }
        mBillGroupModels.add(mGroupIndex + 1, newBillGroupModel);
        setBillGroupModels(mBillGroupModels);
        notifyDataSetChanged();
    }
}
