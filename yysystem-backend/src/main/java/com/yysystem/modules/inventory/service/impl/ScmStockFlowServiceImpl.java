package com.yysystem.modules.inventory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.inventory.entity.ScmStockFlow;
import com.yysystem.modules.inventory.mapper.ScmStockFlowMapper;
import com.yysystem.modules.inventory.service.ScmStockFlowService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import cn.hutool.core.date.DateUtil;
import java.util.Date;

@Service
public class ScmStockFlowServiceImpl extends ServiceImpl<ScmStockFlowMapper, ScmStockFlow> implements ScmStockFlowService {

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    public void recordFlow(ScmStockFlow flow) {
        if (flow.getFlowNo() == null) {
            try {
                flow.setFlowNo(sysCodeRuleService.generateNextCode("STOCK_FLOW"));
            } catch (RuntimeException e) {
                String today = DateUtil.format(new Date(), "yyyyMMdd");
                String seq = String.format("%06d", (int)(System.currentTimeMillis() % 1000000));
                flow.setFlowNo("SF-" + today + "-" + seq);
            }
        }
        if (flow.getCreateTime() == null) {
            flow.setCreateTime(LocalDateTime.now());
        }
        this.save(flow);
    }
}
