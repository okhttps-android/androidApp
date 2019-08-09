package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.core.widget.EmptyLayout;
import com.core.widget.crouton.Crouton;
import com.core.widget.crouton.Style;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.SmoothCheckBox;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BusinessSelectCustomerActivity extends BaseActivity {
    private BussinessDetailAdapter mAdapter;
    private ArrayList<Business> mData = new ArrayList<Business>();
    private String code;
    private ClearEditText search_edit;
    private PullToRefreshListView mlist;

    private EmptyLayout mEmptyLayout;

    private String cu_code;
    private String cu_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_select_customer);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        search_edit = (ClearEditText) findViewById(R.id.search_edit);
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);
        setTitle("选择客户");
        if (getIntent() != null) {
            code = getIntent().getStringExtra("code");

        }

    }

    private void initListener() {

        mEmptyLayout = new EmptyLayout(this, mlist.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mlist.setMode(PullToRefreshBase.Mode.DISABLED);
        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                BusinessSelectCustomerActivity.BussinessDetailAdapter.ViewHolder holder = (BussinessDetailAdapter.ViewHolder) view.getTag();
                cu_code = holder.code;
                cu_name = holder.tv_business_name.getText().toString();
                final boolean flag = !mAdapter.getMdata().get(position - 1).isChecked;
                Log.i(TAG, "onClick:" + position + " check:" + !flag);
                for (Business model : mAdapter.getMdata()) {
                    model.setIsChecked(false);
                }
                //把源数据清空
                for (Business model : mData) {
                    model.setIsChecked(false);
                }
                mAdapter.getMdata().get(position - 1).setIsChecked(flag);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                }, 190);

            }
        });
        search_edit.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mAdapter == null) {
                    ToastUtil.showToast(ct, R.string.networks_out);
                } else {
                    if (!TextUtils.isEmpty(search_edit.getText())) {
                        mAdapter.getFilter().filter(s);
                    } else {
                        mAdapter.getFilter().filter("");
                    }
                }
            }
        });
    }

    private void initData() {
        sendHttpResquest(Constants.HTTP_SUCCESS_INIT, code);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_submit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_save){
            if (canSave()) {
                LogUtil.prinlnLongMsg("Arison", code + "|" + cu_code + "|" + cu_name);
                sendHttpResquest(2, code, cu_code, cu_name);
            }
        }else if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    private boolean canSave() {
        if (StringUtil.isEmpty(code)) {
            ToastUtil.showToast(ct, R.string.error_system_findunknow_error);
            return false;
        } else if (StringUtil.isEmpty(cu_code) || StringUtil.isEmpty(cu_name)) {
            ToastUtil.showToast(ct, R.string.CRM_pleasePickClientFirst);
            return false;
        }
        return true;
    }

    public class BussinessDetailAdapter extends BaseAdapter implements Filterable {
        private Context ct;
        private ArrayList<Business> mdata = new ArrayList<>();
        private LayoutInflater inflater;
        private String searchkeys;

        public ArrayList<Business> getMdata() {
            return mdata;
        }

        public BussinessDetailAdapter(Context ct, ArrayList<Business> data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(mdata);
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
                convertView = inflater.inflate(R.layout.item_business_customer, null);
                holder = new ViewHolder();
                holder.cb_left = (SmoothCheckBox) convertView.findViewById(R.id.cb_left);
                holder.tv_business_name = (TextView) convertView.findViewById(R.id.tv_business_name);
                holder.tv_business_leader = (TextView) convertView.findViewById(R.id.tv_business_leader);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//           holder.cb_left.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
//                    cu_code= mdata.get(position).getCode();
//                    cu_name=mdata.get(position).getName();
//                    final  boolean flag = !mdata.get(position).isChecked;
//                    Log.i(TAG, "onClick:" + position + " check:" + !flag);
//                    for (Business model : mdata) {
//                        model.setIsChecked(false);
//                    }
//
//                    mdata.get(position-1).setIsChecked(flag);
//                    mAdapter.notifyDataSetChanged();
//                }
//
//            });
            holder.cb_left.setFocusable(false);
            holder.cb_left.setEnabled(false);
            holder.cb_left.setClickable(false);
            holder.code = mdata.get(position).getCode();
            holder.cb_left.setChecked(mdata.get(position).isChecked(), mdata.get(position).isChecked());

            if (!StringUtil.isEmpty(searchkeys)) {
                holder.tv_business_name.setText(mdata.get(position).getName());

                holder.tv_business_leader.setText("负责人:" + mdata.get(position).getLeader());
//                CommonUtil.textSpanForStyle(holder.tv_business_name, mdata.get(position).getName(),
//                        searchkeys, ct.getResources().getColor(R.color.yellow));
//                CommonUtil.textSpanForStyle(holder.tv_business_leader, "负责人:"+mdata.get(position).getLeader(),
//                        searchkeys, ct.getResources().getColor(R.color.yellow));
            } else {
                holder.tv_business_name.setText(mdata.get(position).getName());

                holder.tv_business_leader.setText("负责人:" + mdata.get(position).getLeader());
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    //过滤数据
                    FilterResults searchResults = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {

                        searchResults.values = mData;
                        searchResults.count = mData.size();
                    } else {
                        mdata = mData;
                        ArrayList<Business> newArry = new ArrayList<Business>();
                        for (int i = 0; i < mdata.size(); i++) {
                            Business model = mdata.get(i);
                            if (model != null && (!StringUtil.isEmpty(model.getName()) && model.getName().contains(constraint))
                                    || (!StringUtil.isEmpty(model.getLeader()) && model.getLeader().contains(constraint))) {
                                newArry.add(model);
                            }
                        }
                        //这个是draft版本代码
                        searchResults.values = newArry;
                        searchResults.count = newArry.size();
                    }
                    return searchResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //装配数据
                    mdata = (ArrayList<Business>) results.values;
                    searchkeys = constraint.toString();
                    if (mAdapter.getCount() == 0) {

                    }
                    notifyDataSetChanged();
                }
            };
        }


        class ViewHolder {
            SmoothCheckBox cb_left;
            TextView tv_business_name;
            TextView tv_business_leader;
            String code;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    JSONArray jsonArray = JSON.parseObject(msg.getData().getString("result")).getJSONArray("customers");
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            Business model = new Business();
                            model.setName(object.getString("CU_NAME"));
                            model.setCode(object.getString("CU_CODE"));
                            model.setLeader(object.getString("CU_CONTACT"));
                            mData.add(model);
                        }
                    }
                    mAdapter = new BussinessDetailAdapter(ct, mData);
                    mlist.setAdapter(mAdapter);
                    if (mAdapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();

                    }
                    break;
                case 2:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage("操作成功！");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            jumpToStateActivity();
                            finish();
                        }
                    }, 3000);

                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    //  ToastMessage(JSON.parseObject(msg.getData().getString("result")).getString("exceptionInfo"));
                    Crouton.makeText(BusinessSelectCustomerActivity.this, msg.getData().getString("result"), Style.ALERT).show();
                    break;
            }
        }
    };

    private void jumpToStateActivity() {
        Intent intent = new Intent();
        intent.setClass(BusinessSelectCustomerActivity.this, BusinessStateActivity.class);
        startActivity(intent);
    }

    private void sendHttpResquest(int what, String code) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getCustomerbySeller.action";
        Map<String, Object> params = new HashMap<>();
        params.put("sellercode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void sendHttpResquest(int what, String code, String cu_code, String cu_name) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateBusinessChanceCust.action";
        Map<String, Object> params = new HashMap<>();
        params.put("sellercode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("bc_code", code);
        params.put("cu_code", cu_code);
        params.put("cu_name", cu_name);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }
}
