package com.modular.appmessages.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.core.widget.CircleTextView;
import com.modular.appmessages.R;
import com.modular.appmessages.model.RealTimeFormMenuBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe 实时看板菜单适配器
 * @date 2017/10/25 19:05
 */

public class RealTimeFormMenuAdapter extends RecyclerView.Adapter<RealTimeFormMenuAdapter.RealTimeMenuViewHolder> {
    private Context mContext;
    private List<RealTimeFormMenuBean> mRealTimeFormMenuBeen;
    private OnItemClickListener mOnItemClickListener;

    public RealTimeFormMenuAdapter(Context context, List<RealTimeFormMenuBean> realTimeFormMenuBeen) {
        mContext = context;
        mRealTimeFormMenuBeen = realTimeFormMenuBeen;
    }

    public List<RealTimeFormMenuBean> getRealTimeFormMenuBeen() {
        return mRealTimeFormMenuBeen;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public RealTimeMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_grid_real_time_form_menu, parent, false);
        return new RealTimeMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RealTimeMenuViewHolder holder, final int position) {
        String title = mRealTimeFormMenuBeen.get(position).getTitle();
        holder.contentTextView.setText(title);
        if (title != null && title.length() >= 1) {
            holder.iconTextView.setText(title.substring(0, 1));
        }
        holder.iconTextView.setMyBackgroundColor(mContext.getResources().getColor(mRealTimeFormMenuBeen.get(position).getColor()));
        holder.menuLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRealTimeFormMenuBeen == null ? 0 : mRealTimeFormMenuBeen.size();
    }


    public static class RealTimeMenuViewHolder extends RecyclerView.ViewHolder {
        private CircleTextView iconTextView;
        private TextView contentTextView;
        private LinearLayout menuLinearLayout;

        public RealTimeMenuViewHolder(View itemView) {
            super(itemView);

            iconTextView = (CircleTextView) itemView.findViewById(R.id.real_time_form_menu_icon);
            contentTextView = (TextView) itemView.findViewById(R.id.real_time_form_menu_content);
            menuLinearLayout = (LinearLayout) itemView.findViewById(R.id.real_time_form_menu_ll);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
