package com.yysystem.modules.crm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.mapper.CrmCustomerMapper;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CrmCustomerServiceImpl extends ServiceImpl<CrmCustomerMapper, CrmCustomer> implements CrmCustomerService {

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    public boolean save(CrmCustomer entity) {
        if (StrUtil.isBlank(entity.getCustomerCode())) {
            entity.setCustomerCode(sysCodeRuleService.generateNextCode("CUSTOMER_CODE"));
        }
        try {
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                entity.setCreateBy(authentication.getName());
            }
        } catch (Exception ignored) {}
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(java.time.LocalDateTime.now());
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(java.time.LocalDateTime.now());
        }
        return super.save(entity);
    }

    @Override
    public void receive(Long id, Long userId) {
        CrmCustomer customer = new CrmCustomer();
        customer.setId(id);
        customer.setPrincipalId(userId);
        updateById(customer);
    }

    @Override
    public void assign(Long id, Long userId) {
        CrmCustomer customer = new CrmCustomer();
        customer.setId(id);
        customer.setPrincipalId(userId);
        updateById(customer);
    }

    @Override
    public void returnToPool(Long id) {
        update(null, new LambdaUpdateWrapper<CrmCustomer>()
                .set(CrmCustomer::getPrincipalId, null)
                .eq(CrmCustomer::getId, id));
    }
}
