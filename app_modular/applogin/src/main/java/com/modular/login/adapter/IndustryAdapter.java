package com.modular.login.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.core.widget.view.adapter.SecondaryListAdapter;
import com.modular.login.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/9/26.
 */

public class IndustryAdapter extends SecondaryListAdapter<IndustryAdapter.GroupItemViewHolder, IndustryAdapter.SubItemViewHolder> {
    private Context mContext;
    private List<Integer> mFlags;
    private List<SecondaryListBean<String, String>> mDatas;
    private OnSubItemClickListener mOnSubItemClickListener;

    public IndustryAdapter(Context context) {
        mContext = context;
        mFlags = new ArrayList<>();
    }

    public List<SecondaryListBean<String, String>> getDatas() {
        return mDatas;
    }

    public void setDatas(List<SecondaryListBean<String, String>> datas) {
        mDatas = datas;
        initSecondaryList(mDatas);
        setGroupItemStatu(0, true);
        for (int i = 0; i < mDatas.size(); i++) {
            if (i == 0) {
                mFlags.add(1);
            } else {
                mFlags.add(0);
            }
        }
    }

    public void setOnSubItemClickListener(OnSubItemClickListener onSubItemClickListener) {
        mOnSubItemClickListener = onSubItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateGroupViewHolder(ViewGroup parent) {
        View groupView = LayoutInflater.from(mContext).inflate(R.layout.item_group_layout, parent, false);
        return new GroupItemViewHolder(groupView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateSubViewHolder(ViewGroup parent) {
        View subView = LayoutInflater.from(mContext).inflate(R.layout.item_sub_layout, parent, false);
        return new SubItemViewHolder(subView);
    }

    @Override
    public void onBindGroupViewHolder(RecyclerView.ViewHolder holder, int groupItemIndex) {
        ((GroupItemViewHolder) holder).groupTextView.setText(mDatas.get(groupItemIndex).getGroupItem());

        Integer flag = mFlags.get(groupItemIndex);
        if (flag % 2 == 0) {
            ((GroupItemViewHolder) holder).groupImageView.setImageResource(R.drawable.ic_drop_down);
        } else {
            ((GroupItemViewHolder) holder).groupImageView.setImageResource(R.drawable.ic_menu_spread);
        }
    }

    @Override
    public void onBindSubViewHolder(RecyclerView.ViewHolder holder, int groupItemIndex, int subItemIndex) {
        ((SubItemViewHolder) holder).subTextView.setText(mDatas.get(groupItemIndex).getSubItems().get(subItemIndex));
    }

    @Override
    public void onGroupItemClick(boolean isExpand, GroupItemViewHolder holder, int groupItemIndex) {
//        Toast.makeText(mContext, mDatas.get(groupItemIndex).getGroupItem(), Toast.LENGTH_SHORT).show();
        Integer flag = mFlags.get(groupItemIndex);
        if (flag % 2 == 0) {
            holder.groupImageView.setImageResource(R.drawable.ic_menu_spread);
        } else {
            holder.groupImageView.setImageResource(R.drawable.ic_drop_down);
        }
        flag++;
        mFlags.remove(groupItemIndex);
        mFlags.add(groupItemIndex, flag);
    }

    @Override
    public void onSubItemClick(SubItemViewHolder holder, int groupItemIndex, int subItemIndex) {
//        Toast.makeText(mContext, mDatas.get(groupItemIndex).getGroupItem()
//                + "->" + mDatas.get(groupItemIndex).getSubItems().get(subItemIndex), Toast.LENGTH_SHORT).show();
        if (mOnSubItemClickListener != null) {
            String message = mDatas.get(groupItemIndex).getSubItems().get(subItemIndex);
            mOnSubItemClickListener.onSubItemClick(message);
        }
    }

    public class GroupItemViewHolder extends RecyclerView.ViewHolder {
        TextView groupTextView;
        ImageView groupImageView;

        public GroupItemViewHolder(View itemView) {
            super(itemView);

            groupTextView = (TextView) itemView.findViewById(R.id.group_tv);
            groupImageView = (ImageView) itemView.findViewById(R.id.group_iv);
        }
    }

    public class SubItemViewHolder extends RecyclerView.ViewHolder {
        TextView subTextView;

        public SubItemViewHolder(View itemView) {
            super(itemView);

            subTextView = (TextView) itemView.findViewById(R.id.sub_tv);
        }
    }

    public interface OnSubItemClickListener {
        void onSubItemClick(String message);
    }
}
