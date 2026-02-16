package com.yysystem.modules.outbound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

@TableName("scm_outbound_detail")
public class ScmOutboundDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("out_stock_id")
    private Long outStockId;

    @TableField("contract_detail_id")
    private Long contractDetailId;

    @TableField("product_name")
    private String productName;

    @TableField("product_code")
    private String productCode;

    @TableField("product_spec")
    private String productSpec;

    @TableField("product_unit")
    private String productUnit;

    @TableField("out_quantity")
    private Integer outQuantity;

    @TableField("serial_numbers")
    private String serialNumbers;

    @TableField("remark")
    private String remark;
    /** 商品图片 */
    @TableField("product_image")
    private String productImage;
    /** 商品二维码（商品编码+日期+序列） */
    @TableField("product_qr_code")
    private String productQrCode;
    /** 设备IP地址 */
    @TableField("device_ip")
    private String deviceIp;
    /** 远程登录账号 */
    @TableField("remote_account")
    private String remoteAccount;
    /** 远程登录密码 */
    @TableField("remote_password")
    private String remotePassword;
    /** 系统登录账号 */
    @TableField("system_account")
    private String systemAccount;
    /** 系统登录密码 */
    @TableField("system_password")
    private String systemPassword;
    /** 远程方式（来自通用设置） */
    @TableField("remote_method")
    private String remoteMethod;
    /** 操作系统（来自通用设置） */
    @TableField("operating_system")
    private String operatingSystem;
    /** 现场放置位置 */
    @TableField("onsite_location")
    private String onsiteLocation;
    /** 现场照片（多图，逗号或JSON） */
    @TableField("onsite_photos")
    private String onsitePhotos;
    /** 定时开关机时间说明 */
    @TableField("schedule_power_time")
    private String schedulePowerTime;
    /** 定时开机时间 */
    @TableField("schedule_power_on_time")
    private String schedulePowerOnTime;
    /** 定时关机时间 */
    @TableField("schedule_power_off_time")
    private String schedulePowerOffTime;
    /** 定制程序备份说明 */
    @TableField("program_backup")
    private String programBackup;
    /** 参数（JSON文档，来自合同明细customRemark） */
    @TableField("parameters")
    private String parameters;
    /** 单价（本次发货） */
    @TableField("sales_price")
    private java.math.BigDecimal salesPrice;
    /** 金额（单价*数量） */
    @TableField("amount")
    private java.math.BigDecimal amount;

    @TableField("create_time")
    private java.time.LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOutStockId() {
        return outStockId;
    }

    public void setOutStockId(Long outStockId) {
        this.outStockId = outStockId;
    }

    public Long getContractDetailId() {
        return contractDetailId;
    }

    public void setContractDetailId(Long contractDetailId) {
        this.contractDetailId = contractDetailId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getProductQrCode() { return productQrCode; }
    public void setProductQrCode(String productQrCode) { this.productQrCode = productQrCode; }
    public String getDeviceIp() { return deviceIp; }
    public void setDeviceIp(String deviceIp) { this.deviceIp = deviceIp; }
    public String getRemoteAccount() { return remoteAccount; }
    public void setRemoteAccount(String remoteAccount) { this.remoteAccount = remoteAccount; }
    public String getRemotePassword() { return remotePassword; }
    public void setRemotePassword(String remotePassword) { this.remotePassword = remotePassword; }
    public String getSystemAccount() { return systemAccount; }
    public void setSystemAccount(String systemAccount) { this.systemAccount = systemAccount; }
    public String getSystemPassword() { return systemPassword; }
    public void setSystemPassword(String systemPassword) { this.systemPassword = systemPassword; }
    public String getRemoteMethod() { return remoteMethod; }
    public void setRemoteMethod(String remoteMethod) { this.remoteMethod = remoteMethod; }
    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }
    public String getOnsiteLocation() { return onsiteLocation; }
    public void setOnsiteLocation(String onsiteLocation) { this.onsiteLocation = onsiteLocation; }
    public String getOnsitePhotos() { return onsitePhotos; }
    public void setOnsitePhotos(String onsitePhotos) { this.onsitePhotos = onsitePhotos; }
    public String getSchedulePowerTime() { return schedulePowerTime; }
    public void setSchedulePowerTime(String schedulePowerTime) { this.schedulePowerTime = schedulePowerTime; }
    public String getSchedulePowerOnTime() { return schedulePowerOnTime; }
    public void setSchedulePowerOnTime(String schedulePowerOnTime) { this.schedulePowerOnTime = schedulePowerOnTime; }
    public String getSchedulePowerOffTime() { return schedulePowerOffTime; }
    public void setSchedulePowerOffTime(String schedulePowerOffTime) { this.schedulePowerOffTime = schedulePowerOffTime; }
    public String getProgramBackup() { return programBackup; }
    public void setProgramBackup(String programBackup) { this.programBackup = programBackup; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public java.math.BigDecimal getSalesPrice() { return salesPrice; }
    public void setSalesPrice(java.math.BigDecimal salesPrice) { this.salesPrice = salesPrice; }
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }

    public Integer getOutQuantity() {
        return outQuantity;
    }

    public void setOutQuantity(Integer outQuantity) {
        this.outQuantity = outQuantity;
    }

    public String getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(String serialNumbers) {
        this.serialNumbers = serialNumbers;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public java.time.LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.time.LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
