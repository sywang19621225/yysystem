package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysCodeRule;

public interface SysCodeRuleService extends IService<SysCodeRule> {
    
    /**
     * 根据规则编码生成下一个编号
     * @param ruleCode 规则编码 (如 CUSTOMER_CODE)
     * @return 生成的编号 (如 C-20231210-000001)
     */
    String generateNextCode(String ruleCode);
}
