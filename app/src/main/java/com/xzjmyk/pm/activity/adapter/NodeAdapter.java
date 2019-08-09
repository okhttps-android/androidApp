package com.xzjmyk.pm.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.xzjmyk.pm.activity.R;
import com.core.model.Approval;
import com.core.utils.helper.AvatarHelper;

import java.util.List;

/**
 * Created by Bitliker on 2017/8/11.
 */

public class NodeAdapter extends BaseAdapter {

    private Context ct;
    private List<Approval> approvals;


    public NodeAdapter(Context ct, List<Approval> approvals) {
        this.ct = ct;
        this.approvals = approvals;
    }

    @Override
    public int getCount() {
        return ListUtils.getSize(approvals);
    }

    @Override
    public Object getItem(int position) {
        return approvals.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        NodeViewHolder holder = null;
        if (itemView == null) {
            holder = new NodeViewHolder();
            itemView = LayoutInflater.from(ct).inflate(R.layout.item_approval_node, parent, false);
            holder.timeTv = (TextView) itemView.findViewById(R.id.timeTv);
            holder.keyTv = (TextView) itemView.findViewById(R.id.keyTv);
            holder.valuesTv = (TextView) itemView.findViewById(R.id.valuesTv);
            holder.handIv = (ImageView) itemView.findViewById(R.id.handIv);
            holder.statusIV = (ImageView) itemView.findViewById(R.id.statusIV);
            holder.padding = itemView.findViewById(R.id.padding);
            itemView.setTag(holder);
        } else {
            holder = (NodeViewHolder) itemView.getTag();
        }
        bindNodeView(holder, position);
        return itemView;
    }

    private void bindNodeView(NodeViewHolder holder, int position) {
        Approval approval = approvals.get(position);
        if (position > 0 && approvals.get(position - 1).getType() != Approval.NODES) {
            holder.padding.setVisibility(View.VISIBLE);
        } else {
            holder.padding.setVisibility(View.GONE);
        }
        int textColor = R.color.hintColor;
        if (!StringUtil.isEmpty(approval.getIdKey())) {
            holder.valuesTv.setText(approval.getIdKey());
            if (approval.getIdKey().startsWith("待审批")) {
                textColor = R.color.approvaling;
            } else if (approval.getIdKey().startsWith("未通过")) {
                textColor = R.color.crimson;
            }
        } else {
            holder.valuesTv.setText("");
        }
        holder.valuesTv.setTextColor(ct.getResources().getColor(textColor));
        holder.keyTv.setText(approval.getCaption());
        int reId = R.drawable.weishenpi;
        if (!StringUtil.isEmpty(approval.getIdKey())) {
            String status = approval.getIdKey().split("\\(")[0];
            if (StringUtil.isEmpty(status)) status = "";
            if ("已审批".equals(status)) {
                reId = R.drawable.node_finished3;
            } else if ("待审批".equals(status)) {
                reId = R.drawable.daishenpi;
            } else if ("未通过".equals(status)) {
                reId = R.drawable.node_delete;
            } else {
                reId = R.drawable.weishenpi;
            }
        }
        holder.statusIV.setImageResource(reId);
        AvatarHelper.getInstance().display(String.valueOf(approval.getId()), holder.handIv, true, false);
        holder.timeTv.setTextColor(ct.getResources().getColor(R.color.text_normal));
        holder.timeTv.setText(approval.getValues());
    }

    private class NodeViewHolder {
        ImageView handIv, statusIV;
        TextView timeTv, keyTv, valuesTv;
        View padding;


    }
}
