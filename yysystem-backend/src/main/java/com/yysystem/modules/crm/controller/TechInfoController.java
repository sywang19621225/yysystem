package com.yysystem.modules.crm.controller;

import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomerTechInfo;
import com.yysystem.modules.crm.service.CrmCustomerTechInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/crm")
public class TechInfoController {

    @Autowired
    private CrmCustomerTechInfoService techInfoService;

    @GetMapping("/customer/{customerId}/tech-info")
    public Result<List<CrmCustomerTechInfo>> list(@PathVariable Long customerId) {
        return Result.success(techInfoService.listByCustomerId(customerId));
    }

    @PostMapping(value = "/customer/{customerId}/tech-info", consumes = "application/json")
    public Result<CrmCustomerTechInfo> create(@PathVariable Long customerId, @RequestBody CrmCustomerTechInfo info) {
        info.setCustomerId(customerId);
        techInfoService.save(info);
        return Result.success(info);
    }

    @PutMapping(value = "/tech-info/{id}", consumes = "application/json")
    public Result<CrmCustomerTechInfo> update(@PathVariable Long id, @RequestBody CrmCustomerTechInfo info) {
        info.setId(id);
        techInfoService.updateById(info);
        return Result.success(info);
    }

    @DeleteMapping("/tech-info/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(techInfoService.removeById(id));
    }
}
