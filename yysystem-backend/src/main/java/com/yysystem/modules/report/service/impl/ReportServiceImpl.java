package com.yysystem.modules.report.service.impl;

import cn.hutool.core.date.DateUtil;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.mapper.CrmContractMapper;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.mapper.PdmProductMapper;
import com.yysystem.modules.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private CrmContractMapper contractMapper;

    @Autowired
    private PdmProductMapper productMapper;

    @Override
    public List<Map<String, Object>> getSalesReport(String type) {
        List<CrmContract> contracts = contractMapper.selectList(null);
        Map<String, BigDecimal> dataMap = new HashMap<>();

        DateTimeFormatter formatter;
        if ("month".equals(type)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        for (CrmContract contract : contracts) {
            if (contract.getCreateTime() != null) {
                String key = contract.getCreateTime().format(formatter);
                BigDecimal amount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
                dataMap.merge(key, amount, BigDecimal::add);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        dataMap.forEach((date, amount) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("amount", amount);
            result.add(map);
        });
        
        // Sort by date
        result.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getInventoryReport() {
        List<PdmProduct> products = productMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PdmProduct product : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", product.getProductName());
            map.put("productCode", product.getProductCode());
            map.put("stockQuantity", product.getStockQuantity());
            map.put("costPrice", product.getCostPrice());
            
            BigDecimal stock = new BigDecimal(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            BigDecimal price = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
            map.put("totalValue", stock.multiply(price));
            
            result.add(map);
        }
        
        return result;
    }
}
