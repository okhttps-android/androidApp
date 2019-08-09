package com.uas.appworks.CRM.erp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appworks.CRM.erp.model.DeviceManage;
import com.uas.appworks.R;

import java.util.List;

/**
 * Created by Bitlike on 2017/11/22.
 */

public class DeviceManageAdapter extends RecyclerView.Adapter<DeviceManageAdapter.ViewHolder> implements View.OnClickListener {

    private Context ct;
    private List<DeviceManage> models;


    public DeviceManageAdapter(Context ct, List<DeviceManage> models) {
        this.ct = ct;
        this.models = models;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_devicemanage_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DeviceManage model = models.get(position);
        holder.contentTv.setText(model.getName());
        if (model.getReId() > 0) {
            holder.contentImag.setImageResource(model.getReId());
        }
        holder.itemView.setTag(R.id.tag_key,model);
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(models);
    }

    @Override
    public void onClick(View view) {
        if (view!=null){
            Object tag = view.getTag(R.id.tag_key);
            if (tag!=null&&tag instanceof DeviceManage&&itemClickListener!=null){
                itemClickListener.itemClick((DeviceManage) tag);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView contentTv;
        ImageView contentImag;

        public ViewHolder(View itemView) {
            super(itemView);
            contentImag = (ImageView) itemView.findViewById(R.id.contentImag);
            contentTv = (TextView) itemView.findViewById(R.id.contentTv);
        }
    }

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void itemClick(DeviceManage manage);
    }
}
