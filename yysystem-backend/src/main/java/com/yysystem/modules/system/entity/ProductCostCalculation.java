package com.yysystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("sys_product_cost_calculation")
public class ProductCostCalculation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String productCode;

    private String productName;

    private BigDecimal hardwareCost;

    private BigDecimal installCost;

    private BigDecimal laborCost;

    private BigDecimal testCost;

    private BigDecimal lossCost;

    private BigDecimal certCost;

    private BigDecimal packageCost;

    private BigDecimal transportCost;

    private BigDecimal profitMargin;

    private BigDecimal profitAmount;

    private BigDecimal totalCost;

    private BigDecimal suggestedPrice;

    private BigDecimal retailPrice;

    private BigDecimal channelPrice;

    private Integer status;

    private String remark;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getHardwareCost() {
        return hardwareCost;
    }

    public void setHardwareCost(BigDecimal hardwareCost) {
        this.hardwareCost = hardwareCost;
    }

    public BigDecimal getInstallCost() {
        return installCost;
    }

    public void setInstallCost(BigDecimal installCost) {
        this.installCost = installCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public BigDecimal getTestCost() {
        return testCost;
    }

    public void setTestCost(BigDecimal testCost) {
        this.testCost = testCost;
    }

    public BigDecimal getLossCost() {
        return lossCost;
    }

    public void setLossCost(BigDecimal lossCost) {
        this.lossCost = lossCost;
    }

    public BigDecimal getCertCost() {
        return certCost;
    }

    public void setCertCost(BigDecimal certCost) {
        this.certCost = certCost;
    }

    public BigDecimal getPackageCost() {
        return packageCost;
    }

    public void setPackageCost(BigDecimal packageCost) {
        this.packageCost = packageCost;
    }

    public BigDecimal getTransportCost() {
        return transportCost;
    }

    public void setTransportCost(BigDecimal transportCost) {
        this.transportCost = transportCost;
    }

    public BigDecimal getProfitMargin() {
        return profitMargin;
    }

    public void setProfitMargin(BigDecimal profitMargin) {
        this.profitMargin = profitMargin;
    }

    public BigDecimal getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(BigDecimal profitAmount) {
        this.profitAmount = profitAmount;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getSuggestedPrice() {
        return suggestedPrice;
    }

    public void setSuggestedPrice(BigDecimal suggestedPrice) {
        this.suggestedPrice = suggestedPrice;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    public BigDecimal getChannelPrice() {
        return channelPrice;
    }

    public void setChannelPrice(BigDecimal channelPrice) {
        this.channelPrice = channelPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
