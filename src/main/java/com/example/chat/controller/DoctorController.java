package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.req.CreateDoctorRequest;
import com.example.chat.dto.req.UpdateDoctorRequest;
import com.example.chat.dto.res.DoctorDetailResponse;
import com.example.chat.dto.res.DoctorListResponse;
import com.example.chat.dto.res.DoctorResponse;
import com.example.chat.exception.ConflictException;
import com.example.chat.repository.AccountRepository;
import com.example.chat.service.DoctorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AccountRepository accountRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(@Valid @RequestBody CreateDoctorRequest createDoctorRequest) {
        if (accountRepository.existsByEmail(createDoctorRequest.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }
        DoctorResponse doctorResponse = doctorService.createDoctor(createDoctorRequest);

        return ResponseEntity.ok(
                ApiResponse.<DoctorResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Tạo bác sĩ thành công")
                        .data(doctorResponse)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctorById(
            @PathVariable("id") Long accountId,
            @RequestBody @Valid UpdateDoctorRequest request
    ) {
        DoctorResponse updated = doctorService.updateDoctorById(accountId, request);

        return ResponseEntity.ok(
                ApiResponse.<DoctorResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Cập nhật bac si thành công")
                        .data(updated)
                        .build()
        );
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @RequestBody @Valid UpdateDoctorRequest request
    ) {
        DoctorResponse updated = doctorService.updateDoctor(request);
        return ResponseEntity.ok(
                ApiResponse.<DoctorResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Cập nhật bac si thành công")
                        .data(updated)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctorById(
            @PathVariable("id") Long accountId
    ) {
        doctorService.deleteDoctorById(accountId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa bác sĩ thành công")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorListResponse>>> getDoctors() {
        List<DoctorListResponse> doctors = doctorService.getAllDoctors();

        return ResponseEntity.ok(
                ApiResponse.<List<DoctorListResponse>>builder()
                        .code(200)
                        .message("Danh sách tất cả bác sĩ")
                        .data(doctors)
                        .build()
        );
    }

    @GetMapping("/top3")
    public ResponseEntity<ApiResponse<List<DoctorListResponse>>> getTop3Doctors() {
        List<DoctorListResponse> topDoctors = doctorService.getTop3Doctors();

        return ResponseEntity.ok(
                ApiResponse.<List<DoctorListResponse>>builder()
                        .code(200)
                        .message("Top 3 bác sĩ có nhiều review nhất")
                        .data(topDoctors)
                        .build()
        );
    }

    @GetMapping("/{specialization}")
    public ResponseEntity<ApiResponse<List<DoctorListResponse>>> getDoctorsBySpecialization(
            @PathVariable String specialization
    ) {
        List<DoctorListResponse> doctors = doctorService.getDoctorsBySpecialization(specialization);

        return ResponseEntity.ok(
                ApiResponse.<List<DoctorListResponse>>builder()
                        .code(200)
                        .message("Danh sách bác sĩ chuyên môn: " + specialization)
                        .data(doctors)
                        .build()
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> getDoctorDetail(@PathVariable Long id) {
        DoctorDetailResponse doctor = doctorService.getDoctorDetail(id);

        return ResponseEntity.ok(
                ApiResponse.<DoctorDetailResponse>builder()
                        .code(200)
                        .message("Chi tiết bác sĩ")
                        .data(doctor)
                        .build()
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorProfile() {
        DoctorResponse response = doctorService.getProfileDoctor();

        ApiResponse<DoctorResponse> apiResponse = ApiResponse.<DoctorResponse>builder()
                .code(200)
                .message("Lấy thông tin bác sĩ thành công")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

}
