package com.example.chat.dto.res;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String email;
    private String username;
    private String phone;
    private String gender;
    private String avatar_url;
    private LocalDate date_of_birth;
}
