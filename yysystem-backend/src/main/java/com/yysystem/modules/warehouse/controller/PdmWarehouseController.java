package com.yysystem.modules.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.warehouse.entity.PdmWarehouse;
import com.yysystem.modules.warehouse.service.PdmWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdm/warehouse")
public class PdmWarehouseController {

    @Autowired
    private PdmWarehouseService pdmWarehouseService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('WM:list')")
    public Result<Page<PdmWarehouse>> list(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String warehouseName) {
        Page<PdmWarehouse> page = new Page<>(current, size);
        LambdaQueryWrapper<PdmWarehouse> wrapper = new LambdaQueryWrapper<>();
        if (warehouseName != null && !warehouseName.isEmpty()) {
            wrapper.like(PdmWarehouse::getWarehouseName, warehouseName);
        }
        wrapper.orderByDesc(PdmWarehouse::getCreateTime);
        return Result.success(pdmWarehouseService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WM:get')")
    public Result<PdmWarehouse> getById(@PathVariable Long id) {
        return Result.success(pdmWarehouseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WM:create')")
    public Result<Boolean> save(@RequestBody PdmWarehouse pdmWarehouse) {
        return Result.success(pdmWarehouseService.save(pdmWarehouse));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('WM:update')")
    public Result<Boolean> update(@RequestBody PdmWarehouse pdmWarehouse) {
        return Result.success(pdmWarehouseService.updateById(pdmWarehouse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WM:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(pdmWarehouseService.removeById(id));
    }
}
