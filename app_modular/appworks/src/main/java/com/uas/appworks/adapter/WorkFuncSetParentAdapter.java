package com.uas.appworks.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uas.appworks.R;
import com.uas.appworks.model.bean.WorkMenuBean;

import java.util.Collections;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/16 16:04
 */

public class WorkFuncSetParentAdapter extends RecyclerView.Adapter<WorkFuncSetParentAdapter.ParentViewHolder> {
    private Context mContext;
    private List<WorkMenuBean> mWorkMenuBeans;
    private LayoutInflater mLayoutInflater;
    private Resources mResources;
    private RecyclerView.RecycledViewPool mRecycledViewPool;

    public List<WorkMenuBean> getWorkMenuBeans() {
        return mWorkMenuBeans;
    }

    public WorkFuncSetParentAdapter(Context context, List<WorkMenuBean> workMenuBeans) {
        mContext = context;
        mWorkMenuBeans = workMenuBeans;
        mLayoutInflater = LayoutInflater.from(mContext);
        mResources = mContext.getResources();
    }

    @Override
    public ParentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_work_func_set_parent, parent, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ParentViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        WorkMenuBean workMenuBean = mWorkMenuBeans.get(position);
        if (workMenuBean.isLocalModule()) {
            try {
                holder.titleTextView.setText(mResources.getIdentifier(workMenuBean.getModuleName(), "string", mContext.getPackageName()));
            } catch (Exception e) {
                holder.titleTextView.setText(workMenuBean.getModuleName());
            }
        } else {
            holder.titleTextView.setText(workMenuBean.getModuleName());
        }
        final List<WorkMenuBean.ModuleListBean> mModuleListBeans = workMenuBean.getModuleList();
        final WorkFuncSetChildAdapter mChildAdapter = new WorkFuncSetChildAdapter(mContext, mModuleListBeans);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
        gridLayoutManager.setSmoothScrollbarEnabled(true);
        gridLayoutManager.setAutoMeasureEnabled(true);
        gridLayoutManager.setRecycleChildrenOnDetach(true);

        mRecycledViewPool = new RecyclerView.RecycledViewPool();
        mRecycledViewPool.setMaxRecycledViews(mChildAdapter.getItemViewType(0), 20);

        holder.parentRecyclerView.setLayoutManager(gridLayoutManager);
        holder.parentRecyclerView.setNestedScrollingEnabled(false);
        holder.parentRecyclerView.setRecycledViewPool(mRecycledViewPool);
        holder.parentRecyclerView.setAdapter(mChildAdapter);

        try {
            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    //首先回调的方法 返回int表示是否监听该方向
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    int fromPosition = viewHolder.getAdapterPosition();
                    int toPosition = target.getAdapterPosition();
                    if (fromPosition < toPosition) {
                        for (int i = fromPosition; i < toPosition; i++) {
                            Collections.swap(mChildAdapter.getModuleListBeans(), i, i + 1);
                        }
                    } else {
                        for (int i = fromPosition; i > toPosition; i--) {
                            Collections.swap(mChildAdapter.getModuleListBeans(), i, i - 1);
                        }
                    }
                    mChildAdapter.notifyItemMoved(fromPosition, toPosition);
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                }

                @Override
                public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
                    return true;
                }

                @Override
                public boolean isLongPressDragEnabled() {
                    //是否可拖拽
                    return true;
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                        viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.item_drag_color));
                        viewHolder.itemView.setScaleX(1.10f);
                        viewHolder.itemView.setScaleY(1.10f);
                    }
                    super.onSelectedChanged(viewHolder, actionState);
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                    viewHolder.itemView.setScaleX(1.0f);
                    viewHolder.itemView.setScaleY(1.0f);
                }
            });

            mItemTouchHelper.attachToRecyclerView(holder.parentRecyclerView);
        } catch (Exception e) {
            mChildAdapter.notifyDataSetChanged();
            e.printStackTrace();
        }


        mChildAdapter.setOnVisibleChangeListener(new WorkFuncSetChildAdapter.OnVisibleChangeListener() {
            @Override
            public void onVisibleChange(boolean visible, int position) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mWorkMenuBeans.size();
    }

    public static class ParentViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private RecyclerView parentRecyclerView;

        public ParentViewHolder(View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.work_func_set_parent_title_tv);
            parentRecyclerView = (RecyclerView) itemView.findViewById(R.id.work_func_set_parent_rv);
        }
    }
}
