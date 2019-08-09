package com.uas.appworks.OA.erp.adapter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.system.DisplayUtil;
import com.uas.appworks.OA.erp.activity.ChangeMobileActivity;
import com.uas.appworks.OA.erp.model.WorkLogs;
import com.uas.appworks.R;

import java.util.List;

public class WorkLogsAdapter extends RecyclerView.Adapter<WorkLogsAdapter.ViewHolder> {
    private final String LATE = "(迟到)";
    private final String NEGLECT_WORK = "旷工";
    private final String APPRECORD = "申诉中";
    private final String TO_APPRECORD = "申诉";
    private final String EARLY_RETREAT = "(早退)";


    private AppCompatActivity ct;
    private List<WorkLogs> mWorkLogs;
    private long thisTimes;

    public WorkLogsAdapter(AppCompatActivity ct, List<WorkLogs> mWorkLogs) {
        this.ct = ct;
        this.mWorkLogs = mWorkLogs;
        thisTimes = DateFormatUtil.str2Long(DateFormatUtil.long2Str(DateFormatUtil.YMD), DateFormatUtil.YMD);
        LogUtil.i("gong", "thisTimes=" + thisTimes);
    }

    public void setWorkLogs(List<WorkLogs> mWorkLogs) {
        this.mWorkLogs = mWorkLogs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_work_logs, viewGroup, false));
    }


    @Override
    public int getItemCount() {
        return ListUtils.getSize(this.mWorkLogs);
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTv;
        private TextView w1SignTv;
        private TextView w1TagTv;
        private TextView w1AllegedlyTv;
        private TextView w2SignTv;
        private TextView w2TagTv;
        private TextView w2AllegedlyTv;
        private TextView w3SignTv;
        private TextView w3TagTv;
        private TextView w3AllegedlyTv;
        private TextView off1SignTv;
        private TextView off1TagTv;
        private TextView off1AllegedlyTv;
        private TextView off2SignTv;
        private TextView off2TagTv;
        private TextView off2AllegedlyTv;
        private TextView off3SignTv;
        private TextView off3TagTv;
        private TextView off3AllegedlyTv;

        private LinearLayout off3LL, off2LL, off1LL, w3LL, w2LL, w1LL;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTv = (TextView) itemView.findViewById(R.id.dateTv);
            w1SignTv = (TextView) itemView.findViewById(R.id.w1SignTv);
            w1TagTv = (TextView) itemView.findViewById(R.id.w1TagTv);
            w1AllegedlyTv = (TextView) itemView.findViewById(R.id.w1AllegedlyTv);
            w2SignTv = (TextView) itemView.findViewById(R.id.w2SignTv);
            w2TagTv = (TextView) itemView.findViewById(R.id.w2TagTv);
            w2AllegedlyTv = (TextView) itemView.findViewById(R.id.w2AllegedlyTv);
            w3SignTv = (TextView) itemView.findViewById(R.id.w3SignTv);
            w3TagTv = (TextView) itemView.findViewById(R.id.w3TagTv);
            w3AllegedlyTv = (TextView) itemView.findViewById(R.id.w3AllegedlyTv);
            off1SignTv = (TextView) itemView.findViewById(R.id.off1SignTv);
            off1TagTv = (TextView) itemView.findViewById(R.id.off1TagTv);
            off1AllegedlyTv = (TextView) itemView.findViewById(R.id.off1AllegedlyTv);
            off2SignTv = (TextView) itemView.findViewById(R.id.off2SignTv);
            off2TagTv = (TextView) itemView.findViewById(R.id.off2TagTv);
            off2AllegedlyTv = (TextView) itemView.findViewById(R.id.off2AllegedlyTv);
            off3SignTv = (TextView) itemView.findViewById(R.id.off3SignTv);
            off3TagTv = (TextView) itemView.findViewById(R.id.off3TagTv);
            off3AllegedlyTv = (TextView) itemView.findViewById(R.id.off3AllegedlyTv);
            w1LL = itemView.findViewById(R.id.w1LL);
            w2LL = itemView.findViewById(R.id.w2LL);
            w3LL = itemView.findViewById(R.id.w3LL);
            off1LL = itemView.findViewById(R.id.off1LL);
            off2LL = itemView.findViewById(R.id.off2LL);
            off3LL = itemView.findViewById(R.id.off3LL);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        WorkLogs model = mWorkLogs.get(position);
        viewHolder.dateTv.setText(model.getDate() + " " + model.getWeek());
        List<WorkLogs.Shift> shifts = model.getShifts();
        WorkLogs.Shift shift = null;
        setVisibility(View.GONE, viewHolder.off3LL, viewHolder.off2LL, viewHolder.off1LL, viewHolder.w1LL, viewHolder.w2LL, viewHolder.w3LL);
        switch (ListUtils.getSize(shifts)) {
            case 3:
                setVisibility(View.VISIBLE, viewHolder.off3LL, viewHolder.w3LL);
                shift = shifts.get(2);
                setTag(getTag(thisTimes == model.getWorkTimes(), model.isWorkDate(), true, shift), viewHolder.w3TagTv, viewHolder.w3AllegedlyTv);
                setTag(getTag(thisTimes == model.getWorkTimes(), model.isWorkDate(), false, shift), viewHolder.off3TagTv, viewHolder.off3AllegedlyTv);
                viewHolder.w3SignTv.setText(shift.wSign == null ? "" : shift.wSign);
                viewHolder.off3SignTv.setText(shift.oSign == null ? "" : shift.oSign);
                bindOnClick(model,true, shift.work, viewHolder.w3AllegedlyTv);
                bindOnClick(model, false,shift.off, viewHolder.off3AllegedlyTv);
            case 2:
                setVisibility(View.VISIBLE, viewHolder.off2LL, viewHolder.w2LL);
                shift = shifts.get(1);
                setTag(getTag(thisTimes == model.getWorkTimes(), model.isWorkDate(), true, shift), viewHolder.w2TagTv, viewHolder.w2AllegedlyTv);
                setTag(getTag(thisTimes == model.getWorkTimes(), model.isWorkDate(), false, shift), viewHolder.off2TagTv, viewHolder.off2AllegedlyTv);
                viewHolder.w2SignTv.setText(shift.wSign == null ? "" : shift.wSign);
                viewHolder.off2SignTv.setText(shift.oSign == null ? "" : shift.oSign);
                bindOnClick(model,true, shift.work, viewHolder.w2AllegedlyTv);
                bindOnClick(model, false,shift.off, viewHolder.off2AllegedlyTv);
            case 1:
                setVisibility(View.VISIBLE, viewHolder.off1LL, viewHolder.w1LL);
                shift = shifts.get(0);
                setTag(getTag(thisTimes == model.getWorkTimes(), model.isWorkDate(), true, shift), viewHolder.w1TagTv, viewHolder.w1AllegedlyTv);
                setTag(getTag(thisTimes == model.getWorkTimes(), model.isWorkDate(), false, shift), viewHolder.off1TagTv, viewHolder.off1AllegedlyTv);
                viewHolder.w1SignTv.setText(shift.wSign == null ? "" : shift.wSign);
                viewHolder.off1SignTv.setText(shift.oSign == null ? "" : shift.oSign);
                bindOnClick(model,true, shift.work, viewHolder.w1AllegedlyTv);
                bindOnClick(model,false, shift.off, viewHolder.off1AllegedlyTv);
                break;
        }

    }

    private void bindOnClick(WorkLogs model, boolean w, String time, TextView allegedlyTv) {
        if (allegedlyTv.getVisibility() == View.VISIBLE && TO_APPRECORD.equals(allegedlyTv.getText().toString())) {
            String hhMM = null;
            if (w) {
                hhMM = DateFormatUtil.long2Str(DateFormatUtil.str2Long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + time, DateFormatUtil.YMD_HM) - model.getLate() * 60000, DateFormatUtil.HM);
            } else {
                hhMM = DateFormatUtil.long2Str(DateFormatUtil.str2Long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + time, DateFormatUtil.YMD_HM) + model.getEarly() * 60000, DateFormatUtil.HM);
            }
            allegedlyTv.setTag(R.id.tag_key, model);
            allegedlyTv.setTag(R.id.tag_key2, hhMM);
            allegedlyTv.setOnClickListener(mOnClickListener);
        } else {
            allegedlyTv.setOnClickListener(null);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view != null && view.getTag(R.id.tag_key) != null && view.getTag(R.id.tag_key) instanceof WorkLogs &&
                    view.getTag(R.id.tag_key2) != null && view.getTag(R.id.tag_key2) instanceof String) {
                WorkLogs model = (WorkLogs) view.getTag(R.id.tag_key);
                String hhmm = (String) view.getTag(R.id.tag_key2);
                String macAddress = model.getDate() + " " + model.getWeek() + " " + hhmm + ":00";
                Intent intent = new Intent(ct, ChangeMobileActivity.class);
                intent.putExtra("type", 2);
                intent.putExtra("date", DateFormatUtil.long2Str(model.getWorkTimes(), DateFormatUtil.YMD));//日期
                intent.putExtra("tag", hhmm);//班次时间
                intent.putExtra("macAddress", macAddress);
                ct.startActivityForResult(intent,0x11);
            }
        }
    };


    private void setVisibility(int visibility, View... views) {
        if (views != null) {
            for (View v : views) {
                v.setVisibility(visibility);
            }
        }
    }

    private void setTag(String tag, TextView tagTv, TextView allegedlyTv) {
        if (TextUtils.isEmpty(tag)) {
            tagTv.setVisibility(View.GONE);
            allegedlyTv.setVisibility(View.GONE);
        } else if (NEGLECT_WORK.equals(tag)) {
            tagTv.setVisibility(View.VISIBLE);
            allegedlyTv.setVisibility(View.VISIBLE);
            tagTv.setText(tag);
            allegedlyTv.setText(TO_APPRECORD);
            allegedlyTv.setBackgroundResource(R.drawable.bg_bule_btn);
            allegedlyTv.setPadding(DisplayUtil.dip2px(ct, 6), DisplayUtil.dip2px(ct, 2), DisplayUtil.dip2px(ct, 6), DisplayUtil.dip2px(ct, 2));
        } else if (APPRECORD.equals(tag)) {
            tagTv.setVisibility(View.VISIBLE);
            allegedlyTv.setVisibility(View.VISIBLE);
            tagTv.setText(NEGLECT_WORK);
            allegedlyTv.setText(APPRECORD);
            allegedlyTv.setBackgroundResource(R.drawable.bg_text_pass);
            allegedlyTv.setPadding(DisplayUtil.dip2px(ct, 6), DisplayUtil.dip2px(ct, 2), DisplayUtil.dip2px(ct, 6), DisplayUtil.dip2px(ct, 2));
        } else if (LATE.equals(tag)) {
            tagTv.setVisibility(View.VISIBLE);
            allegedlyTv.setVisibility(View.GONE);
            tagTv.setText(tag);
        } else if (EARLY_RETREAT.equals(tag)) {
            tagTv.setVisibility(View.VISIBLE);
            allegedlyTv.setVisibility(View.GONE);
            tagTv.setText(tag);
        } else {
            tagTv.setVisibility(View.GONE);
            allegedlyTv.setVisibility(View.GONE);
            tagTv.setText(tag);
        }
    }


    /**
     * 1.当工作日，没有打卡记录时候，显示缺勤
     * 2.当工作日，打卡记录不是符合打开时间，显示迟到，早退
     *
     * @param isWork 是否是工作日
     * @param w      是否是上班班次
     * @param shift
     * @return
     */
    private String getTag(boolean isToday, boolean isWork, boolean w, WorkLogs.Shift shift) {
        if (isToday) return "";
        if (isWork) {
            if (w) {
                if (TextUtils.isEmpty(shift.wSign)) {
                    if (shift.wApprecord) {
                        return APPRECORD;
                    } else {
                        return NEGLECT_WORK;
                    }
                } else if (shift.work.compareTo(shift.wSign) < 0) {
                    return LATE;
                }
            } else {
                if (TextUtils.isEmpty(shift.oSign)) {
                    if (shift.offApprecord) {
                        return APPRECORD;
                    } else {
                        return NEGLECT_WORK;
                    }
                } else if (shift.off.compareTo(shift.oSign) > 0) {
                    return EARLY_RETREAT;
                }
            }
        }
        return "";
    }
}
