package com.ffmpeg.web.model;

import java.util.List;

/**
 * 雪碧图生成响应结果
 */
public class SpriteResponse {
    private List<String> spriteUrls;      // 生成的雪碧图OSS URL列表
    private String outputDir;             // 输出目录
    private String status;                // 状态 - success或error
    
    public List<String> getSpriteUrls() {
        return spriteUrls;
    }
    
    public void setSpriteUrls(List<String> spriteUrls) {
        this.spriteUrls = spriteUrls;
    }
    
    public String getOutputDir() {
        return outputDir;
    }
    
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
