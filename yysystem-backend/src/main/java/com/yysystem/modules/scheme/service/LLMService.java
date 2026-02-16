package com.yysystem.modules.scheme.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yysystem.modules.system.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LLMService {

    @Autowired
    private SysConfigService sysConfigService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LLMService() {
        // 设置超时时间为120秒
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 连接超时30秒
        factory.setReadTimeout(120000);   // 读取超时120秒
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 获取AI配置
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAIConfig() {
        try {
            String configValue = sysConfigService.getValue("ai_model_config");
            if (configValue != null && !configValue.isEmpty()) {
                return objectMapper.readValue(configValue, new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("读取AI配置失败", e);
        }
        return null;
    }

    /**
     * 生成图书馆方案文本
     */
    public String generateSchemeText(String customerName, String requirements) {
        try {
            Map<String, Object> config = getAIConfig();
            
            // 如果没有配置或AI未启用，返回模拟数据
            if (config == null || !Boolean.TRUE.equals(config.get("enabled"))) {
                log.warn("AI功能未启用或配置不存在，返回模拟方案内容");
                return generateMockSolution(customerName, requirements);
            }

            String apiKey = firstNonBlank(
                    asString(getProviderConfig(config).get("apiKey")),
                    asString(config.get("apiKey"))
            );
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("AI API Key未配置，返回模拟方案内容");
                return generateMockSolution(customerName, requirements);
            }
            
            String prompt = buildPrompt(customerName, requirements);
            return callAIAPI(prompt, config);
        } catch (Exception e) {
            log.error("AI生成方案失败", e);
            return generateMockSolution(customerName, requirements);
        }
    }
    
    /**
     * 生成模拟方案内容（用于测试）
     */
    private String generateMockSolution(String customerName, String requirements) {
        String name = customerName != null ? customerName : "客户";
        return "# " + name + "图书馆整体规划方案\n\n" +
               "## 一、项目概述\n\n" +
               "根据客户需求：" + requirements + "\n\n" +
               "## 二、功能布局规划\n\n" +
               "### 2.1 借阅区\n" +
               "- 开放式书架设计，容量约5万册\n" +
               "- 配备自助借还设备4台\n" +
               "- 设置新书推荐专区\n\n" +
               "### 2.2 阅览区\n" +
               "- 提供舒适座椅200个\n" +
               "- 配备阅读灯和充电接口\n" +
               "- 静音设计，营造良好阅读环境\n\n" +
               "### 2.3 自习区\n" +
               "- 独立自习座位100个\n" +
               "- 配备台灯和电源插座\n" +
               "- 24小时开放区域规划\n\n" +
               "### 2.4 文创区\n" +
               "- 展示本地文化特色\n" +
               "- 设置咖啡休闲区\n" +
               "- 文创产品销售展示\n\n" +
               "## 三、家具配置建议\n\n" +
               "1. **书架**：采用钢制书架，高度2.2米，宽度1米\n" +
               "2. **阅览桌椅**：人体工学设计，木质桌面\n" +
               "3. **自习桌**：独立隔间设计，配备LED台灯\n" +
               "4. **休闲沙发**：布艺材质，舒适耐用\n\n" +
               "## 四、智能化建设方案\n\n" +
               "1. **自助借还系统**：RFID技术，支持24小时自助服务\n" +
               "2. **智能导航系统**：室内定位，快速找书\n" +
               "3. **数字化管理平台**：图书管理系统、读者管理系统\n" +
               "4. **电子资源平台**：电子书、期刊数据库访问\n\n" +
               "## 五、资源配置规划\n\n" +
               "- 纸质图书：初期配置3万册\n" +
               "- 电子资源：购买10个数据库\n" +
               "- 更新策略：每年新增图书5000册\n\n" +
               "---\n" +
               "*本方案由AI智能生成，仅供参考*";
    }

    /**
     * 构建Prompt
     */
    private String buildPrompt(String customerName, String requirements) {
        return "你是一名专业的图书馆整体规划方案撰写专家，请为" + customerName + "撰写一份图书馆整体规划方案。\n\n" +
               "客户核心需求：\n" +
               requirements + "\n\n" +
               "方案需包含以下模块：\n" +
               "1. 功能布局规划（借阅区、阅览区、自习区、文创区等）\n" +
               "2. 家具配置建议（人体工学、实用性与美观）\n" +
               "3. 智能化建设方案（自助借还、智能导航、数字化管理）\n" +
               "4. 资源配置规划（纸质与电子资源配比、更新策略）\n\n" +
               "要求：\n" +
               "- 语言专业、逻辑清晰、贴合实际场景\n" +
               "- 避免空话套话\n" +
               "- 2000字左右\n" +
               "- 使用Markdown格式输出";
    }

    /**
     * 调用AI API
     */
    private String callAIAPI(String prompt, Map<String, Object> config) throws Exception {
        Map<String, Object> providerConfig = getProviderConfig(config);
        String provider = asString(config.get("provider"));

        String apiKey = firstNonBlank(
                asString(providerConfig.get("apiKey")),
                asString(config.get("apiKey"))
        );
        String baseUrl = firstNonBlank(
                asString(providerConfig.get("baseUrl")),
                asString(config.get("baseUrl")),
                defaultBaseUrl(provider)
        );
        String model = firstNonBlank(
                asString(providerConfig.get("model")),
                asString(config.get("model")),
                defaultModel(provider)
        );
        Double temperature = config.get("temperature") != null ? ((Number) config.get("temperature")).doubleValue() : 0.7;
        Integer maxTokens = config.get("maxTokens") != null ? ((Number) config.get("maxTokens")).intValue() : 4096;
        String systemPrompt = (String) config.get("systemPrompt");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加系统提示词
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }
        
        // 添加用户提示词
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            return choices.get(0).path("message").path("content").asText();
        }

        return "AI生成失败，请手动编辑方案内容。";
    }

    private Map<String, Object> getProviderConfig(Map<String, Object> config) {
        if (config == null) {
            return Collections.emptyMap();
        }
        Object providersObj = config.get("providers");
        if (!(providersObj instanceof Map)) {
            return Collections.emptyMap();
        }
        String provider = asString(config.get("provider"));
        if (provider == null || provider.isEmpty()) {
            return Collections.emptyMap();
        }
        Object providerCfg = ((Map<?, ?>) providersObj).get(provider);
        if (!(providerCfg instanceof Map<?, ?>)) {
            return Collections.emptyMap();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) providerCfg;
        return result;
    }

    private String defaultBaseUrl(String provider) {
        if (provider == null) {
            return null;
        }
        return switch (provider) {
            case "siliconflow" -> "https://api.siliconflow.cn/v1/chat/completions";
            case "volcengine" -> "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
            case "openai" -> "https://api.openai.com/v1/chat/completions";
            case "azure" -> "https://{your-resource}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version=2023-05-15";
            case "baidu" -> "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";
            case "aliyun" -> "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
            case "zhipu" -> "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            default -> null;
        };
    }

    private String defaultModel(String provider) {
        if (provider == null) {
            return null;
        }
        return switch (provider) {
            case "siliconflow" -> "deepseek-ai/DeepSeek-V3.2";
            case "volcengine" -> "doubao-seed-2-0-mini-260215";
            case "openai" -> "gpt-3.5-turbo";
            case "azure" -> "gpt-35-turbo";
            case "baidu" -> "ernie-bot";
            case "aliyun" -> "qwen-turbo";
            case "zhipu" -> "glm-4";
            default -> null;
        };
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }
}
