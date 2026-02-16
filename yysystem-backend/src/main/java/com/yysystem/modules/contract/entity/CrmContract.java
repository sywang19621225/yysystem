package com.yysystem.modules.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@TableName("crm_contract")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmContract {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String contractNo;

    private Long customerId;

    @TableField("customer_name")
    private String customerName;

    private Long linkmanId;

    @TableField("linkman_name")
    private String linkmanName;

    private Long projectId;

    private String phone;

    private String address;

    private String receiveAddress;

    private String receiver;

    private String receiverPhone;

    private String auditStatus;

    private String auditDetail;

    private LocalDateTime auditTime;

    private String customerCode;

    private Long salesmanId;

    private String endCustomerName;

    private BigDecimal arrears;

    private BigDecimal contractAmount;

    private BigDecimal advancePayment;

    private BigDecimal totalCollection;

    private BigDecimal totalExpenditure;

    private BigDecimal invoicedAmount;

    private LocalDate deliveryDate;

    private LocalDate outStockDate;
    private LocalDate signingDate;
    private Integer warrantyPeriodMonths;

    private LocalDate quoteDate;

    private String remark;

    private String extendFields;

    private LocalDateTime createTime;

    @TableField("contract_name")
    private String contractName;

    @TableField("attachment")
    private String attachment;

    @TableField(exist = false)
    private Boolean hasRejectedFinance;

    @TableField(exist = false)
    private Boolean hasRejectedIncome;

    @TableField(exist = false)
    private Boolean hasRejectedExpense;

    @TableField(exist = false)
    private List<CrmContractDetail> detailList;

    @TableField(exist = false)
    private Integer productTotal;

    @TableField(exist = false)
    private BigDecimal costTotalAmount;

    @TableField("intermediary_fee")
    private BigDecimal intermediaryFee;

    @TableField("intermediary_customer_id")
    private Long intermediaryCustomerId;

    @TableField("intermediary_customer_name")
    private String intermediaryCustomerName;

    @TableField("intermediary_fee_status")
    private String intermediaryFeeStatus;

    @TableField("intermediary_pay_time")
    private LocalDateTime intermediaryPayTime;

    @TableField("intermediary_pay_method")
    private String intermediaryPayMethod;

    @TableField("intermediary_pay_remark")
    private String intermediaryPayRemark;

    @TableField(exist = false)
    private BigDecimal depositAmount;

    @TableField(exist = false)
    private String outboundStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLinkmanName() {
        return linkmanName;
    }

    public void setLinkmanName(String linkmanName) {
        this.linkmanName = linkmanName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public String getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
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

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public String getEndCustomerName() {
        return endCustomerName;
    }

    public void setEndCustomerName(String endCustomerName) {
        this.endCustomerName = endCustomerName;
    }

    public BigDecimal getArrears() {
        return arrears;
    }

    public void setArrears(BigDecimal arrears) {
        this.arrears = arrears;
    }

    public BigDecimal getContractAmount() {
        return contractAmount;
    }

    public void setContractAmount(BigDecimal contractAmount) {
        this.contractAmount = contractAmount;
    }

    public BigDecimal getAdvancePayment() {
        return advancePayment;
    }

    public void setAdvancePayment(BigDecimal advancePayment) {
        this.advancePayment = advancePayment;
    }

    public BigDecimal getTotalCollection() {
        return totalCollection;
    }

    public void setTotalCollection(BigDecimal totalCollection) {
        this.totalCollection = totalCollection;
    }

    public BigDecimal getTotalExpenditure() {
        return totalExpenditure;
    }

    public void setTotalExpenditure(BigDecimal totalExpenditure) {
        this.totalExpenditure = totalExpenditure;
    }

    public BigDecimal getInvoicedAmount() {
        return invoicedAmount;
    }

    public void setInvoicedAmount(BigDecimal invoicedAmount) {
        this.invoicedAmount = invoicedAmount;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getOutStockDate() {
        return outStockDate;
    }

    public void setOutStockDate(LocalDate outStockDate) {
        this.outStockDate = outStockDate;
    }
    public LocalDate getSigningDate() { return signingDate; }
    public void setSigningDate(LocalDate signingDate) { this.signingDate = signingDate; }
    public Integer getWarrantyPeriodMonths() { return warrantyPeriodMonths; }
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) { this.warrantyPeriodMonths = warrantyPeriodMonths; }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(LocalDate quoteDate) {
        this.quoteDate = quoteDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Boolean getHasRejectedFinance() {
        return hasRejectedFinance;
    }

    public void setHasRejectedFinance(Boolean hasRejectedFinance) {
        this.hasRejectedFinance = hasRejectedFinance;
    }

    public Boolean getHasRejectedIncome() {
        return hasRejectedIncome;
    }

    public void setHasRejectedIncome(Boolean hasRejectedIncome) {
        this.hasRejectedIncome = hasRejectedIncome;
    }

    public Boolean getHasRejectedExpense() {
        return hasRejectedExpense;
    }

    public void setHasRejectedExpense(Boolean hasRejectedExpense) {
        this.hasRejectedExpense = hasRejectedExpense;
    }

    public List<CrmContractDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<CrmContractDetail> detailList) {
        this.detailList = detailList;
    }

    public Integer getProductTotal() {
        return productTotal;
    }

    public void setProductTotal(Integer productTotal) {
        this.productTotal = productTotal;
    }

    public String getOutboundStatus() {
        return outboundStatus;
    }

    public void setOutboundStatus(String outboundStatus) {
        this.outboundStatus = outboundStatus;
    }

    public BigDecimal getCostTotalAmount() {
        return costTotalAmount;
    }

    public void setCostTotalAmount(BigDecimal costTotalAmount) {
        this.costTotalAmount = costTotalAmount;
    }

    public BigDecimal getIntermediaryFee() {
        return intermediaryFee;
    }

    public void setIntermediaryFee(BigDecimal intermediaryFee) {
        this.intermediaryFee = intermediaryFee;
    }

    public Long getIntermediaryCustomerId() {
        return intermediaryCustomerId;
    }

    public void setIntermediaryCustomerId(Long intermediaryCustomerId) {
        this.intermediaryCustomerId = intermediaryCustomerId;
    }

    public String getIntermediaryCustomerName() {
        return intermediaryCustomerName;
    }

    public void setIntermediaryCustomerName(String intermediaryCustomerName) {
        this.intermediaryCustomerName = intermediaryCustomerName;
    }

    public String getIntermediaryFeeStatus() {
        return intermediaryFeeStatus;
    }

    public void setIntermediaryFeeStatus(String intermediaryFeeStatus) {
        this.intermediaryFeeStatus = intermediaryFeeStatus;
    }

    public LocalDateTime getIntermediaryPayTime() {
        return intermediaryPayTime;
    }

    public void setIntermediaryPayTime(LocalDateTime intermediaryPayTime) {
        this.intermediaryPayTime = intermediaryPayTime;
    }

    public String getIntermediaryPayMethod() {
        return intermediaryPayMethod;
    }

    public void setIntermediaryPayMethod(String intermediaryPayMethod) {
        this.intermediaryPayMethod = intermediaryPayMethod;
    }

    public String getIntermediaryPayRemark() {
        return intermediaryPayRemark;
    }

    public void setIntermediaryPayRemark(String intermediaryPayRemark) {
        this.intermediaryPayRemark = intermediaryPayRemark;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
}
