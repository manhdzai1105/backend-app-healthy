package com.example.chat.service;

import com.example.chat.dto.res.AppointmentResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.Appointment;
import com.example.chat.entity.DeviceToken;
import com.example.chat.entity.Notification;
import com.example.chat.enums.AppointmentStatus;
import com.example.chat.enums.NotificationType;
import com.example.chat.firebase.FcmService;
import com.example.chat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AccountRepository accountRepository;

    private final DoctorDetailRepository doctorDetailRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final UserDetailRepository userDetailRepository;

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;

    // Danh sách slot cố định trong ngày
    private static final List<LocalTime> FIXED_SLOTS = Arrays.asList(
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30),
            LocalTime.of(11, 0),
            LocalTime.of(11, 30),
            LocalTime.of(14, 0),
            LocalTime.of(14, 30),
            LocalTime.of(15, 0),
            LocalTime.of(15, 30),
            LocalTime.of(16, 0),
            LocalTime.of(16, 30)
    );

    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {
        // ❌ Không cho lấy slot cuối tuần
        switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> throw new IllegalArgumentException("Không có lịch vào Thứ 7 hoặc Chủ nhật");
        }

        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));

        List<LocalTime> booked = appointmentRepository.findBookedSlots(doctor, date);
        List<LocalTime> available = new ArrayList<>(FIXED_SLOTS);
        available.removeAll(booked);
        return available;
    }

    @Transactional
    public Appointment bookAppointment(Long doctorId, LocalDate date, LocalTime time){
        switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> throw new IllegalArgumentException("Không cho phép đặt lịch cuối tuần");
        }

        if (!FIXED_SLOTS.contains(time)) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ: " + time);
        }

        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
        Account user = getCurrentUser();

        appointmentRepository.findByDoctorAndAppointmentDateAndAppointmentTime(doctor, date, time)
                .ifPresent(a -> { throw new IllegalStateException("Slot đã được đặt!"); });

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .user(user)
                .appointmentDate(date)
                .appointmentTime(time)
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 🔔 Thông báo cho bác sĩ
        saveAndPushNotification(
                doctor.getId(),
                "Lịch khám mới",
                "Bạn có lịch khám mới từ bệnh nhân " + user.getUsername(),
                NotificationType.APPOINTMENT_PENDING
        );

        return saved;
    }

    /**
     * Cập nhật trạng thái
     */
    @Transactional
    public Appointment updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Long actorId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .iterator().next().getAuthority().replace("ROLE_", "");

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        // ✅ Kiểm tra quyền
        switch (role) {
            case "USER" -> {
                if (!appointment.getUser().getId().equals(actorId)) {
                    throw new IllegalStateException("Bạn không có quyền cập nhật lịch này");
                }
                if (newStatus != AppointmentStatus.CANCELLED) {
                    throw new IllegalStateException("Người dùng chỉ có thể huỷ lịch");
                }
            }
            case "DOCTOR" -> {
                if (!appointment.getDoctor().getId().equals(actorId)) {
                    throw new IllegalStateException("Bạn không có quyền cập nhật lịch này");
                }
            }
            case "ADMIN" -> {
                // admin có toàn quyền
            }
            default -> throw new IllegalStateException("Vai trò không hợp lệ");
        }

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);

        // ✅ Thông báo theo trạng thái
        switch (newStatus) {
            case CONFIRMED -> saveAndPushNotification(
                    saved.getUser().getId(),
                    "Lịch hẹn đã được xác nhận",
                    "Bác sĩ " + saved.getDoctor().getUsername() + " đã xác nhận lịch hẹn",
                    NotificationType.APPOINTMENT_CONFIRMED
            );
            case CANCELLED -> {
                switch (role) {
                    case "USER" -> saveAndPushNotification(
                            saved.getDoctor().getId(),
                            "Lịch hẹn bị huỷ",
                            "Bệnh nhân " + saved.getUser().getUsername() + " đã huỷ lịch hẹn",
                            NotificationType.APPOINTMENT_CANCELLED
                    );
                    case "DOCTOR" -> saveAndPushNotification(
                            saved.getUser().getId(),
                            "Lịch hẹn bị huỷ",
                            "Bác sĩ " + saved.getDoctor().getUsername() + " đã huỷ lịch hẹn",
                            NotificationType.APPOINTMENT_CANCELLED
                    );
                    default -> throw new IllegalStateException("Vai trò không hợp lệ");
                }
            }
            case COMPLETED -> saveAndPushNotification(
                    saved.getUser().getId(),
                    "Hãy đánh giá bác sĩ",
                    "Cuộc hẹn với bác sĩ " + saved.getDoctor().getUsername() + " đã hoàn tất. Vui lòng để lại nhận xét.",
                    NotificationType.APPOINTMENT_REVIEW_REQUEST
            );
            default -> {}
        }

        return saved;
    }


    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctor(AppointmentStatus status) {
        Account doctor = getCurrentUser();
        List<Appointment> list;

        if (status != null) {
            list = appointmentRepository.findByDoctorAndStatus(doctor, status);
        } else {
            list = appointmentRepository.findByDoctor(doctor);
        }

        return list.stream()
                .map(a -> toResponse(a, status))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByUser(AppointmentStatus status) {
        Account user = getCurrentUser();
        List<Appointment> list;

        if (status != null) {
            list = appointmentRepository.findByUserAndStatus(user, status);
        } else {
            list = appointmentRepository.findByUser(user);
        }

        return list.stream()
                .map(a -> toResponse(a, status))
                .toList();
    }



    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, LocalDate date, LocalTime time) {
        Long actorId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn"));

        if (!appointment.getUser().getId().equals(actorId)) {
            throw new IllegalStateException("Bạn không có quyền đổi lịch hẹn này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Không thể đổi lịch ở trạng thái hiện tại");
        }

        // ❌ Không cho reschedule cuối tuần
        switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> throw new IllegalArgumentException("Không có lịch vào Thứ 7 hoặc Chủ nhật");
        }

        // ✅ Chỉ chấp nhận slot cố định
        if (!FIXED_SLOTS.contains(time)) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ: " + time);
        }

        // ❌ Slot mới đã được đặt rồi
        appointmentRepository.findByDoctorAndAppointmentDateAndAppointmentTime(
                appointment.getDoctor(), date, time
        ).ifPresent(a -> { throw new IllegalStateException("Slot đã được đặt!"); });

        // ✅ Cập nhật thông tin lịch hẹn
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);

        Appointment saved = appointmentRepository.save(appointment);

        // 🔔 Thông báo cho bác sĩ
        saveAndPushNotification(
                saved.getDoctor().getId(),
                "Lịch hẹn được thay đổi",
                "Bệnh nhân " + saved.getUser().getUsername() +
                        " đã thay đổi lịch hẹn sang " + date + " lúc " + time,
                NotificationType.APPOINTMENT_RESCHEDULED
        );

        return saved;
    }

    private AppointmentResponse toResponse(Appointment a, AppointmentStatus status) {
        var detail = doctorDetailRepository.findByAccount_Id(a.getDoctor().getId()).orElse(null);
        var userDetail = userDetailRepository.findByAccount_Id(a.getUser().getId()).orElse(null);

        Long totalReviews = doctorReviewRepository.countByDoctorId(a.getDoctor().getId());
        Number avgObj = doctorReviewRepository.avgRatingByDoctorId(a.getDoctor().getId());
        Double avgRating = avgObj != null ? avgObj.doubleValue() : 0.0;

        Boolean reviewed = null;
        if (status == AppointmentStatus.COMPLETED) {
            reviewed = doctorReviewRepository.existsByAppointmentIdAndUserId(
                    a.getId(), a.getUser().getId()
            );
        }

        return AppointmentResponse.builder()
                .id(a.getId())
                .date(a.getAppointmentDate())
                .time(a.getAppointmentTime())
                .status(a.getStatus())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getUsername())
                .specialization(detail != null ? detail.getSpecialization() : null)
                .doctorAvatarUrl(detail != null ? detail.getAvatar_url() : null)
                .totalReviews(totalReviews)
                .avgRating(avgRating)
                .userId(a.getUser().getId())
                .username(a.getUser().getUsername())
                .userAvatarUrl(userDetail != null ? userDetail.getAvatar_url() : null)
                .reviewed(reviewed)
                .build();
    }

    private Account getCurrentUser() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    private void saveAndPushNotification(Long userId, String title, String body,
                                         NotificationType type) {
        Notification noti = Notification.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(noti);

        var tokens = deviceTokenRepository.findByUserId(userId);
        for (DeviceToken token : tokens) {
            try {
                fcmService.sendNotification(
                        token.getFcmToken(),
                        title,
                        body,
                        Map.of("type", type.name())
                );
            } catch (Exception e) {
                // Log lỗi nhưng không làm fail nghiệp vụ
                System.err.println("❌ Push FCM thất bại: " + e.getMessage());
            }
        }
    }

}
