package com.example.chat.service;

import com.example.chat.dto.req.CreateReviewRequest;
import com.example.chat.entity.Appointment;
import com.example.chat.entity.DoctorReview;
import com.example.chat.enums.AppointmentStatus;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.AppointmentRepository;
import com.example.chat.repository.DoctorReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorReviewService {
    private final DoctorReviewRepository doctorReviewRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public DoctorReview createReview(CreateReviewRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Lấy appointment
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy appointment"));

        // Kiểm tra appointment thuộc về user
        if (!appointment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền review appointment này");
        }

        // Kiểm tra appointment đã completed chưa
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Chỉ có thể review khi appointment đã hoàn thành");
        }

        // Kiểm tra đã có review chưa
        doctorReviewRepository.findByAppointmentId(request.getAppointmentId())
                .ifPresent(r -> {
                    throw new IllegalArgumentException("Appointment này đã có review");
                });

        // Tạo review
        DoctorReview review = DoctorReview.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .user(appointment.getUser())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return doctorReviewRepository.save(review);
    }
}
