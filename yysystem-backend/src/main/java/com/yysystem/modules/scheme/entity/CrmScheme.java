package com.yysystem.modules.scheme.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_scheme")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmScheme implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String schemeNo;

    private Long customerId;

    private String customerName;

    private String title;

    private Long salesId;

    private String salesName;

    private String customerRequirements;

    private String customerFiles;

    private String textContent;

    private String textContentAi;
    
    private String solutionContent;
    
    private String attachments;

    private String designFiles;

    private Integer status;

    private Long designerId;

    private String designerName;

    private LocalDateTime designTime;

    private Long approverId;

    private String approverName;

    private String approvalOpinion;

    private LocalDateTime approvalTime;

    private LocalDateTime deliverTime;

    private String deliverFiles;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    @TableLogic
    private Integer deleted;
}
