package com.uas.appworks.model.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 公共询价单转报价后的询价单明细
 */
public class PublicInquiryItem implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 来源（买家ERP采购询价明细）的ID
     */
    private Long sourceId;

    /**
     * 序号
     */
    private Short number;

    /**
     * 买家采购员UU
     */
    private Long userUU;

    /**
     * 联系人姓名
     */
    private String userName;

    /**
     * 联系人电话
     */
    private String userTel;

    /**
     * 联系人编号
     */
    private String userCode;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 币种
     */
    private String currency;

    /**
     * 税率
     */
    private Float taxrate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 供应商UU
     */
    private Long vendUU;

    /**
     * 供应商联系人UU
     */
    private Long vendUserUU;

    /**
     * （买家预先提供的）有效期始
     */
    private Date fromDate;

    /**
     * （买家预先提供的）有效期止
     */
    private Date toDate;

    /**
     * （卖家报的）有效期始
     */
    private Date vendFromDate;

    /**
     * （卖家报的）有效期止
     */
    private Date vendToDate;

    /**
     * （卖家报的）最小订购量
     */
    private Double minOrderQty;

    /**
     * （卖家报的）最小包装量
     */
    private Double minPackQty;

    /**
     * （卖家报的）物料品牌
     */
    private String brand;

    /**
     * （卖家报的）供应商物料编号
     */
    private String vendorprodcode;

    /**
     * （卖家报的）交货周期（天数）
     */
    private Long leadtime;

    /**
     * 分段报价明细
     */
    private List<PublicInquiryReply> replies;

    /**
     * 状态 200 待回复 、201 已回复 、314 已作废
     */
    private Short status;

    /**
     * (针对卖家的)询价传输状态{待上传、已下载}
     */
    private Short sendStatus;

    /**
     * (针对买家的)报价信息传输状态{待上传、已下载}
     */
    private Short backStatus;

    /**
     * (针对卖家的)报价信息传输状态{待上传、已下载}
     */
    private Short replySendStatus;

    /**
     * 是否采纳 1 为已采纳 0为已拒绝  空 未处理
     */
    private Short agreed;


    /**
     * 拒绝采纳理由
     */
    private String refusereason;

    /**
     * (针对卖家的)是否采纳信息传输状态{待上传、已下载}
     */
    private Short decideStatus;

    /**
     * 报价方UAS 是否采纳信息传输状态{待上传、已下载}  202 待上传  203  已下载
     */
    private Short decideDownStatus;

    /**
     * (针对卖家的)作废信息传输状态{待上传、已下载}
     */
    private Short invalidStatus;

    /**
     * 是否买家已设置分段数
     */
    private Short custLap;

    /**
     * 保存erp传入数据的时间
     *
     * @return
     */
    private Date erpDate;

    /**
     * 录入时间(取主表日期字段)
     *
     * @return
     */
    private Date date;

    /**
     * 是否已过期 （目前来看此字段没有用到）
     */
    private Short overdue;

    /**
     * 报价是否还有效 （目前来看此字段没有用到）
     */
    private Short invalid;

    /**
     * 保存询价的应用
     */
    private String source;

    /**
     * 报价的应用
     */
    private String qutoApp;

    /**
     * 需求数量
     */
    private Double needquantity;

    /**
     * erp传输状态
     * <p>
     * <pre>erp发出数据传输请求时，赋给状态，完成后更新状态</pre>
     *
     * 1、 传输完成<br>
     * 0、 正在传输
     */
    private Short erpstatus;

    /**
     * 询价种类
     */
    private String kind;

    /*这下面是供应商报价时存的相关信息；
     * 因为存在非客户报价，而且公共服务里面没有企业信息，现存入相关字段，后续处理*/
    /**
     * 供应商名称
     */
    private String vendName;

    /**
     * 供应商营业执照
     */
    private String businessCode;

    /**
     * 报价时间
     */
    private Date offerTime;

    /**
     * 单价预算
     */
    private Double unitPrice;

    /**
     * 产品生产日期
     */
    private String produceDate;

    /**
     * 封装
     */
    private String encapsulation;

    /**
     * 报价截止日期
     */
    private Date endDate;

    /**
     * ************* 上传的物料信息，物料冗余字段 *************
     */
    /**
     * 物料编号
     */
    private String prodCode;

    /**
     * 名称
     */
    private String prodTitle;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 型号
     */
    private String cmpCode;

    /**
     * 品牌
     */
    private String inbrand;

    /**
     * ******end*******
     */

    /**
     * 剩余时间
     */
    private Long remainingTime;

    /**
     * 审批状态，公共询价单采用明细单独做审批状态
     */
    private Short checked;

    /**
     * 附件链接
     */
    private String attachUrl;

    /**
     * 附件名称
     */
    private String attachName;

    /**
     * 买家发出公共询价单时录入的币别
     */
    private String custCurrency;

    /******** 替代物料信息  */

    /**
     * 替代型号
     */
    private String replaceCmpCode;

    /**
     * 替代规格
     */
    private String replaceSpec;

    /**
     * 替代品牌
     */
    private String replaceBrand;

    /**
     * 是否替代物料报价  1 是替代料报价， 0为普通报价
     */
    private Short isReplace = 0;

    /**
     * end
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Short getNumber() {
        return number;
    }

    public void setNumber(Short number) {
        this.number = number;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProdCode() {
        return prodCode;
    }

    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Float getTaxrate() {
        return taxrate;
    }

    public void setTaxrate(Float taxrate) {
        this.taxrate = taxrate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getVendFromDate() {
        return vendFromDate;
    }

    public void setVendFromDate(Date vendFromDate) {
        this.vendFromDate = vendFromDate;
    }

    public Date getVendToDate() {
        return vendToDate;
    }

    public void setVendToDate(Date vendToDate) {
        this.vendToDate = vendToDate;
    }

    public Long getVendUU() {
        return vendUU;
    }

    public void setVendUU(Long vendUU) {
        this.vendUU = vendUU;
    }

    public List<PublicInquiryReply> getReplies() {
        return replies;
    }

    public void setReplies(List<PublicInquiryReply> replies) {
        this.replies = replies;
    }

    public Long getVendUserUU() {
        return vendUserUU;
    }

    public void setVendUserUU(Long vendUserUU) {
        this.vendUserUU = vendUserUU;
    }

    public Short getAgreed() {
        return agreed;
    }

    public void setAgreed(Short agreed) {
        this.agreed = agreed;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Short getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Short sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Double getMinOrderQty() {
        return minOrderQty;
    }

    public void setMinOrderQty(Double minOrderQty) {
        this.minOrderQty = minOrderQty;
    }

    public Double getMinPackQty() {
        return minPackQty;
    }

    public void setMinPackQty(Double minPackQty) {
        this.minPackQty = minPackQty;
    }

    public Short getBackStatus() {
        return backStatus;
    }

    public void setBackStatus(Short backStatus) {
        this.backStatus = backStatus;
    }

    public Short getReplySendStatus() {
        return replySendStatus;
    }

    public void setReplySendStatus(Short replySendStatus) {
        this.replySendStatus = replySendStatus;
    }

    public Short getDecideStatus() {
        return decideStatus;
    }

    public void setDecideStatus(Short decideStatus) {
        this.decideStatus = decideStatus;
    }

    public Short getDecideDownStatus() {
        return decideDownStatus;
    }

    public void setDecideDownStatus(Short decideDownStatus) {
        this.decideDownStatus = decideDownStatus;
    }

    public Short getCustLap() {
        return custLap;
    }

    public void setCustLap(Short custLap) {
        this.custLap = custLap;
    }

    public Long getUserUU() {
        return userUU;
    }

    public void setUserUU(Long userUU) {
        this.userUU = userUU;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserTel() {
        return userTel;
    }

    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVendorprodcode() {
        return vendorprodcode;
    }

    public void setVendorprodcode(String vendorprodcode) {
        this.vendorprodcode = vendorprodcode;
    }

    public Long getLeadtime() {
        return leadtime;
    }

    public void setLeadtime(Long leadtime) {
        this.leadtime = leadtime;
    }

    public Date getErpDate() {
        return erpDate;
    }

    public void setErpDate(Date erpDate) {
        this.erpDate = erpDate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getQutoApp() {
        return qutoApp;
    }

    public void setQutoApp(String qutoApp) {
        this.qutoApp = qutoApp;
    }

    public Short getInvalidStatus() {
        return invalidStatus;
    }

    public void setInvalidStatus(Short invalidStatus) {
        this.invalidStatus = invalidStatus;
    }

    public Double getNeedquantity() {
        return needquantity;
    }

    public void setNeedquantity(Double needquantity) {
        this.needquantity = needquantity;
    }

    public Short getOverdue() {
        return overdue;
    }

    public void setOverdue(Short overdue) {
        this.overdue = overdue;
    }

    public Short getInvalid() {
        return invalid;
    }

    public void setInvalid(Short invalid) {
        this.invalid = invalid;
    }

    public Short getErpstatus() {
        return erpstatus;
    }

    public void setErpstatus(Short erpstatus) {

        this.erpstatus = erpstatus;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getVendName() {
        return vendName;
    }

    public void setVendName(String vendName) {
        this.vendName = vendName;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public Date getOfferTime() {
        return offerTime;
    }

    public void setOfferTime(Date offerTime) {
        this.offerTime = offerTime;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProduceDate() {
        return produceDate;
    }

    public void setProduceDate(String produceDate) {
        this.produceDate = produceDate;
    }

    public String getEncapsulation() {
        return encapsulation;
    }

    public void setEncapsulation(String encapsulation) {
        this.encapsulation = encapsulation;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getProdTitle() {
        return prodTitle;
    }

    public void setProdTitle(String prodTitle) {
        this.prodTitle = prodTitle;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCmpCode() {
        return cmpCode;
    }

    public void setCmpCode(String cmpCode) {
        this.cmpCode = cmpCode;
    }

    public String getInbrand() {
        return inbrand;
    }

    public void setInbrand(String inbrand) {
        this.inbrand = inbrand;
    }

    public Long getRemainingTime() {
        if (null != endDate) {
            return endDate.getTime() - System.currentTimeMillis();
        }
        return null;
    }

    public Short getChecked() {
        return checked;
    }

    public void setChecked(Short checked) {
        this.checked = checked;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public void setAttachUrl(String attachUrl) {
        this.attachUrl = attachUrl;
    }

    public String getAttachName() {
        return attachName;
    }

    public void setAttachName(String attachName) {
        this.attachName = attachName;
    }

    public String getCustCurrency() {
        return custCurrency;
    }

    public void setCustCurrency(String custCurrency) {
        this.custCurrency = custCurrency;
    }

    public String getRefusereason() {
        return refusereason;
    }

    public void setRefusereason(String refusereason) {
        this.refusereason = refusereason;
    }

    public void setRemainingTime(Long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getReplaceCmpCode() {
        return replaceCmpCode;
    }

    public void setReplaceCmpCode(String replaceCmpCode) {
        this.replaceCmpCode = replaceCmpCode;
    }

    public String getReplaceSpec() {
        return replaceSpec;
    }

    public void setReplaceSpec(String replaceSpec) {
        this.replaceSpec = replaceSpec;
    }

    public String getReplaceBrand() {
        return replaceBrand;
    }

    public void setReplaceBrand(String replaceBrand) {
        this.replaceBrand = replaceBrand;
    }

    public Short getIsReplace() {
        return isReplace;
    }

    public void setIsReplace(Short isReplace) {
        this.isReplace = isReplace;
    }

    public PublicInquiryItem() {
    }

    public static class PublicInquiryReply implements Serializable {

        /**
         * id
         */
        private Long id;

        /**
         * （买家或卖家定义的）分段数量
         */
        private Double lapQty;

        /**
         * （卖家报的）单价
         */
        private Double price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Double getLapQty() {
            return lapQty;
        }

        public void setLapQty(Double lapQty) {
            this.lapQty = lapQty;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

    }

}
