package com.uas.appworks.crm3_0.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.core.app.Constants;
import com.modular.apputils.activity.BillDetailsActivity;
import com.modular.apputils.fragment.ViewPagerLazyFragment;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.businessDetailActivity.BusinessDetailNewActivity;
import com.uas.appworks.crm3_0.activity.CustomerContactActivity;
import com.uas.appworks.crm3_0.activity.VisitRecordBillInputActivity;
import com.uas.appworks.model.CustomerBindBill;

import java.util.ArrayList;
import java.util.List;

public class CustomerDetailsBottomListFragment extends ViewPagerLazyFragment {
    private boolean isMe;//0,客户地址 1，客户联系人 2.拜访记录 3，客户商机
    private int type;//0,客户地址 1，客户联系人 2.拜访记录 3，客户商机
    private int mId;//主表id
    private List<CustomerBindBill> mCusBusiness;
    private RecyclerView mRecyclerView;


    public static CustomerDetailsBottomListFragment newInstance(boolean isMe,int mId,int type, ArrayList<CustomerBindBill> mCusBusiness) {
        Bundle args = new Bundle();
        CustomerDetailsBottomListFragment fragment = new CustomerDetailsBottomListFragment();
        args.putBoolean("isMe", isMe);
        args.putInt("mId", mId);
        args.putInt("type", type);
        args.putParcelableArrayList("mCusBusiness", mCusBusiness);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_customer_details_bottomlist;
    }


