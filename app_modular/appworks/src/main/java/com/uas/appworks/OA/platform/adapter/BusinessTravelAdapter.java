package com.uas.appworks.OA.platform.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.utils.CommonUtil;
import com.modular.apputils.widget.TravelDirectionView;
import com.uas.appworks.OA.platform.model.BusinessTravel;
import com.uas.appworks.R;
import com.uas.appworks.utils.TravelUtils;

import java.util.List;

/**
 * Created by Bitlike on 2017/12/12.
 */

public class BusinessTravelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private final String DONE_SUBMIT = "预约/订票";
    private final String DONE_CANCEL = "查看详情";

    private Context ct;
    private List<BusinessTravel> models;
    private String currentName;
    private String appkey = null;
    private String cusCode = null;
    private String appSceret = null;

    public BusinessTravelAdapter(Context ct, String cusCode, String appkey,String appSceret, List<BusinessTravel> models) {
        this.ct = ct;
        this.appkey = appkey;
        this.cusCode = cusCode;
        this.appSceret = appSceret;
        this.models = models;
        this.currentName = CommonUtil.getName();
    }


    public void setModels(List<BusinessTravel> models) {
        this.models = models;
        notifyDataSetChanged();
    }

    public void addModels(List<BusinessTravel> models) {
        int oldPosition = ListUtils.getSize(this.models);
        this.models.addAll(models);
        int position = ListUtils.getSize(this.models);
        notifyItemRangeInserted(oldPosition, position);
    }

    @Override
    public int getItemViewType(int position) {
        return models.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return ListUtils.getSize(models);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case BusinessTravel.AIR:
            case BusinessTravel.TRAIN:
            case BusinessTravel.UNKOWN:
                return new AirViewHolder(parent);
            case BusinessTravel.HOTEL:
                return new HotelViewHolder(parent);
            case BusinessTravel.LEADER:
                return new LeaderViewHolder(parent);
            default:
                return new TitleViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null) {
            BusinessTravel model = models.get(position);
            if (holder instanceof TitleViewHolder) {
                bindTitleView((TitleViewHolder) holder, model, position);
            } else if (holder instanceof BaseViewHlder) {
                bindBaseView((BaseViewHlder) holder, model, position);
                if (holder instanceof AirViewHolder) {
                    bindAirView((AirViewHolder) holder, model, position);
                } else if (holder instanceof HotelViewHolder) {
                    bindHotelView((HotelViewHolder) holder, model);
                }
            } else if (holder instanceof LeaderViewHolder) {
                bindLeaderHolder((LeaderViewHolder) holder);
            }

        }
    }

    private void bindLeaderHolder(LeaderViewHolder holder) {
        holder.clickAir.setOnClickListener(this);
        holder.clickHotel.setOnClickListener(this);
        holder.clickTrain.setOnClickListener(this);

    }

    private void bindTitleView(TitleViewHolder holder, BusinessTravel model, int position) {
        holder.codeTv.setText(model.getCode());
        holder.addMoreTv.setTag(R.id.tag_key, model);
        holder.addMoreTv.setTag(R.id.tag_key2, position);
        holder.addMoreTv.setOnClickListener(this);
        if (position == 0) {
            holder.titleLine.setVisibility(View.GONE);
        } else {
            holder.titleLine.setVisibility(View.VISIBLE);
        }
    }


    private void bindBaseView(BaseViewHlder holder, BusinessTravel model, int position) {
        holder.codeTv.setText(model.getTitleAndCode());
        String status = model.getStatus();
        holder.statusTv.setText(status);
        if (status.equals(BusinessTravel.EMPTY_STATUS)) {
            holder.orderInfoRl.setVisibility(View.GONE);
            holder.userInfoRl.setVisibility(View.GONE);
            holder.line.setVisibility(View.GONE);
            model.setExpand(true);
            holder.subRl.setTag(R.id.tag_key, model);
            holder.subRl.setTag(R.id.tag_key2, position);
            holder.subRl.setOnClickListener(this);
        } else {
            holder.orderInfoRl.setVisibility(View.VISIBLE);
            holder.userInfoRl.setVisibility(View.VISIBLE);
            holder.line.setVisibility(View.VISIBLE);
            holder.subRl.setOnClickListener(null);
        }
        holder.nameTv.setText(currentName == null ? "**" : currentName);
        holder.idCardTv.setText(model.getCttpid());
        holder.seatTv.setText(model.getSeat());
        String realFee = model.getRealFee();
        realFee = TextUtils.isEmpty(realFee) ? "" : (realFee + "元");
        holder.realFeeTv.setText(realFee);
        holder.payTypeTv.setText(model.getPayType());
        holder.levelTv.setText(model.getLevel());
        holder.expecteFeeTv.setText(model.getExpecteFee());
        holder.idTypeTv.setText("二代身份证");
        String date = DateFormatUtil.long2Str(model.getStartTime(), DateFormatUtil.YMD);
        holder.dateTv.setText(date);
        if (StringUtil.isEmpty(model.getStatus()) || StringUtil.isEmpty(model.getCode())) {
            holder.doneTv.setText(DONE_SUBMIT);
            holder.doneTv.setBackgroundResource(R.drawable.text_frame_radian_hint_bg);
            holder.doneTv.setTextColor(ct.getResources().getColor(R.color.text_hine));
        } else {
            holder.doneTv.setText(DONE_CANCEL);
            holder.doneTv.setBackgroundResource(R.drawable.text_frame_radian_red_bg);
            holder.doneTv.setTextColor(ct.getResources().getColor(R.color.indianred));
        }
        holder.doneTv.setTag(R.id.tag_key, model);
        holder.doneTv.setTag(R.id.tag_key2, position);
        holder.doneTv.setOnClickListener(this);
        holder.expandTv.setTag(R.id.tag_key, model);
        holder.expandTv.setTag(R.id.tag_key2, position);
        holder.expandTv.setOnClickListener(this);
        holder.subRl.setVisibility(model.isExpand() ? View.VISIBLE : View.GONE);
    }

    private void bindAirView(AirViewHolder holder, BusinessTravel model, int position) {
        String fromCity = model.getStarting();
        String toCity = model.getDestination();
        //出发点
        holder.fromCityTv.setText(fromCity);
        holder.startDateTv.setText(DateFormatUtil.long2Str(model.getStartTime(), "yyyy-MM-dd"));
        holder.startTimeTv.setText(DateFormatUtil.long2Str(model.getStartTime(), "HH:mm"));
        holder.orderTypeTv.setText(model.getOrderType());
        //目的地
        holder.toCityTv.setText(toCity);
        holder.toDateTv.setText(DateFormatUtil.long2Str(model.getEndTime(), "yyyy-MM-dd"));
        holder.toTimeTv.setText(DateFormatUtil.long2Str(model.getEndTime(), "HH:mm"));
        //行程

        if (TextUtils.isEmpty(fromCity) || TextUtils.isEmpty(toCity)) {
            holder.tripTv.setText(fromCity + "" + toCity);
        } else {
            holder.tripTv.setText(fromCity + "-" + toCity);
        }
        holder.mTravelDirectionView.setData(model.getFlightCode(), model.getAllTime());
        //改签
        if (StringUtil.getText(holder.doneTv).equals(DONE_CANCEL) && 1 == 2) {
            holder.changeTv.setVisibility(View.VISIBLE);
            holder.changeTv.setBackgroundResource(R.drawable.text_frame_radian_hint_bg);
            holder.changeTv.setTextColor(ct.getResources().getColor(R.color.text_hine));
            holder.changeTv.setTag(R.id.tag_key, model);
            holder.changeTv.setTag(R.id.tag_key2, position);
            holder.changeTv.setOnClickListener(this);
        } else {
            holder.changeTv.setVisibility(View.GONE);
        }
    }

    private void bindHotelView(HotelViewHolder holder, BusinessTravel model) {
        String whenLongTv = "入住: " + DateFormatUtil.long2Str(model.getStartTime(), "MM-dd") + "      离店: " + DateFormatUtil.long2Str(model.getStartTime(), "MM-dd") + "     共" + model.getAllTime() + (model.getAllTime().contains("分钟") ? "" : "晚");
        holder.whenLongTv.setText(whenLongTv);
        holder.businessNameTv.setText(model.getBusinessName());
        holder.numberTv.setText(model.getNumber() + "人");
        if (!StringUtil.isEmpty(model.getCode()) && !StringUtil.isEmpty(model.getStatus())) {
            holder.roomsTv.setText("1间");
        }
        String address = TextUtils.isEmpty(model.getHotelAddress()) ? model.getHotelCity() : model.getHotelAddress();
        holder.addressTv.setText(address);
        holder.numberSubTv.setText(model.getNumber() + "人");
    }


    private LayoutInflater inflater;

    public LayoutInflater getInflater() {
        return inflater == null ? inflater = LayoutInflater.from(ct) : inflater;
    }

    private class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView codeTv;
        ImageView addMoreTv;
        View titleLine;

        public TitleViewHolder(ViewGroup viewGroup) {
            this(getInflater().inflate(R.layout.item_bus_travel_title, viewGroup, false));
        }

        public TitleViewHolder(View itemView) {
            super(itemView);
            codeTv = itemView.findViewById(R.id.codeTv);
            titleLine = itemView.findViewById(R.id.titleLine);
            addMoreTv = itemView.findViewById(R.id.addMoreTv);
        }
    }


    private class LeaderViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout clickTrain;
        private LinearLayout clickAir;
        private LinearLayout clickHotel;


        public LeaderViewHolder(ViewGroup viewGroup) {
            this(getInflater().inflate(R.layout.item_bus_travel_leader, viewGroup, false));
        }

        public LeaderViewHolder(View itemView) {
            super(itemView);
            clickTrain = itemView.findViewById(R.id.clickTrain);
            clickAir = itemView.findViewById(R.id.clickAir);
            clickHotel = itemView.findViewById(R.id.clickHotel);
        }
    }

    private class HotelViewHolder extends BaseViewHlder {
        TextView
                businessNameTv,
                whenLongTv,
                numberSubTv,//人数
                numberTv,
                addressTv,  //地址
                roomsTv;//房间人数

        public HotelViewHolder(ViewGroup viewGroup) {
            this(getInflater().inflate(R.layout.item_bus_travel_hotel, viewGroup, false));
        }

        public HotelViewHolder(View itemView) {
            super(itemView);
            businessNameTv = (TextView) itemView.findViewById(R.id.businessNameTv);
            whenLongTv = (TextView) itemView.findViewById(R.id.whenLongTv);
            numberTv = (TextView) itemView.findViewById(R.id.numberTv);
            roomsTv = (TextView) itemView.findViewById(R.id.roomsTv);
            addressTv = (TextView) itemView.findViewById(R.id.addressTv);
            numberSubTv = (TextView) itemView.findViewById(R.id.numberSubTv);
        }
    }

    public class BaseViewHlder extends RecyclerView.ViewHolder {
        TextView codeTv,//订单编号
                statusTv,//订单状态
                nameTv,//当前人名字
                idCardTv,//身份证号
                seatTv,//座位号|房间号
                realFeeTv,//实际付款
                payTypeTv,//付款类型（前台自付）
                doneTv,     //操作
                expandTv,   //拓展
                levelTv,    //级别
                expecteFeeTv,//预计费用
                idTypeTv, //身份类型
                dateTv;//入住时间|返程时间
        RelativeLayout subRl;
        RelativeLayout userInfoRl;
        View orderInfoRl;
        View line;

        public BaseViewHlder(View itemView) {
            super(itemView);
            subRl = (RelativeLayout) itemView.findViewById(R.id.subRl);
            orderInfoRl = itemView.findViewById(R.id.orderInfoRl);
            line = itemView.findViewById(R.id.line);
            userInfoRl = (RelativeLayout) itemView.findViewById(R.id.userInfoRl);
            codeTv = (TextView) itemView.findViewById(R.id.codeTv);
            statusTv = (TextView) itemView.findViewById(R.id.statusTv);
            nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            idCardTv = (TextView) itemView.findViewById(R.id.idCardTv);
            seatTv = (TextView) itemView.findViewById(R.id.seatTv);
            realFeeTv = (TextView) itemView.findViewById(R.id.realFeeTv);
            payTypeTv = (TextView) itemView.findViewById(R.id.payTypeTv);
            doneTv = (TextView) itemView.findViewById(R.id.doneTv);
            expandTv = (TextView) itemView.findViewById(R.id.expandTv);
            levelTv = (TextView) itemView.findViewById(R.id.levelTv);
            expecteFeeTv = (TextView) itemView.findViewById(R.id.expecteFeeTv);
            idTypeTv = (TextView) itemView.findViewById(R.id.idTypeTv);
            dateTv = (TextView) itemView.findViewById(R.id.dateTv);
        }
    }


    private class AirViewHolder extends BaseViewHlder {
        TextView fromCityTv,
                startDateTv,
                startTimeTv,
                orderTypeTv,//票据类型
                toCityTv,
                toDateTv,
                toTimeTv,
                tripTv,
                changeTv;
        TravelDirectionView mTravelDirectionView;

        public AirViewHolder(ViewGroup viewGroup) {
            this(getInflater().inflate(R.layout.item_bus_travel_air, viewGroup, false));
        }

        public AirViewHolder(View itemView) {
            super(itemView);
            changeTv = itemView.findViewById(R.id.changeTv);
            mTravelDirectionView = itemView.findViewById(R.id.mTravelDirectionView);
            fromCityTv = (TextView) itemView.findViewById(R.id.fromCityTv);
            startDateTv = (TextView) itemView.findViewById(R.id.startDateTv);
            startTimeTv = (TextView) itemView.findViewById(R.id.startTimeTv);
            toCityTv = (TextView) itemView.findViewById(R.id.toCityTv);
            toDateTv = (TextView) itemView.findViewById(R.id.toDateTv);
            toTimeTv = (TextView) itemView.findViewById(R.id.toTimeTv);
            orderTypeTv = (TextView) itemView.findViewById(R.id.orderTypeTv);
            tripTv = (TextView) itemView.findViewById(R.id.tripTv);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.expandTv) {
            int position = (int) v.getTag(R.id.tag_key2);
            BusinessTravel model = (BusinessTravel) v.getTag(R.id.tag_key);
            model.setExpand(!model.isExpand());
            notifyItemChanged(position);
        } else if (R.id.doneTv == id || R.id.subRl == id || R.id.addMoreTv == id) {
            BusinessTravel model = (BusinessTravel) v.getTag(R.id.tag_key);
            TravelUtils.reserve(ct,cusCode, appkey, appSceret, model);
        } else if (R.id.clickAir == id) {
            TravelUtils.reserve(ct, cusCode,appkey, appSceret, getLeaderModel(BusinessTravel.AIR));
        } else if (R.id.clickHotel == id) {
            TravelUtils.reserve(ct,cusCode, appkey, appSceret, getLeaderModel(BusinessTravel.HOTEL));
        } else if (R.id.clickTrain == id) {
            TravelUtils.reserve(ct,cusCode, appkey, appSceret, getLeaderModel(BusinessTravel.TRAIN));
        }
    }

    private BusinessTravel getLeaderModel(int type) {
        BusinessTravel model = new BusinessTravel();
        model.setType(type);
        return model;
    }

}
