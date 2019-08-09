package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.time.wheel.DatePicker;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.uas.appworks.R.id.list_business;

/**
 * @功能:荣誉墙，销售排行榜
 * @author:Arisono
 * @param:
 * @return:
 */
public class
SalesRankingActivity extends BaseActivity {
    private BussinessDetailAdapter mAdapter;
    private TextView tv_date_title;
    private PullToRefreshGridView mlist;
    private TextView tv_msg;
    private ArrayList<Business> mData = new ArrayList<Business>();
    private Context ct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_ranking);

        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mlist.setEnabled(false);
        mlist.setMode(PullToRefreshBase.Mode.DISABLED);

    }

    private void initData() {
        if (CommonUtil.isNetWorkConnected(ct)) {
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, DateFormatUtil.getStrDate4Date(new Date(),
                    "yyyyMM"));
        } else {
            ToastUtil.showToast(ct, R.string.networks_out);
        }
        for (int i = 0; i < 22; i++) {
            Business model = new Business();
            model.setNum("013223" + i);
            model.setName("xxx" + i);
            model.setLeader("****" + i);
            model.setNote("xxxxxxxxxxxx" + i);
            model.setPhone("*********" + i);
            model.setSource("####" + i);
            mData.add(model);
        }
//        mAdapter = new BussinessDetailAdapter(this, mData);
//        mlist.setAdapter(mAdapter);
    }

    private void initView() {
        ct = this;
        tv_date_title = (TextView) findViewById(R.id.tv_date_title);
        mlist = (PullToRefreshGridView) findViewById(list_business);
        tv_msg = (TextView) findViewById(R.id.tv_msg);

        tv_date_title.setText(DateFormatUtil.getStrDate4Date(new Date(), "yyyy年MM月") + getString(R.string.crmmain_wall_of_fame));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_sale_ranking, menu);
        return true;
    }

    /**
     * @desc:日历类
     * @author：Arison on 2016/8/1
     */
    private Calendar calendar = Calendar.getInstance();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sale_date_select) {
            DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
            picker.setRange(1950, 2030);
            picker.setAnimationStyle(R.style.Animation_CustomPopup);
            picker.setSelectedItem(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1);
            picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
                @Override
                public void onDatePicked(String year, String month) {
                    sendHttpResquest(Constants.HTTP_SUCCESS_INIT, year + month);
                    tv_date_title.setText(year + getString(R.string.shorthand_year) + month + getString(R.string.sShorthand_month)
                            + getString(R.string.crmmain_wall_of_fame));
                }
            });
            picker.show();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private class BussinessDetailAdapter extends BaseAdapter {
        private Context ct;
        private JSONArray mdata = new JSONArray();
        private LayoutInflater inflater;

        public BussinessDetailAdapter(Context ct, JSONArray data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mdata == null ? 0 : mdata.size();
        }

        @Override
        public Object getItem(int position) {
            return mdata.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_sale_ranking, null);
                holder = new ViewHolder();
                holder.tv_money = (TextView) convertView.findViewById(R.id.tv_sale_num);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_sale_name);
                holder.tv_depart = (TextView) convertView.findViewById(R.id.tv_sale_dep);
                holder.iv_head = (ImageView) convertView.findViewById(R.id.iv_sale_head);
                holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_sale_desc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_name.setText(mdata.getJSONObject(position).getString("name"));
            holder.tv_money.setText(mdata.getJSONObject(position).getFloatValue("id") + "万元");
            holder.tv_depart.setText(mdata.getJSONObject(position).getString("position")
                    + "\n" + mdata.getJSONObject(position).getString("depart"));
            holder.tv_desc.setText(mdata.getJSONObject(position).getString("desc"));
            AvatarHelper.getInstance().display(
                    mdata.getJSONObject(position).getString("imId")
                    , holder.iv_head, true, true);
            return convertView;
        }


        class ViewHolder {
            TextView tv_name;
            TextView tv_depart;
            TextView tv_desc;
            TextView tv_money;
            ImageView iv_head;
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    JSONObject ranklist = JSON.parseObject(msg.getData().getString("result")).getJSONObject("ranklist");
                    JSONArray sales = ranklist.getJSONArray("sales");
                    JSONArray profits = ranklist.getJSONArray("profits");
                    if (sales != null && profits != null) {
                        JSONArray rootArray = new JSONArray();
                        JSONObject s = new JSONObject();
                        JSONObject p = new JSONObject();

                        float s_id = sales.getFloatValue(0);
                        String s_name = sales.getString(1);
                        String s_position = sales.getString(2);
                        String s_depart = sales.getString(3);
                        int s_imid = sales.getIntValue(4);
                        s.put("id", s_id);
                        s.put("name", s_name);
                        s.put("position", s_position);
                        s.put("depart", s_depart);
                        s.put("imId", s_imid);
                        s.put("desc", "销售总额冠军");

                        float p_id = profits.getFloatValue(0);
                        String p_name = profits.getString(1);
                        String p_position = profits.getString(2);
                        String p_depart = profits.getString(3);
                        int p_imid = profits.getIntValue(4);
                        p.put("id", p_id);
                        p.put("name", p_name);
                        p.put("position", p_position);
                        p.put("depart", p_depart);
                        p.put("imId", p_imid);
                        p.put("desc", "毛利润冠军");
                        rootArray.add(0, s);
                        rootArray.add(1, p);

                        mAdapter = new BussinessDetailAdapter(ct, rootArray);
                        mlist.setAdapter(mAdapter);
                        mlist.setVisibility(View.VISIBLE);
                        tv_msg.setVisibility(View.GONE);
                    } else {
                        //空数据处理
                        JSONArray rootArray = new JSONArray();
                        mAdapter = new BussinessDetailAdapter(ct, rootArray);
                        mlist.setAdapter(mAdapter);

                        mlist.setVisibility(View.GONE);
                        tv_msg.setText(getString(R.string.crm_nodatas));
                        tv_msg.setVisibility(View.VISIBLE);
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));

                    break;
            }
        }
    };

    private void sendHttpResquest(int what, String date) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getRankList.action";
        Map<String, Object> params = new HashMap<>();
        params.put("condition", "and to_char(pi_date,'yyyymm')=" + date);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


}
