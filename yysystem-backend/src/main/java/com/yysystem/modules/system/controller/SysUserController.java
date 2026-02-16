package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.system.entity.SysDept;
import com.yysystem.modules.system.entity.SysRole;
import com.yysystem.modules.system.entity.SysTeam;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysDeptService;
import com.yysystem.modules.system.service.SysRoleService;
import com.yysystem.modules.system.service.SysTeamService;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysTeamService sysTeamService;

    @Autowired
    private CrmCustomerService crmCustomerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public Result<Page<SysUser>> list(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false) Boolean exact) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            String un = username.trim();
            if (Boolean.TRUE.equals(exact)) {
                wrapper.eq(SysUser::getUsername, un);
            } else {
                wrapper.like(SysUser::getUsername, un);
            }
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserService.page(page, wrapper);
        List<SysUser> records = result.getRecords();
        for (SysUser user : records) {
            if (user.getRoleId() != null) {
                SysRole role = sysRoleService.getById(user.getRoleId());
                if (role != null) user.setRoleName(role.getRoleName());
            }
            if (user.getDeptId() != null) {
                SysDept dept = sysDeptService.getById(user.getDeptId());
                if (dept != null) user.setDeptName(dept.getDeptName());
            }
            if (user.getTeamId() != null) {
                SysTeam team = sysTeamService.getById(user.getTeamId());
                if (team != null) user.setTeamName(team.getTeamName());
            }
            if (user.getCompanyId() != null) {
                CrmCustomer customer = crmCustomerService.getById(user.getCompanyId());
                if (customer != null) user.setCompanyName(customer.getCustomerName());
            }
        }
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysUser sysUser) {
        // 新增用户默认密码 123456
        if (sysUser.getPassword() == null || sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(passwordEncoder.encode("123456"));
        } else {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }
        
        // 处理唯一键约束字段，空字符串转为null
        if (sysUser.getPhone() != null && sysUser.getPhone().isEmpty()) {
            sysUser.setPhone(null);
        }
        if (sysUser.getUsername() != null) {
            sysUser.setUsername(sysUser.getUsername().trim());
        }
        if (sysUser.getUsername() == null || sysUser.getUsername().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        long dup = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, sysUser.getUsername()));
        if (dup > 0) {
            return Result.error("用户名已存在");
        }
        if ((sysUser.getUserType() == null || sysUser.getUserType().isEmpty()) && sysUser.getRoleId() != null) {
            SysRole role = sysRoleService.getById(sysUser.getRoleId());
            if (role != null) {
                String name = role.getRoleName() != null ? role.getRoleName() : "";
                if (name.contains("管理员") || name.toLowerCase().contains("admin")) {
                    sysUser.setUserType("admin");
                } else if (name.contains("财务")) {
                    sysUser.setUserType("finance");
                } else if (name.contains("采购")) {
                    sysUser.setUserType("purchase");
                } else if (name.contains("库")) {
                    sysUser.setUserType("stock");
                } else if (name.contains("售后")) {
                    sysUser.setUserType("after_sales");
                } else if (name.contains("技术") || name.contains("运维") || name.contains("研发")) {
                    sysUser.setUserType("tech");
                } else {
                    sysUser.setUserType("sales");
                }
            }
        }
        
        return Result.success(sysUserService.save(sysUser));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser sysUser) {
        // 如果修改密码
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }
        
        // 处理唯一键约束字段，空字符串转为null
        if (sysUser.getPhone() != null && sysUser.getPhone().isEmpty()) {
            sysUser.setPhone(null);
        }
        if ((sysUser.getUserType() == null || sysUser.getUserType().isEmpty()) && sysUser.getRoleId() != null) {
            SysRole role = sysRoleService.getById(sysUser.getRoleId());
            if (role != null) {
                String name = role.getRoleName() != null ? role.getRoleName() : "";
                if (name.contains("管理员") || name.toLowerCase().contains("admin")) {
                    sysUser.setUserType("admin");
                } else if (name.contains("财务")) {
                    sysUser.setUserType("finance");
                } else if (name.contains("采购")) {
                    sysUser.setUserType("purchase");
                } else if (name.contains("库")) {
                    sysUser.setUserType("stock");
                } else if (name.contains("售后")) {
                    sysUser.setUserType("after_sales");
                } else if (name.contains("技术") || name.contains("运维") || name.contains("研发")) {
                    sysUser.setUserType("tech");
                } else {
                    sysUser.setUserType("sales");
                }
            }
        }
        
        return Result.success(sysUserService.updateById(sysUser));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }
}
