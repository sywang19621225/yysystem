package com.yysystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yysystem.modules.system.entity.ProductCostCalculation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductCostCalculationMapper extends BaseMapper<ProductCostCalculation> {

    @Select("<script>" +
            "SELECT c.id, c.product_id as productId, c.product_code as productCode, " +
            "c.product_name as productName, p.product_spec as productSpec, p.product_series as productSeries, " +
            "c.hardware_cost as hardwareCost, c.install_cost as installCost, " +
            "c.labor_cost as laborCost, c.test_cost as testCost, c.loss_cost as lossCost, " +
            "c.cert_cost as certCost, c.package_cost as packageCost, c.transport_cost as transportCost, " +
            "c.profit_margin as profitMargin, c.profit_amount as profitAmount, " +
            "c.total_cost as totalCost, c.suggested_price as suggestedPrice, c.channel_price as channelPrice, c.retail_price as retailPrice, " +
            "c.status, c.remark, c.create_by as createBy, c.create_time as createTime, " +
            "c.update_by as updateBy, c.update_time as updateTime " +
            "FROM sys_product_cost_calculation c " +
            "LEFT JOIN pdm_product p ON c.product_id = p.id " +
            "WHERE 1=1 " +
            "<if test='params.productCode != null and params.productCode != \"\"'>" +
            "AND c.product_code LIKE CONCAT('%', #{params.productCode}, '%') " +
            "</if>" +
            "<if test='params.productName != null and params.productName != \"\"'>" +
            "AND c.product_name LIKE CONCAT('%', #{params.productName}, '%') " +
            "</if>" +
            "<if test='params.status != null'>" +
            "AND c.status = #{params.status} " +
            "</if>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectCalculationList(@Param("params") Map<String, Object> params);
}
