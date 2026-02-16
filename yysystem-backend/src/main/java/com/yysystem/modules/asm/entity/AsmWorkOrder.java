package com.yysystem.modules.asm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("asm_work_order")
public class AsmWorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String workOrderNo;
    
    private Long customerId;
    
    private String contactName;
    
    private String contactPhone;
    
    private String address;
    
    // 故障内容
    private String problemDesc;
    
    private String productName;
    
    private String productSpec;
    
    private Integer quantity;
    
    private LocalDateTime reportDate;
    
    private Long repairerId;
    
    private LocalDateTime repairTime;
    
    private String repairRecord;
    
    // 图片附件
    private String problemImages;
    
    private String remark;
    
    private BigDecimal cost;
    
    private Integer isEnabled; // 1:启用 0:禁用
    
    private Long creatorId;
    
    private String status; // 业务状态: PENDING, PROCESSING, COMPLETED, CANCELLED
    
    private String priority; // HIGH, MEDIUM, LOW
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String customerName;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String problemDesc) { this.problemDesc = problemDesc; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    
    public Long getRepairerId() { return repairerId; }
    public void setRepairerId(Long repairerId) { this.repairerId = repairerId; }
    
    public LocalDateTime getRepairTime() { return repairTime; }
    public void setRepairTime(LocalDateTime repairTime) { this.repairTime = repairTime; }
    
    public String getRepairRecord() { return repairRecord; }
    public void setRepairRecord(String repairRecord) { this.repairRecord = repairRecord; }
    
    public String getProblemImages() { return problemImages; }
    public void setProblemImages(String problemImages) { this.problemImages = problemImages; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    public Integer getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Integer isEnabled) { this.isEnabled = isEnabled; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
