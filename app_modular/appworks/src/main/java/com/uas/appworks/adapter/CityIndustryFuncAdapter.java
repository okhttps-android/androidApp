package com.uas.appworks.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.me.imageloader.ImageLoaderUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.CityIndustryMenuBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/23 16:25
 */

public class CityIndustryFuncAdapter extends RecyclerView.Adapter<CityIndustryFuncAdapter.MyViewholder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<CityIndustryMenuBean.ServesBean> mCityServiceBeans;
    private Resources mResources;
    private OnItemClickListener mOnItemClickListener;

    public CityIndustryFuncAdapter(Context context, List<CityIndustryMenuBean.ServesBean> cityServiceBeans) {
        this.mContext = context;
        this.mCityServiceBeans = cityServiceBeans;
        mInflater = LayoutInflater.from(mContext);
        mResources = mContext.getResources();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public CityIndustryFuncAdapter.MyViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_city_industry_child, null);
        return new MyViewholder(view);
    }

    @Override
    public void onBindViewHolder(CityIndustryFuncAdapter.MyViewholder holder, final int position) {
        holder.mTextView.setText(mCityServiceBeans.get(position).getSv_name());
        if (TextUtils.isEmpty(mCityServiceBeans.get(position).getSv_logourl().getMobile())) {
            holder.mImageView.setImageResource(R.drawable.defaultpic);
        } else {
            ImageLoaderUtil.getInstance().loadImage(mCityServiceBeans.get(position).getSv_logourl().getMobile(), holder.mImageView);
        }

        holder.mWholeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCityServiceBeans.size();
    }

    public class MyViewholder extends RecyclerView.ViewHolder {
        private RelativeLayout mWholeItem;
        private ImageView mImageView;
        private TextView mTextView;

        public MyViewholder(View view) {
            super(view);
            mWholeItem = (RelativeLayout) view.findViewById(R.id.city_industry_child_whole_item);
            mImageView = (ImageView) view.findViewById(R.id.city_industry_child_icon_iv);
            mTextView = (TextView) view.findViewById(R.id.city_industry_child_name_tv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
