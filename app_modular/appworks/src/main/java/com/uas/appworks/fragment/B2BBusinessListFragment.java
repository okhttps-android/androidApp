package com.uas.appworks.fragment;

import android.content.Intent;
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
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.fragment.BaseMVPFragment;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.OA.platform.activity.PurchaseDetailsActivity;
import com.uas.appworks.R;
import com.uas.appworks.activity.CustomerInquiryDetailActivity;
import com.uas.appworks.activity.PublicInquiryDetailActivity;
import com.uas.appworks.activity.PublicInquiryQuoteActivity;
import com.uas.appworks.adapter.B2bBusinessListAdapter;
import com.uas.appworks.model.bean.B2BBusinessListBean;
import com.uas.appworks.presenter.WorkPlatPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe B2B商务列表fragment
 * @date 2018/1/15 11:11
 */

public class B2BBusinessListFragment extends BaseMVPFragment<WorkPlatPresenter> implements HttpCallback {
    private static final int OBTAIN_B2B_LIST = 0x11;
    private static final int INQUIRY_SALE_QUOTE = 0x12;
    private static final int REQUEST_PURCHASE_DETAIL = 0x13, REQUEST_CUSTOMER_DETAIL = 0x14, REQUEST_PUBLIC_DETAIL = 0x15;

