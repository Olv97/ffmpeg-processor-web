package com.ffmpeg.web.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.ffmpeg.web.config.AliyunConfig;
import com.ffmpeg.web.model.ApiResponse;
import com.ffmpeg.web.model.GetSpritesRequest;
import com.ffmpeg.web.model.SpriteResponse;
import com.ffmpeg.web.service.FunctionComputeService;



/**
 * 雪碧图生成控制器
 */
@RestController
@RequestMapping("/api/sprite")
public class SpriteController {

    private static final Logger logger = LoggerFactory.getLogger(SpriteController.class);

    @Autowired
    private FunctionComputeService functionComputeService;
    
    @Autowired
    private OSS ossClient;
    
    @Autowired
    private AliyunConfig aliyunConfig;

    /**
     * 生成雪碧图
     * @param requestMap 雪碧图生成请求参数
     * @return 雪碧图生成结果
     */
    @PostMapping("/generate")
    public ApiResponse<SpriteResponse> generateSprites(@RequestBody Map<String, Object> requestMap) {
        logger.info("接收到雪碧图生成请求: {}", JSON.toJSONString(requestMap));

        try {
            // 2. 构建Sprites请求
            GetSpritesRequest spritesRequest = new GetSpritesRequest();
            spritesRequest.setBucketName(requestMap.get("bucket_name").toString());
            spritesRequest.setObjectKey(requestMap.get("object_key").toString());
            
            logger.info("设置桶名和对象键: {} / {}", spritesRequest.getBucketName(), spritesRequest.getObjectKey());
            
            // 设置output_dir参数（前端传递或使用默认值）
            String outputDir = "sprites/";
            if (requestMap.containsKey("output_dir") && requestMap.get("output_dir") != null) {
                outputDir = requestMap.get("output_dir").toString();
            }
            spritesRequest.setOutputDir(outputDir);
            logger.info("设置输出目录: {}", spritesRequest.getOutputDir());
            
            // 4. 设置布局格式，如果前端提供则使用前端传递的，否则使用默认值
            if (requestMap.containsKey("tile")) {
                spritesRequest.setTile(requestMap.get("tile").toString());
                logger.info("使用前端传递的tile参数: {}", spritesRequest.getTile());
            } else {
                // 如果没有提供tile，尝试从columns参数构建
                if (requestMap.containsKey("columns")) {
                    int columns = Integer.parseInt(requestMap.get("columns").toString());
                    int rows = requestMap.containsKey("rows") ? Integer.parseInt(requestMap.get("rows").toString()) : 1;
                    spritesRequest.setTile(rows + "x" + columns);
                    logger.info("从rows/columns构建tile参数: {}", spritesRequest.getTile());
                } else {
                    spritesRequest.setTile("3x4"); // 默认3衤4列
                    logger.info("使用默认tile参数: {}", spritesRequest.getTile());
                }
            }
            
            // 5. 处理图片缩放参数scale
            if (requestMap.containsKey("scale")) {
                spritesRequest.setScale(requestMap.get("scale").toString());
            } else if (requestMap.containsKey("width") && requestMap.containsKey("height")) {
                // 如果提供了宽高，从width/height构建scale参数
                String width = requestMap.get("width").toString();
                String height = requestMap.get("height").toString();
                spritesRequest.setScale(width + "x" + height);
                logger.info("从width/height构建scale参数: {}", spritesRequest.getScale());
            }
            
            // 6. 处理输出格式
            if (requestMap.containsKey("dst_type")) {
                spritesRequest.setDstType(requestMap.get("dst_type").toString());
            } else if (requestMap.containsKey("format")) {
                // 兼容format参数
                spritesRequest.setDstType(requestMap.get("format").toString());
            } else {
                // 默认使用jpg格式
                spritesRequest.setDstType("jpg");
            }
            
            // 7. 处理其他参数
            if (requestMap.containsKey("interval")) {
                spritesRequest.setInterval(Integer.parseInt(requestMap.get("interval").toString()));
            }
            if (requestMap.containsKey("itsoffset")) {
                spritesRequest.setItsoffset(Integer.parseInt(requestMap.get("itsoffset").toString()));
            }
            if (requestMap.containsKey("padding")) {
                spritesRequest.setPadding(Integer.parseInt(requestMap.get("padding").toString()));
            }
            if (requestMap.containsKey("start")) {
                spritesRequest.setStart(Integer.parseInt(requestMap.get("start").toString()));
            }
            if (requestMap.containsKey("duration")) {
                spritesRequest.setDuration(Integer.parseInt(requestMap.get("duration").toString()));
            }
            if (requestMap.containsKey("color")) {
                spritesRequest.setColor(requestMap.get("color").toString());
            }
            
            // 8. 记录发送到云函数的请求JSON
            String requestJson = JSON.toJSONString(spritesRequest);
            logger.info("发送到云函数的请求JSON: {}", requestJson);
            
            // 9. 调用函数计算服务
            String result = functionComputeService.getSprites(spritesRequest);
            logger.info("雪碧图生成结果: {}", result);
            
            SpriteResponse response = new SpriteResponse();
            
            // 构建雪碧图URL
            java.util.List<String> spriteUrls = new java.util.ArrayList<>();
            
            // 创建一个标准命名约定的URL
            // 定义全局变量避免作用域问题
            String bucketName = spritesRequest.getBucketName();
            try {
                // 直接使用OSS SDK，不需要手动构造URL
                String ossEndpoint = aliyunConfig.getOssEndpoint();
                logger.info("使用OSS端点: {}, 桶名: {}", ossEndpoint, bucketName);
                
                // 直接使用OSS SDK直接获取雪碧图
                boolean foundUrls = false;
                
                // 解析云函数返回的完整响应JSON
                if (result != null && !result.isEmpty()) {
                    try {
                        // 将响应解析为JSON对象
                        JSONObject responseJson = JSON.parseObject(result);
                        
                        // 获取日志内容（Base64编码）
                        if (responseJson.containsKey("headers") && responseJson.getJSONObject("headers") != null) {
                            JSONObject headers = responseJson.getJSONObject("headers");
                            String logResult = headers.getString("x-fc-log-result");
                            if (logResult != null && !logResult.isEmpty()) {
                                // 解码Base64日志
                                try {
                                    String decodedLog = new String(java.util.Base64.getDecoder().decode(logResult), "UTF-8");
                                    logger.info("解码日志: {}", decodedLog);
                                    
                                    // 在日志中搜索"Uploaded""to"关键字来找到上传的文件路径
                                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Uploaded\\s+(/.*?)\\s+to\\s+(.*?)\\s");
                                    java.util.regex.Matcher matcher = pattern.matcher(decodedLog);
                                    
                                    while (matcher.find()) {
                                        // 这里的组二包含了上传到OSS的路径
                                        String ossPath = matcher.group(2).trim();
                                        if (ossPath != null && !ossPath.isEmpty()) {
                                            // 直接使用OSS SDK检查文件是否存在
                                            boolean exists = ossClient.doesObjectExist(bucketName, ossPath);
                                            if (exists) {
                                                // 生成一个时效为1小时的签名URL
                                                java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 3600 * 1000);
                                                String signedUrl = ossClient.generatePresignedUrl(bucketName, ossPath, expiration).toString();
                                                foundUrls = true;
                                                spriteUrls.add(signedUrl);
                                                logger.info("生成的雪碧图签名URL: {}", signedUrl);
                                            } else {
                                                logger.warn("OSS对象不存在: {}/{}", bucketName, ossPath);
                                            }
                                        }
                                    }
                                    
                                    // 如果没找到，再尝试直接查找完整路径
                                    if (!foundUrls) {
                                        // 基于日志信息匹配可能的图片路径
                                        java.util.regex.Pattern pathPattern = java.util.regex.Pattern.compile("sprites/.*?\\.jpg");
                                        java.util.regex.Matcher pathMatcher = pathPattern.matcher(decodedLog);
                                        
                                        while (pathMatcher.find()) {
                                            String ossPath = pathMatcher.group().trim();
                                            // 检查该文件是否存在
                                            boolean exists = ossClient.doesObjectExist(bucketName, ossPath);
                                            if (exists) {
                                                // 生成一个时效为1小时的签名URL
                                                java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 3600 * 1000);
                                                String signedUrl = ossClient.generatePresignedUrl(bucketName, ossPath, expiration).toString();
                                                foundUrls = true;
                                                spriteUrls.add(signedUrl);
                                                logger.info("生成的雪碧图签名URL（简化匹配）: {}", signedUrl);
                                            } else {
                                                logger.warn("OSS对象不存在: {}/{}", bucketName, ossPath);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.warn("解析日志失败: {}", e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("JSON解析失败: {}", e.getMessage());
                    }
                }
                
                // 2. 如果无法从响应中获取URL，则使用约定构造路径并检查OSS中是否存在
                if (!foundUrls) {
                    logger.info("未从响应中找到雪碧图对象，尝试使用命名约定检查");
                    
                    // 构造基础输出路径
                    String baseOutputPath = spritesRequest.getOutputDir();
                    if (!baseOutputPath.endsWith("/")) {
                        baseOutputPath = baseOutputPath + "/";
                    }
                    
                    // 从对象键中提取文件名（不含扩展名）
                    String objectKey = spritesRequest.getObjectKey();
                    String baseName = objectKey;
                    int lastSlash = objectKey.lastIndexOf("/");
                    if (lastSlash >= 0) {
                        baseName = objectKey.substring(lastSlash + 1);
                    }
                    int lastDot = baseName.lastIndexOf(".");
                    if (lastDot >= 0) {
                        baseName = baseName.substring(0, lastDot);
                    }
                    
                    // 拼接可能的雪碧图文件名
                    String spriteBasePath = baseOutputPath + baseName + "_sprite";
                    String extension = spritesRequest.getDstType();
                    if (extension == null || extension.isEmpty()) {
                        extension = "jpg"; // 默认扩展名
                    }
                    
                    // 检查主雪碧图文件
                    String mainSpriteKey = spriteBasePath + "." + extension;
                    if (ossClient.doesObjectExist(bucketName, mainSpriteKey)) {
                        // 生成签名URL
                        java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 3600 * 1000);
                        String mainSignedUrl = ossClient.generatePresignedUrl(bucketName, mainSpriteKey, expiration).toString();
                        spriteUrls.add(mainSignedUrl);
                        logger.info("找到雪碧图并生成签名URL: {}", mainSignedUrl);
                        foundUrls = true;
                    } else {
                        logger.warn("OSS中未找到雪碧图: {}/{}", bucketName, mainSpriteKey);
                    }
                    
                    // 检查VTT文件
                    String vttKey = spriteBasePath + ".vtt";
                    if (ossClient.doesObjectExist(bucketName, vttKey)) {
                        // 生成签名URL
                        java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 3600 * 1000);
                        String vttSignedUrl = ossClient.generatePresignedUrl(bucketName, vttKey, expiration).toString();
                        spriteUrls.add(vttSignedUrl);
                        logger.info("找到VTT文件并生成签名URL: {}", vttSignedUrl);
                        foundUrls = true;
                    } else {
                        logger.warn("OSS中未找到VTT文件: {}/{}", bucketName, vttKey);
                    }
                }
            } catch (Exception e) {
                logger.warn("构造雪碧图URL失败: {}", e.getMessage());
            }
            
            // 如果最终没有找到任何雪碧图，返回错误
            if (spriteUrls.isEmpty()) {
                logger.error("没有找到任何雪碧图文件");
                return ApiResponse.error(404, "没有找到雪碧图文件，请检查日志和参数设置");
            }
            
            response.setSpriteUrls(spriteUrls);
            return ApiResponse.success(response);
        } catch (Exception e) {
            logger.error("生成雪碧图失败", e);
            return ApiResponse.error(500, "生成雪碧图失败: " + e.getMessage());
        }
    }
}
