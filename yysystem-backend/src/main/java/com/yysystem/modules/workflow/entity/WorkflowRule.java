package com.yysystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("workflow_rule")
public class WorkflowRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private java.math.BigDecimal amountMin;
    private java.math.BigDecimal amountMax;
    private String expenseType;
    private Long departmentId;
    private String stepsJson;
    private String parallelPolicy;
    private Integer slaHours;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public java.math.BigDecimal getAmountMin() { return amountMin; }
    public void setAmountMin(java.math.BigDecimal amountMin) { this.amountMin = amountMin; }
    public java.math.BigDecimal getAmountMax() { return amountMax; }
    public void setAmountMax(java.math.BigDecimal amountMax) { this.amountMax = amountMax; }
    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }
    public String getParallelPolicy() { return parallelPolicy; }
    public void setParallelPolicy(String parallelPolicy) { this.parallelPolicy = parallelPolicy; }
    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
