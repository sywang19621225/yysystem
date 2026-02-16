package com.yysystem.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@TableName("scm_purchase_request")
public class ScmPurchaseRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestNo;
    private Long requesterId;
    private String requesterName;
    private Long deptId;
    private Long supplierId;
    private String purchaseCategory;
    @TableField("customer_id")
    private Long customerId;
    @TableField("customer_name")
    private String customerName;
    private Long contractId;
    private String contractNo;
    private String contractName;
    private LocalDate requestDate;
    private java.math.BigDecimal totalAmount;
    private String status;
    private String auditStatus;
    private String auditDetail;
    private String remark;
    private String contractAttachments;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime paymentTime;
    private String paymentVoucher;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime acceptanceTime;
    private String acceptanceRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("lock_status")
    private String lockStatus; // 锁单状态：0-未锁单，1-已锁单
    @TableField("purchase_status")
    private String purchaseStatus; // 采购状态：待采购、已采购、已到货、已入库
    private String supplierName; // 供应商名称
    @TableField(exist = false)
    private List<ScmPurchaseRequestItem> itemList;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }
    public String getPurchaseCategory() { return purchaseCategory; }
    public void setPurchaseCategory(String purchaseCategory) { this.purchaseCategory = purchaseCategory; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditDetail() { return auditDetail; }
    public void setAuditDetail(String auditDetail) { this.auditDetail = auditDetail; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getContractAttachments() { return contractAttachments; }
    public void setContractAttachments(String contractAttachments) { this.contractAttachments = contractAttachments; }
    public java.time.LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(java.time.LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public String getPaymentVoucher() { return paymentVoucher; }
    public void setPaymentVoucher(String paymentVoucher) { this.paymentVoucher = paymentVoucher; }
    public java.time.LocalDateTime getAcceptanceTime() { return acceptanceTime; }
    public void setAcceptanceTime(java.time.LocalDateTime acceptanceTime) { this.acceptanceTime = acceptanceTime; }
    public String getAcceptanceRemark() { return acceptanceRemark; }
    public void setAcceptanceRemark(String acceptanceRemark) { this.acceptanceRemark = acceptanceRemark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getLockStatus() { return lockStatus; }
    public void setLockStatus(String lockStatus) { this.lockStatus = lockStatus; }
    public String getPurchaseStatus() { return purchaseStatus; }
    public void setPurchaseStatus(String purchaseStatus) { this.purchaseStatus = purchaseStatus; }
    public List<ScmPurchaseRequestItem> getItemList() { return itemList; }
    public void setItemList(List<ScmPurchaseRequestItem> itemList) { this.itemList = itemList; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getPurchaseOrderNo() { return requestNo; }
    public void setPurchaseOrderNo(String purchaseOrderNo) { this.requestNo = purchaseOrderNo; }
}
