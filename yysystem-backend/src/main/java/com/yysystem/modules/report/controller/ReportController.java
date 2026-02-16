package com.yysystem.modules.report.controller;

import com.yysystem.common.result.Result;
import com.yysystem.modules.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/sales")
    public Result<List<Map<String, Object>>> getSalesReport(@RequestParam(defaultValue = "day") String type) {
        return Result.success(reportService.getSalesReport(type));
    }

    @GetMapping("/inventory")
    public Result<List<Map<String, Object>>> getInventoryReport() {
        return Result.success(reportService.getInventoryReport());
    }
}
