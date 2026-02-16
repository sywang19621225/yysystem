package com.yysystem.modules.asm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.asm.entity.AsmWorkOrder;
import com.yysystem.modules.asm.service.AsmWorkOrderService;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asm/work-order")
public class AsmWorkOrderController {

    @Autowired
    private AsmWorkOrderService asmWorkOrderService;


    @Autowired
    private CrmCustomerService crmCustomerService;

    @GetMapping("/list")
    public Result<Page<AsmWorkOrder>> list(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String workOrderNo) {
        Page<AsmWorkOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<AsmWorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (workOrderNo != null && !workOrderNo.isEmpty()) {
            wrapper.like(AsmWorkOrder::getWorkOrderNo, workOrderNo);
        }
        wrapper.orderByDesc(AsmWorkOrder::getCreateTime);
        
        Page<AsmWorkOrder> result = asmWorkOrderService.page(page, wrapper);
        
        // Fill customer name
        List<AsmWorkOrder> records = result.getRecords();
        for (AsmWorkOrder record : records) {
            if (record.getCustomerId() != null) {
                CrmCustomer customer = crmCustomerService.getById(record.getCustomerId());
                if (customer != null) {
                    record.setCustomerName(customer.getCustomerName());
                }
            }
        }
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<AsmWorkOrder> getById(@PathVariable Long id) {
        AsmWorkOrder workOrder = asmWorkOrderService.getById(id);
        // Remove old logic for separate repair records if not needed, or adapt
        return Result.success(workOrder);
    }


    @PostMapping
    public Result<String> create(@RequestBody AsmWorkOrder workOrder) {
        asmWorkOrderService.createWorkOrder(workOrder);
        return Result.success("创建成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody AsmWorkOrder workOrder) {
        asmWorkOrderService.updateById(workOrder);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        asmWorkOrderService.removeById(id);
        return Result.success("删除成功");
    }
}
