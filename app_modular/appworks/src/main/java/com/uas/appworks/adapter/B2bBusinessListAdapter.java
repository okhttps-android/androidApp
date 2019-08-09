package com.uas.appworks.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.core.app.Constants;
import com.core.utils.SpanUtils;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.B2BBusinessListBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/15 11:50
 */

public class B2bBusinessListAdapter extends BaseMultiItemQuickAdapter<B2BBusinessListBean, BaseViewHolder> {
    private SpanUtils mSpanUtils;

    public B2bBusinessListAdapter(Context context, List<B2BBusinessListBean> b2BBusinessListBeans) {
        super(b2BBusinessListBeans);
        addItemType(B2BBusinessListBean.PURCHASE_ORDER_LIST, R.layout.layout_list_purchase_order);
        addItemType(B2BBusinessListBean.CUSTOMER_INQUIRY_LIST, R.layout.layout_list_customer_inquiry);
        addItemType(B2BBusinessListBean.PUBLIC_INQUIRY_LIST, R.layout.layout_list_public_inquiry);
        addItemType(B2BBusinessListBean.COMPANY_BUSINESS_LIST, R.layout.layout_list_public_inquiry);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, B2BBusinessListBean b2BBusinessListBean) {
        if (baseViewHolder != null) {
            switch (baseViewHolder.getItemViewType()) {
                case B2BBusinessListBean.PURCHASE_ORDER_LIST:
                    bindPurchaseOrder(baseViewHolder, b2BBusinessListBean);
                    break;
                case B2BBusinessListBean.CUSTOMER_INQUIRY_LIST:
                    bindCustomerInquiry(baseViewHolder, b2BBusinessListBean);
                    break;
                case B2BBusinessListBean.PUBLIC_INQUIRY_LIST:
                    bindPublicInquiry(baseViewHolder, b2BBusinessListBean);
                    break;
                case B2BBusinessListBean.COMPANY_BUSINESS_LIST:
                    bindCompanyBusiness(baseViewHolder, b2BBusinessListBean);
                    break;
            }
        }
    }

    private void bindCompanyBusiness(BaseViewHolder holder, B2BBusinessListBean b2BBusinessListBean) {
        holder.setText(R.id.list_public_inquiry_company_tv, b2BBusinessListBean.getCompanyName());
        holder.setText(R.id.list_public_inquiry_bill_date_tv, b2BBusinessListBean.getBillDate());
        holder.setText(R.id.list_public_inquiry_product_name_tv, b2BBusinessListBean.getProductName());
        holder.setText(R.id.list_public_inquiry_product_model_tv, b2BBusinessListBean.getProductModel());
        holder.setText(R.id.list_public_inquiry_product_specification_tv, b2BBusinessListBean.getProductSpecification());
        holder.setText(R.id.list_public_inquiry_product_amount_tv, b2BBusinessListBean.getProductAmount());
        holder.setText(R.id.list_public_inquiry_product_brand_tv, b2BBusinessListBean.getProductBrand());
        holder.setText(R.id.list_public_inquiry_expiry_date_tv, b2BBusinessListBean.getExpiryDate());

        mSpanUtils = new SpanUtils();
        SpannableStringBuilder stringBuilder = mSpanUtils.append(mContext.getString(R.string.str_remain))
                .append(b2BBusinessListBean.getRemainTime() + "").setForegroundColor(Color.RED)
                .setBold().setFontSize(24, true)
                .append(mContext.getString(R.string.str_days)).create();
        if (stringBuilder != null) {
            holder.setText(R.id.list_public_inquiry_remaintime_tv, stringBuilder);
        }

        String billState = b2BBusinessListBean.getBillState();
        if (billState != null) {
            switch (billState) {
                case Constants.FLAG.STATE_COMPANY_BUSINESS_TODO:
                    setCompanyBtn(holder, true, false, false);
                    break;
                case Constants.FLAG.STATE_COMPANY_BUSINESS_DONE:
                    setCompanyBtn(holder, false, true, false);
                    break;
                case Constants.FLAG.STATE_COMPANY_BUSINESS_INVALID:
                    setCompanyBtn(holder, false, false, true);
                    break;
                default:
                    setCompanyBtn(holder, false, false, false);
                    break;
            }
        } else {
            setCompanyBtn(holder, false, false, false);
        }

    }

