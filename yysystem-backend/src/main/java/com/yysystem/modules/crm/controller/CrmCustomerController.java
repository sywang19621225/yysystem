package com.yysystem.modules.crm.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/customer")
public class CrmCustomerController {

    @Autowired
    private CrmCustomerService crmCustomerService;
    @Autowired
    private SysUserService sysUserService;

    @PostMapping
    @PreAuthorize("hasAuthority('CM:create')")
    public Result<Boolean> save(@RequestBody CrmCustomer crmCustomer) {
        if (StrUtil.isNotBlank(crmCustomer.getCustomerName())) {
            long dup = crmCustomerService.count(new LambdaQueryWrapper<CrmCustomer>()
                    .eq(CrmCustomer::getCustomerName, crmCustomer.getCustomerName()));
            if (dup > 0) {
                return Result.error("客户名称已存在，请更换名称");
            }
        }
        try {
            return Result.success(crmCustomerService.save(crmCustomer));
        } catch (DataIntegrityViolationException e) {
            return Result.error("公司简介内容过长，请精简后再试");
        } catch (Exception e) {
            return Result.error("客户信息保存失败，请稍后重试");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CM:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmCustomer crmCustomer) {
        crmCustomer.setId(id);
        if (StrUtil.isNotBlank(crmCustomer.getCustomerName())) {
            long dup = crmCustomerService.count(new LambdaQueryWrapper<CrmCustomer>()
                    .eq(CrmCustomer::getCustomerName, crmCustomer.getCustomerName())
                    .ne(CrmCustomer::getId, id));
            if (dup > 0) {
                return Result.error("客户名称已存在，请更换名称");
            }
        }
        try {
            crmCustomer.setUpdateTime(java.time.LocalDateTime.now());
            return Result.success(crmCustomerService.updateById(crmCustomer));
        } catch (DataIntegrityViolationException e) {
            return Result.error("公司简介内容过长，请精简后再试");
        } catch (Exception e) {
            return Result.error("客户信息保存失败，请稍后重试");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CM:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(crmCustomerService.removeById(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CM:get')")
    public Result<CrmCustomer> getById(@PathVariable Long id) {
        return Result.success(crmCustomerService.getById(id));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('CM:list')")
    public Result<Page<CrmCustomer>> list(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String customerName) {
        Page<CrmCustomer> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(customerName), CrmCustomer::getCustomerName, customerName);
        wrapper.orderByDesc(CrmCustomer::getCreateTime).orderByDesc(CrmCustomer::getUpdateTime);
        Page<CrmCustomer> result = crmCustomerService.page(page, wrapper);
        java.util.List<CrmCustomer> list = result.getRecords();
        if (list != null && !list.isEmpty()) {
            for (CrmCustomer c : list) {
                if (c.getPrincipalId() != null) {
                    com.yysystem.modules.system.entity.SysUser u = sysUserService.getById(c.getPrincipalId());
                    if (u != null) c.setPrincipalName(u.getName());
                }
            }
        }
        return Result.success(result);
    }

    @GetMapping("/public/list")
    @PreAuthorize("hasAuthority('CM:list')")
    public Result<Page<CrmCustomer>> listPublic(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String customerName) {
        Page<CrmCustomer> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(CrmCustomer::getPrincipalId); // 公海
        wrapper.like(StrUtil.isNotBlank(customerName), CrmCustomer::getCustomerName, customerName);
        wrapper.orderByDesc(CrmCustomer::getCreateTime).orderByDesc(CrmCustomer::getUpdateTime);
        return Result.success(crmCustomerService.page(page, wrapper));
    }

    @PostMapping("/receive/{id}")
    @PreAuthorize("hasAuthority('CM:update')")
    public Result<Boolean> receive(@PathVariable Long id) {
        // TODO: 获取当前用户ID
        Long userId = 1L; // 模拟
        crmCustomerService.receive(id, userId);
        return Result.success(true);
    }

    @PostMapping("/assign/{id}")
    @PreAuthorize("hasAuthority('CM:update')")
    public Result<Boolean> assign(@PathVariable Long id, @RequestParam Long userId) {
        crmCustomerService.assign(id, userId);
        return Result.success(true);
    }

    @PostMapping("/return/{id}")
    @PreAuthorize("hasAuthority('CM:update')")
    public Result<Boolean> returnToPool(@PathVariable Long id) {
        crmCustomerService.returnToPool(id);
        return Result.success(true);
    }
}
