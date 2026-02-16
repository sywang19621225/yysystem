package com.yysystem.modules.scheme.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_scheme_approval")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmSchemeApproval implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long schemeId;

    private Integer approvalType;

    private Long approverId;

    private String approverName;

    private Integer approvalStatus;

    private String approvalOpinion;

    private LocalDateTime approvalTime;
}
