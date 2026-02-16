package com.yysystem.modules.asm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.asm.entity.AsmWorkOrder;
import com.yysystem.modules.asm.mapper.AsmWorkOrderMapper;
import com.yysystem.modules.asm.service.AsmWorkOrderService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AsmWorkOrderServiceImpl extends ServiceImpl<AsmWorkOrderMapper, AsmWorkOrder> implements AsmWorkOrderService {

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createWorkOrder(AsmWorkOrder workOrder) {
        String workOrderNo = sysCodeRuleService.generateNextCode("WORK_ORDER");
        workOrder.setWorkOrderNo(workOrderNo);
        workOrder.setCreateTime(LocalDateTime.now());
        if (workOrder.getStatus() == null) {
            workOrder.setStatus("PENDING");
        }
        this.save(workOrder);
    }
}
