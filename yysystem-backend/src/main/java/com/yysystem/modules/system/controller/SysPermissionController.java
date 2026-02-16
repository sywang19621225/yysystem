package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysPermission;
import com.yysystem.modules.system.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/system/permission")
public class SysPermissionController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public Result<Page<SysPermission>> list(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size) {
        Page<SysPermission> page = new Page<>(current, size);
        return Result.success(sysPermissionService.page(page));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysPermission permission) {
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        return Result.success(sysPermissionService.save(permission));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysPermission permission) {
        permission.setUpdateTime(LocalDateTime.now());
        return Result.success(sysPermissionService.updateById(permission));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysPermissionService.removeById(id));
    }
}
