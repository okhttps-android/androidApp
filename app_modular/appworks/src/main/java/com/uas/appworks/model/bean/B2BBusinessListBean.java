package com.uas.appworks.model.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/1/14 15:35
 */

public class B2BBusinessListBean implements MultiItemEntity {
    public static final int PURCHASE_ORDER_LIST = 2;//客户采购订单
    public static final int CUSTOMER_INQUIRY_LIST = 3;//客户询价单
    public static final int PUBLIC_INQUIRY_LIST = 4;//公共询价
    public static final int COMPANY_BUSINESS_LIST = 5;//公司商机

    private int mItemType = -1;
    private String mId;//单据id
    private String mCompanyName;//公司名称
    private String mBillDate;//单据日期
    private String mBillNum;//单据编号
    private String mMaterialNum;//物料编号
    private String mMaterialName;//物料名称
    private String mMaterialSpec;//物料规格
    private String mCurrency;//币种
    private String mMoney;//金额
    private String mBillState;//单据状态
    private String mExpiryDate;//截止日期
    private String mProductName;//产品名称
    private String mProductModel;//产品型号
    private String mProductSpecification;//产品规格
    private String mProductAmount;//产品数量
    private String mProductBrand;//产品品牌
    private int mRemainTime;//剩余天数
    private String mJsonData;

    @Override
    public int getItemType() {
        return mItemType;
    }

    public void setItemType(int itemType) {
        mItemType = itemType;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String companyName) {
        mCompanyName = companyName;
    }

    public String getBillDate() {
        return mBillDate;
    }

    public void setBillDate(String billDate) {
        mBillDate = billDate;
    }

    public String getBillNum() {
        return mBillNum;
    }

    public void setBillNum(String billNum) {
        mBillNum = billNum;
    }

    public String getMaterialNum() {
        return mMaterialNum;
    }

    public void setMaterialNum(String materialNum) {
        mMaterialNum = materialNum;
    }

    public String getMaterialName() {
        return mMaterialName;
    }

    public void setMaterialName(String materialName) {
        mMaterialName = materialName;
    }

    public String getMaterialSpec() {
        return mMaterialSpec;
    }

    public void setMaterialSpec(String materialSpec) {
        mMaterialSpec = materialSpec;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public String getMoney() {
        return mMoney;
    }

    public void setMoney(String money) {
        mMoney = money;
    }

    public String getBillState() {
        return mBillState;
    }

    public void setBillState(String billState) {
        mBillState = billState;
    }

    public String getExpiryDate() {
        return mExpiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        mExpiryDate = expiryDate;
    }

    public String getProductName() {
        return mProductName;
    }

    public void setProductName(String productName) {
        mProductName = productName;
    }

    public String getProductModel() {
        return mProductModel;
    }

    public void setProductModel(String productModel) {
        mProductModel = productModel;
    }

    public String getProductSpecification() {
        return mProductSpecification;
    }

    public void setProductSpecification(String productSpecification) {
        mProductSpecification = productSpecification;
    }

    public String getProductAmount() {
        return mProductAmount;
    }

    public void setProductAmount(String productAmount) {
        mProductAmount = productAmount;
    }

    public String getProductBrand() {
        return mProductBrand;
    }

    public void setProductBrand(String productBrand) {
        mProductBrand = productBrand;
    }

    public int getRemainTime() {
        return mRemainTime;
    }

    public void setRemainTime(int remainTime) {
        mRemainTime = remainTime;
    }

    public String getJsonData() {
        return mJsonData;
    }

    public void setJsonData(String jsonData) {
        mJsonData = jsonData;
    }
}
