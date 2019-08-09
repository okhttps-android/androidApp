package com.uas.appworks.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.me.imageloader.ImageLoaderUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.WorkMenuBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/16 16:11
 */

public class WorkFuncSetChildAdapter extends RecyclerView.Adapter<WorkFuncSetChildAdapter.ChildViewHolder> {
    private Context mContext;
    private List<WorkMenuBean.ModuleListBean> mModuleListBeans;
    private LayoutInflater mLayoutInflater;
    private Resources mResources;
    private OnVisibleChangeListener mOnVisibleChangeListener;

    public void setOnVisibleChangeListener(OnVisibleChangeListener onVisibleChangeListener) {
        mOnVisibleChangeListener = onVisibleChangeListener;
    }

    public List<WorkMenuBean.ModuleListBean> getModuleListBeans() {
        return mModuleListBeans;
    }

    public WorkFuncSetChildAdapter(Context context, List<WorkMenuBean.ModuleListBean> moduleListBeans) {
        mContext = context;
        mModuleListBeans = moduleListBeans;
        mLayoutInflater = LayoutInflater.from(mContext);
        mResources = mContext.getResources();
    }

    @Override
    public ChildViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_work_func_set_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChildViewHolder holder, final int position) {
        if (mModuleListBeans.get(position).isLocalMenu()) {
            try {
                holder.mTextView.setText(mResources.getIdentifier(mModuleListBeans.get(position).getMenuName(), "string", mContext.getPackageName()));
            } catch (Exception e) {
                holder.mTextView.setText(mModuleListBeans.get(position).getMenuName());
            }
        } else {
            holder.mTextView.setText(mModuleListBeans.get(position).getMenuName());
        }

        if (TextUtils.isEmpty(mModuleListBeans.get(position).getMenuIcon())) {
            holder.mImageView.setImageResource(R.drawable.defaultpic);
        } else {
            if (mModuleListBeans.get(position).isLocalMenu()) {
                try {
                    holder.mImageView.setImageResource(mResources.getIdentifier(mModuleListBeans.get(position).getMenuIcon(), "drawable", mContext.getPackageName()));
                } catch (Exception e) {
                    holder.mImageView.setImageResource(R.drawable.defaultpic);
                }
            } else {
                ImageLoaderUtil.getInstance().loadImage(mModuleListBeans.get(position).getMenuIcon(), holder.mImageView);
            }
        }

        holder.mCheckBox.setChecked(!mModuleListBeans.get(position).isHide());

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mModuleListBeans.get(holder.getLayoutPosition()).setIsHide(!b);
                /*if (mOnVisibleChangeListener != null) {
                    mOnVisibleChangeListener.onVisibleChange(b, position);
                }*/
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mCheckBox.setChecked(!holder.mCheckBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mModuleListBeans.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private ImageView mImageView;
        private CheckBox mCheckBox;

        public ChildViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.work_func_set_child_name_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.work_func_set_child_icon_iv);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.work_func_set_child_cb);
        }
    }

    public interface OnVisibleChangeListener {
        void onVisibleChange(boolean visible, int position);
    }
}
