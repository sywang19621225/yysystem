package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysDept;
import com.yysystem.modules.system.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/system/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    @GetMapping("/list")
    public Result<Page<SysDept>> list(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<SysDept> page = new Page<>(current, size);
        return Result.success(sysDeptService.page(page));
    }

    @GetMapping("/tree")
    public Result<List<SysDept>> getDeptTree() {
        return Result.success(sysDeptService.getDeptTree());
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysDept sysDept) {
        sysDept.setCreateTime(LocalDateTime.now());
        return Result.success(sysDeptService.save(sysDept));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysDept sysDept) {
        return Result.success(sysDeptService.updateById(sysDept));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // TODO: check if has children
        return Result.success(sysDeptService.removeById(id));
    }
}
