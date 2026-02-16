package com.yysystem.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;

@TableName("scm_purchase_contract")
public class ScmPurchaseContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contractNo;
    private Long supplierId;
    private Long requestId;
    private String contractName;
    private java.math.BigDecimal contractAmount;
    private String auditStatus;
    private String auditDetail;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private List<ScmPurchaseContractDetail> detailList;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }
    public java.math.BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(java.math.BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditDetail() { return auditDetail; }
    public void setAuditDetail(String auditDetail) { this.auditDetail = auditDetail; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<ScmPurchaseContractDetail> getDetailList() { return detailList; }
    public void setDetailList(List<ScmPurchaseContractDetail> detailList) { this.detailList = detailList; }
}