    private RecyclerView mRecyclerView;
    private B2bBusinessListAdapter mB2BBusinessListAdapter;
    private List<B2BBusinessListBean> mB2BBusinessListBeans;
    private RefreshLayout mRefreshLayout;
    private int mListType = B2BBusinessListBean.PURCHASE_ORDER_LIST;
    private int mPageIndex = 1, mPageSize = 20;
    private String mListUrl, mListState = "", mEnuu, mKeyWord = "";
    private int mSelectPosition = -1;

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_b2b_list;
    }


    public int getListType() {
        return mListType;
    }

    public void setListType(int listType) {
        mListType = listType;

        switch (mListType) {
            case B2BBusinessListBean.PURCHASE_ORDER_LIST:
                mListUrl = "mobile/sale/orders/info/search";
                break;
            case B2BBusinessListBean.CUSTOMER_INQUIRY_LIST:
                mListUrl = "mobile/sale/inquiry/info/search";
                break;
            case B2BBusinessListBean.PUBLIC_INQUIRY_LIST:
//                mListUrl = "inquiry/public/mobile";
                mListUrl = "inquiry/public/mobile/v2";
                break;
            default:
                mListUrl = "mobile/sale/orders/info/search";
                break;
        }
    }

    public String getListState() {
        return mListState;
    }

    public void setListState(String listState) {
        mListState = listState;
    }

    @Override
    protected void initViews() {
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
    protected void initEvents() {
        mB2BBusinessListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, final int position) {
                mSelectPosition = position;
                final List<B2BBusinessListBean> data = mB2BBusinessListAdapter.getData();
                Intent intent = null;
                switch (data.get(position).getItemType()) {
                    case B2BBusinessListBean.PURCHASE_ORDER_LIST:
                        intent = new Intent();
                        intent.setClass(mContext, PurchaseDetailsActivity.class);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                        startActivityForResult(intent, REQUEST_PURCHASE_DETAIL);
                        break;
                    case B2BBusinessListBean.CUSTOMER_INQUIRY_LIST:
                        intent = new Intent();
                        intent.setClass(mContext, CustomerInquiryDetailActivity.class);
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                        startActivityForResult(intent, REQUEST_CUSTOMER_DETAIL);
                        break;
                    case B2BBusinessListBean.PUBLIC_INQUIRY_LIST:
                        intent = new Intent();
                        String billState = data.get(position).getBillState();
                        if (Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE.equals(billState)) {
                            intent.setClass(mContext, PublicInquiryDetailActivity.class);
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                            startActivity(intent);
                        } else if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(billState)
                                || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(billState)) {
                            intent.setClass(mContext, PublicInquiryQuoteActivity.class);
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                            intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                            startActivityForResult(intent, REQUEST_PUBLIC_DETAIL);
                            /*showLoading(null);

                            Map<String, Object> params = new HashMap<>();
                            params.put("en_uu", mEnuu);
                            params.put("user_tel", MyApplication.getInstance().mLoginUser.getTelephone());
                            params.put("id", data.get(position).getId());

                            HttpRequest.getInstance().sendRequest(Constants.API_INQUIRY,
                                    new HttpParams.Builder()
                                            .url("inquiry/sale/quote")
                                            .flag(INQUIRY_SALE_QUOTE)
                                            .method(Method.GET)
                                            .setHeaders(new HashMap<String, Object>())
                                            .setParams(params)
                                            .build(), new HttpCallback() {
                                        @Override
                                        public void onSuccess(int flag, Object o) throws Exception {
                                            Log.d("publicinquiryquote", "success->" + o.toString());
                                            hideLoading();
                                            Intent intent = null;
                                            String inquiryJson = o.toString();
                                            if (TextUtils.isEmpty(inquiryJson)) {
                                                intent = new Intent();
                                                intent.setClass(mContext, CustomerInquiryDetailActivity.class);
                                                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                                                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                                                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mB2BBusinessListBeans.get(position).getJsonData());
                                                startActivityForResult(intent, REQUEST_PUBLIC_DETAIL);
                                            } else {
                                                intent = new Intent();
                                                intent.setClass(mContext, PublicInquiryDetailActivity.class);
                                                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mB2BBusinessListBeans.get(position).getId());
                                                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mB2BBusinessListBeans.get(position).getBillState());
                                                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, inquiryJson);
                                                startActivity(intent);
                                            }
                                        }

                                        @Override
                                        public void onFail(int flag, String failStr) throws Exception {
                                            hideLoading();
                                            toast(failStr);
                                        }
                                    });*/
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
    protected void initDatas() {
        if (CommonUtil.isNetWorkConnected(mContext)) {
            getListData(1, "");
        } else {
            mB2BBusinessListAdapter.setEmptyView(R.layout.view_net_error, (ViewGroup) mRecyclerView.getParent());
        }
    }

    public void getListData(int pageIndex, String keyWord) {
        showLoading(null);

        mPageIndex = pageIndex;
        mKeyWord = keyWord;

        Map<String, Object> params = new HashMap<>();
        params.put("en_uu", mEnuu);
        params.put("user_tel", MyApplication.getInstance().mLoginUser.getTelephone());
        params.put("_state", mListState);
        params.put("page", mPageIndex);
        params.put("size", mPageSize);
        if (!TextUtils.isEmpty(mKeyWord)) {
            params.put("keyword", mKeyWord);
        }

        LogUtil.d("b2blistparams", params.toString());
        String host = new ApiPlatform().getBaseUrl();
        if (mListType == B2BBusinessListBean.PUBLIC_INQUIRY_LIST) {
            host = Constants.API_INQUIRY;
        }
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
    public void showLoading(String loadStr) {
        if (mRefreshLayout != null && !mRefreshLayout.isRefreshing() && !mRefreshLayout.isLoading()) {
            ((BaseMVPActivity) getActivity()).progressDialog.show();
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
        ((BaseMVPActivity) getActivity()).progressDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        B2BBusinessListBean b2BBusinessListBean = mB2BBusinessListBeans.get(mSelectPosition);
        if (b2BBusinessListBean != null) {
            if (requestCode == REQUEST_PURCHASE_DETAIL
                    && resultCode == Constants.FLAG.RESULT_PURCHASE_ORDER && data != null) {
                String purchaseStatus = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE);
                if (!TextUtils.isEmpty(mListState) && Constants.FLAG.STATE_PURCHASE_ORDER_TODO.equals(mListState)) {
                    if (!Constants.FLAG.STATE_PURCHASE_ORDER_TODO.equals(purchaseStatus)) {
                        mB2BBusinessListBeans.remove(mSelectPosition);
                    }
                } else {
                    b2BBusinessListBean.setBillState(purchaseStatus);
                }
            } else if (requestCode == REQUEST_CUSTOMER_DETAIL
                    && resultCode == Constants.FLAG.RESULT_CUSTOMER_INQUIRY) {
                if (!TextUtils.isEmpty(mListState) && Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO.equals(mListState)) {
                    mB2BBusinessListBeans.remove(mSelectPosition);
                } else {
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_DONE);
                    b2BBusinessListBean.setJsonData(null);
                }
            } else if (requestCode == REQUEST_PUBLIC_DETAIL
                    && resultCode == Constants.FLAG.RESULT_PUBLIC_INQUIRY && data != null) {
                String newId = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
                b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE);
                b2BBusinessListBean.setId(newId);
                b2BBusinessListBean.setJsonData(null);
            } else if (requestCode == REQUEST_CUSTOMER_DETAIL
                    && resultCode == Constants.FLAG.RESULT_COMPANY_BUSINESS && data != null) {
                String newId = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
                b2BBusinessListBean.setBillState(Constants.FLAG.STATE_COMPANY_BUSINESS_DONE);
                b2BBusinessListBean.setId(newId);
                b2BBusinessListBean.setJsonData(null);
            }
            mB2BBusinessListAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onSuccess(int flag, Object o) {
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
                            if (mListType == B2BBusinessListBean.PUBLIC_INQUIRY_LIST
                                    && TextUtils.isEmpty(mListState)) {
                                mB2BBusinessListAdapter.setEmptyView(R.layout.view_public_inquiry_empty, (ViewGroup) mRecyclerView.getParent());
                            } else {
                                mB2BBusinessListAdapter.setEmptyView(R.layout.view_empty, (ViewGroup) mRecyclerView.getParent());
                            }
                        } else {
                            if (mRefreshLayout != null && mRefreshLayout.isLoading()) {
                                mRefreshLayout.finishLoadmoreWithNoMoreData();
                            }
                            ((BaseMVPActivity) getActivity()).progressDialog.dismiss();
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
                    switch (mListType) {
                        case B2BBusinessListBean.PURCHASE_ORDER_LIST:
                            analysisPurchaseOrder(contentArray);
                            break;
                        case B2BBusinessListBean.CUSTOMER_INQUIRY_LIST:
                            analysisCustomerInquiry(contentArray);
                            break;
                        case B2BBusinessListBean.PUBLIC_INQUIRY_LIST:
                            analisisPublicInquiry(contentArray);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }

    private void analisisPublicInquiry(JSONArray contentArray) {
        for (int i = 0; i < contentArray.size(); i++) {
            JSONObject contentObject = contentArray.getJSONObject(i);
            if (contentObject != null) {
                B2BBusinessListBean b2BBusinessListBean = new B2BBusinessListBean();
                b2BBusinessListBean.setItemType(B2BBusinessListBean.PUBLIC_INQUIRY_LIST);

                int quoted = JSONUtil.getInt(contentObject, "quoted");
                if (TextUtils.isEmpty(JSONUtil.getText(contentObject, "quoted"))) {
                    quoted = 1;
                }

                if (TextUtils.isEmpty(contentObject.getString("quteId"))) {
                    b2BBusinessListBean.setId(JSONUtil.getText(contentObject, "id"));
                } else {
                    b2BBusinessListBean.setId(JSONUtil.getText(contentObject, "quteId"));
                }

                long date = contentObject.getLongValue("date");
                if (date != 0) {
                    b2BBusinessListBean.setBillDate(DateFormatUtil.long2Str(date, DateFormatUtil.YMD));
                }

                String needquantity = JSONUtil.getText(contentObject, "needquantity");
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
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE);
                } else {
                    b2BBusinessListBean.setJsonData(contentObject.toString());
                    if ("1".equals(overdue) || remainingTime <= 0) {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID);
                    } else {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO);
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

    private void analysisCustomerInquiry(JSONArray contentArray) {
        for (int i = 0; i < contentArray.size(); i++) {
            JSONObject contentObject = contentArray.getJSONObject(i);
            if (contentObject != null) {
                B2BBusinessListBean b2BBusinessListBean = new B2BBusinessListBean();
                b2BBusinessListBean.setJsonData(contentObject.toString());
                b2BBusinessListBean.setItemType(B2BBusinessListBean.CUSTOMER_INQUIRY_LIST);
                b2BBusinessListBean.setId(JSONUtil.getText(contentObject, "id"));

                JSONObject inquiryObject = contentObject.getJSONObject("inquiry");
                JSONObject productObject = contentObject.getJSONObject("product");

                long endDate = 0, billDate = 0;
                if (inquiryObject != null) {
                    JSONObject enterpriseObject = inquiryObject.getJSONObject("enterprise");
                    if (enterpriseObject != null) {
                        b2BBusinessListBean.setCompanyName(JSONUtil.getText(enterpriseObject, "enName"));
                    }
                    b2BBusinessListBean.setBillNum(JSONUtil.getText(inquiryObject, "code"));

                    endDate = inquiryObject.getLongValue("endDate");
                    if (endDate != 0) {
                        b2BBusinessListBean.setExpiryDate(DateFormatUtil.long2Str(endDate, DateFormatUtil.YMD));
                    }
                }
                if (productObject != null) {
                    b2BBusinessListBean.setMaterialNum(JSONUtil.getText(productObject, "code"));
                    b2BBusinessListBean.setMaterialName(JSONUtil.getText(productObject, "title"));
                    b2BBusinessListBean.setMaterialSpec(JSONUtil.getText(productObject, "spec"));
                }
                billDate = contentObject.getLongValue("date");
                if (billDate != 0) {
                    b2BBusinessListBean.setBillDate(DateFormatUtil.long2Str(billDate, DateFormatUtil.YMD));
                }

                long remainDate = endDate - System.currentTimeMillis();
                if (remainDate <= 0) {
                    remainDate = 0;
                    b2BBusinessListBean.setRemainTime(0);
                } else {
                    b2BBusinessListBean.setRemainTime((int) Math.ceil((float) remainDate / (1000 * 60 * 60 * 24)));
                }

                String invalid = JSONUtil.getText(contentObject, "invalid");
                String status = JSONUtil.getText(contentObject, "status");
                String overdue = JSONUtil.getText(contentObject, "overdue");
                String agreed = JSONUtil.getText(contentObject, "agreed");

                if ("314".equals(status)) {
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_ABANDONED);
                } else if ("200".equals(status)) {
                    if ("1".equals(overdue)) {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_END);
                    } else {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO);
                    }
                } else if ("201".equals(status)) {
                    if ("1".equals(agreed)) {
                        if ("1".equals(invalid)) {
                            b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_INVALID);
                        } else {
                            b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_AGREED);
                        }
                    } else if ("0".equals(agreed)) {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_REFUSED);
                    } else {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_DONE);
                    }
                }

                mB2BBusinessListBeans.add(b2BBusinessListBean);
            }
        }
        mB2BBusinessListAdapter.notifyDataSetChanged();
    }

    private void analysisPurchaseOrder(JSONArray contentArray) {
        for (int i = 0; i < contentArray.size(); i++) {
            JSONObject contentObject = contentArray.getJSONObject(i);
            if (contentObject != null) {
                B2BBusinessListBean b2BBusinessListBean = new B2BBusinessListBean();
                b2BBusinessListBean.setJsonData(contentObject.toString());
                b2BBusinessListBean.setItemType(B2BBusinessListBean.PURCHASE_ORDER_LIST);
                b2BBusinessListBean.setId(JSONUtil.getText(contentObject, "id"));

                JSONObject enterprise = contentObject.getJSONObject("enterprise");
                if (enterprise != null) {
                    b2BBusinessListBean.setCompanyName(JSONUtil.getText(enterprise, "enName"));
                }

                long date = contentObject.getLongValue("date");
                if (date != 0) {
                    b2BBusinessListBean.setBillDate(DateFormatUtil.long2Str(date, DateFormatUtil.YMD));
                }
                b2BBusinessListBean.setBillNum(JSONUtil.getText(contentObject, "code"));
                b2BBusinessListBean.setCurrency(JSONUtil.getText(contentObject, "currency"));

                JSONArray orderItems = contentObject.getJSONArray("orderItems");
                if (orderItems == null || orderItems.size() == 0) {
                    b2BBusinessListBean.setMoney("0");
                } else {
                    double money = 0;
                    for (int j = 0; j < orderItems.size(); j++) {
                        JSONObject orderItem = orderItems.getJSONObject(j);
                        double amount = JSONUtil.getDouble(orderItem, "amount");
                        money += amount;
                    }
                    b2BBusinessListBean.setMoney(CommonUtil.doubleFormat(money));
                }
                String end = JSONUtil.getText(contentObject, "end");
                if (!TextUtils.isEmpty(end) && "1".equals(end)) {
                    b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PURCHASE_ORDER_END);
                } else {
                    String status = JSONUtil.getText(contentObject, "status");
                    if ("200".equals(status)) {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PURCHASE_ORDER_TODO);
                    } else if ("201".equals(status)) {
                        b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PURCHASE_ORDER_DONE);
                    }
                }

                mB2BBusinessListBeans.add(b2BBusinessListBean);
            }
        }
        mB2BBusinessListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFail(int flag, String failStr) {
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
}
