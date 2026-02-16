package com.yysystem.modules.purchase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.system.service.SysUserService;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequest;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestItemMapper;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestMapper;
import com.yysystem.modules.purchase.service.ScmPurchaseRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScmPurchaseRequestServiceImpl extends ServiceImpl<ScmPurchaseRequestMapper, ScmPurchaseRequest> implements ScmPurchaseRequestService {
    @Autowired
    private ScmPurchaseRequestItemMapper itemMapper;
    @Autowired
    private com.yysystem.modules.workflow.service.WorkflowService workflowService;
    @Autowired
    private SysCodeRuleService sysCodeRuleService;
    @Autowired
    private SysUserService sysUserService;
    @Override
    public boolean saveWithItems(ScmPurchaseRequest req) {
        if (req.getRequestNo() == null || req.getRequestNo().isEmpty()) {
            try {
                String code = sysCodeRuleService.generateNextCode("PURCHASE_REQUEST_NO");
                req.setRequestNo(code);
            } catch (Exception e) {
                req.setRequestNo("PR" + System.currentTimeMillis());
            }
        }
        if (req.getRequestDate() == null) {
            req.setRequestDate(LocalDate.now());
        }
        if (req.getRequesterId() == null || req.getDeptId() == null || req.getRequesterName() == null || req.getRequesterName().isEmpty()) {
            try {
                String username = null;
                org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                 if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                     username = authentication.getName();
                 }
                 if (username != null && !username.isEmpty()) {
                    com.yysystem.modules.system.entity.SysUser u = sysUserService.getByUsername(username);
                    if (u != null) {
                        if (req.getRequesterId() == null) req.setRequesterId(u.getId());
                        if (req.getDeptId() == null) req.setDeptId(u.getDeptId());
                        if (req.getRequesterName() == null || req.getRequesterName().isEmpty()) req.setRequesterName(u.getName());
                    }
                }
            } catch (Exception ignored) {}
        }
        req.setStatus("SUBMITTED");
        req.setAuditStatus("PENDING");
        req.setCreateTime(LocalDateTime.now());
        boolean ok = this.save(req);
        List<ScmPurchaseRequestItem> list = req.getItemList();
        if (list != null) {
            for (ScmPurchaseRequestItem it : list) {
                it.setRequestId(req.getId());
                itemMapper.insert(it);
            }
        }
        try {
            workflowService.startInstance("purchase_request", req.getId(), req.getRequesterId(), req.getTotalAmount(), null, req.getDeptId());
        } catch (Exception ignored) {}
        return ok;
    }
    @Override
    public boolean updateWithItems(ScmPurchaseRequest req) {
        req.setUpdateTime(LocalDateTime.now());
        boolean ok = this.updateById(req);
        List<ScmPurchaseRequestItem> list = req.getItemList();
        if (list != null) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmPurchaseRequestItem> delWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmPurchaseRequestItem>()
                            .eq(ScmPurchaseRequestItem::getRequestId, req.getId());
            itemMapper.delete(delWrapper);
            for (ScmPurchaseRequestItem it : list) {
                it.setRequestId(req.getId());
                itemMapper.insert(it);
            }
        }
        
        // 更新采购状态
        updatePurchaseStatus(req.getId());
        
        return ok;
    }
    
    /**
     * 根据商品明细的到货数量和验收数量更新采购状态
     * @param requestId 采购申请表ID
     */
    private void updatePurchaseStatus(Long requestId) {
        // 获取采购申请表
        ScmPurchaseRequest req = this.getById(requestId);
        if (req == null) {
            return;
        }
        
        // 获取采购申请表的商品明细
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmPurchaseRequestItem> itemWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        itemWrapper.eq(ScmPurchaseRequestItem::getRequestId, requestId);
        List<ScmPurchaseRequestItem> items = itemMapper.selectList(itemWrapper);
        
        if (items.isEmpty()) {
            return;
        }
        
        // 检查到货情况
        boolean allArrived = true; // 所有商品都已全部到货
        boolean partiallyArrived = false; // 部分商品已到货
        boolean allToWarehouse = true; // 所有商品都已全部到库
        boolean partiallyToWarehouse = false; // 部分商品已到库
        
        for (ScmPurchaseRequestItem item : items) {
            // 检查到货数量
            if (item.getArrivalQuantity() == null || item.getArrivalQuantity() < item.getPurchaseQuantity()) {
                allArrived = false;
            }
            if (item.getArrivalQuantity() != null && item.getArrivalQuantity() > 0) {
                partiallyArrived = true;
            }
            
            // 检查到库数量（验收数量）
            if (item.getAcceptQuantity() == null || item.getAcceptQuantity() < item.getPurchaseQuantity()) {
                allToWarehouse = false;
            }
            if (item.getAcceptQuantity() != null && item.getAcceptQuantity() > 0) {
                partiallyToWarehouse = true;
            }
        }
        
        // 更新采购状态
        String status;
        if (allToWarehouse) {
            status = "全部到库";
        } else if (partiallyToWarehouse) {
            status = "部分到库";
        } else if (allArrived) {
            status = "全部发货";
        } else if (partiallyArrived) {
            status = "部分发货";
        } else {
            // 检查是否已付款
            // 这里假设如果有支付时间或支付凭证，就认为已付款
            if (req.getPaymentTime() != null || (req.getPaymentVoucher() != null && !req.getPaymentVoucher().isEmpty())) {
                status = "采购付款";
            } else {
                status = "待采购";
            }
        }
        
        req.setPurchaseStatus(status);
        
        // 更新采购申请表
        this.updateById(req);
    }
}
