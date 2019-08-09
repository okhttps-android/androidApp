package com.uas.appworks.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.SimplePresenter;
import com.core.base.view.AndroidBug5497Workaround;
import com.core.base.view.SimpleView;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.uas.appworks.R;
import com.uas.appworks.adapter.B2BDetailListAdapter;
import com.uas.appworks.adapter.B2BQuotePriceAdapter;
import com.uas.appworks.model.bean.B2BDetailListBean;
import com.uas.appworks.model.bean.B2BQuotePriceBean;
import com.uas.appworks.model.bean.PublicInquiryItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/7/10 17:10
 */
public class PublicInquiryQuoteActivity extends BaseMVPActivity<SimplePresenter> implements SimpleView, View.OnClickListener {
    private final int FLAG_PUBLIC_QUOTE_DATA = 0x11;
    private final int FLAG_PUBLIC_SAVE_QUOTE = 0x12;

    private RadioGroup mQuoteTabRg;
    private RadioButton mCurrentRb, mReplaceRb;
    private RecyclerView mDataRv, mCurrentDataRv, mCurrentQuoteRv, mReplaceDataRv, mReplaceQuoteRv;
    private ImageView mCurrentAddIv, mReplaceAddIv;
    private Button mCurrentQuoteBtn, mReplaceQuoteBtn;
    private View mCurrentAddView, mReplaceAddView;
    private LinearLayout mCurrentLinearLayout, mReplaceLinearLayout;

    private String mId, mState = "", mJson, mEnuu, mPhone;
    private B2BDetailListAdapter mPublicHeaderAdapter, mCurrentDataAdapter, mReplaceDataAdapter;
    private List<B2BDetailListBean> mPublicHeaderBeans, mCurrentDataBeans, mReplaceDataBeans;

