package com.example.chat.crons.reminder.service;

import com.example.chat.entity.Appointment;
import com.example.chat.entity.DeviceToken;
import com.example.chat.entity.Notification;
import com.example.chat.enums.NotificationType;
import com.example.chat.firebase.FcmService;
import com.example.chat.repository.DeviceTokenRepository;
import com.example.chat.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemindService {
    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;

    public void send15mReminder(Appointment appt) {
        String title = "Nhắc lịch hẹn";
        String body = String.format(
                "Bạn có lịch hẹn với bác sĩ %s lúc %s",
                appt.getDoctor().getUsername(),
                appt.getAppointmentTime()
        );

        // Gửi cho bệnh nhân
        saveAndPushNotification(appt.getUser().getId(), title, body, NotificationType.REMINDER);

        // Gửi cho bác sĩ
        String doctorBody = String.format(
                "Bạn có lịch hẹn với bệnh nhân %s lúc %s",
                appt.getUser().getUsername(),
                appt.getAppointmentTime()
        );
        saveAndPushNotification(appt.getDoctor().getId(), title, doctorBody, NotificationType.REMINDER);
    }

    private void saveAndPushNotification(Long userId, String title, String body, NotificationType type) {
        // 1. Lưu vào DB
        Notification noti = Notification.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(noti);

        // 2. Gửi FCM
        var tokens = deviceTokenRepository.findByUserId(userId);
        for (DeviceToken token : tokens) {
            try {
                fcmService.sendNotification(
                        token.getFcmToken(),
                        title,
                        body,
                        Map.of("type", type.name())
                );
            } catch (Exception e) {
                log.error("❌ Push FCM thất bại tới user {}: {}", userId, e.getMessage());
            }
        }
    }
}
