package com.example.chat.dto.res;

import com.example.chat.enums.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DoctorResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar_url;
    private String phone_number;
    private Gender gender;
    private LocalDate date_of_birth;
    private String specialization;
    private Integer experience_years;
    private String bio;
    private Long fee;
    private LocalDateTime createdAt;
}