    private B2BQuotePriceAdapter mCurrentQuoteAdapter, mReplaceQuoteAdapter;
    private List<B2BQuotePriceBean> mCurrentQuoteBeans, mReplaceQuoteBeans;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initIntent(intent);
        initData();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_public_inquiry_quote;
    }

    @Override
    protected void initView() {
        AndroidBug5497Workaround.assistActivity(this);
        mDataRv = $(R.id.public_inquiry_quote_data_rv);
        mQuoteTabRg = $(R.id.public_inquiry_quote_tab_rg);
        mCurrentRb = $(R.id.public_inquiry_quote_current_rb);
        mReplaceRb = $(R.id.public_inquiry_quote_replace_rb);

        mCurrentLinearLayout = $(R.id.public_inquiry_quote_current_ll);
        mCurrentDataRv = $(R.id.public_inquiry_quote_current_data_rv);
        mCurrentAddIv = $(R.id.public_inquiry_quote_current_add_iv);
        mCurrentQuoteRv = $(R.id.public_inquiry_quote_current_quote_rv);
        mCurrentQuoteBtn = $(R.id.public_inquiry_quote_current_quote_btn);
        mCurrentAddView = $(R.id.public_inquiry_quote_current_add_line);

        mReplaceLinearLayout = $(R.id.public_inquiry_quote_replace_ll);
        mReplaceDataRv = $(R.id.public_inquiry_quote_replace_data_rv);
        mReplaceAddIv = $(R.id.public_inquiry_quote_replace_add_iv);
        mReplaceQuoteRv = $(R.id.public_inquiry_quote_replace_quote_rv);
        mReplaceQuoteBtn = $(R.id.public_inquiry_quote_replace_quote_btn);
        mReplaceAddView = $(R.id.public_inquiry_quote_replace_add_line);

        mDataRv.setLayoutManager(new LinearLayoutManager(mContext));
        mDataRv.setNestedScrollingEnabled(false);

        mCurrentDataRv.setLayoutManager(new LinearLayoutManager(mContext));
        mCurrentDataRv.setNestedScrollingEnabled(false);
        mCurrentQuoteRv.setLayoutManager(new LinearLayoutManager(mContext));
        mCurrentQuoteRv.setNestedScrollingEnabled(false);

        mReplaceDataRv.setLayoutManager(new LinearLayoutManager(mContext));
        mReplaceDataRv.setNestedScrollingEnabled(false);
        mReplaceQuoteRv.setLayoutManager(new LinearLayoutManager(mContext));
        mReplaceQuoteRv.setNestedScrollingEnabled(false);

        mPublicHeaderBeans = new ArrayList<>();
        mPublicHeaderAdapter = new B2BDetailListAdapter(mContext, mPublicHeaderBeans);
        mDataRv.setAdapter(mPublicHeaderAdapter);
        mCurrentDataBeans = new ArrayList<>();
        mCurrentDataAdapter = new B2BDetailListAdapter(mContext, mCurrentDataBeans);
        mCurrentDataRv.setAdapter(mCurrentDataAdapter);
        mReplaceDataBeans = new ArrayList<>();
        mReplaceDataAdapter = new B2BDetailListAdapter(mContext, mReplaceDataBeans);
        mReplaceDataRv.setAdapter(mReplaceDataAdapter);

        mCurrentQuoteBeans = new ArrayList<>();
        mCurrentQuoteAdapter = new B2BQuotePriceAdapter(mContext, mCurrentQuoteBeans);
        mCurrentQuoteRv.setAdapter(mCurrentQuoteAdapter);
        mReplaceQuoteBeans = new ArrayList<>();
        mReplaceQuoteAdapter = new B2BQuotePriceAdapter(mContext, mReplaceQuoteBeans);
        mReplaceQuoteRv.setAdapter(mReplaceQuoteAdapter);

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
        mPublicHeaderAdapter.setEditable(editable);
        mCurrentDataAdapter.setEditable(editable);
        mReplaceDataAdapter.setEditable(editable);
        mCurrentQuoteAdapter.setEditable(editable);
        mReplaceQuoteAdapter.setEditable(editable);

        mCurrentAddIv.setVisibility(visible);
        mCurrentAddView.setVisibility(visible);
        mReplaceAddIv.setVisibility(visible);
        mReplaceAddView.setVisibility(visible);

        mCurrentQuoteBtn.setVisibility(visible);
        mReplaceQuoteBtn.setVisibility(visible);
    }

    @Override
    protected SimplePresenter initPresenter() {
        return new SimplePresenter();
    }

    @Override
    protected void initEvent() {
        mQuoteTabRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.public_inquiry_quote_current_rb) {
                    mCurrentLinearLayout.setVisibility(View.VISIBLE);
                    mReplaceLinearLayout.setVisibility(View.GONE);
                } else if (id == R.id.public_inquiry_quote_replace_rb) {
                    mCurrentLinearLayout.setVisibility(View.GONE);
                    mReplaceLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mCurrentAddIv.setOnClickListener(this);
        mReplaceAddIv.setOnClickListener(this);

        mCurrentQuoteBtn.setOnClickListener(this);
        mReplaceQuoteBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (mJson != null && JSONUtil.validate(mJson)) {
            initDetailData(mJson);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("en_uu", mEnuu);
            params.put("user_tel", mPhone);
            params.put("id", mId);
            params.put("itemId", mId);

            mPresenter.httpRequest(this, Constants.API_INQUIRY,
                    new HttpParams.Builder()
                            .url("inquiry/sale/publicInquiry/detail")
                            .method(Method.GET)
                            .flag(FLAG_PUBLIC_QUOTE_DATA)
                            .setHeaders(new HashMap<String, Object>())
                            .setParams(params).build());

        }
    }

    private void initDetailData(String detail) {
        if (detail != null && JSONUtil.validate(detail)) {
            mPublicHeaderBeans.clear();
            mCurrentDataBeans.clear();
            mCurrentQuoteBeans.clear();
            mReplaceDataBeans.clear();
            mReplaceQuoteBeans.clear();

            JSONObject detailObject = JSON.parseObject(detail);
            JSONObject inquiryObject = detailObject.getJSONObject("inquiry");
            if (inquiryObject == null) {
                inquiryObject = JSON.parseObject("");
            }
            JSONObject enterpriseObject = inquiryObject.getJSONObject("enterprise");
            JSONObject recorderUser = inquiryObject.getJSONObject("recorderUser");

            String customer = JSONUtil.getText(enterpriseObject, "enName");//客户
            String customerAddress = JSONUtil.getText(enterpriseObject, "enAddress");//客户地址

            String userTel = "", userName = "", material = "",
                    productCode = "", productName = "", productCmpcode = "",
                    productBrand = "", productSpec = "", endDate = "";
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

            productCode = JSONUtil.getText(detailObject, "prodCode");//产品编号
            productName = JSONUtil.getText(detailObject, "prodTitle");//产品名称
            productSpec = JSONUtil.getText(detailObject, "spec");//产品规格
            productCmpcode = JSONUtil.getText(detailObject, "cmpCode");//产品型号
            productBrand = JSONUtil.getText(detailObject, "inbrand");//品牌
            material = productCode + (TextUtils.isEmpty(productName) ? "" : ((TextUtils.isEmpty(productCode) ? "" : ",\n") + productName)
                    + (TextUtils.isEmpty(productSpec) ? "" : ((TextUtils.isEmpty(productCode) ? "" : ",\n") + productSpec)));//物料

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

            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_product_brand), productBrand, "", "品牌", "", 0, null));
            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.public_inquiry_quote_product_name), productName, "", "类目(产品名称)", "", 0, null));
            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.public_inquiry_quote_product_cmpcode), productCmpcode, "", "型号", "", 0, null));
            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.public_inquiry_quote_product_spec), productSpec, "", "规格", "", 0, null));
            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.public_inquiry_quote_needquantity), needquantity, "", "采购数量(PCS)", "", 0, null));
            long endDateLong = inquiryObject.getLongValue("endDate");
            if (endDateLong != 0) {
                endDate = DateFormatUtil.long2Str(endDateLong, DateFormatUtil.YMD);
            }
            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_expirydate), endDate, "", "截止日期", "", 0, null));
            mPublicHeaderBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT, getString(R.string.customer_inquiry_customer_phone), userTel, "", "联系电话", "", 0, null));
            mPublicHeaderAdapter.notifyDataSetChanged();

            /**
             * 当前型号报价
             */
            List<String> options = new ArrayList<>();
            options.add("RMB");
            options.add("USD");
            options.add("HKD");
            options.add("JPY");
            options.add("EUR");
            mCurrentDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_OPTION, getString(R.string.customer_inquiry_currency), TextUtils.isEmpty(currency) ? "RMB" : currency, "", "currency", "", 0, options));
            mCurrentDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_leadtime), leadtime, "天", "leadtime", "", B2BDetailListBean.EDIT_TYPE_NUMBER, null));
            mCurrentDataAdapter.notifyDataSetChanged();


            /**
             * 替代型号报价
             */
            mReplaceDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_OPTION, getString(R.string.customer_inquiry_currency), TextUtils.isEmpty(currency) ? "RMB" : currency, "", "currency", "", 0, options));
            mReplaceDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_product_brand), "", "", "brand", "", B2BDetailListBean.EDIT_TYPE_TEXT, null));
            mReplaceDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.public_inquiry_quote_product_cmpcode), "", "", "model", "", B2BDetailListBean.EDIT_TYPE_TEXT, null));
            mReplaceDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.public_inquiry_quote_product_spec), "", "", "spec", "", B2BDetailListBean.EDIT_TYPE_TEXT, null));
            mReplaceDataBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_EDIT, getString(R.string.customer_inquiry_leadtime), "", "天", "leadtime", "", B2BDetailListBean.EDIT_TYPE_NUMBER, null));
            mReplaceDataAdapter.notifyDataSetChanged();

            JSONArray replies = detailObject.getJSONArray("replies");
            if (replies != null && replies.size() > 0) {
                for (int i = 0; i < replies.size(); i++) {
                    JSONObject replyObject = replies.getJSONObject(i);
                    long id = JSONUtil.getLong(replyObject, "id");
                    String amount = JSONUtil.getText(replyObject, "lapQty");
                    String price = JSONUtil.getText(replyObject, "price");

                    mCurrentQuoteBeans.add(createB2BQuotePriceBean(id, amount, price));
                    mReplaceQuoteBeans.add(createB2BQuotePriceBean(id, amount, price));
                }
            } else {
                mCurrentQuoteBeans.add(createB2BQuotePriceBean(0, "0", ""));
                mReplaceQuoteBeans.add(createB2BQuotePriceBean(0, "0", ""));
            }
            mCurrentQuoteAdapter.notifyDataSetChanged();
            mReplaceQuoteAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.public_inquiry_quote_current_quote_btn) {
            PublicInquiryItem publicInquiryItem = new PublicInquiryItem();

            List<B2BDetailListBean> currentDataBeans = mCurrentDataAdapter.getB2BDetailListBeans();
            List<B2BQuotePriceBean> currentQuoteBeans = mCurrentQuoteAdapter.getB2BQuotePriceBeans();

            String leadtime = "", currency = "";
            for (B2BDetailListBean b2BDetailListBean : currentDataBeans) {
                String flag = b2BDetailListBean.getFlag();
                if ("leadtime".equals(flag)) {
                    leadtime = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(leadtime)) {
                        toast(getString(R.string.delivery_period_can_not_be_empty));
                        return;
                    }
                }
                if ("currency".equals(flag)) {
                    currency = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(currency)) {
                        toast(getString(R.string.currency_can_not_be_empty));
                        return;
                    }
                }
            }
            publicInquiryItem.setSourceId(Long.parseLong(mId));
            publicInquiryItem.setVendUU(Long.valueOf(mEnuu));
            publicInquiryItem.setVendUserUU(Long.valueOf(CommonUtil.getSharedPreferences(mContext, "b2b_uu")));
