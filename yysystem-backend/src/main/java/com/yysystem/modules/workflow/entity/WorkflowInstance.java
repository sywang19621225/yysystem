package com.yysystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("workflow_instance")
public class WorkflowInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private Long businessId;
    private Integer currentStepIndex;
    private String status;
    private Long createdBy;
    private Long deptId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Integer getCurrentStepIndex() { return currentStepIndex; }
    public void setCurrentStepIndex(Integer currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