    @Override
    protected void LazyData() {
        if (getArguments() != null) {
            type = getArguments().getInt("type", 0);
            mId = getArguments().getInt("mId", 0);
            isMe = getArguments().getBoolean("isMe", false);
            mCusBusiness = getArguments().getParcelableArrayList("mCusBusiness");
        }
        EmptyRecyclerView mEmptyRecyclerView = findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
        mRecyclerView.setAdapter(new ListAdapter());
    }


    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater mLayoutInflater;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            //0,客户地址 1，客户联系人 2.拜访记录 3，客户商机
            switch (type) {
                case 1:
                    return new ContactViewHolder(viewGroup);
                case 2:
                    return new VisitRecordViewHolder(viewGroup);
                case 3:
                    return new BusinessViewHolder(viewGroup);
                default:
                    return new AddressViewHolder(viewGroup);
            }
        }

        public LayoutInflater getLayoutInflater() {
            if (mLayoutInflater == null) {
                mLayoutInflater = LayoutInflater.from(ct);
            }
            return mLayoutInflater;
        }

        @Override
        public int getItemCount() {
            return ListUtils.getSize(mCusBusiness);
        }

        //客户联系人
        private class ContactViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTv;
            private TextView subNameTv;
            private TextView phoneTv;

            public ContactViewHolder(ViewGroup viewGroup) {
                this(getLayoutInflater().inflate(R.layout.item_customer_detail_bottom_conteact, viewGroup, false));
            }

            public ContactViewHolder(View itemView) {
                super(itemView);
                nameTv = (TextView) itemView.findViewById(R.id.nameTv);
                subNameTv = (TextView) itemView.findViewById(R.id.subNameTv);
                phoneTv = (TextView) itemView.findViewById(R.id.phoneTv);

            }
        }

        //拜访记录visitRecord
        private class VisitRecordViewHolder extends RecyclerView.ViewHolder {
            private TextView dateTv;
            private TextView contactTv;
            private TextView doManTv;
            private TextView statusTv;

            public VisitRecordViewHolder(ViewGroup viewGroup) {
                this(getLayoutInflater().inflate(R.layout.item_customer_detail_bottom_visitrecord, viewGroup, false));
            }

            public VisitRecordViewHolder(View itemView) {
                super(itemView);
                dateTv = (TextView) itemView.findViewById(R.id.dateTv);
                contactTv = (TextView) itemView.findViewById(R.id.contactTv);
                doManTv = (TextView) itemView.findViewById(R.id.doManTv);
                statusTv = (TextView) itemView.findViewById(R.id.statusTv);

            }
        }


        //address  客户地址
        private class AddressViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTv;
            private TextView addressTv;

            public AddressViewHolder(ViewGroup viewGroup) {
                this(getLayoutInflater().inflate(R.layout.item_customer_detail_bottom_address, viewGroup, false));
            }

            public AddressViewHolder(View itemView) {
                super(itemView);
                nameTv = (TextView) itemView.findViewById(R.id.nameTv);
                addressTv = (TextView) itemView.findViewById(R.id.addressTv);


            }
        }

        //客户商机
        private class BusinessViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTv;
            private TextView stageTv;
            private TextView statusTv;
            private TextView dateTv;

            public BusinessViewHolder(ViewGroup viewGroup) {
                this(getLayoutInflater().inflate(R.layout.item_customer_detail_bottom_business, viewGroup, false));
            }

            public BusinessViewHolder(View itemView) {
                super(itemView);
                nameTv = (TextView) itemView.findViewById(R.id.nameTv);
                stageTv = (TextView) itemView.findViewById(R.id.stageTv);
                statusTv = (TextView) itemView.findViewById(R.id.statusTv);
                dateTv = (TextView) itemView.findViewById(R.id.dateTv);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            CustomerBindBill bill = mCusBusiness.get(i);
            if (viewHolder instanceof ContactViewHolder) {
                bindContactView((ContactViewHolder) viewHolder, i, bill);
            } else if (viewHolder instanceof VisitRecordViewHolder) {
                bindVisitRecordView((VisitRecordViewHolder) viewHolder, i, bill);
            } else if (viewHolder instanceof BusinessViewHolder) {
                bindBusinessView((BusinessViewHolder) viewHolder, i, bill);
            } else if (viewHolder instanceof AddressViewHolder) {
                bindAddressView((AddressViewHolder) viewHolder, i, bill);
            }
            viewHolder.itemView.setTag(bill);
            viewHolder.itemView.setOnClickListener(mOnClickListener);
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null && view.getTag() instanceof CustomerBindBill) {
                    CustomerBindBill bill = (CustomerBindBill) view.getTag();
                    switch (type) {
                        case 1://客户联系人
                            startActivity(new Intent(ct, CustomerContactActivity.class)
                                    .putExtra(Constants.Intents.CALLER, "Contact")
                                    .putExtra(Constants.Intents.INPUT_CLASS, CustomerContactActivity.class)
                                    .putExtra(Constants.Intents.TITLE, "客户联系人")
                                    .putExtra(Constants.Intents.ID, mId));
                            break;
                        case 2://拜访记录
                            startActivity(new Intent(ct, BillDetailsActivity.class)
                                    .putExtra(Constants.Intents.CALLER, "VisitRecord")
                                    .putExtra(Constants.Intents.TITLE, "拜访记录")
                                    .putExtra(Constants.Intents.INPUT_CLASS, VisitRecordBillInputActivity.class)
                                    .putExtra(Constants.Intents.ID, bill.getId()));
                            break;
                        case 3://客户商机
                            startActivity(new Intent(ct, BusinessDetailNewActivity.class)
                                    .putExtra("id", bill.getId())
                                    .putExtra("type", bill.getType())
                                    .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, isMe?"businessCharge":"businessBranch")
                                    .putExtra("bc_code", bill.getCode()));
                            break;
                        default://客户地址
                            //TODO 客户地址
                    }
                }


            }
        };

        private void bindContactView(ContactViewHolder holder, int position, CustomerBindBill bill) {
            holder.nameTv.setText(bill.getName());
            holder.subNameTv.setText(bill.getSubName());
            holder.phoneTv.setText(bill.getDate());
        }

        private void bindVisitRecordView(VisitRecordViewHolder holder, int position, CustomerBindBill bill) {
            holder.contactTv.setText(bill.getSubName());
            holder.dateTv.setText(bill.getDate());
            holder.doManTv.setText(bill.getName());
            holder.statusTv.setText(bill.getStatus());
        }

        private void bindBusinessView(BusinessViewHolder holder, int position, CustomerBindBill bill) {
            holder.nameTv.setText(bill.getName());
            holder.stageTv.setText(bill.getSubName());
            holder.dateTv.setText(bill.getDate());
            holder.statusTv.setText(bill.getStatus());
        }

        private void bindAddressView(AddressViewHolder holder, int position, CustomerBindBill bill) {
            holder.nameTv.setText(bill.getName());
            holder.addressTv.setText(bill.getAddress());
        }
    }
}
