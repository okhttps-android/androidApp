package com.uas.appworks.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.WorkMenuBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/16 15:46
 */

public class WorkModuleSortDeleteAdapter extends RecyclerView.Adapter<WorkModuleSortDeleteAdapter.MyViewHolder> {
    private Context mContext;
    private Resources mResources;
    private List<WorkMenuBean> mWorkMenuBeans;
    private OnAddItemClickListener mOnAddItemClickListener;

    public List<WorkMenuBean> getWorkMenuBeans() {
        return mWorkMenuBeans;
    }

    public WorkModuleSortDeleteAdapter(Context context, List<WorkMenuBean> workMenuBeans) {
        mContext = context;
        this.mWorkMenuBeans = workMenuBeans;
        mResources = mContext.getResources();
    }

    public void setOnAddItemClickListener(OnAddItemClickListener onAddItemClickListener) {
        mOnAddItemClickListener = onAddItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_work_module_sort_delete, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (mWorkMenuBeans.get(position).isLocalModule()) {
            try {
                holder.contentTextView.setText(mResources.getIdentifier(mWorkMenuBeans.get(position).getModuleName(), "string", mContext.getPackageName()));
            } catch (Exception e) {
                holder.contentTextView.setText(mWorkMenuBeans.get(position).getModuleName());
            }

        } else {
            holder.contentTextView.setText(mWorkMenuBeans.get(position).getModuleName());
        }
        holder.addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnAddItemClickListener != null) {
                    int realPosition = holder.getAdapterPosition();
                    mWorkMenuBeans.get(realPosition).setModuleVisible(true);
                    mOnAddItemClickListener.onAddItemClick(view, realPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWorkMenuBeans == null ? 0 : mWorkMenuBeans.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView addImageView;
        private TextView contentTextView;

        public MyViewHolder(View itemView) {
            super(itemView);

            addImageView = (ImageView) itemView.findViewById(R.id.item_work_module_sort_delete_add_iv);
            contentTextView = (TextView) itemView.findViewById(R.id.item_work_module_sort_delete_content_tv);
        }
    }

    public interface OnAddItemClickListener {
        void onAddItemClick(View view, int position);
    }
}
