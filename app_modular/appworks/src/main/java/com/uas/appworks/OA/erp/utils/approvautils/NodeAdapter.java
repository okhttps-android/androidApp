package com.uas.appworks.OA.erp.utils.approvautils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.model.Approval;
import com.core.utils.helper.AvatarHelper;
import com.uas.appworks.R;

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
            holder.statusTv = (TextView) itemView.findViewById(R.id.statusTv);
            holder.dateTv = (TextView) itemView.findViewById(R.id.dateTv);
            holder.lineBottom = itemView.findViewById(R.id.lineBottom);
            holder.lineTop = itemView.findViewById(R.id.lineTop);
            itemView.setTag(holder);
        } else {
            holder = (NodeViewHolder) itemView.getTag();
        }
        bindNodeView(holder, position);
        return itemView;
    }

    private void bindNodeView(NodeViewHolder holder, int position) {
        Approval approval = approvals.get(position);
        holder.padding.setVisibility(View.GONE);
        if (position == 0) {
            holder.lineTop.setVisibility(View.GONE);
        } else {
            holder.lineTop.setVisibility(View.VISIBLE);

        }
        if (position == (getCount() - 1)) {
            holder.lineBottom.setVisibility(View.GONE);
        } else {
            holder.lineBottom.setVisibility(View.VISIBLE);

        }

        int textColor = R.color.hintColor;
        int reId = R.drawable.daishenpi;
        String status = "";
        if (!StringUtil.isEmpty(approval.getIdKey())) {
            holder.valuesTv.setText(StringUtil.getFirstBrackets(approval.getIdKey()));
            if (approval.getIdKey().startsWith("待审批")) {
                textColor = R.color.approvaling;
                reId = R.drawable.daishenpi;
                status = "等待" + "" + "审批";
            } else if (approval.getIdKey().startsWith("未通过") || approval.getIdKey().startsWith("结束") || approval.getIdKey().startsWith("不同意")) {
                textColor = R.color.done_approval;
                reId = R.drawable.node_finished3;
                status = "不同意";
            } else if (approval.getIdKey().startsWith("已审批") || approval.getIdKey().startsWith("变更") || approval.getIdKey().startsWith("同意")) {
                reId = R.drawable.node_finished3;
                status = "已审批";
            }
        } else {
            holder.valuesTv.setText("");
        }
        holder.dateTv.setText(TextUtils.isEmpty(approval.getValues()) ? "" : approval.getValues());
        holder.timeTv.setText(TextUtils.isEmpty(approval.getDbFind()) ? "" : approval.getDbFind());
        holder.statusTv.setText(status);
        holder.statusTv.setTextColor(ct.getResources().getColor(textColor));
        holder.keyTv.setText(approval.getCaption());
        holder.statusIV.setImageResource(reId);
        AvatarHelper.getInstance().display(String.valueOf(approval.getId()), holder.handIv, true, false);
    }


    private class NodeViewHolder {
        ImageView handIv, statusIV;
        TextView timeTv, keyTv, valuesTv;
        View padding;
        TextView statusTv, dateTv;
        View lineBottom, lineTop;
    }
}
