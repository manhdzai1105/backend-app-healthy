package com.example.chat.firebase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "firebase")
public class GoogleCredentialProvider {

    private String credentials;
    private String projectId;
    private GoogleCredentials googleCredentials;

    @PostConstruct
    private void init() throws Exception {
        if (credentials == null || credentials.isEmpty()) {
            throw new IllegalStateException("Firebase credentials not configured!");
        }

        // Parse JSON để lấy project_id
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(credentials);
        this.projectId = jsonNode.get("project_id").asText();

        // Tạo GoogleCredentials từ chuỗi JSON
        this.googleCredentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(credentials.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
    }

    public String getAccessToken() throws Exception {
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
