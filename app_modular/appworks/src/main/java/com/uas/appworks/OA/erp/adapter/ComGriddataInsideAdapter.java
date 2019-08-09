package com.uas.appworks.OA.erp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.model.CommonDocAMBean;

import java.util.List;


/**
 * Created by FANGlh on 2016/11/27.
 */
public class ComGriddataInsideAdapter extends BaseAdapter {
    private Context mContext;
    private List<CommonDocAMBean.DatasBean.GridconfigsBean> mGridconfigsBean;
    private JSONObject griddataBeans;

    public ComGriddataInsideAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public List<CommonDocAMBean.DatasBean.GridconfigsBean> getmGridconfigsBean() {
        return mGridconfigsBean;
    }

    public void setmGridconfigsBean(List<CommonDocAMBean.DatasBean.GridconfigsBean> mGridconfigsBean) {
        this.mGridconfigsBean = mGridconfigsBean;
    }

    public JSONObject getGriddataBeans() {
        return griddataBeans;
    }

    public void setGriddataBeans(JSONObject griddataBeans) {
        this.griddataBeans = griddataBeans;
    }

    @Override
    public int getCount() {
        return mGridconfigsBean == null ? 0 : mGridconfigsBean.size();
    }

    @Override
    public Object getItem(int position) {
        try {
            return mGridconfigsBean.get(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_comdoc_am, null);
            viewHolder.list_tv = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
            viewHolder.value_tv = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (griddataBeans != null && mGridconfigsBean != null) {
            viewHolder.list_tv.setText(mGridconfigsBean.get(position).getDG_CAPTION());
            try {
                if (griddataBeans.getString(mGridconfigsBean.get(position).getDG_FIELD()) != null) {
                    if (!griddataBeans.getString(mGridconfigsBean.get(position).getDG_FIELD()).equals("null")){
                        viewHolder.value_tv.setText(griddataBeans.getString(mGridconfigsBean.get(position).getDG_FIELD()));
                        Log.i("mmm",mGridconfigsBean.get(position).getDG_FIELD());
                    }else {
                        viewHolder.value_tv.setText("");
                    }

                } else {
                    viewHolder.value_tv.setText("");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    class ViewHolder {
        TextView list_tv;
        TextView value_tv;

    }
}
