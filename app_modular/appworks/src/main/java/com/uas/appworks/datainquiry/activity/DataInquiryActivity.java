package com.uas.appworks.datainquiry.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
import com.uas.appworks.datainquiry.adapter.DataInquiryMenuListAdapter;
import com.uas.appworks.datainquiry.bean.DataInquiryGirdItemBean;
import com.uas.appworks.datainquiry.bean.GridMenuDataInquiryBean;

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
 * 选择查询业务菜单页面
 */
public class DataInquiryActivity extends BaseActivity {
    private final int GET_MENU_DATA = 0x08;
    private PullToRefreshListView mMenuListView;
    private List<GridMenuDataInquiryBean> mGridMenuDataInquiryBeans;
    private DataInquiryMenuListAdapter mDataInquiryMenuListAdapter;
    private DataInquiryMenuGridAdapter mDataInquiryMenuGridAdapter;
    private List<DataInquiryGirdItemBean> mDataInquiryGirdItemBeans;
    private LinearLayout mRecentBrowseLl;
    private View mDataInquiryModulView;
    private TextView mDataInquiryModulTitleTv;
    private MyGridView mDataInquiryModulGv;
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
        setTitle("数据查询");
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
        String dataInquiryMenuCache = CommonUtil.getSharedPreferences(this,
                mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.DATA_INQUIRY_MENU_CACHE);
        if (dataInquiryMenuCache != null) {
            try {
                JSONObject resultObject = new JSONObject(dataInquiryMenuCache);
                JSONArray dataArray = resultObject.optJSONArray("data");
                if (dataArray == null || dataArray.length() == 0) {
                    if (!CommonUtil.isNetWorkConnected(this)) {
                        ToastMessage(getString(R.string.networks_out));
                    } else {
                        progressDialog.show();
                        getMenuData();
                    }
                } else {
                    analysisMenuData(dataInquiryMenuCache);
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
                if (!CommonUtil.isNetWorkConnected(DataInquiryActivity.this)) {
                    ToastMessage(getString(R.string.networks_out));
                    if (mMenuListView.isRefreshing())
                        mMenuListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMenuListView.onRefreshComplete();
                            }
                        }, 500);
                } else {
                    flag = true;
                    getMenuData();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mDataInquiryModulGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataInquiryGirdItemBean dataInquiryGirdItemBean = mDataInquiryMenuGridAdapter.getObjects().get(position);
                String iconText = dataInquiryGirdItemBean.getIconText();
                String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(DataInquiryActivity.this,
                        mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE);
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
                CommonUtil.setSharedPreferences(DataInquiryActivity.this
                        , mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE
                        , recentJson);

//                mDataInquiryGirdItemBeans.clear();
//                mDataInquiryGirdItemBeans.addAll(recentBrowse);
//                mDataInquiryMenuGridAdapter.notifyDataSetChanged();

                for (int i = 0; i < mGridMenuDataInquiryBeans.size(); i++) {
                    List<GridMenuDataInquiryBean.QueryScheme> querySchemes = mGridMenuDataInquiryBeans.get(i).getQuerySchemes();
                    for (int j = 0; j < querySchemes.size(); j++) {
                        if (dataInquiryGirdItemBean.getIconText().equals(querySchemes.get(j).getScheme())) {
                            Intent intent = new Intent();
                            intent.setClass(DataInquiryActivity.this, DataInquiryListActivity.class);
                            intent.putExtra("scheme", querySchemes.get(j));
                            DataInquiryActivity.this.startActivity(intent);
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
                intent.setClass(DataInquiryActivity.this, DataInquirySearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        mMenuListView = (PullToRefreshListView) findViewById(R.id.data_inquiry_menu_lv);
        mSearchTextView = (DrawableCenterTextView) findViewById(R.id.data_inquiry_menu_search_tv);

        mGridMenuDataInquiryBeans = new ArrayList<>();
        mDataInquiryMenuListAdapter = new DataInquiryMenuListAdapter(this, mGridMenuDataInquiryBeans);
        mMenuListView.setAdapter(mDataInquiryMenuListAdapter);

        mRecentBrowseLl = (LinearLayout) View.inflate(this, R.layout.item_list_data_inquiry_menu, null);
        mDataInquiryModulView = (View) mRecentBrowseLl.findViewById(R.id.data_inquiry_modul_view);
        mDataInquiryModulTitleTv = (TextView) mRecentBrowseLl.findViewById(R.id.data_inquiry_modul_title_tv);
        mDataInquiryModulGv = (MyGridView) mRecentBrowseLl.findViewById(R.id.data_inquiry_modul_gv);

        mDataInquiryGirdItemBeans = new ArrayList<>();
        mDataInquiryMenuGridAdapter = new DataInquiryMenuGridAdapter(this, mDataInquiryGirdItemBeans);
        mDataInquiryModulGv.setAdapter(mDataInquiryMenuGridAdapter);

        mDataInquiryModulView.setBackgroundColor(getResources().getColor(R.color.black));
        mDataInquiryModulTitleTv.setText("最近查询业务");

        mEmptyLayout = new EmptyLayout(this, mMenuListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        mRecentBrowseLl.setLayoutParams(layoutParams);

        mCurrentMaster = CommonUtil.getSharedPreferences(this, "erp_master");
        mCurrentUser = CommonUtil.getSharedPreferences(this, "erp_username");
    }

    private synchronized void getRecentBrowse() {
        try {
            String dataInquiryMenuRecentCache = CommonUtil.getSharedPreferences(this,
                    mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE);
            if (!TextUtils.isEmpty(dataInquiryMenuRecentCache)) {
                List<GridMenuDataInquiryBean> dataInquiryBeans = mDataInquiryMenuListAdapter.getObjects();
                mDataInquiryGirdItemBeans.clear();

                List<DataInquiryGirdItemBean> recentBrowse = JSON.parseArray(dataInquiryMenuRecentCache, DataInquiryGirdItemBean.class);
                List<DataInquiryGirdItemBean> resultBrowse = new ArrayList<>();
                for (int i = 0; i < recentBrowse.size(); i++) {
                    DataInquiryGirdItemBean inquiryGirdItemBean = recentBrowse.get(i);
                    boolean isExist = false;
                    if (dataInquiryBeans != null) {
                        loop:
                        for (int j = 0; j < dataInquiryBeans.size(); j++) {
                            List<GridMenuDataInquiryBean.QueryScheme> querySchemes = dataInquiryBeans.get(j).getQuerySchemes();
                            if (querySchemes != null) {
                                for (int k = 0; k < querySchemes.size(); k++) {
                                    if (inquiryGirdItemBean.getIconText().equals(querySchemes.get(k).getScheme())) {
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
                CommonUtil.setSharedPreferences(DataInquiryActivity.this
                        , mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.DATA_INQUIRY_MENU_RECENT_CACHE
                        , recentJson);

                mDataInquiryMenuGridAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getMenuData() {
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/qry/queryJsp.action";
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
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    if (mMenuListView.isRefreshing())
                        mMenuListView.onRefreshComplete();
                    String result = msg.getData().getString("result");
                    if (result != null)
                        CommonUtil.setSharedPreferences(DataInquiryActivity.this,
                                mCurrentUser + mCurrentMaster + com.uas.appworks.datainquiry.Constants.CONSTANT.DATA_INQUIRY_MENU_CACHE,
                                result);
                    mGridMenuDataInquiryBeans.clear();
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
                            GridMenuDataInquiryBean gridMenuDataInquiryBean = new GridMenuDataInquiryBean();
                            String modelName = optStringNotNull(dataObject, "modelName");
                            gridMenuDataInquiryBean.setModelName(modelName);
                            JSONArray listArray = dataObject.optJSONArray("list");
                            if (listArray != null) {
                                List<GridMenuDataInquiryBean.QueryScheme> querySchemes = new ArrayList<>();
                                for (int j = 0; j < listArray.length(); j++) {
                                    JSONObject listObject = listArray.optJSONObject(j);
                                    if (listObject != null) {
                                        String title = optStringNotNull(listObject, "title");
                                        String caller = optStringNotNull(listObject, "caller");
                                        JSONArray schemeArray = listObject.optJSONArray("schemes");
                                        if (schemeArray != null) {
                                            for (int k = 0; k < schemeArray.length(); k++) {
                                                JSONObject schemeObject = schemeArray.optJSONObject(k);
                                                if (schemeObject != null) {
                                                    String scheme = optStringNotNull(schemeObject, "scheme");
                                                    String schemeId = optStringNotNull(schemeObject, "schemeId");

                                                    GridMenuDataInquiryBean.QueryScheme queryScheme = new GridMenuDataInquiryBean.QueryScheme();
                                                    queryScheme.setTitle(title);
                                                    queryScheme.setCaller(caller);
                                                    queryScheme.setScheme(scheme);
                                                    queryScheme.setSchemeId(schemeId);

                                                    querySchemes.add(queryScheme);
                                                }
                                            }
                                        }
                                    }
                                }
                                gridMenuDataInquiryBean.setQuerySchemes(querySchemes);
                            }
                            mGridMenuDataInquiryBeans.add(gridMenuDataInquiryBean);
                        }
                    }
                    mDataInquiryMenuListAdapter.notifyDataSetChanged();

                    if (mGridMenuDataInquiryBeans.size() == 0) {
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


    public String optStringNotNull(JSONObject json, String key) {
        if (json.isNull(key)) {
            return "";
        } else {
            return json.optString(key, "");
        }
    }
}
