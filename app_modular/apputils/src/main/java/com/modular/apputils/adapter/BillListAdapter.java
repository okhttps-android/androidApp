package com.modular.apputils.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.modular.apputils.R;
import com.modular.apputils.model.BillListGroupModel;

import java.util.ArrayList;
import java.util.List;

public class BillListAdapter extends RecyclerView.Adapter<BillListAdapter.ViewHolder> {
    private Context ct;
    private List<BillListGroupModel> groupModels;
    private List<BillListGroupModel.BillListField> listFields;
    private OnAdapterListener mOnAdapterListener;

    public BillListAdapter(Context ct, List<BillListGroupModel> groupModels, OnAdapterListener mOnAdapterListener) {
        this.ct = ct;
        this.groupModels = groupModels;
        this.mOnAdapterListener = mOnAdapterListener;
        changeFieldsList();
    }

    public void updateGroupModels(List<BillListGroupModel> groupModels) {
        this.groupModels = groupModels;
        changeFieldsList();
        notifyDataSetChanged();
    }

    public List<BillListGroupModel> getGroupModels() {
        return groupModels;
    }

    private void changeFieldsList() {
        if (listFields == null) {
            listFields = new ArrayList<>();
        } else {
            listFields.clear();
        }
        if (ListUtils.isEmpty(groupModels)) return;
        for (BillListGroupModel groupModel : groupModels) {
            if (!ListUtils.isEmpty(groupModel.getBillFields())) {
                listFields.addAll(groupModel.getBillFields());
            }
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_bill_list, viewGroup, false));
    }

    @Override
    public int getItemCount() {
        int mItemCount = ListUtils.getSize(listFields);
        return mItemCount;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView captionTV;
        private TextView valueTv;
        private View lineView;

        public ViewHolder(View itemView) {
            super(itemView);
            captionTV = (TextView) itemView.findViewById(R.id.captionTV);
            valueTv = (TextView) itemView.findViewById(R.id.valueTv);
            lineView = itemView.findViewById(R.id.lineView);
            lineView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder mViewHolder, int position) {
        BillListGroupModel.BillListField field = listFields.get(position);
        mViewHolder.captionTV.setText(field.getCaption());
        mViewHolder.valueTv.setText(field.getValue());
        if (field.getGroupIndex() != 0 && position >= 1 && field.getGroupIndex() > listFields.get(position - 1).getGroupIndex()) {
            mViewHolder.lineView.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.lineView.setVisibility(View.GONE);
        }
        mViewHolder.itemView.setTag(field);
        mViewHolder.itemView.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null && view.getTag() instanceof BillListGroupModel.BillListField) {
                if (mOnAdapterListener != null) {
                    BillListGroupModel.BillListField mBillListField = (BillListGroupModel.BillListField) view.getTag();
                   if (mBillListField.getGroupIndex()>=0&&ListUtils.getSize(groupModels)>mBillListField.getGroupIndex()){
                       mOnAdapterListener.onClick(groupModels.get(mBillListField.getGroupIndex()));
                   }
                }
            }
        }
    };

    public interface OnAdapterListener {
        void onClick(BillListGroupModel  mBillListGroupModel);
    }
}