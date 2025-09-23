package com.example.chat.dto.res;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProfileUserResponse {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}
