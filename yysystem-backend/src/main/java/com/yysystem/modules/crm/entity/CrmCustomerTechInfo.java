package com.yysystem.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer_tech_info")
public class CrmCustomerTechInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "customer_id")
    private Long customerId;

    @TableField(value = "location")
    private String location;

    @TableField(value = "system_name")
    private String systemName;

    @TableField(value = "ip_address")
    private String ipAddress;
    @TableField(value = "remote_ip")
    private String remoteIp;

    @TableField(value = "remote_method")
    private String remoteMethod;

    @TableField(value = "system_account")
    private String systemAccount;

    @TableField(value = "system_password")
    private String systemPassword;

    @TableField(value = "remote_account")
    private String remoteAccount;

    @TableField(value = "remote_password")
    private String remotePassword;

    @TableField(value = "software_url")
    private String softwareUrl;

    @TableField(value = "software_name")
    private String softwareName;
    @TableField(value = "attachments")
    private String attachments;

    private String remark;

    @TableField(value = "tech_leader")
    private String techLeader;

    @TableField(value = "tech_contact")
    private String techContact;

    @TableField(value = "tech_wechat")
    private String techWechat;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
