package com.yysystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yysystem.modules.system.entity.ProductCostCalculationDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductCostCalculationDetailMapper extends BaseMapper<ProductCostCalculationDetail> {

    @Select("SELECT * FROM sys_product_cost_calculation_detail WHERE calculation_id = #{calculationId} ORDER BY sort_order")
    List<ProductCostCalculationDetail> selectByCalculationId(@Param("calculationId") Long calculationId);

    @Select("DELETE FROM sys_product_cost_calculation_detail WHERE calculation_id = #{calculationId}")
    void deleteByCalculationId(@Param("calculationId") Long calculationId);
}
