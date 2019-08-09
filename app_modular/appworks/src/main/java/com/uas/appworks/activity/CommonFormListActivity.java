package com.uas.appworks.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.net.http.HttpClient;
import com.core.utils.CommonUtil;
import com.core.utils.FlexJsonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.OA.erp.activity.ExpenseReimbursementActivity;
import com.uas.appworks.OA.erp.activity.form.DataFormDetailActivity;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @desc:动态表单列表界面
 * @author：Arison on 2016/11/29
 */
public class CommonFormListActivity extends BaseActivity implements View.OnClickListener {

    public static boolean isdelete = false;//删除标志位
    private PullToRefreshListView mlistview;
    private VoiceSearchView voiceSearchView;
    private ImageView ivDeleteText;
    private EmptyLayout mEmptyLayout;
    private EmptyLayout mEmptyMenu;

    private SaleAdapter adapter;
    private SimpleAdapter radapter;
    private StateBroadcast sBroadcast;
    private String key;// 筛选条件key
    private Calendar calendar;
    private DatePickerDialog dialog;
    private String from;//来自具体的哪个页面
    private int mPreviousVisibleItem;

    private ArrayList<Map<String, Object>> rdata = new ArrayList<Map<String, Object>>();
    private Map<String, Object> rMap = new HashMap<String, Object>();//@注释：列表数据

    private int cur = 1;
    private View.OnClickListener mErrorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEmptyLayout.showLoading();
            mlistview.setRefreshing(true);
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            switch (msg.what) {
                case 1:
                    if (adapter == null) {
                        LogUtil.d("网络重置适配器");
                        adapter = new SaleAdapter(CommonFormListActivity.this, rMap);
                        mlistview.setAdapter(adapter);
                    } else {
                        if (!StringUtil.isEmpty(voiceSearchView.getText().toString())) {
                            adapter.getFilter().filter(voiceSearchView.getText().toString());
                        }
                        adapter.notifyDataSetChanged();
                    }
                    if (adapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    mlistview.onRefreshComplete();
                    break;
                case 2:
                    Toast.makeText(CommonFormListActivity.this, "数据加载完毕", Toast.LENGTH_LONG).show();
                    mlistview.onRefreshComplete();
                    mlistview.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    break;
                case 3:
                    // 筛选字段
                    if (!rdata.isEmpty()) {
                        rdata.clear();
                    }
                    Bundle bundle = msg.getData();
                    String result = bundle.getString("result");
                    Map<String, Object> map = FlexJsonUtil.fromJson(result);
                    ArrayList<HashMap<String, Object>> cArrayList = new ArrayList<HashMap<String, Object>>();
                    cArrayList = (ArrayList<HashMap<String, Object>>) map
                            .get("conditions");
                    if (cArrayList != null) {
                        for (int i = 0; i < cArrayList.size(); i++) {
                            String parentStr = (String) cArrayList.get(i).get(
                                    "caption");
                            String field = (String) cArrayList.get(i).get("field");
                            String type = (String) cArrayList.get(i).get("type");
                            /** @注释：FlexJsonUtil.toJsonArray(cArrayList) 解析失败！ */
                            Map<String, Object> item;
                            item = new HashMap<String, Object>();
                            item.put("field", parentStr);
                            /** @注释：页面显示 */
                            item.put("selected", "所有");
                            /** @注释：发送给服务器的数据项 */
                            item.put("value", "所有");
                            /** @注释：服务器数据库字段项，错了会异常 */
                            item.put("dbfield", field);
                            item.put("type", type);
                            rdata.add(item);
                        }
                    }
                    // initRightDrawer(result);
                    break;
                default:
                    break;
            }
        }

