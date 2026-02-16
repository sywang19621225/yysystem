package com.yysystem.modules.asm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("asm_repair_record")
public class AsmRepairRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;

    private String faultCause;

    private String solution;

    private BigDecimal repairCost;

    private BigDecimal materialCost;

    private BigDecimal totalCost;

    private String repairStatus; // FIXED, PENDING, UNFIXABLE

    private LocalDateTime createTime;
}
