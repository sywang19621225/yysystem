package com.yysystem.modules.system.controller;

import com.yysystem.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/common")
public class FileController {

    @Value("${file.upload-path:E:/development/yysystm/uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            // 使用原文件名，若重名则提示
            String fileName = originalFilename;
            File dest = new File(uploadPath + fileName);
            
            if (dest.exists()) {
                return Result.error("文件已存在，请重命名后上传");
            }
            
            file.transferTo(dest);
            
            // 返回访问路径
            return Result.success("/files/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
