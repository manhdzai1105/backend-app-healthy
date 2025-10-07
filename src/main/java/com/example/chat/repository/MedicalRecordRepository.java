package com.example.chat.repository;

import com.example.chat.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    // 🔍 Tìm theo mã hồ sơ (recordCode)
    Optional<MedicalRecord> findByRecordCodeIgnoreCase(String recordCode);

    // 🔍 Tìm theo tên bệnh nhân (có chứa keyword)
    @Query("SELECT m FROM MedicalRecord m WHERE LOWER(m.patientName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MedicalRecord> findByPatientNameContainingIgnoreCase(@Param("name") String name);
}
