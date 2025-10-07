package com.example.chat.service;

import com.example.chat.dto.req.MedicalRecordRequest;
import com.example.chat.dto.req.PrescriptionRequest;
import com.example.chat.dto.res.MedicalRecordResponse;
import com.example.chat.dto.res.PrescriptionResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.MedicalRecord;
import com.example.chat.entity.Prescription;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecordRequest request) {
        // 🔍 Tìm bác sĩ
        Long doctorId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // 🩺 Tạo hồ sơ khám bệnh
        MedicalRecord record = MedicalRecord.builder()
                .patientName(request.getPatientName())
                .patientAge(request.getPatientAge())
                .patientPhone(request.getPatientPhone())
                .diagnosis(request.getDiagnosis())
                .symptoms(request.getSymptoms())
                .notes(request.getNotes())
                .doctor(doctor)
                .build();

        // 💊 Thêm danh sách đơn thuốc (nếu có)
        if (request.getPrescriptions() != null && !request.getPrescriptions().isEmpty()) {
            List<Prescription> prescriptions = new ArrayList<>();
            for (PrescriptionRequest pReq : request.getPrescriptions()) {
                Prescription p = Prescription.builder()
                        .medicineName(pReq.getMedicineName())
                        .quantity(pReq.getQuantity())
                        .unit(pReq.getUnit())
                        .dose(pReq.getDose())
                        .medicalRecord(record)
                        .build();
                prescriptions.add(p);
            }
            record.setPrescriptions(prescriptions);
        }

        record = medicalRecordRepository.save(record);

        record.setRecordCode("HS" + record.getId());

        return medicalRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> searchMedicalRecords(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String normalizedKeyword = keyword.toUpperCase();

        if (normalizedKeyword.startsWith("HS")) {
            return medicalRecordRepository.findByRecordCodeIgnoreCase(normalizedKeyword)
                    .map(List::of)
                    .orElse(List.of());
        }

        return medicalRecordRepository.findByPatientNameContainingIgnoreCase(normalizedKeyword);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordDetail(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        // 🔄 Ánh xạ sang DTO
        return toResponse(record);
    }

    // ✅ Mapper: Entity → DTO
    private MedicalRecordResponse toResponse(MedicalRecord record) {
        List<PrescriptionResponse> presResponses = record.getPrescriptions() == null
                ? List.of()
                : record.getPrescriptions().stream()
                .map(this::toPrescriptionResponse)
                .collect(Collectors.toList());

        return MedicalRecordResponse.builder()
                .id(record.getId())
                .recordCode(record.getRecordCode())
                .patientName(record.getPatientName())
                .patientAge(record.getPatientAge())
                .patientPhone(record.getPatientPhone())
                .doctorName(record.getDoctor() != null ? record.getDoctor().getUsername() : null)
                .specialty(record.getDoctor() != null ? record.getDoctor().getDoctorDetail().getSpecialization() : null)
                .diagnosis(record.getDiagnosis())
                .symptoms(record.getSymptoms())
                .notes(record.getNotes())
                .prescriptions(presResponses)
                .timestamp(record.getCreatedAt())
                .build();
    }

    private PrescriptionResponse toPrescriptionResponse(Prescription p) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .medicineName(p.getMedicineName())
                .quantity(p.getQuantity())
                .unit(p.getUnit())
                .dose(p.getDose())
                .build();
    }
}
