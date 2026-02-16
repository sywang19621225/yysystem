package com.yysystem.modules.purchase.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.purchase.entity.ScmPurchase;
import com.yysystem.modules.supplier.entity.ScmSupplier;
import com.yysystem.modules.supplier.service.ScmSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scm/purchase")
public class ScmPurchaseController {

    @Autowired
    private ScmSupplierService scmSupplierService;

    @PostMapping
    public Result<Long> save(@RequestBody ScmPurchase scmPurchase) {
        // 采购管理功能已停用，此方法暂时禁用
        return Result.error("采购管理功能已停用");
        // return Result.success(scmPurchaseService.savePurchase(scmPurchase));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody ScmPurchase scmPurchase) {
        // 采购管理功能已停用，此方法暂时禁用
        return Result.error("采购管理功能已停用");
        // scmPurchase.setId(id);
        // return Result.success(scmPurchaseService.updatePurchase(scmPurchase));
    }
    
    @PutMapping("/{id}/audit")
    public Result<Boolean> audit(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String detail) {
        // 采购管理功能已停用，此方法暂时禁用
        return Result.error("采购管理功能已停用");
        // return Result.success(scmPurchaseService.auditPurchase(id, status, detail));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        // 采购管理功能已停用，此方法暂时禁用
        return Result.error("采购管理功能已停用");
        // return Result.success(scmPurchaseService.removeById(id));
    }

    @GetMapping("/{id}")
    public Result<ScmPurchase> getById(@PathVariable Long id) {
        // 采购管理功能已停用，此方法暂时禁用
        return Result.error("采购管理功能已停用");
        /*
        ScmPurchase purchase = scmPurchaseService.getPurchaseById(id);
        if (purchase != null && purchase.getSupplierId() != null) {
            ScmSupplier supplier = scmSupplierService.getById(purchase.getSupplierId());
            if (supplier != null) {
                purchase.setSupplierName(supplier.getSupplierName());
            }
        }
        return Result.success(purchase);
        */
    }

    @GetMapping("/list")
    public Result<Page<ScmPurchase>> list(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        // 采购管理功能已停用，此方法暂时禁用
        return Result.error("采购管理功能已停用");
        /*
        Page<ScmPurchase> page = scmPurchaseService.page(new Page<>(current, size));
        List<ScmPurchase> records = page.getRecords();
        for (ScmPurchase record : records) {
            if (record.getSupplierId() != null) {
                ScmSupplier supplier = scmSupplierService.getById(record.getSupplierId());
                if (supplier != null) {
                    record.setSupplierName(supplier.getSupplierName());
                }
            }
        }
        return Result.success(page);
        */
    }
}