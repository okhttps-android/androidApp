package com.uas.appworks.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.core.utils.SpanUtils;
import com.core.widget.DrawableCenterTextView;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appworks.OA.platform.activity.PurchaseDetailsActivity;
import com.uas.appworks.R;
import com.uas.appworks.adapter.B2BAccountAdapter;
import com.uas.appworks.adapter.B2bBusinessListAdapter;
import com.uas.appworks.model.bean.B2BBusinessListBean;
import com.uas.appworks.model.bean.B2BCompanyBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe B2B商务首页
 * @date 2018/1/14 14:19
 */

public class B2BBusinessMainActivity extends BaseMVPActivity<WorkPlatPresenter> implements View.OnClickListener, HttpCallback {
    private static final int OBTAIN_PURCHASE_ORDER_LIST = 0x01,
            OBTAIN_CUSTOMER_INQUIRY_LIST = 0x02,
            OBTAIN_PUBLIC_INQUIRY_LIST = 0x03,
            OBTAIN_COMPANY_BUSINESS_LIST = 0x04,
            REQUEST_PURCHASE_DETAIL = 0x11,
            REQUEST_CUSTOMER_DETAIL = 0x12,
            REQUEST_PUBLIC_DETAIL = 0x13,
            REQUEST_COMPANY_DETAIL = 0x14;

    private RefreshLayout mRefreshLayout;
    private ImageView mBackImageView;
    private DrawableCenterTextView mCompanyNameTextView;
    private LinearLayout mPurchaseOrderIv, mCustomerInquiryIv, mPublicInquiryIv;
    private TextView mPurchaseOrderAllTv, mCustomerInquiryAllTv, mPublicInquiryAllTv, mCompanyBusinessAllTv, mAccountSureTextView, mAccountCancelTextView;
    private RecyclerView mPurchaseOrderRv, mCustomerInquiryRv, mPublicInquiryRv, mCompanyBusinessRv, mAccountRecyclerView;
    private MaterialDialog mAccountDialog;
    private Banner mHeaderBanner;

    private B2BCompanyBean mB2BCompanyBean;
    private SpanUtils mSpanUtils;
    private List<B2BCompanyBean> mB2BCompanyBeans;
    private B2BAccountAdapter mB2BAccountAdapter;
    private List<B2BBusinessListBean> mPurchaseOrderBeans, mCustomerInquiryBeans, mPublicInquiryBeans, mCompanyBusinessBeans;
    private B2bBusinessListAdapter mPurchaseOrderAdapter, mCustomerInquiryAdapter, mPublicInquiryAdapter, mCompanyBusinessAdapter;
    private boolean isPurchaseSuccess, isCustomerSuccess, isPublicSuccess, isCompanySuccess;
    private int mSelectPosition = -1;


