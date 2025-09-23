package com.example.chat.dto.req;

import com.example.chat.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateDoctorRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Tên bác sĩ không được để trống")
    private String username;

    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0"
    )
    private String phone;

    private Gender gender;

    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate date_of_birth;

    // ------------ Thông tin DoctorDetail ------------
    @NotBlank(message = "Chuyên khoa không được để trống")
    private String specialization;

    @NotNull(message = "Số năm kinh nghiệm không được để trống")
    @Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
    @Digits(integer = 10, fraction = 0, message = "Số năm kinh nghiệm phải là số nguyên")
    private Integer experience_years;

    @NotBlank(message = "Mô tả không được để trống")
    private String bio;
}
