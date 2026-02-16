package com.yysystem.modules.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmFollowRecord;
import com.yysystem.modules.crm.service.CrmFollowRecordService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import com.yysystem.modules.crm.entity.CrmContact;
import com.yysystem.modules.crm.service.CrmContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/follow-record")
public class CrmFollowRecordController {

    @Autowired
    private CrmFollowRecordService crmFollowRecordService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private CrmContactService crmContactService;

    @PostMapping
    @PreAuthorize("hasAuthority('PF:create')")
    public Result<Boolean> save(@RequestBody CrmFollowRecord crmFollowRecord) {
        return Result.success(crmFollowRecordService.save(crmFollowRecord));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PF:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmFollowRecord crmFollowRecord) {
        crmFollowRecord.setId(id);
        return Result.success(crmFollowRecordService.updateById(crmFollowRecord));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PF:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(crmFollowRecordService.removeById(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PF:get')")
    public Result<CrmFollowRecord> getById(@PathVariable Long id) {
        return Result.success(crmFollowRecordService.getById(id));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('PF:list')")
    public Result<Page<CrmFollowRecord>> list(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) Long customerId) {
        Page<CrmFollowRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmFollowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customerId != null, CrmFollowRecord::getCustomerId, customerId);
        wrapper.orderByDesc(CrmFollowRecord::getFollowTime);
        Page<CrmFollowRecord> result = crmFollowRecordService.page(page, wrapper);
        if (result != null && result.getRecords() != null) {
            for (CrmFollowRecord r : result.getRecords()) {
                if (r.getFollowerId() != null) {
                    SysUser u = sysUserService.getById(r.getFollowerId());
                    if (u != null) {
                        r.setFollowerName(nonEmpty(u.getName(), u.getUsername()));
                    }
                }
                if (r.getCreateBy() != null && (r.getFollowerName() == null || r.getFollowerName().isEmpty())) {
                    SysUser u2 = sysUserService.getByUsername(r.getCreateBy());
                    if (u2 != null) {
                        r.setCreatorName(nonEmpty(u2.getName(), u2.getUsername()));
                        if (r.getFollowerName() == null || r.getFollowerName().isEmpty()) {
                            r.setFollowerName(r.getCreatorName());
                        }
                    }
                }
                if (r.getContactId() != null) {
                    CrmContact c = crmContactService.getById(r.getContactId());
                    if (c != null) {
                        r.setContactName(c.getName());
                        r.setContactPhone(c.getPhone());
                    }
                }
                if ((r.getContactPhone() == null || r.getContactPhone().isEmpty()) && r.getPhone() != null) {
                    r.setContactPhone(r.getPhone());
                }
            }
        }
        return Result.success(result);
    }

    private String nonEmpty(String... arr) {
        if (arr == null) return null;
        for (String s : arr) {
            if (s != null && !s.trim().isEmpty()) return s;
        }
        return null;
    }
}
