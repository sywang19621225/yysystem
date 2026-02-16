package com.yysystem.modules.report.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
    List<Map<String, Object>> getSalesReport(String type);
    List<Map<String, Object>> getInventoryReport();
}
