package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.res.NotificationResponse;
import com.example.chat.repository.DeviceTokenRepository;
import com.example.chat.service.DeviceTokenService;
import com.example.chat.service.NotificationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;

    @GetMapping()
    public ApiResponse<List<NotificationResponse>> getNotifications() {
        List<NotificationResponse> notifications = notificationService.getUserNotifications();

        return ApiResponse.<List<NotificationResponse>>builder()
                .code(HttpServletResponse.SC_OK)
                .message("Success")
                .data(notifications)
                .build();
    }

    @PutMapping("/mark-all-read-by-date")
    public ApiResponse<Integer> markAllAsReadByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        int updated = notificationService.markAllAsReadByDate(date);

        return ApiResponse.<Integer>builder()
                .code(HttpServletResponse.SC_OK)
                .message("Đã đánh dấu " + updated + " thông báo là đã đọc trong ngày " + date)
                .data(updated)
                .build();
    }

    @PutMapping("/mark-read/{id}")
    public ApiResponse<Void> markAsReadById(@PathVariable Long id) {
        notificationService.markAsReadById(id);

        return ApiResponse.<Void>builder()
                .code(HttpServletResponse.SC_OK)
                .message("Đã đánh dấu thông báo ID " + id + " là đã đọc")
                .build();
    }


    @PostMapping("/register-fcm_token")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, String> body) {
        String fcmToken = body.get("fcmToken");
        String deviceId = body.get("deviceId");

        if (fcmToken == null || fcmToken.isBlank()) {
            return ResponseEntity.badRequest().body("FCM token is required");
        }
        if (deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().body("Device ID is required");
        }

        Long accountId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        deviceTokenService.registerFCMToken(accountId, deviceId, fcmToken);

        return ResponseEntity.ok("Token saved for user " + accountId + " on device " + deviceId);
    }
}
