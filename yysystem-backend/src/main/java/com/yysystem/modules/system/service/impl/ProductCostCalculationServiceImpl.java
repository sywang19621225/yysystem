package com.yysystem.modules.system.service.impl;

import com.yysystem.modules.system.entity.ProductCostCalculation;
import com.yysystem.modules.system.entity.ProductCostCalculationDetail;
import com.yysystem.modules.system.mapper.ProductCostCalculationDetailMapper;
import com.yysystem.modules.system.mapper.ProductCostCalculationMapper;
import com.yysystem.modules.system.service.ProductCostCalculationService;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.mapper.PdmProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class ProductCostCalculationServiceImpl implements ProductCostCalculationService {

    @Autowired
    private ProductCostCalculationMapper calculationMapper;

    @Autowired
    private ProductCostCalculationDetailMapper detailMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Override
    public List<Map<String, Object>> getCalculationList(Map<String, Object> params) {
        return calculationMapper.selectCalculationList(params);
    }

    @Override
    public ProductCostCalculation getCalculationById(Long id) {
        return calculationMapper.selectById(id);
    }

    @Override
    public List<ProductCostCalculationDetail> getCalculationDetails(Long calculationId) {
        return detailMapper.selectByCalculationId(calculationId);
    }

    @Override
    @Transactional
    public void saveCalculation(ProductCostCalculation calculation, List<ProductCostCalculationDetail> details) {
        // 计算硬件成本
        BigDecimal hardwareCost = details.stream()
                .map(ProductCostCalculationDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        calculation.setHardwareCost(hardwareCost);

        // 计算总成本
        BigDecimal totalCost = hardwareCost
                .add(calculation.getInstallCost() != null ? calculation.getInstallCost() : BigDecimal.ZERO)
                .add(calculation.getLaborCost() != null ? calculation.getLaborCost() : BigDecimal.ZERO)
                .add(calculation.getTestCost() != null ? calculation.getTestCost() : BigDecimal.ZERO)
                .add(calculation.getLossCost() != null ? calculation.getLossCost() : BigDecimal.ZERO)
                .add(calculation.getCertCost() != null ? calculation.getCertCost() : BigDecimal.ZERO)
                .add(calculation.getPackageCost() != null ? calculation.getPackageCost() : BigDecimal.ZERO)
                .add(calculation.getTransportCost() != null ? calculation.getTransportCost() : BigDecimal.ZERO);
        calculation.setTotalCost(totalCost);

        // 计算毛利金额
        BigDecimal profitMargin = calculation.getProfitMargin() != null ? calculation.getProfitMargin() : BigDecimal.ZERO;
        BigDecimal profitAmount = totalCost.multiply(profitMargin).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        calculation.setProfitAmount(profitAmount);

        // 计算建议售价
        BigDecimal suggestedPrice = totalCost.add(profitAmount);
        calculation.setSuggestedPrice(suggestedPrice);

        // 保存主表
        calculationMapper.insert(calculation);

        // 保存明细表
        if (details != null && !details.isEmpty()) {
            for (int i = 0; i < details.size(); i++) {
                ProductCostCalculationDetail detail = details.get(i);
                detail.setCalculationId(calculation.getId());
                detail.setSortOrder(i);
                detailMapper.insert(detail);
            }
        }
    }

    @Override
    @Transactional
    public void updateCalculation(ProductCostCalculation calculation, List<ProductCostCalculationDetail> details) {
        // 计算硬件成本
        BigDecimal hardwareCost = details.stream()
                .map(ProductCostCalculationDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        calculation.setHardwareCost(hardwareCost);

        // 计算总成本
        BigDecimal totalCost = hardwareCost
                .add(calculation.getInstallCost() != null ? calculation.getInstallCost() : BigDecimal.ZERO)
                .add(calculation.getLaborCost() != null ? calculation.getLaborCost() : BigDecimal.ZERO)
                .add(calculation.getTestCost() != null ? calculation.getTestCost() : BigDecimal.ZERO)
                .add(calculation.getLossCost() != null ? calculation.getLossCost() : BigDecimal.ZERO)
                .add(calculation.getCertCost() != null ? calculation.getCertCost() : BigDecimal.ZERO)
                .add(calculation.getPackageCost() != null ? calculation.getPackageCost() : BigDecimal.ZERO)
                .add(calculation.getTransportCost() != null ? calculation.getTransportCost() : BigDecimal.ZERO);
        calculation.setTotalCost(totalCost);

        // 计算毛利金额
        BigDecimal profitMargin = calculation.getProfitMargin() != null ? calculation.getProfitMargin() : BigDecimal.ZERO;
        BigDecimal profitAmount = totalCost.multiply(profitMargin).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        calculation.setProfitAmount(profitAmount);

        // 计算建议售价
        BigDecimal suggestedPrice = totalCost.add(profitAmount);
        calculation.setSuggestedPrice(suggestedPrice);

        // 更新主表
        calculationMapper.updateById(calculation);

        // 删除旧明细
        detailMapper.deleteByCalculationId(calculation.getId());

        // 保存新明细
        if (details != null && !details.isEmpty()) {
            for (int i = 0; i < details.size(); i++) {
                ProductCostCalculationDetail detail = details.get(i);
                detail.setCalculationId(calculation.getId());
                detail.setSortOrder(i);
                detailMapper.insert(detail);
            }
        }
    }

    @Override
    @Transactional
    public void deleteCalculation(Long id) {
        // 删除明细
        detailMapper.deleteByCalculationId(id);
        // 删除主表
        calculationMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void applyCalculation(Long id) {
        ProductCostCalculation calculation = calculationMapper.selectById(id);
        if (calculation == null) {
            throw new RuntimeException("核算记录不存在");
        }

        // 更新商品价格
        PdmProduct product = productMapper.selectById(calculation.getProductId());
        if (product != null) {
            // 内部价格 -> 成本单价
            product.setCostPrice(calculation.getSuggestedPrice());
            // 渠道价格 -> 渠道单价
            product.setChannelPrice(calculation.getChannelPrice());
            // 零售价格 -> 终端单价
            product.setTerminalPrice(calculation.getRetailPrice());
            productMapper.updateById(product);
        }

        // 更新状态为已应用
        calculation.setStatus(2);
        calculationMapper.updateById(calculation);
    }
}
