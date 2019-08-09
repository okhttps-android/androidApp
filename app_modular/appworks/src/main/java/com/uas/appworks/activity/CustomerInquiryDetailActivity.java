package com.uas.appworks.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.uas.appworks.R;
import com.uas.appworks.adapter.B2BDetailListAdapter;
import com.uas.appworks.adapter.B2BQuotePriceAdapter;
import com.uas.appworks.model.bean.B2BDetailListBean;
import com.uas.appworks.model.bean.B2BQuotePriceBean;
import com.uas.appworks.presenter.WorkPlatPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 客户询价单详情页
 * @date 2018/1/16 16:46
 */

public class CustomerInquiryDetailActivity extends BaseMVPActivity<WorkPlatPresenter> implements View.OnClickListener, HttpCallback {
    private final int CUSTOMER_INQUIRY_REPLY = 0x14;
    private final int CUSTOMER_INQUIRY_DETAIL = 0x15;
    private final int PUBLIC_INQUIRY_DETAIL = 0x16;

    private RecyclerView mDataRecyclerView, mQuoteRecyclerView;
    private ImageView mAddImageView;
    private View mAddLine;
    private Button mQuoteButton;
    private B2BDetailListAdapter mB2BDetailListAdapter;
    private List<B2BDetailListBean> mB2BDetailListBeans;
    private B2BQuotePriceAdapter mB2BQuotePriceAdapter;
    private List<B2BQuotePriceBean> mB2BQuotePriceBeans;

