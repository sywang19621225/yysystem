package com.yysystem.modules.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("crm_finance")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmFinance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private String auditStatus;

    private String paymentVoucher;

    private Long contractId;

    private String type; // IN-收, OUT-支

    private BigDecimal amount;

    private Long creatorId;

    private LocalDateTime createTime;

    private LocalDateTime arrivalTime;

    private String attachment;

    private String collectionDetail;

    private String auditDetail;

    private String remark;

    private String extendFields;

    private String category;

    @TableField("arrival_amount")
    private java.math.BigDecimal arrivalAmount;

    private String invoiceNo;

    private Long companyId;

    private String companyName;

    private String depositSourceType;

    private java.time.LocalDate refundDueDate;

    private java.time.LocalDate actualRefundDate;

    private LocalDateTime paymentTime;

    private String paymentMethod;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private String contractName;

    @TableField(exist = false)
    private String customerName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getPaymentVoucher() {
        return paymentVoucher;
    }

    public void setPaymentVoucher(String paymentVoucher) {
        this.paymentVoucher = paymentVoucher;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getCollectionDetail() {
        return collectionDetail;
    }

    public void setCollectionDetail(String collectionDetail) {
        this.collectionDetail = collectionDetail;
    }

    public String getAuditDetail() {
        return auditDetail;
    }

    public void setAuditDetail(String auditDetail) {
        this.auditDetail = auditDetail;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public java.math.BigDecimal getArrivalAmount() {
        return arrivalAmount;
    }

    public void setArrivalAmount(java.math.BigDecimal arrivalAmount) {
        this.arrivalAmount = arrivalAmount;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepositSourceType() {
        return depositSourceType;
    }

    public void setDepositSourceType(String depositSourceType) {
        this.depositSourceType = depositSourceType;
    }

    public java.time.LocalDate getRefundDueDate() {
        return refundDueDate;
    }

    public void setRefundDueDate(java.time.LocalDate refundDueDate) {
        this.refundDueDate = refundDueDate;
    }

    public java.time.LocalDate getActualRefundDate() {
        return actualRefundDate;
    }

    public void setActualRefundDate(java.time.LocalDate actualRefundDate) {
        this.actualRefundDate = actualRefundDate;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
