package com.ffmpeg.web.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云配置类
 */
@Configuration
public class AliyunConfig {

    @Value("${aliyun.account-id:}")
    private String accountId;
    
    @Value("${aliyun.access-key:}")
    private String accessKey;

    @Value("${aliyun.secret-key:}")
    private String secretKey;

    @Value("${aliyun.region:cn-hangzhou}")
    private String region;

    @Value("${aliyun.fc.service-name:}")
    private String fcServiceName;

    @Value("${aliyun.fc.endpoint:}")
    private String fcEndpoint;
    
    @Value("${aliyun.oss.bucket-name:}")
    private String ossBucketName;
    
    @Value("${aliyun.oss.endpoint:}")
    private String ossEndpoint;

    public String getAccountId() {
        return accountId;
    }
    
    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    public String getFcServiceName() {
        return fcServiceName;
    }

    public String getFcEndpoint() {
        return fcEndpoint;
    }
    
    public String getOssBucketName() {
        return ossBucketName;
    }
    
    public String getOssEndpoint() {
        return ossEndpoint;
    }
    
    /**
     * 创建OSS客户端
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(ossEndpoint, accessKey, secretKey);
    }
}
