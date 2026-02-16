package com.yysystem.modules.asm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.asm.entity.AsmRepairRecord;

public interface AsmRepairRecordService extends IService<AsmRepairRecord> {
    void addRepairRecord(AsmRepairRecord record);
}
