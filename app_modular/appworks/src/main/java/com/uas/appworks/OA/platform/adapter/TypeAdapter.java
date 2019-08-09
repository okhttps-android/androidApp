package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.ListUtils;
import com.uas.appworks.R;

import java.util.List;

/**
 * Created by Bitlike on 2017/11/8.
 */

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.ViewHolder> implements View.OnClickListener {

    private Context ct;
    private List<String> areaList = null;
    private int lastPosition = 0;
    private ChangeListener changeListener = null;



    public TypeAdapter(Context ct, List<String> areaList,int lastPosition, ChangeListener changeListener) {
        this.ct = ct;
        this.areaList = areaList;
        this.lastPosition = lastPosition;
        this.changeListener = changeListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(getLayoutInflater().inflate(R.layout.item_type_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.typeBtn.setText(areaList.get(position));
        if (lastPosition == position) {
            holder.typeBtn.setBackgroundResource(R.drawable.text_frame_circle_red_bg);
            holder.typeBtn.setTextColor(ct.getResources().getColor(R.color.indianred));
        } else {
            holder.typeBtn.setBackgroundResource(R.drawable.text_frame_circle_bg);
            holder.typeBtn.setTextColor(ct.getResources().getColor(R.color.hintColor));
        }
        holder.typeBtn.setPadding(20, 10, 20, 10);
        final ViewHolder finalHolder = holder;
//        finalHolder.typeBtn.post(new Runnable() {
//            @Override
//            public void run() {
//                int width = finalHolder.typeBtn.getWidth();
//                LogUtil.i(finalHolder.typeBtn.getText().toString() + "==" + width);
//            }
//        });
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(areaList);
    }


    @Override
    public void onClick(View view) {
        if (view != null && view.getTag() != null && view.getTag() instanceof Integer) {
            int upLast = lastPosition;
            lastPosition = (int) view.getTag();
            notifyItemChanged(upLast);
            notifyItemChanged(lastPosition);
            if (changeListener != null && ListUtils.getSize(areaList) > lastPosition) {
                changeListener.change(areaList.get(lastPosition));
            }
        }
    }

    public void clear() {
        int pos = lastPosition;
        lastPosition = -1;
        notifyItemChanged(pos);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            typeBtn = (TextView) itemView.findViewById(R.id.typeBtn);
        }
    }

    private LayoutInflater inflater;

    private LayoutInflater getLayoutInflater() {
        return inflater == null ? inflater = LayoutInflater.from(ct) : inflater;

    }


    public interface ChangeListener {
        void change(String type);
    }
}