//            publicInquiryItem.setVendUserUU(1000003217L);
            publicInquiryItem.setLeadtime(Long.valueOf(leadtime));
            publicInquiryItem.setCurrency(currency);
            publicInquiryItem.setIsReplace((short) 0);

            List<PublicInquiryItem.PublicInquiryReply> replies = getQuoteRelies(currentQuoteBeans);
            publicInquiryItem.setReplies(replies);

            saveQuote(publicInquiryItem);
        } else if (i == R.id.public_inquiry_quote_replace_quote_btn) {
            PublicInquiryItem publicInquiryItem = new PublicInquiryItem();

            List<B2BDetailListBean> replaceDataBeans = mReplaceDataAdapter.getB2BDetailListBeans();
            List<B2BQuotePriceBean> replaceQuoteBeans = mReplaceQuoteAdapter.getB2BQuotePriceBeans();

            String leadtime = "", currency = "", brand = "", model = "", spec = "";
            for (B2BDetailListBean b2BDetailListBean : replaceDataBeans) {
                String flag = b2BDetailListBean.getFlag();
                if ("leadtime".equals(flag)) {
                    leadtime = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(leadtime)) {
                        toast(getString(R.string.delivery_period_can_not_be_empty));
                        return;
                    }
                }
                if ("currency".equals(flag)) {
                    currency = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(currency)) {
                        toast(getString(R.string.currency_can_not_be_empty));
                        return;
                    }
                }
                if ("brand".equals(flag)) {
                    brand = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(brand)) {
                        toast(getString(R.string.brand_can_not_be_empty));
                        return;
                    }
                }
                if ("model".equals(flag)) {
                    model = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(model)) {
                        toast(getString(R.string.model_can_not_be_empty));
                        return;
                    }
                }
                if ("spec".equals(flag)) {
                    spec = b2BDetailListBean.getValue();
                    if (TextUtils.isEmpty(spec)) {
                        toast(getString(R.string.spec_can_not_be_empty));
                        return;
                    }
                }
            }

            publicInquiryItem.setSourceId(Long.parseLong(mId));
            publicInquiryItem.setVendUU(Long.valueOf(mEnuu));
            publicInquiryItem.setVendUserUU(Long.valueOf(CommonUtil.getSharedPreferences(mContext, "b2b_uu")));
