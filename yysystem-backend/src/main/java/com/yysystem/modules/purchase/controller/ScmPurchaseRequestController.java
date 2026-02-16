package com.yysystem.modules.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequest;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestItemMapper;
import com.yysystem.modules.purchase.service.ScmPurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scm/purchase-request")
public class ScmPurchaseRequestController {
    @Autowired
    private ScmPurchaseRequestService requestService;
    @Autowired
    private ScmPurchaseRequestItemMapper itemMapper;
    @Autowired
    private com.yysystem.modules.supplier.service.ScmSupplierService supplierService;
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('PA:list')")
    public Result<Page<ScmPurchaseRequest>> list(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String requestNo,
                                                 @RequestParam(required = false) String supplierName) {
        Page<ScmPurchaseRequest> page = new Page<>(current, size);
        LambdaQueryWrapper<ScmPurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        if (requestNo != null && !requestNo.isEmpty()) {
            wrapper.like(ScmPurchaseRequest::getRequestNo, requestNo);
        }
        if (supplierName != null && !supplierName.isEmpty()) {
            wrapper.like(ScmPurchaseRequest::getSupplierName, supplierName);
        }
        wrapper.orderByDesc(ScmPurchaseRequest::getCreateTime);
        Page<ScmPurchaseRequest> result = requestService.page(page, wrapper);
        // 为每个采购申请表添加商品明细和供应商名称
        for (ScmPurchaseRequest req : result.getRecords()) {
            List<ScmPurchaseRequestItem> items = itemMapper.selectList(new LambdaQueryWrapper<ScmPurchaseRequestItem>().eq(ScmPurchaseRequestItem::getRequestId, req.getId()));
            req.setItemList(items);
            
            // 设置供应商名称
            if (req.getSupplierId() != null) {
                com.yysystem.modules.supplier.entity.ScmSupplier supplier = supplierService.getById(req.getSupplierId());
                if (supplier != null) {
                    req.setSupplierName(supplier.getSupplierName());
                }
            }
        }
        return Result.success(result);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PA:get')")
    public Result<ScmPurchaseRequest> getById(@PathVariable Long id) {
        ScmPurchaseRequest req = requestService.getById(id);
        if (req != null) {
            List<ScmPurchaseRequestItem> items = itemMapper.selectList(new LambdaQueryWrapper<ScmPurchaseRequestItem>().eq(ScmPurchaseRequestItem::getRequestId, id));
            req.setItemList(items);
            
            // 设置供应商名称
            if (req.getSupplierId() != null) {
                com.yysystem.modules.supplier.entity.ScmSupplier supplier = supplierService.getById(req.getSupplierId());
                if (supplier != null) {
                    req.setSupplierName(supplier.getSupplierName());
                }
            }
        }
        return Result.success(req);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('PA:create')")
    public Result<Boolean> save(@RequestBody ScmPurchaseRequest req) {
        return Result.success(requestService.saveWithItems(req));
    }
    @PutMapping
    @PreAuthorize("hasAuthority('PA:update')")
    public Result<Boolean> update(@RequestBody ScmPurchaseRequest req) {
        return Result.success(requestService.updateWithItems(req));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PA:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        itemMapper.delete(new LambdaQueryWrapper<ScmPurchaseRequestItem>().eq(ScmPurchaseRequestItem::getRequestId, id));
        return Result.success(requestService.removeById(id));
    }
    
    /**
     * 锁单功能
     * 当所有商品全部到货验收后，可以锁单
     */
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('PA:update')")
    public Result<Boolean> lock(@PathVariable Long id) {
        ScmPurchaseRequest req = requestService.getById(id);
        if (req == null) {
            return Result.error("采购申请不存在");
        }
        
        // 检查是否所有商品都已到货验收
        List<ScmPurchaseRequestItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<ScmPurchaseRequestItem>()
                .eq(ScmPurchaseRequestItem::getRequestId, id)
        );
        
        if (items.isEmpty()) {
            return Result.error("没有采购商品明细，不能锁单");
        }
        
        // 检查是否所有商品都已到货验收
        for (ScmPurchaseRequestItem item : items) {
            if (item.getArrivalQuantity() == null || item.getAcceptQuantity() == null ||
                item.getArrivalQuantity() < item.getPurchaseQuantity() ||
                item.getAcceptQuantity() < item.getPurchaseQuantity()) {
                return Result.error("还有商品未到货或未验收，不能锁单");
            }
        }
        
        // 设置锁单状态
        req.setLockStatus("1");
        return Result.success(requestService.updateById(req));
    }
    
    /**
     * 解锁功能
     * 管理员可以解锁
     */
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('PA:update')")
    public Result<Boolean> unlock(@PathVariable Long id) {
        ScmPurchaseRequest req = requestService.getById(id);
        if (req == null) {
            return Result.error("采购申请不存在");
        }
        
        // 设置解锁状态
        req.setLockStatus("0");
        return Result.success(requestService.updateById(req));
    }
    
    /**
     * 审核采购申请
     * @param id 采购申请ID
     * @param status 审核状态：PASSED(通过)、REJECTED(驳回)
     * @param detail 审核意见
     */
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('PA:update')")
    public Result<Boolean> audit(@PathVariable Long id, 
                                  @RequestParam String status,
                                  @RequestParam(required = false) String detail) {
        ScmPurchaseRequest req = requestService.getById(id);
        if (req == null) {
            return Result.error("采购申请不存在");
        }
        
        // 验证状态值
        if (!"PASSED".equals(status) && !"REJECTED".equals(status)) {
            return Result.error("审核状态无效，只能是 PASSED 或 REJECTED");
        }
        
        // 设置审核状态
        req.setAuditStatus(status);
        req.setAuditDetail(detail);
        
        return Result.success(requestService.updateById(req));
    }
}
