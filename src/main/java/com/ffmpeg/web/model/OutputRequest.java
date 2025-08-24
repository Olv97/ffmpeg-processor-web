package com.ffmpeg.web.model;

import com.alibaba.fastjson.annotation.JSONField;

// 移除Lombok依赖，手动添加getter/setter
// import lombok.Data;
// import lombok.EqualsAndHashCode;

/**
 * 带输出目录的请求基类
 */
// @Data
// @EqualsAndHashCode(callSuper = true)
public class OutputRequest extends BaseRequest {
    /**
     * 生成的文件在 OSS 存储桶上存储目录
     */
    @JSONField(name = "output_dir")
    private String outputDir;
    
    /**
     * 获取输出目录
     */
    public String getOutputDir() {
        return outputDir;
    }
    
    /**
     * 设置输出目录
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
