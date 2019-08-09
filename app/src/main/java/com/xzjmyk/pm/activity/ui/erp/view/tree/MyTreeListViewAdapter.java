package com.xzjmyk.pm.activity.ui.erp.view.tree;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.uas.appworks.OA.erp.model.BaseNodeBean;
import com.uas.appworks.OA.erp.model.HrorgsModel;
import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;
import java.util.List;

public class MyTreeListViewAdapter<T extends BaseNodeBean> extends TreeListViewAdapter<T> {


    public MyTreeListViewAdapter(ListView mTree, Context context,
                                 List<T> datas, int defaultExpandLevel, boolean isHide)
            throws IllegalArgumentException, IllegalAccessException {
        super(mTree, context, datas, defaultExpandLevel, isHide);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getConvertView(Node node, int position, View convertView,
                               ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(MyApplication.getInstance()).
                    inflate(R.layout.item_department, parent, false);
            viewHolder.node_cb = (CheckBox) convertView.findViewById(R.id.node_cb);
            viewHolder.tagImage = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.node_value = (TextView) convertView.findViewById(R.id.node_value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(viewHolder, node);

        return convertView;
    }


    private void initView(ViewHolder viewHolder, final Node node) {
        viewHolder.node_value.setText(node.getName());
        viewHolder.tagImage.setVisibility(View.VISIBLE);
        if (R.drawable.xiaji_pass == node.getIcon())
            viewHolder.tagImage.setVisibility(View.GONE);
        viewHolder.tagImage.setImageResource(node.getIcon());
        if (!node.isHideChecked()) {
            viewHolder.node_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (node.isInit()) {
                        node.setInit(false);
                        return;
                    }
                    TreeHelper.setNodeChecked(node, isChecked);
                    List<Node> checkedNodes = new ArrayList<>();
                    boolean isSelectAll = true;
                    for (Node n : mAllNodes) {
                        if (n.isChecked()) {
                            checkedNodes.add(n);
                        } else isSelectAll = false;
                    }
                    OnTreeNodeClickListener onTreeNodeClickListener = getOnTreeNodeClickListener();
                    if (onTreeNodeClickListener != null)
                        onTreeNodeClickListener.onCheckChange(node, 0, isSelectAll, checkedNodes);
                    MyTreeListViewAdapter.this.notifyDataSetChanged();
                }

            });
            viewHolder.node_cb.setChecked(node.isChecked());
            node.setInit(false);
        }

    }

    public void selectAll() {
        changeCB(true);
    }

    public void deleteAll() {
        changeCB(false);
    }

    public void setClick(String selectCode) {
        if (StringUtil.isEmpty(selectCode) || ListUtils.isEmpty(mAllNodes)) return;
        for (int i = 0; i < mAllNodes.size(); i++) {
            if (StringUtil.isInclude(selectCode, mAllNodes.get(i).getName()))
                mAllNodes.get(i).setChecked(true);
        }

    }

    public ArrayList<HrorgsModel> getClick() {
        ArrayList<HrorgsModel> checkedNodes = new ArrayList<>();
        for (Node n : mAllNodes) {
            if (n.isChecked() && n.getT() instanceof HrorgsModel) {
                checkedNodes.add((HrorgsModel) n.getT());
            }
        }
        return checkedNodes;
    }

    public List<Node> getAllNodes() {
        return mAllNodes;
    }

    private void changeCB(boolean isClick) {
        for (Node n : mAllNodes) {
            n.setChecked(isClick);
        }
        notifyDataSetChanged();
    }

    private final class ViewHolder {
        CheckBox node_cb;
        ImageView tagImage;
        TextView node_value;
    }

}
