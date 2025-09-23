package com.example.chat.repository;

import com.example.chat.entity.DoctorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DoctorReviewRepository extends JpaRepository<DoctorReview,Long> {
    Optional<DoctorReview> findByAppointmentId(Long appointmentId);

    @Query("SELECT COUNT(r) FROM DoctorReview r WHERE r.doctor.id = :doctorId")
    Long countByDoctorId(Long doctorId);

    @Query("SELECT AVG(r.rating) FROM DoctorReview r WHERE r.doctor.id = :doctorId")
    Double avgRatingByDoctorId(Long doctorId);

    @Query("SELECT r.doctor.id, COUNT(r), AVG(r.rating) " +
            "FROM DoctorReview r " +
            "GROUP BY r.doctor.id " +
            "ORDER BY COUNT(r) DESC")
    List<Object[]> findTopDoctorsByReviewCount();

    List<DoctorReview> findAllByDoctor_Id(Long doctorId);

    boolean existsByAppointmentIdAndUserId(Long appointmentId, Long userId);
}
