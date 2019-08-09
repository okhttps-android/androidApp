package com.core.widget.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.core.app.R;
import com.core.widget.view.model.SearchLocationModel;

import java.util.List;

/**
 * Created by Bitliker on 2017/2/6.
 */

public class SearchLocationAdapter extends BaseAdapter {
    private Context ct;
    private List<SearchLocationModel> listData;
    private String distanceTag;


    public SearchLocationAdapter(Context ct, List<SearchLocationModel> listData, String distanceTag) {
        this.ct = ct;
        this.listData = listData;
        this.distanceTag = distanceTag;
    }

    public List<SearchLocationModel> getListData() {
        return listData;
    }

    public void setListData(List<SearchLocationModel> listData, String distanceTag) {
        this.distanceTag = distanceTag;
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return ListUtils.isEmpty(listData) ? 0 : listData.size();
    }

    @Override
    public Object getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(ct).inflate(R.layout.item_location_ls, null);
            holder = new ViewHolder();
            holder.address_tv = (TextView) view.findViewById(R.id.address_tv);
            holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
            holder.distance_tv = (TextView) view.findViewById(R.id.distance_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.address_tv.setText(listData.get(i).getPoiInfo().address);
        holder.name_tv.setText(listData.get(i).getPoiInfo().name);
        String distance = (listData.get(i).getDistance() == 0 ? "0" : listData.get(i).getDistance()) + "" + distanceTag;
        holder.distance_tv.setText(distance);
        return view;
    }

//    private String getDistance(LatLng location) {
//        String dis = BaiduMapUtil.getInstence().getDistance(location, location);
//        if (StringUtil.isEmpty(dis)) return String.valueOf(0);
//        if (isShowKm) {
//            return getKm(dis) + " km";
//        } else {
//            return dis + " m";
//        }
//    }

//    private String getKm(String dis) {
//        if (StringUtil.isEmpty(dis)) return String.valueOf(0);
//        try {
//            DecimalFormat fnum = new DecimalFormat("##0.00");
//            String dd = fnum.format(Float.valueOf(dis) / 1000);
//            return dd;
//        } catch (ClassCastException e) {
//            return String.valueOf(0);
//        } catch (Exception e) {
//            return String.valueOf(0);
//        }
//    }

    class ViewHolder {
        TextView name_tv, address_tv, distance_tv;
    }
}
