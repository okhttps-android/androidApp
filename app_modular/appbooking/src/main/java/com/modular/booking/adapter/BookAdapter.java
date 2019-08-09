package com.modular.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.modular.booking.R;
import com.modular.booking.model.BookingModel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Arison on 2017/6/23.
 */

public class BookAdapter extends BaseAdapter {

    private Context ct;
    private int type=1;
    private Date currentDate;

    private boolean isTime=false;

    public boolean isTime() {
        return isTime;
    }

    public void setTime(boolean time) {
        isTime = time;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    private ArrayList<BookingModel> datas=new ArrayList<>();

    public BookAdapter(Context ct, ArrayList<BookingModel> data){
        this.ct=ct;
        this.datas=data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getCount() {
        return datas!=null?datas.size():0;
    }

    @Override
    public Object getItem(int position) {
        return   datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(ct).inflate(R.layout.item_booking_list, null);
            holder.status_img = (ImageView) convertView.findViewById(R.id.status_img);
            holder.share_img = (ImageView) convertView.findViewById(R.id.iv_share);
            holder.title_tv = (TextView) convertView.findViewById(R.id.title_tv);
            holder.status_tv = (TextView) convertView.findViewById(R.id.status_tv);
            holder.address_tv = (TextView) convertView.findViewById(R.id.address_tv);
            holder.handler_tv = (TextView) convertView.findViewById(R.id.handler_tv);
            holder.topic_tv=(TextView) convertView.findViewById(R.id.topic_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            BookingModel model=datas.get(position);
            holder.model=model;
            if (DateFormatUtil.getStrDate4Date(currentDate, "yyyy-MM-dd")
                    .equals(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd"))){
                if (StringUtil.isEmpty(model.getAb_starttime())){
                    holder.title_tv.setText(model.getAb_endtime().substring(11,16));
                }else{
                    holder.title_tv.setText(model.getAb_starttime().substring(0,10)+" "+model.getAb_starttime().substring(11,16)+"-"+
                            model.getAb_endtime().substring(11,16));
                }
            }else{


            }
            if (isTime){
                //显示具体天
                holder.title_tv.setText(model.getAb_starttime().substring(0,10)+" "+model.getAb_starttime().substring(11,16)+"-"+
                        model.getAb_endtime().substring(11,16));
            }else{
                //不显示具体天
               if (StringUtil.isEmpty(model.getAb_starttime())){
                   holder.title_tv.setText(model.getAb_endtime().substring(11,16));
               }else{
                   holder.title_tv.setText(model.getAb_starttime().substring(11,16)+"-"+
                           model.getAb_endtime().substring(11,16));
               }
            }

            holder.status_tv.setText(model.getAb_confirmstatus());

            if(model.getAb_sharestatus().equals("已共享")){
                holder.share_img.setVisibility(View.VISIBLE);
            }else{
                holder.share_img.setVisibility(View.GONE);
            }
            if (type==2){
                holder.handler_tv.setText(model.getAb_recordman()+"—>"+model.getAb_bman());
            }else{
                holder.handler_tv.setText(model.getAb_recordman()+"—>"+model.getAb_bman());
            }

            holder.topic_tv.setText(model.getAb_type());
            holder.model.setAb_type(model.getAb_type());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public class ViewHolder {
        public ImageView status_img,share_img;
        public TextView title_tv, status_tv, address_tv, handler_tv,topic_tv;
        public BookingModel model;
        public String data_service;
    }
}
