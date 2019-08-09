/**
 *
 */
package com.xzjmyk.pm.activity.ui.erp.activity;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.FlexJsonUtil;
import com.core.widget.EmptyLayout;
import com.github.clans.fab.FloatingActionButton;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.net.HttpClient;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author LiuJie
 */
@SuppressWarnings("deprecation")
public class SaleSelectActivity extends BaseActivity implements OnClickListener {

    @ViewInject(R.id.lv_sale_list)
    private PullToRefreshListView mlistview;
    @ViewInject(R.id.et_Search)
    private EditText etSearch;
    private EmptyLayout mEmptyLayout;
    private EmptyLayout mEmptyMenu;

    private SaleAdapter adapter;
    private SimpleAdapter radapter;
    private StateBroadcast sBroadcast;
    private String key;// 筛选条件key
    @ViewInject(R.id.iv_DeleteText)
    private ImageView ivDeleteText;
    private Calendar calendar;
    private DatePickerDialog dialog;
    private String from;//来自具体的哪个页面
    private int mPreviousVisibleItem;
    private int mketValue;

    private ArrayList<Map<String, Object>> rdata = new ArrayList<Map<String, Object>>();
    private Map<String, Object> rMap = new HashMap<String, Object>();//@注释：列表数据

    private int cur = 1;
    private OnClickListener mErrorClickListener = new OnClickListener() {
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
            switch (msg.what) {
                case 1:
                    if (adapter == null) {
                        adapter = new SaleAdapter(SaleSelectActivity.this, rMap);
                        mlistview.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    if (adapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    mlistview.onRefreshComplete();
                    break;
                case 2:
                    Toast.makeText(SaleSelectActivity.this, "数据加载完毕", Toast.LENGTH_LONG).show();
                    mlistview.onRefreshComplete();
                    break;
                case 3:
                    // 筛选字段
                    if (!rdata.isEmpty()) {
                        rdata.clear();
                    }
                    Bundle bundle = msg.getData();
                    String result = bundle.getString("result");
                    LogUtil.prinlnLongMsg("Travel",result);
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
                    initRightDrawer(result);
                    break;
                default:
                    break;
            }
        }

        ;
    };
    private String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sale_select_list);
        ViewUtils.inject(this);
        mEmptyLayout = new EmptyLayout(this, mlistview.getRefreshableView());
        mEmptyLayout.setEmptyButtonClickListener(mErrorClickListener);
        initView();
        initData();
    }
    

    private void initView() {
        Intent intent = getIntent();
        caller = intent.getStringExtra("caller");
        from = intent.getStringExtra("from");

        String user = CommonUtil.getSharedPreferences(SaleSelectActivity.this,
                "username");
        String master = CommonUtil.getSharedPreferences(
                SaleSelectActivity.this, "master");
        key = user + master + caller;
       setTitle(intent.getStringExtra("title"));
        calendar = Calendar.getInstance();
        sBroadcast = new StateBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.erp.sale.dataupdate");
        registerReceiver(sBroadcast, intentFilter);
        mlistview.setFilterTouchEvents(false);
        mlistview.setMode(Mode.BOTH);
        mlistview.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                rMap.clear();
                cur = 1;
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                mEmptyLayout.showLoading();
                String where = CommonUtil.getSharedPreferences(
                        SaleSelectActivity.this, "where");
                String condition = CommonUtil.getSharedPreferences(
                        SaleSelectActivity.this, key);
                if (condition != null) {
                    new Thread(new GetSaleData(String.valueOf(cur), "30", condition))
                            .start();
                } else {
                    new Thread(new GetSaleData(String.valueOf(cur), "30", where))
                            .start();
                }

            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                cur++;
//				rBar.setVisibility(View.VISIBLE);
                String where = CommonUtil.getSharedPreferences(
                        SaleSelectActivity.this, "where");
                String condition = CommonUtil.getSharedPreferences(
                        SaleSelectActivity.this, key);
                if (condition != null) {
                    new Thread(new GetSaleData(String.valueOf(cur), "30", condition))
                            .start();
                } else {
                    new Thread(new GetSaleData(String.valueOf(cur), "30", where))
                            .start();
                }
            }
        });

        /**
         * 搜索框监听事件
         */

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (adapter==null) {
                    Toast.makeText(getApplication(),"网络异常，请稍后重试",Toast.LENGTH_SHORT).show();
                }else{
                    if (!StringUtil.isEmpty(etSearch.getText().toString())) {
                        mlistview.setMode(Mode.PULL_FROM_END);
                        adapter.getFilter().filter(etSearch.getText().toString());
                    } else {
                        adapter.getFilter().filter("");
                        mlistview.setMode(Mode.BOTH);
                    }
                }
                mlistview.setAdapter(adapter);

            }
        });

        /** @注释：已显示单据点击监听 */
        mlistview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String keyField = (String) rMap.get("keyField");
                String pfField = (String) rMap.get("pfField");
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

               // String mkeyValue = rList.get(position).
                int keyId = 0;
                if (itemMap.get(keyField) != null) {
                    keyId = Integer.valueOf(itemMap.get(keyField)
                            .toString());
                }
                Intent it_detail = null;
                if ("SignMain".equals(from)) {
                    if ("Ask4Leave".equals(caller)) {
                        it_detail = new Intent(SaleSelectActivity.this,
                                LeaveActivity.class);
//                                                  CommonDocDetailsActivity.class);
                    }
                    if ("SpeAttendance".equals(caller)) {
                        it_detail = new Intent(SaleSelectActivity.this,
                                ExtraLeaveActivity.class);
//                                                      CommonDocDetailsActivity.class);
                    }
                    if ("FeePlease!CCSQ".equals(caller)) {
                        it_detail = new Intent(SaleSelectActivity.this,
                               TravelActivity.class);
//                                        CommonDocDetailsActivity.class);
                    }
                    if ("Workovertime".equals(caller)) {
                        it_detail = new Intent(SaleSelectActivity.this,
                                WorkExtraActivity.class);
//                                                     CommonDocDetailsActivity.class);
                    }
                } else {
                    it_detail = new Intent(SaleSelectActivity.this,
                            SaleDetailActivity.class);
                }
                it_detail.putExtra("formCondition", keyField + "=" + keyId);
                it_detail.putExtra("gridCondition", pfField + "=" + keyId);

                it_detail.putExtra("keyValue",keyId);
                it_detail.putExtra("caller", caller);
                startActivity(it_detail);

            }
        });
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show(true);
                fab.setShowAnimation(AnimationUtils.loadAnimation(SaleSelectActivity.this, R.anim.show_from_bottom));
                fab.setHideAnimation(AnimationUtils.loadAnimation(SaleSelectActivity.this, R.anim.hide_to_bottom));
            }
        }, 300);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it_detail = null;
                if ("请假单查询".equals(getToolBarTitle().toString())) {
                    it_detail = new Intent(SaleSelectActivity.this,
                            LeaveActivity.class);
                }
                if ("出差单查询".equals(getToolBarTitle().toString())) {
                    it_detail = new Intent(SaleSelectActivity.this,
                            TravelActivity.class);

                }
                if ("加班申请查询".equals(getToolBarTitle().toString())) {
                    it_detail = new Intent(SaleSelectActivity.this,
                            WorkExtraActivity.class);
                }
                if ("特殊考勤查询".equals(getToolBarTitle().toString())) {
                    it_detail = new Intent(SaleSelectActivity.this,
                            ExtraLeaveActivity.class);
                }
                if ("请假单2查询".equals(getToolBarTitle().toString())) {
                    it_detail = new Intent(SaleSelectActivity.this,
                            LeaveActivity.class);
                }
                if (it_detail != null) {
                    startActivity(it_detail);
                } else {
                    ViewUtil.ShowMessageTitle(SaleSelectActivity.this, "不支持添加操作！");
                }

            }
        });


        mlistview.getRefreshableView().setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > mPreviousVisibleItem) {
                    fab.hide(true);
                } else if (firstVisibleItem < mPreviousVisibleItem) {
                    fab.show(true);
                }
                mPreviousVisibleItem = firstVisibleItem;
            }
        });


    }

    /**
     * @注释：初始化右边菜单
     */
    public void initRightDrawer(final String res) {
    }

    private void initData() {
        if (CommonUtil.isNetWorkConnected(this)) {
            mEmptyLayout.showLoading();
            CommonUtil.clearSharedPreferences(SaleSelectActivity.this, "where");
            String condition = CommonUtil.getSharedPreferences(
                    SaleSelectActivity.this, key);
            if (condition != null) {
                new Thread(new GetSaleData("1", "30", condition)).start();
            } else {
                new Thread(new GetSaleData("1", "30", null)).start();
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sBroadcast);
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
        String sessionId = CommonUtil.getSharedPreferences(this, "sessionId");
        Map<String, String> params = new HashMap<String, String>();
        params.put("page", page);
        params.put("pageSize", size);
        params.put("caller", caller);
        if (where == null) {
            where = "1=1";
        }
        params.put("condition", where);
        params.put("sessionId", sessionId);
        String result = null;
        try {
            result = client.sendPostRequest(CommonUtil.getAppBaseUrl(this)
                    + "mobile/common/list.action", params);

            JSONObject resultJsonObject =new JSONObject(result);
            JSONArray listdata = resultJsonObject.getJSONArray("listdata");


            LogUtil.prinlnLongMsg("mobile/common/list.action", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("result:" + result);
        if (result != null) {
            Map<String, Object> tMap = new HashMap<String, Object>();
            tMap = FlexJsonUtil.fromJson(result);
            if (rMap.get("columns") != null) {
                @SuppressWarnings("unchecked")
                List<Object> lists = (List<Object>) rMap.get("listdata");
                List<?> newlist = (List<?>) tMap.get("listdata");
                if (newlist.isEmpty()) {
                    cur--;
                    handler.sendEmptyMessage(2);
                } else {
                    lists.addAll(newlist);
                }
                rMap.put("listdata", lists);
            } else {
                rMap.putAll(tMap);
            }
            handler.sendEmptyMessage(1);
        } else {
            handler.sendEmptyMessage(1);
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
            Log.d("SaleAdapter:",data.toString());
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
                            continue;
                        }
                        RelativeLayout rLayout = new RelativeLayout(ct);
                        LayoutParams l = new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT);
                        if (i == showIndex) {
                            TextView nView = new TextView(ct);
                            LayoutParams nParams = new LayoutParams(
                                    LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT);
                            nParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            nParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            // nView.setText(dmap.get("RN").toString());
                            nView.setTextColor(getResources().getColor(
                                    R.color.white));
                            nView.setBackgroundDrawable(getResources().getDrawable(
                                    R.drawable.skin_icon_blue_circle));
                            nView.setLayoutParams(nParams);
                            nView.setGravity(Gravity.CENTER);
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
                        LayoutParams tv = new LayoutParams(
                                LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                        tv.addRule(RelativeLayout.CENTER_VERTICAL);

                        tView.setWidth(CommonUtil.dip2px(ct, 90));
                        tView.setMaxWidth(CommonUtil.dip2px(ct, 100));
                        tv.leftMargin = 10;
                        tv.rightMargin = 10;
                        tv.topMargin = 7;
                        tv.bottomMargin = 7;
                        tView.setTextSize(16);
                        tView.setGravity(Gravity.RIGHT);
                        tView.setText(map.get("caption").toString() + ":");

                        tView.setLayoutParams(tv);
                        rLayout.addView(tView);

                        TextView mView = new TextView(ct);
                        LayoutParams mv = new LayoutParams(
                                LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                        mv.addRule(RelativeLayout.CENTER_VERTICAL);
                        mv.addRule(RelativeLayout.RIGHT_OF, tView.getId());
                        mv.leftMargin = 20;
                        mv.rightMargin = 20;
                        mv.topMargin = 8;
                        mv.bottomMargin = 8;
                        mView.setTextSize(16);
                        // mView.setText();
                        mView.setLayoutParams(mv);
                        tViews[1][i] = mView;
                        rLayout.addView(mView);

                        rLayout.setLayoutParams(l);
                        ly.addView(rLayout);

//						View line = new View(ct);
//						LayoutParams v = new LayoutParams(
//								LayoutParams.MATCH_PARENT, CommonUtil.dip2px(
//										ct, 1));
//						line.setLayoutParams(v);
//						line.setBackgroundColor(getResources().getColor(
//								R.color.lightgray));
//						ly.addView(line);

                        ly.setBackgroundDrawable(ct.getResources().getDrawable(
                                R.drawable.shape_linear_detail));
                        convertView.setTag(tViews);
                    }
                }
            } else {
                tViews = (TextView[][]) convertView.getTag();
            }
            if (position % 2 == 0) {
                convertView.setBackgroundColor(ct.getResources().getColor(R.color.item_color1));
            } else {
                convertView.setBackgroundColor(ct.getResources().getColor(R.color.item_color2));
            }
            for (int j = 0; j < cList.size(); j++) {
                map = (HashMap<String, Object>) cList.get(j);
                // if ((Integer)map.get("width")==0)continue;
                if ((Integer) map.get("width") == 0)
                    continue;
                tViews[1][j].setText(dmap.get(map.get("dataIndex")).toString());
            }
            if (tViews[0][0] != null) {
                //   tViews[0][0].setText(dmap.get("RN").toString());
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults searchResults = new FilterResults();
                    @SuppressWarnings("unchecked")
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
                                searchkey.add(cMap.get("dataIndex").toString());
                            }
                        }

                        List<Object> nList = new ArrayList<Object>();
                        for (int i = 0; i < tList.size(); i++) {
                            @SuppressWarnings("unchecked")
                            HashMap<String, Object> t = (HashMap<String, Object>) tList
                                    .get(i);
                            if (searchkey != null && searchkey.size() != 0) {
                                for (int j = 0; j < searchkey.size(); j++) {
                                    if (t.get(searchkey.get(j))
                                            .toString()
                                            .toLowerCase()
                                            .contains(
                                                    constraint.toString()
                                                            .toLowerCase())) {
                                        nList.add(t);
                                    }
                                }
                            }

                        }
                        teMap.put("listdata", nList);
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
                    rMap = data;
                    if (adapter.getCount() == 0) {
                        mEmptyLayout.showEmpty();
                    }
                    notifyDataSetChanged();
                }

            };
        }

    }

    @Override
    public void onClick(View v) {
    }

    @SuppressWarnings("unchecked")
    public void clearRightMenuStatus() {
        for (int i = 0; i < rdata.size(); i++) {
            @SuppressWarnings("rawtypes")
            Iterator iter = rdata.get(i).entrySet().iterator();
            while (iter.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                if (key.equals("selected")) {
                    entry.setValue("所有");
                }
                if (key.equals("value")) {
                    entry.setValue(null);
                }
            }
        }
        if (radapter != null) {
            radapter.notifyDataSetChanged();
        }
    }

    public class CheckedChangeEvent implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
