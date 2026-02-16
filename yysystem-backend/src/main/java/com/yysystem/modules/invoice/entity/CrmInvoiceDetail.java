package com.yysystem.modules.invoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@TableName("crm_invoice_detail")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmInvoiceDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long invoiceId;

    private String productName;

    private String productSpec;

    private BigDecimal salesPrice;

    private Integer salesQuantity;

    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public BigDecimal getSalesPrice() { return salesPrice; }
    public void setSalesPrice(BigDecimal salesPrice) { this.salesPrice = salesPrice; }
    public Integer getSalesQuantity() { return salesQuantity; }
    public void setSalesQuantity(Integer salesQuantity) { this.salesQuantity = salesQuantity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
