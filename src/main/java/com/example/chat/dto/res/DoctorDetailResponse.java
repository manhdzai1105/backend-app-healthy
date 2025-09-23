package com.example.chat.dto.res;

import lombok.Data;
import java.util.List;

@Data
public class DoctorDetailResponse {
    private Long id;
    private String doctorName;
    private String doctorAvatarUrl;
    private String specialization;
    private Integer experienceYears;
    private String bio;
    private Long totalReviews;
    private Double avgRating;

    private List<DoctorReviewResponse> reviews;
}

