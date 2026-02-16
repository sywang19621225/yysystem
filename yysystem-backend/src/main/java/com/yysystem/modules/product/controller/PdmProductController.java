package com.yysystem.modules.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.service.PdmProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/pdm/product")
public class PdmProductController {

    @Autowired
    private PdmProductService pdmProductService;

    @PreAuthorize("hasAuthority('PDM:create')")
    @PostMapping
    public Result<Boolean> save(@RequestBody PdmProduct pdmProduct) {
        if (StrUtil.isNotBlank(pdmProduct.getProductCode())) {
            long dupCode = pdmProductService.count(new LambdaQueryWrapper<PdmProduct>()
                    .eq(PdmProduct::getProductCode, pdmProduct.getProductCode()));
            if (dupCode > 0) {
                return Result.error("商品编号已存在，请更换编号");
            }
        }
        if (StrUtil.isNotBlank(pdmProduct.getProductName())) {
            LambdaQueryWrapper<PdmProduct> wrapper = new LambdaQueryWrapper<PdmProduct>()
                    .eq(PdmProduct::getProductName, pdmProduct.getProductName())
                    .eq(StrUtil.isNotBlank(pdmProduct.getProductSpec()), PdmProduct::getProductSpec, pdmProduct.getProductSpec())
                    .eq(StrUtil.isNotBlank(pdmProduct.getProductUnit()), PdmProduct::getProductUnit, pdmProduct.getProductUnit());
            long dup = pdmProductService.count(wrapper);
            if (dup > 0) {
                return Result.error("商品已存在，请勿重复登记");
            }
        }
        try {
            return Result.success(pdmProductService.save(pdmProduct));
        } catch (DataIntegrityViolationException e) {
            return Result.error("保存失败：数据唯一性冲突，请检查商品编号或名称");
        } catch (Exception e) {
            return Result.error("保存失败，请稍后重试");
        }
    }

    @PreAuthorize("hasAuthority('PDM:update')")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody PdmProduct pdmProduct) {
        pdmProduct.setId(id);
        if (StrUtil.isNotBlank(pdmProduct.getProductCode())) {
            long dupCode = pdmProductService.count(new LambdaQueryWrapper<PdmProduct>()
                    .eq(PdmProduct::getProductCode, pdmProduct.getProductCode())
                    .ne(PdmProduct::getId, id));
            if (dupCode > 0) {
                return Result.error("商品编号已存在，请更换编号");
            }
        }
        if (StrUtil.isNotBlank(pdmProduct.getProductName())) {
            LambdaQueryWrapper<PdmProduct> wrapper = new LambdaQueryWrapper<PdmProduct>()
                    .eq(PdmProduct::getProductName, pdmProduct.getProductName())
                    .eq(StrUtil.isNotBlank(pdmProduct.getProductSpec()), PdmProduct::getProductSpec, pdmProduct.getProductSpec())
                    .eq(StrUtil.isNotBlank(pdmProduct.getProductUnit()), PdmProduct::getProductUnit, pdmProduct.getProductUnit())
                    .ne(PdmProduct::getId, id);
            long dup = pdmProductService.count(wrapper);
            if (dup > 0) {
                return Result.error("商品已存在，请勿重复登记");
            }
        }
        try {
            return Result.success(pdmProductService.updateById(pdmProduct));
        } catch (DataIntegrityViolationException e) {
            return Result.error("更新失败：数据唯一性冲突，请检查商品编号或名称");
        } catch (Exception e) {
            return Result.error("更新失败，请稍后重试");
        }
    }

    @PreAuthorize("hasAuthority('PDM:delete')")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(pdmProductService.removeById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Result<PdmProduct> getById(@PathVariable Long id) {
        return Result.success(pdmProductService.getById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public Result<Page<PdmProduct>> list(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String productName,
                                         @RequestParam(required = false) String productCode,
                                         @RequestParam(required = false) String productCategory,
                                         @RequestParam(required = false) String brand,
                                         @RequestParam(required = false) Long supplierId) {
        Page<PdmProduct> page = new Page<>(current, size);
        LambdaQueryWrapper<PdmProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(productName), PdmProduct::getProductName, productName);
        wrapper.like(StrUtil.isNotBlank(productCode), PdmProduct::getProductCode, productCode);
        wrapper.eq(StrUtil.isNotBlank(productCategory), PdmProduct::getProductCategory, productCategory);
        wrapper.like(StrUtil.isNotBlank(brand), PdmProduct::getBrand, brand);
        wrapper.eq(supplierId != null, PdmProduct::getSupplierId, supplierId);
        wrapper.orderByDesc(PdmProduct::getCreateTime);
        return Result.success(pdmProductService.page(page, wrapper));
    }
}