//			case R.id.cb_remember_condition:
//				isCheckedRightMenu(isChecked);
//				break;
                default:
                    break;
            }
        }

    }

    /**
     * @author LiuJie
     * @功能:根据参数，检查是否被勾选记住偏好条件
     */
    public void isCheckedRightMenu(boolean isChecked) {
        if (isChecked) {
            /** @注释：user+master+query condition */
            Map<String, Object> kMap = getConditionKeyMap();
            String kValue = FlexJsonUtil.toJson(kMap);
            CommonUtil.setSharedPreferences(SaleSelectActivity.this,
                    key, getConditionForMap());
            CommonUtil.setSharedPreferences(SaleSelectActivity.this,
                    "isCheck" + key, true);
            /** @注释：恢复右边筛选菜单的偏好选择状态 */
            CommonUtil.setSharedPreferences(SaleSelectActivity.this,
                    "kValue" + key, kValue);
        } else {
            CommonUtil.clearSharedPreferences(SaleSelectActivity.this,
                    key);
            CommonUtil.clearSharedPreferences(SaleSelectActivity.this,
                    "kValue" + key);
            CommonUtil.clearSharedPreferences(SaleSelectActivity.this,
                    "isCheck" + key);
        }
    }

    /**
     * @注释：日期选择监听
     */
    OnDateSetListener DateSet = new OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // 每次保存设置的日期
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar
                    .getTime());
            // String str = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
            // ViewUtil.ToastMessage(getApplicationContext(), str+":"+date);
            for (int i = 0; i < rdata.size(); i++) {
                if (rdata.get(i).get("type").equals("condatefield")) {
                    rdata.get(i).put("selected", date);
                    rdata.get(i).put("value", date);
                }
            }
            radapter.notifyDataSetChanged();
        }

    };

    public String getConditionForMap() {
        StringBuffer sBuffer = new StringBuffer();
        int size = 0;
        for (int j = 0; j < rdata.size(); j++) {
            if (rdata.get(j).get("value") != null) {
                if (!rdata.get(j).get("value").equals("所有")) {
                    size++;
                }
            }
        }
        int count = 0;
        for (int i = 0; i < rdata.size(); i++) {
            if (!"所有".equals(rdata.get(i).get("value"))
                    && rdata.get(i).get("value") != null) {
                /** @注释：想想为什么用size=count来判断是否属于最后一项 */
                count++;
                if (size == count) {
                    if (rdata.get(i).get("type").equals("condatefield")) {
                        /** @注释：日期格式 外面没有单引号 */
                        sBuffer.append("to_char(" + rdata.get(i).get("dbfield")
                                + ",'yyyy-mm-dd') =");
                        sBuffer.append("'" + rdata.get(i).get("value") + "'");
                    } else {
                        sBuffer.append(rdata.get(i).get("dbfield") + "=");
                        sBuffer.append("'" + rdata.get(i).get("value") + "'");
                    }

                } else {
                    if (rdata.get(i).get("type").equals("condatefield")) {
                        /** @注释：日期格式 外面没有单引号 */
                        sBuffer.append("to_char(" + rdata.get(i).get("dbfield")
                                + ",'yyyy-mm-dd') =");
                        sBuffer.append("'" + rdata.get(i).get("value") + "'"
                                + " and ");
                    } else {
                        sBuffer.append(rdata.get(i).get("dbfield") + "=");
                        sBuffer.append("'" + rdata.get(i).get("value") + "'"
                                + " and ");
                    }
                }

            }
        }
        System.out.println("筛选字段：" + sBuffer.toString());
        return sBuffer.toString();
    }

    public Map<String, Object> getConditionKeyMap() {
        Map<String, Object> vMap = new HashMap<String, Object>();
        for (int i = 0; i < rdata.size(); i++) {
            if (!"所有".equals(rdata.get(i).get("value"))
                    && rdata.get(i).get("value") != null) {
                /** @注释：想想为什么用size=count来判断是否属于最后一项 */
                vMap.put(rdata.get(i).get("dbfield").toString(), rdata.get(i)
                        .get("value"));

            }
        }
        return vMap;
    }

    /**
     * @author LiuJie
     * @功能:根据Map集合值，恢复右边菜单
     */
    @SuppressWarnings("unchecked")
    public void initRightMenuStatus(Map<String, Object> kValue) {
        @SuppressWarnings("rawtypes")
        Iterator iter0 = kValue.entrySet().iterator();
        while (iter0.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry0 = (Map.Entry) iter0.next();
            String keyFeild = entry0.getKey().toString();
            String valueFeild = entry0.getValue().toString();
            for (int i = 0; i < rdata.size(); i++) {
                String dbfield = (String) rdata.get(i).get("dbfield");
                if (dbfield.endsWith(keyFeild)) {
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
                            entry.setValue(valueFeild);
                        }
                        /** @注释：页面显示 */
                        if (key.equals("selected")) {
                            entry.setValue(valueFeild);
                        }
                    }
                }
            }
            if (radapter != null) {
                radapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * @注释：获取筛选条件数据
     */
    public class GetConditionThread implements Runnable {

        @Override
        public void run() {
            getConditionData();
        }
    }

    @SuppressWarnings("unchecked")
    public void getConditionData() {
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

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (mDrawer.isMenuVisible()) {
//				mDrawer.closeMenu();
//				return true;
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}

}
