package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.HorizontalStepsView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 商机跟进页面
 */
public class BusinessStateActivity extends BaseActivity implements View.OnClickListener {

    private final int LOAD_JIEDUAN = 0x451;
    private BussinessDetailAdapter mAdapter;
    private LinearLayout ll_top_state;
    private PullToRefreshListView mlist;
    private ArrayList<Business> mData = new ArrayList<Business>();
    private VoiceSearchView voiceSearchView;
    private String formCondition;
    private String gridCondition;
    private int page = 1;
    private TextView tv_unmanger;
    private TextView tv_manged;
    private TextView tv_timeout;
    private TextView tv_transtered;
    private EmptyLayout mEmptyLayout;
    private int business_state = 0;//商机状态
    private String[] labels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_qiang_activty);
        initView();
        initListener();
    }

    private void initView() {
        ll_top_state = (LinearLayout) findViewById(R.id.ll_top_state);
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        tv_unmanger = (TextView) findViewById(R.id.tv_unmanger);
        tv_timeout = (TextView) findViewById(R.id.tv_timeout);
        tv_transtered = (TextView) findViewById(R.id.tv_transtered);
        tv_manged = (TextView) findViewById(R.id.tv_manged);
        updateTabSelectState(tv_unmanger);
        mEmptyLayout = new EmptyLayout(this, mlist.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void updateTabSelectState(TextView view) {
        if (view.getId() == R.id.tv_unmanger) {
            tv_unmanger.setSelected(true);
            tv_manged.setSelected(false);
            tv_timeout.setSelected(false);
            tv_transtered.setSelected(false);
        } else if (view.getId() == R.id.tv_manged) {
            tv_unmanger.setSelected(false);
            tv_manged.setSelected(true);
            tv_timeout.setSelected(false);
            tv_transtered.setSelected(false);
        } else if (view.getId() == R.id.tv_timeout) {
            tv_unmanger.setSelected(false);
            tv_manged.setSelected(false);
            tv_timeout.setSelected(true);
            tv_transtered.setSelected(false);
        } else if (view.getId() == R.id.tv_transtered) {
            tv_unmanger.setSelected(false);
            tv_manged.setSelected(false);
            tv_timeout.setSelected(false);
            tv_transtered.setSelected(true);
        }
    }

    private void initListener() {
        tv_unmanger.setOnClickListener(this);
        tv_manged.setOnClickListener(this);
        tv_timeout.setOnClickListener(this);
        tv_transtered.setOnClickListener(this);
        mlist.setMode(PullToRefreshBase.Mode.BOTH);
        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
                //search_edit.setText("");
                // search_edit.setFocusable(false);
                //search_edit.setText("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, ++page);
                // search_edit.setFocusable(false);
                // search_edit.setText("");
                //search_edit.setText("");
            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BussinessDetailAdapter.ViewHolder holder = (BussinessDetailAdapter.ViewHolder) view.getTag();
                startActivity(new Intent(ct, BusinessDetailInfoActivity.class)
                        .putExtra("type", holder.type)
                        .putExtra("formCondition", formCondition + "=" + holder.bc_id)
                        .putExtra("gridCondition", gridCondition + "=" + holder.bc_id)
                        .putExtra("id", holder.bc_id)
                );
            }
        });

        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mAdapter == null) {
                    Toast.makeText(getApplication(), getString(R.string.networks_out), Toast.LENGTH_SHORT).show();
                } else {
                    if (!StringUtil.isEmpty(s.toString())) {
                        mAdapter.getFilter().filter(s.toString().trim());
                    } else {
                        mAdapter.getFilter().filter("");
                    }
                }
            }
        });

    }

    int type;

    private void initData() {
        loadJieDuan();
        Intent intent = getIntent();
        type = 0;
        if (intent != null) {
            type = intent.getIntExtra("bt_type", 0);
        }
        if (type == 0) {
      setTitle(getString(R.string.business_followup));
            ll_top_state.setVisibility(View.VISIBLE);
        } else if (type == 1) {
          setTitle(getString(R.string.business_seize));
            ll_top_state.setVisibility(View.GONE);
        } else if (type == 2) {
          setTitle(getString(R.string.business_distribution));
            ll_top_state.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_unmanger) {
            updateTabSelectState((TextView) v.findViewById(R.id.tv_unmanger));
            business_state = 0;
            page = 1;
            //  mAdapter=null;
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        } else if (v.getId() == R.id.tv_manged) {
            updateTabSelectState((TextView) v.findViewById(R.id.tv_manged));
            business_state = 1;
            page = 1;
            // mAdapter=null;
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        } else if (v.getId() == R.id.tv_timeout) {
            updateTabSelectState((TextView) v.findViewById(R.id.tv_timeout));
            business_state = 2;
            page = 1;
            // mAdapter=null;
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        } else if (v.getId() == R.id.tv_transtered) {
            updateTabSelectState((TextView) v.findViewById(R.id.tv_transtered));
            business_state = 3;
            page = 1;
            // mAdapter=null;
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        }
    }


    private class BussinessDetailAdapter extends BaseAdapter implements Filterable {
        private Context ct;
        private ArrayList<Business> mdata;
        private LayoutInflater inflater;
        private MyFilter myFilter;
        private ArrayList<Business> mOrignalValues;
        private final Object mLock = new Object();

        public BussinessDetailAdapter(Context ct, ArrayList<Business> data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            if (mdata == null) {
                return 0;
            } else {
                return mdata.size();
            }
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
            return getNewView(position, convertView, parent);
        }

        private View getNewView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_business, parent, false);
                holder = new ViewHolder();
                holder.companyname_tv = (TextView) convertView.findViewById(R.id.companyname_tv);
                holder.describe_tv = (TextView) convertView.findViewById(R.id.describe_tv);
                holder.create_date_tv = (TextView) convertView.findViewById(R.id.create_date_tv);
                holder.stepsView = (HorizontalStepsView) convertView.findViewById(R.id.stepsView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bc_id = mdata.get(position).getBc_id();
            holder.type = mdata.get(position).getType();
            holder.create_date_tv.setText(mdata.get(position).getDate());
            holder.companyname_tv.setText(mdata.get(position).getName());
            holder.describe_tv.setText(mdata.get(position).getNote());
            holder.stepsView.setProgress(getPosition(mdata.get(position).getCurrentprocess()), labels);
//            holder.stepsView.setProgress(4, labels);
            holder.stepsView.setFocusable(false);
            holder.stepsView.setClickable(false);
            return convertView;
        }

        private int getPosition(String currentprocess) {
            if (labels == null || labels.length <= 0 || StringUtil.isEmpty(currentprocess))
                return 0;
            for (int i = 0; i < labels.length; i++)
                if (!StringUtil.isEmpty(labels[i]) && labels[i].equals(currentprocess))
                    return i;
            return 0;
        }

        private View getOldView(final int position, View convertView, ViewGroup parent) {
            OldViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_business_detail, null);
                holder = new OldViewHolder();
                holder.tv_num = (TextView) convertView.findViewById(R.id.tv_crm_business_num);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_crm_business_name);
                holder.tv_note = (TextView) convertView.findViewById(R.id.tv_crm_business_note);
                holder.tv_date = (TextView) convertView.findViewById(R.id.tv_crm_business_date);
                holder.tv_datetv = (TextView) convertView.findViewById(R.id.tv_crm_business_datetv);
                holder.tv_phone = (TextView) convertView.findViewById(R.id.tv_crm_business_phone);
                holder.tv_source = (TextView) convertView.findViewById(R.id.tv_crm_business_source);
                holder.tv_steps = (TextView) convertView.findViewById(R.id.tv_crm_business_steps);
                holder.iv_event = (ImageView) convertView.findViewById(R.id.iv_business_qiang);
                convertView.setTag(holder);
            } else {
                holder = (OldViewHolder) convertView.getTag();
            }
            holder.bc_id = mdata.get(position).getBc_id();
            holder.type = mdata.get(position).getType();
            holder.tv_num.setText(mdata.get(position).getNum());
            holder.tv_name.setText(mdata.get(position).getName());
            holder.tv_date.setText(mdata.get(position).getDate());
            holder.tv_datetv.setText(getString(R.string.creat_time));
            holder.tv_steps.setText(mdata.get(position).getSteps());
            holder.tv_note.setText(mdata.get(position).getNote());
            holder.tv_phone.setText(mdata.get(position).getPhone());
            holder.tv_source.setText(mdata.get(position).getSource());
            if (mdata.get(position).getType() == 0) {
                holder.iv_event.setVisibility(View.GONE);
            } else if (mdata.get(position).getType() == 1) {
                holder.iv_event.setVisibility(View.VISIBLE);
                holder.iv_event.setImageResource(R.drawable.ic_qiang);
            } else if (mdata.get(position).getType() == 2) {
                holder.iv_event.setVisibility(View.VISIBLE);
                holder.iv_event.setImageResource(R.drawable.ic_fenpei);
            }
            holder.iv_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mdata.get(position).getType() == 1) {
                        startActivity(new Intent(ct, BusinessDetailInfoActivity.class).putExtra("id", mdata.get(position).getBc_id()));
                    } else if (mdata.get(position).getType() == 2) {
                        startActivityForResult(new Intent(ct, DbfindListActivity.class), 1);
                    }
                }
            });
            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (myFilter == null) {
                myFilter = new MyFilter();
            }
            return myFilter;
        }

        class ViewHolder {
            int bc_id;
            int type;
            TextView companyname_tv;
            TextView describe_tv;
            TextView create_date_tv;
            HorizontalStepsView stepsView;
        }

        class OldViewHolder {
            int bc_id;
            TextView tv_num;
            TextView tv_name;
            TextView tv_steps;
            TextView tv_source;
            TextView tv_phone;
            TextView tv_note;
            TextView tv_datetv;
            TextView tv_date;
            //            Button bt_event;
            ImageView iv_event;
            int type;
        }


        class MyFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //过滤数据
                FilterResults searchResults = new FilterResults();
                if (mOrignalValues == null) {
                    synchronized (mLock) {
                        mOrignalValues = new ArrayList<Business>(mdata);
                    }
                }
                if (constraint == null || constraint.length() == 0) {
                    synchronized (mLock) {
                        ArrayList<Business> tempList = new ArrayList<>(mOrignalValues);
                        searchResults.values = tempList;
                        searchResults.count = tempList.size();

                    }
                } else {
                    final ArrayList<Business> values = mOrignalValues;
                    final int count = values.size();

                    final ArrayList<Business> newBusiness = new ArrayList<>();
                    String input = constraint.toString();
                    for (Business business : values) {
                        String num = business.getNum();
                        String name = business.getName();
                        String source = business.getSource();
                        String links = business.getPhone();
                        String remark = business.getNote();
                        String date = business.getDate();
                        if ((num != null && num.contains(input)) || (name != null && name.contains(input))
                                || (source != null && source.contains(input))
                                || (links != null && links.contains(input))
                                || (remark != null && remark.contains(input))
                                || (date != null && date.contains(input))) {
                            newBusiness.add(business);
                        }
                    }
                    searchResults.values = newBusiness;
                    searchResults.count = newBusiness.size();
                }
                return searchResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mdata = (ArrayList<Business>) results.values;
                mData = mdata;
