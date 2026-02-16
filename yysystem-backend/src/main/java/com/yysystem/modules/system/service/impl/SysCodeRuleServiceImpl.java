package com.yysystem.modules.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysCodeRule;
import com.yysystem.modules.system.mapper.SysCodeRuleMapper;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SysCodeRuleServiceImpl extends ServiceImpl<SysCodeRuleMapper, SysCodeRule> implements SysCodeRuleService {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private com.yysystem.modules.system.service.SysConfigService sysConfigService;

    private static final String REDIS_KEY_PREFIX = "sys:code:seq:";

    @Override
    public String generateNextCode(String ruleCode) {
        SysCodeRule rule = this.getOne(new LambdaQueryWrapper<SysCodeRule>().eq(SysCodeRule::getCode, ruleCode));
        if (rule == null) {
            throw new RuntimeException("编号规则不存在: " + ruleCode);
        }

        // 简单实现：支持 {YYYYMMDD} 和 {SEQ:6} 格式
        // 规则示例: {YYYYMMDD}-{SEQ:6} -> 20231210-000001
        // 规则示例: C-{YYYYMM}-{SEQ:4} -> C-202312-0001

        String format = rule.getRule();
        String result = format;
        String dateKey = null;

        // 1. 处理日期
        if (result.contains("{YYYYMMDD}")) {
            String today = DateUtil.format(new Date(), "yyyyMMdd");
            result = result.replace("{YYYYMMDD}", today);
            dateKey = today;
        } else if (result.contains("{YYYYMM}")) {
            String month = DateUtil.format(new Date(), "yyyyMM");
            result = result.replace("{YYYYMM}", month);
            dateKey = month;
        } else if (result.contains("{YYYY}")) {
            String year = DateUtil.format(new Date(), "yyyy");
            result = result.replace("{YYYY}", year);
            dateKey = year;
        }

        // 2. 处理流水号 {SEQ:N}
        if (result.contains("{SEQ:")) {
            int start = result.indexOf("{SEQ:");
            int end = result.indexOf("}", start);
            String seqPart = result.substring(start, end + 1); // {SEQ:6}
            int length = Integer.parseInt(seqPart.substring(5, seqPart.length() - 1));

            long seq = getNextSequence(ruleCode, dateKey);
            String raw = String.valueOf(seq);
            String normalized = raw.length() > length ? raw.substring(raw.length() - length) : StrUtil.fillBefore(raw, '0', length);
            result = result.replace(seqPart, normalized);
        }

        return result;
    }

    private long getNextSequence(String ruleCode, String dateKey) {
        String key = "SEQ:" + ruleCode + (StrUtil.isNotBlank(dateKey) ? (":" + dateKey) : "");
        String val = sysConfigService.getValue(key);
        long next = 1L;
        if (StrUtil.isNotBlank(val)) {
            try {
                next = Long.parseLong(val) + 1L;
            } catch (Exception ignored) {}
        }
        sysConfigService.setValue(key, String.valueOf(next), "编号序列");
        return next;
    }
}
