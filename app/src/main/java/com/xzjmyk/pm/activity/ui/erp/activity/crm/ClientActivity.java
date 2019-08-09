package com.xzjmyk.pm.activity.ui.erp.activity.crm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.config.VersionUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DatePicker;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.LargeValueFormatter;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.modular.apputils.utils.RecyclerItemDecoration;
import com.uas.appworks.CRM.erp.activity.AddBusinessActivity;
import com.uas.appworks.CRM.erp.activity.BusinessActivity;
import com.uas.appworks.CRM.erp.activity.BusinessAddActivity;
import com.uas.appworks.CRM.erp.activity.CustomerAddActivity;
import com.uas.appworks.CRM.erp.activity.CustomerListActivity;
import com.uas.appworks.CRM.erp.activity.SalesRankingActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.AddVisitReportActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OAActivity;
import com.xzjmyk.pm.activity.ui.erp.adapter.SellHonorAdapter;
import com.xzjmyk.pm.activity.ui.erp.model.SellHonorBean;
import com.xzjmyk.pm.activity.ui.erp.view.MyMarkerView;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.view.crouton.Style;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @功能:CRM 功能模块
 * @author:Arisono
 * @param:
 * @return:
 */
public class ClientActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.ib_business)
    private ImageButton ib_business;
    @ViewInject(R.id.ib_customer)
    private ImageButton ib_customer;
    @ViewInject(R.id.ib_customer_vistor)
    private ImageButton ib_customer_vistor;
    @ViewInject(R.id.ib_waller)
    private ImageButton ib_waller;

    @ViewInject(R.id.tv_rank_sale_left)
    private TextView tv_rank_sale_left;
    @ViewInject(R.id.tv_rank_sale_right)
    private TextView tv_rank_sale_right;
    @ViewInject(R.id.tv_rank_rirun_left)
    private TextView tv_rank_rirun_left;
    @ViewInject(R.id.subs_tv)
    private TextView subs_tv;
    @ViewInject(R.id.tv_rank_rirun_right)
    private TextView tv_rank_rirun_right;
    @ViewInject(R.id.tv_rank_visit_left)
    private TextView tv_rank_visit_left;
    @ViewInject(R.id.tv_rank_visit_right)
    private TextView tv_rank_visit_right;
    @ViewInject(R.id.tv_rank_income_left)
    private TextView tv_rank_income_left;
    @ViewInject(R.id.tv_rank_income_right)
    private TextView tv_rank_income_right;

    @ViewInject(R.id.tv_sale_customer)
    private TextView tv_sale_customer;
    @ViewInject(R.id.tv_sale_linkman)
    private TextView tv_sale_linkman;
    @ViewInject(R.id.tv_sale_business)
    private TextView tv_sale_business;
    @ViewInject(R.id.tv_sale_businessChange)
    private TextView tv_sale_businessChange;
    @ViewInject(R.id.tv_sale_order)
    private TextView tv_sale_order;
    @ViewInject(R.id.tv_sale_visit)
    private TextView tv_sale_visit;
    @ViewInject(R.id.tv_sale_chuhuo)
    private TextView tv_sale_chuhuo;
    @ViewInject(R.id.tv_sale_huikuan)
    private TextView tv_sale_huikuan;

    @ViewInject(R.id.tv_em_a)
    private TextView tv_em_a;
    @ViewInject(R.id.tv_em_b)
    private TextView tv_em_b;
    @ViewInject(R.id.tv_em_c)
    private TextView tv_em_c;

    @ViewInject(R.id.tv_em_plana)
    private TextView tv_em_plana;
    @ViewInject(R.id.tv_em_planb)
    private TextView tv_em_planb;
    @ViewInject(R.id.tv_em_planc)
    private TextView tv_em_planc;

    @ViewInject(R.id.tv_em_salea)
    private TextView tv_em_salea;
    @ViewInject(R.id.tv_em_saleb)
    private TextView tv_em_saleb;
    @ViewInject(R.id.tv_em_salec)
    private TextView tv_em_salec;

    @ViewInject(R.id.tv_cu_count)
    private TextView tv_cu_count;
    @ViewInject(R.id.tv_customer_name)
    private TextView tv_customer_name;
    @ViewInject(R.id.tv_customer_lastTime)
    private TextView tv_customer_lastTime;
    @ViewInject(R.id.tv_customer_days)
    private TextView tv_customer_days;

    @ViewInject(R.id.ib_ranking_condition)
    private TextView ib_ranking_condition;

    @ViewInject(R.id.ib_index_condition)
    private TextView ib_index_condition;

    @ViewInject(R.id.ll_subordinate)
    private LinearLayout ll_subordinate;

    @ViewInject(R.id.crm_chart_bar)
    private BarChart mChart;
    @ViewInject(R.id.crm_chart_sale)
    private BarChart sChart;

    @ViewInject(R.id.ll_data_empty)
    private LinearLayout ll_data_empty;
    @ViewInject(R.id.ll_data_empty1)
    private LinearLayout ll_data_empty1;

    @ViewInject(R.id.ll_data_one)
    private LinearLayout ll_data_one;
    @ViewInject(R.id.ll_data_two)
    private LinearLayout ll_data_two;
    @ViewInject(R.id.ll_data_three)
    private LinearLayout ll_data_three;
    @ViewInject(R.id.ll_data_more)
    private LinearLayout ll_data_more;

    @ViewInject(R.id.ll_customer_lost)
    private LinearLayout ll_customer_lost;

    private BarData data;
    private BarData data1;
    private ArrayList<BarDataSet> dataSets;
    private ArrayList<BarDataSet> dataSets1;
    private MyMarkerView mv;
    private float xZoom = 8f;
    private Typeface tf;
    @ViewInject(R.id.tv_huikuan)
    private TextView tv_huikuan;
    @ViewInject(R.id.tv_chuhuo)
    private TextView tv_chuhuo;
    private JSONArray subs;
    private String date;
    private JSONArray custs;
    @ViewInject(R.id.client_ptrsv)
    private PullToRefreshScrollView mPullToRefreshScrollView;
    private String[] mMonths;
    private String[] sales;
    private RecyclerView mHonorRecyclerView;
    private SellHonorAdapter mSellHonorAdapter;
    private List<SellHonorBean> mSellHonorBeans;
    private TextView mHonorEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ViewUtils.inject(this);
        initView();
        initData();
    }

    private void initView() {
        setTitle(R.string.sales_statistics);

        mHonorRecyclerView = findViewById(R.id.client_sell_honor_rv);
        mHonorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHonorRecyclerView.addItemDecoration(new RecyclerItemDecoration(1));
        mHonorRecyclerView.setNestedScrollingEnabled(false);

        mSellHonorBeans = new ArrayList<>();
        mSellHonorAdapter = new SellHonorAdapter(mSellHonorBeans);
        mHonorRecyclerView.setAdapter(mSellHonorAdapter);

        mHonorEmptyView = findViewById(R.id.client_sell_honor_empty_tv);

        initBarChart();
        initSaleBarChart();
        initListener();
        mMonths = getResources().getStringArray(R.array.month_list);
        sales = getResources().getStringArray(R.array.sale_list);
    }

    private void initListener() {
        ib_business.setOnClickListener(this);
        ib_customer.setOnClickListener(this);
        ib_waller.setOnClickListener(this);
        ib_customer_vistor.setOnClickListener(this);
        ib_ranking_condition.setOnClickListener(this);
        ib_index_condition.setOnClickListener(this);
        ll_data_more.setOnClickListener(this);
        ll_data_empty.setOnClickListener(this);

        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (CommonUtil.isNetWorkConnected(ClientActivity.this)) {
                    mHttpCount = 0;
                    initData();
                } else {
                    ViewUtil.ToastMessage(ClientActivity.this, getString(R.string.common_notlinknet), Style.holoRedLight, 2000);
                    mPullToRefreshScrollView.onRefreshComplete(1000);
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {

            }
        });
    }

    private void initData() {
        if (CommonUtil.isNetWorkConnected(ct)) {
            sendHttpResquest();
            isHasSubordinate(6);//下属

            HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(this),
                    new HttpParams.Builder()
                            .url("mobile/crm/getRankList.action")
                            .method(Method.POST)
                            .addParam("condition", "and to_char(pi_date,'yyyymm')=" + DateFormatUtil.getStrDate4Date(new Date(),
                                    "yyyyMM"))
                            .addHeader("Cookie", com.core.utils.CommonUtil.getErpCookie(this))
                            .build(), new HttpCallback() {
                        @Override
                        public void onSuccess(int flag, Object o) throws Exception {
                            try {
                                String result = o.toString();
                                JSONObject resultObject = JSON.parseObject(result);
                                JSONObject rankListObject = resultObject.getJSONObject("ranklist");
                                if (rankListObject != null) {
                                    JSONArray salesArray = rankListObject.getJSONArray("sales");
                                    JSONArray profitsArray = rankListObject.getJSONArray("profits");

                                    float s_id = salesArray.getFloatValue(0);
                                    String s_name = salesArray.getString(1);
                                    String s_position = salesArray.getString(2);
                                    String s_depart = salesArray.getString(3);
                                    int s_imid = salesArray.getIntValue(4);

                                    SellHonorBean salesHonor = new SellHonorBean();
                                    salesHonor.setId(s_id + "");
                                    salesHonor.setName(s_name);
                                    salesHonor.setPosition(s_position);
                                    salesHonor.setDepart(s_depart);
                                    salesHonor.setImid(s_imid + "");
                                    salesHonor.setDesc("销售总额冠军");

                                    float p_id = profitsArray.getFloatValue(0);
                                    String p_name = profitsArray.getString(1);
                                    String p_position = profitsArray.getString(2);
                                    String p_depart = profitsArray.getString(3);
                                    int p_imid = profitsArray.getIntValue(4);

                                    SellHonorBean profitsHonor = new SellHonorBean();
                                    profitsHonor.setId(p_id + "");
                                    profitsHonor.setName(p_name);
                                    profitsHonor.setPosition(p_position);
                                    profitsHonor.setDepart(p_depart);
                                    profitsHonor.setImid(p_imid + "");
                                    profitsHonor.setDesc("毛利润冠军");

                                    mSellHonorBeans.clear();
                                    mSellHonorBeans.add(salesHonor);
                                    mSellHonorBeans.add(profitsHonor);

                                    mSellHonorAdapter.notifyDataSetChanged();

                                    if (mSellHonorBeans.size() > 0) {
                                        mHonorEmptyView.setVisibility(View.GONE);
                                    } else {
                                        mHonorEmptyView.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    mHonorEmptyView.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                mHonorEmptyView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFail(int flag, String failStr) throws Exception {
                            mHonorEmptyView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            ToastUtil.showToast(ct, R.string.networks_out);
        }
    }


    private void initBarChart() {
        mChart.setDescription("");
        mChart.setMaxVisibleValueCount(60);
        mChart.setPinchZoom(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelsToSkip(0);
        xAxis.setAxisLineColor(getResources().getColor(R.color.light_gray));
        xAxis.setSpaceBetweenLabels(30);
        xAxis.setAxisLineWidth(0f);
        xAxis.setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);

        mChart.animateY(2500);
        tf = Typeface.createFromAsset(getAssets(),
                "OpenSans-Regular.ttf");
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setTypeface(tf);


        final YAxis leftAxis = mChart.getAxisLeft();
       /* leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value != 0) {
                    return String.valueOf(value);

                } else {

                    return "";
                }
            }
        });*/
        leftAxis.setTypeface(tf);
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(25f);
        leftAxis.setAxisMinValue(1f);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisLineColor(getResources().getColor(R.color.light_gray));
        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setEnabled(true);
        mChart.setMaxVisibleValueCount(10);
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();
        BarDataSet set1 = new BarDataSet(yVals1, getString(R.string.crmmain_dayasale));
        set1.setColor(Color.rgb(164, 228, 251));
        dataSets1 = new ArrayList<BarDataSet>();
        dataSets1.add(set1);
        data = new BarData(xVals, dataSets1);
        mChart.setData(data);
        mChart.setNoDataText(getString(R.string.crm_nodatas));
        mChart.invalidate();
    }


    private void initSaleBarChart() {
        sChart.setDescription("");
        sChart.setMaxVisibleValueCount(60);
        sChart.setPinchZoom(false);
        sChart.setDrawBarShadow(false);
        sChart.setDrawGridBackground(false);

        XAxis xAxis = sChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(30);
        xAxis.setLabelsToSkip(0);

        xAxis.setAxisLineColor(getResources().getColor(R.color.light_gray));
        xAxis.setAxisLineWidth(0f);
        xAxis.setDrawGridLines(false);
        sChart.getAxisLeft().setDrawGridLines(false);
        sChart.animateY(2500);
        tf = Typeface.createFromAsset(getAssets(),
                "OpenSans-Regular.ttf");
        Legend l = sChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setTypeface(tf);

        XAxis xl = sChart.getXAxis();
        xl.setTypeface(tf);


        final YAxis leftAxis = sChart.getAxisLeft();
     /*   leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value != 0) {
                    return String.valueOf(value);

                } else {

                    return "";
                }
            }
        });*/
        leftAxis.setTypeface(tf);
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(25f);
        leftAxis.setAxisMinValue(1f);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisLineColor(getResources().getColor(R.color.light_gray));
        sChart.getAxisRight().setEnabled(false);
        sChart.getAxisLeft().setEnabled(true);
        sChart.setMaxVisibleValueCount(10);
        sChart.setDoubleTapToZoomEnabled(false);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();
        BarDataSet set1 = new BarDataSet(yVals1, getString(R.string.crmmain_dayasale));
        set1.setColor(Color.rgb(164, 228, 251));
        dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        data = new BarData(xVals, dataSets);
        sChart.setNoDataText(" ");
        sChart.setData(data);
        sChart.setNoDataText(getString(R.string.crm_nodatas));
        sChart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String master = CommonUtil.getMaster();
        if (!("DATACENTER".equals(master) || "N_SHYZ".equals(master) || "N_AJC".equals(master))) {
            getMenuInflater().inflate(R.menu.menu_crm, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crm_add:
                View view = getWindow().findViewById(item.getItemId());
                showPopupWindow(view);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void setData(int count, float[] ydata) {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add(mMonths[i % 4]);
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        float maxHight = 0;
        for (int i = 0; i < count; i++) {
            float val = ydata[i];
            yVals1.add(new BarEntry(val, i));
            if (maxHight < ydata[i]) {
                maxHight = ydata[i];
            }
        }
        mChart.getAxisLeft().setAxisMaxValue(maxHight + 10);
        BarDataSet set1 = new BarDataSet(yVals1, getString(R.string.crm_Monthly_sales_unit));
        int[] JOYFUL_COLORS = {
                Color.rgb(121, 191, 174), Color.rgb(91, 161, 209)
        };
        set1.setColors(JOYFUL_COLORS);
        //set1.setVisible(false);
        //set1.setDrawValues(false);
        set1.setValueFormatter(new ValueFormatter() {
            private DecimalFormat mFormat;

            public void DefaultValueFormatter(int digits) {

                StringBuffer b = new StringBuffer();
                for (int i = 0; i < digits; i++) {
                    if (i == 0)
                        b.append(".");
                    b.append("0");
                }
                mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
            }

            @Override
            public String getFormattedValue(float value) {
                try {
                    if (value == 0) {
                        return "";
                    } else {
                        return new DecimalFormat("#,##").format(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }
        });
        dataSets.clear();
        dataSets.add(set1);

        data = new BarData(xVals, dataSets);
        data.setDrawValues(true);
        mChart.setData(data);
        /**@注释：清空任何缩放  */
//        mChart.zoom(0, 0, 0, 0);
//        mChart.zoom(ydata.length / 4.2f, 0, 0, 0);
        mChart.invalidate();
    }


    private void setData1(int count, float[] ydata) {
        ArrayList<String> xVals = new ArrayList<String>();
        String[] sale_list = getResources().getStringArray(R.array.sale_list);
        xVals.add(sale_list[0]);
        xVals.add(sale_list[2]);
        xVals.add(sale_list[4]);
        xVals.add(sale_list[6]);
//        for (int i = 0; i < count; i++) {
//            xVals.add(sales[i % 12]);
//        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        try {
            for (int i = 0; i < count; i++) {
                float val = ydata[i];
                yVals1.add(new BarEntry(val, i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BarDataSet set1 = new BarDataSet(yVals1, getString(R.string.crm_unit_one));
        int[] JOYFUL_COLORS = {
                Color.rgb(121, 191, 174), Color.rgb(91, 161, 209)
        };
        set1.setColors(JOYFUL_COLORS);
        //set1.setVisible(false);
        //set1.setDrawValues(false);
        set1.setValueFormatter(new ValueFormatter() {
            private DecimalFormat mFormat;

            public void DefaultValueFormatter(int digits) {

                StringBuffer b = new StringBuffer();
                for (int i = 0; i < digits; i++) {
                    if (i == 0)
                        b.append(".");
                    b.append("0");
                }

                mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
            }

            @Override
            public String getFormattedValue(float value) {
                try {
                    if (value == 0) {
                        return "";
                    } else {
                        return new DecimalFormat("###,###,##0").format(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }
        });
        dataSets1.clear();
        dataSets1.add(set1);

        data1 = new BarData(xVals, dataSets1);
        data1.setDrawValues(true);

        sChart.setData(data1);
        sChart.invalidate();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_business:
                startActivity(new Intent(this, BusinessActivity.class));
                break;
            case R.id.ib_customer:
                startActivity(new Intent(this, CustomerListActivity.class));
                break;

            case R.id.ib_customer_vistor:
                startActivity(new Intent(this, OAActivity.class).putExtra("type", 1));
                break;
            case R.id.ib_waller:
                startActivity(new Intent(this, SalesRankingActivity.class));
                break;
         /*   case R.id.ib_sale_condition:
                showDateDialog(R.id.ib_sale_condition);
                break;*/
            case R.id.ib_index_condition:
                showDateDialog(R.id.ib_index_condition);
                break;
            case R.id.ib_ranking_condition:
                showDateDialog(R.id.ib_ranking_condition);
                break;
            case R.id.ll_data_empty://当点击更多
                //判断是否可以进去
                if (subs != null && subs.size() > 3) {
                    Intent intent = new Intent(ct, SubsActivity.class);
                    intent.putExtra("type", 1);
                    intent.putExtra("data", subs.toString());
                    startActivity(intent);
                }
                break;
            case R.id.ll_data_more:
                if (custs != null && custs.size() > 0) {
                    Intent intent = new Intent(ct, SubsActivity.class);
                    intent.putExtra("data", custs.toString());
                    intent.putExtra("type", 2);
                    startActivity(intent);
                }
                break;
         /*   case R.id.ib_em_condition:
                showDateDialog(R.id.ib_em_condition);
                break;*/
        }
    }

    private int mHttpCount = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    mHttpCount++;
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    break;
                case INIT_PersonalRank://排行
                    mHttpCount++;
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage PersonalRank:" + msg.getData().getString("result"));
                    JSONObject root = JSON.parseObject(msg.getData().getString("result")).getJSONObject("datas");
                    JSONArray sales = root.getJSONArray("sales");
                    JSONArray visits = root.getJSONArray("visits");
                    JSONArray receivables = root.getJSONArray("receivables");
                    JSONArray profits = root.getJSONArray("profits");
                    boolean isInFor = false;
                    if (sales != null && !sales.isEmpty()) {
                        for (int i = 0; i < sales.size(); i++) {
                            JSONArray array = sales.getJSONArray(i);
                            if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_username")) && !StringUtil.isEmpty(array.getString(2))
                                    && array.getString(2).equals(CommonUtil.getSharedPreferences(ct, "erp_username"))) {
                                CommonUtil.textAarrySpanForStyle(tv_rank_sale_left,
                                        getString(R.string.crm_di) + array.get(3) + getString(R.string.crm_ming)
                                                + ""
                                        , new String[]{
                                                String.valueOf(array.get(3))},
                                        ct.getResources().getColor(R.color.red));
                                CommonUtil.textAarrySpanForStyle(tv_rank_sale_right,
                                        array.get(0) + getString(R.string.crm_wyuan)
                                        , new String[]{
                                                String.valueOf(array.get(0))},
                                        ct.getResources().getColor(R.color.gray));
                                isInFor = true;
                                break;
                            }
                        }
                        if (!isInFor) {
                            CommonUtil.textAarrySpanForStyle(tv_rank_sale_left,
                                    getString(R.string.crm_di) + "__" + getString(R.string.crm_ming),
                                    new String[]{"__"}, ct.getResources().getColor(R.color.gray)
                            );
                            CommonUtil.textAarrySpanForStyle(tv_rank_sale_right,
                                    "__" + getString(R.string.crm_wyuan)
                                    , new String[]{"__"},
                                    ct.getResources().getColor(R.color.gray));
                        }
                    } else {
                        CommonUtil.textAarrySpanForStyle(tv_rank_sale_left,
                                getString(R.string.crm_di) + "__" + getString(R.string.crm_ming),
                                new String[]{"__"}, ct.getResources().getColor(R.color.gray)
                        );
                        CommonUtil.textAarrySpanForStyle(tv_rank_sale_right,
                                "__" + getString(R.string.crm_wyuan)
                                , new String[]{"__"},
                                ct.getResources().getColor(R.color.gray));
                    }//end 销售额

                    isInFor = false;
                    if (visits != null && !visits.isEmpty()) {
                        for (int i = 0; i < visits.size(); i++) {
                            JSONArray array = visits.getJSONArray(i);
                            if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_username")) && !StringUtil.isEmpty(array.getString(1))
                                    && array.getString(1).equals(CommonUtil.getSharedPreferences(ct, "erp_username"))) {
                                CommonUtil.textAarrySpanForStyle(tv_rank_visit_left,
                                        getString(R.string.crm_di) + array.get(3).toString()
                                                + getString(R.string.crm_ming)
                                        , new String[]{array.get(3).toString()},
                                        ct.getResources().getColor(R.color.red));

                                CommonUtil.textAarrySpanForStyle(tv_rank_visit_right, array.getIntValue(2)
                                                + getString(R.string.crm_ge)
                                        , new String[]{String.valueOf(array.getIntValue(2))},
                                        ct.getResources().getColor(R.color.gray));
                                isInFor = true;
                                break;
                            }
                        }
                        if (!isInFor) {
                            CommonUtil.textAarrySpanForStyle(tv_rank_visit_left,
                                    getString(R.string.crm_di_ming),
                                    new String[]{"__"}
                                    , ct.getResources().getColor(R.color.gray));
                            CommonUtil.textAarrySpanForStyle(tv_rank_visit_right,
                                    "__" + getString(R.string.crm_ge)
                                    , new String[]{"__"},
                                    ct.getResources().getColor(R.color.gray));
                        }
                    } else {
                        CommonUtil.textAarrySpanForStyle(tv_rank_visit_left,
                                getString(R.string.crm_di_ming),
                                new String[]{"__"}
                                , ct.getResources().getColor(R.color.gray));
                        CommonUtil.textAarrySpanForStyle(tv_rank_visit_right,
                                "__" + getString(R.string.crm_ge)
                                , new String[]{"__"},
                                ct.getResources().getColor(R.color.gray));
                    }

                    if (profits != null && !profits.isEmpty()) {
                        if (profits.size() == 2) {
                            CommonUtil.textAarrySpanForStyle(tv_rank_rirun_left,
                                    getString(R.string.crm_di) + profits.getJSONArray(1).get(3).toString()
                                            + getString(R.string.crm_ming)
                                    , new String[]{
                                            String.valueOf(profits.getJSONArray(1).get(3).toString())
                                    }, ct.getResources().getColor(R.color.red));
                            CommonUtil.textAarrySpanForStyle(tv_rank_rirun_right, profits.getJSONArray(1).get(0)
                                            + getString(R.string.crm_wyuan)
                                    , new String[]{
                                            String.valueOf(profits.getJSONArray(0).get(0))
                                    }, ct.getResources().getColor(R.color.gray));
                        } else {
                            if (profits.getJSONArray(0).getString(2).equals(CommonUtil.getSharedPreferences(ct, "erp_username"))) {
                                CommonUtil.textAarrySpanForStyle(tv_rank_rirun_left,
                                        getString(R.string.crm_di) + profits.getJSONArray(0).get(3).toString()
                                                + getString(R.string.crm_ming)
                                        , new String[]{
                                                String.valueOf(profits.getJSONArray(0).get(3).toString())
                                        }, ct.getResources().getColor(R.color.red));

                                CommonUtil.textAarrySpanForStyle(tv_rank_rirun_right, profits.getJSONArray(0).get(0) +
                                                getString(R.string.crm_wyuan)
                                        , new String[]{
                                                String.valueOf(profits.getJSONArray(0).get(0))
                                        }, ct.getResources().getColor(R.color.gray));

                            } else {
                                CommonUtil.textAarrySpanForStyle(tv_rank_rirun_left,
                                        getString(R.string.crm_di) + "__" + getString(R.string.crm_ming),
                                        new String[]{"__"}, ct.getResources().getColor(R.color.gray)
                                );
                                CommonUtil.textAarrySpanForStyle(tv_rank_rirun_right,
                                        "__" + getString(R.string.crm_wyuan)
                                        , new String[]{"__"},
                                        ct.getResources().getColor(R.color.gray));
                            }
                        }
                    } else {
                        CommonUtil.textAarrySpanForStyle(tv_rank_rirun_left,
                                getString(R.string.crm_di) + "__" + getString(R.string.crm_ming),
                                new String[]{"__"}, ct.getResources().getColor(R.color.gray)
                        );
                        CommonUtil.textAarrySpanForStyle(tv_rank_rirun_right,
                                "__" + getString(R.string.crm_wyuan)
                                , new String[]{"__"},
                                ct.getResources().getColor(R.color.gray));
                    }
                    if (receivables != null && !receivables.isEmpty()) {
                        if (receivables.size() == 2) {
                            CommonUtil.textAarrySpanForStyle(tv_rank_income_left,
                                    getString(R.string.crm_di) + receivables.getJSONArray(1).get(3).toString() +
                                            getString(R.string.crm_ming)
                                    , new String[]{
                                            String.valueOf(receivables.getJSONArray(1).get(3).toString())},
                                    ct.getResources().getColor(R.color.red));
                            CommonUtil.textAarrySpanForStyle(tv_rank_income_right, receivables.getJSONArray(1).get(0)
                                            + getString(R.string.crm_wyuan)
                                    , new String[]{String.valueOf(receivables.getJSONArray(1).get(0))},
                                    ct.getResources().getColor(R.color.gray));
                        } else {
                            if (receivables.getJSONArray(0).getString(2).equals(CommonUtil.getSharedPreferences(ct, "erp_username"))) {
                                CommonUtil.textAarrySpanForStyle(tv_rank_income_left,
                                        getString(R.string.crm_di) + receivables.getJSONArray(0).get(3).toString() + getString(R.string.crm_ming)
                                        , new String[]{receivables.getJSONArray(0).get(3).toString()},
                                        ct.getResources().getColor(R.color.red));

                                CommonUtil.textAarrySpanForStyle(tv_rank_income_right, receivables.getJSONArray(0).get(0)
                                                + getString(R.string.crm_wyuan)
                                        , new String[]{
                                                String.valueOf(receivables.getJSONArray(0).get(0))},
                                        ct.getResources().getColor(R.color.gray));
                            } else {

                                CommonUtil.textAarrySpanForStyle(tv_rank_income_left,
                                        getString(R.string.crm_di) + "__" + getString(R.string.crm_ming),
                                        new String[]{"__"}, ct.getResources().getColor(R.color.gray)
                                );
                                CommonUtil.textAarrySpanForStyle(tv_rank_income_right,
                                        "__" + getString(R.string.crm_wyuan)
                                        , new String[]{"__"},
                                        ct.getResources().getColor(R.color.gray));
                            }

                        }
                    } else {
                        CommonUtil.textAarrySpanForStyle(tv_rank_income_left,
                                getString(R.string.crm_di) + "__" + getString(R.string.crm_ming),
                                new String[]{"__"}, ct.getResources().getColor(R.color.gray)
                        );
                        CommonUtil.textAarrySpanForStyle(tv_rank_income_right,
                                "__" + getString(R.string.crm_wyuan)
                                , new String[]{"__"},
                                ct.getResources().getColor(R.color.gray));
                    }
                    break;
                case INIT_SalesKit://销售
                    mHttpCount++;
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage SalesKit:" + msg.getData().getString("result"));
                    root = JSON.parseObject(msg.getData().getString("result")).getJSONObject("datas");
                    float[] ydata1 = {0, 0, 0, 0};
                    if (root != null) {
                        ydata1[0] = root.getIntValue("customercount");
//                        ydata1[1] = root.getIntValue("contactcount");
                        ydata1[1] = root.getIntValue("nichecount");
//                        ydata1[3] = root.getIntValue("nichechangecount");
                        ydata1[2] = root.getIntValue("salecount");
//                        ydata1[5] = root.getJSONArray("visit").getIntValue(1);
                        ydata1[3] = root.getJSONArray("visit").getIntValue(0);
                        tv_chuhuo.setText(getString(R.string.crm_Shipments) + root.getFloatValue("saleamount") + getString(R.string.crm_wyuan));
                        tv_huikuan.setText(getString(R.string.crm_Back_money) + root.getFloatValue("backamount") + getString(R.string.crm_wyuan));
                        float maxHeight = 0;
                        for (int i = 0; i < ydata1.length; i++) {
                            Log.i(TAG, "handleMessage:ydata1[" + i + "]=" + ydata1[i]);
                            if (maxHeight < ydata1[i]) {
                                maxHeight = ydata1[i];
                            }

                        }
                        sChart.getAxisLeft().setAxisMaxValue(maxHeight + 10);
                        setData1(4, ydata1);
                    } else {
                        setData1(4, ydata1);
                        tv_chuhuo.setText(getString(R.string.crm_Shipments) + root.getIntValue("saleamount") + getString(R.string.crm_wyuan));
                        tv_huikuan.setText(getString(R.string.crm_Back_money) + root.getIntValue("backamount") + getString(R.string.crm_wyuan));
                    }
                    break;
                case INIT_Targets://指标
                    mHttpCount++;
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage Targets:" + msg.getData().getString("result"));
                    root = JSON.parseObject(msg.getData().getString("result")).getJSONObject("datas");
                    //解析指标和下属
                    if (!root.getJSONArray("target").isEmpty()) {
                        JSONObject target = root.getJSONArray("target").getJSONObject(0);
                        if (date.equals(DateFormatUtil.getStrDate4Date(new Date(), "yyyyMM")))
                            tv_em_a.setText(target.getString("CUSTOMERCOUNT"));
                        float[] ydata = {0, 0, 0, 0};
                        ydata[1] = target.getIntValue("RANK");
                        float firstbfcount = target.getFloatValue("FIRSTBFCOUNT");
                        BigDecimal bigDecimal = new BigDecimal(firstbfcount);
                        ydata[0] = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                        ydata[3] = target.getIntValue("TOPCOUNT");
                        bigDecimal = new BigDecimal(target.getFloatValue("ACTUALPROFIT"));
                        ydata[2] = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                        setData(4, ydata);
                    } else {
                        float[] ydata = {0, 0, 0, 0};
                        setData(4, ydata);
                    }
                    if (!date.equals(DateFormatUtil.getStrDate4Date(new Date(), "yyyyMM"))) return;
                    subs = root.containsKey("subs") ? root.getJSONArray("subs") : new JSONArray();
                    if (!subs.isEmpty()) {
                        int size = subs.size();
                        ll_data_one.setVisibility(View.GONE);
                        ll_data_two.setVisibility(View.GONE);
                        ll_data_three.setVisibility(View.GONE);
                        switch (size) {
                            case 3:
                                tv_em_c.setText(subs.getJSONObject(2).getString("CUSTOMERCOUNT"));
                                ll_data_three.setVisibility(View.VISIBLE);
                                tv_em_salec.setText(getFloat(subs.getJSONObject(2).getFloatValue("FIRSTBFCOUNT"))
                                        + "/" + getFloat(subs.getJSONObject(2).getFloatValue("ACTUALPROFIT")));
                                tv_em_planc.setText(getFloat(subs.getJSONObject(2).getIntValue("RANK"))
                                        + "/" + getFloat(subs.getJSONObject(2).getIntValue("TOPCOUNT")));
                            case 2:
                                tv_em_b.setText(subs.getJSONObject(1).getString("CUSTOMERCOUNT"));
                                ll_data_two.setVisibility(View.VISIBLE);
                                tv_em_saleb.setText(getFloat(subs.getJSONObject(1).getFloatValue("FIRSTBFCOUNT"))
                                        + "/" + getFloat(subs.getJSONObject(1).getFloatValue("ACTUALPROFIT")));
                                tv_em_planb.setText(getFloat(subs.getJSONObject(1).getIntValue("RANK"))
                                        + "/" + getFloat(subs.getJSONObject(1).getIntValue("TOPCOUNT")));
                            case 1:
                                tv_em_a.setText(subs.getJSONObject(0).getString("CUSTOMERCOUNT"));
                                ll_data_one.setVisibility(View.VISIBLE);
                                tv_em_salea.setText(getFloat(subs.getJSONObject(0).getFloatValue("FIRSTBFCOUNT"))
                                        + "/" + getFloat(subs.getJSONObject(0).getFloatValue("ACTUALPROFIT")));
                                tv_em_plana.setText(getFloat(subs.getJSONObject(0).getIntValue("RANK"))
                                        + "/" + getFloat(subs.getJSONObject(0).getIntValue("TOPCOUNT")));
                                break;
                            default:
                                break;
                        }
                        if (size > 3) {
                            tv_em_c.setText(subs.getJSONObject(2).getString("CUSTOMERCOUNT"));
                            tv_em_b.setText(subs.getJSONObject(1).getString("CUSTOMERCOUNT"));
                            tv_em_a.setText(subs.getJSONObject(0).getString("CUSTOMERCOUNT"));
                            ll_data_one.setVisibility(View.VISIBLE);
                            ll_data_two.setVisibility(View.VISIBLE);
                            ll_data_three.setVisibility(View.VISIBLE);
                            tv_em_salec.setText(getFloat(subs.getJSONObject(2).getFloatValue("FIRSTBFCOUNT"))
                                    + "/" + getFloat(subs.getJSONObject(2).getFloatValue("ACTUALPROFIT")));
                            tv_em_planc.setText(getFloat(subs.getJSONObject(2).getIntValue("RANK"))
                                    + "/" + getFloat(subs.getJSONObject(2).getIntValue("TOPCOUNT")));
                            tv_em_saleb.setText(getFloat(subs.getJSONObject(1).getFloatValue("FIRSTBFCOUNT"))
                                    + "/" + getFloat(subs.getJSONObject(1).getFloatValue("ACTUALPROFIT")));
                            tv_em_planb.setText(getFloat(subs.getJSONObject(1).getIntValue("RANK"))
                                    + "/" + getFloat(subs.getJSONObject(1).getIntValue("TOPCOUNT")));
                            tv_em_salea.setText(getFloat(subs.getJSONObject(0).getFloatValue("FIRSTBFCOUNT"))
                                    + "/" + getFloat(subs.getJSONObject(0).getFloatValue("ACTUALPROFIT")));
                            tv_em_plana.setText(getFloat(subs.getJSONObject(0).getIntValue("RANK"))
                                    + "/" + getFloat(subs.getJSONObject(0).getIntValue("TOPCOUNT")));
                            ll_data_empty.setVisibility(View.VISIBLE);
                            subs_tv.setText(getString(R.string.click_more));
                        } else
                            ll_data_empty.setVisibility(View.GONE);
                    } else {
                        ll_data_empty.setVisibility(View.VISIBLE);
                        subs_tv.setText(getString(R.string.crm_nodatas));
                        ll_data_one.setVisibility(View.GONE);
                        ll_data_two.setVisibility(View.GONE);
                        ll_data_three.setVisibility(View.GONE);
                    }
                    break;
                case INIT_InactionCusts:
                    mHttpCount++;
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage InactionCusts:" + msg.getData().getString("result"));
                    String message = msg.getData().getString("result");
                    if (JSON.parseObject(message).get("datas") instanceof JSONObject) {
                        root = JSON.parseObject(message).getJSONObject("datas");
                        custs = root.getJSONArray("cusdatas");
                        if (custs != null && custs.size() > 0) {
                            tv_customer_name.setText(getValues(custs.getJSONArray(0), 0));
                            String time = getValues(custs.getJSONArray(0), 1);
                            tv_customer_lastTime.setText(time);
                            if (time.length() > 5)//容cuo
                            {
                                int i = (int) ((System.currentTimeMillis() - DateFormatUtil.str2Long(time, DateFormatUtil.YMD)) / (60 * 1000 * 24 * 60));
                                tv_customer_days.setText(getString(R.string.length_last_gj) + i + getString(R.string.common_day));
                            }
                            ll_data_empty1.setVisibility(View.GONE);
                            ll_data_more.setVisibility(View.VISIBLE);
                            ll_customer_lost.setVisibility(View.VISIBLE);
                        } else {
                            //无数据处理
                            ll_customer_lost.setVisibility(View.GONE);
                            ll_data_empty1.setVisibility(View.VISIBLE);
                            ll_data_more.setVisibility(View.GONE);
                        }
                    } else {
                        //无数据处理
                        ll_customer_lost.setVisibility(View.GONE);
                        ll_data_empty1.setVisibility(View.VISIBLE);
                        ll_data_more.setVisibility(View.GONE);
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    progressDialog.dismiss();
                    if (mPullToRefreshScrollView.isRefreshing()) {
                        mPullToRefreshScrollView.onRefreshComplete();
                    }
                    break;
                case 6:
                    mHttpCount++;
                    JSONArray array = JSON.parseObject(msg.getData().getString("result")).getJSONArray("datas");
                    if (array != null) {
                        if (array.size() > 0) {
                            //显示下属面板
                            Log.i("raomeng", msg.getData().getString("result"));
                            ll_subordinate.setVisibility(View.VISIBLE);
                        } else {
                            //隐藏下属面板
                            Log.i("raomeng", msg.getData().getString("result"));
                            ll_subordinate.setVisibility(View.GONE);
                        }
                    }
                    break;
            }
            if (mPullToRefreshScrollView.isRefreshing()) {
                mPullToRefreshScrollView.onRefreshComplete();
            }
//            未知请求次数达到限制出现网络未连接的提示，先关闭
//            if (mHttpCount > 4) {
//                if (mPullToRefreshScrollView.isRefreshing()) {
//                    ViewUtil.ToastMessage(ClientActivity.this, getString(R.string.common_notlinknet), Style.holoGreenLight, 2000);
//                    mPullToRefreshScrollView.onRefreshComplete();
//                }
//            }
        }
    };

    private final int INIT_PersonalRank = 2;//排名
    private final int INIT_SalesKit = 3;//销售
    private final int INIT_Targets = 4;//指标
    private final int INIT_InactionCusts = 5;//遗忘客户


    //获取浮点型后两位
    private String getFloat(float f) {
        if (f < 1f) {
            return f + "";
        }
        DecimalFormat df = new DecimalFormat(".##");
        return df.format(f);
    }

    private String getValues(JSONArray array, int i) {
        return array.getString(i) == null ? "" : array.getString(i);
    }

    private void sendHttpResquest() {
        sendInactionCusts();
        date = DateFormatUtil.getStrDate4Date(new Date(), "yyyyMM");
        sendPersonalRank(date);
        sendSalesKit(date);
        sendTargets(date);
    }

    private void sendInactionCusts() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getInactionCusts.action";
        Map<String, Object> params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("page", "1");
        params.put("pageSize", "10");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, INIT_InactionCusts, null, null, "post");
    }

    private void sendTargets(String date) {
        this.date = date;
        progressDialog.show();
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        String url;
        Map<String, Object> params;
        url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getTargets.action";
        params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("yearmonth", date);
        params.put("page", "1");
        params.put("pageSize", "10");
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, INIT_Targets, null, null, "post");
    }

    private void sendSalesKit(String date) {
        progressDialog.show();
        String url;
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        Map<String, Object> params;
        url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getSalesKit.action";
        params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("yearmonth", date);
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, INIT_SalesKit, null, null, "post");
    }

    private void sendPersonalRank(String date) {
        progressDialog.show();
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        String url;
        Map<String, Object> params;
        url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getPersonalRank.action";
        params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("yearmonth", date);
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, INIT_PersonalRank, null, null, "post");
    }


    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getPopData(),
                    R.layout.item_pop_list,
                    new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {

                        case 0:
                            if (VersionUtil.canShowCrm2_0() && !CommonUtil.isBiteman())
                                startActivity(new Intent(ClientActivity.this, AddBusinessActivity.class));
                            else
                                startActivity(new Intent(ClientActivity.this, BusinessAddActivity.class));
                            break;
                        case 1:
                            startActivity(new Intent(ClientActivity.this, CustomerAddActivity.class));
                            break;
                        case 3:
                            break;
                        case 2:
                            startActivity(getVisitClass(ct));
                            break;

                    }
                    closePoppupWindow();
                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() / 3, windowManager.getDefaultDisplay().getHeight() / 3);
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ClientActivity.this, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
    }

    private void closePoppupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }


    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        /*map.put("item_name", "新建联系人");
        list.add(map);*/

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.crm_creat_business));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.crm_add_business));
        list.add(map);
        
        
      /*  map = new HashMap<String, Object>();
        map.put("item_name", "新增任务");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("item_name", "新增日程");
        list.add(map);*/

        /*map = new HashMap<String, Object>();
        map.put("item_name", "新增分享");
        list.add(map);*/

//        map = new HashMap<String, Object>();
//        map.put("item_name", "新增拜访计划");
//        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.crm_add_visitrecord));
        list.add(map);

     /*   map = new HashMap<String, Object>();
        map.put("item_name", "录入客户");
        list.add(map);*/
        return list;
    }


    private void showDateDialog(final int resId) {
        DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
        picker.setRange(1950, 2030);
        //Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        picker.setSelectedItem(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1);
        picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
            @Override
            public void onDatePicked(String year, String month) {
//                        ToastUtil.showToast(ct, year + "-" + month + "-" + day);
                switch (resId) {
                    case R.id.ib_ranking_condition://排名
                        // ToastUtil.showToast(ct, year + "-" + month);
                        if (CommonUtil.isNetWorkConnected(ct)) {
                            ib_ranking_condition.setText(month + "月");
                            sendPersonalRank(year + month);
                        } else {
                            ToastUtil.showToast(ct, R.string.networks_out);
                        }

                        break;
                    case R.id.ib_index_condition://图表
                        //  ToastUtil.showToast(ct, year + "-" + month);
                        if (CommonUtil.isNetWorkConnected(ct)) {
                            ib_index_condition.setText(month + "月");
                            sendTargets(year + month);
                        } else {
                            ToastUtil.showToast(ct, R.string.networks_out);
                        }

                        break;
                  /*  case R.id.ib_sale_condition://销售
                        ToastUtil.showToast(ct, year + "-" + month );
                        sendSalesKit(year+month);
                        break;*/
                 /*   case R.id.ib_em_condition://下属
                        sendTargets(year+month);
                        break;*/
                }
            }
        });
        picker.show();
    }


    private void isHasSubordinate(int what) {
        progressDialog.show();
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "mobile/crm/getstaffmsg.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, what, null, null, "get");
    }

    public Intent getVisitClass(Context ct) {
        return new Intent(ct, VersionUtil.canShowCrm2_0() ? AddVisitReportActivity.class : VisitReportAddActivity.class);
    }
}

