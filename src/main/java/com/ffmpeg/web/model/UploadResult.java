package com.ffmpeg.web.model;

/**
 * 文件上传结果
 */
public class UploadResult {
    private String bucketName;
    private String objectKey;
    private String originalFilename;
    
    public UploadResult() {
    }
    
    public UploadResult(String bucketName, String objectKey, String originalFilename) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getObjectKey() {
        return objectKey;
    }
    
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
}
