package com.yysystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;

@TableName("sys_team")
public class SysTeam {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String teamName;
    
    private Long parentId;
    
    private Long managerId;
    
    private String area;
    
    private String teamDesc;
    
    private LocalDateTime createTime;

    private String memberIds;

    private String extendFields;

    @TableField(exist = false)
    private List<SysTeam> children;
    
    @TableField(exist = false)
    private String managerName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    
    public String getTeamDesc() { return teamDesc; }
    public void setTeamDesc(String teamDesc) { this.teamDesc = teamDesc; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public String getMemberIds() { return memberIds; }
    public void setMemberIds(String memberIds) { this.memberIds = memberIds; }

    public String getExtendFields() { return extendFields; }
    public void setExtendFields(String extendFields) { this.extendFields = extendFields; }
    
    public List<SysTeam> getChildren() { return children; }
    public void setChildren(List<SysTeam> children) { this.children = children; }
    
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
}
