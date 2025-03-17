package com.xmq.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.urlPrefix}")
    private String urlPrefix;

    @Value("${minio.maxSize}")
    private Long maxSize;
//
//    @Value("${minio.allowedContentTypes}")
//    private String[] allowedContentTypes;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public Long getMaxSize() {
        return maxSize;
    }

//    public String[] getAllowedContentTypes() {
//        return allowedContentTypes;
//    }
} 