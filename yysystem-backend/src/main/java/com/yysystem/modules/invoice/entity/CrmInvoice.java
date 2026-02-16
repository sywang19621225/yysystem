package com.yysystem.modules.invoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName("crm_invoice")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmInvoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private String creditCode;

    private String auditStatus;

    private Long contractId;

    private BigDecimal invoiceAmount;

    private String applyRemark;

    private String invoiceStatus;

    private String invoiceAttachment;

    private Long invoicerId;

    private LocalDateTime invoiceTime;

    private String invoiceNo;

    private String invoiceUnit;

    private Long applicantId;

    private LocalDateTime applyTime;

    private String invoiceGiveDesc;

    private LocalDateTime giveTime;

    private LocalDateTime arrivalTime;

    private String extendFields;

    @TableField(exist = false)
    private List<CrmInvoiceDetail> detailList;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String contractNo;

    @TableField(exist = false)
    private String invoicerName;

    @TableField(exist = false)
    private String applicantName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    public String getApplyRemark() { return applyRemark; }
    public void setApplyRemark(String applyRemark) { this.applyRemark = applyRemark; }
    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public String getInvoiceAttachment() { return invoiceAttachment; }
    public void setInvoiceAttachment(String invoiceAttachment) { this.invoiceAttachment = invoiceAttachment; }
    public Long getInvoicerId() { return invoicerId; }
    public void setInvoicerId(Long invoicerId) { this.invoicerId = invoicerId; }
    public LocalDateTime getInvoiceTime() { return invoiceTime; }
    public void setInvoiceTime(LocalDateTime invoiceTime) { this.invoiceTime = invoiceTime; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceUnit() { return invoiceUnit; }
    public void setInvoiceUnit(String invoiceUnit) { this.invoiceUnit = invoiceUnit; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public String getInvoiceGiveDesc() { return invoiceGiveDesc; }
    public void setInvoiceGiveDesc(String invoiceGiveDesc) { this.invoiceGiveDesc = invoiceGiveDesc; }
    public LocalDateTime getGiveTime() { return giveTime; }
    public void setGiveTime(LocalDateTime giveTime) { this.giveTime = giveTime; }
    public String getExtendFields() { return extendFields; }
    public void setExtendFields(String extendFields) { this.extendFields = extendFields; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public List<CrmInvoiceDetail> getDetailList() { return detailList; }
    public void setDetailList(List<CrmInvoiceDetail> detailList) { this.detailList = detailList; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getInvoicerName() { return invoicerName; }
    public void setInvoicerName(String invoicerName) { this.invoicerName = invoicerName; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
}
