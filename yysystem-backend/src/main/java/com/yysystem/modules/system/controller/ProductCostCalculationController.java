package com.yysystem.modules.system.controller;

import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.ProductCostCalculation;
import com.yysystem.modules.system.entity.ProductCostCalculationDetail;
import com.yysystem.modules.system.service.ProductCostCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/cost-calculation")
public class ProductCostCalculationController {

    @Autowired
    private ProductCostCalculationService calculationService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("productCode", productCode);
        params.put("productName", productName);
        params.put("status", status);
        return Result.success(calculationService.getCalculationList(params));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ProductCostCalculation calculation = calculationService.getCalculationById(id);
        if (calculation == null) {
            return Result.error("核算记录不存在");
        }
        List<ProductCostCalculationDetail> details = calculationService.getCalculationDetails(id);
        Map<String, Object> result = new HashMap<>();
        result.put("calculation", calculation);
        result.put("details", details);
        return Result.success(result);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody Map<String, Object> request) {
        ProductCostCalculation calculation = parseCalculation(request);
        List<ProductCostCalculationDetail> details = parseDetails(request.get("details"));
        calculationService.saveCalculation(calculation, details);
        return Result.success();
    }

    @PostMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        ProductCostCalculation calculation = parseCalculation(request);
        calculation.setId(id);
        List<ProductCostCalculationDetail> details = parseDetails(request.get("details"));
        calculationService.updateCalculation(calculation, details);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        calculationService.deleteCalculation(id);
        return Result.success();
    }

    @PostMapping("/apply/{id}")
    public Result<Void> apply(@PathVariable Long id) {
        calculationService.applyCalculation(id);
        return Result.success();
    }

    private ProductCostCalculation parseCalculation(Map<String, Object> request) {
        ProductCostCalculation calculation = new ProductCostCalculation();
        calculation.setProductId(Long.valueOf(request.get("productId").toString()));
        calculation.setProductCode((String) request.get("productCode"));
        calculation.setProductName((String) request.get("productName"));
        calculation.setInstallCost(parseBigDecimal(request.get("installCost")));
        calculation.setLaborCost(parseBigDecimal(request.get("laborCost")));
        calculation.setTestCost(parseBigDecimal(request.get("testCost")));
        calculation.setLossCost(parseBigDecimal(request.get("lossCost")));
        calculation.setCertCost(parseBigDecimal(request.get("certCost")));
        calculation.setPackageCost(parseBigDecimal(request.get("packageCost")));
        calculation.setTransportCost(parseBigDecimal(request.get("transportCost")));
        calculation.setProfitMargin(parseBigDecimal(request.get("profitMargin")));
        calculation.setSuggestedPrice(parseBigDecimal(request.get("suggestedPrice")));
        calculation.setChannelPrice(parseBigDecimal(request.get("channelPrice")));
        calculation.setRetailPrice(parseBigDecimal(request.get("retailPrice")));
        calculation.setRemark((String) request.get("remark"));
        calculation.setStatus(0); // 默认草稿状态
        return calculation;
    }

    @SuppressWarnings("unchecked")
    private List<ProductCostCalculationDetail> parseDetails(Object detailsObj) {
        if (detailsObj == null) {
            return null;
        }
        List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
        return detailsList.stream().map(item -> {
            ProductCostCalculationDetail detail = new ProductCostCalculationDetail();
            if (item.get("itemId") != null) {
                detail.setItemId(Long.valueOf(item.get("itemId").toString()));
            }
            detail.setItemType(Integer.valueOf(item.get("itemType").toString()));
            detail.setItemCode((String) item.get("itemCode"));
            detail.setItemName((String) item.get("itemName"));
            detail.setItemSpec((String) item.get("itemSpec"));
            detail.setItemUnit((String) item.get("itemUnit"));
            detail.setUnitPrice(parseBigDecimal(item.get("unitPrice")));
            detail.setQuantity(parseBigDecimal(item.get("quantity")));
            detail.setSubtotal(parseBigDecimal(item.get("subtotal")));
            detail.setRemark((String) item.get("remark"));
            return detail;
        }).collect(java.util.stream.Collectors.toList());
    }

    private java.math.BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return java.math.BigDecimal.ZERO;
        }
        if (value instanceof java.math.BigDecimal) {
            return (java.math.BigDecimal) value;
        }
        return new java.math.BigDecimal(value.toString());
    }
}
