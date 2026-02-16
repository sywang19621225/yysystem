package com.yysystem.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("scm_purchase_contract_detail")
public class ScmPurchaseContractDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contractId;
    private Long requestItemId;
    private String productName;
    private String productCode;
    private String productSpec;
    private Integer quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal amount;
    private String productUnit;
    private String remark;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Long getRequestItemId() { return requestItemId; }
    public void setRequestItemId(Long requestItemId) { this.requestItemId = requestItemId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public java.math.BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

