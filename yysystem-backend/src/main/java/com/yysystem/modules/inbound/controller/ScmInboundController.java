package com.yysystem.modules.inbound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.inbound.entity.ScmInbound;
import com.yysystem.modules.inbound.entity.ScmInboundDetail;
import com.yysystem.modules.inbound.service.ScmInboundService;
import com.yysystem.modules.product.service.PdmProductService;
import com.yysystem.modules.product.entity.PdmProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

@RestController
@RequestMapping("/api/scm/inbound")
public class ScmInboundController {

    @Autowired
    private ScmInboundService scmInboundService;

    @Autowired
    private PdmProductService pdmProductService;

    @PostMapping("/create-from-purchase/{purchaseId}")
    @PreAuthorize("hasAuthority('WHM:create')")
    public Result<Long> createFromPurchase(@PathVariable Long purchaseId) {
        return Result.error("采购管理功能已停用");
    }
    
    @PostMapping("/create-from-purchase-request/{requestId}")
    @PreAuthorize("hasAuthority('WHM:create')")
    public Result<Long> createFromPurchaseRequest(@PathVariable Long requestId) {
        return Result.success(scmInboundService.createFromPurchaseRequest(requestId));
    }
    
    @PostMapping("/create-transfer-from-purchase/{requestId}")
    @PreAuthorize("hasAuthority('WHM:create')")
    public Result<Long> createTransferFromPurchase(@PathVariable Long requestId) {
        return Result.success(scmInboundService.createTransferFromPurchase(requestId));
    }
    
    @PostMapping("/create-from-sales-return/{outboundId}")
    @PreAuthorize("hasAuthority('WHM:create')")
    public Result<Long> createFromSalesReturn(@PathVariable Long outboundId) {
        return Result.success(scmInboundService.createFromSalesReturn(outboundId));
    }
    
    @PostMapping("/create-transfer")
    @PreAuthorize("hasAuthority('WHM:create')")
    public Result<Long> createTransferInbound(@RequestBody ScmInbound inbound) {
        return Result.success(scmInboundService.createTransferInbound(inbound));
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('WHM:update')")
    public Result<Boolean> audit(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String detail) {
        return Result.success(scmInboundService.auditInbound(id, status, detail));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WHM:create')")
    public Result<Long> save(@RequestBody ScmInbound scmInbound) {
        return Result.success(scmInboundService.saveInbound(scmInbound));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WHM:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody ScmInbound scmInbound) {
        scmInbound.setId(id);
        return Result.success(scmInboundService.updateInbound(scmInbound));
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WHM:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        // 查询入库单信息
        ScmInbound inbound = scmInboundService.getInboundById(id);
        if (inbound == null) {
            return Result.error("入库单不存在");
        }

        // 如果已审核通过，需要回滚库存（减去入库的数量）
        if ("PASSED".equals(inbound.getAuditStatus())) {
            List<ScmInboundDetail> details = inbound.getDetailList();
            if (details != null && !details.isEmpty()) {
                for (ScmInboundDetail detail : details) {
                    String productCode = detail.getProductCode();
                    int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;

                    // 查询商品并回滚库存
                    PdmProduct product = pdmProductService.getOne(
                            new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, productCode)
                    );

                    if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() - quantity);
                        product.setAvailableStock(product.getAvailableStock() - quantity);
                        product.setTotalStock(product.getTotalStock() - quantity);
                        pdmProductService.updateById(product);
                    }
                }
            }
        }

        return Result.success(scmInboundService.removeById(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WHM:get')")
    public Result<ScmInbound> getById(@PathVariable Long id) {
        return Result.success(scmInboundService.getInboundById(id));
    }
    
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('WHM:get')")
    public Result<java.util.List<ScmInboundDetail>> getDetails(@PathVariable Long id) {
        return Result.success(scmInboundService.getInboundDetails(id));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('WHM:list')")
    public Result<Page<ScmInbound>> list(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String inboundType,
                                         @RequestParam(required = false) String inboundNo,
                                         @RequestParam(required = false) String supplierName,
                                         @RequestParam(required = false) String auditStatus) {
        Page<ScmInbound> page = scmInboundService.getInboundList(new Page<>(current, size), inboundType, inboundNo, supplierName, auditStatus);
        return Result.success(page);
    }

    @PostMapping("/detail")
    public Result<Long> addDetail(@RequestBody ScmInboundDetail detail) {
        return Result.success(scmInboundService.addInboundDetail(detail));
    }

    @PutMapping("/detail/{id}")
    public Result<Boolean> updateDetail(@PathVariable Long id, @RequestBody ScmInboundDetail detail) {
        detail.setId(id);
        return Result.success(scmInboundService.updateInboundDetail(detail));
    }

    @DeleteMapping("/detail/{id}")
    public Result<Boolean> removeDetail(@PathVariable Long id) {
        return Result.success(scmInboundService.deleteInboundDetail(id));
    }
}