    private void setCompanyBtn(BaseViewHolder holder, boolean b1, boolean b2, boolean b3) {
        holder.setVisible(R.id.list_public_inquiry_offer_quotation_btn, b1);
        holder.setVisible(R.id.list_public_inquiry_done_btn, b2);
        holder.setVisible(R.id.list_public_inquiry_invalid_btn, b3);
    }


    private void bindPublicInquiry(BaseViewHolder holder, B2BBusinessListBean b2BBusinessListBean) {
        holder.setText(R.id.list_public_inquiry_company_tv, b2BBusinessListBean.getCompanyName());
        holder.setText(R.id.list_public_inquiry_bill_date_tv, b2BBusinessListBean.getBillDate());
        holder.setText(R.id.list_public_inquiry_product_name_tv, b2BBusinessListBean.getProductName());
        holder.setText(R.id.list_public_inquiry_product_model_tv, b2BBusinessListBean.getProductModel());
        holder.setText(R.id.list_public_inquiry_product_specification_tv, b2BBusinessListBean.getProductSpecification());
        holder.setText(R.id.list_public_inquiry_product_amount_tv, b2BBusinessListBean.getProductAmount());
        holder.setText(R.id.list_public_inquiry_product_brand_tv, b2BBusinessListBean.getProductBrand());
        holder.setText(R.id.list_public_inquiry_expiry_date_tv, b2BBusinessListBean.getExpiryDate());

        mSpanUtils = new SpanUtils();
        SpannableStringBuilder stringBuilder = mSpanUtils.append(mContext.getString(R.string.str_remain))
                .append(b2BBusinessListBean.getRemainTime() + "").setForegroundColor(Color.RED)
                .setBold().setFontSize(24, true)
                .append(mContext.getString(R.string.str_days)).create();
        if (stringBuilder != null) {
            holder.setText(R.id.list_public_inquiry_remaintime_tv, stringBuilder);
        }

        String billState = b2BBusinessListBean.getBillState();
        if (billState != null) {
            switch (billState) {
                case Constants.FLAG.STATE_PUBLIC_INQUIRY_TODO:
                    setPublicBtn(holder, true, false, false);
                    break;
                case Constants.FLAG.STATE_PUBLIC_INQUIRY_DONE:
                    setPublicBtn(holder, false, true, false);
                    break;
                case Constants.FLAG.STATE_PUBLIC_INQUIRY_INVALID:
                    setPublicBtn(holder, false, false, true);
                    break;
                default:
                    setPublicBtn(holder, false, false, false);
                    break;
            }
        } else {
            setPublicBtn(holder, false, false, false);
        }

    }

    private void setPublicBtn(BaseViewHolder holder, boolean b1, boolean b2, boolean b3) {
        holder.setVisible(R.id.list_public_inquiry_offer_quotation_btn, b1);
        holder.setVisible(R.id.list_public_inquiry_done_btn, b2);
        holder.setVisible(R.id.list_public_inquiry_invalid_btn, b3);
    }

