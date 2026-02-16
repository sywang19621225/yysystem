package com.yysystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roleName;
    
    private String userIds;

    private String userType;

    private String faceAvatar;

    private Long teamId;

    private Long deptId;

    private String phone;

    private Integer status;

    private LocalDateTime createTime;

    private String permissions;

    private String extendFields;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    public String getUserIds() { return userIds; }
    public void setUserIds(String userIds) { this.userIds = userIds; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getFaceAvatar() { return faceAvatar; }
    public void setFaceAvatar(String faceAvatar) { this.faceAvatar = faceAvatar; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public String getExtendFields() { return extendFields; }
    public void setExtendFields(String extendFields) { this.extendFields = extendFields; }
}
