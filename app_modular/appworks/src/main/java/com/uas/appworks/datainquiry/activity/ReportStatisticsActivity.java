package com.uas.appworks.datainquiry.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.DrawableCenterTextView;
import com.core.widget.EmptyLayout;
import com.core.widget.view.MyGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.adapter.DataInquiryMenuGridAdapter;
import com.uas.appworks.datainquiry.adapter.ReportStatisticsMenuListAdapter;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;
import com.uas.appworks.datainquiry.bean.GridMenuReportStatisticsBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2017/8/3.
 * 报表统计业务菜单页面
 */
public class ReportStatisticsActivity extends BaseActivity {
    private final int GET_MENU_DATA = 0x16;
    private PullToRefreshListView mMenuListView;
    private List<GridMenuReportStatisticsBean> mGridMenuReportStatisticsBeans;
    private ReportStatisticsMenuListAdapter mReportStatisticsMenuListAdapter;
    private DataInquiryMenuGridAdapter mDataInquiryMenuGridAdapter;
    private List<DataInquiryGirdItemBean> mDataInquiryGirdItemBeans;
    private LinearLayout mRecentBrowseLl;
    private View mReportStatisticsModulView;
    private TextView mReportStatisticsModulTitleTv;
    private MyGridView mReportStatisticsModulGv;
    private DrawableCenterTextView mSearchTextView;
    private String mCurrentMaster;
    private String mCurrentUser;
    private EmptyLayout mEmptyLayout;
    boolean flag = false;
    private int[] mColors = new int[]{R.color.data_inquiry_gird_menu_color1, R.color.data_inquiry_gird_menu_color2
            , R.color.data_inquiry_gird_menu_color3, R.color.data_inquiry_gird_menu_color4, R.color.data_inquiry_gird_menu_color5,
            R.color.data_inquiry_gird_menu_color6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_inquiry);
        setTitle("报表统计");

        initViews();
        initEvents();
        initDatas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRecentHeader();
    }

    private void initDatas() {
        String reportQueryMenuCache = CommonUtil.getSharedPreferences(this,
                mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.REPORT_QUERY_MENU_CACHE);
        if (reportQueryMenuCache != null) {
            try {
                JSONObject resultObject = new JSONObject(reportQueryMenuCache);
                JSONArray dataArray = resultObject.optJSONArray("data");
                if (dataArray == null || dataArray.length() == 0) {
                    if (!CommonUtil.isNetWorkConnected(this)) {
                        ToastMessage(getString(R.string.networks_out));
                    } else {
                        progressDialog.show();
                        getMenuData();
                    }
                } else {
                    analysisMenuData(reportQueryMenuCache);
                }
            } catch (Exception e) {

            }
        } else {
            if (!CommonUtil.isNetWorkConnected(this)) {
                ToastMessage(getString(R.string.networks_out));
            } else {
                progressDialog.show();
                getMenuData();
            }
        }
    }

    private void initEvents() {
        mMenuListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!CommonUtil.isNetWorkConnected(ReportStatisticsActivity.this)) {
                    ToastMessage(getString(R.string.networks_out));
                    if (mMenuListView.isRefreshing()) {
                        mMenuListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMenuListView.onRefreshComplete();
                            }
                        }, 500);
                    }
                } else {
                    flag = true;
                    getMenuData();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mReportStatisticsModulGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataInquiryGirdItemBean dataInquiryGirdItemBean = mDataInquiryMenuGridAdapter.getObjects().get(position);
                String iconText = dataInquiryGirdItemBean.getIconText();
                String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(ReportStatisticsActivity.this,
                        mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE);
                List<DataInquiryGirdItemBean> recentBrowse = new ArrayList<DataInquiryGirdItemBean>();
                if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
                    try {
                        recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);

                        for (int i = 0; i < recentBrowse.size(); i++) {
                            if (iconText != null && iconText.equals(recentBrowse.get(i).getIconText())) {
                                recentBrowse.remove(i);
                            }
                        }
                    } catch (Exception e) {

                    }
                }

                recentBrowse.add(0, dataInquiryGirdItemBean);

                String recentJson = JSON.toJSON(recentBrowse).toString();
                CommonUtil.setSharedPreferences(ReportStatisticsActivity.this
                        , mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE
                        , recentJson);

