package com.yysystem.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer_linkman")
public class CrmContact implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("customer_id")
    private Long customerId;

    @TableField("linkman_name")
    private String name;

    @TableField(exist = false)
    private Integer gender;

    @TableField("phone")
    private String phone;

    @TableField("wechat")
    private String wechat;

    @TableField("position")
    private String position;

    @TableField(exist = false)
    private String email;

    @TableField("personal_photo")
    private String avatarUrl;

    @TableField("is_default")
    private Integer isPrimary;

    private String remark;

    @TableField("extend_fields")
    private String extendFields;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
