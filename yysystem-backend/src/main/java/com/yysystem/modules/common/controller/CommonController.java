package com.yysystem.modules.common.controller;

import com.yysystem.common.result.Result;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
public class CommonController {

    @Value("${file.upload-path:E:/development/yysystm/uploads/}")
    private String uploadPath;

    // 兼容旧的上传路径 /api/file/upload
    @PostMapping(value = "/api/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadFile(@org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file,
                                      @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authHeader) {
        return uploadInternal(file, authHeader);
    }

    @PostMapping(value = "/api/common/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file,
                                  @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authHeader) {
        return uploadInternal(file, authHeader);
    }

    private Result<String> uploadInternal(MultipartFile file, String authHeader) {
        System.out.println("[上传] 收到上传请求");
        System.out.println("[上传] Authorization头: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
        
        // 检查是否已登录
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未登录或token无效");
        }
        
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("文件为空");
            }
            if (file.getSize() > 500L * 1024 * 1024) {
                return Result.error("文件大小超过500MB");
            }
            
            // 获取文件名和扩展名
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String baseName = (original == null ? "file" : original.substring(0, original.lastIndexOf(".")));
            String safeBase = baseName.replaceAll("[\\r\\n\\\\/]+", "_");
            
            // 按日期分文件夹存储，格式：uploads/2026/02/16/
            LocalDate today = LocalDate.now();
            String datePath = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成文件名：原文件名_时间戳.扩展名
            String ts = String.valueOf(System.currentTimeMillis());
            String fileName = safeBase + "_" + ts + ext;
            Path target = uploadDir.resolve(fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // 返回访问路径，包含日期文件夹
            String url = "/files/" + datePath + "/" + fileName;
            System.out.println("[上传] 文件保存成功: " + url);
            return Result.success(url);
        } catch (Exception e) {
            System.err.println("[上传] 失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/api/common/file/{*filePath}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getFile(@org.springframework.web.bind.annotation.PathVariable String filePath) {
        try {
            // 处理带日期路径的文件，如：2026/02/16/filename.jpg
            Path target = Paths.get(uploadPath, filePath);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(target.toUri());
            if (!resource.exists()) {
                // 兼容旧文件（直接在 uploads 目录下）
                Path oldTarget = Paths.get(uploadPath, filePath.substring(filePath.lastIndexOf('/') + 1));
                resource = new org.springframework.core.io.UrlResource(oldTarget.toUri());
                if (!resource.exists()) {
                    return org.springframework.http.ResponseEntity.notFound().build();
                }
                target = oldTarget;
            }
            
            String contentType = Files.probeContentType(target);
            String fileName = target.getFileName().toString();
            if (contentType == null) {
                if (fileName.toLowerCase().endsWith(".pdf")) {
                    contentType = org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
                } else {
                    contentType = org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
                }
            }
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("[文件访问] 失败: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }
}
