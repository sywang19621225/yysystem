package com.yysystem.modules.scm.stock.mapper;

import com.yysystem.modules.scm.stock.entity.ScmStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScmStockMapper {
    
    /**
     * 查询库存信息
     */
    ScmStock getStock(@Param("productCode") String productCode, @Param("warehouseId") Long warehouseId);
    
    /**
     * 更新库存
     */
    int updateStock(@Param("productCode") String productCode, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);
    
    /**
     * 插入库存记录
     */
    int insertStock(ScmStock stock);
    
    /**
     * 批量更新库存
     */
    int batchUpdateStock(List<ScmStock> list);
    
    /**
     * 根据商品编码查询库存
     */
    List<ScmStock> getStockByProductCode(@Param("productCode") String productCode);
}
