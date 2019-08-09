package com.uas.appworks.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.WorkMenuBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/16 15:32
 */

public class WorkModuleSortAddAdapter extends RecyclerView.Adapter<WorkModuleSortAddAdapter.MyViewHolder> {
    private Context mContext;
    private List<WorkMenuBean> mWorkMenuBeans;
    private Resources mResources;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemDragListener mOnItemDragListener;
    private OnDeleteItemClickListener mOnDeleteItemClickListener;

    public List<WorkMenuBean> getWorkMenuBeans() {
        return mWorkMenuBeans;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemDragListener(OnItemDragListener onItemDragListener) {
        mOnItemDragListener = onItemDragListener;
    }

    public void setOnDeleteItemClickListener(OnDeleteItemClickListener onDeleteItemClickListener) {
        mOnDeleteItemClickListener = onDeleteItemClickListener;
    }

    public WorkModuleSortAddAdapter(Context context, List<WorkMenuBean> workMenuBeans) {
        mContext = context;
        this.mWorkMenuBeans = workMenuBeans;
        mResources = mContext.getResources();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView contentTv;
        private ImageView sortTv, deleteIv;
        private LinearLayout wholeItem;

        public MyViewHolder(View itemView) {
            super(itemView);
            deleteIv = (ImageView) itemView.findViewById(R.id.item_work_module_sort_add_delete_iv);
            contentTv = (TextView) itemView.findViewById(R.id.item_work_module_sort_add_content_tv);
            sortTv = (ImageView) itemView.findViewById(R.id.item_work_module_sort_add_sort_iv);
            wholeItem = (LinearLayout) itemView.findViewById(R.id.item_work_module_sort_add_whole_item);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_work_module_sort_add, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final WorkMenuBean msg = mWorkMenuBeans.get(position);
        if (msg.isLocalModule()){
            try {
                holder.contentTv.setText(mResources.getIdentifier(msg.getModuleName(), "string", mContext.getPackageName()));
            } catch (Exception e) {
                holder.contentTv.setText(msg.getModuleName());
            }
        }else {
            holder.contentTv.setText(msg.getModuleName());
        }

        holder.wholeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, holder.getLayoutPosition());
                }
            }
        });

        holder.wholeItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(view, holder.getLayoutPosition());
                }
                return true;
            }
        });

        holder.sortTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mOnItemDragListener.onItemDrag(holder);
                }
                return false;
            }
        });

        holder.deleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnDeleteItemClickListener != null) {
                    msg.setModuleVisible(false);
                    mOnDeleteItemClickListener.onDeleteItemClick(view, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWorkMenuBeans.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public interface OnItemDragListener {
        void onItemDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnDeleteItemClickListener {
        void onDeleteItemClick(View view, int position);
    }

}
