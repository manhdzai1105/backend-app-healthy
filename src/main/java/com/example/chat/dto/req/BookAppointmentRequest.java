package com.example.chat.dto.req;

import com.example.chat.enums.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookAppointmentRequest {
    @NotNull(message = "doctorId không được để trống")
    private Long doctorId;

    @NotNull(message = "Ngày khám không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull(message = "Giờ khám không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    // Phí khám phải >= 0
    @NotNull(message = "Phí khám không được để trống")
    @Min(value = 0, message = "Phí khám phải >= 0")
    private Long fee;
}
