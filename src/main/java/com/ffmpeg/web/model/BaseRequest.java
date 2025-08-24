package com.ffmpeg.web.model;

import com.alibaba.fastjson.annotation.JSONField;

// 移除Lombok依赖，手动添加getter/setter
// import lombok.Data;

/**
 * 所有请求的基础类
 */
// @Data
public class BaseRequest {
    /**
     * OSS 存储桶名字
     */
    @JSONField(name = "bucket_name")
    private String bucketName;
    
    /**
     * 音视频文件 OSS object key
     */
    @JSONField(name = "object_key")
    private String objectKey;
    
    /**
     * 获取OSS存储桶名字
     */
    public String getBucketName() {
        return bucketName;
    }
    
    /**
     * 设置OSS存储桶名字
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    /**
     * 获取音视频文件OSS object key
     */
    public String getObjectKey() {
        return objectKey;
    }
    
    /**
     * 设置音视频文件OSS object key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}
