package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.B2BCompanyBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/16 11:09
 */

public class B2BAccountAdapter extends RecyclerView.Adapter<B2BAccountAdapter.B2BAccountViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<B2BCompanyBean> mB2BCompanyBeans;

    public B2BAccountAdapter(Context context, List<B2BCompanyBean> b2BCompanyBeans) {
        mContext = context;
        mB2BCompanyBeans = b2BCompanyBeans;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public B2BAccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_list_b2b_account, parent, false);
        return new B2BAccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(B2BAccountViewHolder holder, final int position) {
        B2BCompanyBean b2BCompanyBean = mB2BCompanyBeans.get(position);
        holder.mCheckBox.setChecked(b2BCompanyBean.isSelected());
        holder.mCheckBox.setText(b2BCompanyBean.getName());

        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < mB2BCompanyBeans.size(); i++) {
                    if (mB2BCompanyBeans.get(i).isSelected()) {
                        if (i == position) {
                            break;
                        } else {
                            mB2BCompanyBeans.get(i).setSelected(false);
                            notifyItemChanged(i);
                        }
                    }
                }
                mB2BCompanyBeans.get(position).setSelected(true);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mB2BCompanyBeans == null ? 0 : mB2BCompanyBeans.size();
    }

    class B2BAccountViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mCheckBox;

        public B2BAccountViewHolder(View itemView) {
            super(itemView);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.list_b2b_account_cb);
        }
    }
}
