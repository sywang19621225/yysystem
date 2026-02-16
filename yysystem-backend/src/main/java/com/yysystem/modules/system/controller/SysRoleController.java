package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysRole;
import com.yysystem.modules.system.service.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/list")
    public Result<Page<SysRole>> list(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<SysRole> page = new Page<>(current, size);
        return Result.success(sysRoleService.page(page));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysRole sysRole) {
        if (sysRole.getRoleName() == null || sysRole.getRoleName().trim().isEmpty()) {
            throw new BizException(400, "角色名称不能为空");
        }
        SysRole existed = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleName, sysRole.getRoleName()).last("limit 1"));
        if (existed != null) {
            throw new BizException(400, "角色名称已存在");
        }
        sysRole.setCreateTime(LocalDateTime.now());
        return Result.success(sysRoleService.save(sysRole));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysRole sysRole) {
        if (sysRole.getRoleName() != null && !sysRole.getRoleName().trim().isEmpty()) {
            SysRole duplicate = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleName, sysRole.getRoleName())
                    .ne(SysRole::getId, sysRole.getId())
                    .last("limit 1"));
            if (duplicate != null) {
                throw new BizException(400, "角色名称已存在");
            }
        }
        return Result.success(sysRoleService.updateById(sysRole));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysRoleService.removeById(id));
    }
}
