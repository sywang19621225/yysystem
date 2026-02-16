package com.yysystem.modules.inventory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.inventory.entity.ScmInventoryCheck;
import com.yysystem.modules.inventory.entity.ScmInventoryCheckDetail;
import com.yysystem.modules.inventory.mapper.ScmInventoryCheckDetailMapper;
import com.yysystem.modules.inventory.mapper.ScmInventoryCheckMapper;
import com.yysystem.modules.inventory.service.ScmInventoryCheckService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScmInventoryCheckServiceImpl extends ServiceImpl<ScmInventoryCheckMapper, ScmInventoryCheck> implements ScmInventoryCheckService {

    @Autowired
    private ScmInventoryCheckDetailMapper scmInventoryCheckDetailMapper;

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCheck(ScmInventoryCheck check) {
        if (check.getCheckNo() == null) {
            check.setCheckNo(sysCodeRuleService.generateNextCode("INVENTORY_CHECK"));
        }
        if (check.getCreateTime() == null) {
            check.setCreateTime(LocalDateTime.now());
        }
        if (check.getStatus() == null) {
            check.setStatus("DRAFT");
        }
        
        this.save(check);
        
        if (CollUtil.isNotEmpty(check.getDetailList())) {
            for (ScmInventoryCheckDetail detail : check.getDetailList()) {
                detail.setCheckId(check.getId());
                scmInventoryCheckDetailMapper.insert(detail);
            }
        }
        
        return check.getId();
    }
    
    @Override
    public ScmInventoryCheck getById(java.io.Serializable id) {
        ScmInventoryCheck check = super.getById(id);
        if (check != null) {
            List<ScmInventoryCheckDetail> details = scmInventoryCheckDetailMapper.selectList(
                    new LambdaQueryWrapper<ScmInventoryCheckDetail>().eq(ScmInventoryCheckDetail::getCheckId, id)
            );
            check.setDetailList(details);
        }
        return check;
    }
}
