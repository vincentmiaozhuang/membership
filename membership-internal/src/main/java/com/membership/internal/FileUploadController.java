package com.membership.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    private static final String UPLOAD_DIR = "uploads/";
    
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("开始上传文件，原始文件名: {}, 文件大小: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                logger.debug("创建上传目录: {}", UPLOAD_DIR);
                uploadDir.mkdirs();
            }
            
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            logger.debug("生成新文件名: {}", filename);
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());
            logger.debug("文件保存路径: {}", filePath);
            
            // 返回文件访问URL
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + filename);
            result.put("filename", filename);
            
            logger.info("文件上传成功，原始文件名: {}, 存储文件名: {}", originalFilename, filename);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("文件上传失败: " + e.getMessage());
        }
    }
}