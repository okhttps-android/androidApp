package com.xzjmyk.pm.activity.ui.erp.adapter.oa;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConfig;
import com.core.model.OAConfig;
import com.core.model.WorkModel;
import com.core.utils.TimeUtils;
import com.uas.appworks.OA.erp.activity.ChangeMobileActivity;
import com.uas.appworks.OA.erp.activity.WorkActivity;
import com.xzjmyk.pm.activity.R;

import java.util.List;

/**
 * Created by Bitliker on 2016/12/5.
 */

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.ViewHoler> {
    private final String NONCLASS;
    private final String LATETIME;
    private final String OVERLATETIME;
    private final String EARLYOFF;

    private List<WorkModel> models;
    private Context ct;
    private boolean isToday;
    private String newTime;
    private long seletTime;
    private boolean isFreeWork = false;//是否是自由打卡

    public WorkAdapter(Context ct) {
        this.ct = ct;
        NONCLASS = ct.getString(R.string.sign_Absenteeism);
        LATETIME = ct.getString(R.string.sign_late);
        OVERLATETIME = ct.getString(R.string.over_latetime);
        EARLYOFF = ct.getString(R.string.sign_leave);
        isToday = true;
        newTime = DateFormatUtil.long2Str(System.currentTimeMillis(), "HH:mm");
    }

    public List<WorkModel> getModels() {
        return models;
    }

    public void setModels(boolean isToday, boolean isFreeWork, long seletTime, List<WorkModel> models) {
        this.models = models;
        this.isFreeWork = isFreeWork;
        this.isToday = isToday;
        this.seletTime = seletTime;
        newTime = DateFormatUtil.long2Str(System.currentTimeMillis(), "HH:mm");
        notifyDataSetChanged();
    }

    @Override
    public WorkAdapter.ViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHoler holer = new ViewHoler(LayoutInflater.from(ct).inflate(R.layout.item_works, parent, false));
        return holer;
    }

    @Override
    public void onBindViewHolder(WorkAdapter.ViewHoler holder, int position) {
        try {
            WorkModel model = models.get(position);
            if (isFreeWork) {
                holder.free_context.setVisibility(View.VISIBLE);
                holder.work_context.setVisibility(View.GONE);
                bindFreeView(holder, model, position);
            } else {
                holder.free_context.setVisibility(View.GONE);
                holder.work_context.setVisibility(View.VISIBLE);
                bindWorkView(holder, model, (position == models.size() - 1));
            }
        } catch (ClassCastException e) {
            if (e != null)
                log("onBindViewHolder ClassCastException=" + e.getMessage());
        } catch (Exception e) {
            if (e != null)
                log("onBindViewHolder Exception=" + e.getMessage());
        }
    }

    private void bindFreeView(WorkAdapter.ViewHoler holder, final WorkModel model, int position) {
        holder.free_work_tag.setText(position % 2 == 0 ? ct.getString(R.string.work_signin) : ct.getString(R.string.unwork_signin));
        holder.free_work_time.setText(model.getWorkTime());
    }

    private void bindWorkView(WorkAdapter.ViewHoler holder, final WorkModel model, boolean isFinal) {
        holder.work_time.setText("  " + getNull(model.getWorkTime()));
        holder.work_signin.setText("  " + getNull(model.getWorkSignin()));
        holder.off_time.setText("  " + getNull(model.getOffTime()));
        holder.off_signin.setText("  " + getNull(model.getOffSignin()));
        //处理迟到、早退、缺勤
        String workTag = getWorkTag(model);
        if (StringUtil.isEmpty(workTag)) {
            holder.work_tag.setVisibility(View.GONE);
            holder.work_supple.setVisibility(View.GONE);
        } else {//TODO 进入申述
            bindTag(true, workTag, model, holder);
        }
        String offTag = getOffTag(model, isFinal);
        if (StringUtil.isEmpty(offTag)) {
            holder.off_tag.setVisibility(View.GONE);
            holder.off_supple.setVisibility(View.GONE);
        } else {//TODO 进入申述
            bindTag(false, offTag, model, holder);
        }
    }


    @Override
    public int getItemCount() {
        return ListUtils.isEmpty(models) ? 0 : models.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder {
        TextView work_time,
                work_signin,
                off_time,
                off_signin,
                work_tag,//上班迟到标识
                free_work_tag,//上班打卡  下班打卡
                free_work_time,//上下班打卡时间
                off_tag;//上班迟到标识
        ImageView
                off_supple,
                work_supple;
        RelativeLayout work_context;//
        LinearLayout free_context;//

        public ViewHoler(View itemView) {
            super(itemView);
            work_time = (TextView) itemView.findViewById(R.id.work_time);
            work_signin = (TextView) itemView.findViewById(R.id.work_signin);
            off_time = (TextView) itemView.findViewById(R.id.off_time);
            off_signin = (TextView) itemView.findViewById(R.id.off_signin);
            work_tag = (TextView) itemView.findViewById(R.id.work_tag);
            off_tag = (TextView) itemView.findViewById(R.id.off_tag);
            free_work_tag = (TextView) itemView.findViewById(R.id.free_work_tag);
            free_work_time = (TextView) itemView.findViewById(R.id.free_work_time);
            off_supple = (ImageView) itemView.findViewById(R.id.off_supple);
            work_supple = (ImageView) itemView.findViewById(R.id.work_supple);
            work_context = (RelativeLayout) itemView.findViewById(R.id.work_context);
            free_context = (LinearLayout) itemView.findViewById(R.id.free_context);

        }
    }

    private void bindTag(final boolean isWork, String tag, final WorkModel model, ViewHoler holder) {
        TextView tag_tv = isWork ? holder.work_tag : holder.off_tag;
        ImageView supple_img = isWork ? holder.work_supple : holder.off_supple;
        tag_tv.setText(tag);
        tag_tv.setVisibility(View.VISIBLE);
        if (NONCLASS.equals(tag)) {
            int suppleReid = R.drawable.btn_shensu;
            supple_img.setVisibility(View.VISIBLE);
            String allegedly = isWork ? model.getWorkAllegedly() : model.getOffAllegedly();
            if (!StringUtil.isEmpty(allegedly)) {
                supple_img.setOnClickListener(null);
                TextView signin_tv = isWork ? holder.work_signin : holder.off_signin;
                signin_tv.setText("");
                suppleReid = R.drawable.allegedlying;
            } else {
                supple_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!StringUtil.isEmpty(isWork ? model.getWorkAllegedly() : model.getOffAllegedly()))
                            return;
                        Intent intent = new Intent(ct, ChangeMobileActivity.class);
                        intent.putExtra("type", 2);
                        if (seletTime > 0)
                            intent.putExtra("date", TimeUtils.s_long_2_str(seletTime));
                        intent.putExtra("tag", isWork ? model.getWorkTime() : model.getOffTime());
                        intent.putExtra("macAddress", getTimeForHHmm(isWork ? model.getWorkTime() : model.getOffTime()));
                        intent.putExtra("model", model);
                        intent.putExtra("isWork", isWork);
                        if (ct instanceof WorkActivity)
                            ((WorkActivity) ct).startActivityForResult(intent, WorkActivity.ALLEGEDLY);
                        else
                            ct.startActivity(intent);
                    }
                });
            }
            supple_img.setImageResource(suppleReid);
        } else {
            supple_img.setVisibility(View.GONE);
        }
    }

    /**
     * 获取上班签到的状态
     *
     * @param model
     * @return
     */

    private String getWorkTag(WorkModel model) {
        if (StringUtil.isEmpty(model.getWorkTime())) return "";
        if (isToday) {//是今天
            if (StringUtil.isEmpty(model.getWorkSignin())) {
                if (!model.isNextDay() && getWorkEndOrOffStart(model.getWorkTime(), true).compareTo(newTime) < 0) {
                    //矿工 当前时间在上班结束时间或是下班打卡存在
                    return NONCLASS;
                } else return "";
            } else {
                if (getForAddMin(model.getWorkTime(), OAConfig.latetime).compareTo(model.getWorkSignin()) < 0//迟到时间之后
                        && getForAddMin(model.getWorkTime(), OAConfig.overlatetime).compareTo(model.getWorkSignin()) >= 0) {
                    //迟到   迟到之后  严重迟到之前
                    return LATETIME;
                } else if (getForAddMin(model.getWorkTime(), OAConfig.overlatetime).compareTo(model.getWorkSignin()) < 0//迟到时间之后
                        && getWorkEndOrOffStart(model.getWorkTime(), true).compareTo(model.getWorkSignin()) >= 0) {
                    //严重迟到  严重迟到之后   矿工之前
                    return OVERLATETIME;
                } else return "";
            }
        } else {//是昨天及以前的
            if (StringUtil.isEmpty(model.getWorkSignin()))
                return NONCLASS;
            else if (getForAddMin(model.getWorkTime(), OAConfig.latetime).compareTo(model.getWorkSignin()) < 0
                    && getForAddMin(model.getWorkTime(), OAConfig.overlatetime).compareTo(model.getWorkSignin()) >= 0) {
                //迟到  迟到时间getForAddMin(model.getWorkTime(), AutoMemoryUtil.latetime) 到严重迟到时间之间
                return LATETIME;
            } else if (getForAddMin(model.getWorkTime(), OAConfig.overlatetime).compareTo(model.getWorkSignin()) < 0
                    && getWorkEndOrOffStart(model.getWorkTime(), true).compareTo(model.getWorkSignin()) >= 0) {
                //严重迟到 严重迟到时间到矿工之间
                return OVERLATETIME;
            } else {
                return "";
            }
        }
    }


    /**
     * 下班只显示早退和矿工
     * 早退：下班开始时间到早退时间之间
     * 矿工：非今天没有签到记录为矿工  今天有签到记录不会显示矿工
     *
     * @param model
     * @param isFinal
     * @return
     */
    private String getOffTag(WorkModel model, boolean isFinal) {
        if (StringUtil.isEmpty(model.getOffTime())) return "";
        if (!StringUtil.isEmpty(model.getOffSignin())) {
            if (getForAddMin(model.getOffTime(), -OAConfig.earlyoff).compareTo(model.getOffSignin()) > 0
                    && getWorkEndOrOffStart(model.getOffTime(), false).compareTo(model.getOffSignin()) <= 0) {
                //早退  下班开始时间到早退时间之间
                return EARLYOFF;
            }
            return "";
        } else if (!isToday) {
            return NONCLASS;
        } else if (!model.isNextDay() && !isFinal && !StringUtil.isEmpty(model.getOffend()) && model.getOffend().compareTo(newTime) < 0) {
            return NONCLASS;
        } else return "";
    }


    //当前时间
    private String getWorkEndOrOffStart(String hhmm, boolean iswork) {
        if (OAConfig.nonclass == 0)
            OAConfig.nonclass = 90;
        String time = getForAddMin(hhmm, iswork ? OAConfig.nonclass : -OAConfig.nonclass);
        if (iswork && time.compareTo(hhmm) < 0)//上跨天
            return "23:59";
        else
            return time;
    }

    private String getForAddMin(String hhmm, int time) {
        long f_time = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) +
                " " + hhmm + ":00");
        f_time += time * 60000;
        return DateFormatUtil.long2Str(f_time, "HH:mm");
    }

    private String getTimeForHHmm(String hhmm) {
        if (StringUtil.isEmpty(hhmm)) return "";
        return TimeUtils.s_long_2_str(seletTime) + " " + CalendarUtil.getWeek(seletTime) + " " + hhmm;
    }

    private String getNull(String str) {
        if (StringUtil.isEmpty(str)) return "";
        return str;
    }

    private void log(String message) {
        try {
            if (!AppConfig.DEBUG || StringUtil.isEmpty(message)) return;
            Log.i("gongpengming", message);
        } catch (Exception e) {

        }
    }


}
