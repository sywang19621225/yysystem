package com.yysystem.modules.crm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_follow_record")
public class CrmFollowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long customerId;

    @TableField("contact_id")
    private Long contactId;

    @TableField("phone")
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followTime;

    private String followType;

    private String followContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextFollowTime;

    @TableField("follower_id")
    private Long followerId;

    @TableField("attachments")
    private String attachments;

    @TableField("site_photos")
    private String sitePhotos;

    @TableField("budget_amount")
    private java.math.BigDecimal budgetAmount;

    @TableField("quote_id")
    private Long quoteId;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField("extend_fields")
    private String extendFields;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String followerName;

    @TableField(exist = false)
    private String contactName;

    @TableField(exist = false)
    private String contactPhone;
}