    private void bindCustomerInquiry(BaseViewHolder holder, B2BBusinessListBean b2BBusinessListBean) {
        holder.setText(R.id.list_customer_inquiry_company_tv, b2BBusinessListBean.getCompanyName());
        holder.setText(R.id.list_customer_inquiry_bill_date_tv, b2BBusinessListBean.getBillDate());
        holder.setText(R.id.list_customer_inquiry_bill_num_tv, b2BBusinessListBean.getBillNum());
        holder.setText(R.id.list_customer_inquiry_material_num_tv, b2BBusinessListBean.getMaterialNum());
        holder.setText(R.id.list_customer_inquiry_material_name_tv, b2BBusinessListBean.getMaterialName());
        holder.setText(R.id.list_customer_inquiry_material_spec_tv, b2BBusinessListBean.getMaterialSpec());
        holder.setText(R.id.list_customer_inquiry_expiry_date_tv, b2BBusinessListBean.getExpiryDate());

        mSpanUtils = new SpanUtils();
        SpannableStringBuilder stringBuilder = mSpanUtils.append(mContext.getString(R.string.str_remain))
                .append(b2BBusinessListBean.getRemainTime() + "").setForegroundColor(Color.RED)
                .setBold().setFontSize(24, true)
                .append(mContext.getString(R.string.str_days)).create();
        if (stringBuilder != null) {
            holder.setText(R.id.list_customer_inquiry_remaintime_tv, stringBuilder);
        }

        String billState = b2BBusinessListBean.getBillState();
        if (billState != null) {
            switch (billState) {
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_TODO:
                    setCustomerBtn(holder, true, false, false, false, false, false, false);
                    break;
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_DONE:
                    setCustomerBtn(holder, false, true, false, false, false, false, false);
                    break;
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_AGREED:
                    setCustomerBtn(holder, false, false, true, false, false, false, false);
                    break;
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_END:
                    setCustomerBtn(holder, false, false, false, true, false, false, false);
                    break;
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_REFUSED:
                    setCustomerBtn(holder, false, false, false, false, true, false, false);
                    break;
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_INVALID:
                    setCustomerBtn(holder, false, false, false, false, false, true, false);
                    break;
                case Constants.FLAG.STATE_CUSTOMER_INQUIRY_ABANDONED:
                    setCustomerBtn(holder, false, false, false, false, false, false, true);
                    break;
                default:
                    setCustomerBtn(holder, false, false, false, false, false, false, false);
                    break;
            }
        } else {
            setCustomerBtn(holder, false, false, false, false, false, false, false);
        }
    }

    private void setCustomerBtn(BaseViewHolder holder, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7) {
        holder.setVisible(R.id.list_customer_inquiry_wait_quoted_btn, b1);
        holder.setVisible(R.id.list_customer_inquiry_quoted_price_btn, b2);
        holder.setVisible(R.id.list_customer_inquiry_quotation_adopted_btn, b3);
        holder.setVisible(R.id.list_customer_inquiry_expired_btn, b4);
        holder.setVisible(R.id.list_customer_inquiry_quotation_unadopted_btn, b5);
        holder.setVisible(R.id.list_customer_inquiry_invalid_btn, b6);
        holder.setVisible(R.id.list_customer_inquiry_abandoned_btn, b7);
    }

    private void bindPurchaseOrder(BaseViewHolder holder, B2BBusinessListBean b2BBusinessListBean) {
        holder.setText(R.id.list_purchase_order_company_tv, b2BBusinessListBean.getCompanyName());
        holder.setText(R.id.list_purchase_order_bill_date_tv, b2BBusinessListBean.getBillDate());
        holder.setText(R.id.list_purchase_order_bill_num_tv, b2BBusinessListBean.getBillNum());
        holder.setText(R.id.list_purchase_order_currency_tv, b2BBusinessListBean.getCurrency());
        holder.setText(R.id.list_purchase_order_money_tv, b2BBusinessListBean.getMoney());

        String billState = b2BBusinessListBean.getBillState();
        if (billState != null) {
            switch (billState) {
                case Constants.FLAG.STATE_PURCHASE_ORDER_DONE:
                    setPurchaseBtn(holder, false, true, false);
                    break;
                case Constants.FLAG.STATE_PURCHASE_ORDER_TODO:
                    setPurchaseBtn(holder, false, false, true);
                    break;
                case Constants.FLAG.STATE_PURCHASE_ORDER_END:
                    setPurchaseBtn(holder, true, false, false);
                    break;
                default:
                    setPurchaseBtn(holder, false, false, false);
                    break;
            }
        } else {
            setPurchaseBtn(holder, false, false, false);
        }

    }

    private void setPurchaseBtn(BaseViewHolder holder, boolean b1, boolean b2, boolean b3) {
        holder.setVisible(R.id.list_purchase_order_case_closed_btn, b1);
        holder.setVisible(R.id.list_purchase_order_have_replied_btn, b2);
        holder.setVisible(R.id.list_purchase_order_wait_reply_btn, b3);
    }

}
