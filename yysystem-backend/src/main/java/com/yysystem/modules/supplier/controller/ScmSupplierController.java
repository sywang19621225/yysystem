package com.yysystem.modules.supplier.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.supplier.entity.ScmSupplier;
import com.yysystem.modules.supplier.service.ScmSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scm/supplier")
public class ScmSupplierController {

    @Autowired
    private ScmSupplierService scmSupplierService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('SM:list')")
    public Result<Page<ScmSupplier>> list(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String supplierName) {
        Page<ScmSupplier> page = new Page<>(current, size);
        LambdaQueryWrapper<ScmSupplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(ScmSupplier::getSupplierName, supplierName);
        }
        wrapper.orderByDesc(ScmSupplier::getCreateTime);
        return Result.success(scmSupplierService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SM:get')")
    public Result<ScmSupplier> getById(@PathVariable Long id) {
        return Result.success(scmSupplierService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SM:create')")
    public Result<Boolean> save(@RequestBody ScmSupplier scmSupplier) {
        try {
            return Result.success(scmSupplierService.save(scmSupplier));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("保存失败，请稍后重试");
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('SM:update')")
    public Result<Boolean> update(@RequestBody ScmSupplier scmSupplier) {
        try {
            return Result.success(scmSupplierService.updateById(scmSupplier));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新失败，请稍后重试");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SM:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(scmSupplierService.removeById(id));
    }
}
