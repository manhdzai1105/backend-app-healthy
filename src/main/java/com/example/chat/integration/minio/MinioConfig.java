package com.example.chat.integration.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProps props;

    @Bean
    public MinioClient minioClient() {
        String raw = props.getEndpoint();

        // sanitize endpoint
        URI u = URI.create(raw);
        String safeEndpoint = u.getScheme() + "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "");

        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5))
                .callTimeout(Duration.ofMinutes(5))
                .build();

        return MinioClient.builder()
                .endpoint(safeEndpoint)
                .credentials(props.getAccessKey(), props.getSecretKey())
                .httpClient(http)
                .build();
    }
}