//                if (mAdapter.getCount() > 0) {
                notifyDataSetChanged();
//                } else {
//                    notifyDataSetInvalidated();
//                }
            }
        }


    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            if (!JSONUtil.validate(message)) return;
            JSONObject object = JSON.parseObject(message);
            switch (msg.what) {
                case LOAD_JIEDUAN:
                    try {
                        JSONArray array = object.getJSONArray("stages");
                        labels = new String[array.size()];
                        if (!array.isEmpty()) {
                            for (int i = 0; i < array.size(); i++) {
                                labels[i] = array.getJSONObject(i).getString("BS_NAME");
                            }
                        }
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        ToastMessage("扔出异常！");
                    }
                    sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
                    break;
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    formCondition = "";
                    gridCondition = "bcd_bcid";
                    JSONArray arraya = object.getJSONArray("lista");
                    JSONArray arrayb = object.getJSONArray("listb");
                    JSONArray arrayc = object.getJSONArray("listc");
                    JSONArray arrayd = object.getJSONArray("listd");
//                    int counta = ListUtils.getSize(arraya);
//                    int countb = ListUtils.getSize(arrayb);
//                    int countc = ListUtils.getSize(arrayc);
//                    int countd = ListUtils.getSize(arrayd);
                    int counta = JSONUtil.getInt(object, "counta");
                    int countb = JSONUtil.getInt(object, "countb");
                    int countc = JSONUtil.getInt(object, "countc");
                    int countd = ListUtils.getSize(arrayd);
//                    if (page > 1) {
//                        counta += StringUtil.getFirstInt(StringUtil.getTextRexHttp(tv_unmanger), 0);
//                        countb += StringUtil.getFirstInt(StringUtil.getTextRexHttp(tv_manged), 0);
//                        countc += StringUtil.getFirstInt(StringUtil.getTextRexHttp(tv_timeout), 0);
//                        countd += StringUtil.getFirstInt(StringUtil.getTextRexHttp(tv_transtered), 0);
//                    }
                    CommonUtil.textSpanForStyle(tv_unmanger, counta + getString(R.string.Unscheduled_schedule), String.valueOf(counta), ct.getResources().getColor(R.color.yellow_home));
                    CommonUtil.textSpanForStyle(tv_manged, countb + getString(R.string.Has_been_scheduled), String.valueOf(countb), ct.getResources().getColor(R.color.yellow_home));
                    CommonUtil.textSpanForStyle(tv_timeout, countc + getString(R.string.Will_time_out), String.valueOf(countc), ct.getResources().getColor(R.color.yellow_home));
                    CommonUtil.textSpanForStyle(tv_transtered, countd + getString(R.string.Has_switched_customers), String.valueOf(countd), ct.getResources().getColor(R.color.yellow_home));
                    JSONArray array = new JSONArray();
                    switch (business_state) {
                        case 0:
                            array = arraya;
                            Log.i(TAG, "handleMessage:" + 0);
                            break;
                        case 1:
                            array = arrayb;
                            Log.i(TAG, "handleMessage:" + 1);
                            break;
                        case 2:
                            array = arrayc;
                            Log.i(TAG, "handleMessage:" + 2);
                            break;
                        case 3:
                            array = arrayd;
                            Log.i(TAG, "handleMessage:" + 3);
                            break;
                    }
                    if (!array.isEmpty()) {
                        ArrayList<Business> temps = new ArrayList<>();
                        for (int i = 0; i < array.size(); i++) {
                            Business model = new Business();
                            model.setNum(array.getJSONObject(i).getString("BC_CODE"));
                            model.setDate(array.getJSONObject(i).getString("BC_RECORDDATE"));
                            model.setName(array.getJSONObject(i).getString("BC_CUSTNAME"));
                            model.setPhone(array.getJSONObject(i).getString("BC_TEL"));
                            model.setNote(array.getJSONObject(i).getString("BC_REMARK"));
                            model.setSteps(array.getJSONObject(i).getString("BC_NICHEHOUSE"));
                            model.setSource(array.getJSONObject(i).getString("BC_FROM"));
                            model.setCurrentprocess(JSONUtil.getText(array.getJSONObject(i), "BC_CURRENTPROCESS"));
                            if (array.getJSONObject(i).getObject("BC_ID", Object.class) instanceof Integer) {
                                model.setBc_id(array.getJSONObject(i).getInteger("BC_ID"));
                            }
                            model.setType(type);
                            temps.add(model);
                        }
                        Log.i(TAG, "数据：" + JSON.toJSONString(temps));
                        mData.addAll(temps);
                        if (mAdapter == null) {
                            mAdapter = new BussinessDetailAdapter(ct, mData);
                            mlist.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }

                    } else {
                        if (page == 1) {
                            if (mAdapter != null) mAdapter.notifyDataSetChanged();
                            mEmptyLayout.showEmpty();
                        } else {
                            ViewUtil.ToastMessage(activity, getString(R.string.common_up_finish));
                        }
                    }
                    mlist.onRefreshComplete();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    mlist.onRefreshComplete();
                    break;
            }
        }
    };

    /**
     * @desc:加载阶段
     * @author：Arison on 2016/7/20
     */
    public void loadJieDuan() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceStage.action";
        Map<String, Object> params = new HashMap<>();
        params.put("condition", "1=1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_JIEDUAN, null, null, "post");
    }

    private void sendHttpResquest(int what, int page) {
        progressDialog.show();
        if (CommonUtil.isNetWorkConnected(this)) {
            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getNicheByCondition.action";
            Map<String, Object> params = new HashMap<>();
            if (page == 1) mData.clear();
            params.put("bc_domancode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            params.put("pageIndex", page);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
        } else {
            progressDialog.dismiss();
            mlist.onRefreshComplete();
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                String en_name = data.getStringExtra("en_name");
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
