package com.yysystem.modules.supplier.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.supplier.entity.ScmSupplier;
import com.yysystem.modules.supplier.mapper.ScmSupplierMapper;
import com.yysystem.modules.supplier.service.ScmSupplierService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScmSupplierServiceImpl extends ServiceImpl<ScmSupplierMapper, ScmSupplier> implements ScmSupplierService {

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    public boolean save(ScmSupplier entity) {
        String name = StrUtil.trimToEmpty(entity.getSupplierName());
        if (StrUtil.isNotBlank(name)) {
            long dup = this.count(new LambdaQueryWrapper<ScmSupplier>().eq(ScmSupplier::getSupplierName, name));
            if (dup > 0) {
                throw new IllegalArgumentException("供应商名称已存在");
            }
        }
        String code;
        do {
            String generated = sysCodeRuleService.generateNextCode("SUPPLIER_NO");
            String prefix = StrUtil.isNotBlank(generated) ? generated.substring(0, 1) : "G";
            String digits = StrUtil.isNotBlank(generated) ? generated.replaceAll("\\D", "") : "";
            String datePart = digits.length() >= 8 ? digits.substring(0, 8) : StrUtil.padPre(digits, 8, '0');
            String seqPart = digits.length() > 8 ? digits.substring(digits.length() - 4) : "0001";
            code = prefix + datePart + seqPart;
        } while (this.count(new LambdaQueryWrapper<ScmSupplier>().eq(ScmSupplier::getSupplierNo, code)) > 0);
        entity.setSupplierNo(code);
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        return super.save(entity);
    }
    
    @Override
    public boolean updateById(ScmSupplier entity) {
        String name = StrUtil.trimToEmpty(entity.getSupplierName());
        if (StrUtil.isNotBlank(name)) {
            long dup = this.count(new LambdaQueryWrapper<ScmSupplier>()
                    .eq(ScmSupplier::getSupplierName, name)
                    .ne(ScmSupplier::getId, entity.getId()));
            if (dup > 0) {
                throw new IllegalArgumentException("供应商名称已存在");
            }
        }
        return super.updateById(entity);
    }
}
