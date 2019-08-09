package com.uas.appworks.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.uas.appworks.R;
import com.uas.appworks.model.Schedule;

import java.util.List;

public class SchedulerAdapter extends RecyclerView.Adapter<SchedulerAdapter.SchedulerViewHolder> {

    private Context ct;
    private List<Schedule> mSchedules;
    private LayoutInflater mLayoutInflater;

    public SchedulerAdapter(Context ct, List<Schedule> mSchedules) {
        this.ct = ct;
        this.mSchedules = mSchedules;
    }

    public void setSchedules(List<Schedule> mSchedules) {
        this.mSchedules = mSchedules;
        notifyDataSetChanged();
    }

    public List<Schedule> getSchedules() {
        return mSchedules;
    }

    public LayoutInflater getLayoutInflater() {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(ct);
        }
        return mLayoutInflater;
    }

    @Override
    public SchedulerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SchedulerViewHolder(getLayoutInflater().inflate(R.layout.item_schedule_search, parent, false));
    }


    @Override
    public int getItemCount() {
        return ListUtils.getSize(mSchedules);
    }

    class SchedulerViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTv, timeTv;

        public SchedulerViewHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }


    @Override
    public void onBindViewHolder(SchedulerViewHolder holder, int position) {

    }
}
