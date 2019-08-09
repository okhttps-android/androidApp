package com.uas.appworks.CRM.erp.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.widget.view.model.SelectAimModel;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Bitliker on 2017/1/12.
 */

public class SelectAimAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SelectAimModel> models;

    public List<SelectAimModel> getModels() {
        return models;
    }

    public void setModels(List<SelectAimModel> models ) {
        this.models = models;
    }

    @Override
    public int getItemViewType(int position) {
        return models.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view = null;
        if (viewType == 1) {
            view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_select_aim_empty, parent, false);
            holder = new EmptyViewHolder(view);
        } else if (viewType == 2) {
            view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_select_aim_load, parent, false);
            holder = new LoadViewHolder(view);
        } else if (viewType == 3) {
            view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.item_select_aim_map, parent, false);
            holder = new MapViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            final SelectAimModel model = models.get(position);
            if (holder instanceof EmptyViewHolder) {
                bindEmptyView((EmptyViewHolder) holder, model);
            } else if (holder instanceof LoadViewHolder) {
                bindLoadView((LoadViewHolder) holder, model);
            } else if (holder instanceof MapViewHolder) {
                bindMapView((MapViewHolder) holder, model);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onitemClickListener != null)
                        onitemClickListener.click(model);
                }
            });
        } catch (Exception e) {
            if (e != null)
                Log.i("gongpengming", "onBindViewHolder Exception e=" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return ListUtils.isEmpty(models) ? 0 : models.size();
    }

    private void bindEmptyView(EmptyViewHolder holder, SelectAimModel model) throws Exception {

    }

    private void bindLoadView(LoadViewHolder holder, SelectAimModel model) throws Exception {
        holder.title_tv.setText(getNull(model.getName()));
        holder.sub_tv.setText(getNull(model.getAddress()));

        if (model.getLatLng()==null){
            holder.distance_tv.setText("");
        }else{
            holder.distance_tv.setText(getKm(LocationDistanceUtils.distanceMeStr(model.getLatLng())));
    }
        if (model.getTimes()==-1){
            holder.times_tv.setVisibility(View.GONE);
        }else{
            holder.times_tv.setVisibility(View.VISIBLE);
            holder.times_tv.setText(MyApplication.getInstance().getString(R.string.visit_num)+" "  + getNull(model.getTimes() + MyApplication.getInstance().getString(R.string.sign_Times)));
        }
        if (StringUtil.isEmpty(model.getTime())){
            holder.time_tv.setVisibility(View.GONE);
        }else{
            holder.time_tv.setVisibility(View.VISIBLE);
            holder.time_tv.setText(MyApplication.getInstance().getString(R.string.last_visit)+" " + getNull(model.getTime()));
        }

        holder.tag_tv.setVisibility(model.isFirst() ? View.VISIBLE : View.GONE);
        holder.tag_view.setVisibility(model.isFirst() ? View.VISIBLE : View.GONE);
    }

    private void bindMapView(MapViewHolder holder, SelectAimModel model) throws Exception {
        holder.title_tv.setText(getNull(model.getName()));
        holder.sub_tv.setText(getNull(model.getAddress()));
        holder.distance_tv.setText(getKm(LocationDistanceUtils.distanceMeStr(model.getLatLng())));
        holder.tag_tv.setVisibility((model.isFirst()) ? View.VISIBLE : View.GONE);
        holder.tag_view.setVisibility((model.isFirst()) ? View.VISIBLE : View.GONE);
    }

    private String getKm(String dis) {
        if (StringUtil.isEmpty(dis)) return String.valueOf(0) + "km";
        try {
            DecimalFormat fnum = new DecimalFormat("##0.00");
            String dd = fnum.format(Float.valueOf(dis) / 1000);
            return dd + "km";
        } catch (ClassCastException e) {
            return String.valueOf(0) + "km";
        } catch (Exception e) {
            return String.valueOf(0) + "km";
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    class MapViewHolder extends RecyclerView.ViewHolder {
        TextView distance_tv,
                title_tv,
                sub_tv,
                tag_tv;
        View tag_view;

        public MapViewHolder(View itemView) {
            super(itemView);
            distance_tv = (TextView) itemView.findViewById(R.id.distance_tv);
            title_tv = (TextView) itemView.findViewById(R.id.title_tv);
            sub_tv = (TextView) itemView.findViewById(R.id.sub_tv);
            tag_tv = (TextView) itemView.findViewById(R.id.tag_tv);
            tag_view = itemView.findViewById(R.id.tag_view);
        }
    }

    class LoadViewHolder extends RecyclerView.ViewHolder {
        TextView distance_tv,
                title_tv,
                sub_tv,
                times_tv,
                time_tv,
                tag_tv;
        View tag_view;

        public LoadViewHolder(View itemView) {
            super(itemView);
            distance_tv = (TextView) itemView.findViewById(R.id.distance_tv);
            title_tv = (TextView) itemView.findViewById(R.id.title_tv);
            sub_tv = (TextView) itemView.findViewById(R.id.sub_tv);
            times_tv = (TextView) itemView.findViewById(R.id.times_tv);
            time_tv = (TextView) itemView.findViewById(R.id.time_tv);
            tag_tv = (TextView) itemView.findViewById(R.id.tag_tv);
            tag_view = itemView.findViewById(R.id.tag_view);
        }
    }

    public void setOnitemClickListener(OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    private OnitemClickListener onitemClickListener;

    public interface OnitemClickListener {
        void click(SelectAimModel model);
    }

    private String getNull(String messgae) {
        return StringUtil.isEmpty(messgae) ? "" : messgae;
    }
}
