package com.yysystem.modules.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("crm_sales_cost")
public class CrmSalesCost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long contractId;
    private LocalDateTime expenditureDate;
    private String expenditureType;
    private String remark;
    private Long creatorId;
    private Long applicantId;
    private Long applicantDeptId;
    private LocalDateTime applyTime;
    private String reimburseCode;
    private String reimburseType;
    private String reimburseUnit;
    private BigDecimal totalAmount;
    private String applyRemark;
    private LocalDateTime reimburseTime;
    private String reimburseMethod;
    private Long financeUserId;
    private LocalDateTime receiptConfirmTime;
    private String receiptFeedback;
    private String auditStatus;
    private LocalDateTime auditTime;
    private String settlementStatus;
    private BigDecimal invoiceAmount;
    private String invoiceStatus;
    private Long invoicerId;
    private LocalDateTime invoiceTime;
    private String invoiceGiveDesc;
    private LocalDateTime giveTime;
    private String invoiceAttachment;
    private String extendFields;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public LocalDateTime getExpenditureDate() { return expenditureDate; }
    public void setExpenditureDate(LocalDateTime expenditureDate) { this.expenditureDate = expenditureDate; }
    public String getExpenditureType() { return expenditureType; }
    public void setExpenditureType(String expenditureType) { this.expenditureType = expenditureType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public Long getApplicantDeptId() { return applicantDeptId; }
    public void setApplicantDeptId(Long applicantDeptId) { this.applicantDeptId = applicantDeptId; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public String getReimburseCode() { return reimburseCode; }
    public void setReimburseCode(String reimburseCode) { this.reimburseCode = reimburseCode; }
    public String getReimburseType() { return reimburseType; }
    public void setReimburseType(String reimburseType) { this.reimburseType = reimburseType; }
    public String getReimburseUnit() { return reimburseUnit; }
    public void setReimburseUnit(String reimburseUnit) { this.reimburseUnit = reimburseUnit; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getApplyRemark() { return applyRemark; }
    public void setApplyRemark(String applyRemark) { this.applyRemark = applyRemark; }
    public LocalDateTime getReimburseTime() { return reimburseTime; }
    public void setReimburseTime(LocalDateTime reimburseTime) { this.reimburseTime = reimburseTime; }
    public String getReimburseMethod() { return reimburseMethod; }
    public void setReimburseMethod(String reimburseMethod) { this.reimburseMethod = reimburseMethod; }
    public Long getFinanceUserId() { return financeUserId; }
    public void setFinanceUserId(Long financeUserId) { this.financeUserId = financeUserId; }
    public LocalDateTime getReceiptConfirmTime() { return receiptConfirmTime; }
    public void setReceiptConfirmTime(LocalDateTime receiptConfirmTime) { this.receiptConfirmTime = receiptConfirmTime; }
    public String getReceiptFeedback() { return receiptFeedback; }
    public void setReceiptFeedback(String receiptFeedback) { this.receiptFeedback = receiptFeedback; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
    public String getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(String settlementStatus) { this.settlementStatus = settlementStatus; }
    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public Long getInvoicerId() { return invoicerId; }
    public void setInvoicerId(Long invoicerId) { this.invoicerId = invoicerId; }
    public LocalDateTime getInvoiceTime() { return invoiceTime; }
    public void setInvoiceTime(LocalDateTime invoiceTime) { this.invoiceTime = invoiceTime; }
    public String getInvoiceGiveDesc() { return invoiceGiveDesc; }
    public void setInvoiceGiveDesc(String invoiceGiveDesc) { this.invoiceGiveDesc = invoiceGiveDesc; }
    public LocalDateTime getGiveTime() { return giveTime; }
    public void setGiveTime(LocalDateTime giveTime) { this.giveTime = giveTime; }
    public String getInvoiceAttachment() { return invoiceAttachment; }
    public void setInvoiceAttachment(String invoiceAttachment) { this.invoiceAttachment = invoiceAttachment; }
    public String getExtendFields() { return extendFields; }
    public void setExtendFields(String extendFields) { this.extendFields = extendFields; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
