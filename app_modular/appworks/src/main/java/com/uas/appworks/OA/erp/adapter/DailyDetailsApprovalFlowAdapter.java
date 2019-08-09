package com.uas.appworks.OA.erp.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.core.app.MyApplication;
import com.core.dao.DBManager;
import com.core.utils.helper.AvatarHelper;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.model.CommonApprovalFlowBean;

import java.util.List;


/**
 * Created by FANGlh on 2016/11/4.
 */
public class DailyDetailsApprovalFlowAdapter extends BaseAdapter {
    private CommonApprovalFlowBean mCommonApprovalFlowBean;
    private Context mContext;
    DBManager manager;
    private List<String> im_ids;
    private List<String> afpeople_names;

    public List<String> getAfpeople_names() {
        return afpeople_names;
    }

    public void setAfpeople_names(List<String> afpeople_names) {
        this.afpeople_names = afpeople_names;
    }

    public List<String> getIm_ids() {
        return im_ids;
    }

    public void setIm_ids(List<String> im_ids) {
        this.im_ids = im_ids;
    }

    public DailyDetailsApprovalFlowAdapter(Context mContext) {
        manager = new DBManager(mContext);
        this.mContext = mContext;
    }

    public CommonApprovalFlowBean getmCommonApprovalFlowBean() {
        return mCommonApprovalFlowBean;
    }

    public void setmCommonApprovalFlowBean(CommonApprovalFlowBean mCommonApprovalFlowBeanList) {
        this.mCommonApprovalFlowBean = mCommonApprovalFlowBeanList;
    }

