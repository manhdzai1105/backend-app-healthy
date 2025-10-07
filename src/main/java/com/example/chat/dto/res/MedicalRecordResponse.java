package com.example.chat.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MedicalRecordResponse {
    private Long id;
    private String recordCode;
    private String patientName;
    private Integer patientAge;
    private String patientPhone;
    private String doctorName;
    private String specialty;
    private String diagnosis;
    private String symptoms;
    private String notes;
    private List<PrescriptionResponse> prescriptions;
    private LocalDateTime timestamp;
}
