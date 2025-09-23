package com.example.chat.repository;

import com.example.chat.entity.DoctorDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorDetailRepository extends JpaRepository<DoctorDetail, Long> {
    Optional<DoctorDetail> findByAccount_Id(Long userId);
    List<DoctorDetail> findBySpecializationIgnoreCase(String specialization);
}
