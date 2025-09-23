package com.example.chat.integration.minio;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class MinioProps {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.keyPrefix:uploads/}")
    private String keyPrefix;

    @Value("${minio.presignExpirySeconds:300}")
    private int presignExpirySeconds;

    @Value("${minio.makeBucketPublic:false}")
    private boolean makeBucketPublic;
}
