package com.yysystem.modules.quote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@TableName("crm_quote")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmQuote {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String quoteNo;

    private Long customerId;

    private Long linkmanId;

    private String phone;

    private String address;

    private String auditStatus;

    private String auditDetail;

    private String quoteStatus;

    private Long contractId;

    private BigDecimal quoteAmount;

    private Integer productTotal;

    private BigDecimal costTotalAmount;

    private BigDecimal channelTotalAmount;

    private BigDecimal terminalTotalAmount;

    private LocalDate deliveryDate;

    private LocalDate quoteDate;

    private Long salesmanId;

    private String remark;

    private String extendFields;

    private String attachment;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<CrmQuoteDetail> detailList;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String salesmanName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteNo() {
        return quoteNo;
    }

    public void setQuoteNo(String quoteNo) {
        this.quoteNo = quoteNo;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getLinkmanId() {
        return linkmanId;
    }

    public void setLinkmanId(Long linkmanId) {
        this.linkmanId = linkmanId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditDetail() {
        return auditDetail;
    }

    public void setAuditDetail(String auditDetail) {
        this.auditDetail = auditDetail;
    }

    public String getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public BigDecimal getQuoteAmount() {
        return quoteAmount;
    }

    public void setQuoteAmount(BigDecimal quoteAmount) {
        this.quoteAmount = quoteAmount;
    }

    public Integer getProductTotal() {
        return productTotal;
    }

    public void setProductTotal(Integer productTotal) {
        this.productTotal = productTotal;
    }

    public BigDecimal getCostTotalAmount() {
        return costTotalAmount;
    }

    public void setCostTotalAmount(BigDecimal costTotalAmount) {
        this.costTotalAmount = costTotalAmount;
    }

    public BigDecimal getChannelTotalAmount() {
        return channelTotalAmount;
    }

    public void setChannelTotalAmount(BigDecimal channelTotalAmount) {
        this.channelTotalAmount = channelTotalAmount;
    }

    public BigDecimal getTerminalTotalAmount() {
        return terminalTotalAmount;
    }

    public void setTerminalTotalAmount(BigDecimal terminalTotalAmount) {
        this.terminalTotalAmount = terminalTotalAmount;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(LocalDate quoteDate) {
        this.quoteDate = quoteDate;
    }

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getExtendFields() {
        return extendFields;
    }

    public void setExtendFields(String extendFields) {
        this.extendFields = extendFields;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<CrmQuoteDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<CrmQuoteDetail> detailList) {
        this.detailList = detailList;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }
}
