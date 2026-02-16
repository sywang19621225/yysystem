package com.yysystem.modules.warehouse.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.warehouse.entity.PdmWarehouse;
import com.yysystem.modules.warehouse.mapper.PdmWarehouseMapper;
import com.yysystem.modules.warehouse.service.PdmWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PdmWarehouseServiceImpl extends ServiceImpl<PdmWarehouseMapper, PdmWarehouse> implements PdmWarehouseService {

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    public boolean save(PdmWarehouse entity) {
        if (StrUtil.isBlank(entity.getWarehouseNo())) {
            entity.setWarehouseNo(sysCodeRuleService.generateNextCode("WAREHOUSE_NO"));
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        return super.save(entity);
    }
}
