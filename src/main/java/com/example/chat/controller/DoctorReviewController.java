package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.req.CreateReviewRequest;
import com.example.chat.entity.DoctorReview;
import com.example.chat.service.DoctorReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class DoctorReviewController {
    private final DoctorReviewService doctorReviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorReview>> createReview(
            @Valid @RequestBody CreateReviewRequest request
    ) {
        DoctorReview review = doctorReviewService.createReview(request);

        return ResponseEntity.status(201).body(
                ApiResponse.<DoctorReview>builder()
                        .code(201)
                        .message("Tạo review thành công!")
                        .data(review)
                        .build()
        );
    }
}