    public int getCount() {
        if (ListUtils.isEmpty(mCommonApprovalFlowBean.getData())){
            return mCommonApprovalFlowBean == null ? 0 : mCommonApprovalFlowBean.getNodes().size();
        }else{
            return mCommonApprovalFlowBean == null ? 0 : mCommonApprovalFlowBean.getData().size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (ListUtils.isEmpty(mCommonApprovalFlowBean.getData())){
            return mCommonApprovalFlowBean.getNodes().get(position);
        }else {
            return mCommonApprovalFlowBean.getData().get(position);
        }
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_daily_details_approval_flow, null);
            viewHolder = new ViewHolder();
            viewHolder.AppFlowNode = (ImageView) convertView.findViewById(R.id.item_daily_details_approval_flow_node_img);
            viewHolder.AppFlowPhoto = (ImageView) convertView.findViewById(R.id.item_daily_details_approval_flow_photo_img);
            viewHolder.AppFlowName = (TextView) convertView.findViewById(R.id.item_daily_details_approval_flow_name_tv);
            viewHolder.AppFlowStatus = (TextView) convertView.findViewById(R.id.item_daily_details_approval_flow_status_tv);
            viewHolder.AppFlowTime = (TextView) convertView.findViewById(R.id.item_daily_details_approval_flow_time_tv);
            viewHolder.AppFlowDescriptionll = (LinearLayout) convertView.findViewById(R.id.Description_ll);
            viewHolder.AppFlowDescriptiontv = (TextView) convertView.findViewById(R.id.item_daily_details_approval_flow_statusDescription_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 日报详情审批流过程显示，逻辑比较繁琐
        final int nodesnum = mCommonApprovalFlowBean.getNodes().size();
        final int datanum = mCommonApprovalFlowBean.getData().size();
        //设置显示审批人头像
        if (ListUtils.isEmpty(im_ids)) {
            String imageUri = "drawable://" + R.drawable.common_header_boy;
            AvatarHelper.getInstance().display(imageUri, viewHolder.AppFlowPhoto, true);
        } else if (!ListUtils.isEmpty(im_ids)){
            AvatarHelper.getInstance().display(im_ids.get(getCount() - position - 1), viewHolder.AppFlowPhoto, true, false);//显示圆角图片
        }
        //设置显示审批人名字
        if (ListUtils.isEmpty(afpeople_names)){
            viewHolder.AppFlowName.setText("");
        }else if (!ListUtils.isEmpty(afpeople_names)){
            viewHolder.AppFlowName.setText(afpeople_names.get(getCount() - position - 1));
        }

        //   审批流倒序排列
        if (nodesnum == 0) {
            viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
            if (mCommonApprovalFlowBean.getCurrentnode().getNodename()
                    .equals(mCommonApprovalFlowBean.getData().get(getCount() - position - 1).getJP_NODENAME())) {
                viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_pending));
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_wait3);
                viewHolder.AppFlowTime.setText("");
            } else {
                viewHolder.AppFlowStatus.setText("");
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_none3);
            }
        } else if (datanum !=0 && nodesnum != 0){
            if (position > (datanum - nodesnum - 1)) { //已走过审批流的节点
                if (mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_dealResult().equals("同意")) {
                    viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                    viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_approved));
                    viewHolder.AppFlowNode.setImageResource(R.drawable.node_finished3);
                    viewHolder.AppFlowTime.setText(mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_dealTime());

                } else if (mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_dealResult().equals("不同意")) {
                    viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                    viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.common_disagree));
                    viewHolder.AppFlowNode.setImageResource(R.drawable.icon_disagree_nor);
                    viewHolder.AppFlowTime.setText(mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_dealTime());
                }
                //当已审批时判断有没有审批语
                if (mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_nodeDescription() != null) {
                    viewHolder.AppFlowDescriptionll.setVisibility(View.VISIBLE);
                    viewHolder.AppFlowDescriptiontv.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                    viewHolder.AppFlowDescriptiontv.setText("( " + mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_nodeDescription().toString() + " )");
                    final String des_msg = mCommonApprovalFlowBean.getNodes().get(datanum - position - 1).getJn_nodeDescription().toString();
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
            } else if (position == (datanum - nodesnum - 1) ) { //当前待处理的节点（nodes里面将要统计的数据）
                if (mCommonApprovalFlowBean.getNodes().get(datanum -position -2).getJn_dealResult().equals("同意")) {
                    viewHolder.AppFlowNode.setImageResource(R.drawable.node_wait3);
                    viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
                    viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_pending));
                }else {
                    viewHolder.AppFlowNode.setImageResource(R.drawable.node_none3);
                    viewHolder.AppFlowStatus.setText("");
                }
                viewHolder.AppFlowTime.setText("");
                viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
            } else if (position < (datanum - nodesnum - 1)) {    //未走过审批流的节点
                viewHolder.AppFlowStatus.setText("");
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_none3);
                viewHolder.AppFlowTime.setText("");
                viewHolder.AppFlowDescriptionll.setVisibility(View.GONE);
            }
        }else if (datanum == 0 && nodesnum != 0){  // 兼容data 为空情况
            if (mCommonApprovalFlowBean.getNodes().get(nodesnum - position - 1).getJn_dealResult().equals("同意")) {
                viewHolder.AppFlowStatus.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                viewHolder.AppFlowStatus.setText(MyApplication.getInstance().getString(R.string.status_approved));
                viewHolder.AppFlowNode.setImageResource(R.drawable.node_finished3);
                viewHolder.AppFlowTime.setText(mCommonApprovalFlowBean.getNodes().get(nodesnum - position - 1).getJn_dealTime());

                //当已审批时判断有没有审批语
                if (mCommonApprovalFlowBean.getNodes().get(nodesnum - position - 1).getJn_nodeDescription() != null) {
                    viewHolder.AppFlowDescriptionll.setVisibility(View.VISIBLE);
                    viewHolder.AppFlowDescriptiontv.setTextColor(mContext.getResources().getColor(R.color.done_approval));
                    viewHolder.AppFlowDescriptiontv.setText("( " + mCommonApprovalFlowBean.getNodes().get(nodesnum - position - 1).getJn_nodeDescription().toString() + " )");
                    final String des_msg = mCommonApprovalFlowBean.getNodes().get(nodesnum - position - 1).getJn_nodeDescription().toString();
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
