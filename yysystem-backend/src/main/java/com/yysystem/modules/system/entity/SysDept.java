package com.yysystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;

@TableName("sys_dept")
public class SysDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String deptName;
    
    private Long parentId;
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<SysDept> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public List<SysDept> getChildren() { return children; }
    public void setChildren(List<SysDept> children) { this.children = children; }
}