//            publicInquiryItem.setVendUserUU(1000003217L);
            publicInquiryItem.setLeadtime(Long.valueOf(leadtime));
            publicInquiryItem.setCurrency(currency);
            publicInquiryItem.setReplaceBrand(brand);
            publicInquiryItem.setReplaceCmpCode(model);
            publicInquiryItem.setReplaceSpec(spec);
            publicInquiryItem.setIsReplace((short) 1);

            List<PublicInquiryItem.PublicInquiryReply> replies = getQuoteRelies(replaceQuoteBeans);
            if (replies == null) {
                return;
            }
            publicInquiryItem.setReplies(replies);
            saveQuote(publicInquiryItem);
        } else if (i == R.id.public_inquiry_quote_current_add_iv) {
            mCurrentQuoteBeans.add(createB2BQuotePriceBean(0, "", ""));
            mCurrentQuoteAdapter.notifyDataSetChanged();
        } else if (i == R.id.public_inquiry_quote_replace_add_iv) {
            mReplaceQuoteBeans.add(createB2BQuotePriceBean(0, "", ""));
            mReplaceQuoteAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    private List<PublicInquiryItem.PublicInquiryReply> getQuoteRelies(List<B2BQuotePriceBean> quoteBeans) {
        List<PublicInquiryItem.PublicInquiryReply> replies = new ArrayList<>();
        for (int j = 0; j < quoteBeans.size(); j++) {
            B2BQuotePriceBean b2BQuotePriceBean = quoteBeans.get(j);
            PublicInquiryItem.PublicInquiryReply reply = new PublicInquiryItem.PublicInquiryReply();
            String amount = b2BQuotePriceBean.getAmount();
            String price = b2BQuotePriceBean.getPrice();

            if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(price)) {
                toast(getString(R.string.unfilled_items_in_subsection_quotation));
                return null;
            }

            if (j >= 1) {
                try {
                    double amountN = Double.parseDouble(amount);

                    B2BQuotePriceBean lastB2BQuotePriceBean = quoteBeans.get(j - 1);
                    String lastAmount = lastB2BQuotePriceBean.getAmount();
                    double amountB = Double.parseDouble(lastAmount);
                    if (CommonUtil.doubleCompare(amountN, amountB) <= 0) {
                        toast(getString(R.string.number_of_segments_please_keep_increasing));
                        return null;
                    }
                } catch (Exception e) {

                }
            }
            reply.setLapQty(Double.parseDouble(amount));
            reply.setPrice(Double.parseDouble(price));

            replies.add(reply);
        }
        return replies;
    }

    private void saveQuote(PublicInquiryItem publicInquiryItem) {
        mCurrentRb.setEnabled(false);
        mReplaceRb.setEnabled(false);
        mCurrentQuoteBtn.setEnabled(false);
        mReplaceQuoteBtn.setEnabled(false);

        mPresenter.httpRequest(this, Constants.API_INQUIRY,
                new HttpParams.Builder()
                        .url("/inquiry/sale/item/saveQuote")
                        .method(Method.POSTBODY)
                        .addHeader("Content-Type", "application/json; charset=UTF-8")
                        .flag(FLAG_PUBLIC_SAVE_QUOTE)
                        .postBody(JSON.toJSONString(publicInquiryItem))
                        .build());
    }

    @Override
    public void requestSuccess(int what, Object object) {
        try {
            switch (what) {
                case FLAG_PUBLIC_QUOTE_DATA:
                    if (Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID.equals(mState)
                            || Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID.equals(mState)) {
                        initEditable(false, View.GONE);
                    } else {
                        initEditable(true, View.VISIBLE);
                    }
                    String result = object.toString();
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONObject inquiryItem = resultObject.getJSONObject("inquiryItem");
                    initDetailData(inquiryItem.toString());

                    break;
                case FLAG_PUBLIC_SAVE_QUOTE:
                    setTitle(R.string.str_quoted_price);
                    String publicId = "";
                    result = "";
                    if (object != null) {
                        result = object.toString();
                    }
                    mCurrentRb.setEnabled(true);
                    mReplaceRb.setEnabled(true);
                    mCurrentQuoteBtn.setEnabled(true);
                    mReplaceQuoteBtn.setEnabled(true);
                    if (JSONUtil.validate(result)) {
                        JSONObject publicObject = JSON.parseObject(result);
                        boolean success = JSONUtil.getBoolean(publicObject, "success");
                        if (!success) {
                            String message = JSONUtil.getText(publicObject, "message");
                            if (TextUtils.isEmpty(message)) {
                                message = "报价异常！请稍后重试";
                            }
                            toast(message);
                            return;
                        }
                        initEditable(false, View.GONE);
//                        if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)) {
//                            publicId = JSONUtil.getText(publicObject, "content");
//                        } else if (Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)) {
                        JSONObject contentObject = publicObject.getJSONObject("content");
                        publicId = JSONUtil.getText(contentObject, "id");
//                        }
                        toast(getString(R.string.quote_success));
                        Intent intent = getIntent();
                        intent.putExtra(Constants.FLAG.EXTRA_B2B_LIST_ID, publicId);
                        if (Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO.equals(mState)) {
                            setResult(Constants.FLAG.RESULT_PUBLIC_INQUIRY, intent);
                        } else if (Constants.FLAG.STATE_COMPANY_BUSINESS_TODO.equals(mState)) {
                            setResult(Constants.FLAG.RESULT_COMPANY_BUSINESS, intent);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!PublicInquiryQuoteActivity.this.isFinishing()) {
                                    finish();
                                }
                            }
                        }, 1000);
                    }

                    break;
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
        switch (what) {
            case FLAG_PUBLIC_QUOTE_DATA:
                toast(errorMsg);
                break;
            case FLAG_PUBLIC_SAVE_QUOTE:
                mCurrentRb.setEnabled(true);
                mReplaceRb.setEnabled(true);
                mCurrentQuoteBtn.setEnabled(true);
                mReplaceQuoteBtn.setEnabled(true);
                toast(errorMsg);
                break;
            default:
                break;
        }
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
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

}
