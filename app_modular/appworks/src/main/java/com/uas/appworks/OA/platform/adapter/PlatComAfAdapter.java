package com.uas.appworks.OA.platform.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.utils.helper.AvatarHelper;
import com.uas.appworks.R;
import com.uas.appworks.OA.platform.model.PlatComAfBean;

import java.util.List;

/**
 * Created by FANGlh on 2017/3/18.
 * function: 所有单据审批流节点公用界面
 */
public class PlatComAfAdapter extends BaseAdapter{
    private PlatComAfBean mPlatComAfBean;
    private Context mContext;
    private List<String> afpeople_names;

    public List<String> getAfpeople_names() {
        return afpeople_names;
    }

    public void setAfpeople_names(List<String> afpeople_names) {
        this.afpeople_names = afpeople_names;
    }

    public PlatComAfAdapter(Context mContext){
        this.mContext = mContext;
    }

    public PlatComAfBean getmPlatComAfBean() {
        return mPlatComAfBean;
    }

    public void setmPlatComAfBean(PlatComAfBean mPlatComAfBean) {
        this.mPlatComAfBean = mPlatComAfBean;
    }

    @Override
    public int getCount() {
        if (mPlatComAfBean != null){
            if (!ListUtils.isEmpty(mPlatComAfBean.getData())){
                return mPlatComAfBean.getData()== null ? 0 : mPlatComAfBean.getData().size();
            }else{
                return mPlatComAfBean.getNodes() == null ? 0 : mPlatComAfBean.getNodes().size();
            }
        }else {
            return 0;
        }

    }

    @Override
    public Object getItem(int position) {
        if (mPlatComAfBean != null){
            if (!ListUtils.isEmpty(mPlatComAfBean.getData())){
                return mPlatComAfBean.getData().get(position);
            }else {
                return mPlatComAfBean.getNodes().get(position);
            }
        }else {
            return 0;
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_common_doc_approval_flow, null);
            viewHolder = new ViewHolder();
            viewHolder.AppFlowNode = (ImageView) convertView.findViewById(R.id.item_common_doc_approval_flow_node_img);
            viewHolder.AppFlowPhoto = (ImageView) convertView.findViewById(R.id.item_dcommon_doc_approval_flow_photo_img);
            viewHolder.AppFlowName = (TextView) convertView.findViewById(R.id.item_common_doc_approval_flow_name_tv);
            viewHolder.AppFlowStatus = (TextView) convertView.findViewById(R.id.item_common_doc_approval_flow_status_tv);
            viewHolder.AppFlowTime = (TextView) convertView.findViewById(R.id.item_common_doc_approval_flow_time_tv);
            viewHolder.AppFlowDescriptionll = (LinearLayout) convertView.findViewById(R.id.Description_ll);
            viewHolder.AppFlowDescriptiontv = (TextView) convertView.findViewById(R.id.item_common_doc_approval_flow_statusDescription_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String imageUri = "drawable://" + R.drawable.common_header_boy;
        AvatarHelper.getInstance().display(imageUri, viewHolder.AppFlowPhoto, true);

        //获取 已审批节点数量 和总审批节点数
        final int nodesnum = mPlatComAfBean.getNodes().size();
        int datanum = mPlatComAfBean.getData().size();

        viewHolder.AppFlowName.setText(mPlatComAfBean.getData().get(getCount() - position - 1).getJp_nodename());
        //设置显示审批人名字
        if (ListUtils.isEmpty(afpeople_names) ){
            viewHolder.AppFlowName.setText("");
        }else if (!ListUtils.isEmpty(afpeople_names) ){
            viewHolder.AppFlowName.setText(afpeople_names.get(getCount() - position - 1));
        }

        if (nodesnum == 0){
            viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
            if (position == datanum - 1){
                viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_pending));
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_wait3);
                viewHolder.AppFlowTime.setText("");
            }else {
                viewHolder.AppFlowStatus.setText("");
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_none3);
            }
        }else if (datanum != 0 && nodesnum != 0){
            if (position > (datanum - nodesnum - 1)){//已走过审批流的节点
                if (!StringUtil.isEmpty(mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_result()) &&
                        mPlatComAfBean.getNodes().get(datanum - position -1).getJn_result().equals("同意")){
                    viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                    viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_approved));
                    viewHolder.AppFlowNode.setImageResource(R.drawable.node_finished3);
                    viewHolder.AppFlowTime.setText(mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_dealTime());

                }else if (!StringUtil.isEmpty(mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_result()) &&
                        mPlatComAfBean.getNodes().get(datanum -position -1).getJn_result().equals("不同意")){
                    viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                    viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.common_disagree));
                    viewHolder.AppFlowNode.setImageResource(R.drawable.icon_disagree_nor);
                    viewHolder.AppFlowTime.setText(mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_dealTime());
                }

                //当已审批时判断有没有审批语
                if (mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_nodeDescription() != null) {
                    viewHolder.AppFlowDescriptionll.setVisibility(View.VISIBLE);
                    viewHolder.AppFlowDescriptiontv.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                    viewHolder.AppFlowDescriptiontv.setText("( " + mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_nodeDescription().toString() + " )");
                    final String des_msg = mPlatComAfBean.getNodes().get(datanum - position - 1).getJn_nodeDescription().toString();
                    viewHolder.AppFlowDescriptionll.setOnClickListener(new View.OnClickListener() {
                        //点击查看详细批语
                        @Override
                        public void onClick(View v) {
                            new AlertDialog
                                    .Builder(mContext)
                                    .setTitle(MyApplication.getInstance().getString(R.string.approvel_detail))
                                    .setMessage("\t" + des_msg)
                                    .setPositiveButton(MyApplication.getInstance().getString(R.string.common_sure), null)
                                            .show();
                        }
                    });
                } else {
                    viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
                    viewHolder.AppFlowDescriptiontv.setText("");
                }
            }else if (position == (datanum - nodesnum -1)){//当前待处理的节点（nodes里面将要统计的数据）
                if (!StringUtil.isEmpty(mPlatComAfBean.getNodes().get(datanum - position - 2).getJn_result()) &&
                        mPlatComAfBean.getNodes().get(datanum - position - 2).getJn_result().equals("同意")){
                    viewHolder.AppFlowNode.setImageResource(R.drawable.node_wait3);
                    viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                    viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_pending));
                }else {
                    viewHolder.AppFlowNode.setImageResource(R.drawable.node_none3);
                    viewHolder.AppFlowStatus.setText("");
                }
                viewHolder.AppFlowTime.setText("");
                viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
            }else if (position < (datanum - nodesnum - 1)) {    //未走过审批流的节点
                viewHolder.AppFlowStatus.setText("");
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_none3);
                viewHolder.AppFlowTime.setText("");
                viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView AppFlowNode;
        ImageView AppFlowPhoto;
        TextView AppFlowName;
        TextView AppFlowStatus;
        TextView AppFlowTime;
        LinearLayout AppFlowDescriptionll;
        TextView AppFlowDescriptiontv;
    }
}
