package com.example.chat.service;

import com.example.chat.dto.res.AppointmentResponse;
import com.example.chat.dto.res.PaymentInfoDto;
import com.example.chat.entity.*;
import com.example.chat.enums.AppointmentStatus;
import com.example.chat.enums.NotificationType;
import com.example.chat.enums.PaymentMethod;
import com.example.chat.firebase.FcmService;
import com.example.chat.integration.zalopay.PaymentService;
import com.example.chat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final DoctorDetailRepository doctorDetailRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final UserDetailRepository userDetailRepository;

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;
    private final PaymentService  paymentService;

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
    public Map<String, Object> bookAppointment(
            Long doctorId,
            LocalDate date,
            LocalTime time,
            PaymentMethod paymentMethod,
            Long fee
    ) {
        // ❌ Không cho phép cuối tuần
        switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> throw new IllegalArgumentException("Không cho phép đặt lịch cuối tuần");
        }

        // ❌ Slot không hợp lệ
        if (!FIXED_SLOTS.contains(time)) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ: " + time);
        }

        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
        Account user = getCurrentUser();

        // ❌ Slot đã có người đặt
        appointmentRepository.findByDoctorAndAppointmentDateAndAppointmentTime(doctor, date, time)
                .ifPresent(a -> { throw new IllegalArgumentException("Slot đã được đặt!"); });

        // ✅ Tạo appointment
        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .user(user)
                .appointmentDate(date)
                .appointmentTime(time)
                .paymentMethod(paymentMethod)
                .fee(fee)
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 🔔 Thông báo cho bác sĩ
        saveAndPushNotification(
                doctor.getId(),
                "Lịch khám mới",
                "Bệnh nhân " + user.getUsername()
                        + " vừa đặt lịch hẹn khám: " + time + " ngày " + date,
                NotificationType.APPOINTMENT_PENDING
        );

        // ✅ Nếu thanh toán qua ZaloPay
        if (saved.getPaymentMethod() == PaymentMethod.ZALOPAY) {
                Map<String, Object> result = paymentService.createZaloPayOrder(saved);
                return Map.of("result", result);
        }

        // Nếu CASH thì không có orderUrl
        return null;
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


        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cuộc hẹn đã hoàn tất, không thể thay đổi trạng thái nữa");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cuộc hẹn đã bị huỷ, không thể thay đổi trạng thái nữa. Vui lòng đặt lịch mới!");
        }

        // ✅ Kiểm tra quyền
        switch (role) {
            case "USER" -> {
                if (!appointment.getUser().getId().equals(actorId)) {
                    throw new IllegalArgumentException("Bạn không có quyền cập nhật lịch này");
                }
                if (newStatus != AppointmentStatus.CANCELLED) {
                    throw new IllegalArgumentException("Người dùng chỉ có thể huỷ lịch");
                }
            }
            case "DOCTOR" -> {
                if (!appointment.getDoctor().getId().equals(actorId)) {
                    throw new IllegalArgumentException("Bạn không có quyền cập nhật lịch này");
                }
            }
            case "ADMIN" -> {
                // admin có toàn quyền
            }
            default -> throw new IllegalArgumentException("Vai trò không hợp lệ");
        }

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);

        // ✅ Thông báo theo trạng thái
        switch (newStatus) {
            case CONFIRMED -> saveAndPushNotification(
                    saved.getUser().getId(),
                    "Lịch hẹn đã được xác nhận",
                    "Bác sĩ " + saved.getDoctor().getUsername()
                            + " đã xác nhận lịch hẹn: " + saved.getAppointmentTime()
                            + " ngày " + saved.getAppointmentDate(),
                    NotificationType.APPOINTMENT_CONFIRMED
            );
            case CANCELLED -> {
                switch (role) {
                    case "USER" -> saveAndPushNotification(
                            saved.getDoctor().getId(),
                            "Lịch hẹn bị huỷ",
                            "Bệnh nhân " + saved.getUser().getUsername() + " đã huỷ lịch hẹn: "
                                    + saved.getAppointmentTime()
                                    + " ngày " + saved.getAppointmentDate(),
                            NotificationType.APPOINTMENT_CANCELLED
                    );
                    case "DOCTOR" -> saveAndPushNotification(
                            saved.getUser().getId(),
                            "Lịch hẹn bị huỷ",
                            "Bác sĩ " + saved.getDoctor().getUsername() + " đã huỷ lịch hẹn: "
                                    + saved.getAppointmentTime()
                                    + " ngày " + saved.getAppointmentDate(),
                            NotificationType.APPOINTMENT_CANCELLED
                    );
                    default -> throw new IllegalStateException("Vai trò không hợp lệ");
                }
            }
            case COMPLETED -> saveAndPushNotification(
                    saved.getUser().getId(),
                    "Hãy đánh giá bác sĩ",
                    "Cuộc hẹn với bác sĩ " + saved.getDoctor().getUsername()
                            + " vào lúc " + saved.getAppointmentTime()
                            + " ngày " + saved.getAppointmentDate()
                            + " đã hoàn tất. Vui lòng để lại nhận xét.",
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
            throw new IllegalArgumentException("Bạn không có quyền đổi lịch hẹn này");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalArgumentException("Không thể đổi lịch ở trạng thái hiện tại");
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

        Transaction tx = transactionRepository.findByAppointmentId(a.getId()).orElse(null);

        PaymentInfoDto paymentInfo = null;

        if (tx != null) {
            paymentInfo = PaymentInfoDto.builder()
                    .paymentStatus(tx.getPaymentStatus())
                    .appTransId(tx.getAppTransId())
                    .zpTransId(tx.getZpTransId())
                    .refundId(tx.getRefundId())
                    .zpRefundId(tx.getZpRefundId())
                    .paymentDate(tx.getPaymentDate())
                    .build();
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
                .paymentMethod(a.getPaymentMethod())
                .amount(a.getFee())
                .paymentInfo(paymentInfo)
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
