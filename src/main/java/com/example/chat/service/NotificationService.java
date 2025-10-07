package com.example.chat.service;

import com.example.chat.dto.res.NotificationResponse;
import com.example.chat.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getUserNotifications() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .body(n.getBody())
                        .type(n.getType())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public int markAllAsReadByDate(LocalDate date) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationRepository.markAllAsReadByUserAndDate(userId, date);
    }

    public void markAsReadById(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
}
