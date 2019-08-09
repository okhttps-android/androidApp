package com.uas.appworks.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
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
 * @describe 公共询价单详情页
 * @date 2018/1/16 15:31
 */

public class PublicInquiryDetailActivity extends BaseMVPActivity<WorkPlatPresenter> implements HttpCallback {
    private final int PUBLIC_INQUIRY_DETAIL = 0x16;

    private RecyclerView mInquiryRecyclerView, mMaterialRecyclerView, mQuoteRecyclerView;
    private TextView mLeadTimeTextView, mMinOrderTextView, mMinPackTextView;
    private List<B2BDetailListBean> mInquiryListBeans, mMaterialListBeans;
    private B2BDetailListAdapter mInquiryListAdapter, mMaterialListAdapter;
    private B2BQuotePriceAdapter mB2BQuotePriceAdapter;
    private List<B2BQuotePriceBean> mB2BQuotePriceBeans;

    private String mId, mState, mJson;
    private String mEnuu;

    @Override
    protected int getLayout() {
        return R.layout.activity_public_inquiry_detail;
    }

    @Override
    protected void initView() {
        setTitle(R.string.title_quoted_price);

        mInquiryRecyclerView = $(R.id.public_inquiry_detail_inquiry_information_rv);
        mMaterialRecyclerView = $(R.id.public_inquiry_detail_material_information_rv);
        mQuoteRecyclerView = $(R.id.public_inquiry_detail_quote_rv);
        mLeadTimeTextView = $(R.id.public_inquiry_detail_leadtime_tv);
        mMinOrderTextView = $(R.id.public_inquiry_detail_min_order_tv);
        mMinPackTextView = $(R.id.public_inquiry_detail_min_pack_tv);

        mInquiryRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mInquiryRecyclerView.setNestedScrollingEnabled(false);
        mMaterialRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mMaterialRecyclerView.setNestedScrollingEnabled(false);
        mQuoteRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mQuoteRecyclerView.setNestedScrollingEnabled(false);

        mInquiryListBeans = new ArrayList<>();
        mInquiryListAdapter = new B2BDetailListAdapter(mContext, mInquiryListBeans);
        mInquiryRecyclerView.setAdapter(mInquiryListAdapter);

        mMaterialListBeans = new ArrayList<>();
        mMaterialListAdapter = new B2BDetailListAdapter(mContext, mMaterialListBeans);
        mMaterialRecyclerView.setAdapter(mMaterialListAdapter);

        mB2BQuotePriceBeans = new ArrayList<>();
        mB2BQuotePriceAdapter = new B2BQuotePriceAdapter(mContext, mB2BQuotePriceBeans);
        mB2BQuotePriceAdapter.setEditable(false);
        mQuoteRecyclerView.setAdapter(mB2BQuotePriceAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            mId = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ID);
            mState = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_STATE);
            mJson = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_JSON);
            mEnuu = intent.getStringExtra(Constants.FLAG.EXTRA_B2B_LIST_ENUU);
        }

        if (TextUtils.isEmpty(mEnuu)) {
            mEnuu = CommonUtil.getSharedPreferences(mContext, Constants.CACHE.B2B_BUSINESS_ENUU);
        }
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        if (mJson != null && JSONUtil.validate(mJson)) {
            initDetailData(mJson);
        } else {
            showLoading(null);

            Map<String, Object> params = new HashMap<>();
            params.put("en_uu", mEnuu);
            params.put("user_tel", MyApplication.getInstance().mLoginUser.getTelephone());
            params.put("id", mId);
            params.put("itemId", mId);

            HttpRequest.getInstance().sendRequest(Constants.API_INQUIRY,
                    new HttpParams.Builder()
                            .url("inquiry/sale/inquiry/detail")
                            .flag(PUBLIC_INQUIRY_DETAIL)
                            .method(Method.GET)
                            .setHeaders(new HashMap<String, Object>())
                            .setParams(params)
                            .build(), this);
        }
    }

    private void initDetailData(String detail) {
        JSONObject contentObject = JSON.parseObject(detail);
        JSONObject inquiryObject = contentObject.getJSONObject("inquiry");
        int isReplace = JSONUtil.getInt(contentObject, "isReplace");

        String inquiryId = "", inquiryCompany = "",
                phone = "", contact = "", enddate = "",
                attachs = "", model = "", brand = "",
                spec = "", material = "", unit = "",
                amount = "", currency = "", taxrate = "",
                replaceBrand = "", replaceCmpCode = "", replaceSpec = "";

        amount = JSONUtil.getText(contentObject, "needquantity");
        contact = JSONUtil.getText(contentObject, "userName");
        phone = JSONUtil.getText(contentObject, "userTel");
        currency = JSONUtil.getText(contentObject, "currency");
        taxrate = JSONUtil.getText(contentObject, "taxrate");

        model = JSONUtil.getText(contentObject, "cmpCode");
        brand = JSONUtil.getText(contentObject, "inbrand");
        spec = JSONUtil.getText(contentObject, "spec");
        material = JSONUtil.getText(contentObject, "prodTitle");
        unit = JSONUtil.getText(contentObject, "unit");
        replaceBrand = JSONUtil.getText(contentObject, "replaceBrand");
        replaceCmpCode = JSONUtil.getText(contentObject, "replaceCmpCode");
        replaceSpec = JSONUtil.getText(contentObject, "replaceSpec");
        if (inquiryObject != null) {
            inquiryId = JSONUtil.getText(inquiryObject, "code");
            long endDate = inquiryObject.getLongValue("endDate");
            if (endDate != 0) {
                enddate = DateFormatUtil.long2Str(endDate, DateFormatUtil.YMD);
            }

            JSONObject enterpriseObject = inquiryObject.getJSONObject("enterprise");
            if (enterpriseObject != null) {
                inquiryCompany = JSONUtil.getText(enterpriseObject, "enName");
            }

            //附件
            /*JSONArray attachsArray = inquiryObject.getJSONArray("attachs");
            if (attachsArray != null && attachsArray.size() > 0) {
                SpanUtils spanUtils = new SpanUtils();
                for (int i = 0; i < attachsArray.size(); i++) {
                    final B2BAttachBean b2BAttachBean = new B2BAttachBean();

                    JSONObject attachObject = attachsArray.getJSONObject(i);
                    String name = JSONUtil.getText(attachObject, "name");
                    String path = JSONUtil.getText(attachObject, "path");
                    long size = attachObject.getLongValue("size");

                    b2BAttachBean.setName(name);
                    b2BAttachBean.setPath(path);
                    b2BAttachBean.setSize(size);

                    spanUtils.append(name + ((i == (attachsArray.size() - 1)) ? "" : "\n")).setUnderline()
                            .setForegroundColor(getResources().getColor(R.color.titleBlue)).setClickSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            gotoReadEnclosure(b2BAttachBean);
                        }
                    });
                }
                attachs = spanUtils.create().toString();
            }*/
        }

        /**
         * 询价信息
         */
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_brand), brand, "", "品牌", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_material), material, "", "物料名称", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_model), model, "", "型号", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_spec), spec, "", "规格", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_id), inquiryId, "", "询价编号", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_company), inquiryCompany, "", "询价企业", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_contact), contact, "", "联系人", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_phone), phone, "", "联系电话", ""));
        mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_enddate), enddate, "", "报价截止日期", ""));
        if (!TextUtils.isEmpty(attachs)) {
            mInquiryListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_attachs), attachs, "", "附件", ""));
        }

        mInquiryListAdapter.notifyDataSetChanged();

        /**
         * 报价信息
         */
        if (isReplace == 1) {
            mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_brand), replaceBrand, "", "品牌", ""));
            mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_model), replaceCmpCode, "", "型号", ""));
            mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_spec), replaceSpec, "", "规格", ""));
        }
        mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_unit), unit, "", "单位", ""));
        if (!TextUtils.isEmpty(amount)) {
            mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_amount), amount, "", "数量", ""));
        }
        mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.customer_inquiry_currency), currency, "", "币别", ""));
        if (!TextUtils.isEmpty(taxrate)) {
            mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.public_inquiry_taxrate), taxrate, "", "税率", ""));
        }
        String leadtime = JSONUtil.getText(contentObject, "leadtime");//交货周期
        String minOrderQty = JSONUtil.getText(contentObject, "minOrderQty");//最小起订
        String minPackQty = JSONUtil.getText(contentObject, "minPackQty");//最小包装
        mMaterialListBeans.add(createB2BDetailListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.customer_inquiry_leadtime), leadtime, "", "交货周期", ""));

        mMaterialListAdapter.notifyDataSetChanged();


        mLeadTimeTextView.setText(leadtime);
        mMinOrderTextView.setText(minOrderQty);
        mMinPackTextView.setText(minPackQty);

        JSONArray replies = contentObject.getJSONArray("replies");
        if (replies != null && replies.size() > 0) {
            for (int i = 0; i < replies.size(); i++) {
                JSONObject replyObject = replies.getJSONObject(i);
                long id = JSONUtil.getLong(replyObject, "id");
                String lapQty = JSONUtil.getText(replyObject, "lapQty");
                String price = JSONUtil.getText(replyObject, "price");

                mB2BQuotePriceBeans.add(createB2BQuotePriceBean(id, lapQty, price));
            }
        } else {
            mB2BQuotePriceBeans.add(createB2BQuotePriceBean(0, "0", ""));
        }
        mB2BQuotePriceAdapter.notifyDataSetChanged();
    }

    private B2BDetailListBean createB2BDetailListBean(int itemType,
                                                      String caption,
                                                      String value,
                                                      String unit,
                                                      String flag,
                                                      String type) {
        B2BDetailListBean b2BDetailListBean = new B2BDetailListBean();

        b2BDetailListBean.setItemType(itemType);
        b2BDetailListBean.setCaption(caption);
        b2BDetailListBean.setFlag(flag);
        b2BDetailListBean.setValue(value);
        b2BDetailListBean.setType(type);
        b2BDetailListBean.setUnit(unit);

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
    public void onSuccess(int flag, Object o) throws Exception {
        hideLoading();
        String result = o.toString();
        LogUtil.prinlnLongMsg("publicdetailsuccess", result);
        if (flag == PUBLIC_INQUIRY_DETAIL) {
            if (JSONUtil.validate(result)) {
                initDetailData(result);
            } else {
                initDetailData("");
                toast("报价信息为空");
            }
        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        hideLoading();
        LogUtil.prinlnLongMsg("publicdetailfail", failStr);
        if (flag == PUBLIC_INQUIRY_DETAIL) {
            toast(failStr);
        }
    }


    private boolean isImage(String name) {
        return name.toUpperCase().endsWith("jpeg".toUpperCase())
                || name.toUpperCase().endsWith("jpg".toUpperCase())
                || name.toUpperCase().endsWith("png".toUpperCase());
    }
}
