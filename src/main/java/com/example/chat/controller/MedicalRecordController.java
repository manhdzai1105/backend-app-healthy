package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.req.MedicalRecordRequest;
import com.example.chat.dto.res.MedicalRecordResponse;
import com.example.chat.entity.MedicalRecord;
import com.example.chat.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicalRecord>> createMedicalRecord(@RequestBody MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordService.createMedicalRecord(request);

        ApiResponse<MedicalRecord> response = ApiResponse.<MedicalRecord>builder()
                .code(HttpStatus.CREATED.value())
                .message("Tạo hồ sơ bệnh án thành công")
                .data(record)
                .build();

        // HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedicalRecord>>> searchMedicalRecords(
            @RequestParam("keyword") String keyword) {

        List<MedicalRecord> results = medicalRecordService.searchMedicalRecords(keyword);

        ApiResponse<List<MedicalRecord>> response = ApiResponse.<List<MedicalRecord>>builder()
                .code(HttpStatus.OK.value())
                .message("Tìm thấy " + results.size() + " hồ sơ phù hợp")
                .data(results)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordDetail(
            @PathVariable("recordId") Long recordId) {

        MedicalRecordResponse detail = medicalRecordService.getMedicalRecordDetail(recordId);

        ApiResponse<MedicalRecordResponse> response = ApiResponse.<MedicalRecordResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin hồ sơ thành công")
                .data(detail)
                .build();

        return ResponseEntity.ok(response);
    }
}
