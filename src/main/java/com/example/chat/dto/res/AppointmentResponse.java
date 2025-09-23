package com.example.chat.dto.res;

import com.example.chat.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status;

    private Long doctorId;
    private String doctorName;
    private String specialization;
    private String doctorAvatarUrl;
    private Long totalReviews;
    private Double avgRating;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private Boolean reviewed;
}
