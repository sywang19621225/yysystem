package com.yysystem.modules.system.service;

import com.yysystem.modules.system.entity.ProductCostCalculation;
import com.yysystem.modules.system.entity.ProductCostCalculationDetail;

import java.util.List;
import java.util.Map;

public interface ProductCostCalculationService {

    List<Map<String, Object>> getCalculationList(Map<String, Object> params);

    ProductCostCalculation getCalculationById(Long id);

    List<ProductCostCalculationDetail> getCalculationDetails(Long calculationId);

    void saveCalculation(ProductCostCalculation calculation, List<ProductCostCalculationDetail> details);

    void updateCalculation(ProductCostCalculation calculation, List<ProductCostCalculationDetail> details);

    void deleteCalculation(Long id);

    void applyCalculation(Long id);
}
