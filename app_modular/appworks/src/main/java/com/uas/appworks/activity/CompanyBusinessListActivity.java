package com.uas.appworks.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.utils.CommonUtil;
import com.core.widget.SearchActionView;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.R;
import com.uas.appworks.adapter.B2bBusinessListAdapter;
import com.uas.appworks.model.bean.B2BBusinessListBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 公司商机列表页面
 * @date 2018/6/20 9:31
 */
public class CompanyBusinessListActivity extends BaseMVPActivity<SimplePresenter> implements HttpCallback {
    private static final int OBTAIN_B2B_LIST = 0x01;
    private static final int REQUEST_COMPANY_DETAIL = 0x11;

    private RecyclerView mRecyclerView;
    private B2bBusinessListAdapter mB2BBusinessListAdapter;
    private List<B2BBusinessListBean> mB2BBusinessListBeans;
    private RefreshLayout mRefreshLayout;
    private int mPageIndex = 1, mPageSize = 20;
    private String mListUrl, mEnuu, mKeyWord = "";
    private int mSelectPosition = -1;
    private SearchActionView mSearchActionView;

    @Override
    protected int getLayout() {
        return R.layout.activity_company_business_list;
    }

    @Override
    protected void initView() {
        setTitle("");
        mSearchActionView = new SearchActionView(mContext);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(mSearchActionView);
        mSearchActionView.setSearchHint(getString(R.string.str_company_business_list));

        mEnuu = CommonUtil.getSharedPreferences(mContext, Constants.CACHE.B2B_BUSINESS_ENUU);
        mRecyclerView = $(R.id.b2b_list_rv);
        mRefreshLayout = $(R.id.b2b_list_refreshlayout);
        mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);

