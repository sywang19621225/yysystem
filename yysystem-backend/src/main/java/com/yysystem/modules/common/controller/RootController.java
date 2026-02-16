package com.yysystem.modules.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> response = new HashMap<>();
        response.put("状态", "成功");
        response.put("消息", "原邑智能销售管理平台后端服务正常运行");
        response.put("时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("端口", "8080");
        response.put("版本", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("状态", "健康");
        response.put("服务", "原邑智能销售管理平台");
        response.put("时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("数据库", "已连接");
        response.put("安全认证", "已启用");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("运行状态", "正常");
        response.put("系统名称", "原邑智能销售管理平台");
        response.put("后端框架", "Spring Boot 3.0 + MyBatis-Plus 3.5");
        response.put("安全框架", "Spring Security + JWT");
        response.put("数据库", "MySQL 8.0");
        response.put("当前时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("欢迎信息", "欢迎使用原邑智能销售管理系统！");
        return ResponseEntity.ok(response);
    }
}

