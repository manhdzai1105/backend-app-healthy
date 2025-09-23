package com.example.chat.service;

import com.example.chat.entity.DeviceToken;
import com.example.chat.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;

    public void registerFCMToken(Long userId, String deviceId, String fcmToken) {
        deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .ifPresentOrElse(
                        existing -> {
                            // cập nhật fcmToken nếu đã tồn tại
                            existing.setFcmToken(fcmToken);
                            deviceTokenRepository.save(existing);
                        },
                        () -> {
                            // tạo mới nếu chưa có
                            DeviceToken token = DeviceToken.builder()
                                    .userId(userId)
                                    .deviceId(deviceId)
                                    .fcmToken(fcmToken)
                                    .build();
                            deviceTokenRepository.save(token);
                        }
                );
    }
}
