package com.yysystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysConfig;
import com.yysystem.modules.system.mapper.SysConfigMapper;
import com.yysystem.modules.system.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public String getValue(String key) {
        SysConfig config = this.getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void setValue(String key, String value, String configName) {
        SysConfig config = this.getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setCreateTime(LocalDateTime.now());
        }
        config.setConfigValue(value);
        if (configName != null) {
            config.setConfigName(configName);
        }
        this.saveOrUpdate(config);
    }
}
