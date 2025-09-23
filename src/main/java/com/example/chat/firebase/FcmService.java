package com.example.chat.firebase;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final GoogleCredentialProvider credentialProvider;

    /**
     * Gửi push notification đơn giản chỉ có title + body.
     */
    public void sendNotification(String targetToken, String title, String body) throws Exception {
        sendNotification(targetToken, title, body, null);
    }

    /**
     * Gửi push notification kèm data payload.
     * @param targetToken FCM token của thiết bị
     * @param title tiêu đề thông báo
     * @param body nội dung thông báo
     * @param data map chứa payload (ví dụ type, appointmentId)
     */
    public void sendNotification(String targetToken, String title, String body, Map<String, String> data) throws Exception {
        String endpoint = "https://fcm.googleapis.com/v1/projects/"
                + credentialProvider.getProjectId() + "/messages:send";

        // Thông tin hiển thị trên notification bar
        Map<String, Object> notification = Map.of("title", title, "body", body);

        // Message
        var messageBuilder = new java.util.HashMap<String, Object>();
        messageBuilder.put("token", targetToken);
        messageBuilder.put("notification", notification);
        if (data != null && !data.isEmpty()) {
            messageBuilder.put("data", data);
        }

        Map<String, Object> payload = Map.of("message", messageBuilder);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(credentialProvider.getAccessToken());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(endpoint, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
    }
}
