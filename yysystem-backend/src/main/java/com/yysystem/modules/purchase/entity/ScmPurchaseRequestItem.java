package com.yysystem.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("scm_purchase_request_item")
public class ScmPurchaseRequestItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long requestId;
    private Long productId;
    private String productName;
    private String productCode;
    private Integer purchaseQuantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal purchasePrice;
    private LocalDate shipDate;
    private String productSpec;
    private String productUnit;
    private LocalDate expectedDate;
    private Integer arrivalQuantity; // 到货数量
    private Integer acceptQuantity; // 验收数量
    private String remark;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Integer getPurchaseQuantity() { return purchaseQuantity; }
    public void setPurchaseQuantity(Integer purchaseQuantity) { this.purchaseQuantity = purchaseQuantity; }
    public java.math.BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public java.math.BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(java.math.BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public LocalDate getShipDate() { return shipDate; }
    public void setShipDate(LocalDate shipDate) { this.shipDate = shipDate; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
    public Integer getArrivalQuantity() { return arrivalQuantity; }
    public void setArrivalQuantity(Integer arrivalQuantity) { this.arrivalQuantity = arrivalQuantity; }
    public Integer getAcceptQuantity() { return acceptQuantity; }
    public void setAcceptQuantity(Integer acceptQuantity) { this.acceptQuantity = acceptQuantity; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
