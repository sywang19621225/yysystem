package com.yysystem.modules.crm.controller;

import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmContact;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmContactService;
import com.yysystem.modules.crm.service.CrmCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crm/contact")
public class CrmContactController {

    @Autowired
    private CrmContactService contactService;

    @Autowired
    private CrmCustomerService customerService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('CC:list')")
    public Result<List<CrmContact>> list(@RequestParam Long customerId) {
        List<CrmContact> list = contactService.listByCustomerId(customerId);
        for (CrmContact c : list) {
            if (c.getExtendFields() != null) {
                try {
                    // simple parse for {"gender":1}
                    String ef = c.getExtendFields();
                    int idx = ef.indexOf("\"gender\"");
                    if (idx >= 0) {
                        int colon = ef.indexOf(":", idx);
                        if (colon > 0) {
                            String rest = ef.substring(colon + 1).trim();
                            String num = rest.replaceAll("[^0-9]", "");
                            if (!num.isEmpty()) {
                                c.setGender(Integer.parseInt(num));
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return Result.success(list);
    }
    
    @GetMapping("/list-all")
    @PreAuthorize("hasAuthority('CC:list')")
    public Result<List<CrmContact>> listAll() {
        List<CrmContact> list = contactService.list();
        for (CrmContact c : list) {
            if (c.getExtendFields() != null) {
                try {
                    String ef = c.getExtendFields();
                    int idx = ef.indexOf("\"gender\"");
                    if (idx >= 0) {
                        int colon = ef.indexOf(":", idx);
                        if (colon > 0) {
                            String rest = ef.substring(colon + 1).trim();
                            String num = rest.replaceAll("[^0-9]", "");
                            if (!num.isEmpty()) {
                                c.setGender(Integer.parseInt(num));
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return Result.success(list);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CC:create')")
    public Result<Boolean> save(@RequestBody CrmContact contact) {
        if (contact.getGender() != null) {
            contact.setExtendFields("{\"gender\":" + contact.getGender() + "}");
        }
        return Result.success(contactService.save(contact));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CC:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmContact contact) {
        contact.setId(id);
        if (contact.getGender() != null) {
            contact.setExtendFields("{\"gender\":" + contact.getGender() + "}");
        }
        return Result.success(contactService.updateById(contact));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CC:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(contactService.removeById(id));
    }

    @PostMapping("/set-primary/{id}")
    @PreAuthorize("hasAuthority('CC:update')")
    public Result<Boolean> setPrimary(@PathVariable Long id) {
        contactService.setPrimary(id);
        return Result.success(true);
    }

    @GetMapping("/list-by-condition")
    @PreAuthorize("hasAuthority('CC:list')")
    public Result<List<CrmContact>> listByCondition(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String customerName) {
        
        List<CrmContact> allContacts = contactService.list();
        
        // 先根据客户名称筛选客户ID
        List<Long> matchedCustomerIds = null;
        if (customerName != null && !customerName.isEmpty()) {
            List<CrmCustomer> customers = customerService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CrmCustomer>()
                            .like("customer_name", customerName));
            matchedCustomerIds = customers.stream()
                    .map(CrmCustomer::getId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            if (matchedCustomerIds.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
        }
        
        // 筛选联系人
        final List<Long> finalMatchedCustomerIds = matchedCustomerIds;
        List<CrmContact> filtered = allContacts.stream().filter(c -> {
            // 按姓名筛选
            if (name != null && !name.isEmpty()) {
                if (c.getName() == null || !c.getName().contains(name)) {
                    return false;
                }
            }
            // 按手机号筛选
            if (phone != null && !phone.isEmpty()) {
                if (c.getPhone() == null || !c.getPhone().contains(phone)) {
                    return false;
                }
            }
            // 按客户名称筛选
            if (finalMatchedCustomerIds != null) {
                if (c.getCustomerId() == null || !finalMatchedCustomerIds.contains(c.getCustomerId())) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
        
        // 处理 extendFields 中的 gender
        for (CrmContact c : filtered) {
            if (c.getExtendFields() != null) {
                try {
                    String ef = c.getExtendFields();
                    int idx = ef.indexOf("\"gender\"");
                    if (idx >= 0) {
                        int colon = ef.indexOf(":", idx);
                        if (colon > 0) {
                            String rest = ef.substring(colon + 1).trim();
                            String num = rest.replaceAll("[^0-9]", "");
                            if (!num.isEmpty()) {
                                c.setGender(Integer.parseInt(num));
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        
        return Result.success(filtered);
    }
}
