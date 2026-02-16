package com.yysystem.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmCustomer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String customerCode;

    private String customerName;

    private String customerTag;

    private String customerCategory;

    private String address;

    private String website;

    private String linkman;

    private String phone;

    private String bankAccountName;

    private String bankName;

    private String bankAccount;

    private String creditCode;

    private String registerAddress;

    private String registerPhone;

    private String status;

    private Long principalId;
    @TableField(exist = false)
    private String principalName;

    private String createBy;

    private Long creatorId;

    private String companyIntro;

    private String companyLogo;
    
    private String transportationMode;
    
    private String fundingSource;
    
    private java.math.BigDecimal registeredCapital;
    
    private LocalDate validUntil;
    
    private String registrationStatus;
    
    private String registrationAuthority;
    
    private String wechatOfficialAccount;
    
    private String wechatId;
    
    private String qrCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
