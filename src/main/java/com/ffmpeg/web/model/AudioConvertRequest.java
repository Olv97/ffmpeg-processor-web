package com.ffmpeg.web.model;

import com.alibaba.fastjson.annotation.JSONField;

// 移除Lombok依赖，手动添加getter/setter
// import lombok.Data;
// import lombok.EqualsAndHashCode;

/**
 * 音频转换请求
 */
// @Data
// @EqualsAndHashCode(callSuper = true)
public class AudioConvertRequest extends OutputRequest {
    /**
     * 生成的音频的格式，例如 .wav
     */
    @JSONField(name = "dst_type")
    private String dstType;
    
    /**
     * 声道数
     */
    @JSONField(name = "ac")
    private Integer ac;
    
    /**
     * 采样率
     */
    @JSONField(name = "ar")
    private Integer ar;
    
    /**
     * 获取生成的音频格式
     */
    public String getDstType() {
        return dstType;
    }
    
    /**
     * 设置生成的音频格式
     */
    public void setDstType(String dstType) {
        this.dstType = dstType;
    }
    
    /**
     * 获取声道数
     */
    public Integer getAc() {
        return ac;
    }
    
    /**
     * 设置声道数
     */
    public void setAc(Integer ac) {
        this.ac = ac;
    }
    
    /**
     * 获取采样率
     */
    public Integer getAr() {
        return ar;
    }
    
    /**
     * 设置采样率
     */
    public void setAr(Integer ar) {
        this.ar = ar;
    }
}
