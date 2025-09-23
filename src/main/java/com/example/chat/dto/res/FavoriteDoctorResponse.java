package com.example.chat.dto.res;

import lombok.Data;

@Data
public class FavoriteDoctorResponse {
    private Long id;
    private String doctorName;
    private String doctorAvatarUrl;
    private String specialization;
    private Long totalReviews;
    private Double avgRating;
}
