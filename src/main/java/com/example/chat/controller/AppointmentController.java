package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.req.BookAppointmentRequest;
import com.example.chat.dto.req.RescheduleAppointmentRequest;
import com.example.chat.dto.req.UpdateAppointmentStatusRequest;
import com.example.chat.dto.res.AppointmentResponse;
import com.example.chat.entity.Appointment;
import com.example.chat.enums.AppointmentStatus;
import com.example.chat.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Appointment>> bookAppointment(
            @RequestBody BookAppointmentRequest request
    ) {
        Appointment appointment = appointmentService.bookAppointment(
                request.getDoctorId(),
                request.getDate(),
                request.getTime()
        );

        return ResponseEntity.status(201).body(
                ApiResponse.<Appointment>builder()
                        .code(201)
                        .message("Đặt lịch thành công")
                        .data(appointment)
                        .build()
        );
    }

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<LocalTime>>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<LocalTime> slots = appointmentService.getAvailableSlots(doctorId, date);

        return ResponseEntity.ok(
                ApiResponse.<List<LocalTime>>builder()
                        .code(200)
                        .message("Danh sách slot còn trống")
                        .data(slots)
                        .build()
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Appointment>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateAppointmentStatusRequest request
    ) {
        Appointment appointment = appointmentService.updateStatus(id, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.<Appointment>builder()
                        .code(200)
                        .message("Cập nhật trạng thái thành công")
                        .data(appointment)
                        .build()
        );
    }

    // 📌 Lấy lịch hẹn theo bác sĩ (có thể lọc theo status)
    @GetMapping("/doctor")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDoctor(
            @RequestParam(required = false) AppointmentStatus status
    ) {
        List<AppointmentResponse> data = appointmentService.getAppointmentsByDoctor(status);
        return ResponseEntity.ok(
                ApiResponse.<List<AppointmentResponse>>builder()
                        .code(200)
                        .message("Danh sách lịch hẹn của bác sĩ")
                        .data(data)
                        .build()
        );
    }

    // 📌 Lấy lịch hẹn theo bệnh nhân (có thể lọc theo status)
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByUser(
            @RequestParam(required = false) AppointmentStatus status
    ) {
        List<AppointmentResponse> data = appointmentService.getAppointmentsByUser(status);
        return ResponseEntity.ok(
                ApiResponse.<List<AppointmentResponse>>builder()
                        .code(200)
                        .message("Danh sách lịch hẹn của bệnh nhân")
                        .data(data)
                        .build()
        );
    }


    @PutMapping("/reschedule/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Appointment>> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody RescheduleAppointmentRequest request
    ) {
        Appointment appointment = appointmentService.rescheduleAppointment(
                id,
                request.getDate(),
                request.getTime()
        );

        return ResponseEntity.ok(
                ApiResponse.<Appointment>builder()
                        .code(200)
                        .message("Đổi lịch hẹn thành công")
                        .data(appointment)
                        .build()
        );
    }


}
