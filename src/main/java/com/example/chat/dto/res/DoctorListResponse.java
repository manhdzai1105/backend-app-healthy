package com.example.chat.dto.res;

import lombok.Data;

@Data
public class DoctorListResponse {
    private Long id;
    private String doctorName;
    private String doctorAvatarUrl;
    private Integer experienceYears;
    private String specialization;
    private Long totalReviews;
    private Double avgRating;
    private Boolean isFavorited;
}
