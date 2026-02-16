package com.yysystem.modules.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.inventory.entity.ScmStockFlow;

public interface ScmStockFlowService extends IService<ScmStockFlow> {
    void recordFlow(ScmStockFlow flow);
}
