package com.yysystem.modules.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.purchase.entity.ScmPurchaseContract;
import com.yysystem.modules.purchase.entity.ScmPurchaseContractDetail;
import com.yysystem.modules.purchase.mapper.ScmPurchaseContractDetailMapper;
import com.yysystem.modules.purchase.service.ScmPurchaseContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scm/purchase-contract")
public class ScmPurchaseContractController {
    @Autowired
    private ScmPurchaseContractService contractService;
    @Autowired
    private ScmPurchaseContractDetailMapper detailMapper;
    @GetMapping("/list")
    public Result<Page<ScmPurchaseContract>> list(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String contractNo) {
        Page<ScmPurchaseContract> page = new Page<>(current, size);
        LambdaQueryWrapper<ScmPurchaseContract> wrapper = new LambdaQueryWrapper<>();
        if (contractNo != null && !contractNo.isEmpty()) {
            wrapper.like(ScmPurchaseContract::getContractNo, contractNo);
        }
        wrapper.orderByDesc(ScmPurchaseContract::getCreateTime);
        return Result.success(contractService.page(page, wrapper));
    }
    @GetMapping("/{id}")
    public Result<ScmPurchaseContract> getById(@PathVariable Long id) {
        ScmPurchaseContract c = contractService.getContractById(id);
        return Result.success(c);
    }
    @PostMapping
    public Result<Long> save(@RequestBody ScmPurchaseContract contract) {
        return Result.success(contractService.saveContract(contract));
    }
    @PutMapping
    public Result<Boolean> update(@RequestBody ScmPurchaseContract contract) {
        return Result.success(contractService.updateContract(contract));
    }
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        detailMapper.delete(new LambdaQueryWrapper<ScmPurchaseContractDetail>().eq(ScmPurchaseContractDetail::getContractId, id));
        return Result.success(contractService.removeById(id));
    }
    @PostMapping("/create-from-request/{requestId}")
    public Result<Long> createFromRequest(@PathVariable Long requestId) {
        return Result.success(contractService.createFromRequest(requestId));
    }
}

