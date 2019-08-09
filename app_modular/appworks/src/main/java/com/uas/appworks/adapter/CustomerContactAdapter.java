package com.uas.appworks.adapter;

import android.content.Context;

import com.common.data.ListUtils;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.model.BillGroupModel;

import java.util.ArrayList;
import java.util.List;

public class CustomerContactAdapter extends BillAdapter {
    private CustomerContactAdapterListener mCustomerContactAdapterListener;

    public CustomerContactAdapter(Context ct, List<BillGroupModel> mBillGroupModels, OnAdapterListener mOnAdapterListener,
                                  CustomerContactAdapterListener mCustomerContactAdapterListener) {
        super(ct, mBillGroupModels, mOnAdapterListener);
        this.mCustomerContactAdapterListener = mCustomerContactAdapterListener;
    }

    @Override
    public void deleteGroup(int groupIndex) {
        int ctId = 0;
        try{
            BillGroupModel mGroupModel = getBillGroupModel(groupIndex);
            if (mGroupModel != null) {
                List<BillGroupModel.BillModel> mBillFields = new ArrayList<>();
                if (mGroupModel.getShowBillFields() != null) {
                    mBillFields.addAll(mGroupModel.getShowBillFields());
                }
                if (mGroupModel.getHideBillFields() != null) {
                    mBillFields.addAll(mGroupModel.getHideBillFields());
                }
                if (!ListUtils.isEmpty(mBillFields)) {
                    for (BillGroupModel.BillModel mBillField : mBillFields) {
                        if (mBillField.getField().equals("ct_id")) {
                            ctId = Integer.valueOf(mBillField.getValue());
                        }
                    }
                }
            }
        }catch (Exception e){

        }
        if (ctId == 0) {
            super.deleteGroup(groupIndex);
        } else if (mCustomerContactAdapterListener != null) {
            mCustomerContactAdapterListener.deleteGroup(groupIndex,ctId);
        }
    }
    public  void superDeleteGroup(int groupIndex){
        super.deleteGroup(groupIndex);
    }

    public interface CustomerContactAdapterListener {
        void deleteGroup(int groupIndex,int ctId);
    }
}
