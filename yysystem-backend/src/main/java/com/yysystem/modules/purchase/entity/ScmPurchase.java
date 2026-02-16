package com.yysystem.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// @TableName("scm_purchase") // 表已停用，不再使用
public class ScmPurchase {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String purchaseNo;

    private Long supplierId;

    private BigDecimal totalAmount;

    private Integer purchaseQuantity;

    private String inboundProgress;

    private String paymentProgress;

    private String invoiceProgress;

    private java.math.BigDecimal paymentAmount;

    private String paymentMethod;

    private LocalDate deliveryDate;

    private String auditStatus;

    private String auditDetail;

    private String remark;

    private Integer purchaseReturn;

    private Integer purchaseInbound;

    private Long creatorId;

    private Long purchaseUserId;

    private LocalDateTime createTime;

    private String extendFields;

    @TableField(exist = false)
    private List<ScmPurchaseDetail> detailList;

    @TableField(exist = false)
    private String supplierName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurchaseNo() {
        return purchaseNo;
    }

    public void setPurchaseNo(String purchaseNo) {
        this.purchaseNo = purchaseNo;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(Integer purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public String getInboundProgress() {
        return inboundProgress;
    }

    public void setInboundProgress(String inboundProgress) {
        this.inboundProgress = inboundProgress;
    }

    public String getPaymentProgress() {
        return paymentProgress;
    }

    public void setPaymentProgress(String paymentProgress) {
        this.paymentProgress = paymentProgress;
    }

    public String getInvoiceProgress() {
        return invoiceProgress;
    }

    public void setInvoiceProgress(String invoiceProgress) {
        this.invoiceProgress = invoiceProgress;
    }

    public java.math.BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(java.math.BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getPurchaseReturn() {
        return purchaseReturn;
    }

    public void setPurchaseReturn(Integer purchaseReturn) {
        this.purchaseReturn = purchaseReturn;
    }

    public Integer getPurchaseInbound() {
        return purchaseInbound;
    }

    public void setPurchaseInbound(Integer purchaseInbound) {
        this.purchaseInbound = purchaseInbound;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getPurchaseUserId() {
        return purchaseUserId;
    }

    public void setPurchaseUserId(Long purchaseUserId) {
        this.purchaseUserId = purchaseUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getExtendFields() {
        return extendFields;
    }

    public void setExtendFields(String extendFields) {
        this.extendFields = extendFields;
    }

    public List<ScmPurchaseDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<ScmPurchaseDetail> detailList) {
        this.detailList = detailList;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
