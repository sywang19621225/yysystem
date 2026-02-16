package com.yysystem.modules.outbound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName("scm_outbound")
public class ScmOutbound {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String outStockNo;

    private Long customerId;

    private Long linkmanId;

    private String customerAddress;

    private String transportMethod;

    private BigDecimal transportAmount;
    /** 运输单位（公司名称） */
    private String transportCompany;
    /** 运输联系人姓名 */
    private String transportContact;
    /** 运输联系人电话 */
    private String transportContactPhone;
    /** 运输金额单位（例如：人民币） */
    private String transportUnit;

    private Long contractId;
    /** 合同名称（非数据库字段） */
    @TableField(exist = false)
    private String contractName;

    private Long salesmanId;

    private String outStockType;
    /** 出库分类 */
    private String outboundCategory;

    private BigDecimal costPrice;

    private BigDecimal totalCost;

    private BigDecimal salesPrice;

    private BigDecimal totalSales;

    private LocalDateTime outStockDate;
    private LocalDateTime scheduledShipTime;
    /** 预计到货时间 */
    private LocalDateTime expectedArrivalTime;
    private LocalDateTime actualShipTime;
    private LocalDateTime actualArrivalTime;
    private String arrivalRemark;
    private String arrivalImages;
    /** 质保到期时间 */
    private LocalDateTime warrantyExpireTime;

    /** 退货签收时间（采购退货使用） */
    private LocalDateTime returnSignTime;

    /** 退货回告时间（采购退货使用） */
    private LocalDateTime returnNotifyTime;

    /** 退货附件（采购退货使用，JSON格式存储图片/PDF路径数组） */
    private String returnAttachments;

    private String auditStatus;

    private String auditDetail;

    private String remark;

    private Long delivererId;

    private Long creatorId;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<ScmOutboundDetail> detailList;

    @TableField(exist = false)
    private String customerName;

    /** 采购合同ID（采购退货时使用） */
    private Long purchaseContractId;

    /** 供应商ID（采购退货时使用） */
    private Long supplierId;

    /** 供应商名称（采购退货时使用） */
    private String supplierName;

    /** 采购申请号（非数据库字段，用于列表显示） */
    @TableField(exist = false)
    private String purchaseContractNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOutStockNo() {
        return outStockNo;
    }

    public void setOutStockNo(String outStockNo) {
        this.outStockNo = outStockNo;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getLinkmanId() {
        return linkmanId;
    }

    public void setLinkmanId(Long linkmanId) {
        this.linkmanId = linkmanId;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getTransportMethod() {
        return transportMethod;
    }

    public void setTransportMethod(String transportMethod) {
        this.transportMethod = transportMethod;
    }

    public BigDecimal getTransportAmount() {
        return transportAmount;
    }

    public void setTransportAmount(BigDecimal transportAmount) {
        this.transportAmount = transportAmount;
    }
    public String getTransportCompany() { return transportCompany; }
    public void setTransportCompany(String transportCompany) { this.transportCompany = transportCompany; }
    public String getTransportContact() { return transportContact; }
    public void setTransportContact(String transportContact) { this.transportContact = transportContact; }
    public String getTransportContactPhone() { return transportContactPhone; }
    public void setTransportContactPhone(String transportContactPhone) { this.transportContactPhone = transportContactPhone; }
    public String getTransportUnit() { return transportUnit; }
    public void setTransportUnit(String transportUnit) { this.transportUnit = transportUnit; }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public String getOutStockType() {
        return outStockType;
    }

    public void setOutStockType(String outStockType) {
        this.outStockType = outStockType;
    }

    public String getOutboundCategory() {
        return outboundCategory;
    }

    public void setOutboundCategory(String outboundCategory) {
        this.outboundCategory = outboundCategory;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public LocalDateTime getOutStockDate() {
        return outStockDate;
    }

    public void setOutStockDate(LocalDateTime outStockDate) {
        this.outStockDate = outStockDate;
    }
    public LocalDateTime getScheduledShipTime() { return scheduledShipTime; }
    public void setScheduledShipTime(LocalDateTime scheduledShipTime) { this.scheduledShipTime = scheduledShipTime; }
    public LocalDateTime getExpectedArrivalTime() { return expectedArrivalTime; }
    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) { this.expectedArrivalTime = expectedArrivalTime; }
    public LocalDateTime getActualShipTime() { return actualShipTime; }
    public void setActualShipTime(LocalDateTime actualShipTime) { this.actualShipTime = actualShipTime; }
    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }
    public String getArrivalRemark() { return arrivalRemark; }
    public void setArrivalRemark(String arrivalRemark) { this.arrivalRemark = arrivalRemark; }
    public String getArrivalImages() { return arrivalImages; }
    public void setArrivalImages(String arrivalImages) { this.arrivalImages = arrivalImages; }
    public LocalDateTime getWarrantyExpireTime() { return warrantyExpireTime; }
    public void setWarrantyExpireTime(LocalDateTime warrantyExpireTime) { this.warrantyExpireTime = warrantyExpireTime; }

    public LocalDateTime getReturnSignTime() { return returnSignTime; }
    public void setReturnSignTime(LocalDateTime returnSignTime) { this.returnSignTime = returnSignTime; }

    public LocalDateTime getReturnNotifyTime() { return returnNotifyTime; }
    public void setReturnNotifyTime(LocalDateTime returnNotifyTime) { this.returnNotifyTime = returnNotifyTime; }

    public String getReturnAttachments() { return returnAttachments; }
    public void setReturnAttachments(String returnAttachments) { this.returnAttachments = returnAttachments; }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditDetail() {
        return auditDetail;
    }

    public void setAuditDetail(String auditDetail) {
        this.auditDetail = auditDetail;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getDelivererId() {
        return delivererId;
    }

    public void setDelivererId(Long delivererId) {
        this.delivererId = delivererId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<ScmOutboundDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<ScmOutboundDetail> detailList) {
        this.detailList = detailList;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getPurchaseContractId() {
        return purchaseContractId;
    }

    public void setPurchaseContractId(Long purchaseContractId) {
        this.purchaseContractId = purchaseContractId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getPurchaseContractNo() {
        return purchaseContractNo;
    }

    public void setPurchaseContractNo(String purchaseContractNo) {
        this.purchaseContractNo = purchaseContractNo;
    }
}