        mB2BBusinessListBeans = new ArrayList<>();
        mB2BBusinessListAdapter = new B2bBusinessListAdapter(mContext, mB2BBusinessListBeans);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setNestedScrollingEnabled(false);
//        mRecyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light)));
        mRecyclerView.setAdapter(mB2BBusinessListAdapter);
    }

    @Override
    protected SimplePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mSearchActionView.setOnEnterActionListener(new SearchActionView.OnEnterActionListener() {
            @Override
            public void onEnterAction() {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mKeyWord = mSearchActionView.getText();
                    getListData(1, mKeyWord);
                } else {
                    toast(R.string.networks_out);
                }
            }
        });

        mSearchActionView.setOnVoiceCompleteListener(new SearchActionView.OnVoiceCompleteListener() {
            @Override
            public void onVoiceComplete(String text) {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mKeyWord = mSearchActionView.getText();
                    getListData(1, mKeyWord);
                } else {
                    toast(R.string.networks_out);
                }
            }
        });

        mSearchActionView.setOnTextChangedListener(new SearchActionView.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(mKeyWord)) {
                    if (CommonUtil.isNetWorkConnected(mContext)) {
                        mKeyWord = "";
                        getListData(1, mKeyWord);
                    }
                }
            }
        });

        mB2BBusinessListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, final int position) {
                mSelectPosition = position;
                final List<B2BBusinessListBean> data = mB2BBusinessListAdapter.getData();
                Intent intent = null;
                switch (data.get(position).getItemType()) {
                    case B2BBusinessListBean.COMPANY_BUSINESS_LIST:
                        intent = new Intent();
                        String billState = data.get(position).getBillState();
                        if (Constants.FLAG.STATE_COMPANY_BUSINESS_DONE.equals(billState)) {
                            intent.setClass(mContext, PublicInquiryDetailActivity.class);
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                            startActivity(intent);
                        } else if (Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(billState)
                                || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(billState)) {
                            intent.setClass(mContext, PublicInquiryQuoteActivity.class);
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                            startActivityForResult(intent, REQUEST_COMPANY_DETAIL);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mRefreshLayout.resetNoMoreData();
                    mPageIndex = 1;
                    getListData(mPageIndex, mKeyWord);
                } else {
                    toast(R.string.networks_out);
                    mRefreshLayout.finishRefresh(500, false);
                }
            }
        });

        mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                if (CommonUtil.isNetWorkConnected(mContext)) {
                    mPageIndex++;
                    getListData(mPageIndex, mKeyWord);
                } else {
                    toast(R.string.networks_out);
                    mRefreshLayout.finishLoadMore(500, false, false);
                }
            }
        });
    }

    @Override
    protected void initData() {
        mListUrl = "inquiry/sale/enremind";

        if (CommonUtil.isNetWorkConnected(mContext)) {
            getListData(1, "");
        } else {
            mB2BBusinessListAdapter.setEmptyView(R.layout.view_net_error, (ViewGroup) mRecyclerView.getParent());
        }
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    public void getListData(int pageIndex, String keyWord) {
        showLoading(null);

        mPageIndex = pageIndex;
        mKeyWord = keyWord;

        Map<String, Object> params = new HashMap<>();
        try {
            params.put("enuu", Long.parseLong(mEnuu));
        } catch (Exception e) {
            params.put("enuu", mEnuu);
        }
        params.put("useruu", CommonUtil.getUseruuLong(this));
        params.put("pageNumber", mPageIndex);
        params.put("pageSize", mPageSize);
        if (!TextUtils.isEmpty(mKeyWord)) {
            params.put("keyword", mKeyWord);
        }

        LogUtil.d("b2blistparams", params.toString());
        String host = Constants.API_INQUIRY;

        HttpRequest.getInstance().sendRequest(host,
                new HttpParams.Builder()
                        .url(mListUrl)
                        .flag(OBTAIN_B2B_LIST)
                        .method(Method.GET)
                        .setHeaders(new HashMap<String, Object>())
                        .setParams(params)
                        .build(), this);
    }

    @Override
    public void onSuccess(int flag, Object o) throws Exception {
        try {
            String result = o.toString();
            LogUtil.prinlnLongMsg("b2blistlogsuccess", result);
            switch (flag) {
                case OBTAIN_B2B_LIST:
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray contentArray = resultObject.getJSONArray("content");
                    if (contentArray == null || contentArray.size() == 0) {
                        if (mPageIndex == 1) {
                            hideLoading();
                            mB2BBusinessListBeans.clear();
                            mB2BBusinessListAdapter.notifyDataSetChanged();
                            mB2BBusinessListAdapter.setEmptyView(R.layout.view_public_inquiry_empty, (ViewGroup) mRecyclerView.getParent());
                        } else {
                            if (mRefreshLayout != null && mRefreshLayout.isLoading()) {
                                mRefreshLayout.finishLoadmoreWithNoMoreData();
                            }
                            progressDialog.dismiss();
                        }
                        return;
                    }
                    if (mPageIndex == 1) {
                        mB2BBusinessListBeans.clear();
                    }
                    if (contentArray.size() < mPageSize && mPageIndex > 1 && mRefreshLayout != null && mRefreshLayout.isLoading()) {
                        mRefreshLayout.finishLoadmoreWithNoMoreData();
                    }
                    hideLoading();
                    analisisCompanyBusiness(contentArray);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        try {
            hideLoading();
            if (flag == OBTAIN_B2B_LIST && mPageIndex > 1) {
                mPageIndex--;
            }
            if (!CommonUtil.isRepeatClick(2000)) {
                toast(failStr);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        B2BBusinessListBean b2BBusinessListBean = mB2BBusinessListBeans.get(mSelectPosition);
        if (b2BBusinessListBean != null) {
            if (requestCode == REQUEST_COMPANY_DETAIL
                    && resultCode == Constants.FLAG.RESULT_COMPANY_BUSINESS && data != null) {
                String newId = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
                b2BBusinessListBean.setBillState(Constants.FLAG.STATE_COMPANY_BUSINESS_DONE);
                b2BBusinessListBean.setId(newId);
                b2BBusinessListBean.setJsonData(null);
            }
            mB2BBusinessListAdapter.notifyDataSetChanged();
        }

    }

    private void analisisCompanyBusiness(JSONArray contentArray) {
        for (int i = 0; i < contentArray.size(); i++) {
            JSONObject contentObject = contentArray.getJSONObject(i);
            if (contentObject != null) {
                JSONObject quotationObject = contentObject.getJSONObject("quotation");
                B2BBusinessListBean b2BBusinessListBean = new B2BBusinessListBean();
                b2BBusinessListBean.setItemType(B2BBusinessListBean.COMPANY_BUSINESS_LIST);

                int quoted = 0;

                if (TextUtils.isEmpty(contentObject.getString("newId"))) {
                    b2BBusinessListBean.setId(JSONUtil.getText(contentObject, "itemId"));
                } else {
                    b2BBusinessListBean.setId(JSONUtil.getText(contentObject, "newId"));
                    if (quotationObject != null) {
                        b2BBusinessListBean.setJsonData(quotationObject.toString());
                    }
                    quoted = 1;
                }

                long date = contentObject.getLongValue("date");
                if (date != 0) {
                    b2BBusinessListBean.setBillDate(DateFormatUtil.long2Str(date, DateFormatUtil.YMD));
                }

                String needquantity = JSONUtil.getText(contentObject, "needQty");
                b2BBusinessListBean.setProductAmount(needquantity);

                JSONObject inquiryObject = contentObject.getJSONObject("inquiry");

                b2BBusinessListBean.setProductName(JSONUtil.getText(contentObject, "prodTitle"));
                b2BBusinessListBean.setProductModel(JSONUtil.getText(contentObject, "cmpCode"));
                b2BBusinessListBean.setProductSpecification(JSONUtil.getText(contentObject, "spec"));
                b2BBusinessListBean.setProductBrand(JSONUtil.getText(contentObject, "inbrand"));
                if (inquiryObject != null) {
                    JSONObject enterpriseObject = inquiryObject.getJSONObject("enterprise");
                    if (enterpriseObject != null) {
                        b2BBusinessListBean.setCompanyName(JSONUtil.getText(enterpriseObject, "enName"));
                    }

                    long endDate = inquiryObject.getLongValue("endDate");
                    if (endDate != 0) {
                        b2BBusinessListBean.setExpiryDate(DateFormatUtil.long2Str(endDate, DateFormatUtil.YMD));
                    }
                }
                String invalid = JSONUtil.getText(contentObject, "invalid");
                String overdue = JSONUtil.getText(contentObject, "overdue");
                String status = JSONUtil.getText(contentObject, "status");
                long remainingTime = JSONUtil.getLong(contentObject, "remainingTime");//剩余时间

                if (remainingTime <= 0) {
                    b2BBusinessListBean.setRemainTime(0);
                } else {
                    b2BBusinessListBean.setRemainTime((int) Math.ceil((float) remainingTime / (1000 * 60 * 60 * 24)));
                }

                if (quoted == 1) {
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_COMPANY_BUSINESS_DONE);
                } else {
                    b2BBusinessListBean.setJsonData(contentObject.toString());
                    if (remainingTime <= 0) {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID);
                    } else {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_COMPANY_BUSINESS_TODO);
                    }
                }
                /*if ("1".equals(invalid) || "1".equals(overdue) || "201".equals(status)) {
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE);
                } else {
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO);
                }*/
                mB2BBusinessListBeans.add(b2BBusinessListBean);
            }
        }
        mB2BBusinessListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading(String loadStr) {
        if (mRefreshLayout != null && !mRefreshLayout.isRefreshing() && !mRefreshLayout.isLoading()) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(0);
        }
        if (mRefreshLayout != null && mRefreshLayout.isLoading()) {
            mRefreshLayout.finishLoadmore(0);
        }
        progressDialog.dismiss();
    }

}
