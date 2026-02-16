package com.yysystem.modules.asm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.asm.entity.AsmRepairRecord;
import com.yysystem.modules.asm.entity.AsmWorkOrder;
import com.yysystem.modules.asm.mapper.AsmRepairRecordMapper;
import com.yysystem.modules.asm.service.AsmRepairRecordService;
import com.yysystem.modules.asm.service.AsmWorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AsmRepairRecordServiceImpl extends ServiceImpl<AsmRepairRecordMapper, AsmRepairRecord> implements AsmRepairRecordService {

    @Autowired
    private AsmWorkOrderService asmWorkOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRepairRecord(AsmRepairRecord record) {
        record.setCreateTime(LocalDateTime.now());
        
        // Calculate total cost
        BigDecimal repair = record.getRepairCost() != null ? record.getRepairCost() : BigDecimal.ZERO;
        BigDecimal material = record.getMaterialCost() != null ? record.getMaterialCost() : BigDecimal.ZERO;
        record.setTotalCost(repair.add(material));
        
        this.save(record);

        // Update work order status if needed
        AsmWorkOrder workOrder = asmWorkOrderService.getById(record.getWorkOrderId());
        if (workOrder != null && "PENDING".equals(workOrder.getStatus())) {
            workOrder.setStatus("PROCESSING");
            asmWorkOrderService.updateById(workOrder);
        }
    }
}
