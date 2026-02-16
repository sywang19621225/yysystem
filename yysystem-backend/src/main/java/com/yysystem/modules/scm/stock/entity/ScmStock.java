package com.yysystem.modules.scm.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存实体类
 */
@Data
@TableName("scm_stock")
public class ScmStock {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商品编码
     */
    private String productCode;
    
    /**
     * 仓库ID
     */
    private Long warehouseId;
    
    /**
     * 库存数量
     */
    private Integer stockQuantity;
    
    /**
     * 可用库存
     */
    private Integer availableStock;
    
    /**
     * 总库存
     */
    private Integer totalStock;
    
    /**
     * 成本价
     */
    private BigDecimal costPrice;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
