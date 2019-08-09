package com.uas.appworks.OA.erp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 工作汇报菜单内容适配器
 * @date 2017/10/17
 */

public class WorkReportMenuAdapter extends RecyclerView.Adapter<WorkReportMenuAdapter.WorkReportMenuViewHolder> {
    private List<String> mMenuList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public WorkReportMenuAdapter(Context context) {
        mContext = context;
        mMenuList = new ArrayList<>();
        mMenuList.add("日报");
        mMenuList.add("周报");
        mMenuList.add("月报");
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public WorkReportMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View menuView = LayoutInflater.from(mContext).inflate(R.layout.item_work_report_menu, parent, false);
        return new WorkReportMenuViewHolder(menuView);
    }

    @Override
    public void onBindViewHolder(WorkReportMenuViewHolder holder, final int position) {
        holder.menuTextView.setText(mMenuList.get(position));
        if (position == 0) {
            holder.menuImageView.setImageResource(R.drawable.ic_work_report_day);
        } else if (position == 1) {
            holder.menuImageView.setImageResource(R.drawable.ic_work_report_week);
        } else if (position == 2) {
            holder.menuImageView.setImageResource(R.drawable.ic_work_report_month);
        }

        holder.menuLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMenuList == null ? 0 : mMenuList.size();
    }

    public static class WorkReportMenuViewHolder extends RecyclerView.ViewHolder {
        private ImageView menuImageView;
        private TextView menuTextView;
        private LinearLayout menuLinearLayout;

        public WorkReportMenuViewHolder(View itemView) {
            super(itemView);
            menuImageView = (ImageView) itemView.findViewById(R.id.item_work_report_menu_iv);
            menuTextView = (TextView) itemView.findViewById(R.id.item_work_report_menu_tv);
            menuLinearLayout = (LinearLayout) itemView.findViewById(R.id.item_work_report_menu_ll);
        }
    }

    public interface OnItemClickListener {
        /**
         * item点击事件
         *
         * @param view
         * @param positon
         */
        void onItemClick(View view, int positon);
    }
}
