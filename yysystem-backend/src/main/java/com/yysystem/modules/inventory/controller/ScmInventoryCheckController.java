package com.yysystem.modules.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.inventory.entity.ScmInventoryCheck;
import com.yysystem.modules.inventory.service.ScmInventoryCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scm/inventory/check")
public class ScmInventoryCheckController {

    @Autowired
    private ScmInventoryCheckService scmInventoryCheckService;

    @GetMapping("/list")
    public Result<Page<ScmInventoryCheck>> list(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String checkNo) {
        Page<ScmInventoryCheck> page = new Page<>(current, size);
        LambdaQueryWrapper<ScmInventoryCheck> wrapper = new LambdaQueryWrapper<>();
        if (checkNo != null && !checkNo.isEmpty()) {
            wrapper.like(ScmInventoryCheck::getCheckNo, checkNo);
        }
        wrapper.orderByDesc(ScmInventoryCheck::getCreateTime);
        return Result.success(scmInventoryCheckService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<ScmInventoryCheck> getById(@PathVariable Long id) {
        return Result.success(scmInventoryCheckService.getById(id));
    }

    @PostMapping
    public Result<Long> save(@RequestBody ScmInventoryCheck scmInventoryCheck) {
        return Result.success(scmInventoryCheckService.createCheck(scmInventoryCheck));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody ScmInventoryCheck scmInventoryCheck) {
        return Result.success(scmInventoryCheckService.updateById(scmInventoryCheck));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(scmInventoryCheckService.removeById(id));
    }
}
