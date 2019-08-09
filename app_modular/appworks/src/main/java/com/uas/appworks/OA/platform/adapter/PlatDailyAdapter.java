package com.uas.appworks.OA.platform.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.platform.model.PlatDailyBean;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.CUPCAKE)

/**
 * Created by FANGlh on 2017/3/8.
 * function: b2b平台日报列表适配器，返回值参数key变了，只能重写了
 */
public class PlatDailyAdapter extends BaseAdapter {
    private static final int PLAT_DELETE_DAILY = 309;
    private List<PlatDailyBean.DataBean> pdata;
    private Context mContext;
    private int mPosition;
    private String search_content;

    public String getSearch_content() {
        return search_content;
    }

    public void setSearch_content(String search_content) {
        this.search_content = search_content;
    }

    //获取上下文对象
    public PlatDailyAdapter(Context mContext) {
        this.mContext = mContext;
    }
    public List<PlatDailyBean.DataBean> getPdata() {
        return pdata;
    }

    public void setPdata(List<PlatDailyBean.DataBean> pdata) {
        this.pdata = pdata;
    }
    @Override
    public int getCount() {
        return pdata == null ? 0 : pdata.size();
    }
    @Override
    public Object getItem(int position) {
        return pdata.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_activity_workdaily, null);
            viewHolder.WorkDailyDate = (TextView) convertView.findViewById(R.id.item_activity_workdaily_time_tv);
            viewHolder.WorkDailySummary = (TextView) convertView.findViewById(R.id.item_activity_workdaily_summary_tv);
            viewHolder.WorkDailyStatus = (TextView) convertView.findViewById(R.id.item_activity_workdaily_status);
            viewHolder.WorkDailyDelete = (TextView) convertView.findViewById(R.id.unsubmit_delete_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.WorkDailyDate.setText(TimeUtils.s_long_2_str(CommonUtil.getlongNumByString(pdata.get(position).getWd_date())));
        //为日报列表界面赋值（状态，总结）,注意已审批和待审批的字体颜色
        if(!TextUtils.isEmpty(pdata.get(position).getWd_status()) &&
                pdata.get(position).getWd_status().equals("已审核")){
            viewHolder.WorkDailyStatus.setTextColor(mContext.getResources().getColor(R.color.approval));
            viewHolder.WorkDailyStatus.setText(mContext.getString(R.string.status_approved));
            viewHolder.WorkDailyDelete.setVisibility(View.GONE);
        }else if (!TextUtils.isEmpty(pdata.get(position).getWd_status()) &&
                pdata.get(position).getWd_status().equals("已提交")){
            viewHolder.WorkDailyStatus.setTextColor(mContext.getResources().getColor(R.color.no_approval));
            viewHolder.WorkDailyStatus.setText(mContext.getString(R.string.status_pending));
            viewHolder.WorkDailyDelete.setVisibility(View.GONE);
        }else if(!TextUtils.isEmpty(pdata.get(position).getWd_status()) &&
                pdata.get(position).getWd_status().equals("在录入")){
            viewHolder.WorkDailyStatus.setTextColor(mContext.getResources().getColor(R.color.done_approval));
            viewHolder.WorkDailyStatus.setText(mContext.getString(R.string.status_unsubmit));
            viewHolder.WorkDailyDelete.setVisibility(View.VISIBLE);
        }
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES <= 20){ // TODO 处理Android 4.4以下某些机型 android:ellipsize=“end”失效bug
            viewHolder.WorkDailySummary.setEllipsize(null);
        }
        viewHolder.WorkDailySummary.setText(pdata.get(position).getWd_comment());

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.WorkDailyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog
                        .Builder(mContext)
                        .setTitle(mContext.getString(R.string.common_notice))
                        .setMessage(mContext.getString(R.string.delete_notice1))
                        .setNegativeButton(mContext.getString(R.string.common_cancel), null)
                        .setPositiveButton(mContext.getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String mCaller = "WorkDaily";
                                    int mkeyValue = pdata.get(position).getWd_id();
                                    mPosition = position;
                                    doPlatdelete(mCaller, mkeyValue);
                                    finalViewHolder.WorkDailyDelete.setVisibility(View.GONE);
                                }
                            }).show();
            }
        });

        return convertView;
    }

    private void doPlatdelete(String mCaller, int mkeyValue) {
        // 删除已反提交的在录入状态的日报
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().delete_work_daily;
        Map<String,Object> param = new HashMap<>();
        param.put("id",mkeyValue);
        param.put("whichpage",4);
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode",CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(mContext, url, param, handler, headers, PLAT_DELETE_DAILY, null, null, "post");
    }

    class ViewHolder{
        TextView WorkDailyDate;
        TextView WorkDailySummary;
        TextView WorkDailyStatus;
        TextView WorkDailyDelete;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PLAT_DELETE_DAILY:
                    if (msg.getData() != null){
                        String delete_result = msg.getData().getString("result");
                        if (JSON.parseObject(delete_result).containsKey("success") && JSON.parseObject(delete_result).getBoolean("success")){
                            Toast.makeText(mContext, mContext.getString(R.string.daily_delate_success),Toast.LENGTH_LONG);
                            pdata.remove(mPosition);
                            notifyDataSetChanged();
                        }
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            Toast.makeText(mContext, msg.getData().getString("result"), Toast.LENGTH_LONG);
                        }
                    }
                    break;
            }
        }
    };
}
