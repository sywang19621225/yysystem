package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysConfig;

public interface SysConfigService extends IService<SysConfig> {
    String getValue(String key);
    void setValue(String key, String value, String configName);
}
