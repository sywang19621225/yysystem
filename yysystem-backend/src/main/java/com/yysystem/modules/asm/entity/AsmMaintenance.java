package com.yysystem.modules.asm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("asm_maintenance")
public class AsmMaintenance {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String maintenanceNo;
    
    private String maintenanceName;
    
    private Long customerId;
    
    private String contactName;
    
    private String contactPhone;
    
    private String address;

    private LocalDateTime warrantyExpirationTime;
    
    private LocalDateTime scheduledTime;
    
    private Long maintainerId;
    
    private String deviceName;
    
    private Integer quantity;
    
    private String content;
    
    private LocalDateTime executionDate;
    
    private String executionRecord;
    
    private String images;
    
    private String remark;
    
    private BigDecimal cost;
    
    private Integer status; // 1:启用 0:禁用
    
    private Long creatorId;
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String customerName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMaintenanceNo() { return maintenanceNo; }
    public void setMaintenanceNo(String maintenanceNo) { this.maintenanceNo = maintenanceNo; }
    
    public String getMaintenanceName() { return maintenanceName; }
    public void setMaintenanceName(String maintenanceName) { this.maintenanceName = maintenanceName; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getWarrantyExpirationTime() { return warrantyExpirationTime; }
    public void setWarrantyExpirationTime(LocalDateTime warrantyExpirationTime) { this.warrantyExpirationTime = warrantyExpirationTime; }
    
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Long getMaintainerId() { return maintainerId; }
    public void setMaintainerId(Long maintainerId) { this.maintainerId = maintainerId; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDateTime executionDate) { this.executionDate = executionDate; }
    
    public String getExecutionRecord() { return executionRecord; }
    public void setExecutionRecord(String executionRecord) { this.executionRecord = executionRecord; }
    
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
