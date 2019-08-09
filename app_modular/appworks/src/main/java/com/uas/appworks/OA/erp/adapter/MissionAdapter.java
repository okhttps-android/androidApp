package com.uas.appworks.OA.erp.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.model.MissionModel;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.R;

import java.text.DecimalFormat;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Bitliker on 2016/12/19.
 */

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.BaseViewHolder> {
    private BaseActivity ct;
    private List<MissionModel> models;
    private OnitemClickListener onitemClickListener;

    public MissionAdapter(BaseActivity ct, OnitemClickListener onitemClickListener) {
        this.ct = ct;
        this.onitemClickListener = onitemClickListener;
    }

    public List<MissionModel> getModels() {
        return models;
    }

    public void setModels(List<MissionModel> models) {
        this.models = models;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ct).inflate(R.layout.item_mission, null);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof BaseViewHolder) {
            try {
                initView(holder, position);
                initEvent(holder, position);
            } catch (Exception e) {
                if (e != null)
                    Log.i("gongpengming", "onBindViewHolder Exception " + e.getMessage());
                else Log.i("gongpengming", "Exception==null");
            }
        }
    }

    private void initView(BaseViewHolder holder, int position) {
        MissionModel entity = models.get(position);
        if (entity == null) return;
        holder.item_title_tv.setText("");
        holder.item_delete_rl.setVisibility(position == 0?View.GONE:View.VISIBLE);
        holder.item_delete_tv.setVisibility((inputItem(entity) && position != 0) ? View.VISIBLE : View.GONE);
       double distance=entity.getDistance();
        if (distance != 0) {
            holder.item_length_tv.setText(getKm(distance) + "km");
            holder.navigation_tv.setVisibility(View.VISIBLE);
        } else {
            holder.navigation_tv.setVisibility(View.INVISIBLE);
            holder.item_length_tv.setText("0米");
        }
        if (entity.getLatLng() != null) {
            holder.item_long_tv.setText(getKm(LocationDistanceUtils.distanceMeStr(entity.getLatLng())) + "km");
        } else {
            holder.item_long_tv.setText("");
        }
        holder.item_reckontime_tv.setText(getStringNotNull(entity.getVisitTime()));
        holder.item_realtime_tv.setText(getStringNotNull(entity.getRealTime()));
        if (!getStringNotNull(entity.getRealTime()).equals(getStringNotNull(entity.getRealLeave())))
            holder.item_realleave_tv.setText(getStringNotNull(entity.getRealLeave()));
        else holder.item_realleave_tv.setText("");
        holder.item_company_tv.setText(getStringNotNull(entity.getCompanyName()));
        holder.item_companyaddr_tv.setText(getStringNotNull(entity.getCompanyAddr()));
        holder.item_time_tv.setText(getStringNotNull(entity.getRecorddate()));
        holder.item_location_tv.setText(getStringNotNull(entity.getLocation()));
        holder.item_remark_tv.setText(getStringNotNull(entity.getRemark()));
        boolean focusable = inputItem(entity);
        setViewFocusable(holder.item_remark_tv, focusable);
        setViewFocusable(holder.item_half_rb, focusable);
        setViewFocusable(holder.item_all_rb, focusable);
        setViewFocusable(holder.item_reckontime_tv, focusable);
        setViewFocusable(holder.item_company_tv, focusable);
        setViewFocusable(holder.item_companyaddr_tv, focusable);
        if (entity.getType() == 1) {//半天
            holder.item_half_rb.setChecked(true);
            holder.item_all_rb.setChecked(false);
        } else {//全天
            holder.item_half_rb.setChecked(false);
            holder.item_all_rb.setChecked(true);
        }
        holder.item_all_rb.setEnabled(focusable);
        holder.item_half_rb.setEnabled(focusable);
    }


    private void setViewFocusable(View view, boolean focusable) {
        view.setFocusable(focusable);
        view.setClickable(focusable);
    }

    private void initEvent(final BaseViewHolder holder, final int position) {
        if (holder.item_company_tv.isFocusable()) {
            holder.item_company_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onitemClickListener != null) {
                        onitemClickListener.click(position, models.get(position), holder.item_company_tv);
                    }
                }
            });
        }

        if (holder.item_companyaddr_tv.isFocusable()) {
            holder.item_companyaddr_tv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (onitemClickListener != null) {
                        onitemClickListener.click(position, models.get(position), holder.item_companyaddr_tv);
                    }
                }
            });
        }
        if (holder.item_delete_tv.getVisibility() == View.VISIBLE) {
            holder.item_delete_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onitemClickListener != null) {
                        onitemClickListener.click(position, models.get(position), holder.item_delete_tv);
                    }
                }
            });
        }

        if (holder.item_reckontime_tv.isFocusable()) {
            holder.item_reckontime_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onitemClickListener != null)
                        onitemClickListener.click(position, models.get(position), holder.item_reckontime_tv);
                }
            });
        }
        if (holder.item_remark_tv.isFocusable()) {
            holder.item_remark_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onitemClickListener != null)
                        onitemClickListener.click(position, models.get(position), holder.item_remark_tv);
                }
            });
        }
        holder.navigation_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onitemClickListener != null)
                    onitemClickListener.click(position, models.get(position), holder.navigation_tv);
            }
        });
        if (holder.item_half_rb.isFocusable()) {
            holder.item_half_rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!inputItem(models.get(position))) return;
                    if (b)
                        models.get(position).setType(1);
                    else models.get(position).setType(2);
                }
            });
        }
    }

    private boolean inputItem(MissionModel missionModel) {
        return missionModel.getStatus() == 0;
    }


    @Override
    public int getItemCount() {
        return ListUtils.isEmpty(models) ? 0 : models.size();
    }

    class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView item_title_tv,//标题
                item_delete_tv,//删除按钮
                item_length_tv,//里程
                item_reckontime_tv,//预计到达时间
                item_realtime_tv,//实际到达时间
                item_realleave_tv,//实际离开时间
                item_companyaddr_tv,//公司地址
                item_location_tv,//创建地点
                item_time_tv,//创建时间
                item_long_tv,
                item_company_tv, //公司名称
                navigation_tv, //公司名称
                item_remark_tv;//目的
        RadioButton item_half_rb, item_all_rb;
        RelativeLayout item_delete_rl;

        public BaseViewHolder(View itemView) {
            super(itemView);
            item_title_tv = (TextView) itemView.findViewById(R.id.item_title_tv);//标题
            item_delete_tv = (TextView) itemView.findViewById(R.id.item_delete_tv);
            item_length_tv = (TextView) itemView.findViewById(R.id.item_length_tv);
            item_reckontime_tv = (TextView) itemView.findViewById(R.id.item_reckontime_tv);
            item_realtime_tv = (TextView) itemView.findViewById(R.id.item_realtime_tv);
            item_realleave_tv = (TextView) itemView.findViewById(R.id.item_realleave_tv);
            item_company_tv = (TextView) itemView.findViewById(R.id.item_company_tv);
            item_companyaddr_tv = (TextView) itemView.findViewById(R.id.item_companyaddr_tv);
            item_location_tv = (TextView) itemView.findViewById(R.id.item_location_tv);
            item_time_tv = (TextView) itemView.findViewById(R.id.item_time_tv);
            item_long_tv = (TextView) itemView.findViewById(R.id.item_long_tv);
            item_remark_tv = (TextView) itemView.findViewById(R.id.item_remark_tv);
            item_half_rb = (RadioButton) itemView.findViewById(R.id.item_half_rb);
            item_all_rb = (RadioButton) itemView.findViewById(R.id.item_all_rb);
            navigation_tv = (TextView) itemView.findViewById(R.id.navigation_tv);
            item_delete_rl = (RelativeLayout)itemView.findViewById(R.id.item_delete_rl);
        }
    }

    private String getStringNotNull(String str) {
        if (StringUtil.isEmpty(str)) return "";
        return str;
    }

    private String getKm(double dis) {
        try {
            return getKm(String.valueOf(dis));
        } catch (Exception e) {
            return String.valueOf(0);
        }
    }

    private String getKm(String dis) {
        if (StringUtil.isEmpty(dis)) return String.valueOf(0);
        try {
            DecimalFormat fnum = new DecimalFormat("##0.00");
            String dd = fnum.format(Float.valueOf(dis) / 1000);
            return dd;
        } catch (ClassCastException e) {
            return String.valueOf(0);
        } catch (Exception e) {
            return String.valueOf(0);
        }
    }

    public interface OnitemClickListener {
        void click(int position, MissionModel model, View view);
    }
}
