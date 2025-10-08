package com.example.chat.integration.zalopay;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "zalopay")
public class ZaloPayConfig {

    private String appId;
    private String key1;
    private String key2;
    private String endpoint;
    private String callbackUrl;
}
