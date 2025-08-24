package com.ffmpeg.web.model;

import com.alibaba.fastjson.annotation.JSONField;

// 移除Lombok依赖，手动添加getter/setter
// import lombok.Data;
// import lombok.EqualsAndHashCode;

/**
 * 视频转GIF请求
 */
// @Data
// @EqualsAndHashCode(callSuper = true)
public class VideoGifRequest extends OutputRequest {
    /**
     * 视频开始时间点（秒）
     */
    @JSONField(name = "start")
    private Integer start = 0;
    
    /**
     * 转换的帧数
     */
    @JSONField(name = "vframes")
    private Integer vframes;
    
    /**
     * 转换的时长（秒）
     */
    @JSONField(name = "duration")
    private Integer duration;
    
    /**
     * 获取视频开始时间点
     */
    public Integer getStart() {
        return start;
    }
    
    /**
     * 设置视频开始时间点
     */
    public void setStart(Integer start) {
        this.start = start;
    }
    
    /**
     * 获取转换的帧数
     */
    public Integer getVframes() {
        return vframes;
    }
    
    /**
     * 设置转换的帧数
     */
    public void setVframes(Integer vframes) {
        this.vframes = vframes;
    }
    
    /**
     * 获取转换的时长
     */
    public Integer getDuration() {
        return duration;
    }
    
    /**
     * 设置转换的时长
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
