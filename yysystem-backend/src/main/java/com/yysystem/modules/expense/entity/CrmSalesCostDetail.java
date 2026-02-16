package com.yysystem.modules.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("crm_sales_cost_detail")
public class CrmSalesCostDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long salesCostId;
    private Long customerId;
    private String category;
    private String content;
    private BigDecimal amount;
    private String invoiceType;
    private String invoiceAttachment;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSalesCostId() { return salesCostId; }
    public void setSalesCostId(Long salesCostId) { this.salesCostId = salesCostId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public String getInvoiceAttachment() { return invoiceAttachment; }
    public void setInvoiceAttachment(String invoiceAttachment) { this.invoiceAttachment = invoiceAttachment; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
