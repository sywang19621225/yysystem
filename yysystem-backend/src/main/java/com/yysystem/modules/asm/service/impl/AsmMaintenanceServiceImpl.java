package com.yysystem.modules.asm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.asm.entity.AsmMaintenance;
import com.yysystem.modules.asm.mapper.AsmMaintenanceMapper;
import com.yysystem.modules.asm.service.AsmMaintenanceService;
import org.springframework.stereotype.Service;

@Service
public class AsmMaintenanceServiceImpl extends ServiceImpl<AsmMaintenanceMapper, AsmMaintenance> implements AsmMaintenanceService {
}
