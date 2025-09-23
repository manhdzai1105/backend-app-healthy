package com.example.chat.firebase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Getter
@Component
public class GoogleCredentialProvider {

    private final String projectId;
    private final GoogleCredentials googleCredentials;

    public GoogleCredentialProvider(@Value("${firebase.credentials}") String firebaseCredentials) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(firebaseCredentials);
        this.projectId = jsonNode.get("project_id").asText();

        this.googleCredentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
    }

    public String getAccessToken() throws Exception {
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
