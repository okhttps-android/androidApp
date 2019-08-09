package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.crouton.Crouton;
import com.core.widget.crouton.Style;
import com.core.widget.view.HorizontalStepsView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;
import com.uas.appworks.activity.businessManage.businessDetailActivity.BusinessDetailNewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @功能:商机列表
 * @author:Arisono
 * @param:
 * @return:
 */
public class BusinessDetailActivty extends BaseActivity {

    private final int LOAD_JIEDUAN = 0x210;
    private BussinessDetailAdapter mAdapter;
    private LinearLayout ll_top_state;
    private PullToRefreshListView mlist;
    private VoiceSearchView voiceSearchView;
    private ArrayList<Business> mData = new ArrayList<Business>();

    private String formCondition;
    private String gridCondition;
    private int page = 1;
    private String bc_code;
    private EmptyLayout mEmptyLayout;
    private final int CAN_QIANG_NOT = 34;
    private String[] labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_qiang_activty);
        initView();
        initListener();
        LogUtil.d("onCreate()");
        initData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume()");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d("onDestroy()");
    }

    private void initView() {
        ll_top_state = (LinearLayout) findViewById(R.id.ll_top_state);
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);

        mEmptyLayout = new EmptyLayout(this, mlist.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
    }

    private void initListener() {
        mlist.setMode(PullToRefreshBase.Mode.BOTH);
        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
                //清除查询条件
                voiceSearchView.setText("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, ++page);
                //清除查询条件
                voiceSearchView.setText("");
            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               /* final BussinessDetailAdapter.OldViewHolder holder = (BussinessDetailAdapter.OldViewHolder) view.getTag();
                startActivity(new Intent(BusinessDetailActivty.this, BusinessDetailInfoActivity.class)
                        .putExtra("type", holder.type)
                        .putExtra("formCondition", formCondition + "=" + holder.bc_id)
                        .putExtra("gridCondition", gridCondition + "=" + holder.bc_id)
                        .putExtra("id", holder.bc_id)
                );*/

                try {
                    int itemAtPosition = (int) parent.getItemIdAtPosition(position);
                    Business business = mData.get(itemAtPosition);
                    startActivity(new Intent(mContext, BusinessDetailNewActivity.class)
                            .putExtra("id", business.getBc_id())
                            .putExtra("type", mBusinessType)
                            .putExtra("bc_code", business.getCode())
                            .putExtra("bc_description", business.getName())
                            .putExtra(Constants.FLAG.COMMON_WHICH_PAGE, "businessCompany"));
                } catch (Exception e) {

                }
            }
        });

        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mAdapter == null) {
                    Toast.makeText(getApplication(), getString(R.string.networks_out), Toast.LENGTH_SHORT).show();
                } else {
                    if (!StringUtil.isEmpty(voiceSearchView.getText().toString())) {
                        mAdapter.getFilter().filter(voiceSearchView.getText().toString().replaceAll(" ", ""));
                    } else {
                        mAdapter.getFilter().filter("");
                    }
                }
                mlist.setAdapter(mAdapter);
            }
        });

    }

    int type;
    String business_steps;
    String currentdate;
    String mBusinessType = "项目商机";

    private void initData() {
        Intent intent = getIntent();
        loadJieDuan();
        type = 0;
        if (intent != null) {
            type = intent.getIntExtra("bt_type", 0);
            business_steps = intent.getStringExtra("steps");
            currentdate = intent.getStringExtra("currentdate");
            mBusinessType = intent.getStringExtra("businessType");
        }
        if (type == 0) {
            setTitle(getString(R.string.business_followup));
            ll_top_state.setVisibility(View.VISIBLE);
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        } else if (type == 1) {
            setTitle(getString(R.string.business_seize));
            ll_top_state.setVisibility(View.GONE);
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        } else if (type == 2) {
            setTitle(getString(R.string.business_distribution));
            ll_top_state.setVisibility(View.GONE);
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        } else if (type == 3) {
            setTitle(business_steps);
            ll_top_state.setVisibility(View.GONE);
            isAdminer(12);
        } else {
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page);
        }

    }

    private static final String TAG = "BusinessDetailActivty";


    private class BussinessDetailAdapter extends BaseAdapter implements Filterable {
        private Context ct;
        private ArrayList<Business> mdata = new ArrayList<>();
        private LayoutInflater inflater;

        public BussinessDetailAdapter(Context ct, ArrayList<Business> data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        public void setMdata(ArrayList<Business> mdata) {
            this.mdata = mdata;
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
            return getOldView(position, convertView, parent);
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
                //  holder.tv_num= (TextView) convertView.findViewById(R.id.tv_crm_business_num);
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
            //holder.tv_leader.setText(mdata.get(position).getLeader());
            holder.tv_note.setText(mdata.get(position).getNote());
            holder.tv_phone.setText(mdata.get(position).getPhone());
            holder.tv_steps.setText(mdata.get(position).getSteps());
            holder.tv_source.setText(mdata.get(position).getSource());
            if (mdata.get(position).getType() == 0) {
                holder.iv_event.setVisibility(View.GONE);
            } else if (mdata.get(position).getType() == 1) {
                holder.iv_event.setVisibility(View.VISIBLE);
                holder.iv_event.setImageResource(R.drawable.ic_qiang);
            } else if (mdata.get(position).getType() == 2) {
                // holder.bt_event.setText("分配");
                holder.iv_event.setVisibility(View.VISIBLE);
                holder.iv_event.setImageResource(R.drawable.ic_fenpei);
            } else {
                holder.iv_event.setVisibility(View.GONE);

            }
            // holder.tv_date.setText(mdata.get(position).getDate());
            holder.iv_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ListUtils.getSize(mdata) > position) {
                        if (mdata.get(position).getType() == 1) {
                            bc_code = mdata.get(position).getNum();
                            progressDialog.show();
                            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/ifoverrecv.action";
                            Map<String, Object> stringMap = new HashMap<String, Object>();
                            stringMap.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_emname"));
                            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                            ViewUtil.httpSendRequest(ct, url, stringMap, mHandler, headers, CAN_QIANG_NOT, null, null, "post");
                        } else if (mdata.get(position).getType() == 2) {
                            bc_code = mdata.get(position).getNum();
                            startActivityForResult(new Intent(ct, DbfindList2Activity.class), 1);
                        }
                    }
                }
            });
            return convertView;
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
            if (StringUtil.isEmpty(currentprocess))
                currentprocess = business_steps;
            if (labels == null || labels.length <= 0 || StringUtil.isEmpty(currentprocess)) {
                return 0;
            }
            for (int i = 0; i < labels.length; i++) {
                if (!StringUtil.isEmpty(labels[i]) && labels[i].equals(currentprocess))
                    return i;
            }
            return 0;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    return performFilteringSynch(constraint);
                }

                public synchronized FilterResults performFilteringSynch(CharSequence constraint) {
                    FilterResults searchResults = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        searchResults.values = mData;
                        searchResults.count = mData.size();
                    } else {
                        ArrayList<Business> businessList = mData;
                        ArrayList<Business> newBusiness = new ArrayList<>();
                        for (Business business : businessList) {
                            String num = business.getNum() == null ? "" : business.getNum();
                            String name = business.getName() == null ? "" : business.getName();
                            String source = business.getSource() == null ? "" : business.getSource();
                            String links = business.getPhone() == null ? "" : business.getPhone();
                            String remark = business.getNote() == null ? "" : business.getNote();
                            String date = business.getDate() == null ? "" : business.getDate();
                            if (num.contains(constraint) || name.contains(constraint) || source.contains(constraint)
                                    || links.contains(constraint) || remark.contains(constraint) || date.contains(constraint)) {
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
                    //装配数据
                    mdata = (ArrayList<Business>) results.values;
                    notifyDataSetChanged();
                }
            };
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


    }


    private int isAdmin;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_JIEDUAN:
                    try {
                        JSONArray array = JSON.parseObject(msg.getData().getString("result")).getJSONArray("stages");
                        labels = new String[array.size()];
                        if (!ListUtils.isEmpty(array)) {
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
                    int page = msg.getData().getInt("page", BusinessDetailActivty.this.page);
                    LogUtil.i("page=" + page);
                    formCondition = JSON.parseObject(msg.getData().getString("result")).getString("keyField");
                    gridCondition = JSON.parseObject(msg.getData().getString("result")).getString("pfField");
                    if (StringUtil.isEmpty(formCondition)) formCondition = "bc_id";
                    if (StringUtil.isEmpty(gridCondition)) gridCondition = "bcd_bcid";
                    JSONArray array = JSON.parseObject(msg.getData().getString("result")).getJSONArray("listdata");
                    if (page == 1 && ListUtils.getSize(mData) > 0)
                        mData.clear();
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            Business model = new Business();
                            model.setNum(array.getJSONObject(i).getString("bc_code"));
                            model.setDate(array.getJSONObject(i).getString("bc_recorddate"));
                            model.setName(array.getJSONObject(i).getString("bc_description"));
                            model.setPhone(array.getJSONObject(i).getString("bc_tel"));
                            model.setNote(array.getJSONObject(i).getString("bc_remark"));
                            model.setSource(array.getJSONObject(i).getString("bc_from"));
                            model.setSteps(array.getJSONObject(i).getString("bc_nichehouse"));
                            model.setBc_id(JSONUtil.getInt(array.getJSONObject(i), "bc_id"));
                            model.setType(type);
                            mData.add(model);
                        }
                    } else {
                        mEmptyLayout.showEmpty();
                        if (page == 1) {
                            showToast(R.string.crm_nodatas);
                        } else {
                            showToast(R.string.common_up_finish);
                        }

                    }
                    if (mAdapter == null) {
                        mAdapter = new BussinessDetailAdapter(ct, mData);
                        mlist.setAdapter(mAdapter);
                    } else {
                        mAdapter.setMdata(mData);
                        mAdapter.notifyDataSetChanged();
                    }
