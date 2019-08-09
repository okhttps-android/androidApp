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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.view.SmoothCheckBox;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @功能:商机转移
 * @author:Arisono
 * @param:
 * @return:
 */
public class BusinessTransferActivity extends BaseActivity {

    private BussinessDetailAdapter mAdapter;
    private PullToRefreshListView mlist;
    private ArrayList<Business> mData = new ArrayList<Business>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_transfer);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        setTitle("转移到商机库");
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);
    }

    private void initListener() {
        mlist.setMode(PullToRefreshBase.Mode.DISABLED);
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                final boolean flag = !mData.get(position - 1).isChecked;
                Log.i(TAG, "onClick:" + position + " check:" + !flag);
                nicehouse = mData.get(position - 1).getName();
                for (Business model : mData) {
                    model.setIsChecked(false);
                }

                mData.get(position - 1).setIsChecked(flag);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                }, 90);


            }
        });

    }

    private String nicehouse;
    private String code;
    private String currentHouse;
    private final int BUSSINE_TRANSFER = 3;

    private void initData() {
        sendHttpResquest(Constants.HTTP_SUCCESS_INIT);
        if (getIntent() != null) {
            code = getIntent().getStringExtra("code");
            currentHouse = getIntent().getStringExtra("name");
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
        mAdapter = new BussinessDetailAdapter(this, mData);
        mlist.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_save){
            if (StringUtil.isEmpty(nicehouse)) {
                ToastMessage("请选择一种商机库");
            } else {
                sendHttpResquestTransfer(BUSSINE_TRANSFER, code, nicehouse);
            }
        }else if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    private class BussinessDetailAdapter extends BaseAdapter {
        private Context ct;
        private ArrayList<Business> mdata = new ArrayList<>();
        private LayoutInflater inflater;

        public BussinessDetailAdapter(Context ct, ArrayList<Business> data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mdata.size();
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
                convertView = inflater.inflate(R.layout.item_business_transfer, null);
                holder = new ViewHolder();
                holder.cb_left = (SmoothCheckBox) convertView.findViewById(R.id.cb_left);
                holder.tv_business_name = (TextView) convertView.findViewById(R.id.tv_business_name);

//                convertView.setClickable(true);
//                convertView.setFocusable(true);
                holder.view = convertView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
//            holder.cb_left.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
//
//                    final  boolean flag = !mdata.get(position).isChecked;
//                    Log.i(TAG, "onClick:" + position + " check:" + !flag);
//                    nicehouse=mdata.get(position).getName();
//                    for (Business model : mdata) {
//                        model.setIsChecked(false);
//                    }
//
//                    mdata.get(position).setIsChecked(flag);
//                    mAdapter.notifyDataSetChanged();
//                }
//            });
//            holder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    boolean flag = !mdata.get(position).isChecked;
//                    Log.i(TAG, "onClick:" +position+" check:"+!flag );
//
//                    for (Business model : mdata) {
//                        model.setIsChecked(false);
//                    }
//
//                    mdata.get(position).setIsChecked(flag);
//                    notifyDataSetChanged();
//                }
//            });
            holder.cb_left.setFocusable(false);
            holder.cb_left.setEnabled(false);
            holder.cb_left.setClickable(false);
            holder.tv_business_name.setText(mdata.get(position).getName());
            holder.cb_left.setChecked(mdata.get(position).isChecked(), mdata.get(position).isChecked());
            return convertView;
        }


        class ViewHolder {
            View view;
            SmoothCheckBox cb_left;
            TextView tv_business_name;
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    mData.clear();
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    JSONObject root = JSON.parseObject(msg.getData().getString("result"));
                    JSONArray items = root.getJSONArray("combos");
                    if (!items.isEmpty()) {
                        for (int i = 0; i < items.size(); i++) {
                            if (!items.getJSONObject(i).getString("BD_NAME").equals(currentHouse)){
                                Business model = new Business();
                                model.setName(items.getJSONObject(i).getString("BD_NAME"));
                                // model.setNum(items.getJSONObject(i).getString("DLC_DISPLAY"));
                                mData.add(model);

                            }
                        }
                    }
                    mAdapter = new BussinessDetailAdapter(ct, mData);
                    mlist.setAdapter(mAdapter);
                    break;
                case BUSSINE_TRANSFER:
                    progressDialog.dismiss();
                    jumpToStateActivity();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));

                    break;
            }
        }
    };

    private void jumpToStateActivity() {
        ToastMessage("转移成功！");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2500);
    }

    private void sendHttpResquest(int what) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getNichehouse.action";
        Map<String, Object> params = new HashMap<>();
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    /**
     * @desc:商机转移
     * @author：Arison on 2016/7/25
     */
    private void sendHttpResquestTransfer(int what, String bc_code, String bc_nicehehouse) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/updateBusinessChanceHouse.action";
        Map<String, Object> params = new HashMap<>();
        params.put("bc_code", bc_code);
        params.put("bc_nichehouse", bc_nicehehouse);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }
}
