package com.xzjmyk.pm.activity.ui.platform.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.common.data.ListUtils;
import com.core.widget.EmptyLayout;
import com.xzjmyk.pm.activity.ui.platform.pageforms.PagesModel;
import com.core.utils.TimeUtils;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Arison on 2017/3/7.
 */
public class PagesModelAdapter extends BaseAdapter implements Filterable {

    private static final int DELETE_SUCCEED = 0x328;
    private Context ct;
    private List<PagesModel> datas = new ArrayList<>();
    private List<PagesModel> searchDatas;
    private List<PagesModel> allDatas;
    private LayoutInflater inflater;
    private int type;
    private MyFilter mMyFilter;
    private int mkeyValue = -1;
    private int whichpage = -1;
    private int mPosition;
    private EmptyLayout mEmptyLayout;

    public EmptyLayout getmEmptyLayout() {
        return mEmptyLayout;
    }

    public void setmEmptyLayout(EmptyLayout mEmptyLayout) {
        this.mEmptyLayout = mEmptyLayout;
    }

    public PagesModelAdapter(List<PagesModel> datas, Context ct) {
        this.datas = datas;
        this.ct = ct;
        this.inflater = LayoutInflater.from(ct);
        this.searchDatas = new ArrayList<>();
        this. allDatas = datas;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 :datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas == null ? 0 : datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewModel model = null;
        if (convertView == null) {
            model = new ViewModel();
            switch (type) {
                case 0://请假单
                    convertView = inflater.inflate(R.layout.item_simple_text11, null);
                    model.status = (TextView) convertView.findViewById(R.id.tv_state_value);
                    model.type = (TextView) convertView.findViewById(R.id.tv_type_value);
                    model.startTime = (TextView) convertView.findViewById(R.id.tv_start_value);
                    model.endTime = (TextView) convertView.findViewById(R.id.tv_end_value);
                    model.content = (TextView) convertView.findViewById(R.id.tv_content_value);

                    model.filed2 = (LinearLayout) convertView.findViewById(R.id.ll_type);
                    model.filed5 = (LinearLayout) convertView.findViewById(R.id.ll_field_5);
                    model.delete_tv = (LinearLayout) convertView.findViewById(R.id.delete_tv);
                    convertView.setTag(model);
                    break;
                case 1://出差单
                    convertView = inflater.inflate(R.layout.item_simple_text12, null);
                    model.status = (TextView) convertView.findViewById(R.id.tv_state_value);
                    model.type = (TextView) convertView.findViewById(R.id.tv_type_value);
                    model.startTime = (TextView) convertView.findViewById(R.id.tv_start_value);
                    model.endTime = (TextView) convertView.findViewById(R.id.tv_end_value);
                    model.content = (TextView) convertView.findViewById(R.id.tv_content_value);
                    model.fp_recorddate = (TextView) convertView.findViewById(R.id.fp_recorddate);
                    model.fp_people2 = (TextView) convertView.findViewById(R.id.fp_people2);
                    model.fp_code = (TextView) convertView.findViewById(R.id.fp_code);
                    model.filed2 = (LinearLayout) convertView.findViewById(R.id.ll_type);
                    model.filed5 = (LinearLayout) convertView.findViewById(R.id.ll_field_5);
                    model.delete_tv = (LinearLayout) convertView.findViewById(R.id.delete_tv);
                    convertView.setTag(model);
                    break;
                case 2://加班单
                    convertView = inflater.inflate(R.layout.item_simple_text13, null);
                    model.status = (TextView) convertView.findViewById(R.id.tv_state_value);
                    model.type = (TextView) convertView.findViewById(R.id.tv_type_value);
                    model.startTime = (TextView) convertView.findViewById(R.id.tv_start_value);
                    model.endTime = (TextView) convertView.findViewById(R.id.tv_end_value);
                    model.content = (TextView) convertView.findViewById(R.id.tv_content_value);

                    model.filed2 = (LinearLayout) convertView.findViewById(R.id.ll_type);
                    model.filed5 = (LinearLayout) convertView.findViewById(R.id.ll_field_5);
                    model.delete_tv = (LinearLayout) convertView.findViewById(R.id.delete_tv);
                    convertView.setTag(model);
                    break;
            }
        } else {
            model = (ViewModel) convertView.getTag();
        }

        switch (type) {
            case 0:
                JSONObject jsonObject = JSON.parseObject(datas.get(position).getModeJson());
                model.status.setText(datas.get(position).getState());
                model.startTime.setText(datas.get(position).getStartTime());
                model.endTime.setText(datas.get(position).getEndTime());
                model.content.setText(jsonObject.getString("va_remark"));
                model.type.setText(jsonObject.getString("va_vacationtype"));
                mkeyValue = CommonUtil.getNumByString(datas.get(position).getId());
                whichpage = 1;
                break;
            case 1:
                jsonObject = JSON.parseObject(datas.get(position).getModeJson());
                model.status.setText(datas.get(position).getState());
                model.startTime.setText(datas.get(position).getStartTime());
                model.endTime.setText(datas.get(position).getEndTime());
                model.fp_people2.setText(jsonObject.getString("fp_v3"));
                model.fp_recorddate.setText(TimeUtils.f_long_2_str(jsonObject.getLongValue("fp_recorddate")));
                model.fp_code.setText(jsonObject.getString("fp_code"));
                mkeyValue = CommonUtil.getNumByString(datas.get(position).getId());
                whichpage = 2;
                break;
            case 2:
                jsonObject = JSON.parseObject(datas.get(position).getModeJson());
                model.status.setText(datas.get(position).getState());
                model.startTime.setText(datas.get(position).getStartTime());
                model.endTime.setText(datas.get(position).getEndTime());
                mkeyValue = CommonUtil.getNumByString(datas.get(position).getId());
                whichpage = 3;
        }

        if (unsubmit(datas.get(position).getState())){
            model.delete_tv.setVisibility(View.VISIBLE);
        }else {
            model.delete_tv.setVisibility(View.GONE);
        }
        final ViewModel finalModel = model;
        model.delete_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog
                        .Builder(ct)
                        .setTitle(MyApplication.getInstance().getString(R.string.common_notice))
                        .setMessage(MyApplication.getInstance().getString(R.string.delete_notice1))
                        .setNegativeButton(MyApplication.getInstance().getString(R.string.common_cancel), null)
                        .setPositiveButton(MyApplication.getInstance().getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int item_id = CommonUtil.getNumByString(datas.get(position).getId());
                                mPosition = position;
                                doPlatDeleteByid(item_id, whichpage);
                                finalModel.delete_tv.setEnabled(false);
                            }
                        }).show();
            }
        });
        return convertView;
    }

    private boolean unsubmit(String status){
        if (!StringUtil.isEmpty(status) && "在录入".equals(status)) return true;
        else return false;
    }
    private void doPlatDeleteByid(int item_id, int whichpage) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().delete_common_doc_url;
        Map<String,Object> param = new HashMap<>();
        param.put("id",item_id);
        param.put("whichpage",whichpage);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, DELETE_SUCCEED, null, null, "post");

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DELETE_SUCCEED:
                    if (msg.getData() != null){
                        String delete_result=msg.getData().getString("result");
                        LogUtil.d("delete_result", delete_result);
                        if (JSON.parseObject(delete_result).containsKey("success")
                                && JSON.parseObject(delete_result).getBooleanValue("success")){
                            Toast.makeText(ct,MyApplication.getInstance().getString(R.string.delete_succeed_notice1),Toast.LENGTH_LONG).show();
                            if (!ListUtils.isEmpty(datas)) {
                                datas.remove(mPosition);
                                notifyDataSetChanged();
                            }
                        }
                    }else {
                        Toast.makeText(ct,MyApplication.getInstance().getString(R.string.delete_failed_notice1),Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            Toast.makeText(ct, msg.getData().getString("result"), Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    };
    @Override
    public Filter getFilter() {
        if (mMyFilter == null) {
            mMyFilter = new MyFilter();
        }
        return mMyFilter;
    }

    class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults searchResults = new FilterResults();
            searchDatas = new ArrayList<>();

            if (TextUtils.isEmpty(constraint)) {
                searchDatas = allDatas;
            } else {
                for (int i = 0; i < allDatas.size(); i++) {
                    String state = "";
                    String startTime = "";
                    String endTime = "";
                    String remark = "";
                    String vacationType = "";
                    String people2 = "";
                    String recordDate = ""; // 录入时间
                    String code = "";
                    String tra_reason = ""; //出差事由
                    String tra_location = "" ; // 出差目的地
                    String work_reason = ""; //加班目的
                    String work_count = ""; //加班时长
                    String work_starttime = "";
                    String work_endtime = "";


                    JSONObject jsonObject = JSON.parseObject(allDatas.get(i).getModeJson());
                    state =allDatas.get(i).getState();
                    startTime = allDatas.get(i).getStartTime();
                    endTime = allDatas.get(i).getEndTime();
                    switch (type) {
                        case 0:
                            remark = jsonObject.getString("va_remark");
                            vacationType = jsonObject.getString("va_vacationtype");
                            break;
                        case 1:
//                            people2 = jsonObject.getString("fp_people2");
                            recordDate = DateFormatUtil.long2Str(jsonObject.getLongValue("fp_recorddate"), DateFormatUtil.YMD_HMS);
//                            code = jsonObject.getString("fp_code");
                            tra_reason =jsonObject.getString("fp_v3");
                            tra_location = jsonObject.getJSONArray("feePleaseDetails").getJSONObject(0).getString("fpd_location");
                            break;
                        case 2:
                            work_count = Double.toString(jsonObject.getJSONArray("workovertimedet").getJSONObject(0).getLongValue("wod_count"));
                            work_reason = jsonObject.getString("wo_worktask");
                            work_starttime = DateFormatUtil.long2Str(jsonObject.getJSONArray("workovertimedet").getJSONObject(0).getLongValue("wod_startdate"), DateFormatUtil.YMD_HMS);
                            work_endtime = DateFormatUtil.long2Str(jsonObject.getJSONArray("workovertimedet").getJSONObject(0).getLongValue("wod_enddate"), DateFormatUtil.YMD_HMS);
                            break;
                    }

                    if (state.contains(constraint) || startTime.contains(constraint)
                            || endTime.contains(constraint) || remark.contains(constraint)
                            || vacationType.contains(constraint) || tra_reason.contains(constraint)
                            || recordDate.contains(constraint) || tra_location.contains(constraint)
                            || work_reason.contains(constraint) || work_starttime.contains(constraint)
                            || work_endtime.contains(constraint) || work_count.contains(constraint)) {
                        searchDatas.add(allDatas.get(i));
                    }
                }
            }
            searchResults.values = searchDatas;
            searchResults.count = searchDatas.size();
            return searchResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            datas = (List<PagesModel>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
                mEmptyLayout.showEmpty();
            }
        }
    }

    public class ViewModel {
        TextView status;
        TextView startTime;
        TextView endTime;
        TextView content;

        TextView type;
        TextView fp_people2;
        TextView fp_recorddate;
        TextView fp_code;

        LinearLayout filed2;
        LinearLayout filed5;

        LinearLayout delete_tv;
    }
}
