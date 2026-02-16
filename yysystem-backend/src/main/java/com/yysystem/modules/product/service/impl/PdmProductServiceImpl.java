package com.yysystem.modules.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.mapper.PdmProductMapper;
import com.yysystem.modules.product.service.PdmProductService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PdmProductServiceImpl extends ServiceImpl<PdmProductMapper, PdmProduct> implements PdmProductService {

    private static final Logger log = LoggerFactory.getLogger(PdmProductServiceImpl.class);

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Override
    public boolean save(PdmProduct entity) {
        // 自动生成商品编号
        if (StrUtil.isBlank(entity.getProductCode())) {
            entity.setProductCode(sysCodeRuleService.generateNextCode("PRODUCT_CODE"));
        }
        
        // 自动填充创建人
        try {
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                String username = authentication.getName();
                log.info("Current user: {}", username);
                entity.setCreateBy(username);
            } else {
                log.warn("SecurityContext Authentication is null");
            }
        } catch (Exception e) {
            log.error("Failed to set createBy", e);
        }
        
        return super.save(entity);
    }
}
