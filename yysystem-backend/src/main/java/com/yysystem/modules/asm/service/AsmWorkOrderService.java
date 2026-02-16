package com.yysystem.modules.asm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.asm.entity.AsmWorkOrder;

public interface AsmWorkOrderService extends IService<AsmWorkOrder> {
    void createWorkOrder(AsmWorkOrder workOrder);
}