    @Override
    protected void onStart() {
        super.onStart();
        mHeaderBanner.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHeaderBanner.stopAutoPlay();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_b2b_business_main;
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    @Override
    protected void initView() {
        setTitle("");
        View actionView = View.inflate(mContext, R.layout.action_b2b_company, null);
        mBackImageView = (ImageView) actionView.findViewById(R.id.b2b_company_back);
        mCompanyNameTextView = (DrawableCenterTextView) actionView.findViewById(R.id.b2b_company_name);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(actionView);

        mRefreshLayout = $(R.id.b2b_business_main_refreshlayout);
        mPurchaseOrderIv = $(R.id.b2b_business_main_purchase_order_iv);
        mCustomerInquiryIv = $(R.id.b2b_business_main_customer_inquiry_iv);
        mPublicInquiryIv = $(R.id.b2b_business_main_public_inquiry_iv);
        mPurchaseOrderAllTv = $(R.id.b2b_business_main_purchase_order_all_tv);
        mPurchaseOrderRv = $(R.id.b2b_business_main_purchase_order_rv);
        mCustomerInquiryAllTv = $(R.id.b2b_business_main_customer_inquiry_all_tv);
        mCustomerInquiryRv = $(R.id.b2b_business_main_customer_inquiry_rv);
        mPublicInquiryAllTv = $(R.id.b2b_business_main_public_inquiry_all_tv);
        mPublicInquiryRv = $(R.id.b2b_business_main_public_inquiry_rv);
        mCompanyBusinessAllTv = $(R.id.b2b_business_main_company_business_all_tv);
        mCompanyBusinessRv = $(R.id.b2b_business_main_company_business_rv);

        mHeaderBanner = $(R.id.b2b_business_main_header_banner);
        mHeaderBanner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(path).into(imageView);
            }
        });

        View accountView = View.inflate(mContext, R.layout.pop_b2b_change_account, null);
        mAccountRecyclerView = (RecyclerView) accountView.findViewById(R.id.b2b_change_account_rv);
        mAccountRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAccountSureTextView = (TextView) accountView.findViewById(R.id.b2b_change_account_sure_tv);
        mAccountCancelTextView = (TextView) accountView.findViewById(R.id.b2b_change_account_cancel_tv);

        mB2BCompanyBeans = new ArrayList<>();
        mB2BAccountAdapter = new B2BAccountAdapter(mContext, mB2BCompanyBeans);
        mAccountRecyclerView.setAdapter(mB2BAccountAdapter);

        mAccountDialog = new MaterialDialog.Builder(mContext)
                .customView(accountView, true)
                .build();

        mPurchaseOrderRv.setLayoutManager(new LinearLayoutManager(mContext));
        mPurchaseOrderRv.setNestedScrollingEnabled(false);
        mPurchaseOrderBeans = new ArrayList<>();
        mPurchaseOrderAdapter = new B2bBusinessListAdapter(mContext, mPurchaseOrderBeans);
        mPurchaseOrderRv.setAdapter(mPurchaseOrderAdapter);

        mCustomerInquiryRv.setLayoutManager(new LinearLayoutManager(mContext));
        mCustomerInquiryRv.setNestedScrollingEnabled(false);
        mCustomerInquiryBeans = new ArrayList<>();
        mCustomerInquiryAdapter = new B2bBusinessListAdapter(mContext, mCustomerInquiryBeans);
        mCustomerInquiryRv.setAdapter(mCustomerInquiryAdapter);

        mPublicInquiryRv.setLayoutManager(new LinearLayoutManager(mContext));
        mPublicInquiryRv.setNestedScrollingEnabled(false);
        mPublicInquiryBeans = new ArrayList<>();
        mPublicInquiryAdapter = new B2bBusinessListAdapter(mContext, mPublicInquiryBeans);
        mPublicInquiryRv.setAdapter(mPublicInquiryAdapter);

        mCompanyBusinessRv.setLayoutManager(new LinearLayoutManager(mContext));
        mCompanyBusinessRv.setNestedScrollingEnabled(false);
        mCompanyBusinessBeans = new ArrayList<>();
        mCompanyBusinessAdapter = new B2bBusinessListAdapter(mContext, mCompanyBusinessBeans);
        mCompanyBusinessRv.setAdapter(mCompanyBusinessAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mBackImageView.setOnClickListener(this);
        mCompanyNameTextView.setOnClickListener(this);
        mPurchaseOrderIv.setOnClickListener(this);
        mPurchaseOrderAllTv.setOnClickListener(this);
        mCustomerInquiryIv.setOnClickListener(this);
        mCustomerInquiryAllTv.setOnClickListener(this);
        mPublicInquiryIv.setOnClickListener(this);
        mPublicInquiryAllTv.setOnClickListener(this);
        mCompanyBusinessAllTv.setOnClickListener(this);
        mAccountSureTextView.setOnClickListener(this);
        mAccountCancelTextView.setOnClickListener(this);

        mHeaderBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                switch (position) {
                    case 0:
                        startActivity(PurchaseOrderListActivity.class);
                        break;
                    case 1:
                        startActivity(CustomerInquiryListActivity.class);
                        break;
                    case 2:
                        startActivity(CompanyBusinessListActivity.class);
                        break;
                    case 3:
                        startActivity(PublicInquiryListActivity.class);
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
                    isPurchaseSuccess = false;
                    isCustomerSuccess = false;
                    isPublicSuccess = false;
                    isCompanySuccess = false;
                    getListData(OBTAIN_PURCHASE_ORDER_LIST);
                    getListData(OBTAIN_CUSTOMER_INQUIRY_LIST);
                    getListData(OBTAIN_PUBLIC_INQUIRY_LIST);
                    getListData(OBTAIN_COMPANY_BUSINESS_LIST);
                } else {
                    toast(R.string.networks_out);
                    mRefreshLayout.finishRefresh(500, false);
                }

            }
        });

        mPurchaseOrderAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                mSelectPosition = i;
                Intent intent = new Intent();
                intent.setClass(mContext, PurchaseDetailsActivity.class);
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mPurchaseOrderBeans.get(i).getId());
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mPurchaseOrderBeans.get(i).getBillState());
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mPurchaseOrderBeans.get(i).getJsonData());
                startActivityForResult(intent, REQUEST_PURCHASE_DETAIL);
            }
        });

        mCustomerInquiryAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                mSelectPosition = i;
                Intent intent = new Intent();
                intent.setClass(mContext, CustomerInquiryDetailActivity.class);
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mCustomerInquiryBeans.get(i).getId());
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mCustomerInquiryBeans.get(i).getBillState());
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mCustomerInquiryBeans.get(i).getJsonData());
                startActivityForResult(intent, REQUEST_CUSTOMER_DETAIL);
            }
        });

        mPublicInquiryAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, final int position) {
                mSelectPosition = position;
                Intent intent = new Intent();
                String billState = mPublicInquiryBeans.get(position).getBillState();
                if (Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE.equals(billState)) {
                    intent.setClass(mContext, PublicInquiryDetailActivity.class);
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mPublicInquiryBeans.get(position).getId());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mPublicInquiryBeans.get(position).getBillState());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mPublicInquiryBeans.get(position).getJsonData());
                    startActivity(intent);
                } else if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(billState)
                        || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(billState)) {
                    intent.setClass(mContext, PublicInquiryQuoteActivity.class);
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mPublicInquiryBeans.get(position).getId());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mPublicInquiryBeans.get(position).getBillState());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mPublicInquiryBeans.get(position).getJsonData());
                    startActivityForResult(intent, REQUEST_PUBLIC_DETAIL);
                }
            }
        });

        mCompanyBusinessAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, final int position) {
                mSelectPosition = position;
                Intent intent = new Intent();
                String billState = mCompanyBusinessBeans.get(position).getBillState();
                if (Constants.FLAG.STATE_COMPANY_BUSINESS_DONE.equals(billState)) {
                    intent.setClass(mContext, PublicInquiryDetailActivity.class);
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mCompanyBusinessBeans.get(position).getId());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mCompanyBusinessBeans.get(position).getBillState());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mCompanyBusinessBeans.get(position).getJsonData());
                    startActivity(intent);
                } else if (Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(billState)
                        || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(billState)) {
                    intent.setClass(mContext, PublicInquiryQuoteActivity.class);
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, mCompanyBusinessBeans.get(position).getId());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE, mCompanyBusinessBeans.get(position).getBillState());
                    intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON, mCompanyBusinessBeans.get(position).getJsonData());
                    startActivityForResult(intent, REQUEST_COMPANY_DETAIL);
                }
            }
        });
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mB2BCompanyBean = (B2BCompanyBean) intent.getSerializableExtra(Constants.FLAG.B2B_COMPANY_BEAN);
            mSpanUtils = new SpanUtils();
            SpannableStringBuilder companyName = mSpanUtils.append(mB2BCompanyBean.getName()).setUnderline().create();
            mCompanyNameTextView.setText(companyName);
        }

        List<Integer> headerImages = new ArrayList<>();
        headerImages.add(R.drawable.ic_b2b_header_purchase);
        headerImages.add(R.drawable.ic_b2b_header_customer);
        headerImages.add(R.drawable.ic_b2b_header_company);
        headerImages.add(R.drawable.ic_b2b_header_public);
        mHeaderBanner.setImages(headerImages);
        mHeaderBanner.start();

        if (CommonUtil.isNetWorkConnected(mContext)) {
            showLoading(null);
            getListData(OBTAIN_PURCHASE_ORDER_LIST);
            getListData(OBTAIN_CUSTOMER_INQUIRY_LIST);
            getListData(OBTAIN_PUBLIC_INQUIRY_LIST);
            getListData(OBTAIN_COMPANY_BUSINESS_LIST);
        } else {
            toast(R.string.networks_out);
        }

    }

    private void getListData(int flag) {
        String host = new ApiPlatform().getBaseUrl();
        String url = "";
        switch (flag) {
            case OBTAIN_PURCHASE_ORDER_LIST:
                url = "mobile/sale/orders/info/search";
                break;
            case OBTAIN_CUSTOMER_INQUIRY_LIST:
                url = "mobile/sale/inquiry/info/search";
                break;
            case OBTAIN_PUBLIC_INQUIRY_LIST:
                host = Constants.API_INQUIRY;
//                url = "inquiry/public/mobile";
                url = "inquiry/public/mobile/v2";
                break;
            case OBTAIN_COMPANY_BUSINESS_LIST:
                host = Constants.API_INQUIRY;
                url = "inquiry/sale/enremind";
                break;
        }
        showLoading(null);

        Map<String, Object> params = new HashMap<>();
        params.put("en_uu", mB2BCompanyBean.getEnuu());
        params.put("user_tel", MyApplication.getInstance().mLoginUser.getTelephone());
        params.put("_state", "");
        params.put("page", 1);
        params.put("size", 3);

        //企业商机参数
        try {
            params.put("enuu", Long.parseLong(mB2BCompanyBean.getEnuu()));
        } catch (Exception e) {
            params.put("enuu", mB2BCompanyBean.getEnuu());
        }
        params.put("useruu", CommonUtil.getUseruuLong(this));
        params.put("pageNumber", 1);
        params.put("pageSize", 3);

        HttpRequest.getInstance().sendRequest(host,
                new HttpParams.Builder()
                        .url(url)
                        .flag(flag)
                        .method(Method.GET)
                        .setHeaders(new HashMap<String, Object>())
                        .setParams(params)
                        .build(), this);
    }

    @Override
    public void showLoading(String loadStr) {
        if (!mRefreshLayout.isRefreshing()) {
            progressDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(0);
        }
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PURCHASE_DETAIL
                && resultCode == Constants.FLAG.RESULT_PURCHASE_ORDER && data != null) {
            String purchaseStatus = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE);
            B2BBusinessListBean b2BBusinessListBean = mPurchaseOrderBeans.get(mSelectPosition);
            if (b2BBusinessListBean != null) {
                b2BBusinessListBean.setBillState(purchaseStatus);
                mPurchaseOrderAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == REQUEST_CUSTOMER_DETAIL
                && resultCode == Constants.FLAG.RESULT_CUSTOMER_INQUIRY) {
            B2BBusinessListBean b2BBusinessListBean = mCustomerInquiryBeans.get(mSelectPosition);
            if (b2BBusinessListBean != null) {
                b2BBusinessListBean.setBillState(Constants.FLAG.STATE_CUSTOMER_INQUIRY_DONE);
                b2BBusinessListBean.setJsonData(null);
            }
            mCustomerInquiryAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_PUBLIC_DETAIL
                && resultCode == Constants.FLAG.RESULT_PUBLIC_INQUIRY && data != null) {
            String newId = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
            B2BBusinessListBean b2BBusinessListBean = mPublicInquiryBeans.get(mSelectPosition);
            if (b2BBusinessListBean != null) {
                b2BBusinessListBean.setBillState(Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE);
                b2BBusinessListBean.setId(newId);
                b2BBusinessListBean.setJsonData(null);
            }
            mPublicInquiryAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_COMPANY_DETAIL
                && resultCode == Constants.FLAG.RESULT_COMPANY_BUSINESS && data != null) {
            String newId = data.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
            B2BBusinessListBean b2BBusinessListBean = mCompanyBusinessBeans.get(mSelectPosition);
            if (b2BBusinessListBean != null) {
                b2BBusinessListBean.setBillState(Constants.FLAG.STATE_COMPANY_BUSINESS_DONE);
                b2BBusinessListBean.setId(newId);
                b2BBusinessListBean.setJsonData(null);
            }
            mCompanyBusinessAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSuccess(int flag, Object o) {
        try {
            String result = o.toString();
            LogUtil.prinlnLongMsg("b2blistlogsuccess", result);
            JSONObject resultObject = JSON.parseObject(result);
            JSONArray contentArray = resultObject.getJSONArray("content");
            switch (flag) {
                case OBTAIN_PURCHASE_ORDER_LIST:
                    isPurchaseSuccess = true;
                    if (isCustomerSuccess && isPublicSuccess && isCompanySuccess) {
                        hideLoading();
                    }
                    if (contentArray == null || contentArray.size() == 0) {
                        mPurchaseOrderBeans.clear();
                        mPurchaseOrderAdapter.notifyDataSetChanged();
                        return;
                    }
                    mPurchaseOrderBeans.clear();
                    analysisPurchaseOrder(contentArray);
                    break;
                case OBTAIN_CUSTOMER_INQUIRY_LIST:
                    isCustomerSuccess = true;
                    if (isPurchaseSuccess && isPublicSuccess && isCompanySuccess) {
                        hideLoading();
                    }
                    if (contentArray == null || contentArray.size() == 0) {
                        mCustomerInquiryBeans.clear();
                        mCustomerInquiryAdapter.notifyDataSetChanged();
                        return;
                    }
                    mCustomerInquiryBeans.clear();
                    analysisCustomerInquiry(contentArray);
                    break;
                case OBTAIN_PUBLIC_INQUIRY_LIST:
                    isPublicSuccess = true;
                    if (isCustomerSuccess && isPurchaseSuccess && isCompanySuccess) {
                        hideLoading();
                    }
                    if (contentArray == null || contentArray.size() == 0) {
                        mPublicInquiryBeans.clear();
                        mPublicInquiryAdapter.notifyDataSetChanged();
                        return;
                    }
                    mPublicInquiryBeans.clear();
                    analisisPublicInquiry(contentArray);
                    break;
                case OBTAIN_COMPANY_BUSINESS_LIST:
                    isCompanySuccess = true;
                    if (isCustomerSuccess && isPurchaseSuccess && isPublicSuccess) {
                        hideLoading();
                    }
                    if (contentArray == null || contentArray.size() == 0) {
                        mCompanyBusinessBeans.clear();
                        mCompanyBusinessAdapter.notifyDataSetChanged();
                        return;
                    }
                    mCompanyBusinessBeans.clear();
                    analisisCompanyBusiness(contentArray);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {

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
                mCompanyBusinessBeans.add(b2BBusinessListBean);
            }
        }
        mCompanyBusinessAdapter.notifyDataSetChanged();
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
                mPublicInquiryBeans.add(b2BBusinessListBean);
            }
        }
        mPublicInquiryAdapter.notifyDataSetChanged();
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

                mCustomerInquiryBeans.add(b2BBusinessListBean);
            }
        }
        mCustomerInquiryAdapter.notifyDataSetChanged();
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

                mPurchaseOrderBeans.add(b2BBusinessListBean);
            }
        }
        mPurchaseOrderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFail(int flag, String failStr) {
        try {
            if (!CommonUtil.isRepeatClick(2000)) {
                toast(failStr);
            }
            switch (flag) {
                case OBTAIN_PURCHASE_ORDER_LIST:
                    isPurchaseSuccess = true;
                    if (isCustomerSuccess && isPublicSuccess && isCompanySuccess) {
                        hideLoading();
                    }
                    break;
                case OBTAIN_CUSTOMER_INQUIRY_LIST:
                    isCustomerSuccess = true;
                    if (isPurchaseSuccess && isPublicSuccess && isCompanySuccess) {
                        hideLoading();
                    }
                    break;
                case OBTAIN_PUBLIC_INQUIRY_LIST:
                    isPublicSuccess = true;
                    if (isCustomerSuccess && isPurchaseSuccess && isCompanySuccess) {
                        hideLoading();
                    }
                    break;
                case OBTAIN_COMPANY_BUSINESS_LIST:
                    isPublicSuccess = true;
                    if (isCustomerSuccess && isPurchaseSuccess && isPublicSuccess) {
                        hideLoading();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.b2b_company_back) {
            onBackPressed();
        } else if (i == R.id.b2b_company_name) {
            popAccountList();
        } else if (i == R.id.b2b_business_main_purchase_order_iv
                || i == R.id.b2b_business_main_purchase_order_all_tv) {
            startActivity(PurchaseOrderListActivity.class);
        } else if (i == R.id.b2b_business_main_customer_inquiry_iv
                || i == R.id.b2b_business_main_customer_inquiry_all_tv) {
            startActivity(CustomerInquiryListActivity.class);
        } else if (i == R.id.b2b_business_main_public_inquiry_iv
                || i == R.id.b2b_business_main_public_inquiry_all_tv) {
            startActivity(PublicInquiryListActivity.class);
        } else if (i == R.id.b2b_business_main_company_business_all_tv) {
            startActivity(CompanyBusinessListActivity.class);
        } else if (i == R.id.b2b_change_account_sure_tv) {
            if (CommonUtil.isNetWorkConnected(mContext)) {
                for (B2BCompanyBean b2BCompanyBean : mB2BCompanyBeans) {
                    if (b2BCompanyBean.isSelected()) {
                        mB2BCompanyBean = b2BCompanyBean;
                        break;
                    }
                }
                CommonUtil.setSharedPreferences(mContext, Constants.CACHE.B2B_BUSINESS_ENUU, mB2BCompanyBean.getEnuu());
                mSpanUtils = new SpanUtils();
                SpannableStringBuilder companyName = mSpanUtils.append(mB2BCompanyBean.getName()).setUnderline().create();
                mCompanyNameTextView.setText(companyName);
                mAccountDialog.dismiss();

                isPurchaseSuccess = false;
                isCustomerSuccess = false;
                isPublicSuccess = false;
                isCompanySuccess = false;
                showLoading(null);
                getListData(OBTAIN_PURCHASE_ORDER_LIST);
                getListData(OBTAIN_CUSTOMER_INQUIRY_LIST);
                getListData(OBTAIN_PUBLIC_INQUIRY_LIST);
                getListData(OBTAIN_COMPANY_BUSINESS_LIST);
            } else {
                toast(R.string.networks_out);
            }

        } else if (i == R.id.b2b_change_account_cancel_tv) {
            mAccountDialog.dismiss();
        }
    }

    private void popAccountList() {
        if (mB2BCompanyBeans == null || mB2BCompanyBeans.size() == 0) {
            getB2BCompanys();
        }
        if (mB2BCompanyBeans == null || mB2BCompanyBeans.size() == 0) {
            toast("您的账号未绑定任何B2B账套");
        } else {
            for (B2BCompanyBean b2BCompanyBean : mB2BCompanyBeans) {
                String enuu = b2BCompanyBean.getEnuu();
                if (enuu != null && enuu.equals(mB2BCompanyBean.getEnuu())) {
                    b2BCompanyBean.setSelected(true);
                } else {
                    b2BCompanyBean.setSelected(false);
                }
            }
            mB2BAccountAdapter.notifyDataSetChanged();
            mAccountDialog.show();
        }
    }

    private void getB2BCompanys() {
        String companyJson = CommonUtil.getSharedPreferences(this, "loginJson");
        if (JSONUtil.validate(companyJson)) {
            JSONArray companyArray = JSON.parseArray(companyJson);
            if (companyArray != null && companyArray.size() > 0) {
                for (int i = 0; i < companyArray.size(); i++) {
                    JSONObject companyObject = companyArray.getJSONObject(i);
                    if (companyObject != null) {
                        String platform = JSONUtil.getText(companyObject, "platform");
                        if ("B2B".equals(platform)) {
                            JSONArray spacesArray = companyObject.getJSONArray("spaces");
                            if (spacesArray != null && spacesArray.size() > 0) {
                                for (int j = 0; j < spacesArray.size(); j++) {
                                    JSONObject spacesObject = spacesArray.getJSONObject(j);
                                    if (spacesObject != null) {
                                        String name = JSONUtil.getText(spacesObject, "name");
                                        B2BCompanyBean b2BCompanyBean = new B2BCompanyBean();
                                        b2BCompanyBean.setId(JSONUtil.getInt(spacesObject, "id"));
                                        b2BCompanyBean.setEnuu(JSONUtil.getText(spacesObject, "enuu"));
                                        b2BCompanyBean.setBusinessCode(JSONUtil.getText(spacesObject, "businessCode"));
                                        b2BCompanyBean.setName(name);
                                        if (mB2BCompanyBean != null && name.equals(mB2BCompanyBean.getName())) {
                                            b2BCompanyBean.setSelected(true);
                                        } else {
                                            b2BCompanyBean.setSelected(false);
                                        }

                                        mB2BCompanyBeans.add(b2BCompanyBean);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

}
