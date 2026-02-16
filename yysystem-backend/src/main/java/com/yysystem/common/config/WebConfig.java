package com.yysystem.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:E:/development/yysystm/uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
            uploadPath += "/";
        }
        // 统一使用配置的 uploadPath，不添加额外的 userDirUploads
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
