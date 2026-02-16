package com.yysystem.modules.outbound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.outbound.entity.ScmOutbound;

public interface ScmOutboundService extends IService<ScmOutbound> {

    Long createFromContract(Long contractId);

    boolean auditOutbound(Long id, String status, String detail);

    /**
     * 解锁出库单（将已审核状态改为待审核，允许删除）
     * @param id 出库单ID
     * @return 是否成功
     */
    boolean unlockOutbound(Long id);
}
