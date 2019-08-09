package com.uas.appworks.model.bean;

/**
 * @author RaoMeng
 * @describe 分段报价
 * @date 2018/1/16 20:44
 */

public class B2BQuotePriceBean {
    private long mId;
    private String mAmount;
    private String mPrice;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    @Override
    public String toString() {
        return "B2BQuotePriceBean{" +
                "mId=" + mId +
                ", mAmount='" + mAmount + '\'' +
                ", mPrice='" + mPrice + '\'' +
                '}';
    }
}
