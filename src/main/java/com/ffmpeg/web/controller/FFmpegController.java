package com.ffmpeg.web.controller;

import com.ffmpeg.web.model.*;
import com.ffmpeg.web.service.FunctionComputeService;
import com.ffmpeg.web.service.MediaMetadataParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FFmpeg处理控制器
 */
@RestController
@RequestMapping("/api/ffmpeg")
public class FFmpegController {

    @Autowired
    private FunctionComputeService functionComputeService;
    
    @Autowired
    private MediaMetadataParser mediaMetadataParser;

    /**
     * 获取媒体元信息
     * 返回结构化的媒体元数据，适合前端表格显示
     */
    @PostMapping("/getMediaMeta")
    public ApiResponse<MediaMetadata> getMediaMeta(@RequestBody BaseRequest request) {
        try {
            // 调用函数计算获取原始元数据
            String rawMetadata = functionComputeService.getMediaMeta(request);
            
            // 解析原始元数据为结构化对象
            MediaMetadata metadata = mediaMetadataParser.parseMetadata(rawMetadata);
            
            // 设置文件名（如果没有从元数据中提取到）
            if (metadata.getFileName() == null || metadata.getFileName().isEmpty()) {
                metadata.setFileName(request.getObjectKey());
            }
            
            return ApiResponse.success(metadata);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "获取媒体元信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取媒体时长
     */
    @PostMapping("/getDuration")
    public ApiResponse<String> getDuration(@RequestBody BaseRequest request) {
        try {
            String result = functionComputeService.getDuration(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取媒体时长失败: " + e.getMessage());
        }
    }

    /**
     * 生成雪碧图
     */
    @PostMapping("/getSprites")
    public ApiResponse<String> getSprites(@RequestBody GetSpritesRequest request) {
        try {
            String result = functionComputeService.getSprites(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "生成雪碧图失败: " + e.getMessage());
        }
    }

    /**
     * 视频添加水印
     */
    @PostMapping("/videoWatermark")
    public ApiResponse<String> videoWatermark(@RequestBody VideoWatermarkRequest request) {
        try {
            String result = functionComputeService.videoWatermark(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "视频添加水印失败: " + e.getMessage());
        }
    }

    /**
     * 视频转GIF
     */
    @PostMapping("/videoGif")
    public ApiResponse<String> videoGif(@RequestBody VideoGifRequest request) {
        try {
            String result = functionComputeService.videoGif(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "视频转GIF失败: " + e.getMessage());
        }
    }

    /**
     * 音频格式转换
     */
    @PostMapping("/audioConvert")
    public ApiResponse<String> audioConvert(@RequestBody AudioConvertRequest request) {
        try {
            String result = functionComputeService.audioConvert(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "音频格式转换失败: " + e.getMessage());
        }
    }
}