        ;
    };
    private String caller;
    private String status;
    private int currentPosition;
    private String serveId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sale_select_list);
        ViewUtils.inject(this);
        initIDs();
        mEmptyLayout = new EmptyLayout(this, mlistview.getRefreshableView());
        mEmptyLayout.setEmptyButtonClickListener(mErrorClickListener);
        initView();
        LogUtil.d("onCreate()");
        initData();
    }

    private void initIDs() {
        mlistview = (PullToRefreshListView) findViewById(R.id.lv_sale_list);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        ivDeleteText = (ImageView) findViewById(R.id.iv_DeleteText);
    }


    private void initView() {
        Intent intent = getIntent();
        caller = intent.getStringExtra("caller");
        from = intent.getStringExtra("from");
        serveId = intent.getStringExtra("serveId");
        if ("Ask4Leave".equals(caller)) {
            setTitle("请假单");
        }
        if ("SpeAttendance".equals(caller)) {
            setTitle("特殊考勤");
        }
        if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {
            setTitle("出差单");
        }
        if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {
            setTitle("加班单");
        }
        if ("MaterielApply".equals(caller)) {
           setTitle("物料申请单");
        } else if ("MainTain".equals(caller)) {
            setTitle("维修申请单");
        }
        if ("FeePlease!FYBX".equals(caller)) {
           setTitle("费用报销");
        }

        if (!StringUtil.isEmpty(intent.getStringExtra("title"))) {
           setTitle(intent.getStringExtra("title"));
        }
        String user = CommonUtil.getSharedPreferences(CommonFormListActivity.this,
                "username");
        String master = CommonUtil.getSharedPreferences(
                CommonFormListActivity.this, "master");
        key = user + master + caller;
        // setTitle(intent.getStringExtra("mTitle"));
        calendar = Calendar.getInstance();
        sBroadcast = new StateBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.erp.sale.dataupdate");
        registerReceiver(sBroadcast, intentFilter);
        mlistview.setFilterTouchEvents(false);
        mlistview.setMode(PullToRefreshBase.Mode.BOTH);
        mlistview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                if (rMap != null) {
                    rMap.clear();
                }
                cur = 1;
                mlistview.setMode(PullToRefreshBase.Mode.BOTH);
//                if (adapter != null) {
//                    adapter.notifyDataSetChanged();
//                }
                mEmptyLayout.showLoading();
                String where = CommonUtil.getSharedPreferences(
                        CommonFormListActivity.this, "where");
                String condition = CommonUtil.getSharedPreferences(
                        CommonFormListActivity.this, key);

                if ("Ask4Leave".equals(caller)) {

                    condition = "va_emcode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("SpeAttendance".equals(caller)) {

                    condition = "sa_appmancode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {

                    condition = "wod_empcode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {

                    condition = "FP_PEOPLE2='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("FeePlease!FYBX".equals(caller)) {

                    condition = "fp_pleasemancode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
                }
                if (condition != null) {
                    new Thread(new GetSaleData(String.valueOf(cur), "10", condition))
                            .start();
                } else {
                    new Thread(new GetSaleData(String.valueOf(cur), "10", where))
                            .start();
                }

            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                cur++;
//				rBar.setVisibility(View.VISIBLE);
                String where = CommonUtil.getSharedPreferences(
                        CommonFormListActivity.this, "where");
                String condition = CommonUtil.getSharedPreferences(
                        CommonFormListActivity.this, key);
                if ("Ask4Leave".equals(caller)) {

                    condition = "va_emcode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("SpeAttendance".equals(caller)) {

                    condition = "sa_appmancode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {

                    condition = "wod_empcode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {

                    condition = "FP_PEOPLE2='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";

                } else if ("FeePlease!FYBX".equals(caller)) {

                    condition = "fp_pleasemancode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
                }
                if (condition != null) {
                    new Thread(new GetSaleData(String.valueOf(cur), "10", condition))
                            .start();
                } else {
                    new Thread(new GetSaleData(String.valueOf(cur), "10", where))
                            .start();
                }
//                if (!StringUtil.isEmpty(etSearch.toString())) {
//                    etSearch.setText("");
//                }
            }
        });

        /**
         * 搜索框监听事件
         */

        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (adapter == null) {
                    Toast.makeText(getApplication(), "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                } else {
                    if (!StringUtil.isEmpty(voiceSearchView.getText().toString())) {
                        mlistview.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
                        adapter.getFilter().filter(voiceSearchView.getText().toString());
                    } else {
                        mlistview.setMode(PullToRefreshBase.Mode.BOTH);
                        initData();
//                        adapter.getFilter().filter("");
//                        adapter.notifyDataSetChanged();
                    }
                }
                //  mlistview.setAdapter(adapter);
            }
        });


        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                currentPosition = position;
                String keyField = (String) rMap.get("keyField");
                String pfField = (String) rMap.get("pfField");
                String statusKey = "";

                if ("Ask4Leave".equals(caller)) {

                    statusKey = "va_status";

                } else if ("SpeAttendance".equals(caller)) {

                    statusKey = "sa_status";

                } else if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {

                    statusKey = "wo_status";

                } else if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {

                    statusKey = "fp_status";


                } else if ("StandbyApplication".equals(caller)) {
                    statusKey = "sa_status";

                } else if ("MaterielApply".equals(caller)) {
                    statusKey = "ama_status";
                } else if ("MainTain".equals(caller)) {
                    statusKey = "mt_status";
                  setTitle("维修申请单");
                } else if ("FeePlease!FYBX".equals(caller)) {
                    statusKey = "fp_status";
                }
                //防错处理
                if (StringUtil.isEmpty(pfField)) {
                    pfField = keyField;
                }
                if (StringUtil.isEmpty(keyField)) {
                    keyField = pfField;
                }
                List<Object> rList = (List<Object>) rMap.get("listdata");
                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = (Map<String, Object>) rList
                        .get(position - 1);
                int keyId = 0;
                if (itemMap.get(keyField) != null) {
                    keyId = Integer.valueOf(itemMap.get(keyField)
                            .toString());
                }

                if (itemMap.get(statusKey) == null) {
//                    ToastMessage("单据状态为空！");
//                    return;
                }
                status = "无状态";
                LogUtil.d("caller:" + caller + "  id:" + id);
                if ("在录入".equals(status)) {
                    if ("FeePlease!FYBX".equals(caller)) {
                        startActivity(new Intent(mContext,
                                ExpenseReimbursementActivity.class)
                                .putExtra("caller", caller)
                                .putExtra("id", keyId));
                    } else {
                        startActivity(new Intent(mContext,
                                DataFormDetailActivity.class)
                                .putExtra("caller", caller)
                                .putExtra("title", getToolBarTitle().toString())
                                .putExtra("status", status)
                                .putExtra("id", keyId));
                    }
                } else {
                    startActivity(new Intent(mContext,
                            CommonCityIndustryDetailsActivity.class)
                            .putExtra("title",getToolBarTitle().toString())
                            .putExtra("keyValue", keyId)
                            .putExtra("serve_id", serveId));
                }

            }
        });
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        isdelete = false;
    }


    private void initData() {
        if (CommonUtil.isNetWorkConnected(this)) {
            mEmptyLayout.showLoading();
            if (rMap != null) rMap.clear();
            CommonUtil.clearSharedPreferences(this, "where");
            String condition = CommonUtil.getSharedPreferences(
                    this, key);
            if ("Ask4Leave".equals(caller)) {
                condition = "va_emcode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
            } else if ("SpeAttendance".equals(caller)) {
                condition = "sa_appmancode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
            } else if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {
                condition = "wod_empcode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
            } else if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {
                condition = "FP_PEOPLE2='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
            } else if ("FeePlease!FYBX".equals(caller)) {
                condition = "fp_pleasemancode='" + CommonUtil.getSharedPreferences(ct, "erp_username") + "'";
            }
            if (condition != null) {
                progressDialog.show();
                new Thread(new GetSaleData(String.valueOf(cur), "10", condition))
                        .start();
            } else {
                progressDialog.show();
                new Thread(new GetSaleData(String.valueOf(cur), "10", null))
                        .start();
            }
        } else {
            mEmptyLayout.setEmptyMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showEmpty();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    public static boolean reload = false;

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d("onResume intent:" + reload + "currentPosition:" + currentPosition);
        if (reload) {
            initData();
        } else {
            try {
                if (currentPosition != 0 && isdelete) {
                    LogUtil.d(String.valueOf(currentPosition));
                    List<Object> lists = (List<Object>) rMap.get("listdata");
                    lists.remove(currentPosition - 1);
                    rMap.put("listdata", lists);
                    LogUtil.d("onResume:" + JSON.toJSONString(lists));
                    adapter.notifyDataSetChanged();
                    if (adapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LogUtil.d("onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sBroadcast);
        LogUtil.d("onDestroy()");
    }

    public class GetSaleData implements Runnable {
        private String curpage = "1";
        private String pagesize = "10";
        private String where;

        public GetSaleData(String curpage, String pagesize, String where) {
            this.curpage = curpage;
            this.pagesize = pagesize;
            this.where = where;
        }

        @Override
        public void run() {
            sendResquest(curpage, pagesize, where);
        }
    }

    public void sendResquest(String page, String size, String where) {
        HttpClient client = new HttpClient();
        Map<String, String> params = new HashMap<String, String>();
        params.put("page", page);
        params.put("pageSize", size);
        params.put("serve_id", serveId);
        params.put("sessionId", CommonUtil.getB2BUid(this));
        params.put("from", "city_service");
        String result = null;
        try {
            result = client.sendPostRequest(CommonUtil.getCityBaseUrl(this)
                    + "api/serve/query.action", params);
            LogUtil.prinlnLongMsg("HttpLogs", JSON.toJSONString(params) + ":" + result);
            if (result != null && rMap != null) {
                if (result.contains("exceptionInfo")) {
                    //这里报销单相同参数第一次请求正常，下啦刷新报错不知道原因，只能这么处理
                    handler.sendEmptyMessage(1);
                    mlistview.onRefreshComplete();
                    return;
                } else {
                    Map<String, Object> tMap = new HashMap<String, Object>();
                    tMap = FlexJsonUtil.fromJson(result);
                    if (rMap.get("columns") != null) {
                        @SuppressWarnings("unchecked")
                        List<Object> lists = (List<Object>) rMap.get("listdata");
                        List<?> newlist = (List<?>) tMap.get("listdata");
                        if (ListUtils.isEmpty(newlist)) {
                            cur--;
                            handler.sendEmptyMessage(2);
                        } else {
                            lists.addAll(newlist);
                        }
                        rMap.put("listdata", lists);
                    } else {
//                    if (!"FeePlease!FYBX".equals(caller)){
                        rMap.putAll(tMap);
//                    }
                    }
                }
                handler.sendEmptyMessage(1);
            } else {
                handler.sendEmptyMessage(1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @注释：适配器
     */
    public class SaleAdapter extends BaseAdapter implements Filterable {

        private Context ct;
        private LayoutInflater inflater;
        private Map<String, Object> data = new HashMap<String, Object>();
        private Map<String, Object> dMap = new HashMap<String, Object>();

        public SaleAdapter(Context ct, Map<String, Object> data) {
            this.ct = ct;
            this.inflater = LayoutInflater.from(ct);
            this.data = data;
            this.dMap = data;
//            Log.d("SaleAdapter:", data.toString());
        }

        @Override
        public int getCount() {
            if (data != null) {
                if ((List<?>) data.get("listdata") == null) {
                    return 0;
                } else {
                    return ((List<?>) data.get("listdata")).size();
                }
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return ((List<?>) data.get("listdata")).get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            List<Object> dList = ((List<Object>) data.get("listdata"));
            List<Object> cList = ((List<Object>) data.get("columns"));
            Map<String, Object> dmap = new HashMap<String, Object>();
            Map<String, Object> map = new HashMap<String, Object>();

            TextView[][] tViews = new TextView[2][cList.size()];
            dmap = (Map<String, Object>) dList.get(position);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.act_sale_find_item,
                        null);
                LinearLayout ly = (LinearLayout) convertView
                        .findViewById(R.id.ly_sale_view);
                int showIndex = 0;

                if (!data.isEmpty()) {
                    /** @注释：第一列， 多增加序列号文本 */
                    for (int i = 0; i < cList.size(); i++) {
                        if (!cList.isEmpty()) {
                            map = (HashMap<String, Object>) cList.get(i);
                        }
                        if ((Integer) map.get("width") == 0) {
                            if (i == 0)
                                showIndex = 1;
                            continue;//隐藏width=0的字段
                        }
                        RelativeLayout rLayout = new RelativeLayout(ct);
                        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        if (i == showIndex) {
                            TextView nView = new TextView(ct);
                            RelativeLayout.LayoutParams nParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                            nParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            nParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                            nView.setText(String.valueOf(cur));
                            nView.setTextColor(getResources().getColor(
                                    R.color.white));
                            nView.setBackgroundDrawable(getResources().getDrawable(
                                    R.drawable.skin_icon_blue_circle));
                            nView.setLayoutParams(nParams);
                            nView.setGravity(Gravity.CENTER_HORIZONTAL);
                            nView.setText((position + 1) + "");
                            nView.setVisibility(View.GONE);
                            rLayout.addView(nView);
                            tViews[0][0] = nView;

                        }
                        if (i == cList.size() - 1) {
                            // l.height = CommonUtil.dip2px(ct, 30);
                        }


                        // 创建两个TextView
                        TextView tView = new TextView(ct);
                        tView.setId(i + 1);
                        RelativeLayout.LayoutParams tv = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        tv.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                        tView.setWidth(CommonUtil.dip2px(ct, 90));
                        tView.setMaxWidth(CommonUtil.dip2px(ct, 100));
                        tView.getPaint().setFakeBoldText(true);//加粗
                        tv.leftMargin = 30;
                        tv.rightMargin = 10;
                        tv.topMargin = 7;
                        tv.bottomMargin = 7;
                        tView.setTextSize(16);
                        tView.setGravity(Gravity.LEFT | Gravity.TOP);


                        tView.setText(map.get("caption").toString() + "");
                        tView.setLayoutParams(tv);
                        rLayout.addView(tView);


                        TextView mView = new TextView(ct);
                        RelativeLayout.LayoutParams mv = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        mv.addRule(RelativeLayout.CENTER_VERTICAL);
                        mv.addRule(RelativeLayout.RIGHT_OF, tView.getId());
                        mv.leftMargin = 20;
                        mv.rightMargin = 20;
                        mv.topMargin = 8;
                        mv.bottomMargin = 8;
                        mView.setTextSize(16);
                        mView.setTextColor(mContext.getResources().getColor(R.color.light_gray));
                        // mView.setText();
                        mView.setLayoutParams(mv);
                        tViews[1][i] = mView;
                        rLayout.addView(mView);

                        rLayout.setLayoutParams(l);
                        ly.addView(rLayout);


                        ly.setBackgroundDrawable(ct.getResources().getDrawable(
                                R.drawable.shape_linear_detail));
                        convertView.setTag(tViews);
                    }
                }
            } else {
                tViews = (TextView[][]) convertView.getTag();
            }

            convertView.setBackgroundColor(ct.getResources().getColor(R.color.item_color2));

            for (int j = 0; j < cList.size(); j++) {
                map = (HashMap<String, Object>) cList.get(j);
                // if ((Integer)map.get("width")==0)continue;
                if ((Integer) map.get("width") == 0)
                    continue;
                tViews[1][j].setText(dmap.get(map.get("dataIndex")).toString());
            }
            if (tViews[0][0] != null) {
                // tViews[0][0].setText((position + 1) + "");
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults searchResults = new FilterResults();

                    List<Object> tList = ((List<Object>) dMap.get("listdata"));
                    if (constraint == null || constraint.length() == 0) {
                        searchResults.values = dMap;
                        searchResults.count = tList.size();
                    } else {
                        Map<String, Object> teMap = new HashMap<String, Object>();
                        List<?> columns = (List<?>) dMap.get("columns");
                        teMap.put("columns", dMap.get("columns"));
                        String keyField = (String) dMap.get("keyField");
                        String pfField = (String) dMap.get("pfField");
                        teMap.put("keyField", keyField);
                        teMap.put("pfField", pfField);
                        ArrayList<String> searchkey = new ArrayList<String>();
                        if (columns != null) {
                            for (int i = 0; i < columns.size(); i++) {
                                @SuppressWarnings("unchecked")
                                HashMap<String, Object> cMap = (HashMap<String, Object>) columns
                                        .get(i);
                                if (!cMap.get("dataIndex").toString().equals(keyField)
                                        && !cMap.get("dataIndex").toString().equals(pfField)) {
                                    searchkey.add(cMap.get("dataIndex").toString());//取到所有的keys
                                }
                            }
                        }
                        // Lg.d(JSON.toJSONString(tList));
                        //循环遍历values
                        List<Object> nList = new ArrayList<Object>();
                        for (int i = 0; i < tList.size(); i++) {
                            @SuppressWarnings("unchecked")
                            HashMap<String, Object> t = (HashMap<String, Object>) tList
                                    .get(i);
//                            Lg.d(JSON.toJSONString(searchkey));
                            if (searchkey != null && searchkey.size() != 0) {
                                for (int j = 0; j < searchkey.size(); j++) {
                                    if (t.get(searchkey.get(j))
                                            .toString()
                                            .toLowerCase()
                                            .contains(
                                                    constraint.toString()
                                                            .toLowerCase())) {
                                        nList.add(t);
                                        // Lg.d(JSON.toJSONString(t));
                                        break;
                                    }
                                }
                            }

                        }
                        teMap.put("listdata", nList);
                        //Lg.d("preare:" + JSON.toJSONString(nList));
                        searchResults.values = teMap;
                        searchResults.count = nList.size();
                    }
                    return searchResults;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {
                    data = (Map<String, Object>) results.values;
                    LogUtil.d("遍历过后的数据：" + JSON.toJSONString(data));
                    rMap = data;//传递数据源
                    if (adapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    if (!StringUtil.isEmpty(constraint.toString())) {
                        //这里为什么这样做，主要是由于item是动态创建的，查询的数据的item高度大于其它item高度时，
                        //会发生错位问题，重新设置adaper就没有问题
                        // Lg.d("listview 重置适配器");
                        mlistview.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetInvalidated();
                        LogUtil.d("本地查询刷新。。。");
                        //mlistview.getRefreshableView().setSelection(0);
                    }

                }

            };
        }

    }

    @Override
    public void onClick(View v) {
    }


    public class StateBroadcast extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("value");
            String field = intent.getStringExtra("field");
            for (int i = 0; i < rdata.size(); i++) {
                String dbfield = (String) rdata.get(i).get("dbfield");
                if (dbfield.endsWith(field)) {
                    @SuppressWarnings("rawtypes")
                    Iterator iter = rdata.get(i).entrySet().iterator();
                    while (iter.hasNext()) {
                        @SuppressWarnings("rawtypes")
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String) entry.getKey();
                        @SuppressWarnings("unused")
                        String val = (String) entry.getValue();
                        /** @注释：索引值 */
                        if (key.equals("value")) {
                            entry.setValue(value);
                        }
                        /** @注释：页面显示 */
                        if (key.equals("selected")) {
                            entry.setValue(value);
                        }
                    }
                }
            }
            if (radapter != null) {
                radapter.notifyDataSetChanged();
            }

        }
    }

}