//                mDataInquiryGirdItemBeans.clear();
//                mDataInquiryGirdItemBeans.addAll(recentBrowse);
//                mDataInquiryMenuGridAdapter.notifyDataSetChanged();

                for (int i = 0; i < mGridMenuReportStatisticsBeans.size(); i++) {
                    List<GridMenuReportStatisticsBean.ListBean> reportlist = mGridMenuReportStatisticsBeans.get(i).getList();
                    for (int j = 0; j < reportlist.size(); j++) {
                        if (dataInquiryGirdItemBean.getIconText().equals(reportlist.get(j).getTitle())) {
                            Intent intent = new Intent();
                            intent.setClass(ReportStatisticsActivity.this, ReportQueryCriteriaActivity.class);
                            intent.putExtra("reportinfo", reportlist.get(j));
                            ReportStatisticsActivity.this.startActivity(intent);
                            return;
                        }
                    }
                }
            }
        });

        mSearchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ReportStatisticsActivity.this, ReportQuerySearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        mMenuListView = (PullToRefreshListView) findViewById(R.id.data_inquiry_menu_lv);
        mSearchTextView = (DrawableCenterTextView) findViewById(R.id.data_inquiry_menu_search_tv);

        mGridMenuReportStatisticsBeans = new ArrayList<>();
        mReportStatisticsMenuListAdapter = new ReportStatisticsMenuListAdapter(this, mGridMenuReportStatisticsBeans);
        mMenuListView.setAdapter(mReportStatisticsMenuListAdapter);

        mRecentBrowseLl = (LinearLayout) View.inflate(this, R.layout.item_list_data_inquiry_menu, null);
        mReportStatisticsModulView = (View) mRecentBrowseLl.findViewById(R.id.data_inquiry_modul_view);
        mReportStatisticsModulTitleTv = (TextView) mRecentBrowseLl.findViewById(R.id.data_inquiry_modul_title_tv);
        mReportStatisticsModulGv = (MyGridView) mRecentBrowseLl.findViewById(R.id.data_inquiry_modul_gv);

        mDataInquiryGirdItemBeans = new ArrayList<>();
        mDataInquiryMenuGridAdapter = new DataInquiryMenuGridAdapter(this, mDataInquiryGirdItemBeans);
        mReportStatisticsModulGv.setAdapter(mDataInquiryMenuGridAdapter);

        mReportStatisticsModulView.setBackgroundColor(getResources().getColor(R.color.black));
        mReportStatisticsModulTitleTv.setText("最近查询业务");

        mEmptyLayout = new EmptyLayout(this, mMenuListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("数据为空");

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        mRecentBrowseLl.setLayoutParams(layoutParams);

        mCurrentMaster = CommonUtil.getSharedPreferences(this, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(this, "erp_username");
    }

    private void refreshRecentHeader() {
        getRecentBrowse();
        ListView refreshableView = mMenuListView.getRefreshableView();
        int headerViewsCount = refreshableView.getHeaderViewsCount();
        Log.d("headerViewsCount", headerViewsCount + "");
        if (mDataInquiryGirdItemBeans.size() == 0) {
            if (headerViewsCount == 2) {
                refreshableView.removeHeaderView(mRecentBrowseLl);
            }
        } else {
            if (headerViewsCount == 1) {
                refreshableView.addHeaderView(mRecentBrowseLl);
            }
        }
        flag = false;
    }

    private synchronized void getRecentBrowse() {
        try {
            String reportRecentMenuRecentCache = CommonUtil.getSharedPreferences(this,
                    mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE);
            if (!TextUtils.isEmpty(reportRecentMenuRecentCache)) {
                List<GridMenuReportStatisticsBean> reportStatisticsBeans = mReportStatisticsMenuListAdapter.getObjects();
                mDataInquiryGirdItemBeans.clear();

                List<DataInquiryGirdItemBean> recentBrowse = JSON.parseArray(reportRecentMenuRecentCache, DataInquiryGirdItemBean.class);
                List<DataInquiryGirdItemBean> resultBrowse = new ArrayList<>();
                for (int i = 0; i < recentBrowse.size(); i++) {
                    DataInquiryGirdItemBean inquiryGirdItemBean = recentBrowse.get(i);
                    boolean isExist = false;
                    if (reportStatisticsBeans != null) {
                        loop:
                        for (int j = 0; j < reportStatisticsBeans.size(); j++) {
                            List<GridMenuReportStatisticsBean.ListBean> reportList = reportStatisticsBeans.get(j).getList();
                            if (reportList != null) {
                                for (int k = 0; k < reportList.size(); k++) {
                                    if (inquiryGirdItemBean.getIconText().equals(reportList.get(k).getTitle())) {
                                        isExist = true;
                                        inquiryGirdItemBean.setColor(mColors[(j) % mColors.length]);
                                        break loop;
                                    }
                                }
                            }
                        }

                        if (i < 9 && isExist) {
                            mDataInquiryGirdItemBeans.add(inquiryGirdItemBean);
                            resultBrowse.add(recentBrowse.get(i));
                        }
                    }

                }

                String recentJson = JSON.toJSON(resultBrowse).toString();
                CommonUtil.setSharedPreferences(ReportStatisticsActivity.this
                        , mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.REPORT_QUERY_MENU_RECENT_CACHE
                        , recentJson);

                mDataInquiryMenuGridAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getMenuData() {
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/qry/getReport.action";
        Map<String, Object> params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, mHandler, headers, GET_MENU_DATA, null, null, "get");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_MENU_DATA:
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (mMenuListView.isRefreshing()) {
                        mMenuListView.onRefreshComplete();
                    }
                    String result = msg.getData().getString("result");
                    if (result != null) {
                        CommonUtil.setSharedPreferences(ReportStatisticsActivity.this,
                                mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.REPORT_QUERY_MENU_CACHE,
                                result);
                    }
                    mGridMenuReportStatisticsBeans.clear();
                    analysisMenuData(result);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    if (mMenuListView.isRefreshing())
                        mMenuListView.onRefreshComplete();
                    ToastMessage(msg.getData().getString("result"));
                    mEmptyLayout.setErrorMessage(msg.getData().getString("result"));
                    mEmptyLayout.showError();
                    break;
            }
        }
    };

    private void analysisMenuData(String result) {
        if (result != null) {
            LogUtil.prinlnLongMsg("menudata", result);
            try {
                JSONObject resultObject = new JSONObject(result);
                JSONArray dataArray = resultObject.optJSONArray("data");
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.optJSONObject(i);
                        if (dataObject != null) {
                            GridMenuReportStatisticsBean gridMenuReportStatisticsBean
                                    = new GridMenuReportStatisticsBean();
                            String modelName = optStringNotNull(dataObject, "modelName");
                            gridMenuReportStatisticsBean.setModelName(modelName);
                            JSONArray listArray = dataObject.optJSONArray("list");
                            List<GridMenuReportStatisticsBean.ListBean> listBeans = new ArrayList<>();
                            if (listArray != null) {
                                for (int j = 0; j < listArray.length(); j++) {
                                    JSONObject listObject = listArray.optJSONObject(j);
                                    if (listObject != null) {
                                        GridMenuReportStatisticsBean.ListBean listBean = new GridMenuReportStatisticsBean.ListBean();
                                        String caller = optStringNotNull(listObject, "caller");
                                        String title = optStringNotNull(listObject, "title");
                                        String reportName = optStringNotNull(listObject, "reportName");

                                        listBean.setCaller(caller);
                                        listBean.setReportName(reportName);
                                        listBean.setTitle(title);

                                        listBeans.add(listBean);
                                    }
                                }
                                gridMenuReportStatisticsBean.setList(listBeans);
                            }
                            mGridMenuReportStatisticsBeans.add(gridMenuReportStatisticsBean);
                        }
                    }
                    mReportStatisticsMenuListAdapter.notifyDataSetChanged();

                    if (mGridMenuReportStatisticsBeans.size() == 0) {
                        mSearchTextView.setVisibility(View.GONE);
                        mEmptyLayout.showEmpty();
                    } else {
                        mSearchTextView.setVisibility(View.VISIBLE);
                    }

                    if (flag) {
                        refreshRecentHeader();
                    }
                } else {
                    mEmptyLayout.showEmpty();
                    mSearchTextView.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String optStringNotNull(JSONObject json, String key) {
        if (json.isNull(key)) {
            return "";
        } else {
            return json.optString(key, "");
        }
    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
