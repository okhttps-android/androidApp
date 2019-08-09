package com.uas.appme.settings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.MyListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;
import com.uas.appme.pedometer.utils.TimeUtil;
import com.uas.appme.settings.model.CompanyRestBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/10/12.
 * function:
 */

public class BComSetCompanyRestActivity extends SupportToolBarActivity implements View.OnClickListener{
    private MyListView mComList;
    private List<CompanyRestBean> mList;  //进行保存的员工休息数据列表
    private ComRestAdapter myAdapter;
    private String current_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_bcom_setting_activity);
        initView();
    }



    private void initView() {
        mList = new ArrayList<>();
        mComList = (MyListView) findViewById(R.id.com_list);
        findViewById(R.id.add_new_rl).setOnClickListener(this);
        myAdapter = new ComRestAdapter(this);
        myAdapter.setModelList(mList);
        mComList.setAdapter(myAdapter);
        findViewById(R.id.save_bt).setOnClickListener(this);

        //初始化今天日期
        String CURRENT_DATE = TimeUtil.getCurrentDate();
        current_date = TimeUtils.s_long_2_str(DateFormatUtil.str2Long(CURRENT_DATE, "yyyy年MM月dd日"));

        CompanyRestBean model = new CompanyRestBean();
        model.setSc_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
//            model.setSc_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        model.setSc_companyname(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany"));
        model.setSc_date("");
        mList.add(model);
        myAdapter.notifyDataSetChanged();

        //接收商家类型
          sc_industry = getIntent().getStringExtra("sc_industry");
         sc_industrycode = getIntent().getStringExtra("sc_industrycode");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_new_rl){
            CompanyRestBean model = new CompanyRestBean();
            model.setSc_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
//            model.setSc_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
            model.setSc_companyname(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany"));
            model.setSc_date("");
            mList.add(model);
            myAdapter.notifyDataSetChanged();
        }else if (v.getId() == R.id.save_bt){
            if (!CommonUtil.isNetWorkConnected(ct)) {
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            LogUtil.prinlnLongMsg("mList", JSON.toJSONString(mList));
            if (ListUtils.isEmpty(mList)) return;

            for (int i = 0; i < mList.size(); i++) {
                if (StringUtil.isEmpty(mList.get(i).getSc_date())){
                    ToastMessage(getString(R.string.input_all_date));
                    break;
                }
                if (i==mList.size()-1)
                    doSave(mList);
            }
        }
    }

    private void doSave(List<CompanyRestBean> mList) {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appBatchCrest")
                .add("map",JSON.toJSONString(mList))
                .add("token",MyApplication.getInstance().mAccessToken)
                .connectTimeout(10000)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) {
                    progressDialog.dismiss();
                    return;
                }
                LogUtil.prinlnLongMsg("appBatchMrest", o.toString()+"");
                if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")) {
                    Toast.makeText(ct,getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ct,BSetComRestListActivity.class)
                            .putExtra("sc_industry",sc_industry)
                            .putExtra("sc_industrycode",sc_industrycode)
                            .putExtra("type","company"));
                    finish();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Object t) {
                progressDialog.dismiss();
                ToastMessage(getString(R.string.too_long_to_http));
            }
        }));

    }

    private class ComRestAdapter extends BaseAdapter{
        private Context mContext;
        private List<CompanyRestBean> modelList;

        public List<CompanyRestBean> getModelList() {
            return modelList;
        }

        public void setModelList(List<CompanyRestBean> modelList) {
            this.modelList = modelList;
        }

        public ComRestAdapter(Context mContext){
            this.mContext = mContext;
        }
        @Override
        public int getCount() { return ListUtils.isEmpty(modelList) ? 0 : modelList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView =  View.inflate(mContext, R.layout.com_rest_item,null);
                viewHolder.date_tv = (TextView) convertView.findViewById(R.id.date_tv);
                viewHolder.name_rl = (RelativeLayout) convertView.findViewById(R.id.name_rl);
                viewHolder.line = convertView.findViewById(R.id.line);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.name_rl.setVisibility(View.GONE);
            viewHolder.line.setVisibility(View.GONE);
            if (!ListUtils.isEmpty(modelList)){
                viewHolder.date_tv.setText(modelList.get(position).getSc_date()+"");
            }

            viewHolder.date_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSelectEndDate(position);
                }
            });
            return convertView;
        }

        class ViewHolder{
            TextView date_tv;
            RelativeLayout name_rl;
            View line;
        }
    }

    private void doSelectEndDate(final int pos) {
        OASigninPicker picker = new OASigninPicker(this);
        picker.setRange(CalendarUtil.getYear()+1, CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                if (current_date.compareTo(time) > 0) {
                    ToastMessage(getString(R.string.cannot_select_beforedate));
                    return;
                }else {
                    if (JSON.toJSONString(mList).contains(time)){
                        ToastMessage(getString(R.string.relax_time_rapeat));
                        return;
                    }
                    mList.get(pos).setSc_date(time);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
        picker.show();
    }

    private String sc_industry;
    private String sc_industrycode;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bsetting_more, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.booking_set_list){
            startActivity(new Intent(ct,BSetComRestListActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode)
                    .putExtra("type","company"));
        }
        return super.onOptionsItemSelected(item);
    }
}


