package com.uas.appworks.OA.erp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.model.FlightsModel;

import java.util.List;


/**
 * Created by Bitliker on 2017/1/17.
 */

public class FlightsAdapter extends RecyclerView.Adapter<FlightsAdapter.FlightViewHolder> {

    private List<FlightsModel> models;

    public FlightsAdapter(List<FlightsModel> models) {
        this.models = models;
    }

    public List<FlightsModel> getModels() {
        return models;
    }

    public void setModels(List<FlightsModel> models) {
        this.models = models;
    }

    @Override
    public FlightViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_flights, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlightViewHolder holder, final int position) {
        try {
            final FlightsModel model = models.get(position);
            setType(holder, model.getType());//设置隐藏和显示
            holder.rule_name_tv.setText(StringUtil.getMessage(model.getName()));
            holder.mumber_tv.setText(getNumber(model.getCount(), model.getDepartments()));
            if (model.getTimeModel() != null) {
                String week = model.getWeek() + " " + model.getTime();
                holder.week_tv.setText(week);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null)
                        onItemClickListener.click(model, position, true);
                }
            });
            holder.delete_rule_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null)
                        onItemClickListener.click(model, position, false);
                }
            });
        } catch (Exception e) {

        }
    }

    //  1、正常班次（可有排版 可删除）  2.默认班次 （有排版  无删除） 3.自由排班
    private void setType(FlightViewHolder holder, int type) {
        if (type == 1) {//正常班次
            holder.week_tag_tv.setVisibility(View.VISIBLE);
            holder.week_tv.setVisibility(View.VISIBLE);
            holder.delete_rule_img.setVisibility(View.VISIBLE);
        } else if (type == 2) {//默认班次
            holder.week_tag_tv.setVisibility(View.VISIBLE);
            holder.week_tv.setVisibility(View.VISIBLE);
            holder.delete_rule_img.setVisibility(View.GONE);
        } else {//自由排班
            holder.week_tag_tv.setVisibility(View.GONE);
            holder.week_tv.setVisibility(View.GONE);
            holder.delete_rule_img.setVisibility(View.GONE);
        }
    }

    private String getNumber(int count, int departments) {
        if (departments == 0 && count == 0)
            return 0 + getString(R.string.a_person);
        return (departments == 0 ? "" : (departments + getString(R.string.a_department))) + (count == 0 ? "" : (count +getString(R.string.a_person)));
    }

    private String getString(int id) {
        return MyApplication.getInstance().getString(id);
    }

    @Override
    public int getItemCount() {
        return ListUtils.isEmpty(models) ? 0 : models.size();
    }

    class FlightViewHolder extends RecyclerView.ViewHolder {
        TextView rule_name_tv,
                mumber_tv,
                week_tag_tv,
                week_tv;
        ImageView delete_rule_img;

        public FlightViewHolder(View itemView) {
            super(itemView);
            rule_name_tv = (TextView) itemView.findViewById(R.id.rule_name_tv);
            mumber_tv = (TextView) itemView.findViewById(R.id.mumber_tv);
            week_tag_tv = (TextView) itemView.findViewById(R.id.week_tag_tv);
            week_tv = (TextView) itemView.findViewById(R.id.week_tv);
            delete_rule_img = (ImageView) itemView.findViewById(R.id.delete_rule_img);
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void click(FlightsModel model, int position, boolean itemView);
    }
}