    private String mId, mState = "", mJson, mEnuu, mPhone;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initIntent(intent);
        initData();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_customer_inquiry_detail;
    }

    @Override
    protected void initView() {
        AndroidBug5497Workaround.assistActivity(this);
        mDataRecyclerView = $(R.id.customer_inquiry_detail_data_rv);
        mQuoteRecyclerView = $(R.id.customer_inquiry_detail_quote_rv);
        mAddImageView = $(R.id.customer_inquiry_detail_add_iv);
        mAddLine = $(R.id.customer_inquiry_detail_add_line);
        mQuoteButton = $(R.id.customer_inquiry_detail_quote_btn);

        mDataRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mDataRecyclerView.setNestedScrollingEnabled(false);
        mQuoteRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mQuoteRecyclerView.setNestedScrollingEnabled(false);

        mB2BDetailListBeans = new ArrayList<>();
        mB2BDetailListAdapter = new B2BDetailListAdapter(mContext, mB2BDetailListBeans);
        mDataRecyclerView.setAdapter(mB2BDetailListAdapter);

        mB2BQuotePriceBeans = new ArrayList<>();
        mB2BQuotePriceAdapter = new B2BQuotePriceAdapter(mContext, mB2BQuotePriceBeans);
        mQuoteRecyclerView.setAdapter(mB2BQuotePriceAdapter);

        Intent intent = getIntent();
        initIntent(intent);
    }

    private void initIntent(Intent intent) {
        if (intent != null) {
            mId = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
            mState = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE);
            mJson = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON);
            mEnuu = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ENUU);
            mPhone = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_TEL);

            if (Constants.FLAG.GET_LOCAL_ENUU.equals(mEnuu)) {
                mEnuu = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu");
            } else {
                mEnuu = CommonUtil.getSharedPreferences(mContext, Constants.CACHE.B2B_BUSINESS_ENUU);
            }

            if (TextUtils.isEmpty(mPhone)) {
                mPhone = MyApplication.getInstance().mLoginUser.getTelephone();
            }

            if (mState != null) {
                switch (mState) {
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO:
                        setTitle(R.string.str_wait_quoted);
                        initEditable(true, View.VISIBLE);
                        break;
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_DONE:
                        setTitle(R.string.str_quoted_price);
                        if (mJson != null && JSONUtil.validate(mJson)) {
                            JSONObject detailObject = JSON.parseObject(mJson);
                            JSONObject inquiryObject = detailObject.getJSONObject("inquiry");
                            String checked = JSONUtil.getText(inquiryObject, "checked");
                            String overdue = JSONUtil.getText(inquiryObject, "overdue");
                            if (!"1".equals(checked) || "0".equals(overdue)) {
                                initEditable(true, View.VISIBLE);
                                mQuoteButton.setText(R.string.modify_quote_price);
                            } else {
                                initEditable(false, View.GONE);
                            }
                        } else {
                            initEditable(false, View.GONE);
                        }
                        break;
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_AGREED:
                        setTitle(R.string.str_quotation_adopted);
                        initEditable(false, View.GONE);
                        break;
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_END:
                        setTitle(R.string.str_expired);
                        initEditable(false, View.GONE);
                        break;
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_REFUSED:
                        setTitle(R.string.str_quotation_unadopted);
                        initEditable(false, View.GONE);
                        break;
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_INVALID:
                        setTitle(R.string.str_invalid);
                        initEditable(false, View.GONE);
                        break;
                    case Constants.FLAG.STATE_CUSTOMER_INQUIRY_ABANDONED:
                        setTitle(R.string.str_abandoned);
                        initEditable(false, View.GONE);
                        break;
                    case Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO:
                    case Constants.FLAG.STATE_COMPANY_BUSINESS_TODO:
                        setTitle(R.string.str_wait_quoted);
                        initEditable(true, View.VISIBLE);
                        break;
                    case Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID:
                    case Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID:
                        setTitle(R.string.str_finished);
                        initEditable(false, View.GONE);
                    default:
                        initEditable(false, View.GONE);
                        break;
                }
            }
        }
    }

    private void initEditable(boolean editable, int visible) {
        mB2BDetailListAdapter.setEditable(editable);
        mB2BQuotePriceAdapter.setEditable(editable);

        mAddImageView.setVisibility(visible);
        mAddLine.setVisibility(visible);

        mQuoteButton.setVisibility(visible);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mAddImageView.setOnClickListener(this);

        mQuoteButton.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (mJson != null && JSONUtil.validate(mJson)) {
            initDetailData(mJson);
        } else {
            showLoading(null);

            Map<String, Object> params = new HashMap<>();
            params.put("en_uu", mEnuu);
            params.put("user_tel", mPhone);
            params.put("id", mId);
            params.put("itemId", mId);

            String host = new ApiPlatform().getBaseUrl();
            String url = "mobile/sale/inquiry/" + mId + "/info";
            int flag = CUSTOMER_INQUIRY_DETAIL;
            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                host = Constants.API_INQUIRY;
                url = "inquiry/sale/publicInquiry/detail";
                flag = PUBLIC_INQUIRY_DETAIL;
            }
            HttpRequest.getInstance().sendRequest(host
                    , new HttpParams.Builder()
                            .url(url)
                            .method(Method.GET)
                            .flag(flag)
                            .setHeaders(new HashMap<String, Object>())
                            .setParams(params).build(), this);

        }
    }

    private void initDetailData(String detail) {
        if (detail != null && JSONUtil.validate(detail)) {
            mB2BDetailListBeans.clear();
            mB2BQuotePriceBeans.clear();

            JSONObject detailObject = JSON.parseObject(detail);
            JSONObject inquiryObject = detailObject.getJSONObject("inquiry");
            JSONObject productObject = detailObject.getJSONObject("product");
            if (inquiryObject == null) {
                inquiryObject = JSON.parseObject("");
            }
            JSONObject enterpriseObject = inquiryObject.getJSONObject("enterprise");
            JSONObject recorderUser = inquiryObject.getJSONObject("recorderUser");

            String customer = JSONUtil.getText(enterpriseObject, "enName");//客户
            String customerAddress = JSONUtil.getText(enterpriseObject, "enAddress");//客户地址

            String userTel = "", userName = "", material = "", productCode = "", productName = "", productCmpcode = "", productBrand = "", productSpec = "";
            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                userTel = JSONUtil.getText(detailObject, "userTel");
                userName = JSONUtil.getText(detailObject, "userName");
            } else if (enterpriseObject != null) {
                userTel = JSONUtil.getText(enterpriseObject, "enTel");//客户联系电话
            }

            String billNum = JSONUtil.getText(inquiryObject, "code");//单据

            if (productObject != null) {
                String title = JSONUtil.getText(productObject, "title");
                String spec = JSONUtil.getText(productObject, "spec");
                String code = JSONUtil.getText(productObject, "code");
                material = code + (TextUtils.isEmpty(title) ? "" : ((TextUtils.isEmpty(code) ? "" : ",\n") + title)
                        + (TextUtils.isEmpty(spec) ? "" : ((TextUtils.isEmpty(code) ? "" : ",\n") + spec)));//物料

                productCode = JSONUtil.getText(productObject, "code");//产品编号
                productName = JSONUtil.getText(productObject, "title");//产品名称
                productCmpcode = JSONUtil.getText(productObject, "cmpCode");//产品型号
                productBrand = JSONUtil.getText(productObject, "brand");//品牌
                productSpec = JSONUtil.getText(productObject, "spec");//产品规格
            }

            String encapsulation = JSONUtil.getText(detailObject, "encapsulation");//封装
            String needquantity = JSONUtil.getText(detailObject, "needquantity");//采购数量
            String unitPrice = JSONUtil.getText(detailObject, "unitPrice");//单价
            String produceDate = JSONUtil.getText(detailObject, "produceDate");//生产日期
            String currency = JSONUtil.getText(detailObject, "currency");//币别
            String taxrate = JSONUtil.getText(detailObject, "taxrate");//税率
            String environment = JSONUtil.getText(inquiryObject, "environment");//环保要求
            String priceType = JSONUtil.getText(inquiryObject, "priceType");//价格类型

            String leadtime = JSONUtil.getText(detailObject, "leadtime");//交货周期
            String minOrderQty = JSONUtil.getText(detailObject, "minOrderQty");//最小起订
            String minPackQty = JSONUtil.getText(detailObject, "minPackQty");//最小包装

            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_customer), customer, "", "客户", "", 0, null));
            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_customer_address), customerAddress, "", "客户地址", "", 0, null));
            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.public_inquiry_contact), userName, "", "联系人", "", 0, null));
            }
            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_customer_phone), userTel, "", "联系电话", "", 0, null));
            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_bill_num), billNum, "", "单据", "", 0, null));

            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                if (!TextUtils.isEmpty(productCode)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_product_code), productCode, "", "产品编号", "", 0, null));
                }
                if (!TextUtils.isEmpty(productName)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_product_name), productName, "", "产品名称", "", 0, null));
                }
                if (!TextUtils.isEmpty(productCmpcode)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_product_cmpcode), productCmpcode, "", "产品型号", "", 0, null));
                }
                if (!TextUtils.isEmpty(productBrand)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_product_brand), productBrand, "", "品牌", "", 0, null));
                }
                if (!TextUtils.isEmpty(productSpec) && !productSpec.equals(productCmpcode)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_product_spec), productSpec, "", "产品规格", "", 0, null));
                }
                if (!TextUtils.isEmpty(encapsulation)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_encapsulation), encapsulation, "", "封装", "", 0, null));
                }
                if (!TextUtils.isEmpty(needquantity)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_needquantity), needquantity, "", "采购数量", "", 0, null));
                }
                if (!TextUtils.isEmpty(unitPrice) && !TextUtils.isEmpty(currency)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_unitPrice), unitPrice + "(" + currency + ")", "", "单价预算", "", 0, null));
                }
                if (!TextUtils.isEmpty(produceDate)) {
                    mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_produceDate), produceDate, "", "生产日期", "", 0, null));
                }
            } else {
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_material), material, "", "物料", "", 0, null));
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_currency), currency, "", "币别", "", 0, null));
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_taxrate), taxrate, "", "税率", "", 0, null));
            }
            if (!TextUtils.isEmpty(environment)) {
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_environment), environment, "", "环保要求", "", 0, null));
            }
            if (!(Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState))) {
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_price_type), priceType, "", "价格类型", "", 0, null));
            }

            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                List<String> options = new ArrayList<>();
                options.add("RMB");
                options.add("USD");
                options.add("HKD");
                options.add("JPY");
                options.add("EUR");
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_OPTION, getString(R.string.customer_inquiry_currency), currency, "", "currency", "", 0, options));
                mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_taxrate), taxrate, "%", "taxrate", "", B2BDetailListBean.EDIT_TYPE_DECIMAL, null));
            }
            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_leadtime), leadtime, "天", "leadtime", "", B2BDetailListBean.EDIT_TYPE_NUMBER, null));
            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_min_order), minOrderQty, "PCS", "minOrderQty", "", B2BDetailListBean.EDIT_TYPE_NUMBER, null));
            mB2BDetailListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_min_pack), minPackQty, "PCS", "minPackQty", "", B2BDetailListBean.EDIT_TYPE_NUMBER, null));

            mB2BDetailListAdapter.notifyDataSetChanged();

            JSONArray replies = detailObject.getJSONArray("replies");
            if (replies != null && replies.size() > 0) {
                for (int i = 0; i < replies.size(); i++) {
                    JSONObject replyObject = replies.getJSONObject(i);
                    long id = JSONUtil.getLong(replyObject, "id");
                    String amount = JSONUtil.getText(replyObject, "lapQty");
                    String price = JSONUtil.getText(replyObject, "price");

                    mB2BQuotePriceBeans.add(createB2BQuotePriceBean(id, amount, price));
                }
            } else {
                mB2BQuotePriceBeans.add(createB2BQuotePriceBean(0, "0", ""));
            }
            mB2BQuotePriceAdapter.notifyDataSetChanged();
        }
    }

    private B2BDetailListBean createB2BDetailListBean(int itemType,
                                                      String caption,
                                                      String value,
                                                      String unit,
                                                      String flag,
                                                      String type,
                                                      int editType,
                                                      List<String> options) {
        B2BDetailListBean b2BDetailListBean = new B2BDetailListBean();

        b2BDetailListBean.setItemType(itemType);
        b2BDetailListBean.setCaption(caption);
        b2BDetailListBean.setFlag(flag);
        b2BDetailListBean.setValue(value);
        b2BDetailListBean.setType(type);
        b2BDetailListBean.setUnit(unit);
        b2BDetailListBean.setEditType(editType);
        b2BDetailListBean.setOptions(options);

        return b2BDetailListBean;
    }

    private B2BQuotePriceBean createB2BQuotePriceBean(long id, String amount, String price) {
        B2BQuotePriceBean b2BQuotePriceBean = new B2BQuotePriceBean();

        b2BQuotePriceBean.setId(id);
        b2BQuotePriceBean.setAmount(amount);
        b2BQuotePriceBean.setPrice(price);

        return b2BQuotePriceBean;
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.customer_inquiry_detail_add_iv) {
            mB2BQuotePriceBeans.add(createB2BQuotePriceBean(0, "", ""));
            mB2BQuotePriceAdapter.notifyDataSetChanged();
        } else if (i == R.id.customer_inquiry_detail_quote_btn) {
            if (TextUtils.isEmpty(mEnuu)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.prompt_title)
                        .content(R.string.notice_cannot_quote)
                        .positiveText(R.string.have_knew)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                materialDialog.dismiss();
                            }
                        }).build().show();
                return;
            }
            List<B2BQuotePriceBean> b2BQuotePriceBeans = mB2BQuotePriceAdapter.getB2BQuotePriceBeans();
            List<B2BDetailListBean> b2BDetailListBeans = mB2BDetailListAdapter.getB2BDetailListBeans();

            String leadtime = "", minPackQty = "", minOrderQty = "", currency = "", taxrate = "";
            for (B2BDetailListBean b2BDetailListBean : b2BDetailListBeans) {
                String flag = b2BDetailListBean.getFlag();
                if ("leadtime".equals(flag)) {
                    leadtime = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(leadtime)) {
                        toast(getString(R.string.delivery_period_can_not_be_empty));
                        return;
                    }
                }
                if ("minPackQty".equals(flag)) {
                    minPackQty = b2BDetailListBean.getValue();
                }
                if ("minOrderQty".equals(flag)) {
                    minOrderQty = b2BDetailListBean.getValue();
                }
                if ("currency".equals(flag)) {
                    currency = b2BDetailListBean.getValue();
                }
                if ("taxrate".equals(flag)) {
                    taxrate = b2BDetailListBean.getValue();
                }
            }
            List<Reply> replies = new ArrayList<>();
            for (int j = 0; j < b2BQuotePriceBeans.size(); j++) {
                B2BQuotePriceBean b2BQuotePriceBean = b2BQuotePriceBeans.get(j);
                Reply reply = new Reply();
                String amount = b2BQuotePriceBean.getAmount();
                String price = b2BQuotePriceBean.getPrice();

                if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(price)) {
                    toast(getString(R.string.unfilled_items_in_subsection_quotation));
                    return;
                }

                if (j >= 1) {
                    try {
                        double amountN = Double.parseDouble(amount);

                        B2BQuotePriceBean lastB2BQuotePriceBean = b2BQuotePriceBeans.get(j - 1);
                        String lastAmount = lastB2BQuotePriceBean.getAmount();
                        double amountB = Double.parseDouble(lastAmount);
                        if (CommonUtil.doubleCompare(amountN, amountB) <= 0) {
                            toast(getString(R.string.number_of_segments_please_keep_increasing));
                            return;
                        }
                    } catch (Exception e) {

                    }
                }
                reply.setLapQty(amount);
                reply.setPrice(price);

                replies.add(reply);
            }

            String repliesJson = JSON.toJSONString(replies);

            Map<String, Object> params = new HashMap<>();
            params.put("en_uu", mEnuu);
            params.put("user_tel", mPhone);
            params.put("inquiryItemId", mId);
            params.put("replies", repliesJson);
            params.put("leadtime", leadtime);
            params.put("minPackQty", minPackQty);
            params.put("minOrderQty", minOrderQty);
            params.put("currency", currency);
            try {
                params.put("taxrate", Float.parseFloat(taxrate));
            } catch (Exception e) {

            }

            params.put("useruu", CommonUtil.getSharedPreferences(mContext, "b2b_uu"));

            LogUtil.d("inquiryparam", params.toString());
            showLoading(null);
            String url = "mobile/sale/inquiry/items/" + mId + "/reply";
            String host = new ApiPlatform().getBaseUrl();
            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                url = "inquiry/sale/mobile/quote";
                host = Constants.API_INQUIRY;
            }
            HttpRequest.getInstance().sendRequest(host
                    , new HttpParams.Builder()
                            .url(url)
                            .method(Method.POST)
                            .flag(CUSTOMER_INQUIRY_REPLY)
                            .setHeaders(new HashMap<String, Object>())
                            .setParams(params).build(), this);
        }
    }

    @Override
    public void onSuccess(int flag, Object o) {
        hideLoading();
        String result = o.toString();
        LogUtil.prinlnLongMsg("customerdetailsuccess", result);
        if (flag == CUSTOMER_INQUIRY_REPLY) {
            setTitle(R.string.str_quoted_price);
            toast(getString(R.string.quote_success));
            String publicId = "";
            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)) {
                initEditable(false, View.GONE);
                if (JSONUtil.validate(result)) {
                    JSONObject publicObject = JSON.parseObject(result);
                    publicId = JSONUtil.getText(publicObject, "content");
                }
                Intent intent = getIntent();
                intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, publicId);
                if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)) {
                    setResult(Constants.FLAG.RESULT_PUBLIC_INQUIRY, intent);
                } else if (Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)) {
                    setResult(Constants.FLAG.RESULT_COMPANY_BUSINESS, intent);
                }
            } else {
                setResult(Constants.FLAG.RESULT_CUSTOMER_INQUIRY);
            }

            initEditable(false, View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!CustomerInquiryDetailActivity.this.isFinishing()) {
                        finish();
                    }
                }
            }, 1000);
        } else if (flag == CUSTOMER_INQUIRY_DETAIL) {

            if (result != null && JSONUtil.validate(result)
                    && (Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO.equals(mState)
                    || Constants.FLAG.STATE_CUSTOMER_INQUIRY_DONE.equals(mState))) {
                JSONObject contentObject = JSON.parseObject(result);
                JSONObject inquiryObject = contentObject.getJSONObject("inquiry");
                String checked = JSONUtil.getText(inquiryObject, "checked");
                String overdue = JSONUtil.getText(inquiryObject, "overdue");
                if (!"1".equals(checked) || "0".equals(overdue)) {
                    initEditable(true, View.VISIBLE);
                } else {
                    initEditable(false, View.GONE);
                }
            } else {
                initEditable(false, View.GONE);
            }
            initDetailData(result);

        } else if (flag == PUBLIC_INQUIRY_DETAIL) {
            if (Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                    || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                initEditable(false, View.GONE);
            } else {
                initEditable(true, View.VISIBLE);
            }

            JSONObject resultObject = JSON.parseObject(result);
            JSONObject inquiryItem = resultObject.getJSONObject("inquiryItem");
            initDetailData(inquiryItem.toString());
        }
    }

    @Override
    public void onFail(int flag, String failStr) {
        hideLoading();
        LogUtil.prinlnLongMsg("customerdetailfail", failStr);
        toast(failStr);
    }

    public static class Reply {

        /**
         * lapQty : 10
         * price : 8.88
         */

        @JSONField(name = "lapQty")
        private String lapQty;
        @JSONField(name = "price")
        private String price;

        public String getLapQty() {
            return lapQty;
        }

        public void setLapQty(String lapQty) {
            this.lapQty = lapQty;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
