package com.yysystem.modules.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("pdm_product")
public class PdmProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productSource;

    private Long warehouseId;

    private String productName;

    private String productNameEn;

    private String productSeries;

    private String productCode;

    private String productSpec;

    private String brand;

    private String productUnit;

    private String productImage;

    private String productCategory;

    private BigDecimal costPrice;

    private Integer stockQuantity;

    private Integer availableStock;

    private Integer totalStock;

    private Long supplierId;

    private String attachment;

    private String remark;

    private Integer status;

    private Integer stockUpperWarning;

    private Integer stockLowerWarning;

    private String extendFields;

    private BigDecimal channelPrice;

    private BigDecimal terminalPrice;

    private String certificates;

    private String softwareAttachment;

    private String caseImages;

    private String createBy;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String warehouseName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductSource() {
        return productSource;
    }

    public void setProductSource(String productSource) {
        this.productSource = productSource;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductNameEn() {
        return productNameEn;
    }

    public void setProductNameEn(String productNameEn) {
        this.productNameEn = productNameEn;
    }

    public String getProductSeries() {
        return productSeries;
    }

    public void setProductSeries(String productSeries) {
        this.productSeries = productSeries;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductSpec() {
        return productSpec;
    }

    public void setProductSpec(String productSpec) {
        this.productSpec = productSpec;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStockUpperWarning() {
        return stockUpperWarning;
    }

    public void setStockUpperWarning(Integer stockUpperWarning) {
        this.stockUpperWarning = stockUpperWarning;
    }

    public Integer getStockLowerWarning() {
        return stockLowerWarning;
    }

    public void setStockLowerWarning(Integer stockLowerWarning) {
        this.stockLowerWarning = stockLowerWarning;
    }

    public String getExtendFields() {
        return extendFields;
    }

    public void setExtendFields(String extendFields) {
        this.extendFields = extendFields;
    }

    public BigDecimal getChannelPrice() {
        return channelPrice;
    }

    public void setChannelPrice(BigDecimal channelPrice) {
        this.channelPrice = channelPrice;
    }

    public BigDecimal getTerminalPrice() {
        return terminalPrice;
    }

    public void setTerminalPrice(BigDecimal terminalPrice) {
        this.terminalPrice = terminalPrice;
    }

    public String getCertificates() {
        return certificates;
    }

    public void setCertificates(String certificates) {
        this.certificates = certificates;
    }

    public String getSoftwareAttachment() {
        return softwareAttachment;
    }

    public void setSoftwareAttachment(String softwareAttachment) {
        this.softwareAttachment = softwareAttachment;
    }

    public String getCaseImages() {
        return caseImages;
    }

    public void setCaseImages(String caseImages) {
        this.caseImages = caseImages;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }
}
