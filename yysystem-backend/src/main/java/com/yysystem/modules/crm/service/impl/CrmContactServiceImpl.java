package com.yysystem.modules.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.crm.entity.CrmContact;
import com.yysystem.modules.crm.mapper.CrmContactMapper;
import com.yysystem.modules.crm.service.CrmContactService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CrmContactServiceImpl extends ServiceImpl<CrmContactMapper, CrmContact> implements CrmContactService {
    @Override
    public List<CrmContact> listByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<CrmContact>().eq(CrmContact::getCustomerId, customerId));
    }

    @Override
    public void setPrimary(Long id) {
        CrmContact c = this.getById(id);
        if (c == null) return;
        this.update(new LambdaUpdateWrapper<CrmContact>()
                .eq(CrmContact::getCustomerId, c.getCustomerId())
                .set(CrmContact::getIsPrimary, 0));
        c.setIsPrimary(1);
        this.updateById(c);
    }
    
    @Override
    public boolean save(CrmContact entity) {
        String name = entity.getName() == null ? "" : entity.getName().trim();
        String phone = entity.getPhone() == null ? "" : entity.getPhone().trim();
        String phoneNorm = phone.replaceAll("\\D+", "");
        Long customerId = entity.getCustomerId();
        if (customerId != null) {
            if (!name.isEmpty()) {
                java.util.List<CrmContact> sameNameList = this.list(new LambdaQueryWrapper<CrmContact>()
                        .eq(CrmContact::getCustomerId, customerId)
                        .eq(CrmContact::getName, name));
                boolean nameDupWithSamePhone = false;
                for (CrmContact c : sameNameList) {
                    String p = c.getPhone() == null ? "" : c.getPhone().trim();
                    String pn = p.replaceAll("\\D+", "");
                    if (!phone.isEmpty() && pn.equals(phoneNorm)) {
                        nameDupWithSamePhone = true;
                        break;
                    }
                }
                if (nameDupWithSamePhone) {
                    throw new IllegalArgumentException("同一客户下该联系人已存在");
                }
            }
            if (!phone.isEmpty()) {
                long dupPhone = this.count(new LambdaQueryWrapper<CrmContact>()
                        .eq(CrmContact::getCustomerId, customerId)
                        .eq(CrmContact::getPhone, phone));
                if (dupPhone > 0) {
                    throw new IllegalArgumentException("同一客户下该联系电话已存在");
                }
            }
        }
        return super.save(entity);
    }
    
    @Override
    public boolean updateById(CrmContact entity) {
        String name = entity.getName() == null ? "" : entity.getName().trim();
        String phone = entity.getPhone() == null ? "" : entity.getPhone().trim();
        String phoneNorm = phone.replaceAll("\\D+", "");
        Long customerId = entity.getCustomerId();
        if (customerId != null) {
            if (!name.isEmpty()) {
                java.util.List<CrmContact> sameNameList = this.list(new LambdaQueryWrapper<CrmContact>()
                        .eq(CrmContact::getCustomerId, customerId)
                        .eq(CrmContact::getName, name)
                        .ne(CrmContact::getId, entity.getId()));
                boolean nameDupWithSamePhone = false;
                for (CrmContact c : sameNameList) {
                    String p = c.getPhone() == null ? "" : c.getPhone().trim();
                    String pn = p.replaceAll("\\D+", "");
                    if (!phone.isEmpty() && pn.equals(phoneNorm)) {
                        nameDupWithSamePhone = true;
                        break;
                    }
                }
                if (nameDupWithSamePhone) {
                    throw new IllegalArgumentException("同一客户下该联系人已存在");
                }
            }
            if (!phone.isEmpty()) {
                long dupPhone = this.count(new LambdaQueryWrapper<CrmContact>()
                        .eq(CrmContact::getCustomerId, customerId)
                        .eq(CrmContact::getPhone, phone)
                        .ne(CrmContact::getId, entity.getId()));
                if (dupPhone > 0) {
                    throw new IllegalArgumentException("同一客户下该联系电话已存在");
                }
            }
        }
        return super.updateById(entity);
    }
}
