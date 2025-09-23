package com.example.chat.dto.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorReviewResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
