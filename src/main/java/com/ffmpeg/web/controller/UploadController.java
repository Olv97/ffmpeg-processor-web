package com.ffmpeg.web.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.ffmpeg.web.config.AliyunConfig;
import com.ffmpeg.web.model.ApiResponse;
import com.ffmpeg.web.model.UploadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    
    @Autowired
    private OSS ossClient;
    
    @Autowired
    private AliyunConfig aliyunConfig;

    /**
     * 上传文件到OSS
     * 
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping
    public ApiResponse<UploadResult> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error(400, "文件为空");
            }
            
            // 获取配置的存储桶名称
            String bucketName = aliyunConfig.getOssBucketName();
            
            // 生成唯一的文件名 (时间戳 + UUID + 原始文件名)
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
            String objectKey = "uploads/" + System.currentTimeMillis() + "_" + uuid + "_" + originalFilename;
            
            // 上传文件到OSS
            PutObjectResult result = ossClient.putObject(bucketName, objectKey, file.getInputStream());
            
            logger.info("文件上传成功: bucketName={}, objectKey={}, ETag={}", bucketName, objectKey, result.getETag());
            
            // 返回上传结果
            UploadResult uploadResult = new UploadResult(bucketName, objectKey, originalFilename);
            return ApiResponse.success(uploadResult);
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("文件上传发生未知错误: {}", e.getMessage(), e);
            return ApiResponse.error(500, "文件上传发生未知错误: " + e.getMessage());
        }
    }
}
