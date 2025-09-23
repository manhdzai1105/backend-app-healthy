package com.example.chat.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull(message = "AppointmentId không được để trống")
    private Long appointmentId;

    @NotNull(message = "Rating không được để trống")
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}
