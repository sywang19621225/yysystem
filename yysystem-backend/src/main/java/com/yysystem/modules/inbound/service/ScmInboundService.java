package com.yysystem.modules.inbound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.inbound.entity.ScmInbound;
import com.yysystem.modules.inbound.entity.ScmInboundDetail;

import java.util.List;

public interface ScmInboundService extends IService<ScmInbound> {
    
    /**
     * 分页查询入库单列表（支持按类型、单号、供应商、状态过滤）
     */
    Page<ScmInbound> getInboundList(Page<ScmInbound> page, String inboundType, String inboundNo, String supplierName, String auditStatus);
    
    Long createFromPurchase(Long purchaseId);
    
    Long createFromPurchaseRequest(Long requestId);
    
    /**
     * 从采购申请表创建外转入库单
     * @param requestId 采购申请表ID
     * @return 入库单ID
     */
    Long createTransferFromPurchase(Long requestId);
    
    /**
     * 从发货单创建销售退货入库单
     * @param outboundId 发货单ID
     * @return 入库单ID
     */
    Long createFromSalesReturn(Long outboundId);
    
    /**
     * 创建外转入库单
     * @param inbound 入库单数据
     * @return 入库单ID
     */
    Long createTransferInbound(ScmInbound inbound);
    
    boolean auditInbound(Long id, String status, String detail);
    
    ScmInbound getInboundById(Long id);
    
    Long saveInbound(ScmInbound inbound);
    
    boolean updateInbound(ScmInbound inbound);
    
    List<ScmInboundDetail> getInboundDetails(Long id);
    
    Long addInboundDetail(ScmInboundDetail detail);
    
    boolean updateInboundDetail(ScmInboundDetail detail);
    
    boolean deleteInboundDetail(Long id);
}
