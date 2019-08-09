package com.uas.appworks.OA.erp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.core.widget.MyListView;
import com.uas.appworks.OA.erp.model.CommonDocAMBean;
import com.uas.appworks.R;

import java.util.List;


/**
 * Created by FANGlh on 2016/11/27.
 */
public class ComDocGriddataOutAdapter extends BaseAdapter {
    private List<CommonDocAMBean.DatasBean.GridconfigsBean> mGridconfigsBean;
    private JSONArray griddataBeans;
    private Context mContext;
    private ComGriddataInsideAdapter  mComGriddataInsideAdapter;
    private String caller;

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public ComDocGriddataOutAdapter(Context mContext) {

        this.mContext = mContext;
        mComGriddataInsideAdapter = new ComGriddataInsideAdapter(mContext);

    }

    public List<CommonDocAMBean.DatasBean.GridconfigsBean> getmGridconfigsBean() {
        return mGridconfigsBean;
    }

    public void setmGridconfigsBean(List<CommonDocAMBean.DatasBean.GridconfigsBean> mGridconfigsBean) {
        this.mGridconfigsBean = mGridconfigsBean;
    }

    public JSONArray getGriddataBeans() {
        return griddataBeans;
    }

    public void setGriddataBeans(JSONArray griddataBeans) {
        this.griddataBeans = griddataBeans;
    }


    @Override
    public int getCount() {
        return griddataBeans == null ? 0 : griddataBeans.size();
    }

    @Override
    public Object getItem(int position) {
        try {
            return griddataBeans.get(position);
        } catch (JSONException e) {
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

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_comdoc_secondout,null);
            viewHolder.detail_title_num = (TextView) convertView.findViewById(R.id.item_comdoc_secondout_detail_title_tv);
            viewHolder.secondout_lv = (MyListView) convertView.findViewById(R.id.item_comdoc_secondout_lv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        mComGriddataInsideAdapter.setmGridconfigsBean(mGridconfigsBean);
        try {
            mComGriddataInsideAdapter.setGriddataBeans(griddataBeans.getJSONObject(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        viewHolder.secondout_lv.setAdapter(mComGriddataInsideAdapter);

        //明细显示
        if(!TextUtils.isEmpty(getCaller())){
//            String num = CommonUtil.numToCN(position + 1);
//            if ("Ask4Leave".equals(getCaller())){
//                viewHolder.detail_title_num.setText("请假明细" );
//            }
//            if ("FeePlease!CCSQ!new".equals(getCaller())){
//                viewHolder.detail_title_num.setText("出差明细");
//            }
//            if ("Workovertime".equals(getCaller())){
//                viewHolder.detail_title_num.setText("加班明细");
//            }
//            if ("SpeAttendance".equals(getCaller())){
//                viewHolder.detail_title_num.setText("特殊考勤明细");
//            }
//            if ("FeePlease!FYBX".equals(getCaller())){
//                viewHolder.detail_title_num.setText("消费明细");
//            }
            viewHolder.detail_title_num.setText("明细" );
        }


        return convertView;
    }
    class ViewHolder{
        MyListView secondout_lv;
        TextView detail_title_num;
    }
}
