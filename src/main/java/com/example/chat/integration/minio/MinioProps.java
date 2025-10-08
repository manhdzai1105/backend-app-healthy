package com.example.chat.integration.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProps {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String keyPrefix ;
    private int presignExpirySeconds;
    private boolean makeBucketPublic;
}
