package com.core.widget.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/21.
 * recyclerview二级列表适配器基类
 */

public abstract class SecondaryListAdapter<GVH, SVH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {
    private List<Boolean> mGroupItemStatus;
    private List<SecondaryListBean> mSecondaryListBeen;

    /**
     * 创建分组布局ViewHolder
     *
     * @param parent
     * @return
     */
    public abstract RecyclerView.ViewHolder onCreateGroupViewHolder(ViewGroup parent);

    /**
     * 创建子项布局ViewHolder
     *
     * @param parent
     * @return
     */
    public abstract RecyclerView.ViewHolder onCreateSubViewHolder(ViewGroup parent);

    /**
     * 绑定分组布局ViewHolder
     *
     * @param holder
     * @param groupItemIndex
     */
    public abstract void onBindGroupViewHolder(RecyclerView.ViewHolder holder, int groupItemIndex);

    /**
     * 绑定子项布局ViewHolder
     *
     * @param holder
     * @param groupItemIndex
     * @param subItemIndex
     */
    public abstract void onBindSubViewHolder(RecyclerView.ViewHolder holder, int groupItemIndex, int subItemIndex);

    /**
     * 一级列表点击事件
     *
     * @param isExpand
     * @param holder
     * @param groupItemIndex
     */
    public abstract void onGroupItemClick(boolean isExpand, GVH holder, int groupItemIndex);

    /**
     * 二级子项点击事件
     *
     * @param holder
     * @param groupItemIndex
     * @param subItemIndex
     */
    public abstract void onSubItemClick(SVH holder, int groupItemIndex, int subItemIndex);

    /**
     * 初始化数据源
     *
     * @param secondaryListBeen
     */
    public void initSecondaryList(List secondaryListBeen) {
        mGroupItemStatus = new ArrayList<>();
        mSecondaryListBeen = new ArrayList<>();

        setSecondaryListBeen(secondaryListBeen);
    }

    public void setSecondaryListBeen(List secondaryListBeen) {
        mSecondaryListBeen = secondaryListBeen;
        initGroupItemStatus();
        notifyDataSetChanged();
    }

    /**
     * 初始化一级列表展开状态，默认全部收起
     */
    public void initGroupItemStatus() {
        for (int i = 0; i < mSecondaryListBeen.size(); i++) {
            mGroupItemStatus.add(false);
        }
    }

    /**
     * 设置某项一级列表的展开装状态
     *
     * @param groupIndex
     * @param isExpand
     */
    public void setGroupItemStatu(int groupIndex, boolean isExpand) {
        if (mGroupItemStatus != null && mGroupItemStatus.size() > groupIndex) {
            mGroupItemStatus.set(groupIndex, isExpand);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ItemStatus.TYPE_GROUPITEM) {
            viewHolder = onCreateGroupViewHolder(parent);
        } else if (viewType == ItemStatus.TYPE_SUBITEM) {
            viewHolder = onCreateSubViewHolder(parent);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemStatus itemStatus = getItemStatusByPosition(position);

        if (itemStatus.getItemType() == ItemStatus.TYPE_GROUPITEM) {
            onBindGroupViewHolder(holder, itemStatus.getGroupIndex());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int groupIndex = itemStatus.getGroupIndex();
                    boolean groupStatus = mGroupItemStatus.get(groupIndex);

                    onGroupItemClick(groupStatus, (GVH) holder, groupIndex);
                    mGroupItemStatus.set(groupIndex, !groupStatus);

                    //这里必须要用holder.getAdapterPosition()来确认点击的位置
                    if (groupStatus) {
                        notifyItemRangeRemoved(holder.getAdapterPosition() + 1, mSecondaryListBeen.get(groupIndex).getSubItems().size());
                    } else {
                        notifyItemRangeInserted(holder.getAdapterPosition() + 1, mSecondaryListBeen.get(groupIndex).getSubItems().size());
                    }
                }
            });
        } else if (itemStatus.getItemType() == ItemStatus.TYPE_SUBITEM) {
            onBindSubViewHolder(holder, itemStatus.getGroupIndex(), itemStatus.getSubIndex());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSubItemClick((SVH) holder, itemStatus.getGroupIndex(), itemStatus.getSubIndex());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;

        if (mGroupItemStatus.size() == 0) {
            return 0;
        }

        for (int i = 0; i < mGroupItemStatus.size(); i++) {
            if (mGroupItemStatus.get(i)) {
                itemCount += mSecondaryListBeen.get(i).getSubItems().size() + 1;
            } else {
                itemCount++;
            }
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemStatusByPosition(position).getItemType();
    }

    /**
     * 获取条目状态信息
     *
     * @param position
     * @return
     */
    private ItemStatus getItemStatusByPosition(int position) {
        ItemStatus itemStatus = new ItemStatus();

        int i = 0;
        int groupItemPosition = 0;
        for (i = 0; i < mGroupItemStatus.size(); i++) {
            if (position == groupItemPosition) {
                itemStatus.setItemType(ItemStatus.TYPE_GROUPITEM);
                itemStatus.setGroupIndex(i);
                break;
            }

            if (position < groupItemPosition) {
                itemStatus.setItemType(ItemStatus.TYPE_SUBITEM);
                itemStatus.setGroupIndex(i - 1);
                itemStatus.setSubIndex(position - (groupItemPosition
                        - mSecondaryListBeen.get(i - 1).getSubItems().size()));
                break;
            }

            groupItemPosition++;

            if (mGroupItemStatus.get(i)) {
                groupItemPosition += mSecondaryListBeen.get(i).getSubItems().size();
            }

        }

        if (i >= mGroupItemStatus.size()) {
            itemStatus.setItemType(ItemStatus.TYPE_SUBITEM);
            itemStatus.setGroupIndex(i - 1);
            itemStatus.setSubIndex(position - (groupItemPosition
                    - mSecondaryListBeen.get(i - 1).getSubItems().size()));
        }

        return itemStatus;
    }

    /**
     * 条目状态类
     */
    private static class ItemStatus {
        public static final int TYPE_GROUPITEM = 0;
        public static final int TYPE_SUBITEM = 1;

        private int itemType;
        private int groupIndex = 0;
        private int subIndex = -1;

        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public void setGroupIndex(int groupIndex) {
            this.groupIndex = groupIndex;
        }

        public int getSubIndex() {
            return subIndex;
        }

        public void setSubIndex(int subIndex) {
            this.subIndex = subIndex;
        }
    }

    /**
     * 二级列表泛型实体类
     *
     * @param <G> 一级实体类
     * @param <S> 二级实体类
     */
    public final static class SecondaryListBean<G, S> {
        private G groupItem;
        private List<S> subItems;

        public SecondaryListBean(G groupItem, List<S> subItems) {
            this.groupItem = groupItem;
            this.subItems = subItems;
        }

        public G getGroupItem() {
            return groupItem;
        }

        public void setGroupItem(G groupItem) {
            this.groupItem = groupItem;
        }

        public List<S> getSubItems() {
            return subItems;
        }

        public void setSubItems(List<S> subItems) {
            this.subItems = subItems;
        }
    }
}
