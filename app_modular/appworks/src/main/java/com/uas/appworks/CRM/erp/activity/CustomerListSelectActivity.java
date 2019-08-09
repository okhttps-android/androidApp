package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerListSelectActivity extends BaseActivity   {

    private PullToRefreshListView mlist;
    private VoiceSearchView voiceSearchView;
    private ArrayList<Business> mData = new ArrayList<Business>();
    private Context ct;
    private BussinessDetailAdapter mAdapter;
    private int page=1;
    private EmptyLayout mEmptyLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list_select);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        ct = this;
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
      setTitle(getString(R.string.client_search));
        mEmptyLayout = new EmptyLayout(this,mlist.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
    }

    private void initListener() {
        mlist.setMode(PullToRefreshBase.Mode.BOTH);
        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, 0, page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, 0, ++page);
            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BussinessDetailAdapter.ViewHolder viewHolder = (BussinessDetailAdapter.ViewHolder) view.getTag();
                if (viewHolder.tv_auditstatus.getText().toString().equals("已审核")) {
                    startActivity(new Intent(ct, CustomerDetailActivity.class).putExtra("code", viewHolder.tv_code
                            .getText().toString()));
                } else {
                    startActivity(new Intent(ct, CustomerAddActivity.class)
                                    .putExtra("type", 1)
                                    .putExtra("status", viewHolder.tv_auditstatus.getText().toString()).
                                            putExtra("code", viewHolder.tv_code
                                                    .getText().toString())
                    );
                }
            }
        });
        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (!StringUtil.isEmpty(search_edit.getText().toString())) {
//                    mAdapter.getFilter().filter(search_edit.getText().toString());
//                } else {
//                    mAdapter.getFilter().filter("");
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mAdapter == null) {
                    Toast.makeText(getApplication(), "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                } else {
                    if (!StringUtil.isEmpty(voiceSearchView.getText().toString())) {
                        mAdapter.getFilter().filter(voiceSearchView.getText().toString());
                    } else {
                        mAdapter.getFilter().filter("");
                    }
                }
                mlist.setAdapter(mAdapter);
            }
        });

    }

    private void initData() {
        sendHttpResquest(Constants.HTTP_SUCCESS_INIT,0,page);

    }

    private class BussinessDetailAdapter extends BaseAdapter  implements Filterable{
        private Context ct;
        private JSONArray mdata = new JSONArray();
        private JSONArray store=new JSONArray();
        private LayoutInflater inflater;

        public BussinessDetailAdapter(Context ct, JSONArray  data) {
            this.ct = ct;
            this.mdata = data;
            this.store=data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mdata==null?0:mdata.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return mdata.getJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_customer_main, null);
                holder = new ViewHolder();
                holder.tv_title= (TextView) convertView.findViewById(R.id.tv_cu_title);
                holder.tv_state = (TextView) convertView.findViewById(R.id.tv_cu_state);
                holder.tv_money = (TextView) convertView.findViewById(R.id.tv_cu_money);
                holder.tv_leader = (TextView) convertView.findViewById(R.id.tv_cu_leader);
                holder.tv_date = (TextView) convertView.findViewById(R.id.tv_crm_business_date);
                holder.tv_datetv = (TextView) convertView.findViewById(R.id.tv_crm_business_datetv);
                holder.tv_step = (TextView) convertView.findViewById(R.id.tv_cu_step);
                holder.tv_code=(TextView)convertView.findViewById(R.id.tv_cu_code);
                holder.tv_auditstatus= (TextView) convertView.findViewById(R.id.tv_cu_auditstatus);
                holder.tv_state.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_title.setText(mdata.getJSONObject(position).getString("cu_name"));
            holder.tv_money.setText(mdata.getJSONObject(position).getString("count"));
            holder.tv_step.setText(mdata.getJSONObject(position).getString("cu_nichestep"));
            holder.tv_date.setText(mdata.getJSONObject(position).getString("cu_lastdate"));
            holder.tv_datetv.setText("最后跟进时间：");
            holder.tv_auditstatus.setText(mdata.getJSONObject(position).getString("cu_auditstatus"));
            holder.tv_leader.setText(mdata.getJSONObject(position).getString("cu_sellername"));
            holder.tv_code.setText(mdata.getJSONObject(position).getString("cu_code"));
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    //过滤数据
                    FilterResults searchResults = new FilterResults();

                    if (constraint == null || constraint.length() == 0){
                        searchResults.values=store;
                        searchResults.count=store.size();
                    }else{
                        JSONArray newArry=new JSONArray();
                       for (int i=0;i<mdata.size();i++){
                           JSONObject jsonObject=mdata.getJSONObject(i);
                           Log.i("Arison", "performFiltering:" + jsonObject.toString() );
                           String cu_name=jsonObject.getString("cu_name")==null?"":jsonObject.getString("cu_name");
                           String count=jsonObject.getString("count")==null?"":jsonObject.getString("count");
                           String cu_nichestep=jsonObject.getString("cu_nichestep")==null?"":jsonObject.getString("cu_nichestep");
                           String cu_lastdate=jsonObject.getString("cu_lastdate")==null?"":jsonObject.getString("cu_lastdate");
                           String cu_contact=jsonObject.getString("cu_contact")==null?"":jsonObject.getString("cu_contact");
                           String cu_code=jsonObject.getString("cu_code")==null?"":jsonObject.getString("cu_code");
                           
                           if (cu_name.contains(constraint)
                                   ||count.contains(constraint)
                                   ||cu_nichestep.contains(constraint)
                                   ||cu_lastdate.contains(constraint)
                                   ||cu_contact.contains(constraint)
                                   ||cu_code.contains(constraint)){
                               newArry.add(jsonObject);
                           }
                       }

                        searchResults.values=newArry;
                        searchResults.count=newArry.size();
                    }
                    return searchResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //装配数据
                    mdata= (JSONArray) results.values;
                    if (mAdapter.getCount() == 0) {

                    }
                    notifyDataSetChanged();
                }
            };
            
        }


        class ViewHolder {
            TextView tv_auditstatus;
            TextView tv_code;
            TextView tv_state;
            TextView tv_title;
            TextView tv_money;
            TextView tv_step;
            TextView tv_leader;
            TextView tv_datetv;
            TextView tv_date;
        }
    }
    JSONArray jsonArray=new JSONArray();

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    ToastMessage("数据加载完成!");
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    JSONArray json= JSON.parseObject(msg.getData().getString("result")).getJSONArray("customers");
                    if (json!=null){
                        if (json.size() == 0){
                            mEmptyLayout.showEmpty();
                        }else {
                            if (page==1)jsonArray.clear();
                            jsonArray.addAll(json);
                            mAdapter = new BussinessDetailAdapter(ct, jsonArray);
                            mlist.setAdapter(mAdapter);
                        }
                    }
                    mlist.onRefreshComplete();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    break;
            }
        }
    };

    private void sendHttpResquest(int what,int kind,int page){
        progressDialog.show();
        String url= CommonUtil.getAppBaseUrl(ct)+"/mobile/crm/getCustomerDetail.action";
        Map<String,Object> params=new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("page", page);
        params.put("pageSize", "10");
        params.put("type", "2");
        params.put("isSelected",0);
        params.put("emplist","");
        params.put("kind",kind);
        LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }
}
