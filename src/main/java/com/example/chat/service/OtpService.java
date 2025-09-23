package com.example.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final RedisBaseService redisBaseService;

    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisBaseService.setWithTTL("OTP:" + email, otp, 15); // TTL = 15 phút
        return otp;
    }

    // Kiểm tra OTP
    public boolean verifyOtp(String email, String otp) {
        String key = "OTP:" + email;
        Object cached = redisBaseService.get(key);

        return cached != null && cached.toString().equals(otp);
    }

    // Xóa OTP sau khi reset thành công
    public void deleteOtp(String email) {
        redisBaseService.delete("OTP:" + email);
    }

}
