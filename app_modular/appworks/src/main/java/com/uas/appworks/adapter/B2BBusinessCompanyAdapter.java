package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.B2BCompanyBean;

import org.w3c.dom.Text;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/15 16:04
 */

public class B2BBusinessCompanyAdapter extends RecyclerView.Adapter<B2BBusinessCompanyAdapter.MyViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<B2BCompanyBean> mB2BCompanyBeans;

    public B2BBusinessCompanyAdapter(Context context, List<B2BCompanyBean> b2BCompanyBeans) {
        mContext = context;
        mB2BCompanyBeans = b2BCompanyBeans;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.layout_list_b2b_company, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.companyTextView.setText(mB2BCompanyBeans.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mB2BCompanyBeans == null ? 0 : mB2BCompanyBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView companyTextView;

        public MyViewHolder(View itemView) {
            super(itemView);

            companyTextView = (TextView) itemView.findViewById(R.id.b2b_company_name_tv);
        }
    }
}
