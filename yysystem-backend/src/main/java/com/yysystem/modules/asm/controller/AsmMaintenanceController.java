package com.yysystem.modules.asm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.asm.entity.AsmMaintenance;
import com.yysystem.modules.asm.service.AsmMaintenanceService;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/asm/maintenance")
public class AsmMaintenanceController {

    @Autowired
    private AsmMaintenanceService asmMaintenanceService;
    @Autowired
    private SysCodeRuleService sysCodeRuleService;
    @Autowired
    private CrmCustomerService crmCustomerService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('EM:list')")
    public Result<Page<AsmMaintenance>> list(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String maintenanceNo) {
        Page<AsmMaintenance> page = new Page<>(current, size);
        LambdaQueryWrapper<AsmMaintenance> wrapper = new LambdaQueryWrapper<>();
        if (maintenanceNo != null && !maintenanceNo.isEmpty()) {
            wrapper.like(AsmMaintenance::getMaintenanceNo, maintenanceNo);
        }
        wrapper.orderByDesc(AsmMaintenance::getCreateTime);
        
        Page<AsmMaintenance> result = asmMaintenanceService.page(page, wrapper);
        
        // Fill customer name
        List<AsmMaintenance> records = result.getRecords();
        for (AsmMaintenance record : records) {
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
    @PreAuthorize("hasAuthority('EM:get')")
    public Result<AsmMaintenance> getById(@PathVariable Long id) {
        return Result.success(asmMaintenanceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EM:create')")
    public Result<Boolean> save(@RequestBody AsmMaintenance asmMaintenance) {
        if (asmMaintenance.getMaintenanceNo() == null || asmMaintenance.getMaintenanceNo().isEmpty()) {
            asmMaintenance.setMaintenanceNo(sysCodeRuleService.generateNextCode("MAINTENANCE"));
        }
        asmMaintenance.setCreateTime(LocalDateTime.now());
        if (asmMaintenance.getStatus() == null) {
            asmMaintenance.setStatus(1);
        }
        return Result.success(asmMaintenanceService.save(asmMaintenance));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('EM:update')")
    public Result<Boolean> update(@RequestBody AsmMaintenance asmMaintenance) {
        return Result.success(asmMaintenanceService.updateById(asmMaintenance));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EM:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(asmMaintenanceService.removeById(id));
    }
}
