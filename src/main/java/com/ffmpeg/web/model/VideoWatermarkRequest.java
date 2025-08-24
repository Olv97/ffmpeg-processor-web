package com.ffmpeg.web.model;

import com.alibaba.fastjson.annotation.JSONField;

// 移除Lombok依赖，手动添加getter/setter
// import lombok.Data;
// import lombok.EqualsAndHashCode;

/**
 * 视频水印请求
 */
// @Data
// @EqualsAndHashCode(callSuper = true)
public class VideoWatermarkRequest extends OutputRequest {
    /**
     * 文字水印或静态图片水印参数
     */
    @JSONField(name = "vf_args")
    private String vfArgs;
    
    /**
     * 动态GIF图片水印参数
     */
    @JSONField(name = "filter_complex_args")
    private String filterComplexArgs;
    
    /**
     * 获取文字水印或静态图片水印参数
     */
    public String getVfArgs() {
        return vfArgs;
    }
    
    /**
     * 设置文字水印或静态图片水印参数
     */
    public void setVfArgs(String vfArgs) {
        this.vfArgs = vfArgs;
    }
    
    /**
     * 获取动态GIF图片水印参数
     */
    public String getFilterComplexArgs() {
        return filterComplexArgs;
    }
    
    /**
     * 设置动态GIF图片水印参数
     */
    public void setFilterComplexArgs(String filterComplexArgs) {
        this.filterComplexArgs = filterComplexArgs;
    }
}
