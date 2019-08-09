package com.uas.appworks.OA.erp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.common.data.ListUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.model.CommonDocAMBean;

import java.util.List;

/**
 * Created by FANGlh on 2016/11/25.
 */
public class CommonDocMainMsgAdapter extends BaseAdapter {
    private List<CommonDocAMBean.DatasBean.FormconfigsBean> mFormconfigsBean;
    private JSONArray formdataBeans;
    private Context mContext;

    public CommonDocMainMsgAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public JSONArray getFormdataBeans() {
        return formdataBeans;
    }

    public void setFormdataBeans(JSONArray formdataBeans) {
        this.formdataBeans = formdataBeans;
    }


    public List<CommonDocAMBean.DatasBean.FormconfigsBean> getmFormconfigsBean() {
        return mFormconfigsBean;
    }

    public void setmFormconfigsBean(List<CommonDocAMBean.DatasBean.FormconfigsBean> mFormconfigsBean) {
        this.mFormconfigsBean = mFormconfigsBean;
    }


    @Override
    public int getCount() {
        return mFormconfigsBean == null ? 0 : mFormconfigsBean.size();
    }

    @Override
    public Object getItem(int position) {
        return mFormconfigsBean.get(position);
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
            viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
            viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (mFormconfigsBean != null && formdataBeans != null) {
            viewHolder.docmainmsg_list.setText(mFormconfigsBean.get(position).getFD_CAPTION());
            if (!ListUtils.isEmpty(formdataBeans)) try {
                if (formdataBeans.getJSONObject(0).getString(mFormconfigsBean.get(position).getFD_FIELD()) != null) {
                    if (!formdataBeans.getJSONObject(0).getString(mFormconfigsBean.get(position).getFD_FIELD()).equals("null")) {
                        Log.i("VALUE", mFormconfigsBean.get(position).getFD_FIELD().toString());
                        viewHolder.docmainmsg_value.setText(formdataBeans.getJSONObject(0)
                                .getString(mFormconfigsBean.get(position).getFD_FIELD().toString()));
                    } else {
                        viewHolder.docmainmsg_value.setText("");
                    }

                } else {
                    viewHolder.docmainmsg_value.setText("");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView docmainmsg_list;
        TextView docmainmsg_value;
    }
}
