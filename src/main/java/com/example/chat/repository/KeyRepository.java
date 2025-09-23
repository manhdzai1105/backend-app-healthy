package com.example.chat.repository;

import com.example.chat.entity.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeyRepository extends JpaRepository<Key, Long> {
    Optional<Key> findByAccount_IdAndDeviceId(Long accountId, String deviceId);
    Optional<Key> findByRefreshToken(String refreshToken);
    void deleteByAccount_IdAndDeviceId(Long accountId, String deviceId);
}

