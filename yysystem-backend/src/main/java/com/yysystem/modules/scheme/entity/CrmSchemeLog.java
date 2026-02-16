package com.yysystem.modules.scheme.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_scheme_log")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmSchemeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long schemeId;

    private String operationType;

    private String operationDesc;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime operationTime;

    private String oldValue;

    private String newValue;
}
