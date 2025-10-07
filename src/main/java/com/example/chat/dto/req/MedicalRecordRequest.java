package com.example.chat.dto.req;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordRequest {
    private String patientName;
    private Integer patientAge;
    private String patientPhone;
    private String diagnosis;
    private String symptoms;
    private String notes;
    private List<PrescriptionRequest> prescriptions;
}
