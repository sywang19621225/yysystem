package com.yysystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String operation;
    
    private String content;
    
    private String operator;
    
    private LocalDateTime operationTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    
    public LocalDateTime getOperationTime() { return operationTime; }
    public void setOperationTime(LocalDateTime operationTime) { this.operationTime = operationTime; }
}
