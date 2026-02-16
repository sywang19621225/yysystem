package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysConfig;
import com.yysystem.modules.system.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/system/config")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/list")
    public Result<Page<SysConfig>> list(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size) {
        Page<SysConfig> page = new Page<>(current, size);
        return Result.success(sysConfigService.page(page));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysConfig config) {
        sysConfigService.setValue(config.getConfigKey(), config.getConfigValue(), config.getConfigName());
        return Result.success(true);
    }

    @GetMapping("/deposit-payment-methods")
    public Result<List<String>> getDepositPaymentMethods() {
        String value = sysConfigService.getValue("general_settings");
        if (value == null || value.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> settings = mapper.readValue(value, new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            Map<String, Object> customCategories = (Map<String, Object>) settings.get("customCategories");
            if (customCategories != null) {
                @SuppressWarnings("unchecked")
                List<String> methods = (List<String>) customCategories.get("保证金支付方式");
                return Result.success(methods != null ? methods : Collections.emptyList());
            }
            return Result.success(Collections.emptyList());
        } catch (Exception e) {
            return Result.success(Collections.emptyList());
        }
    }

    @PostMapping("/ai-test")
    public Result<Boolean> testAIConnection(@RequestBody Map<String, Object> config) {
        try {
            Boolean enabled = (Boolean) config.get("enabled");
            if (enabled == null || !enabled) {
                return Result.error("AI功能未启用");
            }

            String apiKey = (String) config.get("apiKey");
            String baseUrl = (String) config.get("baseUrl");
            String model = (String) config.get("model");

            if (apiKey == null || apiKey.isEmpty()) {
                return Result.error("API Key不能为空");
            }
            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "https://api.siliconflow.cn/v1/chat/completions";
            }
            if (model == null || model.isEmpty()) {
                return Result.error("模型名称不能为空");
            }

            System.out.println("[AI测试] API 地址: " + baseUrl);
            System.out.println("[AI测试] 模型: " + model);

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            System.out.println("[AI测试] Authorization: Bearer " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");

            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 100);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", "你好");
            messages.add(msg);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("[AI测试] 发送请求到: " + baseUrl);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            System.out.println("[AI测试] 响应状态: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                return Result.success(true);
            } else {
                return Result.error("连接失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("[AI测试] 异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("连接失败: " + e.getMessage());
        }
    }
}
