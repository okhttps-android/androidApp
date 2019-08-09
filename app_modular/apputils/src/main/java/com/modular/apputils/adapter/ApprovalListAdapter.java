package com.modular.apputils.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.modular.apputils.R;
import com.modular.apputils.model.ApprovalList;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ApprovalListAdapter extends RecyclerView.Adapter<ApprovalListAdapter.ViewHolder> implements Filterable {

    private Context ct;
    private List<ApprovalList> showModels;
    private List<ApprovalList> allModels;
    private int tabItem;
    private String whichsys ;

    public ApprovalListAdapter(Context ct, int tabItem, List<ApprovalList> approvalLists, OnItemClickListener mOnItemClickListener) {
        this.ct = ct;
        this.tabItem = tabItem;
        this.mOnItemClickListener = mOnItemClickListener;
        this.allModels = approvalLists;
        this.showModels = this.allModels;
        whichsys = CommonUtil.getSharedPreferences(ct, "erp_master");
    }

    public void setApprovalLists(List<ApprovalList> approvalLists) {
        this.allModels = approvalLists;
        this.showModels = this.allModels;
        notifyDataSetChanged();
    }

    public void addApprovalLists(List<ApprovalList> approvalLists) {
        if (ListUtils.isEmpty(this.allModels)) {
            setApprovalLists(approvalLists);
        } else {
            this.allModels.addAll(approvalLists);
            this.showModels = this.allModels;
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_approval_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ApprovalList model = showModels.get(position);
        String jpName = model.getName() == null ? "" : (model.getName().replace("流程", ""));
        String name = (!TextUtils.isEmpty(model.getLauncherName()) ? (model.getLauncherName() + "的") : "") + jpName;
        viewHolder.titleTv.setText(name);
        viewHolder.statusTv.setText(DateFormatUtil.long2Str(model.getDealTime(), "MM-dd HH:mm") + "");
        viewHolder.subTitle.setTextColor(ct.getResources().getColor(model.getSubTitleColor()));
        viewHolder.subTitle.setText(model.getSubTitle());
        viewHolder.orderNumTv.setText(model.getCodeValue());
        viewHolder.itemView.setTag(model);
        viewHolder.itemView.setOnClickListener(mOnClickListener);
        if (viewHolder.headImage.getVisibility() == View.VISIBLE) {
            String imid=model.getImid();
            if (TextUtils.isEmpty(imid)){
                imid=getImId(model.getLauncherCode());
                model.setImid(imid);
            }
            if (!StringUtil.isEmpty(model.getImid())) {
                AvatarHelper.getInstance().displayCircular(model.getImid(), viewHolder.headImage, true, false);//显示圆角图片
            } else {
                String imageUri = "drawable://" + R.drawable.common_header_boy;
                AvatarHelper.getInstance().displayCircular(imageUri, viewHolder.headImage, true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(showModels);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view != null & view.getTag() != null && view.getTag() instanceof ApprovalList && mOnItemClickListener != null) {
                ApprovalList model = (ApprovalList) view.getTag();
                mOnItemClickListener.itemClick(model);
            }

        }
    };

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void itemClick(ApprovalList model);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults searchResults = new FilterResults();
                List<ApprovalList> values = new ArrayList<>();
                if (constraint == null || constraint.length() <= 0) {
                    values = allModels;
                } else {
                    for (ApprovalList e : allModels) {
                        if (e.hasContext(constraint))
                            values.add(e);
                    }
                }
                searchResults.values = values;
                searchResults.count = values.size();
                return searchResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                showModels = (List<ApprovalList>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView headImage;
        TextView statusTv, titleTv, subTitle,orderNumTv;

        public ViewHolder(View itemView) {
            super(itemView);
            headImage = itemView.findViewById(R.id.headImage);
            statusTv = itemView.findViewById(R.id.statusTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            subTitle = itemView.findViewById(R.id.subTitle);
            orderNumTv = itemView.findViewById(R.id.orderNumTv);
            if (tabItem == 3) {
                headImage.setVisibility(View.GONE);
            } else {
                headImage.setVisibility(View.VISIBLE);
            }
        }
    }
    private String getImId(String em_code) {
        if (!TextUtils.isEmpty(em_code))try {
            String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
            String selection = "em_code=? and whichsys=? ";
            //获取数据库数据
            EmployeesEntity bean = DBManager.getInstance().selectForEmployee(selectionArgs, selection);
            if (bean != null) {
                String imId = String.valueOf(bean.getEm_IMID());
                return imId;
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