//                    if (page != 1) {
//                        mlist.getRefreshableView().setSelection(mAdapter.getCount());
//                    }
                    mlist.onRefreshComplete();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    Crouton.cancelAllCroutons();
                    final String m = msg.getData().getString("result");
                    LogUtil.d(TAG, "handleMessage:" + msg.getData().getString("result"));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ViewUtil.ToastMessage(mContext, m,
                                    Style.holoRedLight,
                                    3000);
                        }
                    }, 500);
                    break;
                case BUSINESS_QIANG:
                    LogUtil.d(TAG, "handleMessage:" + msg.getData().getString("result"));
                    progressDialog.dismiss();
                    showToast(getString(R.string.qiang_business_success) + ","
                            + getString(R.string.business_notice1));

                    sendHttpResquest(Constants.HTTP_SUCCESS_INIT, page = 1);
                    break;
                case BUSINESS_FENPEI:
                    progressDialog.dismiss();
                    showToast(R.string.split_business_success);
                    initData();
                    break;
                case CAN_QIANG_NOT:
                    JSONObject result = JSON.parseObject(msg.getData().getString("result"));
                    String isok = result.getString("isok");
                    if (!JSONUtil.validate(msg.getData().getString("result"))) {
                        showToast(msg.getData().getString("result"));
                    }
                    if ("0".equals(isok)) {
                        Log.i("qiang", isok);
                        sendHttpBusinessQiang(BUSINESS_QIANG, bc_code, CommonUtil.getSharedPreferences(ct, "erp_emname"),
                                CommonUtil.getSharedPreferences(ct, "erp_username"));
                    } else if ("1".equals(isok)) {
                        Log.i("qiang", isok);
                        progressDialog.dismiss();
                        showToast(R.string.business_limit);
                    }
                case 12://admin ?
                    result = JSON.parseObject(msg.getData().getString("result"));
                    String admin = result.getString("result");
                    if (!StringUtil.isEmpty(admin)) {
                        if ("admin".equals(admin)) {
                            isAdmin = 1;
                        } else {
                            isAdmin = 0;
                        }
                    } else {
                        isAdmin = 0;
                    }
                    sendHttpResquest(Constants.HTTP_SUCCESS_INIT, 1);
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
            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/list.action";
            Map<String, Object> params = new HashMap<>();
            if (page == 1) mData.clear();
            params.put("page", page);
            params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            params.put("currentMaster", CommonUtil.getSharedPreferences(ct, "erp_master"));
            params.put("pageSize", 10);
            params.put("caller", "BusinessChance");
            if (type == 1) {//抢
                params.put("condition", "nvl(bc_status,' ')<>'已失效' and nvl(bc_doman,' ')=' ' and  bc_nichehouse in (select  bd_name from BusinessDataBase where bd_prop='可领取可分配')");
            } else if (type == 2) {//分配
                params.put("condition", "nvl(bc_status,' ')<>'已失效' and nvl(bc_doman,' ')=' ' and bc_nichehouse in (select bd_name from BusinessDataBase where bd_admincode=" +
                        "'" + CommonUtil.getSharedPreferences(ct, "erp_username") + "' and bd_prop='管理员分配') ");
            } else if (type == 3) {//商机列表
                url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getBusinessChancebyMonthAndProcess.action";
                params.clear();
                params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
                params.put("currentprocess", business_steps);
                params.put("currentdate", currentdate);
                params.put("page", page);
                params.put("pageSize", "10");
                params.put("type", mBusinessType);
            } else {//跟进
                params.put("condition", "(bc_type='公有' or nvl(bc_type,' ')=' ')");
            }
            //bc_currentprocess=商机阶段
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putInt("page", page);
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, message, bundle, "post");
        } else {
            progressDialog.dismiss();
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }
    }

    private final int BUSINESS_FENPEI = 3;
    private final int BUSINESS_QIANG = 2;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                String bt_doman = data.getStringExtra("en_name");
                String en_code = data.getStringExtra("en_code");
                sendHttpBusinessQiang(BUSINESS_FENPEI, bc_code, bt_doman, en_code);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void sendHttpBusinessQiang(int what, String bc_code, String bc_doman, String en_code) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateBusinessChanceDoman.action";
        Map<String, Object> params = new HashMap<>();
        params.put("bc_code", bc_code);//商机编号
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        if (what == BUSINESS_FENPEI) {
            params.put("type", 1);
        } else {
            params.put("type", 0);
        }
        params.put("bc_doman", bc_doman);//商机跟进人
        params.put("bc_domancode", en_code);//商机跟进人编号
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    public void isAdminer(int what) {
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/isSysAdmin.action";
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


}
