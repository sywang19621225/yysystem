package com.yysystem.modules.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.inventory.entity.ScmStockFlow;
import com.yysystem.modules.inventory.service.ScmStockFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scm/inventory/flow")
public class ScmStockFlowController {

    @Autowired
    private ScmStockFlowService scmStockFlowService;

    @GetMapping("/list")
    public Result<Page<ScmStockFlow>> list(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String productCode) {
        Page<ScmStockFlow> page = new Page<>(current, size);
        LambdaQueryWrapper<ScmStockFlow> wrapper = new LambdaQueryWrapper<>();
        if (productCode != null && !productCode.isEmpty()) {
            wrapper.eq(ScmStockFlow::getProductCode, productCode);
        }
        wrapper.orderByDesc(ScmStockFlow::getCreateTime);
        return Result.success(scmStockFlowService.page(page, wrapper));
    }
}
